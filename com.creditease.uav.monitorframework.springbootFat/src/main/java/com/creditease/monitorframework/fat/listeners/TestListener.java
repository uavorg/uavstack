package com.creditease.monitorframework.fat.listeners;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent event) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {

        System.out.println("LISTENER==>" + TestListener.class.getName());
    }

}
