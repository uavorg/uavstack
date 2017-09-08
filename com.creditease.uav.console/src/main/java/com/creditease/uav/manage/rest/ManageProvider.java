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

package com.creditease.uav.manage.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.apphub.core.AppHubBaseComponent;

/**
 * jax-rs api ：对管理restful资源接口做输入流转译（JSON转对象） Created by lbay on 2016/4/18.
 */
@SuppressWarnings("rawtypes")
@Provider
public class ManageProvider extends AppHubBaseComponent implements MessageBodyReader {

    @Override
    public boolean isReadable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {

        /**
         * 注册需要映射的包 readFrom：将自动解析填充包下class
         */
        String[] providerPages = new String[] { "com.creditease.uav.manage.rest.entity" };
        boolean isReadable = false;
        for (String s : providerPages) {
            if (type.toString().contains(s)) {
                isReadable = true;
                break;
            }
        }

        return isReadable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {

        Object jsonObj = null;
        try {
            StringBuilder paramStr = new StringBuilder();
            int length = 1024;
            byte[] b = new byte[length];
            int index = 0;
            while ((index = inputStream.read(b)) > 0) {
                paramStr.append(new String(b, 0, index, "UTF-8"));
            }
            jsonObj = JSONHelper.toObject(paramStr.toString(), aClass);
        }
        catch (Exception e) {
            logger.err(this, "ManageProvider readFrom error:" + e);
        }
        finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
            catch (Exception e2) {
                logger.err(this, "ManageProvider readFrom finally error:" + e2);
            }
        }
        return jsonObj;
    }
}
