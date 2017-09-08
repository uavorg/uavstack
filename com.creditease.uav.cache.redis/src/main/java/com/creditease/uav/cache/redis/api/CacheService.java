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

package com.creditease.uav.cache.redis.api;

/**
 * Created with IntelliJ IDEA. User: Administrator Date: 15-7-27 Time: 上午10:52
 */
public interface CacheService {

    public void start();

    public void shutdown();

    public Object[] submitCommands(CommandInfo... commands);

    public void submitCommands(AbstractAsyncHandler<CommandInfo> handler, CommandInfo... commands);
}
