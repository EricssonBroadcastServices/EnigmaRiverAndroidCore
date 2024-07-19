package com.redbeemedia.enigma.core.http;

import android.os.Process;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Copyright (c) 2018 Ericsson. All Rights Reserved
// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

public class KeepAliveHttpHandler implements IHttpHandler {

    private final ExecutorService mService;

    public static ExecutorService createDefaultExecutorService() {
        final ThreadFactory factory = new ThreadFactory() {

            private final AtomicInteger COUNTER = new AtomicInteger(0);

            @Override
            public Thread newThread(final Runnable r) {
                final int threadCount = COUNTER.getAndIncrement();

                final Thread thread = new BackgroundThread(r, "KeepAlive thread " + threadCount);
                thread.setDaemon(false);

                return thread;
            }
        };

        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, factory);
    }

    public KeepAliveHttpHandler() {
        this(createDefaultExecutorService());
    }

    public KeepAliveHttpHandler(final ExecutorService mService) {
        this.mService = mService;
    }

    @Override // Maybe this should return a Future? https://developer.android.com/reference/java/util/concurrent/Future
    public IHttpTask doHttp(final URL url,
                       final IHttpCall httpCall,
                       final IHttpResponseHandler responseHandler)
    {
        final URLConnectionRunnable runnable = new URLConnectionRunnable(url, httpCall, responseHandler);
        Future<?> future = mService.submit(runnable);
        return new IHttpTask() {
            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public void cancel(long joinMillis) {
                long start = getCurrentTimeMillis();

                future.cancel(true);

                while (!OpenContainerUtil.getValueSynchronized(runnable.done) && (joinMillis == 0 || (getCurrentTimeMillis()-start) <= joinMillis)) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
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
        URLConnectionRunnable runnable = new URLConnectionRunnable(url, httpCall, responseHandler);
        runnable.run();
    }

    private final class URLConnectionRunnable implements Runnable {
        private final URL mURL;
        private final IHttpCall mCall;
        private final IHttpResponseHandler mHandler;

        private final OpenContainer<Boolean> done = new OpenContainer<>(false);

        public URLConnectionRunnable(final URL url,
                                     final IHttpCall call,
                                     final IHttpResponseHandler handler)
        {
            this.mURL = url;
            this.mCall = call;
            this.mHandler = handler;
        }

        @Override
        public void run() {
            final Thread currentThread = Thread.currentThread();

            try {
                final HttpURLConnection urlConnection = (HttpURLConnection) mURL.openConnection();
                urlConnection.setRequestProperty(
                        "User-agent",
                        EnigmaRiverContext.getAppName()
                                + "/"
                                + EnigmaRiverContext.getAppVersion()
                                + " "
                                + System.getProperty("http.agent")
                );
                final String method = mCall.getRequestMethod();

                urlConnection.setRequestMethod(method);
                urlConnection.setConnectTimeout(getDefaultConnectTimeout());
                urlConnection.setReadTimeout(getDefaultReadTimeout());

                final boolean doOutput = "POST".equals(method) || "PUT".equals(method);
                urlConnection.setDoOutput(doOutput);

                mCall.prepare(new IHttpConnection() {
                    @Override
                    public void setHeader(final String name,
                                          final String value)
                    {
                        urlConnection.setRequestProperty(name, value);
                    }

                    @Override
                    public void setDoOutput(boolean value) {
                        urlConnection.setDoOutput(value);
                    }

                    @Override
                    public void setDoInput(boolean value) {
                        urlConnection.setDoInput(value);
                    }
                });

                try {
                    if (currentThread.isInterrupted()) {
                        return;
                    }

                    if (doOutput) {
                        mCall.writeBodyTo(urlConnection.getOutputStream());
                    } else {
                        // Disables Nagle's
                        urlConnection.setFixedLengthStreamingMode(0);
                    }

                    if (currentThread.isInterrupted()) {
                        return;
                    }

                    final int responseCode = urlConnection.getResponseCode();
                    final String responseMessage = urlConnection.getResponseMessage();

                    final HttpStatus status = new HttpStatus(responseCode, responseMessage);

                    if (currentThread.isInterrupted()) {
                        return;
                    }

                    final InputStream inputStream;
                    if (status.isError()) {
                        inputStream = urlConnection.getErrorStream();
                    } else {
                        inputStream = urlConnection.getInputStream();
                    }

                    final InputStream bufferedStream = new BufferedInputStream(inputStream);

                    mHandler.onResponse(status, bufferedStream);

                    /// Robin Galmin - 2018.11.30
                    // Empty InputStream to be able to reuse it.
                    // If the IHttpResponseHandler has not read the whole thing.
                    // if (inputStream.available() > 0) {
                    //    final byte[] buffer = new byte[8 * 1024];
                    //    while (inputStream.read(buffer) > -1) {}
                    // }
                    /// Robin Galmin - 2018.12.03
                    // Because we don't know what is done to the InputStream in the
                    // onResponse method I think it is to unstable to have this
                    // I think it is better to let the application developer empty
                    // the InputStream and the times they don't the socket will not
                    // be able to be reused.

                } finally {
                    // Disconnect instead of close on InputStream so that HttpURLConnection
                    // can keep the socket alive.
                    // https://developer.android.com/reference/java/net/HttpURLConnection
                    urlConnection.disconnect();
                }

            } catch (final IOException e) {
                mHandler.onException(e);
            } finally {
                OpenContainerUtil.setValueSynchronized(done, true, null);
            }
        }
    }

    private static final class BackgroundThread extends Thread {

        public BackgroundThread(final Runnable runnable,
                                final String name)
        {
            super(runnable, name);
        }

        @Override
        public void run() {
            // Set background priority to not disturb the UI.
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            super.run();
        }
    }
}
