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

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 
 * 测试transmitable-thread-local是否有效
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("ttl")
public class TtlService {

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String aredistest() {

        Test test = new Test();
        // Test.THREAD_LOCAL.set("test1");
        // System.out.println(Test.THREAD_LOCAL.get());
        // test.testSimpleThread();
        // test.testThreadPool();
        // Test.THREAD_LOCAL.set("test2");
        // System.out.println(Test.THREAD_LOCAL.get());
        // test.testSimpleThread();
        // test.testThreadPool();
        //
        // test.testThreadPool2();
        // System.out.println(Test.THREAD_LOCAL.get());
        //
        // test.testThreadPool3();
        // System.out.println(Test.THREAD_LOCAL.get());

        test.testThreadPool4();
        System.out.println(Test.THREAD_LOCAL.get());
        return "transmitable-thread-local success";
    }
}
