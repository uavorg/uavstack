#!/bin/sh

# del crontab
process_flag=$1
count=`crontab -l 2>/dev/null | grep "$process_flag" | wc -l`
if [ $count -ne 0 ]; then
    cronfile=/tmp/$process_flag".tmp"
    crontab -l | grep -v "$process_flag" > $cronfile
    crontab $cronfile
    rm -rf $cronfile
fi

# kill running watchers
runing_watcher=$(ps -ef | grep "uav_proc_watcher.sh" | grep "$process_flag" | awk '{printf "%s ",$2}')
for pid in $runing_watcher; do
    kill -9 "$pid"
done

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

cd ..  
for i in lib/*;
do CLASSPATH=$i:"$CLASSPATH";
done
export CLASSPATH=.:$CLASSPATH
echo $CLASSPATH
$executeJava -classpath $CLASSPATH com.creditease.agent.feature.nodeopagent.NodeOperCtrlClient $1 shutdown