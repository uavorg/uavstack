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

package com.creditease.uav.monitorframework.webservice.interceptors;

import java.util.ArrayList;
import java.util.List;

import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.profiling.handlers.webservice.WebServiceProfileInfo;

public class WebServiceListenerIT {

    String appid;

    public WebServiceListenerIT(String appid) {

        this.appid = appid;
    }

    public void obtainWsInfo(Object address, Object impl) {

        InterceptContext ic = InterceptSupport.instance().getThreadLocalContext(Event.WEBCONTAINER_STARTED);
        @SuppressWarnings("unchecked")
        List<WebServiceProfileInfo> list = (ArrayList<WebServiceProfileInfo>) ic.get("webservice.profile.info");
        if (null == list) {

            list = new ArrayList<WebServiceProfileInfo>();

            ic.put("webservice.profile.info", list);
        }
        setWsInfo(list, address, impl);
    }

    private void setWsInfo(List<WebServiceProfileInfo> list, Object address, Object impl) {

        WebServiceProfileInfo wsli = new WebServiceProfileInfo();
        wsli.setImpl(impl);
        wsli.setUrl((String) address);
        list.add(wsli);
    }
}
