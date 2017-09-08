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

package com.creditease.uav.feature.ida;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AbstractHandler;

public abstract class BaseIDAHandler extends AbstractHandler<Object> {

    protected boolean isEnable = false;

    public BaseIDAHandler(String cName, String feature) {
        super(cName, feature);

        isEnable = DataConvertHelper.toBoolean(
                this.getConfigManager().getFeatureConfiguration(this.feature, this.cName + ".enable"), false);
    }

}
