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

package com.creditease.uav.threadanalysis.server.da;

public class MonitorObject {

    public enum MonitorState {
        LOCKING, BLOCKING, WAITING, PARKING, UNKNOWN
    }

    private MonitorState state;
    private String id;
    private String obj;
    private ThreadObject lockedBy;

    public MonitorState getState() {

        return state;
    }

    public void setState(MonitorState state) {

        this.state = state;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getObj() {

        return obj;
    }

    public void setObj(String obj) {

        this.obj = obj;
    }

    public ThreadObject getLockedBy() {

        return lockedBy;
    }

    public void setLockedBy(ThreadObject lockedBy) {

        this.lockedBy = lockedBy;
    }

}
