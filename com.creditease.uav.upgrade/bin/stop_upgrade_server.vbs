set ws = createobject("wscript.shell")
set args = WScript.Arguments
dim cmd 
cmd = "stop.bat upgrade_server"
ws.run cmd, vbhide