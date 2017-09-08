set ws = createobject("wscript.shell")
set args = WScript.Arguments
dim cmd 
cmd = "start.bat upgrade_server UpgradeServer"
ws.run cmd, vbhide