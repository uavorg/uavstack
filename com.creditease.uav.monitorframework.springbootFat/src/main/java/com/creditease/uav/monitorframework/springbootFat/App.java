package com.creditease.uav.monitorframework.springbootFat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ServletComponentScan(basePackages = { "com.creditease.monitorframework.fat",
        "com.creditease.uav.monitorframework.springbootFat" })
@ImportResource("classpath:config.xml")
public class App {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(App.class, args);
        System.out.println(SpringApplication.class.getClassLoader());
    }

}