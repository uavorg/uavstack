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

import com.creditease.agent.helpers.StringHelper;

public class StandardProfileRespository {

    protected final Map<String, StandardProfileElement> elemsMap = new LinkedHashMap<String, StandardProfileElement>();
    protected boolean isUpdate = false;
    protected int state = 0;

    public StandardProfileRespository() {
    }

    public StandardProfileRespository(String[] elemNames) {

        for (String elemName : elemNames) {

            StandardProfileElement spe = new StandardProfileElement(elemName);

            this.addElement(spe);
        }
    }

    public String toJSONString() {

        StringBuilder sb = new StringBuilder("[");
        StandardProfileElement[] elems = getElements();
        for (int i = 0; i < elems.length; i++) {
            sb.append(elems[i].toJSONString());

            if (i < elems.length - 1) {
                sb.append(",");
            }
        }
        return sb.append("]").toString();
    }

    public StandardProfileElement getElement(String elemId) {

        if (StringHelper.isEmpty(elemId)) {
            return null;
        }

        return this.elemsMap.get(elemId);
    }

    public void addElement(StandardProfileElement elem) {

        if (null == elem) {
            return;
        }

        this.elemsMap.put(elem.getElemId(), elem);
    }

    public boolean isUpdate() {

        return this.isUpdate;
    }

    public void setUpdate(boolean check) {

        this.isUpdate = check;
    }

    public StandardProfileElement[] getElements() {

        StandardProfileElement[] array = new StandardProfileElement[elemsMap.values().size()];
        return elemsMap.values().toArray(array);
    }

    public void destroy() {

        for (StandardProfileElement elem : elemsMap.values()) {
            elem.destroy();
        }

        this.elemsMap.clear();
    }

    public int getState() {

        return state;
    }

    public void setState(int state) {

        this.state = state;
    }

}
