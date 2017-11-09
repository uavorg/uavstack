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

import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.IActionEngine;

public class JavaMailAction extends AbstractMailAction {

    private String smtpHost = "";
    private String smtpPort = "25";
    private String username = "";
    private String password = "";

    public JavaMailAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);

        smtpHost = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.javamail.smtphost");
        smtpPort = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.javamail.smtpport");
        username = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.javamail.username");
        password = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.javamail.password");
    }

    private MimeMessage buildMailMessage(Session session, NotificationEvent notifyEvent, String title,
            String mailTemplatePath) throws MessagingException {

        MimeMessage message = new MimeMessage(session);

        // 邮件标题
        message.setSubject(title);

        String html = IOHelper.readTxtFile(mailTemplatePath, "utf-8");
        html = buildMailBody(html, notifyEvent);
        // 正文
        message.setContent(html, "text/html;charset=utf-8");
        // 发件人
        message.setFrom(username);

        String addressStr = notifyEvent.getArg(cName);
        String[] toAddr = addressStr.split(",");
        Address[] receiver = new Address[toAddr.length];
        int i = 0;
        for (String addr : toAddr) {
            if (!StringHelper.isEmpty(addr)) {
                receiver[i] = new InternetAddress(addr);
                i++;
            }
        }
        // 收件人
        message.setRecipients(MimeMessage.RecipientType.TO, receiver);

        message.saveChanges();

        return message;
    }

    @Override
    public boolean sendMail(String title, String mailTemplatePath, NotificationEvent event) {

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtpHost);
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.auth", "true");

        Transport ts = null;
        try {
            Session session = Session.getInstance(props);
            ts = session.getTransport();
            ts.connect(username, password);
            MimeMessage message = buildMailMessage(session, event, title, mailTemplatePath);
            ts.sendMessage(message, message.getAllRecipients());
            return true;
        }
        catch (Exception e) {
            log.err(this, "Send Mail FAIL.", e);
        }
        finally {
            if (null != ts) {
                try {
                    ts.close();
                }
                catch (MessagingException e) {
                    // ignore
                }
            }
        }
        return false;
    }

}
