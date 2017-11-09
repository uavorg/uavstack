package com.creditease.monitorframework.fat.struts;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.struts2.ServletActionContext;

public class TestStruts2Action {

    public void hello() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2Action hello()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void hello2() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2Action hello2()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void hello3() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2Action hello3()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void hello4() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2Action hello4()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
