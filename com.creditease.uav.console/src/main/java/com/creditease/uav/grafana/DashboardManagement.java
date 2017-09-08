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

package com.creditease.uav.grafana;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;

public class DashboardManagement {

    private static String dashboardTemp = null;
    private static String datasourceTemp = null;
    private static String esPanelTemp = null;
    private static String opentsdbPanelTemp = null;
    private static DashboardManagement dm = null;

    public static DashboardManagement getInstance() {

        if (dm == null) {
            synchronized (DashboardManagement.class) {
                if (dm == null) {
                    dm = new DashboardManagement();
                }
            }
        }
        return dm;
    }

    private DashboardManagement() {

    }

    public enum DSType {
        OPENTSDB("opentsdb"), ES("es");

        private String type;

        DSType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {

            return type;
        }
    }

    /**
     * 根据配置结合模板创建dashboard
     *
     */
    @SuppressWarnings({ "rawtypes" })
    public void dashboardCreate(String configString, String contextPath) {

        List<Map> configs = JSONHelper.toObjectArray(configString, Map.class);

        for (Map config : configs) {
            String orgName = (String) config.get("orgName");

            if (dashboardTemp == null) {
                dashboardTemp = IOHelper.readTxtFile(contextPath + "/config/dashboardTemp.json", "UTF-8");
            }
            // 根据配置创建模板
            String template = templateInit(config);

            Map<String, Object> params = new HashMap<String, Object>();

            params.put("template", template);
            params.put("orgName", orgName);
            params.put("contextPath", contextPath);
            params.put("usage", "dashboardCreate");

            GrafanaHttpUtils.doAsyncHttp("get", "/api/orgs/name/" + EncodeHelper.urlEncode(orgName), null,
                    new GetOrgIdbyNameCallBack(params));
        }

    }

    /**
     * 根据配置初始化dashboard模板
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String templateInit(Map config) {

        String title = (String) config.get("title");
        String servId = (String) config.get("servId");
        String urlId = (String) config.get("urlId");
        String appId = (String) config.get("appId");
        List<String> appInstids = (List<String>) config.get("appInstids");
        String clientId = (String) config.get("clientId");

        String temp = dashboardTemp.replace("TITLE", title).replace("servId", servId.replace(":", "/u003a"))
                .replace("appId", appId.replace(":", "/u003a")).replace("urlStr", urlId).replace("clientStr", clientId);

        Map template = JSONObject.parseObject(temp, Map.class);
        List<Map> rows = (List<Map>) ((Map) template.get("dashboard")).get("rows");
        List<Map> appRespPanel = (List<Map>) rows.get(1).get("panels");
        List<Map> urlRespPanel = (List<Map>) rows.get(2).get("panels");
        List<Map> clientRespPanel = (List<Map>) rows.get(3).get("panels");

        for (String appInstid : appInstids) {

            String appurl = appInstid.split("---")[0];
            String addr = appurl.split("//")[1].split("/")[0];
            String appid = appInstid.split("---")[1];
            if (appurl.endsWith("/")) {
                appurl = appurl.substring(0, appurl.length() - 1);
            }
            String urlInstid = appurl + urlId;
            String clientInstid = addr + "#" + appid + "#" + clientId;

            initPanel(appRespPanel, DataStoreHelper.encodeForOpenTSDB(appInstid));
            initPanel(urlRespPanel, DataStoreHelper.encodeForOpenTSDB(urlInstid));
            initPanel(clientRespPanel, DataStoreHelper.encodeForOpenTSDB(clientInstid));
        }
        return JSONObject.toJSON(template).toString();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initPanel(List<Map> panels, String instid) {

        for (Map panel : panels) {
            List<Map> targets = (List<Map>) panel.get("targets");
            if (targets.size() == 1 && ((String) ((Map) (targets.get(0).get("tags"))).get("instid")).endsWith("*Id")) {
                ((Map) (targets.get(0).get("tags"))).put("instid", instid);
            }
            else {
                String targetJson = JSONObject.toJSONString(targets.get(0));
                Map target = JSONObject.parseObject(targetJson);
                ((Map) target.get("tags")).put("instid", instid);
                targets.add(target);
            }
        }

    }

    /**
     * 根据配置创建datasource
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void datasourceCreate(String contextPath, String orgId, Map config) {

        config.put("url", GrafanaHttpUtils.getConfigValue("datasource." + config.get("type") + ".url"));
        Map<String, String> header = new HashMap<String, String>();
        if (datasourceTemp == null) {
            datasourceTemp = IOHelper.readTxtFile(contextPath + "/config/datasourceTemp.json", "UTF-8");
        }
        Map template = JSONHelper.toObject(datasourceTemp, Map.class);
        mapReplace(template, config);
        if (config != null) {
            header.put("X-Grafana-Org-Id", orgId);
            GrafanaHttpUtils.doAsyncHttp("post", "/api/datasources", JSONHelper.toString(template), header, null);

        }
    }

    /**
     * 根据配置添加panel
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map addPanel(String panelType, Map template, Map panelConfig, String contextPath) {

        Map dashboard = (Map) template.get("dashboard");
        List<Map> rows = (List) dashboard.get("rows");

        // 计算新增panel id
        int maxid = 0;
        for (Map row : rows) {
            List<Map> panels = (List<Map>) row.get("panels");
            for (Map panel : panels) {
                int id = (int) panel.get("id");
                maxid = (id > maxid) ? id : maxid;
            }
        }
        panelConfig.put("id", maxid + 1);

        DSType datasource = null;
        int rowIndex = 0;
        switch (panelType) {
            case "custom":
                datasource = DSType.OPENTSDB;
                rowIndex = 4;
                break;
            case "log":
                datasource = DSType.ES;
                rowIndex = 5;
                break;
            case "ivc":
                datasource = DSType.ES;
                rowIndex = 6;
                break;
            default:
                break;
        }
        // 根据配置创建panel并添加到源dashboard中
        Map panelMap = buildPanel(contextPath, datasource, panelConfig);
        Map row = rows.get(rowIndex);
        List<Map> panels = (List<Map>) row.get("panels");
        panels.add(panelMap);
        return template;
    }

    /**
     * 根据配置创建panel
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, Object> buildPanel(String contextPath, DSType dsType, Map configData) {

        Map<String, Object> template = null;
        switch (dsType) {
            case OPENTSDB:
                if (opentsdbPanelTemp == null) {
                    opentsdbPanelTemp = IOHelper.readTxtFile(contextPath + "/config/opentsdbPanelTemp.json", "UTF-8");
                }
                template = JSONHelper.toObject(opentsdbPanelTemp, Map.class);
                break;
            case ES:
                if (esPanelTemp == null) {
                    esPanelTemp = IOHelper.readTxtFile(contextPath + "/config/esPanelTemp.json", "UTF-8");
                }
                template = JSONHelper.toObject(esPanelTemp, Map.class);
                break;
            default:
                break;
        }
        mapReplace(template, configData);
        return template;
    }

    /**
     * 将config Map中的字段深度递归的替换到template Map中
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void mapReplace(Map template, Map config) {

        for (Object key : config.keySet()) {
            Object value = config.get(key);
            if (!template.containsKey(key)) {
                template.put(key, value);
                continue;
            }
            if (value instanceof Map) {
                mapReplace((Map<String, Object>) template.get(key), (Map<String, Object>) value);
            }
            else if (value instanceof List) {
                listReplace((List) template.get(key), (List) value);
            }
            else {
                template.put(key, value);
            }
        }
    }

    /**
     * 将config List中的元素深度递归的替换到template Map中
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void listReplace(List template, List config) {

        for (int i = 0; i < config.size(); i++) {
            Object value = config.get(i);

            if (value instanceof Map) {
                // when configList size is bigger than template,we copy the first obj in template to do the replace if
                // it exists
                if (template.size() <= i) {
                    if (template.size() == 0) {
                        template.add(value);
                        continue;
                    }
                    else {
                        String copy = JSONHelper.toString(template.get(0));
                        Map temp = JSONHelper.toObject(copy, Map.class);
                        template.add(temp);
                    }
                }
                mapReplace((Map) template.get(i), (Map<String, Object>) value);
            }
            else if (value instanceof List) {
                // when list in list and configList size is bigger, we just put it in template;
                if (template.size() <= i) {
                    template.add(value);
                    continue;
                }
                listReplace((List) template.get(i), (List) value);
            }
            else {
                if (template.size() <= i) {
                    template.add(value);
                    continue;
                }
                template.set(i, value);
            }
        }
    }

}
