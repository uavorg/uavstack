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

package com.creditease.uav.hook.jdbc.interceptors;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.sql.DataSource;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.jdbc.invokeChain.JdbcDriverAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;
import com.creditease.uav.util.MonitorServerUtil;

public class JdbcDriverIT extends BaseComponent {

    static final Set<String> prepareStatementMethods = new HashSet<String>();
    static {
        prepareStatementMethods.add("setNull");
        prepareStatementMethods.add("setBoolean");
        prepareStatementMethods.add("setByte");
        prepareStatementMethods.add("setShort");
        prepareStatementMethods.add("setInt");
        prepareStatementMethods.add("setLong");
        prepareStatementMethods.add("setFloat");
        prepareStatementMethods.add("setDouble");
        prepareStatementMethods.add("setBigDecimal");
        prepareStatementMethods.add("setString");
        prepareStatementMethods.add("setBytes");
        prepareStatementMethods.add("setDate");
        prepareStatementMethods.add("setTime");
        prepareStatementMethods.add("setTimestamp");
        // prepareStatementMethods.add("setAsciiStream");
        // prepareStatementMethods.add("setUnicodeStream");
        // prepareStatementMethods.add("setBinaryStream");
        prepareStatementMethods.add("setObject");
    }

    /**
     * 
     * DataSourceProxy description: to install DataSource Proxy
     *
     */
    private class DataSourceProxy extends JDKProxyInvokeProcessor<DataSource> {

        @Override
        public void preProcess(DataSource t, Object proxy, Method method, Object[] args) {

        }

        @Override
        public Object postProcess(Object res, DataSource t, Object proxy, Method method, Object[] args) {

            if ("getConnection".equals(method.getName())) {

                Connection p = doProxyConnection(res);

                return p;
            }

            return res;
        }

    }

    /**
     * 
     * DriverProxy description: to install Connection Proxy
     *
     */
    private class DriverProxy extends JDKProxyInvokeProcessor<Driver> {

        @Override
        public void preProcess(Driver t, Object proxy, Method method, Object[] args) {

            // ignore
        }

        @Override
        public Object postProcess(Object res, Driver t, Object proxy, Method method, Object[] args) {

            if ("connect".equals(method.getName())) {

                if (res == null) {
                    return res;
                }

                String tempURL = (String) args[0];
                jdbcUrl = MonitorServerUtil.formatJDBCURL(tempURL);

                setTargetServer(tempURL);

                Connection p = doProxyConnection(res);

                return p;
            }
            return res;
        }

    }

    /**
     * 
     * ConnectionProxy description: to install Statement Proxy
     *
     */
    private class ConnectionProxy extends JDKProxyInvokeProcessor<Connection> {

        @Override
        public void preProcess(Connection t, Object proxy, Method method, Object[] args) {

            // ignore
        }

        @Override
        public Object postProcess(Object res, Connection t, Object proxy, Method method, Object[] args) {

            if (res == null) {
                return res;
            }

            if ("createStatement".equals(method.getName())) {

                Set<Class<?>> interfaces = ReflectionHelper.getAllInterfaces(res.getClass(), true);
                Class<?>[] intArrays = new Class[interfaces.size()];
                intArrays = interfaces.toArray(intArrays);

                Statement stmt = (Statement) res;
                return JDKProxyInvokeUtil.newProxyInstance(this.getClass().getClassLoader(), intArrays,
                        new JDKProxyInvokeHandler<Statement>(stmt, new StatementProxy<Statement>()));
            }
            else if ("prepareStatement".equals(method.getName())) {

                Set<Class<?>> interfaces = ReflectionHelper.getAllInterfaces(res.getClass(), true);
                Class<?>[] intArrays = new Class[interfaces.size()];
                intArrays = interfaces.toArray(intArrays);

                String sql = (String) args[0];

                // get prepare sql
                String action = matchSqlAction(sql);

                PreparedStatement pstmt = (PreparedStatement) res;
                return JDKProxyInvokeUtil.newProxyInstance(this.getClass().getClassLoader(), intArrays,
                        new JDKProxyInvokeHandler<PreparedStatement>(pstmt,
                                new StatementProxy<PreparedStatement>(action, sql)));
            }
            else if ("prepareCall".equals(method.getName())) {
                // TODO: NOT SUPPORT YET
            }
            return res;
        }

    }

    /**
     * 
     * StatementProxy description: Standard Statement
     *
     * @param <T>
     */
    private class StatementProxy<T extends Statement> extends JDKProxyInvokeProcessor<T> {

        private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

        private String action = "";

        private String sql = "";

        // 用来存储PreparedStatement时的sql参数
        private List<Map<Integer, String>> parameters = new LinkedList<Map<Integer, String>>();

        public StatementProxy() {

        }

        public StatementProxy(String action, String sql) {
            this.sql = sql;
            this.action = action;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void preProcess(T t, Object proxy, Method method, Object[] args) {

            if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
                storeParameter(method, args);
            }
            if (method.getName().indexOf("execute") == -1) {
                return;
            }

            Map<String, Object> params = new HashMap<String, Object>();

            boolean isPrepareSt = false;

            if (PreparedStatement.class.isAssignableFrom(t.getClass())) {
                isPrepareSt = true;
            }

            if (method.getName().equals("execute")) {
                if (isPrepareSt == false) {
                    this.sql = (String) args[0];

                    action = matchSqlAction(sql);
                }
            }
            else if (method.getName().equals("executeQuery")) {
                if (isPrepareSt == false) {
                    this.sql = (String) args[0];
                }
                action = "Q";
            }
            else if (method.getName().equals("executeUpdate")) {
                if (isPrepareSt == false) {
                    this.sql = (String) args[0];

                    action = matchSqlAction(sql);
                }
            }
            else if (method.getName().equals("executeBatch")) {
                action = "B";
            }

            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, jdbcUrl);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, action);
            params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "jdbc.client");

            if (logger.isDebugable()) {
                logger.debug("Install JDBC Proxy START: " + jdbcUrl + "," + action + "," + appid, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

            // register adapter
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                    "registerAdapter", JdbcDriverAdapter.class);

            Object[] objs = { this.sql, this.parameters };
            ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                    "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                    JdbcDriverAdapter.class, objs);
        }

        /**
         * 暂存sql参数
         * 
         * @param method
         * @param args
         */
        private void storeParameter(Method method, Object[] args) {

            if (prepareStatementMethods.contains(method.getName()) && args.length == 2) {
                // 为空说明之前不存在参数
                if (this.parameters.isEmpty()) {
                    Map<Integer, String> paraMap = new HashMap<Integer, String>();
                    paraMap.put((Integer) args[0], String.valueOf(args[1]));
                    this.parameters.add(paraMap);
                }
                // 不为空则往最后一位追加参数
                else {
                    Map<Integer, String> paraMap = this.parameters.get(this.parameters.size() - 1);
                    paraMap.put((Integer) args[0], String.valueOf(args[1]));
                }
            }
            // batch操作则在末尾追加map
            if (method.getName().equals("addBatch")) {
                Map<Integer, String> paraMap = new HashMap<Integer, String>();
                this.parameters.add(paraMap);
            }
        }

        @Override
        public Object postProcess(Object res, T t, Object proxy, Method method, Object[] args) {

            if (method.getName().indexOf("execute") == -1) {
                return res;
            }

            doCap(t, 1, null, method, res);

            return res;
        }

        @Override
        public void catchInvokeException(T t, Object proxy, Method method, Object[] args, Throwable e) {

            if (method.getName().indexOf("execute") == -1) {
                return;
            }

            doCap(t, -1, e, method, null);
        }

        /**
         * do real capture
         * 
         * @param t
         * @param rc
         */
        private void doCap(T t, int rc, Throwable throwable, Method method, Object res) {

            String user = "";
            try {
                Connection conn = null;
                try {
                    conn = t.getConnection();
                }
                catch (SQLException ignore) {
                    /**
                     * if connection is closed, Statement.getConnect() throws a SQLException. ignore
                     */
                }
                if (conn != null && !conn.isClosed()) {
                    user = conn.getMetaData().getUserName();
                }
            }
            catch (SQLException e) {
                logger.info("GET JDBC User FAIL. " + e.getMessage());
            }

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer + "@" + user);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            if (logger.isDebugable()) {
                logger.debug("Install JDBC Proxy END: " + targetServer + "@" + user + "," + rc, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            if (rc == -1) {
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, throwable.toString());
            }
            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }
            Object[] objs = { method, res, t };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    JdbcDriverAdapter.class, objs);
        }
    }

    private String appid;

    private String jdbcUrl;

    private String targetServer = "unknown";

    public JdbcDriverIT(String appid) {
        this.appid = appid;
    }

    /**
     * 获取jdbcurl
     * 
     * @param obj
     * @param fieldName
     */
    public void setJDBCUrlByObjField(Object obj, String fieldName) {

        String jdbcUrl = (String) ReflectionHelper.getField(obj.getClass(), obj, fieldName);

        if (jdbcUrl != null) {
            this.jdbcUrl = MonitorServerUtil.formatJDBCURL(jdbcUrl);
        }
    }

    public void setJDBCUrl(String jdbcURL) {

        this.jdbcUrl = MonitorServerUtil.formatJDBCURL(jdbcURL);
    }

    /**
     * this is the way to work in appserver
     */
    public void initSomeDrivers(ClassLoader webapploader) {

        // mysql
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            // ignore
        }

        // oracle
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (ClassNotFoundException e) {
            // ignore
        }

    }

    /**
     * we need inject DataSource, as all DataSource will do there getConnection way which may be not from DriverManager
     * 
     * @param obj
     * @param args
     * @param vendor
     * @return
     */
    public Object doProxyDataSource(Object dataSourceObj, Object dataSourceCfgObj, String vendor) {

        if (!DataSource.class.isAssignableFrom(dataSourceObj.getClass())) {
            return dataSourceObj;
        }

        String tmpjdbcURL = null;

        if ("Tomcat".equalsIgnoreCase(vendor)) {
            try {
                Reference ref = (Reference) dataSourceCfgObj;
                RefAddr ra = ref.get("url");
                tmpjdbcURL = ra.getContent().toString();
            }
            catch (Exception e) {
                // ignore
            }
        }

        if (tmpjdbcURL == null) {
            return dataSourceObj;
        }

        this.jdbcUrl = MonitorServerUtil.formatJDBCURL(tmpjdbcURL);

        setTargetServer(tmpjdbcURL);

        DataSource ds = (DataSource) dataSourceObj;

        Set<Class<?>> ifs = ReflectionHelper.getAllInterfaces(dataSourceObj.getClass(), true);

        Class<?>[] dsIfs = new Class<?>[ifs.size()];

        dsIfs = ifs.toArray(dsIfs);

        DataSource dsProxy = JDKProxyInvokeUtil.newProxyInstance(this.getClass().getClassLoader(), dsIfs,
                new JDKProxyInvokeHandler<DataSource>(ds, new DataSourceProxy()));

        return dsProxy;
    }

    /**
     * setTargetServer
     * 
     * @param tmpjdbcURL
     */
    private void setTargetServer(String tmpjdbcURL) {

        if (tmpjdbcURL.indexOf("mysql") > -1) {
            this.targetServer = "MySql";
        }
        else if (tmpjdbcURL.indexOf("oracle") > -1) {
            this.targetServer = "Oracle";
        }
    }

    /**
     * do register driver
     * 
     * @param dr
     */
    public Driver doRegisterDriver(Driver dr, boolean needRegisterToDriverManager) {

        try {
            if (needRegisterToDriverManager == true) {
                DriverManager.deregisterDriver(dr);
            }

            Driver drProxy = JDKProxyInvokeUtil.newProxyInstance(this.getClass().getClassLoader(),
                    new Class<?>[] { Driver.class }, new JDKProxyInvokeHandler<Driver>(dr, new DriverProxy()));

            if (needRegisterToDriverManager == true) {
                DriverManager.registerDriver(drProxy);
            }

            return drProxy;
        }
        catch (SQLException e) {
            logger.error("Install JDBC Driver Proxy FAIL.", e);
        }

        return dr;

    }

    private String matchSqlAction(String sql) {

        String action = "";

        if (sql.indexOf("insert") > -1) {
            action = "I";
        }
        else if (sql.indexOf("update") > -1) {
            action = "U";
        }
        else if (sql.indexOf("delete") > -1) {
            action = "D";
        }
        else if (sql.indexOf("select") > -1) {
            action = "Q";
        }
        return action;
    }

    public Connection doProxyConnection(Object res) {

        ClassLoader cl = this.getClass().getClassLoader();

        Set<Class<?>> connectionInterfaces = ReflectionHelper.getAllInterfaces(res.getClass(), true);

        connectionInterfaces.add(Connection.class);

        /**
         * NOTE: besides java.sql.Connection，we have to add other vendor Connection interfaces into proxy, in case some
         * users will use that
         */
        // mysql
        if (jdbcUrl.indexOf("mysql") > -1) {
            findSqlConnectionClass("com.mysql.jdbc.Connection", cl, connectionInterfaces);
            findSqlConnectionClass("com.mysql.jdbc.MySQLConnection", cl, connectionInterfaces);
        }
        // oracle
        else if (jdbcUrl.indexOf("oracle") > -1) {
            findSqlConnectionClass("oracle.jdbc.OracleConnection", cl, connectionInterfaces);
        }

        Class<?>[] connInterfaces = new Class[connectionInterfaces.size()];

        connInterfaces = connectionInterfaces.toArray(connInterfaces);

        Connection conn = (Connection) res;
        Connection p = JDKProxyInvokeUtil.newProxyInstance(cl, connInterfaces,
                new JDKProxyInvokeHandler<Connection>(conn, new ConnectionProxy()));
        return p;
    }

    private void findSqlConnectionClass(String clsName, ClassLoader cl, Set<Class<?>> connectionInterfaces) {

        try {
            Class<?> driverConnectionInterfaceCls = cl.loadClass(clsName);
            if (driverConnectionInterfaceCls.isInterface()) {
                connectionInterfaces.add(driverConnectionInterfaceCls);
            }
        }
        catch (ClassNotFoundException e) {
            // ignore
        }
    }

}
