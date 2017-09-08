package com.creditease.monitorframework.fat.service;

import javax.jws.WebService;

@WebService(serviceName = "ProfileService", endpointInterface = "com.creditease.monitorframework.fat.service.ProfileService")
public class ProfileServiceImpl implements ProfileService {

    @Override
    public String doProfile(String arg) {

        return arg;
    }

    @Override
    public void doProfile2(String arg2) {

        // Do nothing but must pass sonar check
    }

}
