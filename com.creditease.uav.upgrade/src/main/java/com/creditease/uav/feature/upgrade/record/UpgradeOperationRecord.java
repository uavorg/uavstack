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

package com.creditease.uav.feature.upgrade.record;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.helpers.JSONHelper;

/**
 * 
 * ProcessRecord description: This class is to wrap the upgrade process record for every phase.
 *
 */
public class UpgradeOperationRecord extends JSONObject {

    private static final long serialVersionUID = 1L;

    private String phase;
    private boolean result;
    private Object action;

    public UpgradeOperationRecord(Map<String, Object> recordMap) {
        super(recordMap);
        this.phase = this.getString("phase");
        this.result = this.getBooleanValue("result");
        this.action = this.get("action");
    }

    @SuppressWarnings("unchecked")
    public UpgradeOperationRecord(String jsonStr) {
        this(JSONHelper.toObject(jsonStr, Map.class));
    }

    public String getPhase() {

        return phase;
    }

    public boolean getResult() {

        return result;
    }

    public Object getAction() {

        return action;
    }

    public static void main(String[] args) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("phase", "start");
        map.put("action", new HashMap<String, String>());
        UpgradeOperationRecord pr = new UpgradeOperationRecord(map);
        pr.toJSONString();
        // System.out.println(pr.toJSONString());

        UpgradeOperationRecord pr1 = new UpgradeOperationRecord(pr.toJSONString());
        pr1.getPhase();
        // System.out.println(pr1.getPhase());

    }

}
