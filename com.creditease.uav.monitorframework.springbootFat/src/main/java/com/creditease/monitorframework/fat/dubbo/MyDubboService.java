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

package com.creditease.monitorframework.fat.dubbo;

import java.io.IOException;

public class MyDubboService implements IMyDubboService {

    @Override
    public String sayHello(String name) {

        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public String sayException(String name) throws IOException {

        throw new IOException("专门测试用的异常");
    }

    @Override
    public String sayUncatchException(String name) {

        String exceptionInt = "test";
        int num = Integer.parseInt(exceptionInt);
        return name + num;
    }

}
