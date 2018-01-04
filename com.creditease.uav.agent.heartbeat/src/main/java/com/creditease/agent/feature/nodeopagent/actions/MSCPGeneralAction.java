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

package com.creditease.agent.feature.nodeopagent.actions;

import java.nio.file.Paths;
import java.util.Map;

import com.creditease.agent.SystemStarter;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.OSProcessHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;

public class MSCPGeneralAction extends AbstractBaseAction {

    public MSCPGeneralAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        UAVHttpMessage data = (UAVHttpMessage) context.getParam("msg");

        switch (data.getIntent()) {

            case "fstart":
                this.startFeature(data);
                break;
            case "fstop":
                this.stopFeature(data);
                break;
            case "killproc":
                this.killProcess(data);
                break;
            case "kill":
                break;
            case "shutdown":
                this.shutdown();
                break;
            case "chgsyspro":
                this.changeSystemProperties(data);
                break;
            case "loadnodepro":
                this.LoadNodeConfig(data);
                break;
            case "chgnodepro":
                this.changeNodeConfig(data);
                break;
            case "watch":
                this.watchProc(data);
                break;
            case "unwatch":
                this.unwatchProc(data);
                break;
            case "upgrade":
                this.upgrade(data);
                break;
            case "restart":
                this.restartProc(data);
                break;
            case "stopuav":
                this.stopUAV(data);
                break;
        }
    }

    @Override
    public String getSuccessNextActionId() {

        return null;
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

    /**
     * stopFeature
     * 
     * @param data
     */
    private void stopFeature(UAVHttpMessage data) {

        String feature = data.getRequest("feature");

        SystemStarter ss = this.getSystemStarter();

        if (ss.uninstallFeature(feature)) {
            data.putResponse("rs", "OK");
        }
        else {
            data.putResponse("rs", "ERR");
        }
    }

    /**
     * startFeature
     * 
     * @param data
     */
    private void startFeature(UAVHttpMessage data) {

        String feature = data.getRequest("feature");

        SystemStarter ss = this.getSystemStarter();

        if (ss.uninstallFeature(feature) == false) {
            data.putResponse("rs", "ERR");
            return;
        }

        if (ss.installFeature(feature)) {
            data.putResponse("rs", "OK");
        }
        else {
            data.putResponse("rs", "ERR");
        }
    }

    /**
     * kill process
     * 
     * @param data
     */
    private void killProcess(final UAVHttpMessage data) {

        final String pid = data.getRequest("pid");

        Thread thd = new Thread(new Runnable() {

            @Override
            public void run() {

                ThreadHelper.suspend(2000);

                OSProcessHelper.killProcess(pid);
            }

        });

        thd.start();

        data.putResponse("rs", "OK");
    }

    /**
     * changeSystemProperties
     * 
     * @param data
     */
    private void changeSystemProperties(UAVHttpMessage data) {

        Map<String, String> params = data.getRequest();

        this.getConfigManager().fireSystemPropertiesUpdateEvent(params);

        data.putResponse("rs", "OK");
    }

    /**
     * LoadNodeConfig
     * 
     * @param data
     */
    private void LoadNodeConfig(UAVHttpMessage data) {

        String pStr = JSONHelper.toString(this.getConfigManager().getConfigurations());

        data.putResponse("rs", pStr);
    }

    /**
     * changeNodeConfig
     * 
     * @param data
     */
    private void changeNodeConfig(UAVHttpMessage data) {

        final Map<String, String> params = data.getRequest();

        final IConfigurationManager icm = this.getConfigManager();

        Thread thd = new Thread(new Runnable() {

            @Override
            public void run() {

                /**
                 * fire configuration update event
                 */
                icm.fireProfileConfigurationUpdateEvent(params);
            }

        });

        thd.start();

        data.putResponse("rs", "OK");
    }

    /**
     * watchProc
     * 
     * @param data
     */
    private void watchProc(UAVHttpMessage data) {

        String pid = data.getRequest("pid");

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("procwatch",
                "ProcWatchAgent");
        if (afc != null) {
            Object res = afc.exchange("agent.procwatch.watch", pid);

            if (res != null) {
                data.putResponse("rs", String.valueOf(res));
                return;
            }
        }
        data.putResponse("rs", "ERR");
    }

    /**
     * unwatchProc
     * 
     * @param data
     */
    private void unwatchProc(UAVHttpMessage data) {

        String pid = data.getRequest("pid");

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("procwatch",
                "ProcWatchAgent");
        if (afc != null) {
            Object res = afc.exchange("agent.procwatch.unwatch", pid);
            if (res != null) {
                data.putResponse("rs", String.valueOf(res));
                return;
            }
        }

        data.putResponse("rs", "ERR");
    }

    /**
     * Upgrade UAV
     * 
     * @param data
     */
    private void upgrade(UAVHttpMessage data) {

        Map<String, String> upgradeInfo = data.getRequest();
        String rootDir = this.getConfigManager().getContext(IConfigurationManager.ROOT);
        String profileName = this.getConfigManager().getContext(IConfigurationManager.PROFILENAME);
        String upgrade_profile = null;
        if (profileName.contains("pro")) {
            upgrade_profile = "upgrade_pro";
        }
        else if (profileName.contains("test")) {
            upgrade_profile = "upgrade_test";
        }
        else {
            upgrade_profile = "upgrade";
        }

        StringBuffer sbf = new StringBuffer().append("cd ").append(Paths.get(rootDir, "bin").toString());

        if (JVMToolHelper.isWindows()) {
            sbf.append(" && wscript start_upgrade.vbs ");
        }
        else {
            sbf.append(" && sh start_upgrade.sh ");
        }

        sbf.append(upgrade_profile + " ").append(System.getProperty("NetCardIndex", "0")).append(" ");

        if (JVMToolHelper.isWindows()) {
            sbf.append(JSONHelper.toString(upgradeInfo).replace("\"", "'"));
        }
        else {
            sbf.append("\"" + JSONHelper.toString(upgradeInfo).replace("\"", "'") + "\"");
        }

        final String cmd = sbf.toString();

        if (log.isTraceEnable()) {
            log.info(this, "Will execute cmd: " + cmd);
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    if (JVMToolHelper.isWindows()) {
                        RuntimeHelper.exec(5000, "cmd.exe", "/c", cmd);
                    }
                    else {
                        RuntimeHelper.exec(5000, "sh", "-c", cmd);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        thread.start();

        data.putResponse("rs", "OK");
    }

    /**
     * restartProc
     * 
     * @param data
     */
    private void restartProc(UAVHttpMessage data) {

        String pid = data.getRequest("pid");
        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("procwatch",
                "ProcWatchAgent");
        if (afc != null) {
            Object res = afc.exchange("agent.procwatch.restart", pid);
            if (res != null) {
                data.putResponse("rs", String.valueOf(res));
                return;
            }
        }

        data.putResponse("rs", "ERR");
    }

    /**
     * stopUAV
     * 
     * @param data
     */
    private void stopUAV(UAVHttpMessage data) {

        final String pid = data.getRequest("pid");
        final String profile = data.getRequest("profile");

        log.warn(this, "STOP UAV node: pid=" + pid + ", profile=" + profile);

        if (pid == null || profile == null) {
            data.putResponse("rs", "ERR");
            return;
        }

        Thread t = new Thread(new Runnable() {

            /**
             * stopOnWin
             */
            private void stopOnWin() {

                OSProcessHelper.killProcess(pid);
            }

            /**
             * stopOnLinux
             */
            private void stopOnLinux() {

                StringBuilder sb = new StringBuilder();
                sb.append("process_flag=" + profile + "\n");
                sb.append("count=`crontab -l 2>/dev/null | grep \"$process_flag\" | wc -l`" + "\n")
                        .append("if [ $count -ne 0 ]; then" + "\n").append("cronfile=/tmp/$process_flag\".tmp\"" + "\n")
                        .append("crontab -l | grep -v \"$process_flag\" > $cronfile" + "\n")
                        .append("crontab $cronfile" + "\n").append("rm -rf $cronfile" + "\n").append("fi" + "\n");

                sb.append(
                        "runing_watcher=$(ps -ef | grep \"uav_proc_watcher.sh\" | grep \"$process_flag\" | awk '{printf \"%s \",$2}')"
                                + "\n")
                        .append("for pid in $runing_watcher; do" + "\n").append("kill -9 \"$pid\"" + "\n")
                        .append("done" + "\n");

                sb.append("kill -9 " + pid);
                try {
                    RuntimeHelper.exeShell(sb.toString(),
                            getConfigManager().getContext(IConfigurationManager.METADATAPATH));
                }
                catch (Exception e) {
                    log.err(this, "shop uav shell failed.", e);
                }
            }

            @Override
            public void run() {

                ThreadHelper.suspend(1000L);

                if (JVMToolHelper.isWindows()) {
                    stopOnWin();
                }
                else {
                    stopOnLinux();
                }

            }

        });

        t.start();

        data.putResponse("rs", "OK");
    }

    /**
     * shut down
     */
    private void shutdown() {

        SystemStarter ss = this.getSystemStarter();

        try {
            ss.stop();
        }
        catch (Exception e) {
            // ignore
        }

        System.exit(0);
    }
}
