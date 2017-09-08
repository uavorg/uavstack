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

package com.creditease.monitorframework.fat.service;

import javax.jws.WebService;

@WebService(serviceName = "ProfileService", endpointInterface = "com.creditease.monitorframework.fat.service.ProfileService")
public class ProfileServiceImpl implements ProfileService {

    @Override
    public String doProfile(String arg) {

        return arg;
    }

    @Override
    public void doProfile2(String arg2) {

        // Do nothing but must pass sonar check
    }

}
