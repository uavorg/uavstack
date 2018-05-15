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
public class GroupEntity {
    private String id;
    private String groupid;
    private String ldapkey;
    private String appids;
    private int state;
    private String createtime;
    private String operationtime;
    private String operationuser;
    private String pageindex;
    private String pagesize;

    public String getId() {
    
        return id;
    }

    public void setId(String id) {
    
        this.id = id;
    }

    public String getGroupid() {

        return groupid;
    }

    public void setGroupid(String groupid) {

        this.groupid = groupid;
    }

    public String getLdapkey() {
    
        return ldapkey;
    }

    public void setLdapkey(String ldapkey) {
    
        this.ldapkey = ldapkey;
    }

    public String getAppids() {

        return appids;
    }

    public void setAppids(String appids) {

        this.appids = appids;
    }

    public int getState() {

        return state;
    }

    public void setState(int state) {

        this.state = state;
    }

    public String getCreatetime() {

        return createtime;
    }

    public void setCreatetime(String createtime) {

        this.createtime = createtime;
    }

    public String getOperationtime() {

        return operationtime;
    }

    public void setOperationtime(String operationtime) {

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

}
