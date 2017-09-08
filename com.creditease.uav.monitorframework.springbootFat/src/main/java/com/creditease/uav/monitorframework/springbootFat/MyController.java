package com.creditease.uav.monitorframework.springbootFat;

import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/controller")
@Lazy
public class MyController {

    @RequestMapping("/app")
    public String home() {

        return this.getClass().getClassLoader().toString();
    }
}
