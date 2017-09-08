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

package com.creditease.monitorframework.fat.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.ttl.TransmittableThreadLocal;

public class QueueTest {

    public static final TransmittableThreadLocal<String> THREAD_LOCAL = new TransmittableThreadLocal<String>();

    public static final BlockingQueue<Msg> queue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

        QueueTest.THREAD_LOCAL.set("main");
        ExecutorService producers = Executors.newFixedThreadPool(2);
        ExecutorService consumers = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            QueueTest.THREAD_LOCAL.set("main" + i);
            producers.submit(new Producer(i, queue));
        }
        for (int i = 0; i < 10; i++) {
            consumers.submit(new Consumer(i, queue));
        }

        try {
            Thread.sleep(5 * 1000);
            producers.shutdown();
            consumers.shutdown();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
