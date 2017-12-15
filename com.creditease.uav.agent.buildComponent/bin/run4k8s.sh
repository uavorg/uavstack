#!/bin/bash
#NOT READY,PLEASE REFINE THIS

UAVBASE=/app

working_directory=$(pwd)
proc_watcher="yes"
if [ "$1" == "--no-watch" ]; then
    proc_watcher="no" 
    shift 
fi 

echo $4
executeJava="java"
jhome=""
if [ -d "$4" ]; then
    export JAVA_HOME=$4
    export JRE_HOME=$JAVA_HOME/jre 
    export PATH=$JAVA_HOME/bin:$PATH 
    export CLASSPATH=.:$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
    jhome=$4
fi

if [ -z $javaHome ];then
javaHomeArray=(/opt/jdk1.7.0_45 /opt/jdk1.7.0_65 /opt/jdk1.7.0_71 /opt/jdk1.7.0_79 /opt/jdk1.7.0_80 /opt/jdk1.8.0_77 /opt/jdk1.8.0_121 /opt/jdk1.8.0_131 /app/jdk1.7.0_79 /app/jdk1.7.0_80)
for jhome in ${javaHomeArray[@]}
do
    tmp="$jhome/bin/java"
    if [ -x "$tmp" ]; then
        executeJava=$tmp
        break
    fi
done
fi

netcardIndex=
netcardName=
if [ $# -gt 2 ]; then
    if [[ $3 =~ ^[0-9]$ ]]; then 
        netcardIndex=$3
    else
        netcardName=$3
    fi
fi

cd .. 
export CLASSPATH=$UAVBASE/uav/uavagent/bin/com.creditease.uav.base-1.0-boot.jar 
echo $CLASSPATH
 
javaAgent="-javaagent:$UAVBASE/uav/uavmof/com.creditease.uav.agent/com.creditease.uav.monitorframework.agent-1.0-agent.jar"
javaOpts="-server -Xms64m -Xmx256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=." 
$executeJava $javaAgent $javaOpts -XX:OnOutOfMemoryError='kill -9 %p' -DNetCardIndex=$netcardIndex -DNetCardName=$netcardName -DJAppID=$2 -DDeploymentType=container -classpath $CLASSPATH com.creditease.mscp.boot.MSCPBoot -p $1 