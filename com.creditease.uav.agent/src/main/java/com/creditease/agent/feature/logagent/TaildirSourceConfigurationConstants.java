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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.creditease.agent.feature.logagent;

public class TaildirSourceConfigurationConstants {

    public static final String BACKOFF_SLEEP_INCREMENT = "backoffSleepIncrement";
    public static final String MAX_BACKOFF_SLEEP = "maxBackoffSleep";
    public static final long DEFAULT_BACKOFF_SLEEP_INCREMENT = 1000;
    public static final long DEFAULT_MAX_BACKOFF_SLEEP = 5000;
    // ---------------------------------------------------------------------
    /** Mapping for tailing file groups. */
    // public static final String FILE_GROUPS = "filegroups";
    // public static final String FILE_GROUPS_PREFIX = FILE_GROUPS + ".";

    /** Mapping for putting headers to events grouped by file groups. */
    public static final String HEADERS_PREFIX = "headers.";

    /** Path of position file. */
    public static final String POSITION_FILE_ROOT = "positionfileroot";
    public static final String DEFAULT_POSITION_FILE = "/uav.log.posfile.json";

    /** What size to batch with before sending to ChannelProcessor. */
    public static final String BATCH_SIZE = "batchSize";
    public static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * Whether to skip the position to EOF in the case of files not written on the position file.
     */
    public static final String SKIP_TO_END = "skipToEnd";
    public static final boolean DEFAULT_SKIP_TO_END = false;

    /** Time (ms) to close idle files. */
    public static final String IDLE_TIMEOUT = "idleTimeout";
    public static final int DEFAULT_IDLE_TIMEOUT = 120000;

    /**
     * Interval time (ms) to write the last position of each file on the position file.
     */
    public static final String WRITE_POS_INTERVAL = "writePosInterval";
    public static final int DEFAULT_WRITE_POS_INTERVAL = 3000;

    /** Whether to add the byte offset of a tailed line to the header */
    public static final String BYTE_OFFSET_HEADER = "byteOffsetHeader";
    public static final String BYTE_OFFSET_HEADER_KEY = "byteoffset";
    public static final boolean DEFAULT_BYTE_OFFSET_HEADER = false;

    /** add line number and timestamp in event */
    public static final String READ_LINE_NUMBER = "line.number";
    public static final String READ_TIMESTAMP = "read.timestamp";

    public static final String MONITOR_SERVER_ID = "serverid";

    public static final String OS_WINDOWS = "Windows";

    public static final String OS_MAC = "Mac";
}
