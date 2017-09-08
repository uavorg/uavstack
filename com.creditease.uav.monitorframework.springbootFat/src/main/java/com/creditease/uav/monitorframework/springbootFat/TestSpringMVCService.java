package com.creditease.uav.monitorframework.springbootFat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/TestSpringMVCService")
public class TestSpringMVCService {

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping() {

        return "hello spring mvc";
    }

    @RequestMapping(value = "/testEcho", method = RequestMethod.GET)
    public String testEcho(String param) {

        return param;
    }

    @RequestMapping(value = "/testPostMethod1", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public String testPostMethod1(String jsonString) {

        return jsonString;
    }
}
