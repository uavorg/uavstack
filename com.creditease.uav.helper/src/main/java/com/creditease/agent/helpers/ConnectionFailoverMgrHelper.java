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

import java.util.Collection;

import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;

public class ConnectionFailoverMgrHelper {

    private ConnectionFailoverMgrHelper() {
    }

    /**
     * getConnectionFailoverMgr
     * 
     * @param addressList
     * @param expireTimeout
     * @return
     */
    public static ConnectionFailoverMgr getConnectionFailoverMgr(String addressList, long expireTimeout) {

        ConnectionFailoverMgr cfmgr = new ConnectionFailoverMgr(addressList, expireTimeout);

        return cfmgr;
    }

    /**
     * getConnectionFailoverMgr
     * 
     * @param addressList
     * @param expireTimeout
     * @return
     */
    public static ConnectionFailoverMgr getConnectionFailoverMgr(Collection<String> addressList, long expireTimeout) {

        ConnectionFailoverMgr cfmgr = new ConnectionFailoverMgr(addressList, expireTimeout);

        return cfmgr;
    }
}
