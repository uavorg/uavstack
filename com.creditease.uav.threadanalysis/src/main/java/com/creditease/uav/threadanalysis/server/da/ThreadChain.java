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

package com.creditease.uav.threadanalysis.server.da;

import java.util.ArrayList;
import java.util.List;

public class ThreadChain {

    private ThreadDigraph g;

    public ThreadChain(ThreadDigraph g) {

        this.g = g;
    }

    public List<ThreadObject> getChain(ThreadObject t) {

        if (t == null) {
            return null;
        }

        List<Integer> chain = new ArrayList<>();
        dfs(g.index(t), chain);

        List<ThreadObject> list = new ArrayList<>();
        for (int v : chain) {
            list.add(g.thread(v));
        }
        return list;
    }

    private void dfs(int v, List<Integer> chain) {

        chain.add(v);

        for (int w : g.adj(v)) {
            if (chain.contains(w)) {
                return;
            }

            dfs(w, chain);
        }
    }
}
