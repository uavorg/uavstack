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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.uav.profiling.StandardProfile;

public class ProfileFactory {

    private static ProfileFactory instance = new ProfileFactory();

    public static ProfileFactory instance() {

        return instance;
    }

    private Map<String, Profile> profiles = new ConcurrentHashMap<String, Profile>();

    /**
     * buildProfile
     * 
     * @param profileId
     * @return
     */
    public Profile buildProfile(String profileId) {

        if (profileId == null)
            return null;

        if (profiles.containsKey(profileId)) {
            profiles.get(profileId).destroy();
        }

        Profile mInst = new StandardProfile(profileId);

        profiles.put(profileId, mInst);

        return mInst;
    }

    /**
     * getProfile
     * 
     * @param profileId
     * @return
     */
    public Profile getProfile(String profileId) {

        if (!profiles.containsKey(profileId))
            return null;

        return profiles.get(profileId);
    }

    /**
     * destroyProfile
     * 
     * @param profileId
     */
    public Profile destroyProfile(String profileId) {

        if (profileId == null)
            return null;

        Profile p = getProfile(profileId);
        if (null != p) {
            p.destroy();
        }

        return profiles.remove(profileId);
    }
}
