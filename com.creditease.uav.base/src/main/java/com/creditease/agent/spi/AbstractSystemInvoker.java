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

package com.creditease.agent.spi;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;

public abstract class AbstractSystemInvoker<T, U> extends AbstractComponent {

    /**
     * 
     * syncServiceRouteWorker description: need sync the service routes
     *
     */
    private class syncServiceRouteWorker extends AbstractTimerWork {

        public syncServiceRouteWorker(String cName, String feature) {
            super(cName, feature);
        }

        @Override
        public void run() {

            Set<String> services = connectionMgrPool.keySet();

            for (String serviceName : services) {
                ConnectionFailoverMgr cfm = accessServiceRoute(serviceName);

                if (cfm == null) {
                    uninstallServiceRoute(serviceName);
                }
            }

            Set<String> serviceStates = serviceDiscoveryStates.keySet();

            for (String serviceName : serviceStates) {

                /**
                 * ServiceDiscoveryState is DISCARD, then check if it should be deleted
                 */
                ServiceDiscoveryState sds = serviceDiscoveryStates.get(serviceName);
                if (sds.getState() == ServiceDiscoveryState.State.DISCARD
                        && System.currentTimeMillis() - sds.getDiscardTime() > 60000) {
                    serviceDiscoveryStates.remove(serviceName);
                }
            }
        }
    }

    /**
     * 
     * ServiceDiscoveryState description: 控制服务发现路由的状态转换
     * 
     * 第一次调用某服务，创建ServiceDiscoveryState，状态INIT
     * 
     * 如果获取服务路由成功，则状态为WORK
     * 
     * 如果获取服务路由失败，则状态为DISCARD，记录DISCARD时间戳
     * 
     * 另一方面：
     * 
     * 有个线程会定时扫描，已经获得路由的服务，去同步其最新数据
     * 
     * 如果服务路由仍能获得，则更新服务路由，状态仍为WORK
     * 
     * 如果服务路由为空，则表明该服务已经没有可用节点，服务路由被删除，同时状态为DISCARD，记录DISCARD时间戳
     * 
     * 该线程也扫描一遍所有的ServiceDiscoveryState
     * 
     * 如果ServiceDiscoveryState为DISCARD，且DISCARD时间戳已经过去60秒，则删除该ServiceDiscoveryState
     * 
     * 总结：
     * 
     * 该机制带来几个结果：
     * 
     * 1. 没有服务路由的服务名，两次访问心跳查询服务的时间间隔被限制为60s，防止无节制，恶意调用
     * 
     * 2. 服务路由消失，会在同步过程，被干掉
     * 
     * 3. 服务路由被干掉后，ServiceDiscoveryState不会马上消失（参见1），60s后才会消失，也就是60s后又以全新的服务调用INIT开始了
     * 
     * 4. 达成20s内更新到最新的服务路由，服务全挂的最长自动恢复时间为80s
     */
    private static class ServiceDiscoveryState {

        public enum State {
            INIT, WORK, DISCARD
        }

        private long discardTime = -1;

        private State state = State.INIT;

        public void setState(State state) {

            this.state = state;
        }

        public State getState() {

            return this.state;
        }

        public void recordDiscardTime() {

            this.discardTime = System.currentTimeMillis();
        }

        public long getDiscardTime() {

            return discardTime;
        }

        public boolean canDoServiceDiscovery() {

            switch (state) {
                case DISCARD:
                    break;
                case INIT:
                    return true;
                case WORK:
                    return true;
                default:
                    break;

            }

            return false;
        }
    }

    protected Map<String, ConnectionFailoverMgr> connectionMgrPool = new ConcurrentHashMap<String, ConnectionFailoverMgr>();

    protected Map<String, ServiceDiscoveryState> serviceDiscoveryStates = new ConcurrentHashMap<String, ServiceDiscoveryState>();

    public AbstractSystemInvoker(String cName, String feature) {
        super(cName, feature);
    }

    /**
     * installServiceRoute
     * 
     * @param serviceName
     * @param urls
     * @param retryInterval
     */
    protected ConnectionFailoverMgr installServiceRoute(String serviceName, String[] urls, long retryInterval) {

        if (StringHelper.isEmpty(serviceName) || urls == null || urls.length == 0) {
            return null;
        }

        ConnectionFailoverMgr cfm = connectionMgrPool.get(serviceName);

        if (cfm == null) {
            cfm = new ConnectionFailoverMgr(Arrays.asList(urls), retryInterval);
            connectionMgrPool.put(serviceName, cfm);

        }
        else {
            cfm.reset(urls);
            cfm.setRetryInterval(retryInterval);
        }

        ServiceDiscoveryState sds = serviceDiscoveryStates.get(serviceName);

        if (sds != null) {
            sds.setState(ServiceDiscoveryState.State.WORK);
        }

        return cfm;
    }

    /**
     * uninstallServiceRoute
     * 
     * @param serviceName
     */
    protected void uninstallServiceRoute(String serviceName) {

        if (StringHelper.isEmpty(serviceName)) {
            return;
        }

        ConnectionFailoverMgr cfm = connectionMgrPool.remove(serviceName);

        if (cfm == null) {
            return;
        }

        ServiceDiscoveryState sds = serviceDiscoveryStates.get(serviceName);

        if (sds != null) {
            sds.recordDiscardTime();
            sds.setState(ServiceDiscoveryState.State.DISCARD);
        }
    }

    /**
     * 所有AbstractSystemInvoker派生类应该使用该方法获取ConnectionFailoverMgr
     * 
     * @param serviceName
     * @return
     */
    protected ConnectionFailoverMgr getServiceRoute(String serviceName) {

        if (!serviceDiscoveryStates.containsKey(serviceName)) {
            ServiceDiscoveryState sds = new ServiceDiscoveryState();
            serviceDiscoveryStates.put(serviceName, sds);
        }

        ConnectionFailoverMgr cfm = this.connectionMgrPool.get(serviceName);

        if (cfm != null) {
            return cfm;
        }

        ServiceDiscoveryState sds = serviceDiscoveryStates.get(serviceName);

        if (sds.canDoServiceDiscovery() == false) {
            return null;
        }

        cfm = accessServiceRoute(serviceName);

        if (sds.getState() == ServiceDiscoveryState.State.INIT && cfm == null) {
            sds.recordDiscardTime();
            sds.setState(ServiceDiscoveryState.State.DISCARD);
        }

        return cfm;
    }

    /**
     * 从心跳查询服务获取服务路由
     * 
     * @param serviceName
     * @return
     */
    private ConnectionFailoverMgr accessServiceRoute(String serviceName) {

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("hbclientagent",
                "HeartBeatClientAgent");

        if (afc == null) {
            return null;
        }

        ConnectionFailoverMgr cfm = null;

        String[] urls = (String[]) afc.exchange("hbclientagent.service.discovery", serviceName);
        if (urls != null) {
            cfm = this.installServiceRoute(serviceName, urls, 5000);
        }

        return cfm;
    }

    public void start() {

        String timerName = this.cName + "-syncServiceRoute";

        this.getTimerWorkManager().scheduleWork(timerName, new syncServiceRouteWorker(timerName, feature), 0, 20000);
    }

    public void stop() {

        String timerName = this.cName + "-syncServiceRoute";

        this.getTimerWorkManager().cancel(timerName);

        connectionMgrPool.clear();
    }

    public abstract byte[] invoke(String serviceName, T msg);

    public abstract byte[] invoke(String serviceName, String serviceSubPath, T msg);

    public abstract <V> V invoke(String serviceName, T msg, Class<V> returnClass);

    public abstract <V> V invoke(String serviceName, String serviceSubPath, T msg, Class<V> returnClass);

    public abstract void invoke(String serviceName, T msg, U callback);

    public abstract void invoke(String serviceName, String serviceSubPath, T msg, U callback);
}
