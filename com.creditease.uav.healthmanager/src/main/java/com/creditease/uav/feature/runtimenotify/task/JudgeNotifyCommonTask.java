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

package com.creditease.uav.feature.runtimenotify.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.creditease.agent.spi.Abstract1NTask;

public abstract class JudgeNotifyCommonTask extends Abstract1NTask {

    /**
     * @param name
     * @param feature
     */
    public JudgeNotifyCommonTask(String name, String feature) {

        super(name, feature);
    }
    
    /**
     * 
     * @param causeConvergences convergences corresponding to trigger conditions
     * @return the convergence which has the most gradient
     */
    protected String obtainConvergenceForEvent(List<String> convergences, List<String> conditionIndex) {

        List<String> causeConvergences = new ArrayList<String>();
        for(String index : conditionIndex) {
            causeConvergences.add(convergences.get(Integer.parseInt(index)-1));
        }
        
        Collections.sort(causeConvergences, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                
                String[] strs1 = o1.split(",");
                String[] strs2 = o2.split(",");
                return strs1.length > strs2.length ? -1 : 1;
            }
        });
        return causeConvergences.get(0);
    }

}
