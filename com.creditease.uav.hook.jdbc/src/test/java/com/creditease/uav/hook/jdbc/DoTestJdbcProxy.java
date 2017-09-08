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

package com.creditease.uav.hook.jdbc;

import com.creditease.uav.util.MonitorServerUtil;
import com.creditease.uav.util.MonitorServerUtil.OracleTokenBuffer;

public class DoTestJdbcProxy {

    public static void main(String[] args) {

        testOracleDBUrlParser();

        // testJDBC();
    }

    private static void testOracleDBUrlParser() {

        String str = "jdbc:oracle:thin:@ (DESCRIPTION= (FAILOVER = yes)(ADDRESS = (PROTOCOL = TCP)(HOST =127.0.0.1)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)(HOST =127.0.0.1)(PORT = 1521)) (CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = cedb)))";

        MonitorServerUtil.OracleTokenBuffer otb = new MonitorServerUtil.OracleTokenBuffer(str);

        System.out.println(otb.toString());

        str = "jdbc:oracle:thin:@127.0.0.1:1521:testdb";

        otb = new OracleTokenBuffer(str);

        System.out.println(otb.toString());
    }

    // private static void testJDBC() {
    //
    // ConsoleLogger cl = new ConsoleLogger("test");
    //
    // cl.setDebugable(true);
    //
    // UAVServer.instance().setLog(cl);
    //
    // UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);
    //
    // JdbcHookProxy p = new JdbcHookProxy("test", Collections.emptyMap());
    // p.doInstallDProxy(null, "testApp", null);
    //
    // try {
    // Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");
    //
    // System.out.println("Statement -------------------->");
    //
    // Statement st = c.createStatement();
    //
    // st.execute("insert into mytest values (1,'zz',23)");
    //
    // st.executeQuery("select name from mytest where id=1");
    //
    // st.executeUpdate("update mytest set age=24 where id=1");
    //
    // st.executeUpdate("delete from mytest where id=1");
    //
    // st.close();
    //
    // System.out.println("PreparedStatement -------------------->");
    //
    // PreparedStatement ps = c.prepareStatement("insert into mytest values (?,?,?)");
    //
    // ps.setInt(1, 1);
    //
    // ps.setString(2, "zz");
    //
    // ps.setInt(3, 23);
    //
    // ps.execute();
    //
    // ps.close();
    //
    // ps = c.prepareStatement("select name from mytest where id=?");
    //
    // ps.setInt(1, 1);
    //
    // ps.executeQuery();
    //
    // ps.close();
    //
    // ps = c.prepareStatement("update mytest set age=24 where id=?");
    //
    // ps.setInt(1, 1);
    //
    // ps.executeUpdate();
    //
    // ps.close();
    //
    // ps = c.prepareStatement("delete from mytest where id=?");
    //
    // ps.setInt(1, 1);
    //
    // ps.executeUpdate();
    //
    // ps.close();
    //
    // c.close();
    // }
    // catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }
}
