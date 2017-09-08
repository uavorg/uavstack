import socket
import struct
import thread
import threading
import time
import os
import sys
import json
netcard=sys.argv[1]
host_ip=sys.argv[2]
collectTime=float(sys.argv[3])
net_data = {}
d_net_info = {}
cancel=True
def net_data_normalize():
    for key in net_data:
        net_data[key]="%s"%round(net_data[key]/collectTime,2)

def init_net_data():
    for port in sys.argv[4:]:
        net_data["in_"+port]=0
        net_data["out_"+port]=0
		
def get_packet():
    s = socket.socket(socket.PF_PACKET, socket.SOCK_DGRAM,socket.htons(0x0003))
    s.bind((netcard,0));
    while cancel:
        buf = s.recvfrom(40)
        iphead_version=(struct.unpack("B",buf[0][0:1])[0]&0xf0)/16
        if iphead_version != 4 and iphead_version != 6 :
            continue 
        iphead_len =(struct.unpack("B",buf[0][0:1])[0]&0x0f)*4      
        src_ip = "%d.%d.%d.%d"%struct.unpack('BBBB', buf[0][12:16])
        dest_ip ="%d.%d.%d.%d"%struct.unpack('BBBB', buf[0][16:20])
        port = struct.unpack('HH', buf[0][iphead_len:4+iphead_len])
        if src_ip == host_ip:
            src_port = socket.htons(port[0])
            key = "out_%d"%src_port
            if key in net_data:
                data_len =socket.htons(struct.unpack("H",buf[0][2:4])[0])
                net_data[key]=net_data[key]+data_len
        elif dest_ip == host_ip:
            dest_port = socket.htons(port[1])
            key = "in_%d"%dest_port
            if key in net_data:
                data_len =socket.htons(struct.unpack("H",buf[0][2:4])[0])
                net_data[key]=net_data[key]+data_len

init_net_data()
thread.start_new_thread(get_packet,())
time.sleep(collectTime/1000)
cancel=False
time.sleep(0.5)
net_data_normalize()
result=json.dumps(net_data)
print result
