package com.redbeemedia.enigma.core.context;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.http.ExposureApiCall;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonReaderResponseHandler;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*package-protected*/ class DefaultEpg implements IEpg {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private final OpenContainer<List<Runnable>> pendingRequests = new OpenContainer<>(new ArrayList<>());
    private final OpenContainer<Boolean> loaded = new OpenContainer<>(Boolean.FALSE);
    private final OpenContainer<Map<String, OpenContainer<List<IProgram>>>> epgData = new OpenContainer<>(new HashMap<>());

    private static class PageFetcher {
        private final int pageSize;

        public PageFetcher(int pageSize) {
            this.pageSize = pageSize;
        }

        public void getAllPages(IHttpHandler httpHandler, UrlPath url, IHttpCall call, IPageResults pageResults) throws MalformedURLException {
            getPage(1, httpHandler, url, call, pageResults, new BooleanPointer(false));
        }

        public void getPage(final int page, IHttpHandler httpHandler, UrlPath url, IHttpCall call, IPageResults pageResults, final BooleanPointer gotException) throws MalformedURLException {
            httpHandler.doHttp(url.append("&pageSize=" + pageSize).append("&pageNumber="+page).toURL(), call, new JsonReaderResponseHandler() {
                @Override
                protected void onSuccess(JsonReader jsonReader) {
                    int totalHitsAllChannels = 0;
                    try {
                        totalHitsAllChannels = pageResults.onSuccess(page, jsonReader);
                    } catch (JSONException e) {
                        pageResults.onError(page, new UnexpectedError(e));
                        gotException.set(true);
                    }

                    if(totalHitsAllChannels > 0) {
                        try {
                            getPage(page+1, httpHandler, url, call, pageResults, gotException);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    } else if(!gotException.get()) {
                        pageResults.onAllSuccess();
                    }
                }

                @Override
                protected void onError(Error error) {
                    pageResults.onError(page, error);
                }
            });
        }

        public interface IPageResults {
            int onSuccess(int page, JsonReader jsonReader) throws JSONException;
            void onError(int page, Error error);
            void onAllSuccess();
        }
    }

    @Override
    public void loadData(IBusinessUnit businessUnit, long utcMillis, int daysBack, int daysForward) throws MalformedURLException {
        String date;
        synchronized (dateFormatter) {
            date = dateFormatter.format(new Date(utcMillis));
        }

        UrlPath url = businessUnit.getApiBaseUrl("v2").append("epg/date").append(date).append("?daysBackward=").append(String.valueOf(daysBack)).append("&daysForward=").append(String.valueOf(daysForward));

        PageFetcher pageFetcher = new PageFetcher(10000);

        pageFetcher.getAllPages(EnigmaRiverContext.getHttpHandler(), url, new ExposureApiCall("GET"), new PageFetcher.IPageResults() {
            @Override
            public int onSuccess(int page, JsonReader jsonReader) throws JSONException {
                IResultParser resultParser = new ResultParser(newISO8601Parser());
                try {
                    resultParser.parse(jsonReader);
                } catch (IOException e) {
                    onError(page, new UnexpectedError(e));
                }
                return  resultParser.getTotalHitsAllChannels();
            }


            @Override
            public void onError(int page, Error error) {
                if(error instanceof UnexpectedHttpStatusError) {
                    HttpStatus status = ((UnexpectedHttpStatusError) error).getHttpStatus();
                    throw new RuntimeException(error.getClass().getSimpleName()+" "+status+" "+"("+error.getErrorCode()+") :"+error.getTrace());
                }
                throw new RuntimeException(error.getClass().getSimpleName()+"("+error.getErrorCode()+") :"+error.getTrace());
            }

            @Override
            public void onAllSuccess() {
                synchronized (loaded) {
                    loaded.value = true;
                }
                synchronized (pendingRequests) {
                    for(Runnable request : pendingRequests.value) {
                        request.run();
                    }
                    pendingRequests.value.clear();
                }
            }
        });
    }

    private OpenContainer<List<IProgram>> getChannelList(String channelId) {
        OpenContainer<List<IProgram>> channelProgramList;
        synchronized (epgData) {
            channelProgramList = epgData.value.get(channelId);
            if(channelProgramList == null) {
                channelProgramList = new OpenContainer<>(new ArrayList<>());
                epgData.value.put(channelId, channelProgramList);
            }
        }
        return channelProgramList;
    }

    @Override
    public void getPrograms(String channelId, long fromMillis, long toMillis, IProgramListRequestResultHandler resultHandler) {
        Runnable request = new Runnable() {
            @Override
            public void run() {
                try {
                    OpenContainer<List<IProgram>> programs = getChannelList(channelId);
                    List<IProgram> result = new ArrayList<>();

                    synchronized (programs) {
                        for(IProgram program : programs.value) {
                            if(program.getEndUtcMillis() > fromMillis && program.getStartUtcMillis() < toMillis) {
                                result.add(program);
                            }
                        }
                    }
                    resultHandler.onList(result);
                } catch(Exception e) {
                    resultHandler.onError(new UnexpectedError(e));
                }
            }
        };

        boolean runNow = false;
        synchronized (loaded) {
            if(!loaded.value.booleanValue()) {
                synchronized (pendingRequests) {
                    pendingRequests.value.add(request);
                }
            } else {
                runNow = true;
            }
        }
        if(runNow) {
            request.run();
        }
    }

    private interface IResultParser {
        void parse(JsonReader jsonReader) throws IOException, JSONException;
        int getTotalHitsAllChannels();
    }

    private class ResultParser implements IResultParser {
        private static final String START_TIME = "startTime";
        private static final String END_TIME = "endTime";
        private static final String CHANNEL_ID = "channelId";
        private final IISO8601Parser timeParser;
        private int totalHitsAllChannels = -1;

        public ResultParser(IISO8601Parser timeParser) {
            this.timeParser = timeParser;
        }

        @Override
        public int getTotalHitsAllChannels() {
            return totalHitsAllChannels;
        }

        @Override
        public void parse(JsonReader jsonReader) throws IOException, JSONException {
            jsonReader.beginArray();
            while(jsonReader.hasNext()) {
                onChannelEPGResponse(jsonReader);
            }
            jsonReader.endArray();
        }

        private void onChannelEPGResponse(JsonReader channelEPGResponse) throws IOException, JSONException {
            String channelId = null;
            List<Program> parsedPrograms = new ArrayList<>();
            channelEPGResponse.beginObject();
            while (channelEPGResponse.hasNext()) {
                switch (channelEPGResponse.nextName()) {
                    case CHANNEL_ID: {
                        channelId = channelEPGResponse.nextString();
                    } break;
                    case "programs": {
                        channelEPGResponse.beginArray();
                        while (channelEPGResponse.hasNext()) {
                            parsedPrograms.add(onProgram(channelEPGResponse));
                        }
                        channelEPGResponse.endArray();
                    } break;
                    case "totalHitsAllChannels": {
                        int parsedTotalHitsAllChannels = channelEPGResponse.nextInt();
                        if(totalHitsAllChannels == -1) {
                            totalHitsAllChannels = parsedTotalHitsAllChannels;
                        }
                    } break;
                    default: channelEPGResponse.skipValue();
                }
            }
            channelEPGResponse.endObject();

            if(channelId == null) {
                throw new JSONException("Missing attribute "+CHANNEL_ID);
            }
            OpenContainer<List<IProgram>> channelProgramList = getChannelList(channelId);
            synchronized (channelProgramList) {
                channelProgramList.value.addAll(parsedPrograms);
            }
        }

        private Program onProgram(JsonReader program) throws IOException, JSONException {
            Long startTimeMillis = null;
            Long endTimeMillis = null;
            String assetId = null;

            program.beginObject();
            while(program.hasNext()) {
                switch (program.nextName()) {
                    case START_TIME: {
                        startTimeMillis = timeParser.parse(program.nextString());
                    } break;
                    case END_TIME: {
                        endTimeMillis = timeParser.parse(program.nextString());
                    } break;
                    case "asset": {
                        assetId = getAssetId(program);
                    } break;
                    default: program.skipValue();
                }
            }
            program.endObject();

            if(startTimeMillis == null) {
                throw new JSONException("Missing attribute "+START_TIME);
            }
            if(endTimeMillis == null) {
                throw new JSONException("Missing attribute "+END_TIME);
            }
            if(assetId == null) {
                assetId = "N/A";
            }

            return new Program(assetId, startTimeMillis, endTimeMillis);
        }

        private String getAssetId(JsonReader asset) throws IOException {
            String assteId = null;
            asset.beginObject();
            while(asset.hasNext()) {
                switch (asset.nextName()) {
                    case "assetId": {
                        assteId = asset.nextString();
                    } break;
                    default: asset.skipValue();
                }
            }
            asset.endObject();
            return assteId;
        }
    }


    private static class Program implements IProgram {
        private final String name;
        private final long startUtcMillis;
        private final long endUtcMillis;
        private final Duration duration;

        public Program(String name, long startUtcMillis, long endUtcMillis) {
            this.name = name;
            this.startUtcMillis = startUtcMillis;
            this.endUtcMillis = endUtcMillis;
            this.duration = Duration.millis(endUtcMillis-startUtcMillis);
        }

        @Override
        public Duration getDuration() {
            return duration;
        }

        @Override
        public long getStartUtcMillis() {
            return startUtcMillis;
        }

        @Override
        public long getEndUtcMillis() {
            return endUtcMillis;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static IISO8601Parser newISO8601Parser() {
        return new IISO8601Parser() {
            private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            @Override
            public long parse(String iso8601String) {
                try {
                    return sdf.parse(iso8601String).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                    return -1L;
                }
            }
        };
    }

    private interface IISO8601Parser {
        long parse(String iso8601String);
    }

    private static class BooleanPointer {
        private boolean value;

        public BooleanPointer(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }
}
