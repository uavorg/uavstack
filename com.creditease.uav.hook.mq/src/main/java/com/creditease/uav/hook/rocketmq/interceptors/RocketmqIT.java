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

package com.creditease.uav.hook.rocketmq.interceptors;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.PullCallback;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.util.JDKProxyInvokeUtil;

public class RocketmqIT extends BaseComponent {

    private static final String targetServer = "rocketmq";

    private Map<String, CaptureContext> ccMap;

    static final Map<String, Integer> queueNameIndex = new HashMap<String, Integer>();

    static {
        queueNameIndex.put("createTopic", 1);
        queueNameIndex.put("fetchPublishMessageQueues", 0);
        queueNameIndex.put("fetchMessageQueuesInBalance", 0);
        queueNameIndex.put("queryMessage", 0);
        queueNameIndex.put("subscribe", 0);
    }

    private static ThreadLocal<RocketmqIT> tl = new ThreadLocal<RocketmqIT>();

    private String applicationId;

    public RocketmqIT(String appid) {
        this.applicationId = appid;
    }

    public static Object start(String appid, Object mqClient, String methodName, Object[] args) {

        RocketmqIT m = new RocketmqIT(appid);
        tl.set(m);

        // proxy the MessageListener
        if ("registerMessageListener".equals(methodName)) {

            return messageListenerProxy(mqClient, args, m);

        }
        return m.doBefore(mqClient, methodName, args);

    }

    public static void end(int rc, String methodName) {

        RocketmqIT m = tl.get();
        m.doAfter(rc);
        tl.remove();
    }

    private static MessageListener messageListenerProxy(Object mqClient, Object[] args, RocketmqIT m) {

        MessageListener MessageListener = null;
        if (MessageListenerConcurrently.class.isAssignableFrom(args[0].getClass())) {
            MessageListener = JDKProxyInvokeUtil.newProxyInstance(MessageListenerConcurrently.class.getClassLoader(),
                    new Class<?>[] { MessageListenerConcurrently.class },
                    new JDKProxyInvokeHandler<MessageListenerConcurrently>((MessageListenerConcurrently) args[0],
                            m.new MessageListenerConcurrentlyProxyProcessor(getServerAddr(mqClient))));
        }
        else if (MessageListenerOrderly.class.isAssignableFrom(args[0].getClass())) {
            MessageListener = JDKProxyInvokeUtil.newProxyInstance(MessageListenerOrderly.class.getClassLoader(),
                    new Class<?>[] { MessageListenerOrderly.class },
                    new JDKProxyInvokeHandler<MessageListenerOrderly>((MessageListenerOrderly) args[0],
                            m.new MessageListenerOrderlyProxyProcessor(getServerAddr(mqClient))));
        }
        return MessageListener;
    }

    public Object doBefore(Object mqClient, String methodName, Object args[]) {

        String address = getServerAddr(mqClient);

        String topic = getTopic(mqClient, methodName, args);

        String url = address + (null == topic ? "" : "/" + topic);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, methodName);
        params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "rocketmq.client");

        if (logger.isDebugable()) {
            logger.debug("Invoke START:" + url + ",op=" + methodName, null);
        }

        // proxy the PullCallback
        if (methodName.equals("pull")) {
            PullCallback callback = null;

            if (PullCallback.class.isAssignableFrom(args[args.length - 1].getClass())) {
                callback = (PullCallback) args[args.length - 1];
            }
            else if (PullCallback.class.isAssignableFrom(args[args.length - 1].getClass())) {
                callback = (PullCallback) args[args.length - 2];
            }
            if (null != callback) {
                callback = JDKProxyInvokeUtil.newProxyInstance(PullCallback.class.getClassLoader(),
                        new Class<?>[] { PullCallback.class },
                        new JDKProxyInvokeHandler<PullCallback>(callback, new PullCallbackProxyProcessor()));
                ccMap = UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(
                        CaptureConstants.CAPPOINT_APP_CLIENT, Monitor.CapturePhase.PRECAP, params, ccMap);
                return callback;
            }
        }

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params);
        return null;
    }

    private void doAfter(int rc) {

        if (rc != -1) {
            doCap(rc, null);
        }
    }

    private void doCap(int rc, String contextTag) {

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

        if (contextTag != null) {
            params.put(CaptureConstants.INFO_CAPCONTEXT_TAG, contextTag);
        }

        if (logger.isDebugable()) {
            logger.debug("Invoke END: rc=" + rc + "," + targetServer, null);
        }

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.DOCAP, params);
    }

    private static String getServerAddr(Object mqClient) {

        String addr = "mq:rocket://";
        if (mqClient.getClass().getSimpleName().equals("DefaultMQProducer")) {
            addr += ((DefaultMQProducer) mqClient).getNamesrvAddr();
        }
        else if (mqClient.getClass().getSimpleName().equals("DefaultMQPushConsumer")) {
            addr += ((DefaultMQPushConsumer) mqClient).getNamesrvAddr();
        }
        else if (mqClient.getClass().getSimpleName().equals("DefaultMQPullConsumer")) {
            addr += ((DefaultMQPullConsumer) mqClient).getNamesrvAddr();
        }

        return addr;

    }

    private String getTopic(Object mqClient, String methodName, Object args[]) {

        if ("send".equals(methodName)) {
            Message message = (Message) args[0];
            return message.getTopic();
        }
        else if (queueNameIndex.containsKey(methodName)) {
            return (String) args[queueNameIndex.get(methodName)];
        }
        return null;
    }

    public class MessageListenerOrderlyProxyProcessor extends JDKProxyInvokeProcessor<MessageListenerOrderly> {

        private String address;

        public MessageListenerOrderlyProxyProcessor(String address) {
            this.address = address;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void preProcess(MessageListenerOrderly t, Object proxy, Method method, Object[] args) {

            List<MessageExt> msgs = (List<MessageExt>) args[0];
            String url = address + "/" + msgs.get(0).getTopic();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, "Consumer." + method.getName());
            params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "rabbitmq.client");
            params.put(CaptureConstants.INFO_CAPCONTEXT_TAG, method.getName());

            if (logger.isDebugable()) {
                logger.debug("Invoke START:" + url + ",op=Consumer." + method.getName(), null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

        }

        @Override
        public void catchInvokeException(MessageListenerOrderly t, Object proxy, Method method, Object[] args,
                Throwable e) {

            doCap(0, method.getName());

        }

        @Override
        public Object postProcess(Object res, MessageListenerOrderly t, Object proxy, Method method, Object[] args) {

            doCap(1, method.getName());
            return null;
        }

    }

    public class MessageListenerConcurrentlyProxyProcessor
            extends JDKProxyInvokeProcessor<MessageListenerConcurrently> {

        private String address;

        public MessageListenerConcurrentlyProxyProcessor(String address) {
            this.address = address;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void preProcess(MessageListenerConcurrently t, Object proxy, Method method, Object[] args) {

            List<MessageExt> msgs = (List<MessageExt>) args[0];
            String url = address + "/" + msgs.get(0).getTopic();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, "Consumer." + method.getName());
            params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "rocketmq.client");
            params.put(CaptureConstants.INFO_CAPCONTEXT_TAG, method.getName());

            if (logger.isDebugable()) {
                logger.debug("Invoke START:" + url + ",op=Consumer." + method.getName(), null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

        }

        @Override
        public void catchInvokeException(MessageListenerConcurrently t, Object proxy, Method method, Object[] args,
                Throwable e) {

            doCap(0, method.getName());

        }

        @Override
        public Object postProcess(Object res, MessageListenerConcurrently t, Object proxy, Method method,
                Object[] args) {

            doCap(1, method.getName());
            return null;
        }

    }

    public class PullCallbackProxyProcessor extends JDKProxyInvokeProcessor<PullCallback> {

        @Override
        public void preProcess(PullCallback t, Object proxy, Method method, Object[] args) {

        }

        @Override
        public void catchInvokeException(PullCallback t, Object proxy, Method method, Object[] args, Throwable e) {

            doAsyncCap(0);

        }

        @Override
        public Object postProcess(Object res, PullCallback t, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("onSuccess")) {
                doAsyncCap(1);
            }
            else if (method.getName().equals("onException")) {
                doAsyncCap(0);
            }

            return null;
        }

        public void doAsyncCap(int rc) {

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            if (logger.isDebugable()) {
                logger.debug("Invoke END: rc=" + rc + "," + targetServer, null);
            }

            UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params, ccMap);
        }

    }

}
