#!/bin/sh

# The first param is profile name, e.g., upgrade_test
# The second param is network card number, e.g., 0
# The third param is upgrade info, e.g., {"uav":1,"softwareId":"uavagent","softwarePackage":"uavagent_1.0_20161220140510.zip","targetDir":"/app/uav/uavagent"}

nohup ./run_upgrade.sh $1 $2 $3 >/dev/null 2>&1 &
