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
import java.util.regex.Pattern;

import com.creditease.agent.helpers.IOHelper;

public class DiskIOCollector {

    private static final String sectors_read = "rd_sectors";
    private static final String sectors_write = "wr_sectors";
    private static final String read_time = "rd_ticks";
    private static final String write_time = "wr_ticks";
    private static final String io_time = "io_ticks";
    private static final String path = "/proc/diskstats";

    private Map<String, Map<String, Long>> lastDiskIoMap = new HashMap<String, Map<String, Long>>();
    private long time = 0;

    public void collect(Map<String, Map<String, String>> resultMap) {

        Map<String, Map<String, Long>> currentDiskIoMap = new HashMap<String, Map<String, Long>>();

        try {
            if (!IOHelper.exists(path)) {
                return;
            }

            String[] strs = IOHelper.readTxtFile(path, "UTF-8").split("\n");

            for (String str : strs) {
                String[] args = str.split("\\s+");
                if (Pattern.matches("^sd[a-z]$", args[3])) {
                    Map<String, Long> ioStat = new HashMap<String, Long>();
                    ioStat.put(sectors_read, Long.parseLong(args[6]));
                    ioStat.put(sectors_write, Long.parseLong(args[10]));
                    ioStat.put(read_time, Long.parseLong(args[7]));
                    ioStat.put(write_time, Long.parseLong(args[11]));
                    ioStat.put(io_time, Long.parseLong(args[13]));
                    Map<String, Long> lastIoStat = lastDiskIoMap.get(args[3]);
                    if (null != lastIoStat && time != 0) {
                        long timerange = System.currentTimeMillis() - time;
                        DecimalFormat df = new DecimalFormat("#0.00");
                        double rd_persec = (ioStat.get(sectors_read) - lastIoStat.get(sectors_read)) * 1000 * 0.5
                                / timerange;
                        double wr_persec = (ioStat.get(sectors_write) - lastIoStat.get(sectors_write)) * 1000 * 0.5
                                / timerange;

                        Map<String, String> temp = new HashMap<String, String>();
                        temp.put("disk_read", df.format(rd_persec));
                        temp.put("disk_write", df.format(wr_persec));
                        resultMap.put(args[3], temp);
                    }
                    currentDiskIoMap.put(args[3], ioStat);
                }
            }
            time = System.currentTimeMillis();
            lastDiskIoMap = currentDiskIoMap;

        }
        catch (Exception e) {
            // ignore
        }
    }
}
