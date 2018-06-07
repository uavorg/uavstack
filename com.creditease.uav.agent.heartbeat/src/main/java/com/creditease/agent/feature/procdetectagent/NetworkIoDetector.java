/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.agent.feature.procdetectagent;

import java.text.DecimalFormat;
import java.util.HashMap;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.AgentFeatureComponent;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

public class NetworkIoDetector extends AbstractTimerWork {

    private String portList;

    public NetworkIoDetector(String cName, String feature) {
        super(cName, feature);
    }

    private int countDown = 0;

    @Override
    public void run() {

        if (portList == null) {
            return;
        }

        String result = null;
        String networkdetectTime = this.getConfigManager().getFeatureConfiguration(this.feature,
                "networkDetect.collectTime");
        String ip = NetworkHelper.getLocalIP();

        // windows
        if (JVMToolHelper.isWindows()) {
            
            JpcapCaptor jpcap =null;
            try {
                String Local_ip = "/" + ip;
                // 存端口流量
                HashMap<String, Integer> counter = new HashMap<String, Integer>();
                String[] split_ProtList = portList.split(" ");
                for (String str : split_ProtList) {
                    counter.put("in_" + str, 0);
                    counter.put("out_" + str, 0);
                }
                // 获取网卡设备列表
                NetworkInterface[] devices = JpcapCaptor.getDeviceList();
                // 确定网卡设备接口
                boolean true_devices = false;
                int i = 0;
                for (; i < devices.length; i++) {
                    for (NetworkInterfaceAddress nia : devices[i].addresses) {
                        if (Local_ip.equals(nia.address.toString())) {
                            true_devices = true;
                            break;
                        }

                    }
                    if (true_devices) {
                        break;
                    }
                }
                NetworkInterface nc = devices[i];
                // 打开网卡设备 ，创建某个卡口上的抓取对象,最大为65535个
                jpcap = JpcapCaptor.openDevice(nc, 65535, false, 20);
                jpcap.setFilter("tcp", true);// 设置过滤器

                // 抓包 统计流量
                result = portFlux(jpcap, counter, nc, networkdetectTime, Local_ip);

            }
            catch (Exception e) {
                log.err(this, "NetworkIo Monitor runs FAIL.", e);
            }
            finally {
                if(jpcap!=null) {
                    // 关闭
                    jpcap.close();
                }
             }

        }

        // linux
        else {

            if (countDown != 0) {
                countDown--;
                return;
            }

            try {

                String netcardName = NetworkHelper.getNetCardName(ip);
                String command = "cd bin; sh networkIoDetect.sh " + netcardName + " " + ip + " " + networkdetectTime
                        + " " + portList;

                result = RuntimeHelper.exec(10000, "/bin/sh", "-c", command);
                if (!result.contains("in_") || result.toLowerCase().contains("error")
                        || result.toLowerCase().contains("traceback")) {
                    log.err(this, "NetworkIo Monitor runs FAIL with TechError: error=" + result);
                    countDown = 100;
                    return;
                }
            }
            catch (Exception e) {
                log.err(this, "NetworkIo Monitor runs FAIL.", e);
                countDown = 100;
            }

        }

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent(this.feature,
                "ProcDetectAgent");
        if (null != afc) {
            afc.exchange("procscan.nodeinfo.portFlux", result, System.currentTimeMillis());
        }

        if (log.isDebugEnable()) {
            log.debug(this, "NetworkIo Monitor Result: " + result);
        }

    }

    public void setPortList(String portList) {

        this.portList = portList;
    }

    public String portFlux(JpcapCaptor jpcap, HashMap<String, Integer> counter, NetworkInterface nc,
            String networkdetectTime, String Local_ip) {

        Packet packet;

        int int_networkdetectTime = 1024 * Integer.parseInt(networkdetectTime) / 1000;
        long time = Long.parseLong(networkdetectTime);
        long startTime = System.currentTimeMillis();
        while (startTime + time >= System.currentTimeMillis()) {
            // 抓包
            packet = jpcap.getPacket();
            if (null == packet) {
                continue;
            }
            TCPPacket p = (TCPPacket) packet;
            // 端口不在protList 忽略
            String dst_ip = p.dst_ip.toString();
            String src_ip = p.src_ip.toString();
            // 统计入口流量
            if (dst_ip.equals(Local_ip)) {
                String in_port = "in_" + p.dst_port;
                if (counter.containsKey(in_port)) {
                    int in_value = counter.get(in_port) + p.len;
                    counter.put(in_port, in_value);
                }
            }
            // 统计出口流量
            if (src_ip.equals(Local_ip)) {
                String out_port = "out_" + p.src_port;
                if (counter.containsKey(out_port)) {
                    int out_value = counter.get(out_port) + p.len;
                    counter.put(out_port, out_value);
                }
            }
        }

        HashMap<String, String> counterValueIntToString = new HashMap<String, String>();
        // 字节转kb/s
        for (String key : counter.keySet()) {
            DecimalFormat df = new DecimalFormat("#0.00");
            Double value = Double.valueOf(df.format(counter.get(key) / int_networkdetectTime * 1.0));
            counterValueIntToString.put(key, value.toString());
        }
        // 将hashmap 转为json 返回
        return JSONHelper.toString(counterValueIntToString);
    }

}
