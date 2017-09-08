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

package com.creditease.uav.feature.upgrade.beans;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;

/**
 * 
 * UpgradeProcessLock description: Implement a process lock basing on file lock.
 *
 */
public class UpgradeProcessLock {

    protected ISystemLogger log = SystemLogger.getLogger(UpgradeProcessLock.class);

    private FileLock fileLock;

    private Path filePath;

    public UpgradeProcessLock(Path filePath) {
        this.filePath = filePath;
    }

    public boolean getFileLock() {

        boolean result = true;
        try {
            FileChannel fc = FileChannel.open(this.filePath, StandardOpenOption.READ, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE);
            fileLock = fc.tryLock();

            if (fileLock == null) {
                result = false;
            }
        }
        catch (Exception ex) {
            result = false;
        }

        return result;
    }

    public void releaseFileLock() {

        if (fileLock != null) {
            if (log.isTraceEnable()) {
                log.info(this, "Releasing the file lock of " + this.filePath.getFileName());
            }

            Channel fc = fileLock.acquiredBy();

            try {
                fileLock.release();
                fileLock = null;

                if (fc != null) {
                    fc.close();
                }
            }
            catch (IOException e) {
            }
        }
    }
}
