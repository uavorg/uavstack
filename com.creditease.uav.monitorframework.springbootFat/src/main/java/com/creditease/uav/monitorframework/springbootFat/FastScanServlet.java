package com.creditease.uav.monitorframework.springbootFat;

import java.io.IOException;
import java.util.List;

import javax.jws.WebService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

@WebServlet("/FakeScanServlet")
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

        FastClasspathScanner scanner = new FastClasspathScanner("com.creditease");
        long st = System.nanoTime();
        FastClasspathScanner scannerm1 = scanner.scan();
        long end = System.nanoTime() - st;
        System.out.println(end);
        List<String> lista = scannerm1.getNamesOfClassesWithAnnotation(WebService.class);
        for (String str : lista) {
            System.out.println("webservice m1 -" + str);
        }

        st = System.nanoTime();
        FastClasspathScanner scanner2 = new FastClasspathScanner("com.creditease");
        scanner2.matchClassesWithAnnotation(WebService.class, new ClassAnnotationMatchProcessor() {

            @SuppressWarnings("rawtypes")
            @Override
            public void processMatch(Class arg0) {

                System.out.println("webservice m1 -" + arg0.getName());
            }

        }).scan();
        end = System.nanoTime() - st;
        System.out.println(end);
        resp.getWriter().write("FastClasspathScanner cost:" + end);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        this.doGet(req, resp);
    }

}
