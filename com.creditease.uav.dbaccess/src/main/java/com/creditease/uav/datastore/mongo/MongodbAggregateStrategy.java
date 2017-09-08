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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.mongo.MongodbImplStrategy.QueryStrategy;
import com.mongodb.BasicDBObject;

/**
 * @author peihua
 */

@SuppressWarnings("rawtypes")
public interface MongodbAggregateStrategy {

    static final ISystemLogger log = SystemLogger.getLogger(MongodbAggregateStrategy.class);

    public void concretProcessor(Object key, Map elemData, List<Bson> list);

    public class UnwindStrategy implements MongodbAggregateStrategy {

        @Override
        public void concretProcessor(Object key, Map elemData, List<Bson> list) {

            String unwindValue = elemData.get(key).toString();
            list.add(new BasicDBObject("$unwind", "$" + unwindValue));
        }
    }

    public class GroupStrategy implements MongodbAggregateStrategy {

        @Override
        public void concretProcessor(Object key, Map elemData, List<Bson> list) {

            Object value = elemData.get(key);
            if (value instanceof String) {
                String viewValue = value.toString();
                list.add(new BasicDBObject("$group", new BasicDBObject("_id", "$_id").append(DataStoreProtocol.RESULT,
                        new BasicDBObject("$push", "$" + viewValue))));
            }
            else if (value instanceof Map) {
                Map groupCondition = (Map) elemData.get(key);
                Set keys = groupCondition.keySet();
                Iterator iter = keys.iterator();
                while (iter.hasNext()) {
                    String viewKey = (String) iter.next();
                    String viewValue = groupCondition.get(viewKey).toString();
                    if (DataStoreProtocol.DISTINCT.equals(viewKey)) {
                        list.add(new BasicDBObject("$group", new BasicDBObject("_id", null)
                                .append(DataStoreProtocol.RESULT, new BasicDBObject("$addToSet", "$" + viewValue))));
                    }
                }
            }
        }
    }

    public class ProjectStrategy implements MongodbAggregateStrategy {

        @Override
        public void concretProcessor(Object key, Map elemData, List<Bson> list) {

            if (null == key && null == elemData) {
                list.add(new BasicDBObject("$project",
                        new BasicDBObject("_id", 0).append(DataStoreProtocol.RESULT, "$" + DataStoreProtocol.RESULT)));
            }
            else {
                Document filterBson = new Document();
                filterBson.append("_id", 0);
                String fileds = (String) elemData.get(DataStoreProtocol.FIELDS);
                if (!StringHelper.isEmpty(fileds)) {
                    String[] filters = fileds.split(";");
                    for (String filter : filters) {
                        filterBson.append(filter, 1);
                    }
                }

                list.add(new BasicDBObject("$project", filterBson));
            }
        }
    }

    public class MatchStrategy implements MongodbAggregateStrategy {

        @Override
        public void concretProcessor(Object key, Map elemData, List<Bson> list) {

            BasicDBObject query = new BasicDBObject();
            QueryStrategy qry = new QueryStrategy();
            qry.concretProcessor(key, elemData, query);
            list.add(new BasicDBObject("$match", query));
        }

    }
}
