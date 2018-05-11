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

package com.creditease.uav.datastore.mongo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author pengfei
 * @param:Object expKey,
 *                   Map expValue,BasicDBObject set
 */

@SuppressWarnings("rawtypes")
public interface MongodbImplStrategy {

    static final ISystemLogger log = SystemLogger.getLogger(MongodbImplStrategy.class);

    public void concretProcessor(Object expKey, Map expValue, BasicDBObject set);

    /**
     * 
     * find
     */
    public class QueryStrategy implements MongodbImplStrategy {

        @Override
        public void concretProcessor(Object expKey, Map expValue, BasicDBObject set) {

            Map expressions = (Map) expValue.get(expKey);
            Set keys = expressions.keySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = expressions.get(key);
                
                if ("_id".equals(key)) {
                    /**
                     * 使用api 按id查询格式必须是： _id:string
                     */
                    value = new ObjectId(value.toString());
                }
                
                if (value instanceof Map) {
                    RGLStrategy rgl = new RGLStrategy();
                    rgl.concretProcessor(key, expressions, set);

                }
                else if (value instanceof List) {
                    if ("or".equals(key)) {
                        ORStrategy or = new ORStrategy();
                        or.concretProcessor(key, expressions, set);

                    }
                    else {
                        log.err(this, "can not figure out " + key);
                        continue;
                    }
                }
                else {
                    set.append(key, value);
                    log.info(this, "@key: " + key + "@value:" + value);
                }
            }
        }

    }

    /**
     * 
     * update/remove
     */
    public class UpdateStrategy implements MongodbImplStrategy {

        @Override
        public void concretProcessor(Object expKey, Map expValue, BasicDBObject set) {

            Map expressions = (Map) expValue.get(expKey);
            Set keys = expressions.keySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = expressions.get(key);

                if (value instanceof Map) {
                    KVStrategy kv = new KVStrategy();
                    kv.concretProcessor(key, expressions, set);
                }
                else {
                    set.append(key, value);
                    log.info(this, "@key: " + key + "@value:" + value);
                }
            }
        }

    }

    /**
     * ORStrategy
     * 
     **/
    public class ORStrategy implements MongodbImplStrategy {

        @SuppressWarnings("unchecked")
        @Override
        public void concretProcessor(Object expKey, Map expValue, BasicDBObject set) {

            List<DBObject> orList = new ArrayList<DBObject>();
            List<Map> orListMap = (List<Map>) expValue.get(expKey);
            for (int i = 0; i < orListMap.size(); i++) {
                Map expression = orListMap.get(i);
                Set keyset = expression.keySet();
                Iterator iter = keyset.iterator();
                BasicDBObject sub = new BasicDBObject();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    if (("regex".equals(key)) || ("<".equals(key)) || (">".equals(key))) {
                        RGLStrategy process = new RGLStrategy();
                        process.concretProcessor(key, expression, sub);
                    }
                    else {
                        Object value = expression.get(key);
                        sub.append(key, value);
                        log.info(this, "@key: " + key + "@value:" + value);
                    }

                }
                orList.add(sub);
            }
            set.append("$or", orList);
        }
    }

    /**
     * 
     * RGLStrategy
     **/
    public class RGLStrategy implements MongodbImplStrategy {

        @Override
        public void concretProcessor(Object expKey, Map expValue, BasicDBObject set) {

            Map RGLMap = (Map) expValue.get(expKey);
            Set keyset = RGLMap.keySet();
            Iterator iter = keyset.iterator();
            String key = null;
            while (iter.hasNext()) {
                key = (String) iter.next();
                if (expKey.toString().equals("regex")) {
                    String value = (String) RGLMap.get(key);
                    set.append(key, new BasicDBObject("$regex", value).append("$options", "mi"));
                }
                else if (expKey.toString().equals("<")) {
                    Object value = RGLMap.get(key);
                    set.append(key, new BasicDBObject("$lt", value));
                }
                else if (expKey.toString().equals(">")) {
                    Object value = RGLMap.get(key);
                    set.append(key, new BasicDBObject("$gt", value));
                }
                else {
                    log.err(this, "can not figure out " + key);
                    continue;
                }

            }
        }
    }

    /**
     * 
     * KVStrategy
     **/
    public class KVStrategy implements MongodbImplStrategy {

        @Override
        public void concretProcessor(Object expKey, Map expValue, BasicDBObject set) {

            Map KVMap = (Map) expValue.get(expKey);
            Set keyset = KVMap.keySet();
            Iterator iter = keyset.iterator();
            String key = null;
            BasicDBObject content = new BasicDBObject();
            while (iter.hasNext()) {
                key = (String) iter.next();
                content.append(key, KVMap.get(key));

            }
            switch (expKey.toString()) {
                case "set":
                    set.append("$set", content);
                    break;
                case "unset":
                    set.append("$unset", content);
                    break;
                case "rename":
                    set.append("$rename", content);
                    break;
                case "push":
                    set.append("$push", content);
                    break;
                case "pull":
                    set.append("$pull", content);
                    break;
                default:
                    set.append("$" + expKey.toString(), content);
                    break;

            }
            log.info(this, "set :" + set.toJson());
        }

    }
}
