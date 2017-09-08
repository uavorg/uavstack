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

package com.creditease.monitorframework.fat.dbconnpool;

import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DAOFactory {

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

        ResultSet rs = null;
        Connection conn = null;
        Statement st = null;

        public DAOThreadContext(ResultSet rs, Connection conn, Statement st) {
            this.rs = rs;
            this.conn = conn;
            this.st = st;
        }

        public DAOThreadContext(Connection conn, Statement st) {
            this.conn = conn;
            this.st = st;
        }

        public ResultSet getRs() {

            return rs;
        }

        public Connection getConn() {

            return conn;
        }

        public Statement getSt() {

            return st;
        }

    }

    /**
     * the query helper for select
     * 
     * @author zhenzhang
     *
     */
    public static class QueryHelper {

        private String sqlTemplate;
        private final DAOFactory fac;

        public QueryHelper(DAOFactory fac) {
            this.fac = fac;
        }

        public QueryHelper(DAOFactory fac, String sqlTemplate) {
            this.sqlTemplate = sqlTemplate;
            this.fac = fac;
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
                DAOThreadContext c = new DAOThreadContext(rs, conn, st);
                DAOThreadContext.setContext(c);
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

                        if (String.class.isAssignableFrom(arg.getClass())) {
                            st.setString(index, (String) arg);
                        }
                        else if (Integer.class.isAssignableFrom(arg.getClass())) {
                            st.setInt(index, Integer.class.cast(arg));
                        }
                        else if (Float.class.isAssignableFrom(arg.getClass())) {
                            st.setFloat(index, Float.class.cast(arg));
                        }
                        else if (Double.class.isAssignableFrom(arg.getClass())) {
                            st.setDouble(index, Double.class.cast(arg));
                        }
                        else if (Long.class.isAssignableFrom(arg.getClass())) {
                            st.setLong(index, Long.class.cast(arg));
                        }
                        else if (BigDecimal.class.isAssignableFrom(arg.getClass())) {
                            st.setBigDecimal(index, BigDecimal.class.cast(arg));
                        }
                        else if (arg instanceof byte[]) {
                            st.setBytes(index, (byte[]) arg);
                        }

                        else {
                            throw new RuntimeException("Unkown arg type:" + arg.getClass().getName());
                        }

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
                DAOThreadContext c = new DAOThreadContext(rs, conn, st);
                DAOThreadContext.setContext(c);
            }
        }

        public int execute(String sql) throws SQLException {

            Statement st = null;
            Connection conn = null;
            int ret = 0;
            try {
                conn = fac.getConnection();
                st = conn.createStatement();
                ret = st.executeUpdate(sql);
            }
            catch (SQLException e) {
                ret = 0;
                throw e;
            }
            finally {
                DAOThreadContext c = new DAOThreadContext(conn, st);
                DAOThreadContext.setContext(c);

            }
            return ret;
        }

        public boolean execute(String[] sqls, List<List<Object>> paramsList) throws SQLException {

            if (sqls.length != paramsList.size()) {
                return false;
            }

            Connection conn = fac.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement st = null;
            try {

                for (int i = 0; i < sqls.length; i++) {
                    st = conn.prepareStatement(sqls[i]);
                    int index = 1;
                    for (Object arg : paramsList.get(i)) {

                        if (String.class.isAssignableFrom(arg.getClass())) {
                            st.setString(index, (String) arg);
                        }
                        else if (Integer.class.isAssignableFrom(arg.getClass())) {
                            st.setInt(index, Integer.class.cast(arg));
                        }
                        else if (Float.class.isAssignableFrom(arg.getClass())) {
                            st.setFloat(index, Float.class.cast(arg));
                        }
                        else if (Double.class.isAssignableFrom(arg.getClass())) {
                            st.setDouble(index, Double.class.cast(arg));
                        }
                        else if (Long.class.isAssignableFrom(arg.getClass())) {
                            st.setLong(index, Long.class.cast(arg));
                        }
                        else if (BigDecimal.class.isAssignableFrom(arg.getClass())) {
                            st.setBigDecimal(index, BigDecimal.class.cast(arg));
                        }
                        else if (arg instanceof byte[]) {
                            st.setBytes(index, (byte[]) arg);
                        }

                        else {
                            throw new RuntimeException("Unkown arg type:" + arg.getClass().getName());
                        }

                        index++;
                    }
                    st.executeUpdate();
                }
                conn.commit();
                return true;
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                DAOThreadContext c = new DAOThreadContext(conn, st);
                DAOThreadContext.setContext(c);

            }
        }

        public void executeBatch(String[] sqls) throws SQLException {

            Statement st = null;
            Connection conn = null;

            try {
                conn = fac.getConnection();
                st = conn.createStatement();
                conn.setAutoCommit(false);
                for (int i = 0; i < sqls.length; i++) {
                    st.execute(sqls[i]);
                }
                conn.commit();
            }
            catch (SQLException e) {
                throw e;
            }
            finally {
                DAOThreadContext c = new DAOThreadContext(conn, st);
                DAOThreadContext.setContext(c);
            }
        }

        public void free() {

            DAOThreadContext c = DAOThreadContext.getContext();

            ResultSet rs = c.getRs();
            Connection conn = c.getConn();
            Statement st = c.getSt();

            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }

            try {
                if (st != null) {
                    st.close(); // 关闭Statement
                }
            }
            catch (SQLException e) {
                // ignore
            }
            finally {
                try {
                    if (conn != null) {
                        conn.close(); // 关闭连接
                    }
                }
                catch (SQLException e) {
                    // ignore
                }

            }

            DAOThreadContext.removeContext();
        }

    }

    /**
     * build a DAOFactory
     * 
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
    public static DAOFactory buildDAOFactory(String driverClassName, String jdbcURL, String userName,
            String userPassword, int initPoolSize, int MinPoolSize, int MaxPoolSize, int maxIdleTime,
            int idleConnectionTestPeriod, String testQuerySQL) {

        return new DAOFactory(driverClassName, jdbcURL, userName, userPassword, initPoolSize, MinPoolSize, MaxPoolSize,
                maxIdleTime, idleConnectionTestPeriod, testQuerySQL);
    }

    private final ComboPooledDataSource ds;

    protected DAOFactory(String driverClassName, String jdbcURL, String userName, String userPassword, int initPoolSize,
            int MinPoolSize, int MaxPoolSize) {
        this(driverClassName, jdbcURL, userName, userPassword, initPoolSize, MinPoolSize, MaxPoolSize, 30000, 180000,
                "select 1 from DUAL");
    }

    protected DAOFactory(String driverClassName, String jdbcURL, String userName, String userPassword, int initPoolSize,
            int MinPoolSize, int MaxPoolSize, int maxIdleTime, int idleConnectionTestPeriod, String testQuerySQL) {

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
        ds.setMaxStatementsPerConnection(200);

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

    public QueryHelper getQueryHelper() {

        return new QueryHelper(this);
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
}
