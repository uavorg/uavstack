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

package com.creditease.uav.apphub.rest;

import javax.ws.rs.Path;

import com.creditease.uav.apphub.core.AppHubBaseRestService;

/**
 * APIService 为第三方系统提供Restful Service的API，实现第三方系统的连接：提供各种数据或操作接口
 * 
 * @author zhen zhang
 *
 */
@Path("api")
public class APIService extends AppHubBaseRestService {

    @Override
    protected void init() {

        // nothing
    }
}
