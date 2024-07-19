// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.lifecycle;

import com.redbeemedia.enigma.core.util.IHandler;

public interface ILifecycle<StartArgs,StopArgs> {
    boolean addListener(ILifecycleListener<? super StartArgs, ? super StopArgs> listener);
    boolean addListener(ILifecycleListener<? super StartArgs, ? super StopArgs> listener, IHandler handler);
}
