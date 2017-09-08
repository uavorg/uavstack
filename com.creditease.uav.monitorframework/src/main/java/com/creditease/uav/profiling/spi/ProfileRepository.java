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

package com.creditease.uav.profiling.spi;

public interface ProfileRepository {

    public String toJSONString();

    public void addElement(ProfileElement elem);

    public ProfileElement[] getElements();

    public ProfileElement getElement(String elemId);

    public boolean isUpdate();

    public void setUpdate(boolean check);

    public void destroy();

    public Profile getProfile();

    /**
     * 0: init 1: in processing 2: finished 3: error
     * 
     * @return
     */
    public int getState();

    public void setState(int state);
}
