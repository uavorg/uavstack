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

package com.creditease.agent.profile.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class StandardProfileElement {

    protected String elemId;
    private final Map<String, StandardProfileElementInstance> instances = new LinkedHashMap<String, StandardProfileElementInstance>();

    public StandardProfileElement(String elemId) {
        this.elemId = elemId;
    }

    public String getElemId() {

        return this.elemId;
    }

    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("PEId:\"" + this.elemId + "\",");
        sb.append("Instances:[");
        StandardProfileElementInstance[] insts = getInstances();
        for (int i = 0; i < insts.length; i++) {
            sb.append(insts[i].toJSONString());

            if (i < insts.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.append("}").toString();
    }

    public void destroy() {

        for (StandardProfileElementInstance instance : instances.values()) {
            instance.destroy();
        }

        instances.clear();
    }

    public StandardProfileElementInstance getInstance(String id) {

        if (null == id) {
            return null;
        }

        StandardProfileElementInstance inst = this.instances.get(id);

        if (null == inst) {

            synchronized (instances) {

                inst = this.instances.get(id);

                if (null == inst) {

                    inst = new StandardProfileElementInstance(this, id);

                    this.instances.put(id, inst);
                }
            }
        }

        return inst;
    }

    public StandardProfileElementInstance[] getInstances() {

        StandardProfileElementInstance[] array = new StandardProfileElementInstance[instances.values().size()];
        return instances.values().toArray(array);
    }

}
