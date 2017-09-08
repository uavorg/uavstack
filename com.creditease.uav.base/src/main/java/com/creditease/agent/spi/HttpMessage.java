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

package com.creditease.agent.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class HttpMessage {

    protected Map<String, List<String>> param;

    public abstract String getMethod();

    public abstract String getHeader(String name);

    public abstract URI getRequestURI();

    public abstract String getClientAddress();

    public abstract int getResponseCode();

    protected abstract void putResponseCodeInfo(int retCode, int payloadLength);

    protected abstract InputStream getRequestBody();

    protected abstract OutputStream getResponseBody();

    public abstract void putResponseBodyInChunkedFile(File file);

    public boolean isKeepAlive() {

        return "keep-alive".equals(getHeader("Connection"));
    }

    public String getContextPath() {

        return getRequestURI().toString().split("\\?")[0];
    }

    /**
     * put response body in String
     * 
     * @param payload
     * @param encoding
     */
    public void putResponseBodyInString(String payload, int retCode, String encoding) {

        OutputStream os = getResponseBody();

        OutputStreamWriter osw = null;
        try {
            putResponseCodeInfo(retCode, payload.getBytes(encoding).length);

            osw = new OutputStreamWriter(os, encoding);

            osw.write(payload);

            osw.flush();

        }
        catch (IOException e) {
            // ignore
        }
        finally {

            if (null != osw) {
                try {
                    osw.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }

            if (null != os) {
                try {
                    os.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * get request body as String
     * 
     * @param encoding
     * @return
     */
    public String getRequestBodyAsString(String encoding) {

        StringBuilder resp = new StringBuilder();
        InputStream currReplyData = getRequestBody();

        String line;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(currReplyData, encoding));
            while ((line = in.readLine()) != null) {
                resp.append(line);
            }
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
        return resp.toString();
    }

    public String getParameter(String name) {

        if (!param.containsKey(name)) {
            return null;
        }

        List<String> ls = param.get(name);

        if (ls == null) {
            return null;
        }

        return ls.get(0);
    }

    public String[] getParameterValues(String name) {

        if (!param.containsKey(name)) {
            return null;
        }

        List<String> ls = param.get(name);

        String[] vals = new String[ls.size()];
        ls.toArray(vals);

        return vals;
    }

    // parse decoded query String
    protected Map<String, List<String>> parseQueryString(String s) {

        Map<String, List<String>> map = new HashMap<String, List<String>>();
        if (s == null) {
            return map;
        }

        List<String> vals = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                throw new IllegalArgumentException();
            }
            String key = pair.substring(0, pos);
            String val = pair.substring(pos + 1, pair.length());
            if (map.containsKey(key)) {
                List<String> oldVals = map.get(key);
                // vals = new String[oldVals.length + 1];
                for (int i = 0; i < oldVals.size(); i++) {
                    vals.add(oldVals.get(i));
                }
                vals.add(val);
            }
            else {
                vals.add(val);
            }
            map.put(key, vals);
        }
        return map;
    }
}
