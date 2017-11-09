package com.creditease.monitorframework.fat.struts;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;

import com.opensymphony.xwork2.ActionSupport;

public class TestStruts2ActionAnno4 extends ActionSupport {

    @Action(value = "anno4", interceptorRefs = @InterceptorRef("auth"), results = {
            @Result(type = "stream", params = { "inputName", "inputStream", "contentDisposition",
                    "attachment;filename=\"${downloadFileName}\"", "bufferSize", "512" }) }, params = {})
    // @Action(value = "anno4")
    public void anno4() throws IOException {

        ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
        PrintWriter out = ServletActionContext.getResponse().getWriter();
        try {
            out.print("TestStruts2ActionAnno4 anno4()");
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
