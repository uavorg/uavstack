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

package com.creditease.agent.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.log.api.IPLogger;

public class PLogger implements IPLogger {

    public static class DefaultLogFormatter extends Formatter {

        private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");

        @Override
        public String format(LogRecord record) {

            return "[" + dateFormatter.format(new Date()) + "]	" + record.getThreadID() + " "
                    + getLogLevelStr(record.getLevel()) + "	" + record.getMessage() + JVMToolHelper.getLineSeperator();
        }
    }

    public static class SimpleLogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {

            return record.getMessage() + JVMToolHelper.getLineSeperator();

        }
    }

    private static String getLogLevelStr(Level level) {

        if (level == Level.INFO) {
            return "I ";
        }
        else if (level == Level.SEVERE) {
            return "E ";
        }
        else if (level == Level.WARNING) {
            return "W ";
        }
        else if (level == Level.FINEST) {
            return "D ";
        }
        else if (level == Level.FINE) {
            return "F ";
        }
        else if (level == Level.FINER) {
            return "FR";
        }
        else {
            return "I ";
        }
    }

    private static Level getLevel(LogLevel level) {

        Level l = Level.INFO;
        switch (level) {
            case ALL:
                l = Level.ALL;
                break;
            case DEBUG:
                l = Level.FINEST;
                break;
            case ERR:
                l = Level.SEVERE;
                break;
            case FINE:
                l = Level.FINE;
                break;
            case FINER:
                l = Level.FINER;
                break;
            case INFO:
                l = Level.INFO;
                break;
            case WARNING:
                l = Level.WARNING;
                break;
            default:
                l = Level.INFO;
                break;
        }
        return l;
    }

    private Logger log = null;
    private ConsoleHandler consoleHandler = null;
    private FileHandler fileHandler = null;
    private MemoryHandler memHandler = null;
    private Level level = Level.INFO;
    private boolean isEnableFileOutSus = false;
    private boolean isEnableConsoleOutSus = false;
	
    public PLogger(String name) {
		
        log = Logger.getLogger(name);
        log.setUseParentHandlers(false);
    }

    @Override
    public void setLogLevel(LogLevel level) {

        Level l = getLevel(level);
        log.setLevel(l);
        this.level = l;
        if (this.consoleHandler != null) {
            this.consoleHandler.setLevel(l);
        }
        if (this.memHandler != null) {
            this.memHandler.setLevel(l);
        }
        if (this.fileHandler != null) {
            this.fileHandler.setLevel(l);
        }
    }

    @Override
    public void log(LogLevel level, String info, Object... objects) {

        /**
         * NOTE: only when enable is OK to record the log
         */
        if (isEnableFileOutSus == false && isEnableConsoleOutSus == false) {
            return;
        }
		
        Level l = getLevel(level);
		
        log.log(l, info, objects);
    }

    @Override
    public void info(String info, Object... objects) {

        log(LogLevel.INFO, info, objects);
    }

    @Override
    public void warn(String info, Object... objects) {

        log(LogLevel.WARNING, info, objects);
    }

    @Override
    public void err(String info, Object... objects) {

        log(LogLevel.ERR, info, objects);
    }

    @Override
    public void debug(String info, Object... objects) {

        log(LogLevel.DEBUG, info, objects);
    }

    @Override
    public void fine(String info, Object... objects) {

        log(LogLevel.FINE, info, objects);
    }

    @Override
    public void finer(String info, Object... objects) {

        log(LogLevel.FINER, info, objects);
    }

    @Override
    public boolean enableConsoleOut(boolean check) {

        if (check == true) {
            if (this.consoleHandler == null) {
                this.consoleHandler = new ConsoleHandler();
                this.consoleHandler.setLevel(this.level);
                this.consoleHandler.setFormatter(new DefaultLogFormatter());
            }
            log.addHandler(this.consoleHandler);
			isEnableConsoleOutSus = true;
        }
        else {
            if (this.consoleHandler != null) {
                log.removeHandler(this.consoleHandler);
            }
			isEnableConsoleOutSus = false;
        }
		
		return isEnableConsoleOutSus;
    }

    @Override
    public boolean enableFileOut(String filepattern, boolean check, int bufferSize, int fileSize, int fileCount,
            boolean isAppend, Formatter format) {

        if (check == true) {

            if (this.fileHandler == null) {
                initFileHandler(filepattern, fileSize, fileCount, isAppend);
            }

            if (this.fileHandler != null) {
                this.fileHandler.setLevel(this.level);
                this.fileHandler.setFormatter(format);
            }

            /**
             * NOTE: we use async log buffer
             */
            if (this.memHandler == null&& this.fileHandler != null) {
                this.memHandler = new MemoryHandler(this.fileHandler, bufferSize, this.level);
                this.log.addHandler(this.memHandler);
				isEnableFileOutSus = true;
            }
        }
        else {
            if (this.memHandler != null) {
                log.removeHandler(this.memHandler);
				isEnableFileOutSus = false;
            }
        }
		
		return isEnableFileOutSus;
    }

    /**
     * @param filepattern
     * @param fileSize
     * @param fileCount
     * @param isAppend
     */
    private void initFileHandler(String filepattern, int fileSize, int fileCount, boolean isAppend) {

        try {
            if (fileSize > 0 && fileCount > 0) {

                if (isAppend == false) {

                    this.fileHandler = new FileHandler(filepattern, fileSize, fileCount);

                }
                else {

                    this.fileHandler = new FileHandler(filepattern, fileSize, fileCount, isAppend);

                }

            }
            else {
                this.fileHandler = new FileHandler(filepattern);
            }

        }
        catch (SecurityException e) {
            // ignore
        }
        catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void destroy() {

        if (this.consoleHandler != null) {
            this.consoleHandler.flush();
            this.consoleHandler.close();
        }

        if (this.memHandler != null) {
            this.memHandler.flush();
            this.memHandler.close();
        }

        if (this.fileHandler != null) {
            this.fileHandler.flush();
            this.fileHandler.close();
        }
    }

}
