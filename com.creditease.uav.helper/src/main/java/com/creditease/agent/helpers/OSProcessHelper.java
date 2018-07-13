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

package com.creditease.agent.helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.creditease.agent.helpers.osproc.DiskIOCollector;
import com.creditease.agent.helpers.osproc.ProcDiskIOCollector;
import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class OSProcessHelper {

    // args constant
    public static final String CPU = "cpu";
    public static final String MEM = "mem";
    public static final String MEMRATE = "memRate";
    public static final String CONN = "conn";
    public static final String PROCDIR = "/proc";
    private static double CLK_TCK = 100.0;

    static {
        if (JVMToolHelper.isLinux()) {
            try {
                String ret = RuntimeHelper.exec("getconf CLK_TCK");
                if (!StringHelper.isEmpty(ret)) {

                    CLK_TCK = Double.parseDouble(ret);
                }
            }
            catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * @return the cLK_CLK
     */
    public static double getCLK_TCK() {

        return CLK_TCK;
    }

    public static Map<String, Map<String, String>> getProcessInfo(String shellPath, Set<String> pids) throws Exception {

        ArrayList<String> commands = generateCommands(pids);
        if (commands == null) {
            return null;
        }

        String connResult = RuntimeHelper.exeShell(commands.get(commands.size() - 1), shellPath);
        Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
        for (String pid : pids) {
            Map<String, String> statusMap = new HashMap<String, String>();
            statusMap.put(CONN, "0");
            resultMap.put(pid, statusMap);
        }
        analyseConn(connResult, resultMap);
        for (int i = 0; i < commands.size() - 1; i++) {
            String cpumemResult = RuntimeHelper.exeShell(commands.get(i), shellPath);
            analyseCpuMem(cpumemResult, resultMap);
        }

        return resultMap;
    }

    private static ArrayList<String> generateCommands(Set<String> pids) {

        if (null == pids || pids.size() == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        StringBuffer pidString = new StringBuffer();
        ArrayList<String> commands = new ArrayList<String>();
        int count = 0;
        for (String pid : pids) {
            if (null == pid) {
                continue;
            }
            try {
                Integer.valueOf(pid);
            }
            catch (Exception e) {
                continue;
            }
            pidString.append(pid).append(",");
            sb.append(pid).append("/|");
            count++;
            if (count == 20) {
                commands.add(String.format("top -p %s -bn 2", pidString.substring(0, pidString.length() - 1)));
                count = 0;
                pidString = new StringBuffer();
            }
        }
        if (count != 0) {
            commands.add(String.format("top -p %s -bn 2", pidString.substring(0, pidString.length() - 1)));
        }
        // conn obtain command
        String connCommand = String.format("netstat -pnat |grep -E 'LISTEN +(%s)' | awk '{print $4,$7}'",
                sb.substring(0, sb.length() - 2));
        connCommand += "; netstat -pnat |grep -E  'ESTABLISHED' | awk '{a[$4]++}END{for(i in a){print i,a[i]}}' ";
        commands.add(connCommand);
        return commands;
    }

    private static void analyseCpuMem(String cpumemResult, Map<String, Map<String, String>> resultMap) {

        String[] lines = cpumemResult.split("\n");
        int startLine = 0;
        int count = 0;
        boolean tag = false;
        for (int i = 0; i < lines.length; i++) {

            if (lines[i].length() != 0) {
                tag = true;
                continue;
            }
            if (tag) {
                count++;
            }
            if (count == 3) {
                startLine = i + 2;
                break;
            }
            tag = false;
        }
        // OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // int cpuNum = osmb.getAvailableProcessors();
        // output schema: PID USER PR NI VIRT RES SHR S %CPU %ME TIME+ COMMAND
        for (int i = startLine; i < lines.length; i++) {
            if (lines[i].length() == 0) {
                break;
            }
            String[] args = lines[i].split("\\s+");
            int index = 0;
            if (args[0].length() == 0) {
                index = 1;
            }
            if (!resultMap.containsKey(args[index])) {
                continue;
            }
            Map<String, String> temp = resultMap.get(args[index]);
            float cpuValue = Float.parseFloat(args[8 + index]);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String cpuRate = decimalFormat.format(cpuValue);
            temp.put(CPU, cpuRate);
            String mem = args[5 + index];
            if (mem.endsWith("m") || mem.endsWith("M")) {
                mem = mem.substring(0, mem.length() - 1);
                double memValue = Double.parseDouble(mem) * 1024;
                mem = decimalFormat.format(memValue);
            }
            else if (mem.endsWith("g") || mem.endsWith("G")) {
                mem = mem.substring(0, mem.length() - 1);
                double memValue = Double.parseDouble(mem) * 1024 * 1024;
                mem = decimalFormat.format(memValue);
            }
            temp.put(MEM, mem);
            temp.put(MEMRATE, args[9 + index]);
        }

    }

    private static void analyseConn(String connResult, Map<String, Map<String, String>> resultMap) {

        HashMap<String, String> temp;
        String[] lines = connResult.split("\n");
        int connResultLine = 0;
        // reflect listen port to pid
        Map<String, String> portPid = new HashMap<String, String>();
        String port;
        String pid;
        for (int i = connResultLine; i < lines.length; i++) {
            String[] listenArgs = lines[i].split(" ");
            if (listenArgs.length != 2) {
                continue;
            }
            if (!listenArgs[1].contains("/") && !listenArgs[1].contains("-")) {
                break;
            }
            String[] strs = listenArgs[0].split(":");
            port = strs[strs.length - 1];
            pid = listenArgs[1].split("/")[0];
            temp = (HashMap<String, String>) resultMap.get(pid);
            if (null != temp) {
                temp.put("conn_" + port, "0");
            }
            portPid.put(port, pid);
            connResultLine = i + 1;
        }
        // get connections num of each port
        for (int i = connResultLine; i < lines.length; i++) {
            String[] connArgs = lines[i].split(" ");
            if (connArgs.length != 2) {
                continue;
            }
            String[] strs = connArgs[0].split(":");
            port = strs[strs.length - 1];
            pid = portPid.get(port);
            if (null == pid) {
                continue;
            }
            temp = (HashMap<String, String>) resultMap.get(pid);
            if (null == temp) {
                continue;
            }
            if ("0".equals(temp.get("conn_" + port))) {
                temp.put("conn_" + port, connArgs[1]);
            }
            else {
                int portconn = Integer.parseInt(temp.get("conn_" + port));
                portconn += Integer.parseInt(connArgs[1]);
                temp.put("conn_" + port, String.valueOf(portconn));
            }
            int curconn = Integer.parseInt(temp.get(CONN));
            curconn += Integer.parseInt(connArgs[1]);
            temp.put(CONN, String.valueOf(curconn));
        }
    }

    public static Map<String, Map<String, String>> getWinProcessInfo(String shellPath, Set<String> pids)
            throws Exception {

        Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
        for (String pid : pids) {
            Map<String, String> statusMap = new HashMap<String, String>();
            statusMap.put(CONN, "0");
            resultMap.put(pid, statusMap);
        }
        String command = generateCommandWmic(pids);
        if (command != null) {
            String resultString = RuntimeHelper.exeShell(command, shellPath);
            Map<String, List<Long>> timeRecord = new HashMap<String, List<Long>>();
            analyseWinMem(resultString, resultMap, timeRecord);
            Thread.sleep(500);
            resultString = RuntimeHelper.exeShell(command, shellPath);
            analyseWinCpuRate(resultString, resultMap, timeRecord);
        }
        command = "netstat -aon -p TCP";
        String resultString = RuntimeHelper.exeShell(command, shellPath);
        analyseWinConn(resultString, resultMap);
        return resultMap;
    }

    private static String generateCommandWmic(Set<String> pids) {

        if (null == pids || pids.size() == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (String pid : pids) {
            if (null == pid) {
                continue;
            }
            try {
                Integer.valueOf(pid);
            }
            catch (Exception e) {
                continue;
            }
            sb.append(" processid=").append(pid).append(" or");
        }
        String pidString = sb.substring(0, sb.length() - 2);
        String command = String
                .format("wmic process where '%s' get kernelmodetime,processid,usermodetime,workingsetsize", pidString);
        return command;
    }

    private static void analyseWinMem(String resultString, Map<String, Map<String, String>> resultMap,
            Map<String, List<Long>> timeRecord) {

        long sysTime = System.currentTimeMillis() * 10000;
        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMem = osmb.getTotalPhysicalMemorySize();
        String[] lines = resultString.split("\n");
        int beginline = 3; // data begin at line 3
        for (int i = beginline; i < lines.length; i++) {
            if (lines[i].length() == 0) {
                continue;
            }
            String statusArgs[] = lines[i].split("\\s+");
            String pid = statusArgs[1];
            Map<String, String> statusMap = resultMap.get(pid);
            long mem = Long.parseLong(statusArgs[3]);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            float memrate = (float) mem / (float) totalMem;
            String memRate = decimalFormat.format(memrate * 100);
            statusMap.put(MEM, String.valueOf(mem / 1024));
            statusMap.put(MEMRATE, memRate);
            String kernelmodetime = statusArgs[0];
            String usermodetime = statusArgs[2];
            long nt = Long.parseLong(kernelmodetime) + Long.parseLong(usermodetime);
            List<Long> otList;
            otList = new ArrayList<Long>(2);
            otList.add(nt);
            otList.add(sysTime);
            timeRecord.put(pid, otList);
        }
    }

    private static void analyseWinCpuRate(String resultString, Map<String, Map<String, String>> resultMap,
            Map<String, List<Long>> timeRecord) {

        long sysTime = System.currentTimeMillis() * 10000;
        // OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // int cpuNum = osmb.getAvailableProcessors();
        String[] lines = resultString.split("\n");
        int beginline = 3;
        for (int i = beginline; i < lines.length; i++) {
            if (lines[i].length() == 0) {
                continue;
            }
            String statusArgs[] = lines[i].split("\\s+");
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String kernelmodetime = statusArgs[0];
            String usermodetime = statusArgs[2];
            long nt = Long.parseLong(kernelmodetime) + Long.parseLong(usermodetime);
            Map<String, String> statusMap = resultMap.get(statusArgs[1]);
            List<Long> otList = timeRecord.get(statusArgs[1]);
            String cpuRate = decimalFormat.format((nt - otList.get(0)) * 100 / (float) (sysTime - otList.get(1)));
            statusMap.put(CPU, cpuRate);
            resultMap.put(statusArgs[1], statusMap);
        }
    }

    private static void analyseWinConn(String resultString, Map<String, Map<String, String>> resultMap) {

        Map<String, String> portPid = new HashMap<String, String>();
        String lines[] = resultString.split("\n");
        int beginline = 7; // data begin at line 7
        for (int i = beginline; i < lines.length; i++) {
            String statusArgs[] = lines[i].split("\\s+");
            int index = 0;
            if (statusArgs[0].length() == 0) {
                index = 1;
            }
            String connType = statusArgs[3 + index];
            if (connType.equals("LISTENING")) {
                String pid = statusArgs[4 + index];
                Map<String, String> statusMap = resultMap.get(pid);
                if (null == statusMap) {
                    continue;
                }
                String addr = statusArgs[1 + index];
                String[] strs = addr.split(":");
                String port = strs[strs.length - 1];
                portPid.put(port, pid);
                statusMap.put("conn_" + port, "0");
            }
        }
        for (int i = beginline; i < lines.length; i++) {
            String statusArgs[] = lines[i].split("\\s+");
            int index = 0;
            if (statusArgs[0].length() == 0) {
                index = 1;
            }
            String connType = statusArgs[3 + index];
            if (connType.equals("ESTABLISHED")) {
                String addr = statusArgs[1 + index];
                String[] strs = addr.split(":");
                String port = strs[strs.length - 1];
                String pid = portPid.get(port);
                if (null == pid) {
                    continue;
                }
                Map<String, String> statusMap = resultMap.get(pid);
                String portconnStr = statusMap.get("conn_" + port);
                if (null == portconnStr) {
                    continue;
                }
                int portcount = Integer.valueOf(portconnStr);
                statusMap.put("conn_" + port, String.valueOf(++portcount));
                int curconn = Integer.parseInt(statusMap.get(CONN));
                statusMap.put(CONN, String.valueOf(++curconn));
            }
        }
    }

    // ps h -o pid,ppid,user,cmd -p 5347
    public static Map<String, String> getProcessInfo(String pid, String... args) throws Exception {

        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("args cannot be empty.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ps h -o ");
        for (String arg : args) {
            sb.append(arg).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(" -p ").append(pid);

        String rs = RuntimeHelper.exec(sb.toString());
        if (rs == null || rs.length() == 0) {
            return null;
        }

        Pattern p = Pattern.compile("\\s+");
        String[] ss = p.split(rs.trim(), args.length);
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < ss.length; i++) {
            map.put(args[i], ss[i]);
        }

        return map;
    }

    /**
     * newDiskIOCollector
     * 
     * @return
     */
    public static DiskIOCollector newDiskIOCollector() {

        return new DiskIOCollector();
    }

    /**
     * newProcDiskIOCollector
     * 
     * @return
     */
    public static ProcDiskIOCollector newProcDiskIOCollector() {

        return new ProcDiskIOCollector();
    }

    /**
     * kill process
     * 
     * @param pid
     */
    public static void killProcess(String pid) {

        if (JVMToolHelper.isWindows()) {
            try {
                RuntimeHelper.exec(2000, "taskkill /f /pid " + pid);
            }
            catch (Exception e) {
                // ignore
            }
        }
        else {
            try {
                RuntimeHelper.exec(2000, "kill -9 " + pid);
            }
            catch (Exception e) {
                // ignore
            }
        }
    }
    
    /**
     * get Process Starttime Jiffies by pid for Linux 获取Linux的进程启动时间参数Jiffies
     * 
     * @param pid
     * @return
     */
    private static long getProcStartTimeJiffies(String pid) {

        BufferedReader bufReader = null;
        long procStartTimeJiffi = 0;
        try {
            bufReader = new BufferedReader(new FileReader(PROCDIR + "/" + pid + "/stat"));
            String line = null;
            if ((line = bufReader.readLine()) != null) {
                line = line.trim();
                String[] info = line.split("\\s+");
                procStartTimeJiffi = Long.parseLong(info[21]);
            }
        }
        catch (Exception e) {
            // ignore
        }
        finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
        return procStartTimeJiffi;
    }

    /**
     * get System Runnningtime for Linux 获取Linux系统已经运行的时间
     * 
     * @param pid
     * @return
     */
    private static long getLinuxSystemRunnningTime(String pid) {

        BufferedReader bufReader = null;
        long linuxSystemRunnningTime = 0;
        try {
            bufReader = new BufferedReader(new FileReader(PROCDIR + "/uptime"));
            String line = null;
            if ((line = bufReader.readLine()) != null) {
                line = line.trim();
                String[] info = line.split("\\s+");
                linuxSystemRunnningTime = (long) Double.parseDouble(info[0]);
            }
        }
        catch (Exception e) {
            // ignore
        }
        finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
        return linuxSystemRunnningTime;
    }

    /**
     * get Process Start time by pid for Linux and Windows 获取进程的启动时间,Linux/Windows
     * 
     * @param pid
     * @return
     */
    public static long getProcStartTime(String pid, String shellPath) {

        long procStartTime = 0;
        if (JVMToolHelper.isWindows()) {// 获取Windows系统的进程启动时间
            String resultString;
            try {
                String command = "wmic process where processid=" + pid + " get CreationDate";
                resultString = RuntimeHelper.exeShell(command, shellPath);
                String[] lines = resultString.split("\n");
                int beginline = 3; // data begin at line 3
                for (int i = beginline; i < lines.length; i++) {
                    if (lines[i].length() == 0) {
                        continue;
                    }
                    String ProcStartTimeStr = lines[i].split("\\.")[0];
                    procStartTime = DateTimeHelper.dateFormat(ProcStartTimeStr, "yyyyMMddHHmmss").getTime();
                }
            }
            catch (Exception e) {
                // ignore
            }
        }
        else {
            // 获取Linux系统的进程启动时间
            long procRunningTime = (long) (getLinuxSystemRunnningTime(pid) - getProcStartTimeJiffies(pid) / CLK_TCK);
            procStartTime = System.currentTimeMillis() - procRunningTime * 1000;
        }
        return procStartTime;
    }
}
