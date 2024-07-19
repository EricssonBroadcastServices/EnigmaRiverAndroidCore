// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg.impl;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.request.IEpgRequest;
import com.redbeemedia.enigma.core.epg.response.IEpgResponseHandler;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.InternalError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.http.ExposureApiCall;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonReaderResponseHandler;
import com.redbeemedia.enigma.core.playable.AssetPlayable;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.ISO8601Util;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.error.EnigmaErrorException;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class DirectEpg extends AbstractEpg {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private final IBusinessUnit businessUnit;

    public DirectEpg(IBusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    @Override
    public void getPrograms(IEpgRequest request, IEpgResponseHandler responseHandler) {
        long middle = (request.getFromUtcMillis()+request.getToUtcMillis())/2L;
        String date;
        synchronized (dateFormatter) {
            date = dateFormatter.format(new Date(middle));
        }

        long daysBackwards = (long) Math.ceil(Duration.millis(middle-request.getFromUtcMillis()).inUnits(Duration.Unit.DAYS));
        long daysForward = (long) Math.ceil(Duration.millis(request.getToUtcMillis()-middle).inUnits(Duration.Unit.DAYS));

        UrlPath urlPath = businessUnit.getApiBaseUrl("v2").append("epg/").append(request.getChannelId()).append("/date").append(date).append("?daysBackward=").append(String.valueOf(daysBackwards)).append("&daysForward=").append(String.valueOf(daysForward));

        PageFetcher pageFetcher = new PageFetcher(10000); //The backend has a limit on 10000 for pageSize at the time of writing
        try {
            pageFetcher.getAllPages(EnigmaRiverContext.getHttpHandler(), urlPath, new ExposureApiCall("GET"), new PageFetcher.IPageResults() {
                private final ISO8601Util.IISO8601Parser iso8601Parser = ISO8601Util.newParser();
                private Map<String, List<IProgram>> results = new HashMap<>();

                @Override
                public int onSuccess(int page, JsonReader jsonReader) throws Exception {
                    List<ApiChannelEPGResponse> partialResults = new ArrayList<>();

                    switch (jsonReader.peek()) {
                        case BEGIN_ARRAY: {
                            jsonReader.beginArray();
                            while(jsonReader.hasNext()) {
                                partialResults.add(new ApiChannelEPGResponse(jsonReader));
                            }
                            jsonReader.endArray();
                        } break;
                        case BEGIN_OBJECT: {
                            partialResults.add(new ApiChannelEPGResponse(jsonReader));
                        } break;
                        default: throw new JSONException("Expected array or object");
                    }


                    for(ApiChannelEPGResponse partialResult : partialResults) {
                        String channelId = partialResult.getChannelId();
                        if(channelId == null) {
                            throw new EnigmaErrorException(new InternalError("No channel returned from backend id"));
                        }
                        List<IProgram> programs = results.get(channelId);
                        if(programs == null) {
                            programs = new ArrayList<>();
                            results.put(channelId, programs);
                        }
                        for(ApiProgramResponse program : partialResult.getPrograms()) {
                            String programId = program.getProgramId();
                            String assetId = program.getAssetId();
                            long startUtcMillis = iso8601Parser.parse(program.getStartTime());
                            long endUtcMillis = iso8601Parser.parse(program.getEndTime());
                            programs.add(new Program(programId, assetId, startUtcMillis, endUtcMillis));
                        }
                    }

                    return partialResults.isEmpty() ? 0 : partialResults.get(0).getTotalHitsAllChannels();
                }

                @Override
                public void onError(int page, EnigmaError error) {
                    responseHandler.onError(new InternalError("Error while parsing page "+page, error));
                }

                @Override
                public void onAllSuccess() {
                    List<IProgram> programs = results.get(request.getChannelId());
                    if(programs == null) {
                        programs = Collections.emptyList();
                    }
                    responseHandler.onSuccess(new EpgResponse(programs, request.getFromUtcMillis(), request.getToUtcMillis()));
                }
            });
        } catch (MalformedURLException e) {
            responseHandler.onError(new UnexpectedError(e));
            return;
        }
    }



    private static class PageFetcher {
        private final int pageSize;

        public PageFetcher(int pageSize) {
            this.pageSize = pageSize;
        }

        public void getAllPages(IHttpHandler httpHandler, UrlPath url, IHttpCall call, PageFetcher.IPageResults pageResults) throws MalformedURLException {
            getPage(1, httpHandler, url, call, pageResults, new BooleanPointer(false));
        }

        public void getPage(final int page, IHttpHandler httpHandler, UrlPath url, IHttpCall call, PageFetcher.IPageResults pageResults, final BooleanPointer gotException) throws MalformedURLException {
            httpHandler.doHttp(url.append("&pageSize=" + pageSize).append("&pageNumber="+page).toURL(), call, new JsonReaderResponseHandler() {
                @Override
                protected void onSuccess(JsonReader jsonReader) {
                    int totalHitsAllChannels = 0;
                    try {
                        totalHitsAllChannels = pageResults.onSuccess(page, jsonReader);
                    } catch (Exception e) {
                        EnigmaError error = new UnexpectedError(e);
                        if(e instanceof EnigmaErrorException) {
                            error = ((EnigmaErrorException) e).getError();
                        }
                        pageResults.onError(page, error);
                        gotException.set(true);
                    }

                    if(totalHitsAllChannels - (pageSize * page) > 0) {
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
                protected void onError(EnigmaError error) {
                    pageResults.onError(page, error);
                }
            });
        }

        public interface IPageResults {
            int onSuccess(int page, JsonReader jsonReader) throws Exception;
            void onError(int page, EnigmaError error);
            void onAllSuccess();
        }
    }

    private static class Program implements IProgram {
        private final String programId;
        private final String assetId;
        private final long startUtcMillis;
        private final long endUtcMillis;
        private final Duration duration;
        private final IPlayable playable;

        public Program(String programId, String assetId, long startUtcMillis, long endUtcMillis) {
            this.programId = programId;
            this.assetId = assetId;
            this.startUtcMillis = startUtcMillis;
            this.endUtcMillis = endUtcMillis;
            this.duration = Duration.millis(endUtcMillis-startUtcMillis);
            this.playable = new AssetPlayable(assetId);
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
        public IPlayable getPlayable() {
            return playable;
        }

        @Override
        public String toString() {
            return "{programId="+programId+", assetId="+assetId+"}";
        }

        @Override
        public String getAssetId() {
            return assetId;
        }

        @Override
        public String getProgramId() {
            return programId;
        }
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
