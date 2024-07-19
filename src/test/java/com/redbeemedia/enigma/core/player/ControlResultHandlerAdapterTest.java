// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

import org.junit.Assert;
import org.junit.Test;

public class ControlResultHandlerAdapterTest {
    @Test
    public void testRunWhenDoneAddedBefore() {
        final StringBuilder log = new StringBuilder();
        IControlResultHandler wrapped = new IControlResultHandler() {
            @Override
            public void onRejected(IRejectReason reason) {
                Assert.fail("Should not have been called");
            }

            @Override
            public void onCancelled() {
                Assert.fail("Should not have been called");
            }

            @Override
            public void onError(EnigmaError error) {
                Assert.fail("Should not have been called");
            }

            @Override
            public void onDone() {
                log.append("[onDone]");
            }
        };
        ControlResultHandlerAdapter handlerAdapter = new ControlResultHandlerAdapter(wrapped);
        handlerAdapter.runWhenDone(new Runnable() {
            @Override
            public void run() {
                log.append("[run]");
            }
        });
        Assert.assertEquals(0, log.toString().length());
        handlerAdapter.onDone();
        Assert.assertEquals("[onDone][run]", log.toString());
        handlerAdapter.onDone();
        Assert.assertEquals("[onDone][run][onDone]", log.toString());
    }

    @Test
    public void testRunWhenDoneAddedAfter() {
        final StringBuilder log = new StringBuilder();
        IControlResultHandler wrapped = new IControlResultHandler() {
            @Override
            public void onRejected(IRejectReason reason) {
                Assert.fail("Should not have been called");
            }

            @Override
            public void onCancelled() {
                Assert.fail("Should not have been called");
            }

            @Override
            public void onError(EnigmaError error) {
                Assert.fail("Should not have been called");
            }

            @Override
            public void onDone() {
                log.append("[onDone]");
            }
        };
        ControlResultHandlerAdapter handlerAdapter = new ControlResultHandlerAdapter(wrapped);
        Assert.assertEquals(0, log.toString().length());
        handlerAdapter.onDone();
        Assert.assertEquals("[onDone]", log.toString());
        handlerAdapter.runWhenDone(new Runnable() {
            @Override
            public void run() {
                log.append("[run]");
            }
        });
        Assert.assertEquals("[onDone][run]", log.toString());
        handlerAdapter.onDone();
        Assert.assertEquals("[onDone][run][onDone]", log.toString());
    }
}
