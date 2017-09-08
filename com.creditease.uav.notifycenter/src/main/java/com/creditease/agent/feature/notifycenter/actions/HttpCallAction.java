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

package com.creditease.agent.feature.notifycenter.actions;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class HttpCallAction extends BaseNotifyAction {

    private HttpAsyncClient client = null;

    public HttpCallAction(String cName, String feature, IActionEngine engine) {

        super(cName, feature, engine);

        client = HttpAsyncClientFactory.build(50, 500, 30000, 30000, 30000);
    }

    @Override
    public boolean run(NotificationEvent event) {

        final AtomicBoolean bool = new AtomicBoolean(true);

        final String urlstr = event.getArg(cName);

        String[] urls = urlstr.split(",");

        final CountDownLatch cdl = new CountDownLatch(urls.length);

        for (final String url : urls) {

            try {

                final String eventStr = event.toJSONString();
                if (this.log.isDebugEnable()) {
                    this.log.debug(this, "HttpCallAction START:url=" + url + ",event=" + eventStr);
                }

                client.doAsyncHttpPost(url, eventStr.getBytes("utf-8"), "application/json", "utf-8",
                        new HttpClientCallback() {

                            @Override
                            public void completed(HttpClientCallbackResult result) {

                                bool.set(true & bool.get());
                                cdl.countDown();

                                if (log.isDebugEnable()) {
                                    log.debug(this, "HttpCallAction END:url=" + url + ",event=" + eventStr);
                                }
                            }

                            @Override
                            public void failed(HttpClientCallbackResult result) {

                                bool.set(false & bool.get());
                                cdl.countDown();

                                if (log.isTraceEnable()) {
                                    log.err(this, "HttpCallAction FAIL:retCode=" + result.getRetCode() + ",url=" + url
                                            + ",event=" + eventStr);
                                }
                            }

                        });
            }
            catch (UnsupportedEncodingException e) {
                // ignore
            }
        }

        try {
            cdl.await(3000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            // ignore
        }

        return bool.get();
    }

    @Override
    public void destroy() {

        client.shutdown();
    }
}
