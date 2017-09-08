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

package com.creditease.uav.manage.rest.entity;

/**
 * @author Created by lbay on 2016/4/14.
 */
public class AppEntity {

    private String appid;
    private String appurl;
    private String configpath;
    private int state;
    private long createtime;
    private long operationtime;
    private String operationuser;
    private String pageindex;
    private String pagesize;

    public String getAppid() {

        return appid;
    }

    public void setAppid(String appid) {

        this.appid = appid;
    }

    public String getAppurl() {

        return appurl;
    }

    public void setAppurl(String appurl) {

        this.appurl = appurl;
    }

    public String getConfigpath() {

        return null == configpath ? "" : configpath;
    }

    public void setConfigpath(String configpath) {

        this.configpath = configpath;
    }

    public int getState() {

        return state;
    }

    public void setState(int state) {

        this.state = state;
    }

    public long getCreatetime() {

        return createtime;
    }

    public void setCreatetime(long createtime) {

        this.createtime = createtime;
    }

    public long getOperationtime() {

        return operationtime;
    }

    public void setOperationtime(long operationtime) {

        this.operationtime = operationtime;
    }

    public String getOperationuser() {

        return operationuser;
    }

    public void setOperationuser(String operationuser) {

        this.operationuser = operationuser;
    }

    public String getPageindex() {

        return pageindex;
    }

    public void setPageindex(String pageindex) {

        this.pageindex = pageindex;
    }

    public String getPagesize() {

        return pagesize;
    }

    public void setPagesize(String pagesize) {

        this.pagesize = pagesize;
    }

    @Override
    public String toString() {

        return "AppEntity [appid=" + appid + ", appurl=" + appurl + ", state=" + state + ", createtime=" + createtime
                + ", operationtime=" + operationtime + ", operationuser=" + operationuser + ", pageindex=" + pageindex
                + ", pagesize=" + pagesize + "]";
    }

}
