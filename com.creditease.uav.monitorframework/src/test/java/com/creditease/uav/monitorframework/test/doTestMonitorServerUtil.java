package com.creditease.uav.monitorframework.test;

import com.creditease.uav.util.MonitorServerUtil.OracleTokenBuffer;

public class doTestMonitorServerUtil {

    public static void main(String[] args) {

        OracleTokenBuffer test1 = new OracleTokenBuffer("jdbc:oracle:thin:@127.0.0.1:1521/fso");
        System.out.println(test1);

        OracleTokenBuffer test2 = new OracleTokenBuffer("jdbc:oracle:thin:@127.0.0.1:1521:fso");
        System.out.println(test2);
    }
}
