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

package com.creditease.monitorframework.fat;

import com.creditease.monitorframework.fat.ivc.MyTestInjectObj;

public class MyTestIVCInjectClass {

    public String testMethod1(String t, MyTestInjectObj obj) {

        return "success";
    }

    public int testMethod1(String t, MyTestInjectObj obj, boolean check) {

        return 0;
    }

    public String testMethod2(String y, int h) {

        return "";
    }

    public Integer testMethod3(Integer i) {

        return 0;
    }

    public Double testMethod3(Double i) {

        return 0D;
    }

    public Float testMethod3(Float i) {

        return 0F;
    }

    public Boolean testMethod3(Boolean i) {

        return false;
    }

    public void testException() throws Exception {

        throw new Exception("this is a exception test");
    }

    public void testNPE() {

        String b = null;

        b.toCharArray();
    }
}
