package com.creditease.monitorframework.fat.struts;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;

import com.opensymphony.xwork2.ActionSupport;

@Namespaces({ @Namespace("/a"), @Namespace("/b") })
@Namespace("/anno")
public class TestStruts2ActionAnno extends ActionSupport {

    @Actions({ @Action("aa"), @Action("bb") })
    @Action("anno1")
    public void anno1() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2ActionAnno anno1()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
