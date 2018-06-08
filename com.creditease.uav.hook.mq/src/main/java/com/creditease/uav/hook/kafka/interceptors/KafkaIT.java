/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2018 UAVStack
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

package com.creditease.uav.hook.kafka.interceptors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.Metadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.kafka.invokeChain.KafkaConsumerAdapter;
import com.creditease.uav.hook.kafka.invokeChain.KafkaProducerAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;

public class KafkaIT extends BaseComponent {

    private static ThreadLocal<KafkaIT> tl = new ThreadLocal<KafkaIT>();
    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();
    private String appid;

    private static String sendHost = "";
    private static String pollHost = "";

    public KafkaIT(String appid) {
        this.appid = appid;
    }

    public static void startPoll(String appid, String methodName, Object[] args) {
        KafkaIT kit = new KafkaIT(appid);
        getPollHost((Metadata) args[2]);
        kit.doPollStart(methodName, args);
        tl.set(kit);
    }

    public static void endPoll(String methodName, Object[] args) {
        KafkaIT kit = tl.get();
        if (kit == null) {
            return;
        }
        kit.doPollEnd(methodName, args);
        tl.remove();
    }

    @SuppressWarnings({ "unchecked" })
    private void doPollStart(String methodName, Object[] args) {
        TopicPartition tp = (TopicPartition) args[0];
        String kafkaUrl = pollHost + "/" + tp.topic();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, kafkaUrl);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, "Consumer." + methodName);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "kafka.client");
        if (logger.isDebugable()) {
            logger.debug("KAFKA DOPOLL START: " + kafkaUrl, null);
        }
        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params);

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                KafkaConsumerAdapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                KafkaConsumerAdapter.class, args);

    }

    /**
     * @param methodName
     * @param args
     */
    private void doPollEnd(String methodName, Object[] args) {

        int rc = -1;

        if (args != null && args.length > 0) {
            if (!Throwable.class.isAssignableFrom(args[0].getClass())) {
                rc = 1;
            }
        }
        if (logger.isDebugable()) {
            logger.debug("KAFKA " + methodName + " END: " + rc, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "kafka");
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.DOCAP, params);
        if (rc == -1) {
            params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, ((Throwable) args[0]).toString());
        }

        if (ivcContextParams != null) {
            ivcContextParams.putAll(params);
        }

        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                KafkaConsumerAdapter.class, args);

    }

    @SuppressWarnings("unchecked")
    public void syncSendStart(String appid, String methodName, Object[] args) {
        getSendHost(args);
        ProducerRecord<String, String> record = (ProducerRecord<String, String>) args[1];
        String kafkaUrl = sendHost + "/" + record.topic();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, kafkaUrl);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, "Producer." + methodName);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "kafka.client");
        if (logger.isDebugable()) {
            logger.debug("KAFKA " + methodName + " START: " + kafkaUrl, null);
        }
        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params);

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                KafkaProducerAdapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                KafkaProducerAdapter.class, args);
    }

    public void syncSendEnd(Object[] args, String methodName) {
        if (args != null && args.length > 0) {
            if (args.length == 2 && null != args[1]) {
                return;
            }
        }

        int rc = -1;
        if (args != null && args.length > 0) {
            if (!args[0].getClass().getName().equals("org.apache.kafka.clients.producer.KafkaProducer$FutureFailure")) {
                rc = 1;
            }

        }
        if (logger.isDebugable()) {
            logger.debug("KAFKA " + methodName + " END: " + rc, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "kafka");
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.DOCAP, params);
        if (rc == -1) {
            params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, "kafka send msg Fail : KafkaProducer FutureFailure");
        }

        if (ivcContextParams != null) {
            ivcContextParams.putAll(params);
        }

        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                KafkaProducerAdapter.class, args);
    }

    /**
     * @param metadata
     * @return
     */
    private static void getPollHost(Metadata metadata) {
        if ("".equals(pollHost)) {
            List<Node> nodesList = metadata.fetch().nodes();
            List<String> nList = new ArrayList<String>();
            for (int i = 0; i < nodesList.size(); i++) {
                nList.add(nodesList.get(i).host() + ":" + nodesList.get(i).port());
            }
            Collections.sort(nList);
            pollHost = "mq:kafka://" + StringHelper.join(nList, ",");
        }
    }

    /**
     * @param args
     * @return
     */
    private static String getSendHost(Object[] args) {
        if ("".equals(sendHost)) {
            ProducerConfig producerConfig = (ProducerConfig) args[0];
            Map<String, Object> config = (Map<String, Object>) producerConfig.originals();
            String[] urlArray = config.get("bootstrap.servers").toString().split(",");
            Arrays.sort(urlArray);
            sendHost = "mq:kafka://" + StringHelper.join(urlArray, ",");
        }
        return sendHost;
    }

    private Map<String, CaptureContext> ccMap;

    @SuppressWarnings("unchecked")
    public Object asyncSend(String appid, String methodName, Object[] args) {
        ProducerRecord<String, String> record = (ProducerRecord<String, String>) args[1];
        String kafkaUrl = getSendHost(args) + "/" + record.topic();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, kafkaUrl);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, "Producer." + methodName);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "kafka.client");
        if (logger.isDebugable()) {
            logger.debug("KAFKA DOSEND START: " + kafkaUrl, null);
        }

        ccMap = UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params, null);
        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                KafkaProducerAdapter.class, args);
        return JDKProxyInvokeUtil.newProxyInstance(Callback.class.getClassLoader(), new Class<?>[] { Callback.class },
                new JDKProxyInvokeHandler<Callback>((Callback) args[2], new KafkaCallbackProxyInvokeProcessor()));
    }

    public class KafkaCallbackProxyInvokeProcessor extends JDKProxyInvokeProcessor<Callback> {

        @Override
        public void preProcess(Callback t, Object proxy, Method method, Object[] args) {

        }

        @Override
        public Object postProcess(Object res, Callback t, Object proxy, Method method, Object[] args) {
            doEnd(method, args, null);
            return null;
        }

        /**
         * @param method
         * @param args
         * @param object
         */
        private void doEnd(Method method, Object[] args, Throwable e) {
            int rc = -1;

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "kafka");
            if (args != null && args.length > 0 && null == e) {
                if (null == args[1]) {
                    rc = 1;
                }
                else {
                    Exception ex = (Exception) args[1];
                    params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, ex.toString());
                    if (logger.isDebugable()) {
                        logger.debug("kafka DOSEND EXCEPTION: " + ex.toString(), null);
                    }
                }
            }
            else if (e != null) {
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
                if (logger.isDebugable()) {
                    logger.debug("kafka DOSEND Exception: " + e.toString(), null);
                }
            }
            if (logger.isDebugable()) {
                logger.debug("KAFKA DOSEND END: " + rc, null);
            }
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
            UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params, ccMap);

            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }

            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    KafkaProducerAdapter.class, args);
        }

        @Override
        public void catchInvokeException(Callback t, Object proxy, Method method, Object[] args, Throwable e) {
            doEnd(method, args, e);
        }
    }

}
