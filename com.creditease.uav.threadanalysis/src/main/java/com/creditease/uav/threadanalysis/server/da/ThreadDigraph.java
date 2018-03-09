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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ThreadDigraph {

    private int vertex;
    private int edge;
    private List<Integer>[] adj;

    private Map<String, Integer> threadTable = new HashMap<>();
    private List<String> threadKeys = new ArrayList<>();

    private Map<String, ThreadObject> threadMapping = new HashMap<>();
    private Map<String, MonitorObject> monitorMapping = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ThreadDigraph(List<ThreadObject> threads) {

        // create symbol table
        int i = 0;
        for (ThreadObject thread : threads) {
            threadTable.put(thread.getId(), i++);
            threadKeys.add(thread.getId());

            threadMapping.put(thread.getId(), thread);
            MonitorObject mo = thread.getPendingMonitor();
            if (mo != null) {
                MonitorObject v = monitorMapping.get(mo.getId());
                if (v == null) {
                    monitorMapping.put(mo.getId(), mo);
                }
            }
            List<MonitorObject> locked = thread.getLockedMonitors();
            if (locked != null && !locked.isEmpty()) {
                for (MonitorObject m : locked) {

                    MonitorObject v = monitorMapping.get(m.getId());
                    if (v == null) {
                        monitorMapping.put(m.getId(), m);
                    }

                    monitorMapping.get(m.getId()).setLockedBy(thread);
                }
            }
        }

        // init digraph
        this.vertex = threads.size();
        this.edge = 0;
        this.adj = new List[vertex];
        for (int v = 0; v < vertex; v++) {
            adj[v] = new LinkedList<>();
        }

        // add edge
        for (ThreadObject thread : threadMapping.values()) {
            MonitorObject pending = thread.getPendingMonitor();
            if (pending == null) {
                continue;
            }
            MonitorObject lock = monitorMapping.get(pending.getId());
            ThreadObject pendingTo = lock.getLockedBy();
            if (pendingTo != null && !thread.getId().equals(pendingTo.getId())) { // 排除掉自环
                addEdge(threadTable.get(thread.getId()), threadTable.get(pendingTo.getId()));
            }
        }
    }

    public int vertex() {

        return vertex;
    }

    public int edge() {

        return edge;
    }

    public Iterable<Integer> adj(int v) {

        return adj[v];
    }

    private void addEdge(int v, int w) {

        adj[v].add(w);
        edge++;
    }

    public ThreadObject thread(int v) {

        return threadMapping.get(threadKeys.get(v));
    }

    public ThreadObject thread(String threadId) {

        return threadMapping.get(threadId);
    }

    public int index(ThreadObject thread) {

        return threadTable.get(thread.getId());
    }

    public List<String> getThreadKeys() {

        return threadKeys;
    }

    public Map<String, ThreadObject> getThreadMapping() {

        return threadMapping;
    }

    public Map<String, MonitorObject> getMonitorMapping() {

        return monitorMapping;
    }

    public Map<String, Integer> getThreadTable() {

        return threadTable;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (int v = 0; v < vertex; v++) {
            sb.append(threadMapping.get(threadKeys.get(v)).getName() + "[" + threadKeys.get(v) + "]");
            for (Integer w : adj[v]) {
                sb.append(" --> " + threadMapping.get(threadKeys.get(w)).getName() + "[" + threadKeys.get(w) + "]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
