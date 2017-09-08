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

package com.creditease.monitorframework.fat.invokechain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * 测试与其他系统通过jdbc通信的
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("jdbc")
public class JdbcService {

    /**
     * mysql测试用例
     * 
     * @return
     */
    @GET
    @Path("jdbc")
    public String jdbctest() {

        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

            System.out.println("Statement -------------------->");

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            System.out.println("PreparedStatement -------------------->");

            PreparedStatement ps = c.prepareStatement("insert into mytest values (?,?,?)");

            ps.setInt(1, 1);

            ps.setString(2, "zz");

            ps.setInt(3, 23);

            ps.execute();

            ps.close();

            ps = c.prepareStatement("select name from mytest where id=?");

            ps.setInt(1, 1);

            ps.executeQuery();

            ps.close();

            ps = c.prepareStatement("update mytest set age=24 where id=?");

            ps.setInt(1, 1);

            ps.executeUpdate();

            ps.close();

            ps = c.prepareStatement("delete from mytest where id=?");

            ps.setInt(1, 1);

            ps.executeUpdate();

            ps.close();

            c.close();
            return "jdbcSuccess";
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "jdbc连接出现了问题";
        }
    }

    /**
     * mysql 批量测试用例
     * 
     * @return
     */
    @GET
    @Path("jdbc_batch")
    public String jdbcBatchTest() {

        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

            PreparedStatement ps = c.prepareStatement("insert into mytest values (?,?,?)");

            ps.setInt(1, 1);

            ps.setString(2, "zz");

            ps.setInt(3, 23);

            ps.addBatch();
            ps.setInt(1, 1);

            ps.setString(2, "ss");

            ps.setInt(3, 26);
            ps.addBatch();
            ps.executeBatch();

            ps.close();

            c.close();
            return "jdbcSuccess";
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "jdbc连接出现了问题";
        }
    }

    /**
     * mysql PreparedStatement测试用例
     * 
     * @return
     */
    @GET
    @Path("jdbc_prepared_statement")
    public String jdbcPreparedStatementTest() {

        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

            PreparedStatement ps = c.prepareStatement("insert into mytest values (?,?,?)");

            ps.setInt(1, 1);

            ps.setString(2, "zz");

            ps.setInt(3, 23);

            ps.execute();

            ps.close();

            c.close();
            return "jdbcSuccess";
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "jdbc连接出现了问题";
        }
    }

    /**
     * mysql PreparedStatement测试用例
     * 
     * @return
     */
    @GET
    @Path("jdbc_statement")
    public String jdbcStatementTest() {

        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            System.out.println("+++" + st.executeUpdate("delete from mytest where id=1"));
            System.out.println("         " + st.getUpdateCount());

            st.close();
            return "jdbcSuccess";
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "jdbc连接出现了问题";
        }
    }
}
