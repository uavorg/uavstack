# UAVStack
UAVStack是由宜信技术研发中心推出的智能化服务技术栈，是研发运维一体化的解决方案。UAV是无人机的缩写，寓意无人机翱翔蓝天，智能的，透明的完成任务。
它包括任务机器人（代号HIT），全维监控（代号UAV.Monitor）,应用性能管理（代号UAV.APM）,服务治理（代号UAV.ServiceGovern）,微服务计算（代号UAV.MSCP），用户体验管理（代号UAV.UEM）等。

目前UAVStack开源系列 ( 官方网站：https://uavorg.github.io/main/ ) 包括
![Uavstack开源系列](https://uavorg.github.io/main/index/img/support/openSource.png)
# 工程说明
uavstack是UAVStack的All in One源代码仓库。

## 根POM
* com.creditease.uav.superpom  根POM，包含依赖管理，Build所需plugins等

## UAV.Monitor
* com.creditease.uav.agent             监控代理程序MonitorAgent核心框架
* com.creditease.uav.healthmanager     Monitor核心服务，包括实时数据，画像数据存储/查询，实时报警流式计算，AppHub支持服务
* com.creditease.uav.notifycenter      Monitor服务：报警中心服务端
* com.creditease.uav.monitorframework  探针MOF核心框架
* com.creditease.uav.monitorframework.agent   探针MOF的javaagent实现
* com.creditease.uav.monitorframework.dproxy  javaassit支持
* com.creditease.uav.hook.*          探针MOF针对技术规范的钩子，包括Dubbo，HttpClients,JAXWS,JDBC,MongoClients,MQ,Redis等
* com.creditease.uav.loghook         探针MOF针对log4j,logback支持
* com.creditease.uav.tomcat.plus.core  探针MOF对Tomcat/SpringBoot的支持
* com.creditease.uav.jetty.plus.core   探针MOF对Jetty支持
* com.creditease.uav.mock.sl4j       去除某些框架依赖sl4j
* com.creditease.uav.ttl             com.alibaba.ttl源代码引入
* com.creditease.uav.monitorframework.buildFat        WAR测试程序
* com.creditease.uav.monitorframework.springbootFat   SpringBoot测试程序
* com.creditease.uav.agent.buildComponent             制作监控代理程序部署包
* com.creditease.uav.healthmanager.buildComponent     制作健康管理程序部署包
* com.creditease.uav.monitorframework.buildComponent  制作探针MOF部署包

## UAV.APM
* com.creditease.uav.collect          APM归集客户端/服务端
* com.creditease.uav.invokechain      调用链，日志服务存储和查询
* com.creditease.uav.monitorframework.apm  探针MOF的APM支持
* com.creditease.uav.threadanalysis        一键式线程分析客户端和服务端

## UAV.MSCP
* com.creditease.uav.base              MSCP核心框架
* com.creditease.uav.agent.heartbeat   心跳客户端，心跳服务端，节点远程操控，进程扫描，进程值守
* com.creditease.uav.annoscan          Fastclasspathscanner源代码引入，注解Class扫描
* com.creditease.uav.cache.redis       基于Redis的CacheManager
* com.creditease.uav.dbaccess        存储管理器，目前封装了对MySql，OpenTSDB，HBase，MongoDB的存储和查询
* com.creditease.uav.elasticsearch.client ElasticSearch的Shaded客户端封装，避免冲突
* com.creditease.uav.fastjson           Fastjson源代码引入
* com.creditease.uav.helper             Util型支持类库
* com.creditease.uav.httpasync          Http异步通信客户端，封装Apache AsyncClient
* com.creditease.uav.logging            MSCP日志支持
* com.creditease.uav.messaging          MSCP消息发送者和消费者支持，无需关心底层消息服务，依赖com.creditease.uav.mq
* com.creditease.uav.mq                 消息队列服务底层封装，目前使用RocketMQ      
* com.creditease.uav.notifymanager      MSCP组件级报警支持     
* com.creditease.uav.upgrade            MSCP升级客户端和服务端

## UAV.AppHub
* com.creditease.uav.console           UAVStack交互前端，包括Monitor，APM等前端交互功能
* com.creditease.uav.console.buildComponent    制作AppHub部署包

## UAV.ServiceGovern
* 服务自动注册包含与探针MOF代码中
* 服务发现代码包含与健康管理程序画像数据查询中
* 服务降级保护（代码整理中，陆续更新...）
* 服务授权（代码整理中，陆续更新...）

# 如何构建
1. build com.creditease.uav.superpom
> cd com.creditease.uav.superpom

> mvn clean install -Dmaven.test.skip=true

2. build 中间件增强框架（MOF探针）
> cd com.creditease.uav.monitorframework.buildComponent

> mvn clean install -Dmaven.test.skip=true

成功后，可见如下目录
   ```
  com.creditease.uav.monitorframework.buildComponent
  - target
    - build
    - uavmof_1.0_xxxxxxxxxxxxx.zip
    - uavmof_1.0_xxxxxxxxxxxxx.zip.md5
   ```

3. build 监控代理程序(MA)
> cd com.creditease.uav.agent.buildComponent

> mvn clean install -Dmaven.test.skip=true

成功后，可见如下目录
   ```
  com.creditease.uav.agent.buildComponent
  - target
    - build
    - uavagent_1.0_xxxxxxxxxxxxx.zip
    - uavagent_1.0_xxxxxxxxxxxxx.zip.md5
   ```
4. build 健康管理服务(HM)
> cd com.creditease.uav.healthmanager.buildComponent

> mvn clean install -Dmaven.test.skip=true

成功后，可见如下目录
   ```
  com.creditease.uav.healthmanager.buildComponent
  - target
    - build
    - uavhm_1.0_xxxxxxxxxxxxx.zip
    - uavhm_1.0_xxxxxxxxxxxxx.zip.md5
   ```
5. build AppHub
> cd com.creditease.uav.console

> mvn clean install -Dmaven.test.skip=true

成功后，可见如下目录
   ```
  com.creditease.uav.console
  - target
    - com.creditease.uav.console-1.0.war
   ```
# 文档中心
## 全维监控UAV.Monitor+APM
* [用户指南](https://uavorg.github.io/main/uavdoc_useroperation/index.html)
* [安装部署](https://uavorg.github.io/main/uavdoc_deploydocs/index.html)
* [架构说明](https://uavorg.github.io/main/uavdoc_architecture/index.html)

# 下载中心
## 全维监控UAV.Monitor+APM
* [MOF探针](http://pan.baidu.com/s/1c1P0rni)
* [监控代理程序](http://pan.baidu.com/s/1cD9tuu)
* [健康管理服务](http://pan.baidu.com/s/1eROaqEA)
* [AppHub](http://pan.baidu.com/s/1dEBlhwX)

## 第三方下载
* [rocket.war](http://pan.baidu.com/s/1pKCmJ3P)

## AllInOne开发演示版
* [Windows64位](http://pan.baidu.com/s/1boA9p75)




