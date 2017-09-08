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

import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileRepository;

public class StandardProfileRespository implements ProfileRepository {

    protected final Map<String, ProfileElement> elemsMap = new LinkedHashMap<String, ProfileElement>();
    protected boolean isUpdate = false;
    protected int state = 0;
    private final Profile profile;

    public StandardProfileRespository(Profile p) {
        profile = p;
    }

    @Override
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("[");
        ProfileElement[] elems = getElements();
        for (int i = 0; i < elems.length; i++) {
            sb.append(elems[i].toJSONString());

            if (i < elems.length - 1) {
                sb.append(",");
            }
        }
        return sb.append("]").toString();
    }

    @Override
    public void addElement(ProfileElement elem) {

        if (null == elem) {
            return;
        }

        this.elemsMap.put(elem.getElemId(), elem);
    }

    @Override
    public boolean isUpdate() {

        return this.isUpdate;
    }

    @Override
    public void setUpdate(boolean check) {

        this.isUpdate = check;
    }

    @Override
    public ProfileElement[] getElements() {

        ProfileElement[] array = new ProfileElement[elemsMap.values().size()];
        return elemsMap.values().toArray(array);
    }

    @Override
    public void destroy() {

        for (ProfileElement elem : elemsMap.values()) {
            elem.destroy();
        }

        this.elemsMap.clear();
    }

    @Override
    public Profile getProfile() {

        return profile;
    }

    @Override
    public int getState() {

        return state;
    }

    @Override
    public void setState(int state) {

        this.state = state;
    }

    @Override
    public ProfileElement getElement(String elemId) {

        return this.elemsMap.get(elemId);
    }
}
