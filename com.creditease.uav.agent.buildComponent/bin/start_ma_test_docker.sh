#!/bin/sh 
UAVPATH=/opt
JAVA_HOME=/usr/java/jdk1.7.0_71
cd $UAVPATH/uav/uavagent/bin/ 
nohup  /bin/sh $UAVPATH/uav/uavagent/bin/run4docker.sh ma_test MonitorAgent 0 $JAVA_HOME