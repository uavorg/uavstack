# UAVStack
UAVStack是由宜信技术研发中心推出的智能化服务技术栈，是研发运维一体化的解决方案。UAV是无人机的缩写，寓意无人机翱翔蓝天，智能的，透明的完成任务。
它包括任务机器人（代号HIT），全维监控（代号UAV.Monitor）,应用性能管理（代号UAV.APM）,服务治理（代号UAV.ServiceGovern）,微服务计算（代号UAV.MSCP），用户体验管理（代号UAV.UEM）等。

UAVStack is an intelligent service technology stack launched by CreditEase Technology Research and Development Center. It is a solution for integration of R & D, operation and maintenance. UAV is the abbreviation of unmanned aerial vehicle (UAV), which means that unmanned aerial vehicle flies into the sky, and completes tasks intelligently and transparently.

It includes AIOps Robot (HIT),full dimension monitoring (UAV.Monitor), application performance management (UAV.APM), service governance (UAV.ServiceGovern), micro-service computing (UAV.MSCP), user experience management (UAV.UEM) etc..


目前UAVStack开源系列 ( 官方网站：https://uavorg.github.io/main/ ) 包括

UAVStack Open Source series includes UAV.Monitor, UAV.APM, UAV.ServiceGovern, UAV.MSCP 

![Uavstack开源系列](https://uavorg.github.io/main/index/img/support/openSource.png)

# 工程说明 Source Code Project Description
uavstack是UAVStack的All in One源代码仓库。

uavstack is the All in One source repository of UAVStack.

## 根POM
* com.creditease.uav.superpom  根POM，包含依赖管理，Build所需plugins等 (Parent POM for dependency management & build plugins)

## UAV.Monitor
* com.creditease.uav.agent             监控代理程序MonitorAgent核心框架 (Core of MonitorAgent)
* com.creditease.uav.healthmanager     Monitor核心服务，包括实时数据，画像数据存储/查询，实时报警流式计算，AppHub支持服务 (Core feature of HealthManager, including Monitoring Data Service，Application/Service Profiling，Real-time alarm streaming，AppHub Support Service)
* com.creditease.uav.notifycenter      Monitor服务：报警中心服务端 (Notification Center Service)
* com.creditease.uav.monitorframework  探针MOF核心框架 （Core of JVM MonitorFramework for Tomcat,Jetty,SpringBoot,MSCP, etc...）
* com.creditease.uav.monitorframework.agent   探针MOF的javaagent实现 (JVM MonitorFramework javaagent support)
* com.creditease.uav.monitorframework.dproxy  javaassit支持 (JVM MonitorFramework javassit support)
* com.creditease.uav.hook.*          探针MOF针对技术规范的钩子，包括Dubbo，HttpClients,JAXWS,JDBC,MongoClients,MQ,Redis等 (JVM MonitorFramework Hooks for application framework)
* com.creditease.uav.loghook         探针MOF针对log4j,logback支持 (JVM MonitorFramework Hook for log4j, logback)
* com.creditease.uav.tomcat.plus.core  探针MOF对Tomcat/SpringBoot的支持 (JVM MonitorFramework extension for Tomcat/SpringBoot)
* com.creditease.uav.jetty.plus.core   探针MOF对Jetty支持 (JVM MonitorFramework extension for Jetty)
* com.creditease.uav.mock.sl4j       去除某些框架依赖sl4j (mock sl4j)
* com.creditease.uav.ttl             com.alibaba.ttl源代码引入 (Alibaba TTL)
* com.creditease.uav.monitorframework.buildFat        WAR测试程序 (Function Test of JVM MonitorFramework)
* com.creditease.uav.monitorframework.springbootFat   SpringBoot测试程序 (Function Test of JVM MonitorFramework for springboot)
* com.creditease.uav.agent.buildComponent             制作监控代理程序部署包 (Build POM for MonitorAgent)
* com.creditease.uav.healthmanager.buildComponent     制作健康管理程序部署包 (Build POM for HealthManager)
* com.creditease.uav.monitorframework.buildComponent  制作探针MOF部署包     (Build POM for JVM MonitorFramework)

## UAV.APM
* com.creditease.uav.collect          APM归集客户端/服务端 (Core of APM Data Collection Client & Service)
* com.creditease.uav.invokechain      调用链，日志服务存储和查询 （InvokeChain， Log Collection 2.0）
* com.creditease.uav.monitorframework.apm  探针MOF的APM支持 （JVM MonitorFramework extension for APM）
* com.creditease.uav.threadanalysis        一键式线程分析客户端和服务端 (One Shot Thread Analysis Client & Service)

## UAV.MSCP
* com.creditease.uav.base              MSCP核心框架 (Core of MSCP)
* com.creditease.uav.agent.heartbeat   心跳客户端，心跳服务端，节点远程操控，进程扫描，进程值守 (Base feature of MSCP including heartbeat client & service，node remote opertaions，process auto scanning，process keep-alive support)
* com.creditease.uav.annoscan          Fastclasspathscanner源代码引入，注解Class扫描 （Fastclasspathscanner）
* com.creditease.uav.cache.redis       基于Redis的CacheManager （Cache Framework based on redis）
* com.creditease.uav.dbaccess        存储管理器，目前封装了对MySql，OpenTSDB，HBase，MongoDB的存储和查询 （Data Store Framework for MySQL，OpenTSDB，HBase，MongoDB）
* com.creditease.uav.elasticsearch.client ElasticSearch的Shaded客户端封装，避免冲突 (ElasticSearch-Shaded-Client)
* com.creditease.uav.fastjson           Fastjson源代码引入 (FastJson)
* com.creditease.uav.helper             Util型支持类库 (Util Support Lib)
* com.creditease.uav.httpasync          Http异步通信客户端，封装Apache AsyncClient (RPC Framework based on Apache Http AsyncClient)
* com.creditease.uav.logging            MSCP日志支持 (MSCP Logging support)
* com.creditease.uav.messaging          MSCP消息发送者和消费者支持，无需关心底层消息服务，依赖com.creditease.uav.mq (Messaging Service)
* com.creditease.uav.mq                 消息队列服务底层封装，目前使用RocketMQ  (Message Queue Support based on Rocket MQ)    
* com.creditease.uav.notifymanager      MSCP组件级报警支持     (MSCP Component level notification support)
* com.creditease.uav.upgrade            MSCP升级客户端和服务端 (Remote upgrading support for MSCP)

## UAV.AppHub
* com.creditease.uav.console           UAVStack交互前端，包括Monitor，APM等前端交互功能 (AppHub is the GUI console for UAVStack)
* com.creditease.uav.console.buildComponent    制作AppHub部署包 （Build POM for AppHub）

## UAV.ServiceGovern
* 服务自动注册包含与探针MOF代码中
* 服务发现代码包含与健康管理程序画像数据查询中
* 服务降级保护（代码整理中，陆续更新...）
* 服务授权（代码整理中，陆续更新...）

# 如何构建 How to Build
如果由于缺少jar包或某些依赖jar无法下载导致Maven Build Failure，可下载[参考Maven依赖仓库](http://pan.baidu.com/s/1i5veR33)，使用该Maven仓库或将其复制到你正在使用的Maven仓库中。

If you are unable to build because of the lack of jar packages or certain dependency jar, pleaser refer this [Maven Repository](http://pan.baidu.com/s/1i5veR33), use the Maven repository, or copy it to the Maven warehouse you are using.

1. build com.creditease.uav.superpom
```
cd com.creditease.uav.superpom
mvn clean install 
```
2 中间件增强框架（MOF探针）
```
cd com.creditease.uav.monitorframework.buildComponent
mvn clean install 
```

build结果  
>target   
> -build   
> -uavmof_1.0_XXXXX.zip   
> -uavmof_1.0_XXXXX.zip.MD5   

3. 监控代理程序(MA)
```
cd com.creditease.uav.agent.buildComponent
mvn clean install 
```

build结果   
>target   
> -build   
> -uavagent_1.0_XXXXX.zip   
> -uavagent_1.0_XXXXX.zip.MD5   

4. 健康管理服务(HM)
```
cd com.creditease.uav.healthmanager.buildComponent
mvn clean install 
```

build结果   
>target   
> -build   
> -uavhm_1.0_XXXXX.zip   
> -uavhm_1.0_XXXXX.zip.MD5   

5. AppHub
```
cd com.creditease.uav.console
mvn clean install 
```

build结果   
>target   
> -com.creditease.uav.console-1.0.war   

# 文档中心 Documents
## 全维监控UAV.Monitor+APM
* [用户指南](https://uavorg.github.io/main/uavdoc_useroperation/index.html)
* [安装部署](https://uavorg.github.io/main/uavdoc_deploydocs/index.html)
* [架构说明](https://uavorg.github.io/main/uavdoc_architecture/index.html)

# 下载中心 Downloads
## 全维监控UAV.Monitor+APM
* [MOF探针](http://pan.baidu.com/s/1c1P0rni)
* [监控代理程序](http://pan.baidu.com/s/1cD9tuu)
* [健康管理服务](http://pan.baidu.com/s/1eROaqEA)
* [AppHub](http://pan.baidu.com/s/1dEBlhwX)

## 第三方下载 Thirdparty Downloads
* [rocket.war](http://pan.baidu.com/s/1pKCmJ3P)

## AllInOne开发演示版 AllInOne Install Downloads
开发演示版的健康管理服务(HM)仅适合开发环境，演示环境，小规模测试环境。
生产环境推荐使用[分布式部署](https://uavorg.github.io/main/uavdoc_deploydocs/healmanagerInstall/healmanagerInstall/microservice.html)

* [Windows64位](http://pan.baidu.com/s/1boA9p75)
* [Mac](http://pan.baidu.com/s/1boOMZ2f)
* [Linux(CentOS)](http://pan.baidu.com/s/1qYSG5QW)




