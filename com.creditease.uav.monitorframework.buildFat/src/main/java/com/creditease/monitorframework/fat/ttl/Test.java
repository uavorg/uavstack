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

package com.creditease.monitorframework.fat.ttl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;

public class Test {

    // public static final InheritableThreadLocal<String> THREAD_LOCAL = new InheritableThreadLocal<String>();

    // public static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    public static final TransmittableThreadLocal<String> THREAD_LOCAL = new TransmittableThreadLocal<String>();

    public static void main(String[] args) {

        Test test = new Test();
        Test.THREAD_LOCAL.set("test1");
        System.out.println(Test.THREAD_LOCAL.get());
        test.testSimpleThread();
        test.testThreadPool();
        Test.THREAD_LOCAL.set("test2");
        System.out.println(Test.THREAD_LOCAL.get());
        test.testSimpleThread();
        test.testThreadPool();

        test.testThreadPool2();
        System.out.println(Test.THREAD_LOCAL.get());

        test.testThreadPool3();
        System.out.println(Test.THREAD_LOCAL.get());

        test.testThreadPool4();
        System.out.println(Test.THREAD_LOCAL.get());
    }

    public void testSimpleThread() {

        System.out.println();
        Thread thread = new Thread(new SimpleThread());
        thread.start();
    }

    public void testThreadPool() {

        System.out.println();
        ExecutorService service = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            service.submit(new SimpleThread());
        }
        try {
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        service.shutdown();

    }

    public void testThreadPool2() {

        System.out.println();
        ExecutorService service = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            service.submit(new SimpleThread2(i));
        }
        try {
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        service.shutdown();
    }

    public void testThreadPool3() {

        System.out.println();
        ExecutorService service = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            service.submit(new SimpleThread2(i));
        }
        try {
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Test.THREAD_LOCAL.set("success");
        System.out.println("当前的threadlocal是" + Test.THREAD_LOCAL.get());
        for (int i = 0; i < 10; i++) {
            Test.THREAD_LOCAL.set("success" + i);
            service.submit(TtlRunnable.get(new SimpleThread2(i)));
        }
        try {
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        service.shutdown();
    }

    public void testThreadPool4() {

        System.out.println();
        ExecutorService service = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            service.submit(new SimpleThread2(i));
        }
        try {
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Test.THREAD_LOCAL.set("success");
        System.out.println("当前的threadlocal是" + Test.THREAD_LOCAL.get());
        for (int i = 0; i < 10; i++) {
            Test.THREAD_LOCAL.set("success" + i);
            service.submit(new SimpleThread2(i));
        }
        try {
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        service.shutdown();
    }
}
