#!/bin/sh
#NOT READY,PLEASE REFINE THIS

javaHomeArray=(/app/jdk1.8.0_131 /opt/jdk1.8.0_131 /opt/jdk1.8.0_121 /opt/jdk1.7.0_45 /opt/jdk1.7.0_65 /opt/jdk1.7.0_71 /opt/jdk1.7.0_79 /opt/jdk1.7.0_80 /app/jdk1.7.0_79 /app/jdk1.7.0_80 /opt/jdk1.8.0_77)
executeJava="java"

for jhome in ${javaHomeArray[@]}
do
    tmp="$jhome/bin/java"
    if [ -x "$tmp" ]; then
        executeJava=$tmp        
        break
    fi
done

echo "using $executeJava"

working_directory=$(pwd)
proc_watcher="yes"
if [ "$1" == "--no-watch" ]; then
    proc_watcher="no"
    shift
fi

cd ..  
export CLASSPATH=bin/com.creditease.uav.base-1.0-boot.jar
echo $CLASSPATH
javaAgent="-javaagent:../uavmof/com.creditease.uav.agent/com.creditease.uav.monitorframework.agent-1.0-agent.jar"
javaOpts="-server -Xms256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
$executeJava $javaAgent $3 $javaOpts -XX:OnOutOfMemoryError='kill -9 %p' -DNetCardIndex=0 -DJAppID=$1 -DJAppGroup=UAV -Dsun.net.httpserver.maxReqTime=300 -Dsun.net.httpserver.maxRspTime=300 -classpath $CLASSPATH com.creditease.mscp.boot.MSCPBoot -p $2 &

# add crontab process watcher
if [ "$proc_watcher" == "yes" ]; then
    sleep 1
    # add crontab
    cronfile=/tmp/$2".tmp"
    crontab -l | grep -v "$2" 1>$cronfile 2>/dev/null
    echo "*/1 * * * * sh $working_directory/uav_proc_watcher.sh \"$2\" \"$working_directory\" \"./run.sh --no-watch $1 $2 $3\"  >/dev/null 2>&1" >> $cronfile
    crontab $cronfile
    rm -rf $cronfile
    exit 0
fi