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

package com.creditease.agent.helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

public class JarHelper {

    /**
     * 提取资源为Properties
     * 
     * @param jarFilePath
     * @param resourcePath
     * @return
     */
    public static Properties getResourceAsProperties(String jarFilePath, String resourcePath) {

        InputStream is = getResourceAsStream(jarFilePath, resourcePath);

        Properties props = new Properties();
        try {
            props.load(is);
            return props;
        }
        catch (IOException e) {
            // ignore
        }
        finally {

            if (null != is) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }

        return null;
    }

    /**
     * 提取资源为InputStream
     * 
     * @param jarFilePath
     * @param resourcePath
     * @return
     */
    public static InputStream getResourceAsStream(String jarFilePath, String resourcePath) {

        File file = new File(jarFilePath);

        if (!file.exists()) {
            return null;
        }

        try {
            @SuppressWarnings("resource")
            URLClassLoader cl = new URLClassLoader(new URL[] { file.toURI().toURL() });

            InputStream is = cl.getResourceAsStream(resourcePath);

            return is;

        }
        catch (Exception e) {
            // ignore
        }

        return null;
    }
}
