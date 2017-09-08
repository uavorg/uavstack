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

package com.creditease.uav.hook.mongoclients;

import java.util.Arrays;
import java.util.Collections;

import org.bson.Document;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DoTestMongoClientProxy {

    public static void main(String args[]) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        MongoClientHookProxy p = new MongoClientHookProxy("test", Collections.emptyMap());

        p.doInstallDProxy(null, "testApp");

        MongoClient client = new MongoClient();
        client.listDatabaseNames().first();
        MongoDatabase db = client.getDatabase("apphubDataStore");
        db.listCollectionNames().first();
        MongoCollection<Document> collection = db.getCollection("test");
        collection.listIndexes().first();
        Document doc = new Document("name", "Amarcord Pizzeria")
                .append("contact",
                        new Document("phone", "264-555-0193").append("email", "amarcord.pizzeria@example.net")
                                .append("location", Arrays.asList(-73.88502, 40.749556)))
                .append("stars", 2).append("categories", Arrays.asList("Pizzeria", "Italian", "Pasta"));
        collection.insertOne(doc);
        collection.find().first();

        MongoClient client2 = new MongoClient("localhost:27017");
        db = client2.getDatabase("apphubDataStore");
        db.listCollectionNames().first();
        collection = db.getCollection("test");
        collection.listIndexes().first();

        client.close();
        client2.close();
    }
}
