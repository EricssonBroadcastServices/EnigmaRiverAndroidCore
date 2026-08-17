/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.IBufferingAnalyticsHandler;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;

public class AnalyticsReportingTask implements IAnalyticsReportingTask {
    final ITask analyticsHandlerTask;
    final ITaskFactory taskFactory;
    final IBufferingAnalyticsHandler analyticsHandler;
    public AnalyticsReportingTask(ITaskFactory taskFactory, IBufferingAnalyticsHandler analyticsHandler, int updateFrequency) {
        analyticsHandlerTask = taskFactory.newTask(newAnalyticsHandlerRunnable(analyticsHandler, updateFrequency));
        this.taskFactory = taskFactory;
        this.analyticsHandler = analyticsHandler;
    }

    @Override
    public void start() {
        try {
            analyticsHandlerTask.start();
        } catch (TaskException e) {
            throw new RuntimeException("Could not start analyticsHandlerTask", e);
        }
    }

    @Override
    public void stop() {
        try {
            analyticsHandlerTask.cancel(500);
        } catch (Exception e) {
            e.printStackTrace(); //Suppress
        }
        ITask sendRemainingDataTask = taskFactory.newTask(() -> {
            try {
                analyticsHandler.sendData(); //Try to send any remaining data.
            } catch (Exception e) {
                e.printStackTrace(); //Suppress
            }
        });
        try {
            sendRemainingDataTask.start();
        } catch (Exception e) {
            e.printStackTrace(); //Suppress
        }
    }

    private Runnable newAnalyticsHandlerRunnable(IBufferingAnalyticsHandler analyticsHandler, long updateFrequency) {
        return new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        try {
                            analyticsHandler.sendData();
                            Thread.sleep(updateFrequency);
                        } catch (AnalyticsException e) {
                            handleException(e);
                            Thread.sleep(updateFrequency);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            private void handleException(Exception e) {
                if (BuildConfig.DEBUG) {
                    throw new RuntimeException(e);
                } else {
                    e.printStackTrace(); //Log
                }
            }
        };
    }
}
