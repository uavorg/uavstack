package com.creditease.uav.monitorframework.springbootFat;

public interface UserService {

    String getName(Long userId);

    User getUser(Long userId);
}