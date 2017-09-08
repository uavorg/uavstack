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

package com.creditease.uav.datastore.sql;

import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DAOFactory {

    public static final String DEFAULTNAME = "DAOFACTORY";

    private static Map<String, DAOFactory> instances = new ConcurrentHashMap<String, DAOFactory>();

    /**
     * build a DAOFactory
     * 
     * @param facName
     * @param driverClassName
     * @param jdbcURL
     * @param userName
     * @param userPassword
     * @param initPoolSize
     * @param MinPoolSize
     * @param MaxPoolSize
     * @param maxIdleTime
     * @param idleConnectionTestPeriod
     * @param testQuerySQL
     * @return
     */
    public static DAOFactory buildDAOFactory(String facName, String driverClassName, String jdbcURL, String userName,
            String userPassword, int initPoolSize, int MinPoolSize, int MaxPoolSize, int maxIdleTime,
            int idleConnectionTestPeriod, String testQuerySQL) {

        if (null == facName || "".equals(facName)) {
            return null;
        }

        String key = facName;

        DAOFactory inst = null;
        if (!instances.containsKey(key)) {
            inst = new DAOFactory(facName, driverClassName, jdbcURL, userName, userPassword, initPoolSize, MinPoolSize,
                    MaxPoolSize, maxIdleTime, idleConnectionTestPeriod, testQuerySQL);
            instances.put(key, inst);
        }
        else {
            inst = instances.get(key);
        }

        return inst;
    }

    /**
     * get a DAOFactory
     * 
     * @param facName
     * @return
     */
    public static DAOFactory getDAOFactory(String facName) {

        if (null == facName || "".equals(facName)) {
            return null;
        }

        if (instances.containsKey(facName)) {
            return instances.get(facName);
        }

        return null;
    }

    /**
     * remove a DAOFactory
     * 
     * @param facName
     */
    public static void removeDAOFactory(String facName) {

        if (null == facName || "".equals(facName)) {
            return;
        }

        instances.remove(facName);
    }

    private final ComboPooledDataSource ds;

    private String facName;

    public String getName() {

        return facName;
    }

    protected DAOFactory(String facName, String driverClassName, String jdbcURL, String userName, String userPassword,
            int initPoolSize, int MinPoolSize, int MaxPoolSize) {
        this(facName, driverClassName, jdbcURL, userName, userPassword, initPoolSize, MinPoolSize, MaxPoolSize, 30000,
                180000, "select 1 from DUAL");
    }

    protected DAOFactory(String facName, String driverClassName, String jdbcURL, String userName, String userPassword,
            int initPoolSize, int MinPoolSize, int MaxPoolSize, int maxIdleTime, int idleConnectionTestPeriod,
            String testQuerySQL) {

        this.facName = facName;

        ds = new ComboPooledDataSource();
        // 设置JDBC的Driver类
        try {
            ds.setDriverClass(driverClassName);
        }
        catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
        // 设置JDBC的URL
        ds.setJdbcUrl(jdbcURL);
        // 设置数据库的登录用户名
        ds.setUser(userName);
        // 设置数据库的登录用户密码
        ds.setPassword(userPassword);
        // 设置连接池的最大连接数
        ds.setMaxPoolSize(MaxPoolSize);
        // 设置连接池的最小连接数
        ds.setMinPoolSize(MinPoolSize);
        // 设置初始化连接数
        ds.setInitialPoolSize(initPoolSize);
        // 设置最大闲置时间
        ds.setMaxIdleTime(maxIdleTime);
        // 设置测试SQL
        ds.setPreferredTestQuery(testQuerySQL);
        // 设置闲置测试周期
        ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
        // 增加单个连接的Statements数量
        ds.setMaxStatements(0);
        // 连接池内单个连接所拥有的最大缓存statements数
        ds.setMaxStatementsPerConnection(200);
        // 获取空闲连接超时时间
        ds.setCheckoutTimeout(10 * 1000);
    }

    /**
     * get the update helper with a SQL template
     * 
     * @param sqlTemplate
     * @return
     */
    public UpdateHelper getUpdateHelper(String sqlTemplate) {

        return new UpdateHelper(this, sqlTemplate);
    }

    /**
     * get the transaction helper
     * 
     * @return
     */
    public BatchUpdateHelper getBatchUpdateHelper(String... sqlTemplates) {

        return new BatchUpdateHelper(this, sqlTemplates);
    }

    /**
     * get the query helper with a SQL template
     * 
     * @param sqlTemplate
     * @return
     */
    public QueryHelper getQueryHelper(String sqlTemplate) {

        return new QueryHelper(this, sqlTemplate);
    }

    /**
     * get the call helper with a SQL template
     * 
     * @param sqlTemplate
     * @return
     */
    public CallHelper getCallHelper(String callTemplate) {

        return new CallHelper(this, callTemplate);
    }

    /**
     * shutdown the DAOFactory
     */
    public void shutdown() {

        ds.close();
    }

    /**
     * get the connection
     * 
     * @return
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {

        Connection con = null;

        try {
            synchronized (ds) {
                con = ds.getConnection();
            }
        }
        catch (SQLException e) {
            throw e;
        }

        return con;
    }

    private static class DAOThreadContext {

        private static ThreadLocal<DAOThreadContext> context = new ThreadLocal<DAOThreadContext>();

        public static void setContext(DAOThreadContext c) {

            context.set(c);
        }

        public static DAOThreadContext getContext() {

            return context.get();
        }

        public static void removeContext() {

            context.remove();
        }

        Connection conn = null;

        List<Statement> stList = new ArrayList<Statement>();

        List<ResultSet> rsList = new ArrayList<ResultSet>();

        /**
         * 
         * for batch operation
         * 
         * @param conn
         */
        public DAOThreadContext(Connection conn) {
            this.conn = conn;
        }

        public Connection getConn() {

            return conn;
        }

        public void addSt(Statement st) {

            stList.add(st);
        }

        public void addRs(ResultSet rs) {

            rsList.add(rs);
        }

        public List<Statement> getStList() {

            return stList;
        }

        public List<ResultSet> getRsList() {

            return rsList;
        }
    }

    /**
     * base Helper
     * 
     * @author xiaolong
     *
     */
    private static abstract class DAOHelper {

        public void checkConn(Connection conn) throws SQLException {

            if (null == conn) {
                throw new SQLException("Connection is not created");
            }

            // TODO some other check
        }

        public void closeConn(Connection conn) {

            try {
                if (conn != null) {
                    conn.close(); // 关闭连接
                }
            }
            catch (SQLException e) {
                // ignore
            }
        }

        private void closeRs(ResultSet rs) {

            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
        }

        public void closeRs(List<ResultSet> rss) {

            if (null != rss) {
                for (ResultSet rs : rss) {
                    if (null != rs) {
                        closeRs(rs);
                    }
                }
            }
        }

        private void closeSt(Statement st) {

            if (null != st) {
                try {
                    st.close();// 关闭Statement
                }
                catch (SQLException e) {
                    // ignore
                }
            }
        }

        public void closeSt(List<Statement> sts) {

            if (null != sts) {
                for (Statement st : sts) {
                    if (null != st) {
                        closeSt(st);
                    }
                }
            }
        }

        public void bulidDAOThreadContext(Connection conn) {

            DAOThreadContext c = new DAOThreadContext(conn);
            DAOThreadContext.setContext(c);
        }

        public void bulidDAOThreadContext(Connection conn, Statement st) {

            DAOThreadContext c = new DAOThreadContext(conn);
            DAOThreadContext.setContext(c);
            c.addSt(st);
        }

        public void bulidDAOThreadContext(Connection conn, Statement st, ResultSet rs) {

            DAOThreadContext c = new DAOThreadContext(conn);
            DAOThreadContext.setContext(c);
            c.addSt(st);
            c.addRs(rs);
        }

        public void free() {

            DAOThreadContext c = DAOThreadContext.getContext();

            List<ResultSet> rsList = c.getRsList();
            Connection conn = c.getConn();
            List<Statement> stList = c.getStList();

            closeRs(rsList);
            closeSt(stList);
            closeConn(conn);

            DAOThreadContext.removeContext();
        }

        public void setPrepareStatementParam(PreparedStatement st, int parameterIndex, Object param)
                throws SQLException {

            TypeHandler handler = null;

            if (null == param) {
                handler = new NullTypeHandler();
                handler.setParameter(st, parameterIndex, param);
                return;
            }

            if (String.class.isAssignableFrom(param.getClass())) {
                handler = new StringTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (Integer.class.isAssignableFrom(param.getClass())) {
                handler = new IntegerTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (Float.class.isAssignableFrom(param.getClass())) {
                handler = new FloatTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (Double.class.isAssignableFrom(param.getClass())) {
                handler = new DoubleTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (Long.class.isAssignableFrom(param.getClass())) {
                handler = new LongTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (BigDecimal.class.isAssignableFrom(param.getClass())) {
                handler = new BigDecimalTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (Date.class.isAssignableFrom(param.getClass())) {
                handler = new DateTypeHandler();
                handler.setParameter(st, parameterIndex, param);

            }
            else if (java.util.Date.class.isAssignableFrom(param.getClass())) {
                handler = new DateTypeHandler();
                handler.setParameter(st, parameterIndex, new Date(java.util.Date.class.cast(param).getTime()));

            }
            else {
                throw new RuntimeException("Unkown arg type:" + param.getClass().getName());
            }
        }
    }

    /**
     * the update helper for update
     * 
     * @author xiaolong
     *
     */
    public static class UpdateHelper extends DAOHelper {

        protected String sqlTemplate;

        protected final DAOFactory fac;

        public UpdateHelper(DAOFactory fac, String sqlTemplate) {
            this.sqlTemplate = sqlTemplate;
            this.fac = fac;
        }

        public int update(String sql) throws SQLException {

            Connection conn = null;
            Statement st = null;
            try {
                conn = fac.getConnection();
                st = conn.createStatement();
                return st.executeUpdate(sql);
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                bulidDAOThreadContext(conn, st);
            }
        }

        public int update(Object... args) throws SQLException {

            Connection conn = null;
            PreparedStatement st = null;
            try {
                conn = fac.getConnection();
                st = conn.prepareStatement(sqlTemplate);
                if (null != args && args.length > 0) {
                    int index = 1;
                    for (Object arg : args) {
                        setPrepareStatementParam(st, index, arg);
                        index++;
                    }
                }
                return st.executeUpdate();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                bulidDAOThreadContext(conn, st);
            }
        }
    }

    /**
     * 
     * the batch helper for batch update
     * 
     * @author xiaolong
     *
     */
    public static class BatchUpdateHelper extends DAOHelper {

        protected final DAOFactory fac;

        protected String[] sqlTemplates;

        protected Connection conn = null;

        protected boolean isCommit = false;

        public BatchUpdateHelper(DAOFactory fac, String... sqlTemplates) {
            this.fac = fac;
            this.sqlTemplates = sqlTemplates;
        }

        public void begin() throws SQLException {

            try {
                conn = fac.getConnection();
                conn.setAutoCommit(false);
            }
            catch (SQLException e) {

            }
            finally {
                bulidDAOThreadContext(conn);
            }
        }

        public void submit() throws SQLException {

            try {
                conn.commit();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                isCommit = true;
            }
        }

        public void rollback() throws SQLException {

            try {
                conn.rollback();
            }
            catch (SQLException e) {
                throw e;
            }
        }

        public int update(String sql) throws SQLException {

            if (isCommit) {
                throw new SQLException("Connection is already commit you can begin a new batch");
            }
            Statement st = null;
            try {
                checkConn(conn);
                st = conn.createStatement();
                return st.executeUpdate(sql);
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                addContextSt(st);
            }
        }

        public int update(int sqlTemplateIndex) throws SQLException {

            if (isCommit) {
                throw new SQLException("Connection is already commit you can begin a new batch");
            }
            PreparedStatement st = null;
            try {
                checkConn(conn);
                st = conn.prepareStatement(sqlTemplates[sqlTemplateIndex]);
                return st.executeUpdate();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                addContextSt(st);
            }
        }

        public int update(int sqlTemplateIndex, Object[] args) throws SQLException {

            if (isCommit) {
                throw new SQLException("Connection is already commit you can begin a new batch");
            }
            PreparedStatement st = null;
            try {
                checkConn(conn);
                st = conn.prepareStatement(sqlTemplates[sqlTemplateIndex]);
                if (null != args && args.length > 0) {
                    int index = 1;
                    for (Object arg : args) {
                        setPrepareStatementParam(st, index, arg);
                        index++;
                    }
                }
                return st.executeUpdate();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                addContextSt(st);
            }
        }

        private void addContextSt(Statement st) {

            DAOThreadContext c = DAOThreadContext.getContext();
            if (null != c) {
                c.addSt(st);
            }
        }

        public void free() {

            if (!isCommit) {
                throw new RuntimeException("Transaction is not commit you must do submit() first");
            }

            super.free();
        }
    }

    /**
     * the query helper for select
     * 
     * @author zhenzhang
     *
     */
    public static class QueryHelper extends DAOHelper {

        private String sqlTemplate;

        private final DAOFactory fac;

        private ResultConverter converter = new DefaultResultConverter();

        public QueryHelper(DAOFactory fac, String sqlTemplate) {
            this.sqlTemplate = sqlTemplate;
            this.fac = fac;
        }

        public void setConverter(ResultConverter converter) {

            this.converter = converter;
        }

        public ResultSet query() throws SQLException {

            return query(sqlTemplate);
        }

        public ResultSet query(String sql) throws SQLException {

            Statement st = null;
            ResultSet rs = null;
            Connection conn = null;

            try {
                conn = fac.getConnection();
                st = conn.createStatement();

                rs = st.executeQuery(sql);
                return rs;
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                bulidDAOThreadContext(conn, st, rs);
            }
        }

        public ResultSet query(Object... args) throws SQLException {

            PreparedStatement st = null;
            ResultSet rs = null;
            Connection conn = null;

            try {
                conn = fac.getConnection();
                st = conn.prepareStatement(sqlTemplate);
                if (null != args && args.length > 0) {
                    int index = 1;
                    for (Object arg : args) {
                        setPrepareStatementParam(st, index, arg);
                        index++;
                    }
                }

                rs = st.executeQuery();
                return rs;
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                bulidDAOThreadContext(conn, st, rs);
            }
        }

        private <T> T convertObject(ResultSet rs, Class<T> cls) throws SQLException {

            try {
                if (rs.next()) {
                    return converter.convert(rs, cls);
                }
            }
            catch (SQLException e) {
                throw e;
            }
            return null;
        }

        private <T> List<T> convertList(ResultSet rs, Class<T> cls) throws SQLException {

            List<T> list = new ArrayList<T>();
            try {
                while (rs.next()) {
                    list.add(converter.convert(rs, cls));
                }
            }
            catch (SQLException e) {
                throw e;
            }
            return list;
        }

        public <T> T queryInObject(Class<T> cls) throws SQLException {

            ResultSet rs = query(sqlTemplate);
            return convertObject(rs, cls);
        }

        public <T> T queryInObject(Class<T> cls, String sql) throws SQLException {

            ResultSet rs = query(sql);
            return convertObject(rs, cls);
        }

        public <T> T queryInObject(Class<T> cls, Object... args) throws SQLException {

            ResultSet rs = query(args);
            return convertObject(rs, cls);
        }

        public <T> List<T> queryInList(Class<T> cls) throws SQLException {

            ResultSet rs = query(sqlTemplate);
            return convertList(rs, cls);
        }

        public <T> List<T> queryInList(Class<T> cls, String sql) throws SQLException {

            ResultSet rs = query(sql);
            return convertList(rs, cls);
        }

        public <T> List<T> queryInList(Class<T> cls, Object... args) throws SQLException {

            ResultSet rs = query(args);
            return convertList(rs, cls);
        }
    }

    public static class CallHelper extends DAOHelper {

        public static class CallOut {

            int index;

            int sqlType;

            public CallOut(int index, int sqlType) {
                this.index = index;
                this.sqlType = sqlType;
            }

            public int getIndex() {

                return index;
            }

            public int getSqlType() {

                return sqlType;
            }

            public void setIndex(int index) {

                this.index = index;
            }

            public void setSqlType(int sqlType) {

                this.sqlType = sqlType;
            }
        }

        public static class CallIn {

            int index;

            Object value;

            public CallIn(int index, Object value) {
                this.index = index;
                this.value = value;
            }

            public int getIndex() {

                return index;
            }

            public Object getValue() {

                return value;
            }

            public void setIndex(int index) {

                this.index = index;
            }

            public void setValue(Object value) {

                this.value = value;
            }
        }

        private String callTemplate;

        private final DAOFactory fac;

        public CallHelper(DAOFactory fac, String callTemplate) {
            this.callTemplate = callTemplate;
            this.fac = fac;
        }

        public void call() throws SQLException {

            call(callTemplate);
        }

        public void call(String call) throws SQLException {

            CallableStatement cst = null;
            Connection conn = null;

            try {
                conn = fac.getConnection();
                cst = conn.prepareCall(call);
                cst.execute();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                bulidDAOThreadContext(conn, cst);
            }
        }

        public void call(CallIn... ins) throws SQLException {

            CallableStatement cst = null;
            Connection conn = null;

            try {
                conn = fac.getConnection();
                cst = conn.prepareCall(callTemplate);
                for (CallIn in : ins) {
                    setPrepareStatementParam(cst, in.getIndex(), in.getValue());
                }
                cst.execute();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                bulidDAOThreadContext(conn, cst);
            }
        }

        public Object[] call(CallOut[] outs, CallIn... ins) throws SQLException {

            return call(callTemplate, outs, ins);
        }

        public Object[] call(String call, CallOut[] outs, CallIn... ins) throws SQLException {

            CallableStatement cst = null;
            Connection conn = null;
            Object[] res = null;

            try {
                conn = fac.getConnection();
                cst = conn.prepareCall(call);

                for (CallIn in : ins) {
                    setPrepareStatementParam(cst, in.getIndex(), in.getValue());
                }

                for (CallOut out : outs) {
                    cst.registerOutParameter(out.index, out.sqlType);
                }

                cst.execute();

                int outLength = outs.length;
                if (outLength > 0) {
                    int index = 0;
                    res = new Object[outLength];
                    for (CallOut out : outs) {
                        res[index] = cst.getObject(out.getIndex());
                        index++;
                    }
                }

                return res;

            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                DAOThreadContext c = new DAOThreadContext(conn);
                for (Object rs : res) {
                    if (rs instanceof ResultSet) {
                        c.addRs((ResultSet) rs);
                    }
                }
                DAOThreadContext.setContext(c);
            }
        }
    }

    public static class BigDecimalTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setBigDecimal(i, ((BigDecimal) parameter));
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            Object bigdec = rs.getBigDecimal(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return bigdec;
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            Object bigdec = rs.getBigDecimal(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return bigdec;
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            Object bigdec = cs.getBigDecimal(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return bigdec;
            }
        }
    }

    public class BooleanTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setBoolean(i, ((Boolean) parameter).booleanValue());
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            boolean b = rs.getBoolean(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Boolean(b);
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            boolean b = rs.getBoolean(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Boolean(b);
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            boolean b = cs.getBoolean(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new Boolean(b);
            }
        }
    }

    public static class DateTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setTimestamp(i, new java.sql.Timestamp(((Date) parameter).getTime()));
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            java.sql.Timestamp sqlTimestamp = rs.getTimestamp(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new java.util.Date(sqlTimestamp.getTime());
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            java.sql.Timestamp sqlTimestamp = rs.getTimestamp(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new java.util.Date(sqlTimestamp.getTime());
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            java.sql.Timestamp sqlTimestamp = cs.getTimestamp(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new java.util.Date(sqlTimestamp.getTime());
            }
        }
    }

    public static class DoubleTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setDouble(i, ((Double) parameter).doubleValue());
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            double d = rs.getDouble(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Double(d);
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            double d = rs.getDouble(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Double(d);
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            double d = cs.getDouble(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new Double(d);
            }
        }
    }

    public static class FloatTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setFloat(i, ((Float) parameter).floatValue());
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            float f = rs.getFloat(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Float(f);
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            float f = rs.getFloat(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Float(f);
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            float f = cs.getFloat(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new Float(f);
            }
        }
    }

    public static class IntegerTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setInt(i, ((Integer) parameter).intValue());
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            int i = rs.getInt(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Integer(i);
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            int i = rs.getInt(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Integer(i);
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            int i = cs.getInt(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new Integer(i);
            }
        }
    }

    public static class LongTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setLong(i, ((Long) parameter).longValue());
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            long l = rs.getLong(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Long(l);
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            long l = rs.getLong(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Long(l);
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            long l = cs.getLong(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new Long(l);
            }
        }
    }

    public static class ShortTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setShort(i, ((Short) parameter).shortValue());
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            short s = rs.getShort(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Short(s);
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            short s = rs.getShort(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return new Short(s);
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            short s = cs.getShort(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return new Short(s);
            }
        }
    }

    public static class SqlDateTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setDate(i, (java.sql.Date) parameter);
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            Object sqlDate = rs.getDate(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return sqlDate;
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            Object sqlDate = rs.getDate(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return sqlDate;
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            Object sqlDate = cs.getDate(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return sqlDate;
            }
        }
    }

    public static class SqlTimestampTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setTimestamp(i, (java.sql.Timestamp) parameter);
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            Object sqlTimestamp = rs.getTimestamp(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return sqlTimestamp;
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            Object sqlTimestamp = rs.getTimestamp(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return sqlTimestamp;
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            Object sqlTimestamp = cs.getTimestamp(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return sqlTimestamp;
            }
        }
    }

    public static class SqlTimeTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setTime(i, (java.sql.Time) parameter);
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            Object sqlTime = rs.getTime(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return sqlTime;
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            Object sqlTime = rs.getTime(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return sqlTime;
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            Object sqlTime = cs.getTime(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return sqlTime;
            }
        }
    }

    public static class StringTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setString(i, ((String) parameter));
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            Object s = rs.getString(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return s;
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            Object s = rs.getString(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return s;
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            Object s = cs.getString(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return s;
            }
        }
    }

    public static class NullTypeHandler implements TypeHandler {

        public void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {

            ps.setNull(i, Types.NULL);
        }

        public Object getResult(ResultSet rs, String columnName) throws SQLException {

            Object obj = rs.getObject(columnName);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return obj;
            }
        }

        public Object getResult(ResultSet rs, int columnIndex) throws SQLException {

            Object obj = rs.getObject(columnIndex);
            if (rs.wasNull()) {
                return null;
            }
            else {
                return obj;
            }
        }

        public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {

            Object obj = cs.getObject(columnIndex);
            if (cs.wasNull()) {
                return null;
            }
            else {
                return obj;
            }
        }
    }

    private static class DefaultResultConverter extends ResultConverter {

        public String convertColumnName(String fieldName) {

            if (null == fieldName) {
                return "";
            }
            char[] chars = fieldName.toCharArray();
            StringBuffer sb = new StringBuffer();
            for (char c : chars) {
                if (Character.isUpperCase(c)) {
                    sb.append("_" + Character.toLowerCase(c));
                }
                else {
                    sb.append(c);
                }
            }
            return sb.toString().toUpperCase();
        }

        public enum DataType {
            Short, Integer, Long, Float, Double, BigDecimal, Date, String
        }

        public Object convertColumnValue(String type, String columnName, ResultSet rs) throws SQLException {

            Object obj = null;
            TypeHandler handler = null;
            DataType dataType = null;
            if (type.equals("java.lang.Short")) {
                dataType = DataType.Short;
            }
            else if (type.equals("java.lang.Integer")) {
                dataType = DataType.Integer;
            }
            else if (type.equals("java.lang.Long")) {
                dataType = DataType.Long;
            }
            else if (type.equals("java.lang.Float")) {
                dataType = DataType.Float;
            }
            else if (type.equals("java.lang.Double")) {
                dataType = DataType.Double;
            }
            else if (type.equals("java.math.BigDecimal")) {
                dataType = DataType.BigDecimal;
            }

            else if (type.equals("java.util.Date")) {
                dataType = DataType.Date;
            }
            switch (dataType) {
                case Short:
                    handler = new ShortTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                case Integer:
                    handler = new IntegerTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                case Long:
                    handler = new LongTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                case Float:
                    handler = new FloatTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                case Double:
                    handler = new DoubleTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                case BigDecimal:
                    handler = new BigDecimalTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                case Date:
                    handler = new DateTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
                default:
                    handler = new StringTypeHandler();
                    obj = handler.getResult(rs, columnName);
                    break;
            }
            return obj;
        }

    }
}
