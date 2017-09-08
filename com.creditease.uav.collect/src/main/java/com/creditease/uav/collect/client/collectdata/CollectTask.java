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

package com.creditease.uav.collect.client.collectdata;

public class CollectTask {

    private String target;
    private String action;
    private String file;
    private String filter;
    private String topic;
    private boolean unsplit;

    private String ctxid;
    private boolean eofEvent;
    private boolean eofOccur;

    public boolean hasEofEvent() {

        return eofEvent;
    }

    public String getTarget() {

        return target;
    }

    public void setTarget(String target) {

        this.target = target;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public String getFile() {

        return file;
    }

    public void setFile(String file) {

        this.file = file;
    }

    public String getFilter() {

        return filter;
    }

    public void setFilter(String filter) {

        this.filter = filter;
    }

    public String getTopic() {

        return topic;
    }

    public void setTopic(String topic) {

        this.topic = topic;
    }

    public boolean isUnsplit() {

        return unsplit;
    }

    public void setUnsplit(boolean unsplit) {

        this.unsplit = unsplit;
    }

    public boolean isEofEvent() {

        return eofEvent;
    }

    public void setEofEvent(boolean eofEvent) {

        this.eofEvent = eofEvent;
    }

    public String getCtxid() {

        return ctxid;
    }

    public void setCtxid(String ctxid) {

        this.ctxid = ctxid;
    }

    public boolean isEofOccur() {

        return eofOccur;
    }

    public void setEofOccur(boolean eofOccur) {

        this.eofOccur = eofOccur;
    }

}
