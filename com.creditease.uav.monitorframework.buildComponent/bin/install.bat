cd ..
for %%I in (dir com.creditease.uav/*.jar) do call "bin/makeclasspath.bat" com.creditease.uav/%%I
java -classpath "%CLASSPATH%" com.creditease.monitor.UAVServerInstaller