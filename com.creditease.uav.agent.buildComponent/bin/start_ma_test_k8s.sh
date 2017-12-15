#!/bin/sh 
chmod +x *
UAVBASE=/app
cd $UAVBASE/uav/uavagent/bin/ 
/bin/sh run4k8s.sh ma_test MonitorAgent $1 $2