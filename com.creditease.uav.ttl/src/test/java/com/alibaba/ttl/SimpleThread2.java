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

package com.alibaba.ttl;

public class SimpleThread2 implements Runnable {

    private int id;

    public SimpleThread2(int i) {
        this.id = i;
    }

    @Override
    public void run() {

        System.out.println("before" + id + "---" + Test.THREAD_LOCAL.get());
        Test.THREAD_LOCAL.set(id + "");
        System.out.println("after" + id + "---" + Test.THREAD_LOCAL.get());
    }

}
