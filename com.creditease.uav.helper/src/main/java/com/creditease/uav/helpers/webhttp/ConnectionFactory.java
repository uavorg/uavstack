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

package com.creditease.uav.helpers.webhttp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * HttpURLConnection工厂
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.0
 */
public interface ConnectionFactory {

    /**
     * 根据URL创建一个HttpURLConnection
     *
     * @throws IOException
     */
    HttpURLConnection create(URL url) throws IOException;

    /**
     * 根据URL和代理对象创建一个HttpURLConnection
     *
     * @throws IOException
     */
    HttpURLConnection create(URL url, Proxy proxy) throws IOException;

    /**
     * 一个默认的连接工厂
     */
    ConnectionFactory DEFAULT = new ConnectionFactory() {

        @Override
        public HttpURLConnection create(URL url) throws IOException {

            return (HttpURLConnection) url.openConnection();
        }

        @Override
        public HttpURLConnection create(URL url, Proxy proxy) throws IOException {

            return (HttpURLConnection) url.openConnection(proxy);
        }
    };
}
