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

package com.creditease.uav.mq.api;


public class MQConsumerConfig {

    private Integer pullBatchSize;
    private Integer consumeThreadMax;

    private Integer consumeThreadMin;
    private String comsumerGroup;
    private String namingServer;
    private MQFactory.EngineType engineType;

    public MQConsumerConfig() {
        // 默认 rocketmq
        this.engineType = MQFactory.EngineType.ROCKETMQ;
    }

    public MQConsumerConfig(MQFactory.EngineType engineType) {
        this.engineType = engineType;
    }

    public MQFactory.EngineType getEngineType() {

        return engineType;
    }

    public Integer getConsumeThreadMax() {

        return consumeThreadMax;
    }

    public void setConsumeThreadMax(Integer consumeThreadMax) {

        this.consumeThreadMax = consumeThreadMax;
    }

    public Integer getConsumeThreadMin() {

        return consumeThreadMin;
    }

    public void setConsumeThreadMin(Integer consumeThreadMin) {

        this.consumeThreadMin = consumeThreadMin;
    }

    public String getComsumerGroup() {

        return comsumerGroup;
    }

    public void setComsumerGroup(String comsumerGroup) {

        this.comsumerGroup = comsumerGroup;
    }

    public String getNamingServer() {

        return namingServer;
    }

    public void setNamingServer(String namingServer) {

        this.namingServer = namingServer;
    }

    public Integer getPullBatchSize() {

        return pullBatchSize;
    }

    public void setPullBatchSize(Integer pullBatchSize) {

        this.pullBatchSize = pullBatchSize;
    }

}
