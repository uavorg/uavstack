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

package com.creditease.uav.hook.mongoclients.interceptors;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.mongoclients.invokeChain.MongoClientAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;
import com.mongodb.binding.ReadBinding;
import com.mongodb.binding.WriteBinding;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ServerDescription;
import com.mongodb.connection.ServerVersion;
import com.mongodb.operation.OperationExecutor;
import com.mongodb.operation.ReadOperation;
import com.mongodb.operation.WriteOperation;

public class MongoClientIT extends BaseComponent {

    /**
     * 
     * MDBProxyProcessor description: MDBProxyProcessor
     *
     */
    public class MDBProxyProcessor extends JDKProxyInvokeProcessor<MongoDatabase> {

        @Override
        public void preProcess(MongoDatabase t, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("getCollection")) {

                String dbName = t.getName();

                OperationExecutor oe = (OperationExecutor) ReflectionHelper.getField(t.getClass(), t, "executor");

                OperationExecutor oeProxy = JDKProxyInvokeUtil.newProxyInstance(
                        OperationExecutor.class.getClassLoader(), new Class<?>[] { OperationExecutor.class },
                        new JDKProxyInvokeHandler<OperationExecutor>(oe,
                                new MDBOperationExecutorProxyProcessor(dbName)));

                ReflectionHelper.setField(t.getClass(), t, "executor", oeProxy);
            }
        }

        @Override
        public Object postProcess(Object res, MongoDatabase t, Object proxy, Method method, Object[] args) {

            return null;
        }

    }

    /**
     * 
     * MDBOperationExecutorProxyProcessor description: MDBOperationExecutorProxyProcessor
     *
     */
    public class MDBOperationExecutorProxyProcessor extends JDKProxyInvokeProcessor<OperationExecutor> {

        private String dbName;

        public MDBOperationExecutorProxyProcessor(String dbName) {
            this.dbName = dbName;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void preProcess(OperationExecutor t, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("execute")) {

                if (ReadOperation.class.isAssignableFrom(args[0].getClass())) {
                    ReadOperation ro = (ReadOperation) args[0];

                    args[0] = JDKProxyInvokeUtil.newProxyInstance(ReadOperation.class.getClassLoader(),
                            new Class<?>[] { ReadOperation.class },
                            new JDKProxyInvokeHandler<ReadOperation>(ro, new ReadOperationProcessor(this.dbName)));

                }
                else if (WriteOperation.class.isAssignableFrom(args[0].getClass())) {
                    WriteOperation wo = (WriteOperation) args[0];

                    args[0] = JDKProxyInvokeUtil.newProxyInstance(WriteOperation.class.getClassLoader(),
                            new Class<?>[] { WriteOperation.class },
                            new JDKProxyInvokeHandler<WriteOperation>(wo, new WriteOperationProcessor(this.dbName)));
                }
            }
        }

        @Override
        public Object postProcess(Object res, OperationExecutor t, Object proxy, Method method, Object[] args) {

            return null;
        }

    }

    @SuppressWarnings("rawtypes")
    public class WriteOperationProcessor extends JDKProxyInvokeProcessor<WriteOperation> {

        private Map<String, Object> ivcContextParams = new HashMap<String, Object>();
        private String dbName;

        public WriteOperationProcessor(String dbName) {
            this.dbName = dbName;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void preProcess(WriteOperation t, Object proxy, Method method, Object[] args) {

            if (!method.getName().equals("execute")) {
                return;
            }

            WriteBinding binding = (WriteBinding) args[0];
            ServerDescription serverDesc = binding.getWriteConnectionSource().getServerDescription();

            String address = serverDesc.getAddress().toString();

            String operationType = t.getClass().getSimpleName();

            String url = "mongo://" + address + "/" + dbName;

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, operationType);
            params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "mongo.client");

            if (logger.isDebugable()) {
                logger.debug("Invoke START:" + url + ",op=" + operationType, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

            // register adapter
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                    "registerAdapter", MongoClientAdapter.class);

            ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                    "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                    MongoClientAdapter.class, args);
        }

        @Override
        public void catchInvokeException(WriteOperation t, Object proxy, Method method, Object[] args, Throwable e) {

            if (method.getName().equals("execute")) {

                doCap(-1, (WriteBinding) args[0], e);
            }
        }

        @Override
        public Object postProcess(Object res, WriteOperation operation, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("execute")) {

                doCap(1, (WriteBinding) args[0], null);
            }
            return res;
        }

        private void doCap(int rc, WriteBinding binding, Throwable e) {

            ServerDescription serverDesc = binding.getWriteConnectionSource().getServerDescription();

            String targetServer = "Mongo" + toVersionString(serverDesc.getVersion()) + "_"
                    + serverDesc.getType().toString();

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            if (logger.isDebugable()) {
                logger.debug("Invoke END: rc=" + rc + "," + targetServer, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            if (rc == -1) {
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
            }
            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }

            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    MongoClientAdapter.class, null);
        }

    }

    @SuppressWarnings("rawtypes")
    public class ReadOperationProcessor extends JDKProxyInvokeProcessor<ReadOperation> {

        private Map<String, Object> ivcContextParams = new HashMap<String, Object>();
        private String dbName;

        public ReadOperationProcessor(String dbName) {
            this.dbName = dbName;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void preProcess(ReadOperation t, Object proxy, Method method, Object[] args) {

            if (!method.getName().equals("execute")) {
                return;
            }

            ReadBinding binding = (ReadBinding) args[0];
            ServerDescription serverDesc = binding.getReadConnectionSource().getServerDescription();

            String address = serverDesc.getAddress().toString();

            String operationType = t.getClass().getSimpleName();

            String url = "mongo://" + address + "/" + dbName;

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, url);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, operationType);
            params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "mongo.client");

            if (logger.isDebugable()) {
                logger.debug("Invoke START:" + url + ",op=" + operationType, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

            // register adapter
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                    "registerAdapter", MongoClientAdapter.class);

            ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                    "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                    MongoClientAdapter.class, args);
        }

        @Override
        public void catchInvokeException(ReadOperation t, Object proxy, Method method, Object[] args, Throwable e) {

            if (method.getName().equals("execute")) {

                doCap(-1, (ReadBinding) args[0], e);
            }
        }

        @Override
        public Object postProcess(Object res, ReadOperation operation, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("execute")) {

                doCap(1, (ReadBinding) args[0], null);
            }
            return res;
        }

        private void doCap(int rc, ReadBinding binding, Throwable e) {

            ServerDescription serverDesc = binding.getReadConnectionSource().getServerDescription();

            String targetServer = "Mongo" + toVersionString(serverDesc.getVersion()) + "_"
                    + serverDesc.getType().toString();

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            if (logger.isDebugable()) {
                logger.debug("Invoke END: rc=" + rc + "," + targetServer, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            if (rc == -1) {
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
            }
            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }

            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    MongoClientAdapter.class, null);
        }

    }

    private String applicationId;

    public MongoClientIT(String appid) {
        this.applicationId = appid;
    }

    public MongoDatabase doInstall(MongoDatabase arg) {

        MongoDatabase argProxy = JDKProxyInvokeUtil.newProxyInstance(MongoDatabase.class.getClassLoader(),
                new Class<?>[] { MongoDatabase.class },
                new JDKProxyInvokeHandler<MongoDatabase>(arg, new MDBProxyProcessor()));

        return argProxy;
    }

    protected String toVersionString(ServerVersion version) {

        StringBuffer sb = new StringBuffer();
        for (Integer i : version.getVersionList()) {
            sb.append(i).append(".");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
