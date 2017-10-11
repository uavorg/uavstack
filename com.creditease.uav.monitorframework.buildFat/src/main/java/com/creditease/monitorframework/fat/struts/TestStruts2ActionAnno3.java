package com.creditease.monitorframework.fat.struts;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;

import com.opensymphony.xwork2.ActionSupport;

public class TestStruts2ActionAnno3 extends ActionSupport {

    @Action("anno3")
    public void anno3() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2ActionAnno3 anno3()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
