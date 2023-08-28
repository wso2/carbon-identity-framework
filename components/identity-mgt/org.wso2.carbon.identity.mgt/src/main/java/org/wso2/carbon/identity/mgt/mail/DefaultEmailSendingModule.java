/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.mail;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.CarbonConfigurationContextFactory;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Default email sending implementation
 */
public class DefaultEmailSendingModule extends AbstractEmailSendingModule {

    public static final String CONF_STRING = "confirmation";
    private static final String SEND_MAIL_PROPERTY = "mailto:";
    private static Log log = LogFactory.getLog(DefaultEmailSendingModule.class);
    private BlockingQueue<Notification> notificationQueue = new LinkedBlockingDeque<Notification>();

    /**
     * Replace the {user-parameters} in the config file with the respective
     * values
     *
     * @param text           the initial text
     * @param userParameters mapping of the key and its value
     * @return the final text to be sent in the email
     */
    public static String replacePlaceHolders(String text, Map<String, String> userParameters) {
        if (userParameters != null) {
            for (Map.Entry<String, String> entry : userParameters.entrySet()) {
                String key = entry.getKey();
                if (key != null && entry.getValue() != null) {
                    text = text.replaceAll("\\{" + key + "\\}", entry.getValue());
                }
            }
        }
        return text;
    }

    @Override
    public void sendEmail() {

        Map<String, String> headerMap = new HashMap<String, String>();

        try {
            Notification notification = notificationQueue.take();
            if (notification == null) {
                throw new IllegalStateException("Notification not set. " +
                        "Please set the notification before sending messages");
            }
            PrivilegedCarbonContext.startTenantFlow();
            if (notificationData != null) {
                String tenantDomain = notificationData.getDomainName();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantDomain(tenantDomain, true);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("notification data not found. Tenant might not be loaded correctly");
                }
            }

            headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, notification.getSubject());

            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            StringBuilder contents = new StringBuilder();
            contents.append(notification.getBody())
                    .append(System.getProperty("line.separator"))
                    .append(System.getProperty("line.separator"))
                    .append(notification.getFooter());
            payload.setText(contents.toString());
            ServiceClient serviceClient;
            ConfigurationContext configContext = CarbonConfigurationContextFactory
                    .getConfigurationContext();
            if (configContext != null) {
                serviceClient = new ServiceClient(configContext, null);
            } else {
                serviceClient = new ServiceClient();
            }
            Options options = new Options();
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
            options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                    MailConstants.TRANSPORT_FORMAT_TEXT);
            options.setTo(new EndpointReference(SEND_MAIL_PROPERTY + notification.getSendTo()));
            serviceClient.setOptions(options);
            String recipient = LoggerUtils.isLogMaskingEnable ?
                    LoggerUtils.getMaskedContent(notification.getSendTo()) : notification.getSendTo();
            log.info("Sending an email notification to " + recipient);
            serviceClient.fireAndForget(payload);
            
            if (log.isDebugEnabled()) {
                log.debug("Email content : " + notification.getBody());
            }
            log.info("Email notification has been sent to " + recipient);
        } catch (AxisFault axisFault) {
            log.error("Failed Sending Email", axisFault);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting until an element becomes available in the notification queue.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    public String getRequestMessage(EmailConfig emailConfig) {

        StringBuilder msg;
        String targetEpr = emailConfig.getTargetEpr();
        if (emailConfig.getEmailBody().length() == 0) {
            msg = new StringBuilder(EmailConfig.DEFAULT_VALUE_MESSAGE);
            msg.append("\n");
            if (notificationData.getNotificationCode() != null) {

                msg.append(targetEpr).append("?").append(CONF_STRING).append(notificationData
                        .getNotificationCode()).append("\n");
            }
        } else {
            msg = new StringBuilder(emailConfig.getEmailBody());
            msg.append("\n");
        }
        if (emailConfig.getEmailFooter() != null) {
            msg.append("\n").append(emailConfig.getEmailFooter());
        }
        return msg.toString();
    }

    @Override
    public Notification getNotification() {
        return notificationQueue.peek();
    }

    @Override
    public void setNotification(Notification notification) {
        notificationQueue.add(notification);
    }

}
