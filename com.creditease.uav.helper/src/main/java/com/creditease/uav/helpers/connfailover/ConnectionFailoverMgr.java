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

package com.creditease.uav.helpers.connfailover;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * ConnectionFailoverMgr description: 连接Failover自动管理器
 *
 */
public class ConnectionFailoverMgr {

    /**
     * 
     * ConnectionState
     *
     */
    private class ConnectionState {

        private String url;

        /**
         * state=0, normal; state=1, retrying; state=-1, invalid
         */
        private volatile AtomicInteger isOK = new AtomicInteger(0);

        private volatile long lastFailTimestamp = -1;

        private volatile long shouldWaitTime = 0;

        private volatile AtomicInteger retrySkipRound = new AtomicInteger(3);

        public ConnectionState(String url) {
            this.url = url;

        }

        public String getUrl() {

            return url;
        }

        public int getState() {

            return isOK.get();
        }

        public void setOK(int state) {

            int curState = this.isOK.get();

            if (curState == state) {
                return;
            }

            switch (state) {

                case -1:
                    if (this.isOK.get() != -1) {

                        synchronized (this.isOK) {

                            if (this.isOK.get() != -1) {
                                lastFailTimestamp = System.currentTimeMillis();

                                if (shouldWaitTime < 5 * expireTimeout) {
                                    shouldWaitTime += expireTimeout;
                                }
                                else if (shouldWaitTime >= 5 * expireTimeout) {
                                    shouldWaitTime = 5 * expireTimeout;
                                }

                                retrySkipRound.set(3);
                                this.isOK.set(-1);
                            }
                        }
                    }

                    break;
                case 0:
                    if (this.isOK.get() != 0) {

                        synchronized (this.isOK) {

                            if (this.isOK.get() != 0) {
                                shouldWaitTime = 0;
                                lastFailTimestamp = -1;
                                retrySkipRound.set(3);
                                this.isOK.set(0);
                            }
                        }
                    }

                    break;
                default:
                    break;
            }

        }

        public boolean checkIfRetry() {

            long span = System.currentTimeMillis() - lastFailTimestamp;

            if (span < shouldWaitTime) {
                return false;
            }

            if (isOK.get() == -1) {

                synchronized (this.isOK) {

                    if (isOK.get() == -1) {

                        isOK.compareAndSet(-1, 1);

                        return true;
                    }
                }
            }

            return false;
        }

        public boolean checkIfReachRetrySkipRound() {

            if (this.retrySkipRound.get() == 0) {
                return true;
            }

            this.retrySkipRound.decrementAndGet();

            return false;

        }

        public String doRetry() {

            return this.getUrl();
        }
    }

    private volatile Map<String, ConnectionState> addresses = new ConcurrentHashMap<String, ConnectionState>();
    private volatile List<String> addressesIndex = new CopyOnWriteArrayList<String>();

    private long expireTimeout = 30000;

    private volatile AtomicInteger curConnIndex = new AtomicInteger(-1);

    public ConnectionFailoverMgr(Collection<String> addressList, long expireTimeout) {
        if (addressList == null) {

            return;
        }

        this.expireTimeout = expireTimeout;

        for (String addr : addressList) {

            String addrStr = addr.trim();

            ConnectionState conn = new ConnectionState(addrStr);

            addresses.put(addrStr, conn);
            addressesIndex.add(addrStr);
        }
    }

    public ConnectionFailoverMgr(String addressList, long expireTimeout) {

        if (addressList == null) {

            return;
        }

        this.expireTimeout = expireTimeout;

        String[] addrArray = addressList.split(",");

        for (String addr : addrArray) {

            String addrStr = addr.trim();

            addConnection(addrStr);
        }
    }

    public void addConnection(String url) {

        if (!addresses.containsKey(url)) {
            ConnectionState conn = new ConnectionState(url);
            addresses.put(url, conn);
            addressesIndex.add(url);
        }
    }

    public void removeConnection(String url) {

        if (addresses.containsKey(url)) {
            addresses.remove(url);
            addressesIndex.remove(url);
        }
    }

    public void reset(String[] urls) {

        if (urls == null) {
            return;
        }

        addresses.clear();
        addressesIndex.clear();

        for (String url : urls) {
            this.addConnection(url);
        }
    }

    public void setRetryInterval(long retryInterval) {

        this.expireTimeout = retryInterval;
    }

    /**
     * put fail url
     * 
     * @param url
     */
    public void putFailConnection(String url) {

        if (addresses.containsKey(url)) {

            ConnectionState hbss = addresses.get(url);

            if (hbss.getState() != -1) {
                hbss.setOK(-1);
            }
        }
    }

    /**
     * get the connection
     * 
     * @return
     */
    public String getConnection() {

        boolean isSelected = false;
        ConnectionState selState = null;
        int start = this.curConnIndex.get();
        int index = start;

        /**
         * NOTE: auto load balance
         */
        for (int i = 0; i < this.addresses.size(); i++) {

            index++;

            if (index > this.addresses.size() - 1) {
                index = 0;
            }

            String conn = this.addressesIndex.get(index);
            ConnectionState state = this.addresses.get(conn);

            /**
             * Step 1: ensure there is only 1 request thread to retry this url
             */
            if (state.getState() == -1 && state.checkIfRetry() == true) {
                return state.doRetry();
            }

            /**
             * Step 2: if state==1, need check if reach retry skip round if retry skip round==0, then set the state =0
             * ,that means in those round, this url was not be set to -1 if retry skip round>0, continue, that means
             * skip this url for current request round
             */
            if (state.getState() == 1) {

                if (state.checkIfReachRetrySkipRound() == true) {
                    state.setOK(0);
                }
                else {
                    continue;
                }
            }

            /**
             * Step 3: for current request round, if there is no selected good url, then we select this url (state=0) as
             * the selected one.
             */
            if (isSelected == false && state.getState() == 0) {

                isSelected = true;
                selState = state;
                this.curConnIndex.compareAndSet(start, index);
            }
        }

        if (isSelected == true) {
            return selState.getUrl();
        }
        else {
            return null;
        }
    }
}
