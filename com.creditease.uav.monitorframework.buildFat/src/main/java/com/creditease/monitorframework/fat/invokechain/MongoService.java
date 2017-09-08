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

package com.creditease.monitorframework.fat.invokechain;

import java.util.Arrays;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * 测试未与任何系统有交互的程序
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("mongo")
public class MongoService {

    /**
     * 测试用例
     * 
     * @return
     */
    @GET
    @Path("test")
    public String test() {

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

        // MongoClient client2 = new MongoClient("localhost:27017");
        // db = client2.getDatabase("apphubDataStore");
        // db.listCollectionNames().first();
        // collection = db.getCollection("test");
        // collection.listIndexes().first();

        client.close();
        // client2.close();
        return "mongo perfect";
    }

}
