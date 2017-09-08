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

public class Producer implements Runnable {

    private int id;

    private BlockingQueue<Msg> queue;

    public Producer(int id, BlockingQueue<Msg> queue) {
        this.id = id;
        this.queue = queue;
    }

    @Override
    public void run() {

        System.out.println("producer--" + id + "--" + QueueTest.THREAD_LOCAL.get());
        // 生产消息msg
        try {
            Thread.sleep(1 * 1000);
            this.queue.put(new Msg());
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
