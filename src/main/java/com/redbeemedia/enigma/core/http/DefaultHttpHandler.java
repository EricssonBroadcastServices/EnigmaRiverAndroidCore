package com.redbeemedia.enigma.core.http;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DefaultHttpHandler implements IHttpHandler {
    @Override
    public void doHttp(URL url, IHttpPreparator preparator, IHttpResponseHandler responseHandler) {

        Runnable job = new HttpJob(url, preparator, responseHandler);
        new AsyncTask<Runnable, Void, Void>() {
            @Override
            protected Void doInBackground(Runnable... runnables) {
                runnables[0].run();
                return null;
            }
        }.execute(job);
    }

    private static class HttpJob implements Runnable {
        private URL url;
        private IHttpPreparator preparator;
        private IHttpResponseHandler responseHandler;

        public HttpJob(URL url, IHttpPreparator preparator, IHttpResponseHandler responseHandler) {
            this.url = url;
            this.preparator = preparator;
            this.responseHandler = responseHandler;
        }

        @Override
        public void run() {
            try {
                final HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
                connection.setDoInput(true);
                preparator.prepare(new IHttpConnection() {
                    @Override
                    public void setHeader(String name, String value) {
                        connection.setRequestProperty(name, value);
                    }
                });
                connection.setRequestMethod(preparator.getRequestMethod());
                if("POST".equalsIgnoreCase(connection.getRequestMethod())) {
                    connection.setDoOutput(true); //Maybe the request-method should do this? TODO add Tests for these
                }

                //Do the call
                connection.connect();
                //Send data
                if (connection.getDoOutput()) {
                    OutputStream outputStream = connection.getOutputStream();
                    try {
                        preparator.writeBodyTo(outputStream);
                    } finally {
                        outputStream.flush();
                        outputStream.close();
                    }
                }

                //Recieve data
                HttpStatus responseHttpStatus = new HttpStatus(connection.getResponseCode(), connection.getResponseMessage());
                if(connection.getDoInput()) {
                    InputStream inputStream = responseHttpStatus.code == 200 ? connection.getInputStream() : connection.getErrorStream();
                    try {
                        //TODO do in other thread?
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
