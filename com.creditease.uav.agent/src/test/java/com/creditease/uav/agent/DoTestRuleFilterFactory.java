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

package com.creditease.uav.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.feature.logagent.api.LogFilterAndRule;
import com.creditease.agent.helpers.ReflectHelper;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

public class DoTestRuleFilterFactory {

    LogFilterAndRule far = null;
    String name = "user.attr";
    String ruleString = null;
    String filterString = null;

    @Before
    public void setUp() throws Exception {

        // ruleString = "{\"separator\":\"|\", \"field\":[1,2,4], \"timestamp\":1}";
        // filterString = "{filter:\"\\[CE\\]\"}";
        // RuleFilterFactory.getInstance()
        // .newBuilder()
        // .serverId("sid")
        // .appId("aid")
        // .logId("lid")
        // .build();
    }

    @Test
    public void getLogFilter() {

        String log = "(ce)3245678iuytrqw4567";
        // Matcher m = Pattern.compile("(?:<ce>)(.*?)(?:</ce>)").matcher(log);
        Matcher m = Pattern.compile("\\(ce\\).*?").matcher(log);
        System.out.println(m.matches());
    }

    @Test
    public void getLogRule() {

        // String log = "1234567\t234567\tu98765432";

        // assertEquals("1234567", Iterables.getFirst((far.getResult(log))[0]));
        // assertEquals("u98765432", Iterables.getLast(far.getResult(log)));
    }

    @Test
    public void getFileAttributes() throws IOException {

        Object obj = Files.getAttribute(new File("/Users/fathead/temp/file3").toPath(), "unix:ino");
        System.out.println(obj);
    }

    // suppose fix bug
    @Test
    public void setCustomerAttr() throws IOException {

        Path target = Paths.get("/Users/fathead/temp/file4");
        UserDefinedFileAttributeView view = Files.getFileAttributeView(target, UserDefinedFileAttributeView.class);
        view.write(name, Charset.defaultCharset().encode("pinelet"));
    }

    // suppose fix bug
    @Test
    public void getCustomerAttr() throws IOException {

        Path target = Paths.get("/Users/fathead/temp/file4");
        UserDefinedFileAttributeView view2 = Files.getFileAttributeView(target, UserDefinedFileAttributeView.class);
        ByteBuffer buf = ByteBuffer.allocate(view2.size(name));
        view2.read(name, buf);
        buf.flip();
        String value = Charset.defaultCharset().decode(buf).toString();
        System.out.println("value=" + value);
    }

    @Test
    public void updateFilter() {

        // far.updateLogFilter(filters);
    }

    @Test
    public void updateRule() {

    }

    @Test
    public void getOsInfo() {

        System.out.println(System.getProperties().getProperty("os.name"));
        System.out.println(System.getProperties().getProperty("os.arch"));
    }

    @Test
    public void getNewInstance() {

        Object obj = ReflectHelper.newInstance("com.creditease.agent.feature.logagent.SystemLogFilterAndRule");
        assertNotNull(obj);
        System.out.println(obj);
    }

    @Test
    public void dotestAnalysis() {

        String log = "Feb 18 14:19:14 localhost sshd[111111]: pam_unix(sshd:session): session closed for user yxgly1111111";
        System.out.println(doAnalysis(log));
    }

    public Map<String, String> doAnalysis(String log) {

        Map<String, String> result = new HashMap<String, String>();
        String temp3area = log.substring(15).trim();
        Splitter s = Splitter.onPattern("\\p{Space}").trimResults();
        List<String> it = Lists.newArrayList(s.limit(3).split(temp3area));
        result.put("date", log.substring(0, 15));
        result.put("host", it.get(0));
        result.put("pid", it.get(1));
        result.put("message", it.get(2));
        return result;
    }

    @Test
    public void getDefaultInstance() {

        String classname = "Default";
        String rule = null;
        rule = Optional.fromNullable(rule)
                .or("{\"separator\":\"\t\", \"assignfields\":{\"content\":1}, \"timestamp\": 0}");
        // parse filter json
        String filter = null;
        String filterregex = Optional.fromNullable(filter).or(".*");
        // parse rule json
        JSONObject robject = JSON.parseObject(rule);
        String separator = Optional.fromNullable(robject.getString("separator")).or("\t");
        JSONObject assignFields = Optional.fromNullable(robject.getJSONObject("assignfields"))
                .or(JSON.parseObject("{content:1}"));
        // Verify timeStamp number is available
        int timestampNumber = robject.getIntValue("timestamp");
        LogFilterAndRule mainLogFAR = (LogFilterAndRule) ReflectHelper.newInstance(
                "com.creditease.agent.feature.logagent.far." + classname + "LogFilterAndRule",
                new Class[] { String.class, String.class, JSONObject.class, int.class },
                new Object[] { filterregex, separator, assignFields, timestampNumber });
        assertNotNull(mainLogFAR);
        System.out.println(mainLogFAR);
    }

    class TesTicker extends Ticker {

        private final AtomicLong nanos = new AtomicLong();

        /** Advances the ticker value by {@code time} in {@code timeUnit}. */
        public TesTicker advance(long time, TimeUnit timeUnit) {

            nanos.addAndGet(timeUnit.toNanos(time));
            return this;
        }

        @Override
        public long read() {

            long value = nanos.getAndAdd(0);
            return value;
        }
    }

    @Test
    public void guavaCache() throws InterruptedException {

        TesTicker ticker = new TesTicker();
        Cache<String, Pojo> collection = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).ticker(ticker)
                .<String, Pojo> build();
        Pojo p1 = new Pojo("p1name", "p1val");
        Pojo p2 = new Pojo("p2name", "p2val");
        collection.put("p1", p1);
        collection.put("p2", p2);
        ticker.advance(3, TimeUnit.SECONDS);
        Map<String, Pojo> map = collection.asMap();
        assertTrue(map.containsKey("p1"));
        // map.get("p1");
        ticker.advance(3, TimeUnit.SECONDS);
        assertEquals(2, collection.size());
        assertFalse(map.containsKey("p1"));// 有清除过期操作
        assertEquals(1, collection.size());
        assertNull(collection.getIfPresent("p2"));
        assertNull(collection.getIfPresent("p1"));// 有清除过期操作
        assertEquals(0, collection.size());
    }

    class Pojo {

        private String name;

        private String val;

        public Pojo(String name, String val) {
            this.name = name;
            this.val = val;
        }

        public String getName() {

            return name;
        }

        public Pojo setName(String name) {

            this.name = name;
            return this;
        }

        public String getVal() {

            return val;
        }

        public Pojo setVal(String val) {

            this.val = val;
            return this;
        }
    }
}
