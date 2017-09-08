package com.creditease.uav.monitorframework.springbootFat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public class UserServiceImpl implements UserService {

    private Map<Long, User> userMap = new HashMap<Long, User>();

    public UserServiceImpl() {
        User user = new User();
        user.setUserId(10001L);
        user.setUsername("liyd1");
        user.setEmail("liyd1@xxx");
        user.setGmtCreate(new Date());
        userMap.put(user.getUserId(), user);
        user = new User();
        user.setUserId(10002L);
        user.setUsername("liyd2");
        user.setEmail("liyd2@xxx");
        user.setGmtCreate(new Date());
        userMap.put(user.getUserId(), user);
        user = new User();
        user.setUserId(10003L);
        user.setUsername("liyd3");
        user.setEmail("liyd3@xxx");
        user.setGmtCreate(new Date());
        userMap.put(user.getUserId(), user);
    }

    @Override
    @WebMethod
    public String getName(@WebParam(name = "userId") Long userId) {

        return "liyd-" + userId;
    }

    @Override
    @WebMethod
    public User getUser(Long userId) {

        return userMap.get(userId);
    }

}
