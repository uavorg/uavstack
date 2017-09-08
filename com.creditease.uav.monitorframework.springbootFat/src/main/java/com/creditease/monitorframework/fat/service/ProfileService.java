package com.creditease.monitorframework.fat.service;

import javax.jws.WebService;

@WebService(name = "ProfileService")
public interface ProfileService {

    public String doProfile(String arg);

    public void doProfile2(String arg2);
}
