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

package com.creditease.uav.threadanalysis.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.threadanalysis.server.da.DeadlockCycle;
import com.creditease.uav.threadanalysis.server.da.MonitorObject;
import com.creditease.uav.threadanalysis.server.da.MonitorObject.MonitorState;
import com.creditease.uav.threadanalysis.server.da.ThreadChain;
import com.creditease.uav.threadanalysis.server.da.ThreadDigraph;
import com.creditease.uav.threadanalysis.server.da.ThreadObject;

public class ThreadAnalyser extends AbstractComponent {

    private static final Pattern PATTERN_NAME = Pattern.compile("^\"(.*)\" ");
    private static final Pattern PATTERN_DAEMON = Pattern.compile(" (daemon) ");
    private static final Pattern PATTERN_NID = Pattern.compile(" nid=([0-9a-fx]+) ");
    private static final Pattern PATTERN_TAIL = Pattern.compile(" \\[([0-9a-fx]+)\\]$");
    private static final Pattern PATTERN_LOCK = Pattern.compile(" <([0-9a-fx]+)> ");

    public ThreadAnalyser(String cName, String feature) {
        super(cName, feature);
    }

    /**
     * 查询快照概况
     * 
     * @param records
     *            进程快照信息
     * @return json: {cpu: '50', threadCount: '123', runnableCount: '23', blockedCount: '34', waitingCount: '45',
     *         deadlock: []}
     */
    public Map<String, Object> queryDumpInfo(List<Map<String, Object>> records) {

        List<ThreadObject> threads = parseThreads(records);
        float cpu = 0;
        int threadCount = threads.size();
        int runnableCount = 0;
        int blockedCount = 0;
        int waitingCount = 0;
        for (ThreadObject to : threads) {
            cpu += to.getCpu();
            if (to.getThreadState() == Thread.State.RUNNABLE) {
                runnableCount++;
            }
            else if (to.getThreadState() == Thread.State.BLOCKED) {
                blockedCount++;
            }
            else if (to.getThreadState() == Thread.State.WAITING || to.getThreadState() == Thread.State.TIMED_WAITING) {
                waitingCount++;
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("cpu", cpu);
        map.put("threadCount", threadCount);
        map.put("runnableCount", runnableCount);
        map.put("blockedCount", blockedCount);
        map.put("waitingCount", waitingCount);

        ThreadDigraph digraph = new ThreadDigraph(parseThreads(records));
        DeadlockCycle cycle = new DeadlockCycle(digraph);

        if (cycle.hasCycle()) {
            List<String> list = new ArrayList<>();
            for (Integer i : cycle.cycle()) {
                list.add(digraph.thread(i).getInfo());
            }
            if (list.size() > 1) {
                list.remove(list.size() - 1);
            }
            map.put("deadlock", list);
        }
        else {
            map.put("deadlock", Collections.emptyList());
        }

        return map;
    }

    /**
     * 查找线程依赖
     * 
     * @param record
     * @param threadId
     * @return
     */
    public List<ThreadObject> findThreadChain(List<Map<String, Object>> record, String threadId) {

        ThreadDigraph digraph = new ThreadDigraph(parseThreads(record));
        ThreadChain chain = new ThreadChain(digraph);
        ThreadObject thread = digraph.thread(threadId);
        return chain.getChain(thread);
    }

    /**
     * 构建线程有向图
     * 
     * @param record
     * @return {nodes: [{id:0, name:'', type: '', state: ''}], edges: [{from:0, to:1}]}
     */
    public Map<String, Object> buildThreadDigraph(List<Map<String, Object>> records) {

        List<ThreadObject> threads = parseThreads(records);
        ThreadDigraph digraph = new ThreadDigraph(threads);

        List<String> tKeys = digraph.getThreadKeys();
        List<String> mKeys = new ArrayList<>(digraph.getMonitorMapping().keySet());
        Map<String, Integer> st = new HashMap<>(digraph.getThreadTable());
        int idx = tKeys.size();
        for (String mo : mKeys) {
            st.put(mo, Integer.valueOf(idx++));
        }

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Integer>> edges = new ArrayList<>();

        for (String tk : tKeys) {
            ThreadObject t = digraph.getThreadMapping().get(tk);
            Map<String, Object> node = new HashMap<>();
            node.put("id", st.get(tk));
            node.put("name", t.getName());
            node.put("type", "Thread");
            node.put("tip", t.getInfo().split("\n")[0]);
            node.put("msg", t.getInfo());
            if (t.getThreadState() != null) {
                node.put("state", t.getThreadState());
            }
            nodes.add(node);

            MonitorObject pm = t.getPendingMonitor();
            if (pm != null) {
                Map<String, Integer> edge = new HashMap<>();
                edge.put("from", st.get(tk));
                edge.put("to", st.get(pm.getId()));
                edges.add(edge); // from thread to pending lock
            }
            List<MonitorObject> oms = t.getLockedMonitors();
            if (oms != null && !oms.isEmpty()) {
                for (MonitorObject om : oms) {
                    Map<String, Integer> edge = new HashMap<>();
                    edge.put("from", st.get(om.getId()));
                    edge.put("to", st.get(tk));
                    edges.add(edge); // from own lock to thread
                }
            }
        }

        for (String mk : mKeys) {
            MonitorObject mo = digraph.getMonitorMapping().get(mk);
            Map<String, Object> node = new HashMap<>();
            node.put("id", st.get(mk));
            node.put("name", mo.getObj());
            node.put("type", "Lock");
            node.put("tip", mo.getState() + " <" + mo.getId() + "> " + mo.getObj());
            nodes.add(node);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("nodes", nodes);
        map.put("edges", edges);
        return map;
    }

    /**
     * 
     * @param head
     * @param records
     * @return
     */
    public List<Map<String, String>> queryMutilDumpInfo(List<String> head, List<List<Map<String, Object>>> records) {

        List<Map<String, String>> list = new ArrayList<>();
        Map<String, ThreadObject> base = parseThreadMapping(parseThreads(records.get(0)));

        List<Map<String, ThreadObject>> subsequents = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            subsequents.add(parseThreadMapping(parseThreads(records.get(i))));
        }

        Map<String, String> line = null;
        for (Map.Entry<String, ThreadObject> entry : base.entrySet()) {
            line = new HashMap<>();

            String threadId = entry.getKey();
            line.put("thread", threadId);
            line.put(head.get(0), JSONHelper.toString(entry.getValue()));
            int headIdx = 1;
            for (Map<String, ThreadObject> subsequent : subsequents) {
                ThreadObject thread = subsequent.get(threadId);
                if (thread == null) {
                    line.put(head.get(headIdx), "");
                }
                else {
                    line.put(head.get(headIdx), JSONHelper.toString(thread));
                }
                headIdx++;
            }

            list.add(line);
        }
        return list;
    }

    /**
     * 
     * @param threadIds
     * @param records
     * @return
     */
    public Map<String, Object> queryMutilDumpGraph(List<String> threadIds, List<List<Map<String, Object>>> records) {

        List<ThreadDigraph> dumps = new ArrayList<>();
        for (List<Map<String, Object>> record : records) {
            List<ThreadObject> threads = parseThreads(record);
            dumps.add(new ThreadDigraph(threads));
        }

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (String threadId : threadIds) {
            String timeSeries = null;
            for (ThreadDigraph tg : dumps) {
                ThreadChain threadChain = new ThreadChain(tg);
                ThreadObject thread = tg.thread(threadId);
                List<ThreadObject> chain = threadChain.getChain(thread);
                if (chain == null) {
                    continue;
                }

                String from = null;
                for (ThreadObject to : chain) {
                    String id = to.getId() + "_" + to.getTime();
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", id);
                    node.put("name", DateTimeHelper.toStandardDateFormat(to.getTime()) + "\n" + to.getName());
                    node.put("type", "Thread");
                    node.put("tip", to.getInfo().split("\n")[0]);
                    node.put("msg", to.getInfo());
                    if (to.getThreadState() != null) {
                        node.put("state", to.getThreadState().toString());
                    }
                    nodes.add(node);
                    if (from != null) {
                        Map<String, Object> edge = new HashMap<>();
                        edge.put("from", from);
                        edge.put("to", id);
                        edges.add(edge);
                    }
                    from = id;
                }

                ThreadObject ts = chain.get(0);
                if (timeSeries == null) {
                    timeSeries = ts.getId() + "_" + ts.getTime();
                }
                else {
                    String nextTimeSeries = ts.getId() + "_" + ts.getTime();
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("from", timeSeries);
                    edge.put("to", nextTimeSeries);
                    edge.put("dashes", true);
                    edges.add(edge);
                    timeSeries = nextTimeSeries;
                }
            }
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);

        return graph;
    }

    public List<ThreadObject> parseThreads(List<Map<String, Object>> records) {

        List<ThreadObject> list = new ArrayList<>();
        for (Map<String, Object> map : records) {
            list.add(parseThread(map));
        }
        return list;
    }

    private ThreadObject parseThread(Map<String, Object> map) {

        ThreadObject to = new ThreadObject();

        String info = (String) map.get("info");
        String[] thread = info.split("\n");
        extractFromTitle(thread[0], to);
        extractStackTrace(thread, to);
        to.setInfo(info);

        String percpu = map.get("percpu").toString();
        to.setCpu(DataConvertHelper.toDouble(percpu, 0));
        to.setTime(DataConvertHelper.toLong(map.get("time").toString(), 0));
        return to;
    }

    private Map<String, ThreadObject> parseThreadMapping(List<ThreadObject> list) {

        Map<String, ThreadObject> map = new HashMap<>();
        for (ThreadObject t : list) {
            map.put(t.getId(), t);
        }
        return map;
    }

    private MonitorObject parseMonitor(String monitor) {

        MonitorObject mo = new MonitorObject();
        Matcher matcher = PATTERN_LOCK.matcher(monitor);
        if (matcher.find()) {
            mo.setId(matcher.group(1));
            mo.setObj(monitor.substring(matcher.end(), monitor.length()).trim());
        }
        mo.setState(forMonitorState(monitor));
        return mo;
    }

    private MonitorState forMonitorState(String method) {

        if (method.startsWith("- locked") || method.startsWith("- eliminated")) {
            return MonitorState.LOCKING;
        }
        else if (method.startsWith("- waiting to lock")) {
            return MonitorState.BLOCKING;
        }
        else if (method.startsWith("- waiting on")) {
            return MonitorState.WAITING;
        }
        else if (method.startsWith("- parking to wait for")) {
            return MonitorState.PARKING;
        }
        else {
            return MonitorState.UNKNOWN;
        }
    }

    private void extractFromTitle(String title, ThreadObject to) {

        Matcher matcher = PATTERN_NAME.matcher(title);
        if (matcher.find()) {
            to.setName(matcher.group(1));
        }

        matcher = PATTERN_DAEMON.matcher(title);
        if (matcher.find()) {
            to.setDaemon(true);
        }

        int stateStart = 0;
        matcher = PATTERN_NID.matcher(title);
        if (matcher.find()) {
            to.setId(Integer.valueOf(matcher.group(1).substring(2), 16).toString());
            stateStart = matcher.end();
        }

        int stateEnd = 0;
        matcher = PATTERN_TAIL.matcher(title);
        if (matcher.find()) {
            stateEnd = matcher.start();
        }
        else {
            stateEnd = title.length();
        }

        @SuppressWarnings("unused")
        String titleState = title.substring(stateStart, stateEnd);
    }

    private void extractStackTrace(String[] thread, ThreadObject to) {

        if (thread.length <= 1) {
            List<String> list = Collections.emptyList();
            to.setStackTrace(list);
            return;
        }

        if (thread[1].trim().startsWith("java.lang.Thread.State:")) {
            String line = thread[1].trim();
            int low = "java.lang.Thread.State: ".length();
            int high = line.indexOf(" ", low);
            if (high <= low) {
                high = line.length();
            }
            to.setThreadState(java.lang.Thread.State.valueOf(line.substring(low, high)));
        }

        List<String> stacktrace = new ArrayList<>();
        List<MonitorObject> ownMonitor = new ArrayList<>();
        for (int i = 2; i < thread.length; i++) {
            String method = thread[i].trim();

            if (method.startsWith("-")) {
                MonitorObject mo = parseMonitor(method);
                switch (mo.getState()) {
                    case LOCKING:
                        ownMonitor.add(mo);
                        break;
                    case BLOCKING:
                    case WAITING:
                    case PARKING:
                        to.setPendingMonitor(mo);
                        break;
                    default:
                        break;
                }
                continue;
            }

            stacktrace.add(method);
        }
        to.setLockedMonitors(ownMonitor);
        to.setStackTrace(stacktrace);
    }
}
