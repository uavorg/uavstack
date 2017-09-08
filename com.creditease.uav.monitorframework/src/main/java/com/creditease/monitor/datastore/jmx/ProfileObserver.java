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

package com.creditease.monitor.datastore.jmx;

import java.util.Collection;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.datastore.DataObserver;
import com.creditease.monitor.datastore.spi.DataObserverListener;
import com.creditease.monitor.log.Logger;
import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;

public class ProfileObserver implements ProfileObserverMBean {

    private final static Logger log = UAVServer.instance().getLog();

    protected Profile profile;

    public ProfileObserver(Profile profile) {
        this.profile = profile;
    }

    @Override
    public String getData() {

        /**
         * for iplink, we should remove those iplink not active over 1 min, the health manager will help to maintain the
         * state change
         */
        removeExpireIpLinkPEI();

        String data = this.profile.getRepository().toJSONString();

        // in case of \\ to /
        data = data.replace("\\", "/");

        return data;
    }

    @SuppressWarnings("unchecked")
    private void removeExpireIpLinkPEI() {

        long curTime = System.currentTimeMillis();

        long appTimeout = DataConvertHelper.toLong(System.getProperty("com.creditease.uav.iplink.app.timeout"),
                3600000);
        long userTimeout = DataConvertHelper.toLong(System.getProperty("com.creditease.uav.iplink.user.timeout"),
                60000);
        long proxyTimeout = DataConvertHelper.toLong(System.getProperty("com.creditease.uav.iplink.proxy.timeout"),
                3600000);
        long proxyAppTimeout = DataConvertHelper
                .toLong(System.getProperty("com.creditease.uav.iplink.proxy.app.timeout"), 3600000);
        long proxyUserTimeout = DataConvertHelper
                .toLong(System.getProperty("com.creditease.uav.iplink.proxy.user.timeout"), 60000);

        ProfileElement pe = this.profile.getRepository().getElement(ProfileConstants.PROELEM_IPLINK);

        ProfileElementInstance[] peis = pe.getInstances();

        for (ProfileElementInstance pei : peis) {

            // step 1: check pei expire
            String peiId = pei.getInstanceId();

            long peiTimeout = 0;

            if (peiId.indexOf("browser") == 0) {
                peiTimeout = userTimeout;
            }
            else if (peiId.indexOf("proxy") == 0) {
                peiTimeout = proxyTimeout;
            }
            else {
                peiTimeout = appTimeout;
            }

            long ts = (Long) pei.getValues().get("ts");

            if (curTime - ts > peiTimeout) {
                pe.removeInstance(pei.getInstanceId());
                continue;
            }

            // step 2: check pei's clients expire
            if (!pei.getValues().containsKey(ProfileConstants.PEI_CLIENTS)) {
                continue;
            }

            Map<String, Long> clients = (Map<String, Long>) pei.getValues().get(ProfileConstants.PEI_CLIENTS);

            for (String key : clients.keySet()) {

                Long clientts = (long) clients.get(key);

                long clientTimeout = 0;

                if (key.indexOf("user") == 0) {
                    clientTimeout = proxyUserTimeout;
                }
                else {
                    clientTimeout = proxyAppTimeout;
                }

                if (curTime - clientts > clientTimeout) {
                    clients.remove(key);
                }
            }
        }
    }

    @Override
    public boolean isUpdate() {

        return this.profile.getRepository().isUpdate();
    }

    @Override
    public void setUpdate(boolean check) {

        this.profile.getRepository().setUpdate(check);
    }

    /**
     * Profile的事件监控接口
     * 
     * @param data
     *            接收数据格式：{event:"",data:""} event: 是每个listener自定义的一个事件字符串 data：是每个listener自动以的事件数据
     * @return 必要时返回处理结果
     * 
     *         例如 {event:"uav.dp.service.register",data:
     *         "{appid:\"com.creditease.uav.monitorframework.buildFat\",infos:[{id:\"test\",url:\"testurl\",name:\"testservice\",s:0,sval:\"testval\"}]}"}
     */

    @SuppressWarnings({ "unchecked" })
    @Override
    public String optData(String data) {

        Map<String, String> jo = JSONHelper.toObject(data, Map.class);

        if (null == jo) {
            return null;
        }

        String event = jo.get("event");

        if (null == event) {
            return null;
        }

        String dat = jo.get("data");

        if (null == dat) {
            return null;
        }

        Collection<DataObserverListener> listeners = DataObserver.instance().getListeners();

        for (DataObserverListener l : listeners) {
            /**
             * only choose the first handlable listener
             */
            if (l.isHandlable(event)) {
                try {
                    return l.handle(dat);
                }
                catch (Exception e) {
                    log.error("DataObserverListener[" + l.getClass().getName() + "] handle FAIL,event=" + event
                            + ",data=" + data, e);
                    continue;
                }
            }
        }

        return null;
    }

    @Override
    public int getState() {

        return this.profile.getRepository().getState();
    }
}
