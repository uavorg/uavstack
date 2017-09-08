#!/bin/sh
#NOT READY,PLEASE REFINE THIS
cd ..  
for i in com.creditease.uav/*;
do CLASSPATH=$i:"$CLASSPATH";
done
export CLASSPATH=.:$CLASSPATH
echo $CLASSPATH
java -classpath $CLASSPATH com.creditease.monitor.UAVServerInstaller