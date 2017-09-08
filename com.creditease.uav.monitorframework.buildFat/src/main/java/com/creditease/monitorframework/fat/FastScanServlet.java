/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.monitorframework.fat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/FakeScanServlet" }, loadOnStartup = 1, name = "FastScanServlet", asyncSupported = false, smallIcon = "smallIcon", largeIcon = "largeIcon", description = "description", displayName = "displayName", initParams = {
                @WebInitParam(name = "initP", value = "initPvalue") })
public class FastScanServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6033883738087954911L;

    @Override
    public void init() throws ServletException {

        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // FastClasspathScanner scanner = new FastClasspathScanner("com.creditease");
        // long st = System.nanoTime();
        // FastClasspathScanner scannerm1 = scanner.scan();
        // long end = System.nanoTime() - st;
        // System.out.println(end);
        // List<String> lista = scannerm1.getNamesOfClassesWithAnnotation(WebService.class);
        // for (String str : lista) {
        // System.out.println("webservice m1 -" + str);
        // }
        //
        // st = System.nanoTime();
        // FastClasspathScanner scanner2 = new FastClasspathScanner("com.creditease");
        // scanner2.matchClassesWithAnnotation(WebService.class, new ClassAnnotationMatchProcessor() {
        //
        // @SuppressWarnings("rawtypes")
        // @Override
        // public void processMatch(Class arg0) {
        //
        // System.out.println("webservice m1 -" + arg0.getName());
        // }
        //
        // }).scan();
        // end = System.nanoTime() - st;
        // System.out.println(end);
        // resp.getWriter().write("FastClasspathScanner cost:" + end);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        this.doGet(req, resp);
    }

}
