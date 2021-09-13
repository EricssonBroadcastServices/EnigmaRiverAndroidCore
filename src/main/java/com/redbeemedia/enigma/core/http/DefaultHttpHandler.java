package com.redbeemedia.enigma.core.http;

import android.os.AsyncTask;

import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultHttpHandler implements IHttpHandler {
    @Override
    public IHttpTask doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {

        HttpJob job = new HttpJob(url, httpCall, responseHandler);
        new AsyncTask<Runnable, Void, Void>() {
            @Override
            protected Void doInBackground(Runnable... runnables) {
                runnables[0].run();
                return null;
            }
        }.execute(job);
        return job;
    }

    protected int getDefaultConnectTimeout() {
        return 0;
    }

    protected int getDefaultReadTimeout() {
        return 0;
    }

    protected long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
        Runnable runnable = new HttpJob(url, httpCall, responseHandler);
        runnable.run();
    }

    private class HttpJob implements Runnable, IHttpTask {
        private URL url;
        private IHttpCall httpCall;
        private IHttpResponseHandler responseHandler;

        private final OpenContainer<Boolean> done = new OpenContainer<>(false);
        private final OpenContainer<Thread> executingThread = new OpenContainer<>(null);

        public HttpJob(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
            this.url = url;
            this.httpCall = httpCall;
            this.responseHandler = responseHandler;
        }

        @Override
        public boolean isDone() {
            return OpenContainerUtil.getValueSynchronized(done);
        }

        @Override
        public void cancel(long joinMillis) {
            if(!isDone()) {
                OpenContainerUtil.setValueSynchronized(executingThread, null, (oldValue, newValue) -> {
                    oldValue.interrupt();
                    try {
                        long now = getCurrentTimeMillis();
                        while(!isDone() && (joinMillis == 0 || getCurrentTimeMillis()-now < joinMillis)) {
                            Thread.sleep(1);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        @Override
        public void run() {
            OpenContainerUtil.setValueSynchronized(executingThread, Thread.currentThread(), null);
            try {
                final HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
                connection.setConnectTimeout(getDefaultConnectTimeout());
                connection.setReadTimeout(getDefaultReadTimeout());
                connection.setDoInput(true);
                connection.setRequestMethod(httpCall.getRequestMethod());
                final String connectionRequestMethod = connection.getRequestMethod();
                if("POST".equalsIgnoreCase(connectionRequestMethod) || "PUT".equalsIgnoreCase(connectionRequestMethod)) {
                    connection.setDoOutput(true); //This value is overridden if supplied by httpCall
                }
                httpCall.prepare(new IHttpConnection() {
                    @Override
                    public void setHeader(String name, String value) {
                        connection.setRequestProperty(name, value);
                    }

                    @Override
                    public void setDoOutput(boolean value) {
                        connection.setDoOutput(value);
                    }

                    @Override
                    public void setDoInput(boolean value) {
                        connection.setDoInput(value);
                    }
                });

                //Do the call
                connection.connect();

                if(Thread.currentThread().isInterrupted()) {
                    return;
                }

                //Send data
                if (connection.getDoOutput()) {
                    OutputStream outputStream = connection.getOutputStream();
                    try {
                        httpCall.writeBodyTo(outputStream);
                    } finally {
                        outputStream.flush();
                        outputStream.close();
                    }
                }

                if(Thread.currentThread().isInterrupted()) {
                    return;
                }

                //Receive data
                HttpStatus responseHttpStatus = new HttpStatus(connection.getResponseCode(), connection.getResponseMessage());

                if(Thread.currentThread().isInterrupted()) {
                    return;
                }

                if(connection.getDoInput()) {
                    InputStream inputStream = responseHttpStatus.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
                    try {
                        if (responseHandler != null) {
                            //This needs to be done synchronously since we are closing the inputStream after.
                            responseHandler.onResponse(responseHttpStatus, inputStream);
                        }
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } else if (responseHandler != null) {
                    responseHandler.onResponse(responseHttpStatus);
                }
            } catch (IOException e) {
                if (responseHandler != null) {
                    responseHandler.onException(e);
                }
            } finally {
                OpenContainerUtil.setValueSynchronized(done, true, null);
            }
        }
    }
}
