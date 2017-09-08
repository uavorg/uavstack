:: The first param is profile name, default value is 'agent'
set profile=agent
if not [%1] == [] set profile=%1

cd ..
setlocal enabledelayedexpansion
set CLASSPATH=
for %%I in (dir lib/*.jar) do set CLASSPATH=lib/%%I;!CLASSPATH!
java -classpath "%CLASSPATH%" com.creditease.agent.feature.nodeopagent.NodeOperCtrlClient %profile% shutdown