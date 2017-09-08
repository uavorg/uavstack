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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class PropertiesHelper {

    /**
     * save property file
     * 
     * @param p
     * @param filePath
     * @return
     * @throws IOException
     */
    public static boolean savePropertyFile(Properties p, String filePath) throws IOException {

        if (null == p || StringHelper.isEmpty(filePath)) {
            return false;
        }

        File f = new File(filePath);

        OutputStream os = null;
        try {
            os = new FileOutputStream(f);

            p.store(os, "");

            return true;
        }
        finally {

            if (os != null) {
                os.close();
            }
        }

    }

    /**
     * load property file into properties
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public static Properties loadPropertyFile(String filePath) throws IOException {

        Properties pro = new Properties();

        if (StringHelper.isEmpty(filePath)) {
            return pro;
        }

        InputStream fip = null;
        try {
            String cfgFilePath = filePath;

            File cfgFile = new File(cfgFilePath);

            if (cfgFile.exists()) {
                fip = new FileInputStream(cfgFile);
                pro.load(fip);
            }

        }
        catch (IOException e) {
            throw e;
        }
        finally {
            if (fip != null) {
                try {
                    fip.close();
                }
                catch (IOException e) {
                    throw e;
                }
            }
        }
        return pro;
    }

    /**
     * 下载Properties文件
     * 
     * @param urlString
     * @return
     * @throws IOException
     */
    public static Properties downloadProperties(String urlString) throws IOException {

        Properties properties = new Properties();
        InputStream is = null;
        InputStreamReader inputStreamReader = null;
        try {
            URL url = new URL(urlString);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(30 * 1000);
            con.setRequestProperty("Charset", "UTF-8");
            is = con.getInputStream();
            inputStreamReader = new InputStreamReader(is, "UTF-8");
            properties.load(inputStreamReader);
        }
        finally {
            // 完毕，关闭所有链接
            try {
                if (null != inputStreamReader) {
                    inputStreamReader.close();
                }
                if (null != is) {
                    is.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return properties;
    }

}
