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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.creditease.agent.helpers.DataConvertHelper;

public class HttpClientCallbackResult {

    InputStream replyData;

    OutputStream responseForRequestAsync;

    HttpAsyncException exception;

    int retCode;

    public HttpClientCallbackResult(InputStream replyData, OutputStream responseForRequestAsync) {
        this.replyData = replyData;
        this.responseForRequestAsync = responseForRequestAsync;
    }

    public InputStream getReplyData() {

        return replyData;
    }

    public byte[] getResulDataAsByteArray() {

        if (this.replyData == null) {
            return null;
        }

        byte[] data = DataConvertHelper.inputStream2byte(this.replyData);

        return data;
    }

    public String getReplyDataAsString() {

        if (this.replyData == null) {
            return "";
        }

        StringBuilder resp = new StringBuilder();
        InputStream currReplyData = this.replyData;

        String line;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(currReplyData, "UTF-8"));
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

    public OutputStream getResponseForRequestAsync() {

        return responseForRequestAsync;
    }

    public HttpAsyncException getException() {

        return exception;
    }

    public void setException(HttpAsyncException throwe) {

        this.exception = throwe;
    }

    public int getRetCode() {

        return retCode;
    }

    public void setRetCode(int retCode) {

        this.retCode = retCode;
    }

}
