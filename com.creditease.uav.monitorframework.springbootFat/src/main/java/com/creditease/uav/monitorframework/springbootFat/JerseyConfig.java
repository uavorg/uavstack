package com.creditease.uav.monitorframework.springbootFat;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.creditease.uav.monitorframework.restservice.RestResource;
import com.creditease.uav.monitorframework.restservice.TestRestService;

@Configuration
public class JerseyConfig extends ResourceConfig {

    @Bean
    public ServletRegistrationBean jerseyServlet() {

        ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), "/rest/*");
        // our rest resources will be available in the path /rest/*
        registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName());
        return registration;
    }

    public JerseyConfig() {
        register(RestResource.class);
        register(TestRestService.class);
    }
}