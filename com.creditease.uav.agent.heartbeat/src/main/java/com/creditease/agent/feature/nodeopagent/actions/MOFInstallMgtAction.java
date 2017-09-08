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

import java.util.LinkedList;
import java.util.List;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;

/**
 * 
 * MOFInstallMgtAction description: MOFInstallMgtAction
 *
 */
public class MOFInstallMgtAction extends AbstractBaseAction {

    private String shellParentPath;

    private String isMOFInstallTagFile;

    private String agentArgs;

    public MOFInstallMgtAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);

        shellParentPath = ConfigurationManager.getInstance().getContext(IConfigurationManager.METADATAPATH)
                + "nodeOper";

        isMOFInstallTagFile = ConfigurationManager.getInstance().getContext(IConfigurationManager.METADATAPATH)
                + "isMOFInstall";

        String root = ConfigurationManager.getInstance().getContext(IConfigurationManager.ROOT);

        if (JVMToolHelper.isWindows()) {
            agentArgs = "-javaagent:" + root.substring(0, root.lastIndexOf("\\"))
                    + "\\uavmof\\com.creditease.uav.agent\\com.creditease.uav.monitorframework.agent-1.0-agent.jar";
        }
        else {
            agentArgs = "-javaagent:" + root.substring(0, root.lastIndexOf("/"))
                    + "/uavmof/com.creditease.uav.agent/com.creditease.uav.monitorframework.agent-1.0-agent.jar";
        }
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        UAVHttpMessage msg = (UAVHttpMessage) context.getParam("msg");

        if (JVMToolHelper.isWindows()) {
            return;
        }

        switch (msg.getIntent()) {
            case "installmof":
                this.installUAVMOF(msg, isMOFInstallTagFile, shellParentPath, agentArgs);
                break;
            case "uninstallmof":
                this.uninstallUAVMOF(msg, isMOFInstallTagFile, shellParentPath, agentArgs);
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
     * installUAVMOF
     * 
     * @param data
     * @param agentArgs
     * @param shellParentPath
     * @param isMOFInstallTagFile
     */
    private void installUAVMOF(UAVHttpMessage data, String isMOFInstallTagFile, String shellParentPath,
            String agentArgs) {

        try {
            // step 1, write a tag file
            if (IOHelper.exists(isMOFInstallTagFile)) {
                data.putResponse("rs", "OK");
                return;
            }
            else {
                IOHelper.write("installed", isMOFInstallTagFile);
            }

            // step 2, do special things for each webContainer
            String container = data.getRequest("container");

            switch (container) {
                case "tomcat":
                    installMOFforTomcat(shellParentPath, agentArgs);
                    break;
                default:
                    break;
            }
            data.putResponse("rs", "OK");
        }
        catch (Exception e) {
            log.err(this, "Install UAVMOF FAIL", e);
            data.putResponse("rs", "ERR");
        }
    }

    /**
     * uninstallUAVMOF
     * 
     * @param data
     * @param agentArgs
     * @param isMOFInstallTagFile
     * @param shellParentPath
     */
    private void uninstallUAVMOF(UAVHttpMessage data, String isMOFInstallTagFile, String shellParentPath,
            String agentArgs) {

        try {
            // step 1, delete the tag file
            if (IOHelper.exists(isMOFInstallTagFile)) {
                IOHelper.deleteFile(isMOFInstallTagFile);
            }
            else {
                data.putResponse("rs", "OK");
                return;
            }
            // step 2, recover these special things for each webContainer
            String container = data.getRequest("container");

            switch (container) {
                case "tomcat":
                    uninstallMOFforTomcat(shellParentPath, agentArgs);
                    break;
                default:
                    break;
            }
            data.putResponse("rs", "OK");
        }
        catch (Exception e) {
            log.err(this, "UNInstall UAVMOF FAIL", e);
            data.putResponse("rs", "ERR");
        }
    }

    private void installMOFforTomcat(String shellParentPath, String agentArgs) throws Exception {

        String userHome = (RuntimeHelper.exeShell("echo $HOME", shellParentPath)).split("\n")[0];

        String user = userHome.split("/")[userHome.split("/").length - 1];

        List<String> filePaths = getRcFilePaths(user);

        for (String filePath : filePaths) {

            String content = IOHelper.readTxtFile(filePath, "UTF-8");

            String[] lines = content.split("\n");

            for (String line : lines) {
                if (line.indexOf(agentArgs) > -1) {
                    return;
                }
            }

            String appendString = "export CATALINA_OPTS=\"$CATALINA_OPTS " + agentArgs + "\"";

            IOHelper.writeTxtFile(filePath, appendString, "UTF-8", true);
        }
    }

    private void uninstallMOFforTomcat(String shellParentPath, String agentArgs) throws Exception {

        String userHome = (RuntimeHelper.exeShell("echo $HOME", shellParentPath)).split("\n")[0];

        String user = userHome.split("/")[userHome.split("/").length - 1];

        List<String> filePaths = getRcFilePaths(user);

        for (String filePath : filePaths) {

            String content = IOHelper.readTxtFile(filePath, "UTF-8");

            String[] lines = content.split("\n");

            StringBuffer sb = new StringBuffer();

            boolean isInstalled = false;

            for (String line : lines) {
                if (line.indexOf(agentArgs) > -1) {
                    isInstalled = true;
                    continue;
                }
                sb.append(line + "\n");
            }

            if (isInstalled) {
                IOHelper.writeTxtFile(filePath, sb.toString(), "UTF-8", false);
            }
        }
    }

    private List<String> getRcFilePaths(String user) {

        List<String> filePaths = new LinkedList<String>();

        String content = IOHelper.readTxtFile("/etc/passwd", "UTF-8");

        String[] lines = content.split("\n");

        String userAuth = null;
        /**
         * schema <br>
         * root:x:0:0:root:/root:/bin/bash <br>
         * yxgly:x:0:0::/home/yxgly:/bin/bash
         */
        for (String line : lines) {

            String[] args = line.split(":");

            if (args[0].equals(user)) {
                userAuth = ":" + args[2] + ":" + args[3] + ":";
                break;
            }

        }

        if (userAuth == null) {
            return filePaths;
        }

        for (String line : lines) {

            if (line.indexOf(userAuth) > -1) {
                String[] args = line.split(":");
                filePaths.add(args[5] + "/.bashrc");

            }
        }

        return filePaths;
    }

}
