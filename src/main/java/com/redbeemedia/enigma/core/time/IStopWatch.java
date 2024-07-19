// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.time;

public interface IStopWatch {
    void start();
    Duration stop();
    Duration readTime();
}
