#!/bin/sh

# param 1, upgrade profile name, e.g., upgrade_test

nohup ./stop.sh $1 >/dev/null 2>&1 &