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

package com.creditease.agent.feature.notifycenter.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.mspl.domain.BizProcess;
import com.creditease.mspl.event.vo.EmailEntity;
import com.creditease.mspl.event.vo.EmailEvent;

/**
 * 
 * @author pengfei
 * @since 20160520
 *
 */
public class CEMailAction extends AbstractMailAction {

    private static List<String> nameList = new LinkedList<String>();

    static {
        nameList.add("ip");
        nameList.add("host");
        nameList.add("component");
        nameList.add("nodename");
        nameList.add("feature");
        nameList.add("nodeuuid");
    }

    // default value, read from configuration
    private String userName = "";
    private String password = "";
    private String brokerURL = "tcp://127.0.0.1:61616";

    private String queueName = "Mail.Receive.queue";

    private String activeID = "ACTIVE-20150429-00001";
    private String systemSign = "1";

    public CEMailAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);

        userName = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.username");
        password = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.password");
        brokerURL = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.brokerurl");

        activeID = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.activeid");
        systemSign = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.systemsign");
    }

    /**
     * 装载消息
     * 
     * @param notifyEvent
     * @return EmailEvent
     * @throws Exception
     */
    private EmailEvent buildEmailEvent(NotificationEvent notifyEvent, String title, String mailTemplatePath)
            throws Exception {

        List<EmailEntity> list = new ArrayList<EmailEntity>();

        EmailEntity emailEntity = new EmailEntity();

        /**
         * 如果notifyEvent中含有目的地址，则用该地址，否则使用默认地址
         */
        String address = notifyEvent.getArg(cName);

        if (StringHelper.isEmpty(address)) {
            if (log.isTraceEnable()) {
                log.warn(this, "Send Mail FAIL as no any email addresses");
            }
            return null;
        }
        emailEntity.setToAddress(address);
        String html = IOHelper.readTxtFile(mailTemplatePath, "utf-8");

        if (StringHelper.isEmpty(html)) {
            log.err(this, "Send Mail FAIL as mail template is empty");
            return null;
        }

        /** 变量替换 */
        html = buildMailBody(html, notifyEvent);
        /** 主题 */
        emailEntity.setSubject(title);
        /** 正文 */
        emailEntity.setContent(html);
        /** 添加附件 */
        list.add(emailEntity);

        /** 配置信息 */
        EmailEvent emailEvent = new EmailEvent();
        BizProcess bizProcess = new BizProcess();
        bizProcess.setDealCode("mspl_ltn_0107");
        emailEvent.setBizProcess(bizProcess);
        emailEvent.setUserList(list);
        emailEvent.setActivityId(activeID);
        emailEvent.setChannelCode("channel 001");
        emailEvent.setMailType("消息类22");
        emailEvent.setSystemSign(systemSign);
        return emailEvent;
    }

    @Override
    public boolean sendMail(String title, String mailTemplatePath, NotificationEvent notifyEvent) {

        if (log.isDebugEnable()) {
            log.debug(this, "Send Mail START: event=" + notifyEvent.toJSONString());
        }

        // ConnectionFactory ：连接工厂，JMS 用它创建连接
        ActiveMQConnectionFactory connectionFactory;
        // Connection ：JMS 客户端到JMS Provider 的连接
        Connection connection = null;
        // Session： 一个发送或接收消息的线程
        Session session = null;
        // Destination ：消息的目的地;消息发送给谁.
        Queue destination;
        // MessageProducer：消息发送者
        javax.jms.MessageProducer producer = null;
        // TextMessage message;
        // 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
        connectionFactory = new ActiveMQConnectionFactory(userName, password, brokerURL);
        try {
            // 构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
            // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
            destination = session.createQueue(queueName);
            // 得到消息生成者【发送者】
            producer = session.createProducer(destination);
            // 设置不持久化，此处学习，实际根据项目决定
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // 构造消息，此处写死，项目就是参数，或者方法获取
            EmailEvent emailEvent = buildEmailEvent(notifyEvent, title, mailTemplatePath);

            if (emailEvent == null) {
                return false;
            }

            // 序列化
            ActiveMQObjectMessage message = (ActiveMQObjectMessage) session.createObjectMessage();
            message.setObject(emailEvent);

            // 发送消息到目的地方
            producer.send(message);

            session.commit();

            return true;
        }
        catch (Exception e) {
            log.err(this, "Send Mail FAIL.", e);

        }
        finally {

            try {
                if (producer != null) {
                    producer.close();
                }
            }
            catch (Throwable e) {
                // ignore
            }

            try {
                if (session != null) {
                    session.close();
                }
            }
            catch (Throwable e) {
                // ignore
            }

            try {
                if (null != connection)
                    connection.close();
            }
            catch (Throwable e) {
                // ignore
            }
        }
        return false;
    }

}
