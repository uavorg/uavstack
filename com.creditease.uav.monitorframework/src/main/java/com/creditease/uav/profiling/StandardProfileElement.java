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

package com.creditease.uav.profiling;

import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileRepository;
import com.creditease.uav.util.LimitLinkedHashMap;

public class StandardProfileElement implements ProfileElement {

    protected String elemId;
    protected String elemHandlerClass;
    private final Map<String, ProfileElementInstance> instances;

    private ProfileRepository repository;

    public StandardProfileElement(String elemId, String elemHandlerClass) {
        this.elemId = elemId;
        this.elemHandlerClass = elemHandlerClass;
        int instLimitCount = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.profile.eleminst.limit"),
                80);
        instances = new LimitLinkedHashMap<String, ProfileElementInstance>(instLimitCount);
    }

    @Override
    public String getElemId() {

        return this.elemId;
    }

    @Override
    public String getElemHandlerClass() {

        return this.elemHandlerClass;
    }

    @Override
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("PEId:\"" + this.elemId + "\",");
        sb.append("Instances:[");
        ProfileElementInstance[] insts = getInstances();
        for (int i = 0; i < insts.length; i++) {
            sb.append(insts[i].toJSONString());

            if (i < insts.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.append("}").toString();
    }

    @Override
    public void destroy() {

        for (ProfileElementInstance instance : instances.values()) {
            instance.destroy();
        }

        instances.clear();
    }

    @Override
    public ProfileElementInstance getInstance(String id) {

        if (null == id) {
            return null;
        }

        ProfileElementInstance inst = this.instances.get(id);

        if (null == inst) {

            synchronized (instances) {

                inst = this.instances.get(id);

                if (null == inst) {

                    inst = new StandardProfileElementInstance(this, id, this.instances.size());

                    this.instances.put(id, inst);
                }
            }
        }

        return inst;
    }

    @Override
    public ProfileElementInstance[] getInstances() {

        ProfileElementInstance[] array = new ProfileElementInstance[instances.values().size()];
        return instances.values().toArray(array);
    }

    @Override
    public ProfileRepository getRepository() {

        return repository;
    }

    @Override
    public void setRepository(ProfileRepository pr) {

        this.repository = pr;
    }

    @Override
    public void removeInstance(String id) {

        instances.remove(id);
    }

    @Override
    public ProfileElementInstance existInstance(String id) {

        if (instances.containsKey(id)) {
            return instances.get(id);
        }

        return null;
    }

}
