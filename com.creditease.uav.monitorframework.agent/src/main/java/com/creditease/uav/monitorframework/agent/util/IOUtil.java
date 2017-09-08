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

package com.creditease.uav.monitorframework.agent.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class IOUtil {

    /**
     * 
     * @param path
     * @param encoding
     * @return
     */
    public static String readTxtFile(String path, String encoding) {

        try {
            if (path == null || "".equals(path)) {
                return null;
            }

            FileInputStream fstream = new FileInputStream(path);
            StringBuilder source = new StringBuilder();

            getStringFromStream(encoding, fstream, source);

            return source.toString();

        }
        catch (Exception ee) {
            // ignore
        }

        return null;
    }

    /**
     * @param encoding
     * @param fstream
     * @param source
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static void getStringFromStream(String encoding, FileInputStream fstream, StringBuilder source)
            throws IOException, UnsupportedEncodingException {

        try {

            byte[] buffer = new byte[4096];
            int readct = -1;
            while ((readct = fstream.read(buffer)) > 0) {
                if (encoding != null) {
                    source.append(new String(buffer, 0, readct, encoding));
                }
                else {
                    source.append(new String(buffer, 0, readct));
                }
            }
        }
        finally {
            fstream.close();
        }
    }
}
