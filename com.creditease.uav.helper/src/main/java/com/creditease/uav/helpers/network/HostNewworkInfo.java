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

package com.creditease.uav.helpers.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;

public class HostNewworkInfo {

    private static List<InetAddress> ips;

    private static Map<String, List<InetAddress>> nameIPsMap = new HashMap<String, List<InetAddress>>();
    private List<InetAddress> readInfo(String name) {

        List<InetAddress> ipList = new ArrayList<InetAddress>();

        InetAddress ip = null;
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();

            while (netInterfaces.hasMoreElements()) {

                NetworkInterface ni = netInterfaces.nextElement();

                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = ips.nextElement();

                    if (!ni.isVirtual() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {

                        if (name != null) {

                            if (ni.getName().equalsIgnoreCase(name)) {
                                ipList.add(ip);
                            }
                        }
                        else {

                            ipList.add(ip);
                        }

                    }
                }

            }
        }
        catch (SocketException e) {
            // ignore
        }

        return ipList;
    }

    public List<InetAddress> getIPs(String name) {

        if (!nameIPsMap.containsKey(name)) {

            nameIPsMap.put(name, this.readInfo(name));
        }
        return nameIPsMap.get(name);
    }

    public List<InetAddress> getIPs() {

        if (ips == null) {
            ips = this.readInfo(null);
        }
        return ips;
    }

    public String getNetCardName(String ipString) {

        Enumeration<NetworkInterface> netInterfaces;
        try {
            InetAddress ipAddr;
            ipAddr = InetAddress.getByName(ipString);
            netInterfaces = NetworkInterface.getNetworkInterfaces();

            while (netInterfaces.hasMoreElements()) {

                NetworkInterface ni = netInterfaces.nextElement();

                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();

                    if (!ni.isVirtual() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {

                        if (ip.equals(ipAddr)) {

                            return ni.getName();
                        }

                    }
                }

            }
        }
        catch (SocketException e) {
            // ignore
        }
        catch (UnknownHostException e) {
            // ignore
        }

        return null;
    }

    /**
     * @return {wlan0={ips={"192.168.1.109":{"bcast":"192.168.1.255","mask":"255.255.255.0"}}, mac=28:C6:3F:C4:29:23}}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getNetCardInfo() {

        Map netCardInfos = new HashMap<String, Map<String, String>>();
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                // if (ni.isVirtual()) {
                // continue;
                // }

                Map ipInfos = new HashMap<String, Map<String, String>>();
                List<InterfaceAddress> addresses = ni.getInterfaceAddresses();
                for (InterfaceAddress ia : addresses) {
                    if (ia.getAddress().isLoopbackAddress() || ia.getAddress().getHostAddress().indexOf(":") != -1) {
                        continue;
                    }

                    Map ipInfo = new HashMap<String, String>();
                    ipInfo.put("bcast", ia.getBroadcast().getHostAddress());
                    ipInfo.put("mask", NetmaskLengthToNetmask(ia.getNetworkPrefixLength()));

                    ipInfos.put(ia.getAddress().getHostAddress(), ipInfo);
                }

                if (!ipInfos.isEmpty()) {
                    Map netCardInfo = new HashMap<String, Map<String, String>>();
                    netCardInfo.put("ips", JSONHelper.toString(ipInfos));
                    netCardInfo.put("mac", getMacAddressAsString(ni));
                    netCardInfos.put(ni.getName(), netCardInfo);
                }
            }
        }
        catch (SocketException e) {
            // ignore
        }

        return netCardInfos;
    }

    // only support ipv4
    public String NetmaskLengthToNetmask(int length) {

        if (length < 0 || length > 32) {
            return "";
        }

        int netmask = 0xFFFFFFFF << (32 - length);
        try {
            return InetAddress.getByAddress(new byte[] { (byte) (netmask >>> 24), (byte) (netmask >>> 16 & 0xFF),
                    (byte) (netmask >>> 8 & 0xFF), (byte) (netmask & 0xFF) }).getHostAddress();
        }
        catch (UnknownHostException e) {
            return "";
        }
    }

    public String getMacAddressAsString(NetworkInterface ni) {

        byte[] mac = null;
        try {
            mac = ni.getHardwareAddress();
        }
        catch (SocketException e) {
            return null;
        }

        StringBuffer sb = new StringBuffer("");

        for (int i = 0; i < mac.length; i++) {

            if (i != 0) {

                sb.append(":");

            }

            // 字节转换为整数

            int temp = mac[i] & 0xff;

            String str = Integer.toHexString(temp);

            if (str.length() == 1) {

                sb.append("0" + str);

            }
            else {

                sb.append(str);

            }

        }

        return sb.toString().toUpperCase();
    }

}
