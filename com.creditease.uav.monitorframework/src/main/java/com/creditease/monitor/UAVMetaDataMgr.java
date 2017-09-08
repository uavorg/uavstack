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

package com.creditease.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.Logger;

public class UAVMetaDataMgr {

    public static final Set<String> SystemMeta = new HashSet<String>();

    static {
        SystemMeta.add("NetCardIndex");
        SystemMeta.add("NetCardName");
        SystemMeta.add("JAppGroup");
        SystemMeta.add("JAppID");
    }

    private Map<String, Object> metaData = new HashMap<String, Object>();

    private Logger log;

    public UAVMetaDataMgr(Logger log) {

        this.log = log;
    }

    /**
     * saveSupporterMeta
     * 
     * @param supporters
     */
    @SuppressWarnings("unchecked")
    public void addSupporterMeta(String[] supporters) {

        if (supporters == null) {
            return;
        }

        Map<String, String> supp = (Map<String, String>) this.metaData.get("supporters");

        if (supp == null) {
            supp = new HashMap<String, String>();
            metaData.put("supporters", supp);
        }

        for (String supporter : supporters) {
            supp.put(supporter, "");
        }

        this.saveMetaData();
    }

    /**
     * NOTE：为了区分是那个JVM程序的meta，默认使用JAppID区分，如果没有则使用工作路径
     * 
     * @return
     */
    private String getJVMProcID() {

        String JAppID = System.getProperty("JAppID");

        if (StringHelper.isEmpty(JAppID)) {
            JAppID = System.getProperty("user.dir");

            if (StringHelper.isEmpty(JAppID)) {
                JAppID = IOHelper.getCurrentPath();
            }

            JAppID = JAppID.replace("\\", "_").replace("/", "_").replace(" ", "_").replace(":", "-");
        }
        return JAppID;
    }

    /**
     * removeSupporterMeta
     * 
     * @param supporters
     */
    @SuppressWarnings("unchecked")
    public void removeSupporterMeta(String[] supporters) {

        if (supporters == null) {
            return;
        }

        Map<String, String> supp = (Map<String, String>) this.metaData.get("supporters");

        if (supp == null) {
            supp = new HashMap<String, String>();
            this.metaData.put("supporters", supp);
        }

        for (String supporter : supporters) {
            supp.remove(supporter);
        }

        this.saveMetaData();
    }

    /**
     * loadSupporterMeta
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public String[] loadSupporterMeta() {

        Map<String, String> supp = (Map<String, String>) this.metaData.get("supporters");

        if (supp != null) {
            String[] supps = new String[supp.size()];

            supps = supp.keySet().toArray(supps);

            return supps;
        }

        return null;
    }

    /**
     * loadMetaData
     */
    @SuppressWarnings("unchecked")
    public void loadMetaData() {

        String metaPath = (String) UAVServer.instance().getServerInfo(CaptureConstants.INFO_MOF_METAPATH) + "uavmof_"
                + this.getJVMProcID() + ".meta";

        if (!IOHelper.exists(metaPath)) {
            return;
        }

        String metaDataStr = IOHelper.readTxtFile(metaPath, "utf-8");

        if (StringHelper.isEmpty(metaDataStr)) {
            return;
        }

        // 暂存JAppMOFMetaData
        System.setProperty("JAppMOFMetaData", metaDataStr);

        this.metaData = JSONHelper.toObject(metaDataStr, Map.class);

        // flush systemMeta to SystemProperty
        for (String key : SystemMeta) {

            if (metaData.containsKey(key)) {
                System.setProperty(key, (String) metaData.get(key));
            }
        }
        if (log.isLogEnabled()) {
            log.info("MetaData Loaded: " + metaDataStr);
        }
    }

    /**
     * saveMetaData
     */
    public void saveMetaData() {

        String metaPath = (String) UAVServer.instance().getServerInfo(CaptureConstants.INFO_MOF_METAPATH) + "uavmof_"
                + this.getJVMProcID() + ".meta";

        String metaDataStr = JSONHelper.toString(this.metaData);

        try {
            // 暂存JAppMOFMetaData
            System.setProperty("JAppMOFMetaData", metaDataStr);

            IOHelper.writeTxtFile(metaPath, metaDataStr, "utf-8", false);

            if (log.isLogEnabled()) {
                log.info("MetaData Saved: " + metaDataStr);
            }
        }
        catch (IOException e) {
            log.error("MetaData Save Fail: ", e);
        }
    }

    public void addMetaData(Map<String, Object> metaMap) {

        for (String key : metaMap.keySet()) {
            metaData.put(key, metaMap.get(key));
        }
        saveMetaData();
    }

    public void addMetaData(String key, String value) {

        metaData.put(key, value);

        saveMetaData();
    }

}
