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

package com.creditease.uav.hook.rabbitmq.interceptors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.rabbitmq.adapter.RabbitmqConsumerAdapter;
import com.creditease.uav.hook.rabbitmq.adapter.RabbitmqProducerAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;

public class RabbitmqIT extends BaseComponent {

    private String applicationId;
    static final Map<String, Integer> queueNameIndex = new HashMap<String, Integer>();
    static {
        queueNameIndex.put("basicPublish", 1);
        queueNameIndex.put("queueDeclare", 0);
        queueNameIndex.put("queueDeclareNoWait", 0);
        queueNameIndex.put("queueDeclarePassive", 0);
        queueNameIndex.put("queueDelete", 0);
        queueNameIndex.put("queueDeleteNoWait", 0);
        queueNameIndex.put("queueBind", 0);
        queueNameIndex.put("queueBindNoWait", 0);
        queueNameIndex.put("queueUnbind", 0);
        queueNameIndex.put("queuePurge", 0);
        queueNameIndex.put("basicGet", 0);
        queueNameIndex.put("basicConsume", 0);
    }

    public RabbitmqIT(String appid) {
        this.applicationId = appid;
    }

    public Connection doInstall(Connection arg) {

        arg = JDKProxyInvokeUtil.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] { Connection.class },
                new JDKProxyInvokeHandler<Connection>(arg, new ConnectionProxyProcessor()));

        return arg;
    }

    /**
     *
     * ConnectionProxyProcessor description: ConnectionProxyProcessor
     *
     */
    public class ConnectionProxyProcessor extends JDKProxyInvokeProcessor<Connection> {

        @Override
        public void preProcess(Connection t, Object proxy, Method method, Object[] args) {

        }

        @Override
        public Object postProcess(Object res, Connection t, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("createChannel")) {
                return JDKProxyInvokeUtil.newProxyInstance(Channel.class.getClassLoader(),
                        new Class<?>[] { Channel.class },
                        new JDKProxyInvokeHandler<Channel>((Channel) res, new ChannelProxyProcessor()));
            }
            return null;
        }

    }

    /**
     *
     * ChannelProxyProcessor description: ChannelProxyProcessor
     *
     */
    public class ChannelProxyProcessor extends JDKProxyInvokeProcessor<Channel> {

        String address;
        String targetServer;
        private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

        @SuppressWarnings("unchecked")
        @Override
        public void preProcess(Channel t, Object proxy, Method method, Object[] args) {

            if (method.getExceptionTypes().length > 0
                    && method.getExceptionTypes()[0].getName().equals(IOException.class.getName())) {
                String methodName = method.getName();
                String queueName = null;
                if (queueNameIndex.containsKey(methodName) && args.length != 0) {

                    queueName = (String) args[queueNameIndex.get(methodName)];
                    if (isTempQueue(queueName)) {
                        return;
                    }

                }

                if (null == address) {
                    address = t.getConnection().getAddress().getHostAddress() + ":" + t.getConnection().getPort();
                    address = "mq:rabbit://" + address;
                }
                String url = address + ((null == queueName) ? "" : "/" + queueName);

                if ("basicConsume".equals(method.getName())) {
                    // delegate the consumer
                    args[args.length - 1] = JDKProxyInvokeUtil.newProxyInstance(Consumer.class.getClassLoader(),
                            new Class<?>[] { Consumer.class }, new JDKProxyInvokeHandler<Consumer>(
                                    (Consumer) args[args.length - 1], new ConsumerProxyProcessor(t, url)));

                }

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
                params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, method.getName());
                params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
                params.put(CaptureConstants.INFO_CLIENT_TYPE, "rabbitmq.client");

                if (logger.isDebugable()) {
                    logger.debug("Invoke START:" + url + ",op=" + method.getName(), null);
                }

                UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                        Monitor.CapturePhase.PRECAP, params);

                // 调用链只关心真正的消息通讯
                if (method.getName().equals("basicPublish")) {

                    // register adapter
                    UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                            "registerAdapter", RabbitmqProducerAdapter.class);

                    ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                            "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                            InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                            RabbitmqProducerAdapter.class,
                            new Object[] { (BasicProperties) args[args.length - 2], args[args.length - 1] });
                    if (ivcContextParams != null
                            && ivcContextParams.containsKey(InvokeChainConstants.PARAM_MQHEAD_INFO)) {
                        args[args.length - 2] = ivcContextParams.get(InvokeChainConstants.PARAM_MQHEAD_INFO);
                    }
                }
            }
        }

        @Override
        public Object postProcess(Object res, Channel t, Object proxy, Method method, Object[] args) {

            if (needDoCap(method, args)) {
                doCap(1, t, method, null);
            }

            return null;
        }

        @Override
        public void catchInvokeException(Channel t, Object proxy, Method method, Object[] args, Throwable e) {

            if (needDoCap(method, args)) {
                doCap(-1, t, method, e);
            }

        }

        private boolean needDoCap(Method method, Object[] args) {

            if (method.getExceptionTypes().length == 0
                    || !method.getExceptionTypes()[0].getName().equals(IOException.class.getName())) {
                return false;
            }
            String methodName = method.getName();
            if (queueNameIndex.containsKey(methodName) && args.length != 0) {

                if (isTempQueue((String) args[queueNameIndex.get(methodName)])) {
                    return false;
                }
            }
            return true;
        }

        private void doCap(int rc, Channel t, Method method, Throwable e) {

            if (null == targetServer) {
                Map<String, Object> conn = t.getConnection().getServerProperties();
                String cluster_name = (null == conn.get("cluster_name")) ? "unknown"
                        : conn.get("cluster_name").toString();
                String version = (null == conn.get("version")) ? "unknown" : conn.get("version").toString();
                targetServer = cluster_name + "@" + version;
            }

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            if (logger.isDebugable()) {
                logger.debug("Invoke END: rc=" + rc + "," + targetServer, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            // 调用链只关心真正的消息通讯
            if (method.getName().equals("basicPublish")) {

                if (rc == -1) {
                    ivcContextParams.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
                }
                if (ivcContextParams != null) {
                    ivcContextParams.putAll(params);
                }

                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                        InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP,
                        ivcContextParams, RabbitmqProducerAdapter.class, null);
            }

        }

        public boolean isTempQueue(String queueName) {

            String regEx = "^([0-9]|[a-f]){8}(-([0-9]|[a-f]){4}){3}-([0-9]|[a-f]){12}$";
            Matcher matcher = Pattern.compile(regEx).matcher(queueName);
            return matcher.matches();

        }
    }

    public class ConsumerProxyProcessor extends JDKProxyInvokeProcessor<Consumer> {

        private Channel channel;
        private String url;
        private String targetServer;

        public ConsumerProxyProcessor(Channel channel, String url) {
            this.channel = channel;
            this.url = url;
        }

        @Override
        public void preProcess(Consumer t, Object proxy, Method method, Object[] args) {

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, "Consumer." + method.getName());
            params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "rabbitmq.client");

            if (logger.isDebugable()) {
                logger.debug("Invoke START:" + url + ",op=Consumer." + method.getName(), null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);
            // 调用链只关心真正消费消息
            if (method.getName().equals("handleDelivery")) {

                AMQP.BasicProperties props = (BasicProperties) args[2];
                if (props.getHeaders() != null
                        && props.getHeaders().containsKey(InvokeChainConstants.PARAM_MQHEAD_SPANINFO)) {
                    params.put(InvokeChainConstants.PARAM_MQHEAD_SPANINFO,
                            props.getHeaders().get(InvokeChainConstants.PARAM_MQHEAD_SPANINFO) + "");
                    params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, url);
                }

                // register adapter
                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                        "registerAdapter", RabbitmqConsumerAdapter.class);

                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                        InvokeChainConstants.CHAIN_APP_SERVICE, InvokeChainConstants.CapturePhase.PRECAP, params,
                        RabbitmqConsumerAdapter.class, args);
            }

        }

        @Override
        public void catchInvokeException(Consumer t, Object proxy, Method method, Object[] args, Throwable e) {

            doCap(-1, channel, t, method, e);

        }

        @Override
        public Object postProcess(Object res, Consumer operation, Object proxy, Method method, Object[] args) {

            doCap(1, channel, operation, method, null);
            return null;
        }

        private void doCap(int rc, Channel channel, Consumer consumer, Method method, Throwable e) {

            if (null == targetServer) {
                Map<String, Object> conn = channel.getConnection().getServerProperties();
                String cluster_name = (null == conn.get("cluster_name")) ? "unknown"
                        : conn.get("cluster_name").toString();
                String version = (null == conn.get("version")) ? "unknown" : conn.get("version").toString();
                targetServer = cluster_name + "@" + version;
            }

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            if (logger.isDebugable()) {
                logger.debug("Invoke END: rc=" + rc + "," + targetServer, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            if (method.getName().equals("handleDelivery")) {
                params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, url);
                params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
                params.put(InvokeChainConstants.CLIENT_IT_CLASS, consumer.getClass().getName());
                // 调用链只关心一个方法
                params.put(InvokeChainConstants.CLIENT_IT_METHOD, "handleDelivery");
                params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
                if (rc == -1) {
                    params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
                }

                // invoke chain
                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                        InvokeChainConstants.CHAIN_APP_SERVICE, InvokeChainConstants.CapturePhase.DOCAP, params,
                        RabbitmqConsumerAdapter.class, null);
            }
        }

    }

}
