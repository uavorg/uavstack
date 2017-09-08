' The first param is profile name, e.g., upgrade_test
' The second param is network card number, e.g., 0
' The third param is upgrade info, e.g., {"uav":1,"softwareId":"uavagent","softwarePackage":"uavagent_1.0_20161220140510.zip","targetDir":"/app/uav/uavagent"}

set ws = createobject("wscript.shell")
set args = WScript.Arguments
dim cmd 
cmd = "run_upgrade.bat " & args(0) & " " & args(1) & " " & Chr(34) & args(2) & Chr(34)
ws.run cmd, vbhide