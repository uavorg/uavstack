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

package com.creditease.agent.helpers.osproc;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.StringHelper;

public class ProcDiskIOCollector {

    private static final String read_bytes = "read_bytes";
    private static final String write_bytes = "write_bytes";

    private long time = 0;
    private Map<String, Map<String, Long>> lastPidsDiskIoMap = new HashMap<String, Map<String, Long>>();

    public void collect(Map<String, OSProcess> procs) {

        Map<String, Map<String, Long>> currentPidsDiskIoMap = new HashMap<String, Map<String, Long>>();

        if (JVMToolHelper.isWindows()) {
            // for Windows
            try {
                String output = RuntimeHelper.exec("wmic process list IO");
                if (StringHelper.isEmpty(output)) {
                    return;
                }

                String[] strs = output.split("\n");
                for (String str : strs) {
                    str = str.replaceAll("\\s{2,}", " ");
                    String[] args = str.split(" ");
                    if (1 == args.length) {
                        continue;
                    }

                    if (procs.containsKey(args[1])) {
                        Map<String, Long> lastIoStat = lastPidsDiskIoMap.get(args[1]);
                        Map<String, Long> ioStat = new HashMap<String, Long>();

                        ioStat.put(read_bytes, Long.parseLong(args[3]));
                        ioStat.put(write_bytes, Long.parseLong(args[5]));

                        if (null != lastIoStat && time != 0) {
                            long timerange = System.currentTimeMillis() - time;
                            DecimalFormat df = new DecimalFormat("#0.00");

                            double rd_persec = (ioStat.get(read_bytes) - lastIoStat.get(read_bytes)) * 1.0 / timerange;
                            double wr_persec = (ioStat.get(write_bytes) - lastIoStat.get(write_bytes)) * 1.0
                                    / timerange;
                            procs.get(args[1]).addTag("disk_read", df.format(rd_persec));
                            procs.get(args[1]).addTag("disk_write", df.format(wr_persec));
                        }
                        currentPidsDiskIoMap.put(args[1], ioStat);
                    }
                }
            }
            catch (Exception e) {
                // ignore
            }
        }
        else {
            // for linux
            for (String pid : procs.keySet()) {
                try {
                    Map<String, Long> lastIoStat = lastPidsDiskIoMap.get(pid);
                    Map<String, Long> ioStat = new HashMap<String, Long>();
                    String procpath = "/proc/" + pid + "/io";
                    if (!IOHelper.exists(procpath)) {
                        continue;
                    }

                    String[] strs = IOHelper.readTxtFile(procpath, "UTF-8").split("\n");
                    for (String str : strs) {

                        if (str.startsWith("read_bytes")) {
                            ioStat.put("read_bytes", Long.parseLong(str.split(":")[1].replace(" ", "")));
                        }
                        if (str.startsWith("write_bytes")) {
                            ioStat.put("write_bytes", Long.parseLong(str.split(":")[1].replace(" ", "")));
                        }

                    }
                    if (null != lastIoStat && time != 0) {
                        long timerange = System.currentTimeMillis() - time;
                        DecimalFormat df = new DecimalFormat("#0.00");
                        double rd_persec = (ioStat.get(read_bytes) - lastIoStat.get(read_bytes)) * 1.0 / timerange;
                        double wr_persec = (ioStat.get(write_bytes) - lastIoStat.get(write_bytes)) * 1.0 / timerange;
                        procs.get(pid).addTag("disk_read", df.format(rd_persec));
                        procs.get(pid).addTag("disk_write", df.format(wr_persec));
                    }
                    currentPidsDiskIoMap.put(pid, ioStat);
                }
                catch (Exception e) {
                    // ignore
                }

            }
        }
        lastPidsDiskIoMap = currentPidsDiskIoMap;
        time = System.currentTimeMillis();
    }
}
