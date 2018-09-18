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

package com.creditease.monitorframework.fat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class Log4j2Test extends HttpServlet { 

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {

        super.init();
    }

    @Override
    public void destroy() {
        
        super.destroy();
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<Logger> loggers = new ArrayList<Logger>(); 
        loggers.add(LogManager.getLogger(LogManager.ROOT_LOGGER_NAME));
        loggers.add(LogManager.getLogger("Rolling"));
        loggers.add(LogManager.getLogger("RollingRandomAccess"));
        loggers.add(LogManager.getLogger("RandomAccess"));
        loggers.add(LogManager.getLogger("MemoryMapped"));
        loggers.add(LogManager.getLogger("Async"));
        
        for(int i = 0; i < loggers.size(); i++) {
            Logger log = loggers.get(i);
            log.trace("trace level");
            log.debug("debug level");
            log.info("info level");
            log.error("error level");
            log.fatal("fatal level");
        }
    }
}