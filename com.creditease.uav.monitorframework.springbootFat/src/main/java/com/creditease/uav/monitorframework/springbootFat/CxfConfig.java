package com.creditease.uav.monitorframework.springbootFat;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfConfig {

    @Bean
    public ServletRegistrationBean dispatcherCXFServlet() {

        return new ServletRegistrationBean(new CXFServlet(), "/soap/*");
    }
    //
    // @Bean(name = SpringBus.DEFAULT_BUS_ID)
    // public SpringBus springBus() {
    //
    // return new SpringBus();
    // }
    //
    // @Bean
    // public UserService userService() {
    //
    // return new UserServiceImpl();
    // }
    //
    // @Bean
    // public Endpoint endpoint() {
    //
    // EndpointImpl endpoint = new EndpointImpl(new SpringBus(), userService());
    // endpoint.publish("/user");
    // return endpoint;
    // }
}