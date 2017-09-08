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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.creditease.uav.agent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
// import java.util.ArrayList;
// import java.util.List;

// import org.apache.flume.Transaction;
// import org.apache.flume.channel.ChannelProcessor;
// import org.apache.flume.channel.MemoryChannel;
// import org.apache.flume.channel.ReplicatingChannelSelector;
// import org.apache.flume.conf.Configurables;
// import org.apache.flume.lifecycle.LifecycleController;
// import org.apache.flume.lifecycle.LifecycleState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.LogAgent;
import com.creditease.agent.feature.logagent.RuleFilterFactory;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.spi.ITimerWorkManager;
import com.creditease.agent.workqueue.SystemTimerWorkMgr;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
// import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class DoTestTaildirSource {

    static LogAgent source;
    private File tmpDir;
    @SuppressWarnings("unused")
    private String posFilePath;

    @Before
    public void setUp() throws IOException {

        SystemLogger.init("DEBUG", true, 5);
        source = new LogAgent("Logagent", "Logagent");

        String os = System.getProperty("os.name");
        if (os.indexOf("Windows") != -1)
            tmpDir = new File("F:/temp");
        else
            tmpDir = new File("/Users/fathead/temp");

        posFilePath = tmpDir.getAbsolutePath() + "/taildir_position_test.json";

        // 1) Create 1st file
        File f1 = new File(tmpDir, "file1");
        String line1 = "[CE]file1line1chapter1\tfile1line1chapter2\tfile1line1chapter3\n";// normal sep:\t
        String line2 = "[C]file1line2hiddenchapter1\tfile1line2hiddenchapter2\tfile1line2hiddenchapter3\n";// test [CE]
                                                                                                           // filter
        String line3 = "[CE]file1line3chapter1\tfile1line3chapter2\tfile1line3chapter3\tfile1line3undisplaychapter4\n";// test
                                                                                                                       // plus
                                                                                                                       // chapter
                                                                                                                       // process
        Files.write(line1 + line2 + line3, f1, Charsets.UTF_8);
        try {
            Thread.sleep(1000); // wait before creating a new file
        }
        catch (InterruptedException e) {
        }

        // 1) Create 2nd file
        String line1b = "<CE>file2line1chapter1|file2line1chapter2|file2line1chapter3\n";// normal sep:|
        String line2b = "<CE>file2line2chapter1|file2line2chapter2\n";// test default chapter process
        String line3b = "<CE>file2line3chapter1|file2line3chapter2\tfile2line3chapter3|\n"; // test plus "" chapter
                                                                                            // process
        File f2 = new File(tmpDir, "file2");
        Files.write(line1b + line2b + line3b, f2, Charsets.UTF_8);
        try {
            Thread.sleep(1000); // wait before creating next file
        }
        catch (InterruptedException e) {
        }

        // 3) Create 3rd file
        String line1c = "[CE]file3line1hiddenchapter1|file3line1hiddenchapter2|file3line1hiddenchapter3\n";// test
                                                                                                           // un[CE]
                                                                                                           // filter
        String line2c = "<CE>|file3line2chapter1|\n";
        String line3c = "<CE>|file3line3chapter1\n";// test multi- process
        File f3 = new File(tmpDir, "file3");
        Files.write(line1c + line2c + line3c, f3, Charsets.UTF_8);

        // 5) Now update the 3rd file so that its the latest file and gets
        // consumed last
        f3.setLastModified(System.currentTimeMillis());

        // 4) Consume the files
        Map<String, String> logfile = Maps.newHashMap();
        logfile.put("ser1--app1--" + f1.getName(), f1.getPath());
        logfile.put("ser1--app2--" + f2.getName(), f2.getPath());
        logfile.put("ser1--app2--" + f3.getName(), f3.getPath());
        ITimerWorkManager itwm = new SystemTimerWorkMgr();
        ConfigurationManager.getInstance().registerComponent("Global", "ITimerWorkManager", itwm);
        // source.configure(posFilePath, logfile);
        /**
         * constructor filter rule {\"separator\":\"\t\", \"assignfields\":{\"content\":1}, \"timestamp\": 0}
         */
        RuleFilterFactory.getInstance().newBuilder().serverId("ser1").appId("app1").logId(f1.getName())
                .filterRegex("\\[CE\\].*?")
                .ruleRegex(
                        "{\"separator\":\"\t\", \"assignfields\":{\"content1\":1, \"content2\":2,\"content3\":3}, \"timestamp\": 0}")
                .build();
        RuleFilterFactory.getInstance().newBuilder().serverId("ser1").appId("app2").logId(f2.getName())
                .filterRegex("<CE>.*?")
                .ruleRegex(
                        "{\"separator\":\"|\", \"assignfields\":{\"content1\":1, \"content2\":2,\"content3\":3}, \"timestamp\": 0}")
                .build();
        RuleFilterFactory.getInstance().newBuilder().serverId("ser1").appId("app2").logId(f3.getName())
                .filterRegex("<CE>.*?")
                .ruleRegex(
                        "{\"separator\":\"|\", \"assignfields\":{\"content1\":1, \"content2\":2,\"content3\":3}, \"timestamp\": 0}")
                .build();
        source.start();
    }

    @After
    public void tearDown() {

        for (File f : tmpDir.listFiles()) {
            f.delete();
        }
        source.stop();
    }

    @Test
    public void updatefilterRule() {

        //
        RuleFilterFactory.getInstance();
    }

    // @Test
    // public void updateLogInfo() throws IOException, InterruptedException {
    // String key = "sidlog1--aidlog1--lidlog1";
    // TaildirLogComponent component = ConfigurationManager.getInstance().getComponent(TaildirLogComponent.class);
    // File log1 = new File(tmpDir, "log1.log");
    // Files.write("(ce)log1fileline1\n", log1, Charsets.UTF_8);
    // Map<String, String> filePaths = Maps.newHashMap();
    // filePaths.put(key, log1.getPath());
    //
    // //add log and out
    // component.configure(filePaths);
    // Thread.sleep(5000);
    // Files.write("[CE]log1fileline1\n", log1, Charsets.UTF_8);
    // //modify log and out
    // File log2 = new File(tmpDir, "xxx2.log");
    // Files.write("[CE]xxx2fileline2\n", log2, Charsets.UTF_8);
    // filePaths.put(key, log2.getPath());
    // component.configure(filePaths);
    // Thread.sleep(5000);
    // Files.write("[CE]xxx2fileline3\n", log2, Charsets.UTF_8);
    // Thread.sleep(2000);
    // //delete log and out
    // filePaths.put(key, null);
    // component.configure(filePaths);
    // Files.write("xxx2displayline\n", log2, Charsets.UTF_8);
    // while(true)
    // Thread.sleep(3000);
    // }。。
}
