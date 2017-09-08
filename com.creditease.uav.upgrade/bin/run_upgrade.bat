:: The first param is profile name, e.g., upgrade_test
:: The second param is network card number, e.g., 0
:: The third param is upgrade target package, e.g., uavagent_2.0.0.zip
@echo off

set profile=%1

cd ..

if exist upgrade (
	rmdir /S /Q upgrade
)

md upgrade
setlocal enabledelayedexpansion
set CLASSPATH=bin/com.creditease.uav.base-1.0-boot.jar
for %%I in (dir lib/*.jar) do (
	copy lib\%%I upgrade
)

for %%I in (dir upgrade/*.jar) do set CLASSPATH=upgrade/%%I;!CLASSPATH!

echo %CLASSPATH%

set javaOpts=-server -Xms64m -Xmx1024m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC
java %javaOpts% -DNetCardIndex=%2 -DJAppID=%appID% -DJAppGroup=UAV -classpath "%CLASSPATH%" -DJAppUpgradeInfo=%3 com.creditease.mscp.boot.MSCPBoot -p %profile%