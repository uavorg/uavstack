#!/bin/sh
cd ..  
for i in lib/*;
do CLASSPATH=$i:"$CLASSPATH";
done
export CLASSPATH=.:$CLASSPATH
echo $CLASSPATH
java -classpath $CLASSPATH com.creditease.uav.client.HealthManagerClient $1 $2 $3 $4