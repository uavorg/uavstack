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
package com.creditease.uav.apphub.sso;

import javax.servlet.http.HttpServletRequest;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.exception.ApphubException;

public class GUISSOClientFactory {

    private static volatile GUISSOClient client = null;

    public static GUISSOClient getGUISSOClient(HttpServletRequest request) {

        if (client == null) {
            synchronized (GUISSOClientFactory.class) {
                if (client == null) {
                    client = createGUISSOClient(request);
                }
            }
        }
        return client;
    }

    private static GUISSOClient createGUISSOClient(HttpServletRequest request) {

        String ClientImplClassName = request.getServletContext().getInitParameter("uav.apphub.sso.implclass");

        if (StringHelper.isEmpty(ClientImplClassName)) {
            ClientImplClassName = "com.creditease.uav.apphub.sso.GUISSOSimpleClient";
        }

        GUISSOClient GUISSOClientImpl = null;

        try {
            Class<?> cls = request.getClass().getClassLoader().loadClass(ClientImplClassName);
            GUISSOClientImpl = (GUISSOClient) cls.getDeclaredConstructor(HttpServletRequest.class).newInstance(request);

        }
        catch (Exception e) {
            throw new ApphubException(e);
        }

        return GUISSOClientImpl;
    }
}
