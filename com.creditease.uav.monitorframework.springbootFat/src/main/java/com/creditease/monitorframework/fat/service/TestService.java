package com.creditease.monitorframework.fat.service;

import javax.jws.WebService;

@WebService(name = "TestService", serviceName = "TestService")
public class TestService {

    public String echo() {

        return "hello test";
    }

    @SuppressWarnings("null")
    public String echoFault() {

        Object o = null;
        o.hashCode();

        return "hello fault";
    }

}
