#!/bin/sh
nohup ./run.sh agent MonitorAgent $1 $2  >/dev/null 2>&1 &
