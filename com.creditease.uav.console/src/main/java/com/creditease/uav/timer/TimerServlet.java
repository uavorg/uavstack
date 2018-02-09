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
package com.creditease.uav.timer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;

/**
 * TimerServlet description: apphub的任务调度
 */
public class TimerServlet extends HttpServlet {

    private static final long serialVersionUID = 8252474547216672145L;
    private ISystemLogger logger = null;
    private Timer timer = null;

    @Override
    public void init() {

        if (null == logger) {
            logger = SystemLogger.getLogger(TimerServlet.class);
        }

        if (null == timer) {
            timer = new Timer();
        }

        timer.schedule(new LoggerPointTask(), 0, 1000 * 60);// 0秒后开始， 每次间隔1分钟

    }

    @Override
    public void destroy() {

        super.destroy();
        if (null != timer) {
            timer.cancel();
        }

    }

    private class LoggerPointTask extends TimerTask {

        @Override
        public void run() {

            SimpleDateFormat format = new SimpleDateFormat("mm");
            int minute = Integer.parseInt(format.format(new Date()));
            int diff = minute % 5;
            if (0 == diff) {
                /**
                 * 当前分钟为5,10,15,20....55,60才会计数：1小时只有12条log打印
                 */
                Map<String, String> info = new HashMap<String, String>();
                info.put("key", "apphubLogPoint");
                info.put("localIp", NetworkHelper.getLocalIP());
                logger.info(this, JSONHelper.toString(info));

            }
        }
    }
}
