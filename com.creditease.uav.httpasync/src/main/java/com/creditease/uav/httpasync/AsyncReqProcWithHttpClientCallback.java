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

package com.creditease.uav.httpasync;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.concurrent.FutureCallback;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import java.io.*;

/**
 * Created by lijunteng on 15/7/21.
 */
public class AsyncReqProcWithHttpClientCallback implements AsyncListener, FutureCallback<HttpResponse> {

    private AsyncContext ac;

    private HttpClientCallback callback;

    private HttpResponse response;

    public AsyncReqProcWithHttpClientCallback(HttpClientCallback callback) {
        this.callback = callback;
    }

    public AsyncReqProcWithHttpClientCallback(AsyncContext ac, HttpClientCallback callback) {
        this.ac = ac;

        this.callback = callback;
    }

    @Override
    public void completed(HttpResponse response) {

        this.response = response;

        if (null != ac) {
            ac.complete();
        }
        else {
            InputStream is = null;
            try {
                is = response.getEntity().getContent();
                HttpClientCallbackResult result = new HttpClientCallbackResult(is, null);
                result.setRetCode(getStatusCode());
                callback.completed(result);
            }
            catch (RuntimeException e) {
                HttpClientCallbackResult result = new HttpClientCallbackResult(null, null);
                HttpAsyncException exp = new HttpAsyncException(HttpAsyncException.ExceptionEvent.COMPLELE_ERROR, e);
                result.setRetCode(getStatusCode());
                result.setException(exp);
                callback.failed(result);
            }
            catch (IOException e) {
                HttpClientCallbackResult result = new HttpClientCallbackResult(null, null);
                HttpAsyncException exp = new HttpAsyncException(HttpAsyncException.ExceptionEvent.COMPLELE_ERROR, e);
                result.setRetCode(getStatusCode());
                result.setException(exp);
                callback.failed(result);
            }
            finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public void failed(Exception ex) {

        try {
            HttpClientCallbackResult result = new HttpClientCallbackResult(null, null);
            HttpAsyncException exp = new HttpAsyncException(HttpAsyncException.ExceptionEvent.REPLY_ERROR, ex);
            result.setRetCode(getStatusCode());
            result.setException(exp);
            callback.failed(result);
        }
        catch (RuntimeException e) {
            // ignore
        }

        if (null != ac) {
            ac.complete();
        }
    }

    @Override
    public void cancelled() {

        if (null != ac) {
            ac.complete();
        }
    }

    @Override
    public void onComplete(AsyncEvent arg0) throws IOException {

        // 返回信息

        OutputStream os = ac.getResponse().getOutputStream();
        InputStream is = response.getEntity().getContent();

        // 调用微信平台的Callback
        HttpClientCallbackResult result = new HttpClientCallbackResult(is, os);
        result.setRetCode(getStatusCode());
        try {
            callback.completed(result);
        }
        catch (RuntimeException e) {
            // ignore
        }
        finally {
            os.flush();
            os.close();
            is.close();
        }
    }

    @Override
    public void onError(AsyncEvent arg0) throws IOException {

        // 也要返回合理的错误信息
        try {
            OutputStream os = ac.getResponse().getOutputStream();
            HttpClientCallbackResult result = new HttpClientCallbackResult(null, os);
            HttpAsyncException exp = new HttpAsyncException(HttpAsyncException.ExceptionEvent.REQASYNC_ERROR,
                    arg0.getThrowable());
            result.setRetCode(getStatusCode());
            result.setException(exp);
            callback.failed(result);
        }
        catch (RuntimeException e) {
            // ignore
        }
    }

    @Override
    public void onStartAsync(AsyncEvent arg0) throws IOException {

        // ignore
    }

    @Override
    public void onTimeout(AsyncEvent arg0) throws IOException {

        // 超时，也要返回合理的错误信息
        try {
            OutputStream os = ac.getResponse().getOutputStream();
            HttpClientCallbackResult result = new HttpClientCallbackResult(null, os);
            HttpAsyncException exp = new HttpAsyncException(HttpAsyncException.ExceptionEvent.REQASYNC_TIMEOUT,
                    arg0.getThrowable());
            result.setRetCode(getStatusCode());
            result.setException(exp);
            callback.failed(result);
        }
        catch (RuntimeException e) {
            // ignore
        }
    }

    protected int getStatusCode() {

        StatusLine sl = null;
        try {
            sl = response.getStatusLine();
        }
        catch (Exception e) {
            // ignore
        }

        if (null == sl) {
            // as BAT REQUEST
            return 400;
        }

        return sl.getStatusCode();
    }

}
