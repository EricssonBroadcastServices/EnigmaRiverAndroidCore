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
                connection.setDoOutput(true); //Maybe the request-method should do this? TODO add Tests for these

                //Do the call
                connection.connect();
                int responseCode = connection.getResponseCode();
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
                if(connection.getDoInput()) {
                    InputStream inputStream = connection.getInputStream();
                    try {
                        //TODO do in other thread?
                        responseHandler.onResponse(responseCode, inputStream);
                    } finally {
                        inputStream.close();
                    }
                } else {
                    responseHandler.onResponse(responseCode);
                }
            } catch (IOException e) {
                //TODO callback to something instead? With error?

                throw new RuntimeException(e);
            }
        }
    }
}
