package com.creditease.monitorframework.fat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TestClient implements Runnable {

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new TestClient());

            t.start();
        }
        for (;;) {
            try {
                Thread.sleep(300000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void access() {

        try {
            URL url = new URL("http://localhost:8080/com.creditease.uav.monitorframework.buildFat/CXFClientE2ETest");
            URLConnection conn = url.openConnection();

            conn.getInputStream().close();
            // conn.getOutputStream().close();
        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Thread.sleep(3);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {
            access();
        }
    }

}
