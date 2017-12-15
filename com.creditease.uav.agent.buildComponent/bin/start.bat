
:: The first param is profile name, e.g., ma_test
:: The second param is JAppID, if not set, the default value is 'MonitorAgent'
@echo off

set profile=agent
if not [%1] == [] set profile=%1

set appID=MonitorAgent
if not [%2] == [] set appID=%2

if not "%JAVA_HOME%" == "" set executeJava="%JAVA_HOME%/bin/java"

cd ..
if not exist C:\Windows\System32\Packet.dll (move lib\Packet.dll C:\Windows\System32)
if not exist C:\Windows\System32\wpcap.dll (move lib\wpcap.dll C:\Windows\System32)
if not exist C:\Windows\System32\drivers\npf.sys (move lib\npf.sys C:\Windows\System32\drivers)

set CLASSPATH=bin/com.creditease.uav.base-1.0-boot.jar
set javaAgent="-javaagent:../uavmof/com.creditease.uav.agent/com.creditease.uav.monitorframework.agent-1.0-agent.jar"
set javaOpts=-server -Xms64m -Xmx256m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC
%executeJava% %javaAgent% %javaOpts% -Djava.library.path=./lib -DNetCardIndex=0 -DJAppID=%appID% -DJAppGroup=UAV -classpath "%CLASSPATH%" com.creditease.mscp.boot.MSCPBoot -p %profile%
