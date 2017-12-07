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

package org.uavstack.resources.common.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.PackageHelper;
import com.creditease.agent.helpers.PropertiesHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.messaging.api.MessageConsumer;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingContext;
import com.creditease.uav.messaging.api.MessagingFactory;
import com.creditease.uav.mq.api.MQFactory;

public class StandardMessagingBuilder extends AbstractComponent {

    public StandardMessagingBuilder(String cName, String feature) {
        super(cName, feature);
    }

    // init first
    public void init(String handlerPackageName) throws IOException {

        Properties pro = null;

        pro = PropertiesHelper
                .loadPropertyFile(ConfigurationManager.getInstance().getContext(IConfigurationManager.CONFIGPATH)
                        + "msgtype2topic.properties");

        Map<String, String> map = getConfigOfMsgType2topicData(pro);
        String nameServer = ConfigurationManager.getInstance().getFeatureConfiguration(this.feature,
                "messagingnameserver");
        String[] consumerHandlerClasses = StringHelper.isEmpty(handlerPackageName) ? new String[] {}
                : PackageHelper.getHandlerName(handlerPackageName,
                        this.getConfigManager().getFeatureClassLoader(feature));

        long msgThreshold = DataConvertHelper.toLong(
                ConfigurationManager.getInstance().getResourceConfiguration("messageproducer", "messagingthreshold"),
                MessagingContext.DEFAULT_MESSAGE_THRESHOLD);

        long msgSizeLimit = DataConvertHelper.toLong(
                ConfigurationManager.getInstance().getResourceConfiguration("messageproducer", "messagesizelimit"),
                MessagingContext.DEFAULT_MSG_SIZE_LIMIT);

        this.log.info(this, "The message threshold is " + msgThreshold + " kb");

        MessagingContext.init(nameServer, map, consumerHandlerClasses, msgThreshold, msgSizeLimit);

        MessagingContext.setClassLoaders(this.getConfigManager().getFeatureClassLoader(feature));
    }

    public void init() throws IOException {

        init(null);
    }

    /**
     * build producer
     */
    public MessageProducer buildProducer() {

        return MessagingFactory
                .getMessageProducer(ConfigurationManager.getInstance().getContext(IConfigurationManager.NODEUUID));
    }

    /**
     * buildConsumer
     * 
     * @param groupName
     * @param msgType
     * @param queueType
     * @return
     */
    public MessageConsumer buildConsumer(String groupName, String msgType, MQFactory.QueueType queueType) {

        String ConsumeThreadMax = ConfigurationManager.getInstance().getFeatureConfiguration(feature,
                msgType + ".consumethreadmax");
        String ConsumeStopInterval = ConfigurationManager.getInstance().getFeatureConfiguration(feature,
                msgType + ".consumestopinterval");

        boolean check = DataConvertHelper.toBoolean(
                ConfigurationManager.getInstance().getFeatureConfiguration(feature, msgType + ".enable"), false);

        if (check == false) {
            return null;
        }

        String[] bizIDs = new String[1];
        bizIDs[0] = msgType;
        MessageConsumer consumer = MessagingFactory.createMessageConsumer(groupName, bizIDs,
                ConsumeThreadMax == null ? 10 : Integer.parseInt(ConsumeThreadMax),
                ConsumeStopInterval == null ? 0 : Long.parseLong(ConsumeStopInterval), queueType);

        return consumer;
    }

    /**
     * buildConsumer
     * 
     * @param msgType
     * @param queueType
     * @return
     */
    public MessageConsumer buildConsumer(String msgType, MQFactory.QueueType queueType) {

        return this.buildConsumer(msgType, msgType, queueType);
    }

    /**
     * buildConsumer
     * 
     * @param msgType
     * @return
     */
    public MessageConsumer buildConsumer(String msgType) {

        return buildConsumer(msgType, MQFactory.QueueType.QUEUE);
    }

    /**
     * read MsgType2topicData from properties to Map
     * 
     * @param pro
     * @return
     */
    private Map<String, String> getConfigOfMsgType2topicData(Properties pro) {

        Iterator<Entry<Object, Object>> it = pro.entrySet().iterator();
        Map<String, String> type2topic = new HashMap<String, String>();
        while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            String topicName = entry.getValue().toString();
            String msgType = entry.getKey().toString();

            type2topic.put(msgType, topicName);
        }
        return type2topic;
    }

}
