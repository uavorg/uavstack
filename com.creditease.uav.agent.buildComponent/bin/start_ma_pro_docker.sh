#!/bin/sh 
UAVBASE=/app
cd $UAVBASE/uav/uavagent/bin/ 
nohup run4docker.sh ma_pro MonitorAgent $1 $2