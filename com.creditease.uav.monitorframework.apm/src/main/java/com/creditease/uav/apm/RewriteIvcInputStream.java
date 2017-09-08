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

package com.creditease.uav.apm;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletInputStream;

import com.creditease.agent.helpers.StringHelper;

public class RewriteIvcInputStream extends ServletInputStream {

    private ServletInputStream inputStream;

    private StringBuilder builder = new StringBuilder();

    private AtomicBoolean isReaded = new AtomicBoolean();

    private String encoding;

    public RewriteIvcInputStream(ServletInputStream inputStream, String encoding) {
        this.inputStream = inputStream;
        this.encoding = StringHelper.isEmpty(encoding) ? "utf-8" : encoding;
        if ("iso-8859-1".equalsIgnoreCase(encoding)) {
            this.encoding = "utf-8";
        }
    }

    public ServletInputStream getDefaultServletInputStream() {

        return this.inputStream;
    }

    @Override
    public int read() throws IOException {

        return this.inputStream.read();
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {

        return inputStream.readLine(b, off, len);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        int result = super.read(b, off, len);
        if (!this.isReaded.get()) {
            if (result == -1) {
                this.isReaded.set(true);
            }
            else {
                builder.append(new String(b, encoding));
            }
        }
        return result;
    }

    public StringBuilder getContent() {

        return this.builder;
    }

    @Override
    public synchronized void reset() throws IOException {

        super.reset();
        this.isReaded.set(true);
    }

}
