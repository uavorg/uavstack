#!/bin/sh 
chmod +x *
UAVBASE=/app
cd $UAVBASE/uav/uavagent/bin/ 
/bin/sh run4docker.sh ma_test MonitorAgent $1 $2