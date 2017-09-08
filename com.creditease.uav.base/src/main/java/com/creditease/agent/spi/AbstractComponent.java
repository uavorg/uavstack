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

import java.util.Properties;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.SystemStarter;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.NotificationEvent;

public abstract class AbstractComponent {

    protected String cName = null;
    protected String feature = null;
    protected ISystemLogger log = null;
    protected ComponentMonitor privateMonitor = null;
    protected boolean usePrviateMonitor = false;

    public AbstractComponent(String cName, String feature) {
        this.cName = cName;
        this.feature = feature;
        this.log = getLogger(cName, feature);
        /**
         * NOTE: automatic register abstract component except Abstract1NTask, as Abstract1NTask may be created as many
         * as we can
         */
        if (System.getProperty("com.creditease.uav.debugmode") == null
                && !Abstract1NTask.class.isAssignableFrom(this.getClass())) {
            this.getConfigManager().registerComponent(feature, cName, this);
        }
    }

    /**
     * getLogger
     * 
     * @param cName
     * @param feature
     * @return
     */
    protected ISystemLogger getLogger(String cName, String feature) {

        return SystemLogger.getLogger(AbstractComponent.class);
    }

    /**
     * get ITimerWorkManager
     * 
     * @return ITimerWorkManager
     */
    public ITimerWorkManager getTimerWorkManager() {

        return (ITimerWorkManager) ConfigurationManager.getInstance().getComponent("Global", "ITimerWorkManager");
    }

    /**
     * get ISystemInvokerMgr
     * 
     * @return
     */
    public ISystemInvokerMgr getSystemInvokerMgr() {

        return (ISystemInvokerMgr) ConfigurationManager.getInstance().getComponent("Global", "ISystemInvokerMgr");
    }

    /**
     * get ISystemActionEngineMgr
     * 
     * @return ISystemActionEngineMgr
     */
    public ISystemActionEngineMgr getActionEngineMgr() {

        return (ISystemActionEngineMgr) ConfigurationManager.getInstance().getComponent("Global",
                "ISystemActionEngineMgr");
    }

    /**
     * get IConfigurationManager
     * 
     * @return IConfigurationManager
     */
    public IConfigurationManager getConfigManager() {

        return ConfigurationManager.getInstance();
    }

    /**
     * get I1NQueueWorkerMgr
     * 
     * @return I1NQueueWorkerMgr
     */
    public I1NQueueWorkerMgr get1NQueueWorkerMgr() {

        return (I1NQueueWorkerMgr) ConfigurationManager.getInstance().getComponent("Global", "I1NQueueWorkerMgr");
    }

    /**
     * getSystemStarter
     * 
     * @return
     */
    public SystemStarter getSystemStarter() {

        return (SystemStarter) ConfigurationManager.getInstance().getComponent("Global", "SystemStarter");
    }

    /**
     * get global resource limitation auditor
     * 
     * @return
     */
    public ResourceLimitationAuditor getGlobalResourceLimitationAuditor() {

        return (ResourceLimitationAuditor) ConfigurationManager.getInstance().getComponent("Global",
                "ResourceLimitationAuditor");
    }

    /**
     * get forkjoin worker manager
     * 
     * @return
     */
    public IForkjoinWorkerMgr getForkjoinWorkerMgr() {

        return (IForkjoinWorkerMgr) ConfigurationManager.getInstance().getComponent("Global", "IForkjoinWorkerMgr");
    }

    /**
     * get global common resource object
     * 
     * @param resource
     * @param objName
     * @return
     */
    public Object getComponentResource(String resource, String objName) {

        return ConfigurationManager.getInstance().getComponentResource(resource, objName);
    }

    // /**
    // * enable private monitor
    // */
    // public void enablePrivateMonitor() {
    //
    // if (this.privateMonitor == null) {
    //
    // synchronized (this) {
    // if (this.privateMonitor == null) {
    // this.privateMonitor = ComponentMonitor.getMonitor(this.feature + "." + this.cName + ".monitor");
    // this.usePrviateMonitor = true;
    // }
    // }
    //
    // }
    // }
    //
    // /**
    // * disable private monitor
    // */
    // public void disablePrivateMonitor() {
    //
    // if (this.privateMonitor != null) {
    // synchronized (this) {
    // if (this.privateMonitor != null) {
    // this.usePrviateMonitor = false;
    // this.privateMonitor = null;
    // }
    // }
    // }
    // }

    // /**
    // * incre monitor value
    // *
    // * @param key
    // */
    // public void increMonitorValue(String key) {
    //
    // if (this.usePrviateMonitor == true && this.privateMonitor != null) {
    // this.privateMonitor.increValue(key);
    // }
    // else {
    // ComponentMonitor cm = (ComponentMonitor) this.getConfigManager().getComponent("UAV.Global.Monitor");
    //
    // cm.increValue(key);
    // }
    // }
    //
    // /**
    // * sum monitor value
    // *
    // * @param key
    // * @param addValue
    // */
    // public void sumMonitorValue(String key, long addValue) {
    //
    // if (this.usePrviateMonitor == true && this.privateMonitor != null) {
    // this.privateMonitor.sumValue(key, addValue);
    // }
    // else {
    // ComponentMonitor cm = (ComponentMonitor) this.getConfigManager().getComponent("UAV.Global.Monitor");
    //
    // cm.sumValue(key, addValue);
    // }
    // }
    //
    // /**
    // * set monitor value
    // *
    // * @param key
    // * @param addValue
    // */
    // public void setMonitorValue(String key, long addValue) {
    //
    // if (this.usePrviateMonitor == true && this.privateMonitor != null) {
    // this.privateMonitor.setValue(key, addValue);
    // }
    // else {
    // ComponentMonitor cm = (ComponentMonitor) this.getConfigManager().getComponent("UAV.Global.Monitor");
    //
    // cm.setValue(key, addValue);
    // }
    // }
    //
    // /**
    // * 设置某个计数器的统计周期
    // *
    // * @param key
    // * @param tu
    // * 统计多少秒内的值，过后回0
    // */
    // public void setValueSumBySeconds(String key, Long tu) {
    //
    // if (this.usePrviateMonitor == true && this.privateMonitor != null) {
    // this.privateMonitor.setValueSumBySeconds(key, tu);
    // }
    // else {
    // ComponentMonitor cm = (ComponentMonitor) this.getConfigManager().getComponent("UAV.Global.Monitor");
    //
    // cm.setValueSumBySeconds(key, tu);
    // }
    // }
    //
    // /**
    // * 设置某个计数器的统计周期
    // *
    // * @param key
    // * @param tu
    // * 统计多少秒内的值，过后回0
    // * @param lastRecordNumber
    // * 保存过去记录的个数
    // *
    // */
    // public void setValueSumBySeconds(String key, Long tu, int lastRecordNumber) {
    //
    // if (this.usePrviateMonitor == true) {
    // this.privateMonitor.setValueSumBySeconds(key, tu, lastRecordNumber);
    // }
    // else {
    // ComponentMonitor cm = (ComponentMonitor) this.getConfigManager().getComponent("UAV.Global.Monitor");
    //
    // cm.setValueSumBySeconds(key, tu, lastRecordNumber);
    // }
    // }
    //
    // /**
    // * 清除某个计数器的统计周期
    // *
    // * @param key
    // */
    // public void unsetValueSumBySeconds(String key) {
    //
    // if (this.usePrviateMonitor == true) {
    // this.privateMonitor.unsetValueSumBySeconds(key);
    // }
    // else {
    // ComponentMonitor cm = (ComponentMonitor) this.getConfigManager().getComponent("UAV.Global.Monitor");
    //
    // cm.unsetValueSumBySeconds(key);
    // }
    // }

    /**
     * put NotificationEvent
     * 
     * @param event
     */
    public void putNotificationEvent(NotificationEvent event) {

        String nodeUUID = this.getConfigManager().getContext(IConfigurationManager.NODEUUID);
        String nodeName = this.getConfigManager().getContext(IConfigurationManager.NODETYPE);

        if (null != nodeUUID) {
            event.addArg("nodeuuid", nodeUUID);
        }

        if (null != nodeName) {
            event.addArg("nodename", nodeName);
        }

        event.addArg("component", cName);
        event.addArg("feature", feature);

        if (!event.getArgs(false).containsKey("appgroup")) {
            event.addArg("appgroup", this.getConfigManager().getContext(IConfigurationManager.NODEGROUP));
        }

        AgentFeatureComponent afc = (AgentFeatureComponent) getConfigManager().getComponent("notifyagent",
                "GlobalNotificationAgent");
        if (null != afc) {
            afc.exchange("global.notify", event);
        }
    }

    public String getName() {

        return cName;
    }

    public String getFeature() {

        return feature;
    }

    /**
     * this method is helping to refresh the configuration for each AbstractComponent at runtime
     * 
     * @param updatedConfig
     */
    public void onConfigUpdate(Properties updatedConfig) {

        // do nothing
    }
}
