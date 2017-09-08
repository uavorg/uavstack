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

package com.creditease.uav.cache.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.AsyncCacheCallback;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.test.cache.User;

public class doTestCacheManager {

    class AsyncResult {

        private boolean isOK;
        private AtomicInteger count = new AtomicInteger();

        public boolean isOK() {

            return isOK;
        }

        public void setOK(boolean isOK) {

            this.isOK = isOK;
        }

        public void incre() {

            count.incrementAndGet();
        }

        public int getCount() {

            return count.get();
        }

    }

    private static final CacheManager cm;

    static {
        SystemLogger.init("DEBUG", true, 5);
        String ip = "localhost:6379";
        CacheManager.build(ip, 0, 0, 0);
        cm = CacheManager.instance();
    }

    private void deng(long time) {

        try {
            Thread.sleep(time);
        }
        catch (InterruptedException e) {

        }
    }

    @Before
    public void beforeTest() {

        cm.del("test", "key");
        cm.del("test", "keyjson");
        cm.del("test", "keyhash");

        cm.disableL1Cache("test", "key");
        cm.disableL1Cache("test", "keyjson");
        cm.disableL1Cache("test", "keyhash");
    }

    @Test
    public void testExpire() {

        cm.put("test", "key", "this is a test");
        boolean check = cm.exists("test", "key");
        assertTrue(check);
        String result = cm.get("test", "key");
        assertTrue("this is a test".equals(result));
        cm.expire("test", "key", 1, TimeUnit.SECONDS);
        deng(1500);
        check = cm.exists("test", "key");
        assertTrue(!check);
    }

    @Test
    public void testSimple() {

        cm.put("test", "key", "this is a test");
        boolean check = cm.exists("test", "key");
        assertTrue(check);
        String result = cm.get("test", "key");
        assertTrue("this is a test".equals(result));
        cm.del("test", "key");
        check = cm.exists("test", "key");
        assertTrue(!check);

    }

    @Test
    public void testJSON() {

        Map<String, String> m = new HashMap<String, String>();
        m.put("zz", "1");
        m.put("age", "23");
        cm.putJSON("test", "keyjson", m);
        deng(10);
        boolean check = cm.exists("test", "keyjson");
        assertTrue(check);
        Map<String, String> mt = cm.getJSON("test", "keyjson");
        assertTrue(JSONHelper.toString(mt).equals(JSONHelper.toString(m)));
        cm.del("test", "keyjson");
        check = cm.exists("test", "keyjson");
        assertTrue(!check);
    }

    @Test
    public void testHash() {

        Map<String, String> hash = new HashMap<String, String>();
        hash.put("age", "1");
        hash.put("name", "zz");
        cm.putHash("test", "keyhash", hash);
        boolean check = cm.exists("test", "keyhash");
        assertTrue(check);
        Map<String, String> result = cm.getHash("test", "keyhash", "age", "name");
        assertTrue("1".equals(result.get("age")));
        assertTrue("zz".equals(result.get("name")));

        Map<String, String> result2 = cm.getHashAll("test", "keyhash");
        assertTrue("1".equals(result2.get("age")));
        assertTrue("zz".equals(result2.get("name")));

        cm.del("test", "keyhash");
        check = cm.exists("test", "keyhash");
        assertTrue(!check);
    }

    @Test
    public void testJSONClass() {

        User m = new User();
        m.setAge(22);
        m.setName("zz");
        cm.putJSON("test", "keyjson", m);
        deng(10);
        boolean check = cm.exists("test", "keyjson");
        assertTrue(check);
        User mt = cm.getJSON("test", "keyjson", User.class);
        assertTrue(JSONHelper.toString(mt).equals(JSONHelper.toString(m)));
        cm.del("test", "keyjson");
        check = cm.exists("test", "keyjson");
        assertTrue(!check);
    }

    @Test
    public void testHashAsync() {

        final Map<String, String> hash = new HashMap<String, String>();
        hash.put("age", "1");
        hash.put("name", "zz");
        final AsyncResult ar = new AsyncResult();
        ar.setOK(false);

        cm.putHash("test", "keyhash", hash, new AsyncCacheCallback<Boolean>() {

            @Override
            public void onResult(Boolean t) {

                assertTrue(t);
                boolean check = cm.exists("test", "keyhash");
                assertTrue(check);
                cm.getHash("test", "keyhash", new AsyncCacheCallback<Map<String, String>>() {

                    @Override
                    public void onResult(Map<String, String> t) {

                        assertTrue("1".equals(t.get("age")));
                        assertTrue("zz".equals(t.get("name")));

                        cm.getHashAll("test", "keyhash", new AsyncCacheCallback<Map<String, String>>() {

                            @Override
                            public void onResult(Map<String, String> t) {

                                assertTrue("1".equals(t.get("age")));
                                assertTrue("zz".equals(t.get("name")));

                                cm.del("test", "keyhash", new AsyncCacheCallback<Boolean>() {

                                    @Override
                                    public void onResult(Boolean t) {

                                        assertTrue(t);
                                        boolean check = cm.exists("test", "keyhash");
                                        assertTrue(!check);
                                        ar.setOK(true);
                                    }

                                });
                            }

                        });
                    }

                }, "age", "name");
            }
        });

        deng(1000);
        assertTrue(ar.isOK());

    }

    @Test
    public void testJSONClassAsync() {

        final User m = new User();
        m.setAge(22);
        m.setName("zz");
        final AsyncResult ar = new AsyncResult();
        ar.setOK(false);

        cm.putJSON("test", "keyjson", m, new AsyncCacheCallback<Boolean>() {

            @Override
            public void onResult(Boolean t) {

                assertTrue(t);
                boolean check = cm.exists("test", "keyjson");
                assertTrue(check);

                cm.getJSON("test", "keyjson", new AsyncCacheCallback<User>() {

                    @Override
                    public void onResult(User t) {

                        assertTrue(JSONHelper.toString(t).equals(JSONHelper.toString(m)));

                        cm.del("test", "keyjson", new AsyncCacheCallback<Boolean>() {

                            @Override
                            public void onResult(Boolean t) {

                                assertTrue(t);
                                boolean check = cm.exists("test", "keyjson");
                                assertTrue(!check);
                                ar.setOK(true);
                            }

                        });
                    }

                }, User.class);
            }

        });

        deng(1000);
        assertTrue(ar.isOK());

    }

    @Test
    public void testJSONAsync() {

        final Map<String, String> m = new HashMap<String, String>();
        m.put("zz", "1");
        m.put("age", "23");
        final AsyncResult ar = new AsyncResult();
        ar.setOK(false);

        cm.putJSON("test", "keyjson", m, new AsyncCacheCallback<Boolean>() {

            @Override
            public void onResult(Boolean t) {

                assertTrue(t);
                boolean check = cm.exists("test", "keyjson");
                assertTrue(check);

                cm.getJSON("test", "keyjson", new AsyncCacheCallback<Map<String, String>>() {

                    @Override
                    public void onResult(Map<String, String> t) {

                        assertTrue(JSONHelper.toString(t).equals(JSONHelper.toString(m)));

                        cm.del("test", "keyjson", new AsyncCacheCallback<Boolean>() {

                            @Override
                            public void onResult(Boolean t) {

                                assertTrue(t);
                                boolean check = cm.exists("test", "keyjson");
                                assertTrue(!check);
                                ar.setOK(true);
                            }

                        });
                    }

                });
            }

        });

        deng(1000);
        assertTrue(ar.isOK());
    }

    @Test
    public void testSimpleAsync() {

        final AsyncResult ar = new AsyncResult();
        ar.setOK(false);
        cm.put("test", "key", "this is a test", new AsyncCacheCallback<Boolean>() {

            @Override
            public void onResult(Boolean t) {

                assertTrue(t);
                boolean check = cm.exists("test", "key");
                assertTrue(check);

                cm.get("test", "key", new AsyncCacheCallback<String>() {

                    @Override
                    public void onResult(String t) {

                        assertTrue("this is a test".equals(t));

                        cm.del("test", "key", new AsyncCacheCallback<Boolean>() {

                            @Override
                            public void onResult(Boolean t) {

                                assertTrue(t);

                                boolean check = cm.exists("test", "key");
                                assertTrue(!check);
                                ar.setOK(true);
                            }

                        });
                    }

                });

            }

        });

        deng(1000);

        if (!ar.isOK) {
            fail();
        }
    }

    @Test
    public void testBatch() {

        // batch
        final User pu = new User();
        pu.setAge(22);
        pu.setName("zz");

        final Map<String, String> tm = new HashMap<String, String>();
        tm.put("zz", "1");
        tm.put("age", "23");

        final AsyncResult ar = new AsyncResult();
        ar.setOK(false);

        CacheManager.instance().beginBatch();

        CacheManager.instance().put("test", "key", "this is a test");
        CacheManager.instance().get("test", "key", new AsyncCacheCallback<String>() {

            @Override
            public void onResult(String t) {

                assertTrue("this is a test".equals(t));
                ar.setOK(true);
                ar.incre();
            }
        });

        CacheManager.instance().putJSON("test", "key1", tm);
        CacheManager.instance().getJSON("test", "key1", new AsyncCacheCallback<Map<String, String>>() {

            @Override
            public void onResult(Map<String, String> t) {

                assertTrue(JSONHelper.toString(t).equals(JSONHelper.toString(tm)));
                ar.setOK(true);
                ar.incre();
            }
        });

        CacheManager.instance().putJSON("test", "key2", pu);
        CacheManager.instance().getJSON("test", "key2", new AsyncCacheCallback<User>() {

            @Override
            public void onResult(User t) {

                assertTrue(JSONHelper.toString(t).equals(JSONHelper.toString(pu)));
                ar.setOK(true);
                ar.incre();
            }
        }, User.class);

        CacheManager.instance().submitBatch();

        deng(1000);

        cm.del("test", "key");
        cm.del("test", "key1");
        cm.del("test", "key2");

        if (!ar.isOK || ar.getCount() != 3) {
            fail();
        }
    }

    @Test
    public void testL1CacheAsyncOpers() {

        final AsyncResult ar = new AsyncResult();

        cm.put("test", "key", "this is a test");

        final Map<String, String> m = new HashMap<String, String>();
        m.put("zz", "1");
        m.put("age", "23");
        cm.putJSON("test", "keyjson", m);

        final Map<String, String> hash = new HashMap<String, String>();
        hash.put("age", "1");
        hash.put("name", "zz");
        cm.putHash("test", "keyhash", hash);

        cm.enableL1Cache("test", "key", 30);
        cm.enableL1Cache("test", "keyjson", 30);
        cm.enableL1Cache("test", "keyhash", 30);

        assertTrue(cm.exists("test", "key") == true);
        cm.get("test", "key", new AsyncCacheCallback<String>() {

            @Override
            public void onResult(String t) {

                assertTrue("this is a test".equals(t));
                ar.incre();
            }
        });

        assertTrue(cm.exists("test", "keyjson") == true);
        cm.getJSON("test", "keyjson", new AsyncCacheCallback<Map<String, String>>() {

            @Override
            public void onResult(Map<String, String> t) {

                assertTrue(JSONHelper.toString(t).equals(JSONHelper.toString(m)));
                ar.incre();
            }

        });

        assertTrue(cm.exists("test", "keyhash") == true);
        cm.getHashAll("test", "keyhash", new AsyncCacheCallback<Map<String, String>>() {

            @Override
            public void onResult(Map<String, String> result2) {

                assertTrue("1".equals(result2.get("age")));
                assertTrue("zz".equals(result2.get("name")));
                ar.incre();
            }

        });

        deng(1000);

        assertTrue(cm.getL1CacheCount() == 3);

        cm.expire("test", "key", 1, TimeUnit.SECONDS);
        cm.expire("test", "keyjson", 1, TimeUnit.SECONDS);
        cm.expire("test", "keyhash", 1, TimeUnit.SECONDS);

        deng(1500);

        assertTrue(cm.exists("test", "key") == false);
        assertTrue(cm.exists("test", "keyjson") == false);
        assertTrue(cm.exists("test", "keyhash") == false);

        cm.del("test", "key");
        cm.del("test", "keyjson");
        cm.del("test", "keyhash");

        deng(500);

        assertTrue(cm.getL1CacheCount() == 0);

        cm.disableL1Cache("test", "key");
        cm.disableL1Cache("test", "keyjson");
        cm.disableL1Cache("test", "keyhash");

        if (ar.getCount() != 3) {
            fail();
        }
    }

    @Test
    public void testL1CacheSyncOpers() {

        cm.put("test", "key", "this is a test");

        Map<String, String> m = new HashMap<String, String>();
        m.put("zz", "1");
        m.put("age", "23");
        cm.putJSON("test", "keyjson", m);

        Map<String, String> hash = new HashMap<String, String>();
        hash.put("age", "1");
        hash.put("name", "zz");
        cm.putHash("test", "keyhash", hash);

        cm.enableL1Cache("test", "key", 30);
        cm.enableL1Cache("test", "keyjson", 30);
        cm.enableL1Cache("test", "keyhash", 30);

        assertTrue(cm.exists("test", "key") == true);
        String result = cm.get("test", "key");
        assertTrue("this is a test".equals(result));

        assertTrue(cm.exists("test", "keyjson") == true);
        Map<String, String> mt = cm.getJSON("test", "keyjson");
        assertTrue(JSONHelper.toString(mt).equals(JSONHelper.toString(m)));

        assertTrue(cm.exists("test", "keyhash") == true);
        Map<String, String> result2 = cm.getHashAll("test", "keyhash");
        assertTrue("1".equals(result2.get("age")));
        assertTrue("zz".equals(result2.get("name")));

        assertTrue(cm.getL1CacheCount() == 3);

        cm.expire("test", "key", 1, TimeUnit.SECONDS);
        cm.expire("test", "keyjson", 1, TimeUnit.SECONDS);
        cm.expire("test", "keyhash", 1, TimeUnit.SECONDS);

        deng(1500);

        assertTrue(cm.exists("test", "key") == false);
        assertTrue(cm.exists("test", "keyjson") == false);
        assertTrue(cm.exists("test", "keyhash") == false);

        cm.del("test", "key");
        cm.del("test", "keyjson");
        cm.del("test", "keyhash");

        deng(500);

        assertTrue(cm.getL1CacheCount() == 0);

        cm.disableL1Cache("test", "key");
        cm.disableL1Cache("test", "keyjson");
        cm.disableL1Cache("test", "keyhash");
    }

    @Test
    public void testIncreDecre() {

        cm.put("my", "bb", "100");
        int n = cm.decre("my", "bb");

        assertTrue(n == 99);

        n = cm.incre("my", "bb");

        assertTrue(n == 100);

        int threadCount = 3;

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {

            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {

                    int count = 20;

                    for (int i = 0; i < count; i++) {
                        int n = cm.decre("my", "bb");
                        System.out.println("[CE]" + n);
                    }
                }

            });
            threads[i] = t;
        }

        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
        }

        deng(1000);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testList() {

        cm.del("list", "0");

        cm.lpush("list", "0", "data_0");
        cm.lpush("list", "0", "data_1");
        cm.rpush("list", "0", "data_2");
        cm.rpush("list", "0", "data_3");
        List ls = cm.lrange("list", "0", 0, 50);

        assertTrue("LIST SIZE is not 4", ls.size() == 4);

        cm.lset("list", "0", 0, "data_0");
        cm.lset("list", "0", 1, "data_1");

        assertTrue("lindex data is not data_2", cm.lindex("list", "0", 2).equalsIgnoreCase("data_2"));

        assertTrue("lpop data is not data_0", cm.lpop("list", "0").equalsIgnoreCase("data_0"));
        assertTrue("lpop data is not data_3", cm.rpop("list", "0").equalsIgnoreCase("data_3"));

        cm.lrem("list", "0", 1, "data_0");
        cm.lrem("list", "0", 1, "data_1");
        cm.lrem("list", "0", 1, "data_2");
        cm.lrem("list", "0", 1, "data_3");

        ls = cm.lrange("list", "0", 0, 50);

        assertTrue("LIST SIZE is not 0", ls.size() == 0);

        cm.del("list", "0");

        final AsyncResult ar = new AsyncResult();
        ar.setOK(false);

        cm.lpush("list", "0", "data_0", new AsyncCacheCallback<Boolean>() {

            @Override
            public void onResult(Boolean t) {

                if (t == false) {
                    return;
                }

                ar.incre();
                ar.setOK(true);

                cm.rpush("list", "0", "data_1", new AsyncCacheCallback<Boolean>() {

                    @Override
                    public void onResult(Boolean t) {

                        if (t == false) {
                            return;
                        }

                        ar.incre();
                        ar.setOK(true);

                        cm.lrange("list", "0", 0, 50, new AsyncCacheCallback<List>() {

                            @Override
                            public void onResult(List t) {

                                assertTrue("LIST SIZE is not 2", t.size() == 2);

                                ar.incre();
                                ar.setOK(true);

                                cm.lpop("list", "0", new AsyncCacheCallback<String>() {

                                    @Override
                                    public void onResult(String t) {

                                        assertTrue("lpop data is not data_0", t.equalsIgnoreCase("data_0"));

                                        ar.incre();
                                        ar.setOK(true);

                                        cm.rpop("list", "0", new AsyncCacheCallback<String>() {

                                            @Override
                                            public void onResult(String t) {

                                                assertTrue("lpop data is not data_1", t.equalsIgnoreCase("data_1"));

                                                ar.incre();
                                                ar.setOK(true);
                                            }

                                        });
                                    }

                                });
                            }

                        });
                    }

                });
            }

        });

        deng(1000);

        if (ar.getCount() != 5) {
            fail();
        }
    }
}
