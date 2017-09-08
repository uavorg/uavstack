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

package com.creditease.uav.threadanalysis.server.collect;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;

import com.creditease.agent.apm.api.AbstractCollectDataHandler;
import com.creditease.agent.apm.api.CollectDataFrame;
import com.creditease.agent.apm.api.CollectDataFrame.Line;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.threadanalysis.data.JavaThreadObject;
import com.creditease.uav.threadanalysis.server.ThreadAnalysisIndexMgr;

/**
 * 线程分析文件消费者类
 * 
 * @author xinliang
 *
 */
public class ThreadAnalysisCollectDataHandler extends AbstractCollectDataHandler {

    private ESClient client;

    private ThreadAnalysisIndexMgr indexMgr;

    public ThreadAnalysisCollectDataHandler(String cName, String feature) {
        super(cName, feature);
        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
        indexMgr = (ThreadAnalysisIndexMgr) this.getConfigManager().getComponent(this.feature,
                "ThreadAnalysisIndexMgr");

    }

    /**
     * 数据归集feature消费者消息接收方法
     */
    @Override
    public void handle(CollectDataFrame frame) {

        if (this.log.isDebugEnable()) {
            this.log.debug(this, frame.toJSONString());
        }
        // 线程分析文件按次生成，一次采集完，应该在一个 CollectDataFrame 中
        if (!frame.isEof()) {
            return;
        }
        /**
         * TODO:pname到底从哪获得呢？
         */
        String pname = "";
        String appgroup = frame.getAppgroup();
        String appUUID = frame.getTarget();
        String[] target = appUUID.split("_");

        // 获取数据归集feature的消费信息，frame为一条消费信息，从fame中获取lines为线程分析文件的消费信息
        List<Line> list = frame.getLines();
        // 调用线程分析文件信息处理方法
        List<JavaThreadObject> result = analyse(list);
        /**
         * add additional information: pname,ipport,pid,appgroup,time,user
         */
        for (JavaThreadObject jto : result) {
            jto.setPname(pname);
            jto.setIpport(target[0]);
            jto.setPid(target[1]);
            jto.setAppgroup(appgroup);
            jto.setTime(Long.parseLong(target[2]));

            jto.setUser(target[3]);
        }
        // 处理数据保存工作
        insertThreadObjectToES(result);
    }

    /**
     * TODO:线程分析数据的存储
     * 
     * @param result
     */
    private void insertThreadObjectToES(List<JavaThreadObject> result) {

        if (this.log.isDebugEnable()) {
            this.log.debug(this, "ThreadObjectToES is :" + JSONHelper.toString(result));
        }

        BulkRequestBuilder bulkRequest = client.getClient().prepareBulk();

        for (JavaThreadObject item : result) {
            pushDataToBulkRequest(item, bulkRequest);
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            log.err(this, "INSERT ThreadAnalysis Data to ES FAIL: " + bulkResponse.buildFailureMessage());
        }
    }

    /**
     * 
     * @param appUUID
     * @param appGroup
     * @param span
     * @param bulkRequest
     */
    private void pushDataToBulkRequest(JavaThreadObject jto, BulkRequestBuilder bulkRequest) {

        /**
         * 保证不会重复
         */
        String uuid = EncodeHelper.encodeMD5(jto.toString());

        /**
         * 获取当前正在使用的index名称
         */
        String currentIndex = indexMgr.prepareIndex();

        IndexRequestBuilder irb = client.getClient().prepareIndex(currentIndex, ThreadAnalysisIndexMgr.JTA_TABLE, uuid);

        Map<String, Object> m = jto.toMap();

        irb.setSource(m);

        bulkRequest.add(irb);
    }

    @Override
    public boolean isHandleable(String topic) {

        // 线程分析使用JQ_JTA topic
        return "JQ_JTA".equalsIgnoreCase(topic);
    }

    /**
     * 分析top命令部分
     * 
     * @param topJavaThreadObjectList
     * @param map
     * @param topStartSign
     * @param topEndSign
     * @param line
     */
    private void analyseTopPart(List<JavaThreadObject> topJavaThreadObjectList, Map<String, Integer> lables,
            String line) {

        line = line.trim();
        // 利用空字符串进行分割，分割的每项为top命令每列的值
        String cells[] = line.split("\\s+");
        // 定义top命令的topJavaThreadObject对象
        JavaThreadObject topJavaThreadObject = new JavaThreadObject();
        // 获取线程号，PID 列
        topJavaThreadObject.setTid(cells[lables.get("PID")].trim());
        // 获取线程的CPU占比，%CPU 列
        topJavaThreadObject.setPercpu(Float.parseFloat(cells[lables.get("%CPU")].trim()));
        // 获取线程的内存占比，%MEM 列
        topJavaThreadObject.setPermem(Float.parseFloat(cells[lables.get("%MEM")].trim()));
        // 获取线程的运行时间，TIME+ 列
        topJavaThreadObject.setTimeadd(cells[lables.get("TIME+")].trim());
        // 将topJavaThreadObject对象添加到topJavaThreadObjectList
        topJavaThreadObjectList.add(topJavaThreadObject);

    }

    /**
     * 分析jstack命令部分
     * 
     * @param jstackJavaThreadObjectList
     * @param jstackJavaThreadObject
     * @param jstatckInfo
     * @param line
     */
    private void analyseJstackPart(JavaThreadObject jstackJavaThreadObject, StringBuffer jstatckInfo, String line) {

        // jstack线程栈的第二行信息一般由java.lang.Thread.State:开始，从中获取该线程的状态，守护线程除外
        if (line.contains("java.lang.Thread.State:") && (null != jstackJavaThreadObject)) {
            // 设置线程状态
            jstackJavaThreadObject.setState(line.replace("java.lang.Thread.State:", "").trim());

        }
        // jstatckInfo不为null时，往jstatckInfo添加line内容，为同一个线程栈信息
        if (null != jstatckInfo) {
            jstatckInfo.append(line);
            // 添加空行
            jstatckInfo.append("\r\n");
        }
    }

    /**
     * merge top获取的threadObject和jstack获取的threadObject
     * 
     * @param JavaThreadObjectList
     * @param topJavaThreadObjectList
     * @param jstackJavaThreadObjectList
     */
    private void mergeJavaThreadObject(List<JavaThreadObject> JavaThreadObjectList,
            List<JavaThreadObject> topJavaThreadObjectList, List<JavaThreadObject> jstackJavaThreadObjectList) {

        for (JavaThreadObject jstackObject : jstackJavaThreadObjectList) {
            JavaThreadObject JavaThreadObject = new JavaThreadObject();
            // 添加线程号
            JavaThreadObject.setTid(jstackObject.getTid());
            // 添加线程状态
            JavaThreadObject.setState(jstackObject.getState());
            // 添加线程栈信息
            JavaThreadObject.setInfo(jstackObject.getInfo());

            for (JavaThreadObject topObject : topJavaThreadObjectList) {
                if (JavaThreadObject.getTid().equals(topObject.getTid())) {
                    // 添加CPU占比
                    JavaThreadObject.setPercpu(topObject.getPercpu());
                    // 添加内存占比
                    JavaThreadObject.setPermem(topObject.getPermem());
                    // 添加执行时间
                    JavaThreadObject.setTimeadd(topObject.getTimeadd());

                }

            }
            JavaThreadObjectList.add(JavaThreadObject);
        }
    }

    private enum HANDLE_STATUS {
        IGNORE, TOP, JSTATCK, ADDINFO
    };

    private boolean isJStackInfo(String line) {

        return (line.contains("nid=") && line.contains("tid=") && line.contains("prio="));
    }

    /**
     * 线程分析文件信息处理方法
     * 
     * @param list
     * @return
     */
    private List<JavaThreadObject> analyse(List<Line> list) {

        long stTime = System.currentTimeMillis();
        // 初始化top命令部分的线程list对象
        List<JavaThreadObject> topJavaThreadObjectList = new ArrayList<JavaThreadObject>();
        // 初始化jstack命令部分的线程list对象
        List<JavaThreadObject> jstackJavaThreadObjectList = new ArrayList<JavaThreadObject>();
        // 将top命令部分的threadObject和jstack命令部分的threadObject利用tid关联成同一个threadObject对象
        List<JavaThreadObject> JavaThreadObjectList = new ArrayList<JavaThreadObject>();
        // top命令获取的列名称map，key为序号，value为列名称
        Map<String, Integer> lables = new HashMap<String, Integer>();
        HANDLE_STATUS status = HANDLE_STATUS.IGNORE;
        // 定义jstackJavaThreadObject对象，初始为null
        JavaThreadObject jstackJavaThreadObject = null;
        // 定义jstatckInfo对象，初始为null
        StringBuffer jstatckInfo = null;
        // 开始循环遍历lines
        for (Line collectLine : list) {
            // 获取单行字符串
            String line = collectLine.getContent();

            if("=====".equals(line)){
                status = HANDLE_STATUS.IGNORE;
                continue;
            }
            
            if (StringHelper.isEmpty(line)) {
                if (HANDLE_STATUS.TOP.equals(status)) {
                    status = HANDLE_STATUS.IGNORE;
                }
                else if (HANDLE_STATUS.JSTATCK.equals(status)) {
                    status = HANDLE_STATUS.ADDINFO;
                }

            }
            else if (line.contains("PID")) {
                // 利用空字符串进行分割，分割的每项为top命令的列名称，将列名称保存入循环外部定义的map
                String cells[] = line.trim().split("\\s+");
                for (int i = 0; i < cells.length; i++) {
                    lables.put(cells[i], i);
                }
                status = HANDLE_STATUS.TOP;
                continue;
            }
            else if (isJStackInfo(line)) {
                // 初始化jstackJavaThreadObject对象
                jstackJavaThreadObject = new JavaThreadObject();
                // 初始化jstatckInfo对象
                jstatckInfo = new StringBuffer();
                // 捕获nid值，前两位表示进制(16位)，后面表示值
                Pattern pattern = Pattern.compile("nid=([0-9，a-z]*)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    // 获取16位进制线程号
                    String tid = matcher.group().replace("nid=0x", "");
                    // 转换为十进制的线程号
                    BigInteger srch = new BigInteger(tid, 16);
                    // 保存线程号
                    jstackJavaThreadObject.setTid(srch.toString().trim());
                }
                status = HANDLE_STATUS.JSTATCK;
            }

            /**
             * begin to handle message
             */
            if (HANDLE_STATUS.TOP.equals(status)) {
                analyseTopPart(topJavaThreadObjectList, lables, line);
            }
            else if (HANDLE_STATUS.JSTATCK.equals(status)) {
                analyseJstackPart(jstackJavaThreadObject, jstatckInfo, line);
            }
            else if (HANDLE_STATUS.ADDINFO.equals(status)) {
                // 保存线程栈信息对象jstatckInfo
                jstackJavaThreadObject.setInfo(jstatckInfo.toString());
                // 将jstackJavaThreadObject添加到jstackJavaThreadObjectList
                jstackJavaThreadObjectList.add(jstackJavaThreadObject);
                status = HANDLE_STATUS.IGNORE;
            }
        }

        mergeJavaThreadObject(JavaThreadObjectList, topJavaThreadObjectList, jstackJavaThreadObjectList);
        long elapse = System.currentTimeMillis() - stTime;
        log.info(this, "thread analyse costs: " + elapse + " ms.");
        return JavaThreadObjectList;
    }

}
