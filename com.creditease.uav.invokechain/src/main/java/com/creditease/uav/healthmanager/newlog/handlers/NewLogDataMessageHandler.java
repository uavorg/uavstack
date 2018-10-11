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

package com.creditease.uav.healthmanager.newlog.handlers;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AgentResourceComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.healthmanager.newlog.HMNewLogIndexMgr;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageHandler;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

/**
 * 
 * NewLogDataMessageHandler description: 消费日志数据
 *
 */
public class NewLogDataMessageHandler implements MessageHandler {

    protected static final ISystemLogger log = SystemLogger.getLogger(NewLogDataMessageHandler.class);

    private ESClient client;

    private HMNewLogIndexMgr indexMgr;

    public NewLogDataMessageHandler() {

        client = (ESClient) ConfigurationManager.getInstance().getComponent("newlogservice", "ESClient");

        indexMgr = (HMNewLogIndexMgr) ConfigurationManager.getInstance().getComponent("newlogservice",
                "HMNewLogIndexMgr");
    }

    /**
     * [ { time:1464248773620, host:"09-201211070016", ip:"10.10.37.32",
     * svrid:"F:/testenv/apache-tomcat-7.0.65---F:/testenv/apache-tomcat-7.0.65", tag:"L", frames:{ "ccsp":[ {
     * "MEId":"log", "Instances":[ { "id":"F:/testenv/apache-tomcat-7.0.65/logs/ccsp.log", "values":{ "content":[ {
     * "content":"2016-localhost-startStop-1INFORootWebApplicationContext:initializationstarted",
     * "_timestamp":"1212345678129", "_lnum" : "123" } ] } }, { "id":"/ccsp/log_error.log", "values":{ "content":[
     * {"content":"xxxxxxxxxx", "_timestamp":"1212345678129", "_lnum" : "123"} ] } } ] } ] } } ]
     */
    @Override
    public void handle(Message msg) {

        storeToES(msg);

        // NOW, we send out the MDF for runtime notification
        boolean needGoRuntimeNtf = DataConvertHelper.toBoolean(
                ConfigurationManager.getInstance().getFeatureConfiguration("newlogservice", "to.runtimentf"), true);

        if (needGoRuntimeNtf == false) {
            return;
        }

        AgentResourceComponent arc = (AgentResourceComponent) ConfigurationManager.getInstance()
                .getComponent("messageproducer", "MessageProducerResourceComponent");

        MessageProducer producer = (MessageProducer) arc.getResource();

        if (producer != null) {

            String runtimeKey = MonitorDataFrame.MessageType.RuntimeNtf.toString();
            Message rtntfmsg = MessagingFactory.createMessage(runtimeKey);
            String dataStream = msg.getParam(this.getMsgTypeName());
            rtntfmsg.setParam(runtimeKey, dataStream);
            boolean check = producer.submit(rtntfmsg);
            String sendState = runtimeKey + " Data Sent " + (check ? "SUCCESS" : "FAIL");

            if (log.isDebugEnable()) {
                log.debug(this, sendState + "    " + dataStream);
            }
        }
    }

    /**
     * storeToES
     * 
     * @param msg
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void storeToES(Message msg) {

        String data = msg.getParam(getMsgTypeName());

        List<String> array = JSONHelper.toObjectArray(data, String.class);

        for (String mdfStr : array) {
            // 反序列化为MonitorDataFrame
            MonitorDataFrame mdf = new MonitorDataFrame(mdfStr);

            /**
             * 获取IP和端口，这样唯一性的标识
             */
            String ipport = mdf.getIP();

            String appurl = mdf.getExt("appurl");

            if (!StringHelper.isEmpty(appurl)) {
                ipport = appurl.split("/")[2];
            }

            Map<String, List<Map>> frames = mdf.getDatas();
            for (String appid : frames.keySet()) {

                BulkRequestBuilder bulkRequest = client.getClient().prepareBulk();

                List<Map> applogs = frames.get(appid);
                for (Map applog : applogs) {
                    List<Map> instances = (List<Map>) applog.get("Instances");
                    for (Map logData : instances) {

                        // push to ES BulkRequest
                        pushLogLineToBulkRequest(mdf, appid, ipport, bulkRequest, logData);
                    }
                }

                BulkResponse bulkResponse = bulkRequest.get();
                if (bulkResponse.hasFailures()) {
                    log.err(this, "INSERT App[" + appid + "][" + mdf.getIP() + "] on " + mdf.getServerId()
                            + " Log Data to ES FAIL: " + bulkResponse.buildFailureMessage());
                }
            }
        }
    }

    /**
     * pushLogLineToBulkRequest
     * 
     * @param mdf
     * @param appid
     * @param bulkRequest
     * @param logData
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pushLogLineToBulkRequest(MonitorDataFrame mdf, String appid, String ipport,
            BulkRequestBuilder bulkRequest, Map logData) {

        /**
         * Step 1: get log file name
         */
        String logid = (String) logData.get("id");

        File f = new File(logid);

        String logFileName = f.getName().replace('.', '_');

        /**
         * Step 2: read log file lines
         */
        Map<String, Object> fields = (Map<String, Object>) logData.get("values");

        List<Map> lines = (List<Map>) fields.get("content");

        for (Map line : lines) {

            long ts = DataConvertHelper.toLong(line.get("_timestamp"), -1);

            line.remove("_timestamp");
            line.put("l_timestamp", ts);

            long lnum = DataConvertHelper.toLong(line.get("_lnum"), -1);

            line.remove("_lnum");
            line.put("l_num", lnum);

            /**
             * 如果没有规则设置，使用了全行抓取，则索引Type=日志文件名+"_def"
             */
            String logFileType = logFileName;
            StringBuilder uuidStr = new StringBuilder();
            uuidStr.append(ipport).append(mdf.getServerId()).append("-").append(appid).append("-").append(logid)
                    .append("-").append(lnum);
            if (line.containsKey("content")) {
                logFileType += "_def";
                uuidStr.append("-").append(line.get("content"));
            }
            /**
             * 如果设置了规则，则应该使用索引Type=日志文件名+"_"+<规则名>
             */
            else {
                // TODO: not implement yet
            }

            /**
             * 保证不重复：IP+SvrID+AppID+LogFileName+lineNum+日志内容（def下为content）
             */
            String uuid = EncodeHelper.encodeMD5(uuidStr.toString());

            // 准备index，如果不存在，就创建
            String currentIndex = indexMgr.prepareIndex();

            // 检查type是否存在，不存在就创建
            indexMgr.prepareIndexType(currentIndex, logFileType.toLowerCase());

            IndexRequestBuilder irb = client.getClient().prepareIndex(currentIndex, logFileType.toLowerCase(), uuid);

            /**
             * 用于区分不同机器上的应用实例
             */
            line.put("appid", appid);
            line.put("ipport", ipport);

            irb.setSource(line);

            bulkRequest.add(irb);
        }
    }

    @Override
    public String getMsgTypeName() {

        return MonitorDataFrame.MessageType.Log.toString();
    }

}
