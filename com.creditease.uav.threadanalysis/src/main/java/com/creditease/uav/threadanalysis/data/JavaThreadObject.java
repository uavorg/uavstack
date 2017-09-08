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

package com.creditease.uav.threadanalysis.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 线程分析pojo对象
 * 
 * @author xinliang
 *
 */
public class JavaThreadObject {

    /**
     * 新增属性
     */
    private String pname = "";// 进程的名字
    private String ipport = "";// <ip>:<port>，用来区分宿主机
    private String pid = "";// 线程分析的进程号
    private String appgroup = "";// 用户组
    private long time;// 开始线程分析的时间
    private String user = "";// 启动的线程分析的用户

    /**
     * getter and setter
     */
    public String getPname() {

        return pname;
    }

    public void setPname(String pname) {

        this.pname = pname;
    }

    public String getIpport() {

        return ipport;
    }

    public void setIpport(String ipport) {

        this.ipport = ipport;
    }

    public String getPid() {

        return pid;
    }

    public void setPid(String pid) {

        this.pid = pid;
    }

    public String getAppgroup() {

        return appgroup;
    }

    public void setAppgroup(String appgroup) {

        this.appgroup = appgroup;
    }

    public long getTime() {

        return time;
    }

    public void setTime(long time) {

        this.time = time;
    }

    public String getUser() {

        return user;
    }

    public void setUser(String user) {

        this.user = user;
    }

    // 线程号
    private String tid;

    // cpu占比
    private double percpu;

    // 内存占比
    private double permem;

    // 线程已运行时间
    private String timeadd;

    // 线程状态
    private String state;

    // 线程栈信息
    private String info;

    public String getTid() {

        return tid;
    }

    public void setTid(String tid) {

        this.tid = tid;
    }

    public double getPercpu() {

        return percpu;
    }

    public void setPercpu(double percpu) {

        this.percpu = percpu;
    }

    public double getPermem() {

        return permem;
    }

    public void setPermem(double permem) {

        this.permem = permem;
    }

    public String getTimeadd() {

        return timeadd;
    }

    public void setTimeadd(String timeadd) {

        this.timeadd = timeadd;
    }

    public String getState() {

        return state;
    }

    public void setState(String state) {

        this.state = state;
    }

    public String getInfo() {

        return info;
    }

    public void setInfo(String info) {

        this.info = info;
    }

    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();

        m.put("pname", this.pname);
        m.put("ipport", this.ipport);
        m.put("pid", this.pid);
        m.put("appgroup", this.appgroup);
        m.put("time", this.time);

        m.put("tid", this.tid);
        m.put("percpu", this.percpu);
        m.put("permem", this.permem);
        m.put("timeadd", this.timeadd);
        m.put("state", this.state);

        m.put("info", this.info);
        m.put("user", this.user);
        return m;
    }
}
