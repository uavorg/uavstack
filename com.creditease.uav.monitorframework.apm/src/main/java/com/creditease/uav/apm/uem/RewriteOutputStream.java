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

package com.creditease.uav.apm.uem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class RewriteOutputStream extends ServletOutputStream {

    ByteArrayOutputStream myOutput = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {

        myOutput.write(b);
    }

    public void pFlush() {

        try {
            myOutput.flush();
        }
        catch (IOException e) {
        }
    }

    public void pClose() {

        try {
            myOutput.close();
        }
        catch (IOException e) {
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

        myOutput.write(b, off, len);
    }

    public ByteArrayOutputStream getOutputStream() {

        return this.myOutput;
    }

}
