/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.uav.mq.api;

import org.slf4j.Logger;
import org.slf4j.Marker;

/***
 * Replace Rocketmq-ClientLogger
 */

public class RMQClientLogger implements Logger {

    @Override
    public String getName() {

        // TODO Auto-generated method stub
        return "uav";
    }

    @Override
    public boolean isTraceEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void trace(String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {

        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDebugEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void debug(String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDebugEnabled(Marker marker) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInfoEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void info(String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInfoEnabled(Marker marker) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWarnEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void warn(String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWarnEnabled(Marker marker) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isErrorEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void error(String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean isErrorEnabled(Marker marker) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String format, Object arg) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {

        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {

        // TODO Auto-generated method stub

    }

}
