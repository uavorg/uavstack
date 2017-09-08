#!/bin/sh

javaHomeArray=(/opt/jdk1.7.0_45 /opt/jdk1.7.0_65 /opt/jdk1.7.0_71 /opt/jdk1.7.0_79 /opt/jdk1.7.0_80 /app/jdk1.7.0_79 /app/jdk1.7.0_80 /opt/jdk1.8.0_77 /opt/jdk1.8.0_121 /opt/jdk1.8.0_131)
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

netcardIndex=0
if [ $# -gt 1 ]; then
    netcardIndex=$2
fi
echo $netcardIndex
cd .. 

if [ -d upgrade ]; then
   rm -rf upgrade 
fi

mkdir upgrade

export CLASSPATH=bin/com.creditease.uav.base-1.0-boot.jar
for i in lib/*; do 
    cp $i upgrade
done

for i in upgrade/*; do
    CLASSPATH=$i:"$CLASSPATH"
done

export CLASSPATH=.:$CLASSPATH
echo $CLASSPATH


javaOpts="-server -Xms64m -Xmx1024m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
$executeJava $javaOpts -XX:OnOutOfMemoryError='kill -9 %p' -DNetCardIndex=$netcardIndex -DJAppID=$1 -DJAppGroup=UAV -DJAppUpgradeInfo=$3 -DStartByCronTask=$4  -classpath $CLASSPATH com.creditease.mscp.boot.MSCPBoot -p $1 &


# add crontab process watcher
if [ "$proc_watcher" == "yes" ]; then
    sleep 1
    # add crontab
    cronfile=/tmp/$1".tmp"
    crontab -l | grep -v "$1" 1>$cronfile 2>/dev/null
	echo "*/1 * * * * sh $working_directory/uav_proc_watcher.sh \"$1\" \"$working_directory\" \"./run_upgrade.sh --no-watch $1 $2 $3 1\"  >/dev/null 2>&1" >> $cronfile
    crontab $cronfile
    rm -rf $cronfile
    exit 0
fi