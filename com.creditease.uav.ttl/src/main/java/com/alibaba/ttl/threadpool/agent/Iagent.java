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

package com.alibaba.ttl.threadpool.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * 
 * 只是为了测试
 *
 */
public class Iagent {

    public static void premain(String agentArgs, Instrumentation inst) {

        String jarPath = "D:/creditRepository/ce-datamonitorsystem/com.creditease.uav.ttl/target/com.creditease.uav.ttl-2.1.0-agent.jar";

        inst.addTransformer(new TtlTransformer());
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(jarPath));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
