package com.creditease.uav.monitorframework.springbootFat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TestSpringApiController {

    @RequestMapping(value = "/api/doAService", method = RequestMethod.GET)
    public String ping() {

        return "hello spring mvc without class req-mapping";
    }
}
