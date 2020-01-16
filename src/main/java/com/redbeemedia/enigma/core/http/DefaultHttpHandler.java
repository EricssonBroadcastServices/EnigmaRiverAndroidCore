package com.redbeemedia.enigma.core.http;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultHttpHandler implements IHttpHandler {
    @Override
    public void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {

        Runnable job = new HttpJob(url, httpCall, responseHandler);
        new AsyncTask<Runnable, Void, Void>() {
            @Override
            protected Void doInBackground(Runnable... runnables) {
                runnables[0].run();
                return null;
            }
        }.execute(job);
    }

    protected int getDefaultConnectTimeout() {
        return 0;
    }

    protected int getDefaultReadTimeout() {
        return 0;
    }

    @Override
    public void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
        Runnable runnable = new HttpJob(url, httpCall, responseHandler);
        runnable.run();
    }

    private class HttpJob implements Runnable {
        private URL url;
        private IHttpCall httpCall;
        private IHttpResponseHandler responseHandler;

        public HttpJob(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
            this.url = url;
            this.httpCall = httpCall;
            this.responseHandler = responseHandler;
        }

        @Override
        public void run() {
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

                //Recieve data
                HttpStatus responseHttpStatus = new HttpStatus(connection.getResponseCode(), connection.getResponseMessage());
                if(connection.getDoInput()) {
                    InputStream inputStream = responseHttpStatus.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
                    try {
                        //This needs to be done synchronously since we are closing the inputStream after.
                        responseHandler.onResponse(responseHttpStatus, inputStream);
                    } finally {
                        inputStream.close();
                    }
                } else {
                    responseHandler.onResponse(responseHttpStatus);
                }
            } catch (IOException e) {
                responseHandler.onException(e);
            }
        }
    }
}
