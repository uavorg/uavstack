# UAVStack
中文版README请点击(https://github.com/uavorg/uavstack/blob/master/README%E4%B8%AD%E6%96%87%E7%89%88.md)

UAVStack as an intelligent service technology stack is the integrated solution to R&D, operation and maintenance. As the acronym of unmanned aerial vehicle, UAV depicts the scenario where the unmanned aerial vehicle hovers under the sky and completes tasks in an intelligent and transparent manner. This stack features AIOps Robot (HIT), full dimension monitoring (UAV.Monitor), application performance management (UAV.APM), service governance (UAV.ServiceGovern), micro-service computing (UAV.MSCP), user experience management (UAV.UEM), etc..

Please visit https://uavorg.github.io/main/ for the open source series of UAVStack, including UAV.Monitor, UAV.APM, UAV.ServiceGovern and UAV.MSCP

![Uavstack开源系列](https://uavorg.github.io/main/index/img/support/openSource.png)

# Updating Rules
* Updates of the open source series (at least once a week) shall synchronize with those of the internal versions.
* Feature optimization of the open source series (at least once a week) shall synchronize with that of the internal versions as well.
* New features of the open source series shall be preannounced one week earlier and released within the following week.
* The standard deployment package shall synchronize with the updated source code (at least once a week).
* The AllInOne Demo Package is scheduled to be updated once a month. Please download the standard deployment package updated beyond the schedule.

# Project Description

uavstack is the All in One source repository of UAVStack.

## Parent POM
* com.creditease.uav.superpom: parent POM that support dependency management and build plugins

## UAV.Monitor
* com.creditease.uav.agent: core of MonitorAgent
* com.creditease.uav.healthmanager: core features of HealthManager, including Monitoring Data Service，Application/Service Profiling，Real-time Alarm Streaming and AppHub Support Service
* com.creditease.uav.notifycenter: Notification Center Service
* com.creditease.uav.monitorframework: core of JVM MonitorFramework for Tomcat,Jetty,SpringBoot,MSCP, etc...
* com.creditease.uav.monitorframework.agent: JVM MonitorFramework javaagent support
* com.creditease.uav.monitorframework.dproxy: JVM MonitorFramework javassit support
* com.creditease.uav.hook.* : JVM MonitorFramework Hooks for application framework, including Dubbo, HttpClients, JAXWS, JDBC, MongoClients, MQ, Redis, etc. 
* com.creditease.uav.loghook: JVM MonitorFramework Hook for log4j and logback
* com.creditease.uav.tomcat.plus.core: JVM MonitorFramework extension for Tomcat/SpringBoot
* com.creditease.uav.jetty.plus.core: JVM MonitorFramework extension for Jetty
* com.creditease.uav.mock.sl4j: mock sl4j
* com.creditease.uav.ttl: com.alibaba.ttl source code introduction (Alibaba TTL)
* com.creditease.uav.agent.buildComponent: build POM for MonitorAgent
* com.creditease.uav.healthmanager.buildComponent: build POM for HealthManager
* com.creditease.uav.monitorframework.buildComponent: build POM for JVM MonitorFramework

## UAV.APM
* com.creditease.uav.collect: core of APM Data Collection client and server
* com.creditease.uav.invokechain: Invocation Chain and Log Collection 2.0
* com.creditease.uav.monitorframework.apm: JVM MonitorFramework extension for APM
* com.creditease.uav.threadanalysis: one-click thread analysis client and server

## UAV.MSCP
* com.creditease.uav.base: core of MSCP
* com.creditease.uav.agent.heartbeat: basic features of MSCP, including heartbeat client & server，node remote control，process auto scanning and process keep-alive support
* com.creditease.uav.annoscan: Fastclasspathscanner source code introduction
* com.creditease.uav.cache.redis: Redis-based cache framework
* com.creditease.uav.dbaccess: data store framework for MySQL，OpenTSDB，HBase and MongoDB
* com.creditease.uav.elasticsearch.client: ElasticSearch-shaded-client to avoid conflicts
* com.creditease.uav.fastjson: Fastjson source code introduction
* com.creditease.uav.helper: Util support library
* com.creditease.uav.httpasync: Http asynchronous communication client based on Apache Http AsyncClient)
* com.creditease.uav.logging: MSCP logging support
* com.creditease.uav.messaging: MSCP messaging support that relies on com.creditease.uav.mq and requires no underlying messaging service
* com.creditease.uav.mq: message queue support based on Rocket MQ    
* com.creditease.uav.notifymanager: component-level MSCP notification support
* com.creditease.uav.upgrade: remote upgrading support for MSCP client and server

## UAV.AppHub
* com.creditease.uav.console: GUI console of UAVStack with front-end interactive features such as Monitor and APM
* com.creditease.uav.console.buildComponent: build POM for AppHub

## UAV.ServiceGovern
* Service automatic registration is included in the codes of MOF.
* Codes for service discovery is included in profile data query of HealthManager.
* Service downgrading protection (To be updated).
* Service authorization (To be update).

# How to Build
Download the [reference Maven dependency repository](https://pan.baidu.com/s/1gfeY8pH)for Maven Build Failure resulted from the lack of jar packages or the lack of dependent jar packages, and use the downloaded Maven repository instead or copy it into the Maven repository that you are using.

1. build com.creditease.uav.superpom
```
cd com.creditease.uav.superpom
mvn clean install 
```
2 MonitorFramework
```
cd com.creditease.uav.monitorframework.buildComponent
mvn clean install 
```

build result  
>target   
> -build   
> -uavmof_1.0_XXXXX.zip   
> -uavmof_1.0_XXXXX.zip.MD5   

3. MonitorAgent
```
cd com.creditease.uav.agent.buildComponent
mvn clean install 
```

build result   
>target   
> -build   
> -uavagent_1.0_XXXXX.zip   
> -uavagent_1.0_XXXXX.zip.MD5   

4. HealthManager
```
cd com.creditease.uav.healthmanager.buildComponent
mvn clean install 
```

build result   
>target   
> -build   
> -uavhm_1.0_XXXXX.zip   
> -uavhm_1.0_XXXXX.zip.MD5   

5. AppHub
```
cd com.creditease.uav.console
mvn clean install 
```

build result   
>target   
> -com.creditease.uav.console-1.0.war   

# Documents
## UAV.Monitor+APM
* [User Operation](https://uavorg.github.io/documents/uavdoc_useroperation_EN/index.html)
* [Deployment](https://uavorg.github.io/documents/uavdoc_deploydocs_EN/index.html)
* [Architecture](https://uavorg.github.io/documents/uavdoc_architecture_EN/index.html) 

# Downloads
## UAV.Monitor+APM
* [Monitor Framework](https://pan.baidu.com/s/1cg4J0q)
* [Monitor Agent](https://pan.baidu.com/s/1ge5MJ9h)
* [Health Manager](https://pan.baidu.com/s/1i4HnV85)
* [AppHub](https://pan.baidu.com/s/1dFxtDZV)

## Thirdparty Downloads
* [rocket.war](https://pan.baidu.com/s/1dF6NeHN)

## AllInOne Installation Downloads
The Health Manager (HM) in the AllInOne Installation Package is applicable to development environments, demos and small-scale testing environments.
[A distributed deployment](https://uavorg.github.io/documents/uavdoc_deploydocs/healmanagerInstall/healmanagerInstall/microservice.html) is recommended for the production environment.

* [Windows64](https://pan.baidu.com/s/1jIF0wNs)
* [Mac](https://pan.baidu.com/s/1mhCykp6)
* [Linux(CentOS)](https://pan.baidu.com/s/1nvj6jW1)
