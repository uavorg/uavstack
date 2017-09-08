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

package com.creditease.uav.agent.heartbeat.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.hbagent.HeartBeatClientReqWorker;
import com.creditease.agent.feature.hbagent.handlers.HBClientDefaultHandler;
import com.creditease.agent.feature.hbagent.node.NodeInfo;
import com.creditease.agent.heartbeat.api.HeartBeatEvent;
import com.creditease.agent.heartbeat.api.HeartBeatEvent.Stage;
import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.uav.cache.api.CacheManager;

public class DoTestMockMachineWorkLoad {

    public static String[] systemTags = new String[] { "电子签约.签章服务", "电签签约.电签网关", "电子签约.营业厅交互端", "电子签约.天威出借人系统",
            "电子签约.借款人证书服务", "短信.网关", "短信.定时发送服务", "短信.备份服务", "短信.统计服务", "短信.状态反馈服务" };

    private static class MockNode implements Runnable {

        private HBClientDefaultHandler clientHandler;

        private int index = 0;

        private int macIndex = 0;

        private Random rand = new Random();

        public MockNode(int index, int macIndex) {
            clientHandler = new HBClientDefaultHandler("test", "test");
            this.index = index;
            this.macIndex = macIndex;
        }

        @Override
        public void run() {

            while (true) {

                HeartBeatEvent hbe = new HeartBeatEvent(Stage.CLIENT_OUT);

                clientHandler.handleClientOut(hbe);

                @SuppressWarnings("unchecked")
                List<String> nodeInfoStringArray = (List<String>) hbe.getParam(HeartBeatProtocol.EVENT_DEFAULT,
                        HeartBeatProtocol.EVENT_KEY_NODE_INFO);

                // sync node info to storage
                CacheManager.instance().beginBatch();

                for (String nodeInfoString : nodeInfoStringArray) {

                    NodeInfo ni = NodeInfo.toNodeInfo(nodeInfoString);

                    ni.setIp("10.1.100." + this.macIndex);

                    ni.setHost("testHost-" + this.macIndex);

                    ni.setId("100" + index);

                    Map<String, String> info = ni.getInfo();

                    info.put("cpu.load", String.valueOf(rand.nextInt(95)));

                    long maxmem = Long.parseLong(info.get("os.cpu.maxmem"));

                    info.put("os.cpu.freemem", String.valueOf(Math.abs((rand.nextInt(100) * maxmem) / 100)));

                    info.put("os.conn.cur", String.valueOf(rand.nextInt(1600) + 500));

                    info.put("node.hbserver", "127.0.0.1:8010");

                    info.put("node.pid", "1" + rand.nextInt(4141));

                    info.put("node.tags", systemTags[rand.nextInt(9)]);

                    info.put("node.procs", getNodeProcs());

                    // set server side timestamp
                    ni.setServerTimestamp(System.currentTimeMillis());

                    System.out.println(ni.toJSONString());

                    CacheManager.instance().putHash(HeartBeatProtocol.STORE_REGION_UAV,
                            HeartBeatProtocol.STORE_KEY_NODEINFO, ni.getId(), ni.toJSONString());
                }

                CacheManager.instance().submitBatch();

                ThreadHelper.suspend(5000);
            }
        }
    }

    private static String getNodeProcs() {

        String str = "{\"1060\":{\"name\":\"svchost.exe\",\"pid\":\"1060\",\"ports\":[\"135\"],\"tags\":{}},\"4\":{\"name\":\"UNKOWN\",\"pid\":\"4\",\"ports\":[\"5357\",\"445\",\"139\"],\"tags\":{}},\"7612\":{\"name\":\"java.exe\",\"pid\":\"7612\",\"ports\":[\"2181\"],\"tags\":{\"main\":\"org.apache.zookeeper.server.quorum.QuorumPeerMain\",\"margs\":\"D:\\\\Apps\\\\zookeeper-3.4.7\\\\zookeeper-3.4.7\\\\bin\\\\..\\\\conf\\\\zoo.cfg\",\"jargs\":\"-Dzookeeper.log.dir=D:\\\\Apps\\\\zookeeper-3.4.7\\\\zookeeper-3.4.7\\\\bin\\\\.. -Dzookeeper.root.logger=INFO,CONSOLE\",\"jflags\":\"\"}},\"9668\":{\"name\":\"redis-server.exe\",\"pid\":\"9668\",\"ports\":[\"6379\"],\"tags\":{}},\"16648\":{\"name\":\"javaw.exe\",\"pid\":\"16648\",\"ports\":[\"64315\",\"8005\",\"8009\",\"8080\"],\"tags\":{\"main\":\"org.apache.catalina.startup.Bootstrap\",\"margs\":\"start\",\"jargs\":\"-Dcatalina.home=E:/ZZ/UnionAppServer/Tomcat/bin/apache-tomcat-7.0.65 -Dcatalina.base=E:/ZZ/UnionAppServer/Tomcat/bin/apache-tomcat-7.0.65 -Djava.endorsed.dirs=E:/ZZ/UnionAppServer/Tomcat/bin/apache-tomcat-7.0.65/common/endorsed -Djava.io.tmpdir=E:/ZZ/UnionAppServer/Tomcat/bin/apache-tomcat-7.0.65/temp -Djava.library.path=D:/Apps/mye14/binary/com.sun.java.jdk7.win32.x86_64_1.7.0.u45/bin;E:/ZZ/UnionAppServer/Tomcat/bin/apache-tomcat-7.0.65/bin -Dsun.io.useCanonCaches=false -Dfile.encoding=UTF-8\",\"jflags\":\"\"}},\"18192\":{\"name\":\"javaw.exe\",\"pid\":\"18192\",\"ports\":[\"8010\",\"8765\"],\"tags\":{\"main\":\"com.creditease.agent.SystemStarter\",\"jargs\":\"-Dfile.encoding=UTF-8\",\"jflags\":\"\"}},\"2428\":{\"name\":\"macmnsvc.exe\",\"pid\":\"2428\",\"ports\":[\"8081\"],\"tags\":{}},\"1488\":{\"name\":\"java.exe\",\"pid\":\"1488\",\"ports\":[\"9876\"],\"tags\":{\"main\":\"com.alibaba.rocketmq.namesrv.NamesrvStartup\",\"jargs\":\"-Xms4g -Xmx4g -Xmn2g -XX:PermSize=128m -XX:MaxPermSize=320m -Djava.ext.dirs=E:\\\\ZZ\\\\UnionAppServer\\\\MQ\\\\RocketMQ-master\\\\RocketMQ-master\\\\target\\\\alibaba-rocketmq-3.2.6-alibaba-rocketmq\\\\alibaba-rocketmq/lib\",\"jflags\":\"\"}},\"5844\":{\"name\":\"java.exe\",\"pid\":\"5844\",\"ports\":[\"10911\",\"10912\"],\"tags\":{\"main\":\"com.alibaba.rocketmq.broker.BrokerStartup\",\"margs\":\"-n 127.0.0.1:9876\",\"jargs\":\"-Xms4g -Xmx4g -Xmn2g -XX:PermSize=128m -XX:MaxPermSize=320m -Djava.ext.dirs=E:\\\\ZZ\\\\UnionAppServer\\\\MQ\\\\RocketMQ-master\\\\RocketMQ-master\\\\target\\\\alibaba-rocketmq-3.2.6-alibaba-rocketmq\\\\alibaba-rocketmq/lib\",\"jflags\":\"\"}},\"684\":{\"name\":\"wininit.exe\",\"pid\":\"684\",\"ports\":[\"49152\"],\"tags\":{}},\"1148\":{\"name\":\"svchost.exe\",\"pid\":\"1148\",\"ports\":[\"49153\"],\"tags\":{}},\"1244\":{\"name\":\"svchost.exe\",\"pid\":\"1244\",\"ports\":[\"49155\"],\"tags\":{}},\"764\":{\"name\":\"lsass.exe\",\"pid\":\"764\",\"ports\":[\"49156\"],\"tags\":{}},\"756\":{\"name\":\"services.exe\",\"pid\":\"756\",\"ports\":[\"49158\"],\"tags\":{}},\"7020\":{\"name\":\"SangforPromoteService.exe\",\"pid\":\"7020\",\"ports\":[\"10000\"],\"tags\":{}},\"7256\":{\"name\":\"java\",\"pid\":\"7256\",\"ports\":[],\"tags\":{\"main\":\"C:\\\\Users\\\\ADMINI~1\\\\AppData\\\\Local\\\\Temp\\\\pulA385.tmp\\\\PULSEI~1.JAR\",\"margs\":\"-os win32 -ws win32 -arch x86_64 -showsplash -launcher D:\\\\Apps\\\\mye14\\\\myeclipse.exe -name Myeclipse --launcher.library D:\\\\Apps\\\\mye14\\\\plugins/org.eclipse.equinox.launcher.i18n.win32.win32.x86_64_3.2.0.v201103301700\\\\eclipse_3215.dll -startup D:\\\\Apps\\\\mye14\\\\plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar -exitdata 1c98_98 -install D:\\\\Apps\\\\mye14 -vm D:\\\\Apps\\\\mye14\\\\binary/com.sun.java.jdk7.win32.x86_64_1.7.0.u45/bin/javaw.exe -vmargs -Xmx1536m -XX:MaxPermSize=320m -XX:ReservedCodeCacheSize=64m -Dosgi.nls.warnings=ignore -jar C:\\\\Users\\\\ADMINI~1\\\\AppData\\\\Local\\\\Temp\\\\pulA385.tmp\\\\PULSEI~1.JAR\",\"jargs\":\"-Xmx1536m -XX:MaxPermSize=320m -XX:ReservedCodeCacheSize=64m -Dosgi.nls.warnings=ignore\",\"jflags\":\"\"}},\"12632\":{\"name\":\"java\",\"pid\":\"12632\",\"ports\":[],\"tags\":{\"main\":\"com.creditease.agent.SystemStarter\",\"jargs\":\"-Dfile.encoding=UTF-8\",\"jflags\":\"\"}}}";
        return str;
    }

    public static void main(String[] args) {

        ConfigurationManager.build(new HashMap<String, String>());

        HeartBeatClientReqWorker worker = new HeartBeatClientReqWorker("testReqWorker", "testFeature");

        CacheManager.build("localhost:6379", 10, 100, 10);

        Random rand = new Random();

        int count = 10;

        for (int i = 0; i < count; i++) {

            MockNode node = new MockNode(i, rand.nextInt(254));

            Thread thd = new Thread(node);

            thd.start();

            ThreadHelper.suspend(1000);
        }

        ThreadHelper.suspend(6000 * 1000);
    }
}
