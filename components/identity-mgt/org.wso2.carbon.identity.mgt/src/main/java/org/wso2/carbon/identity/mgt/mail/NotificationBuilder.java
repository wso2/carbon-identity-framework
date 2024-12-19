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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class NotificationBuilder {

    private NotificationBuilder() {
    }

    public static Notification createNotification(String notificationType, String template, NotificationData data)
            throws IdentityMgtServiceException {

        String subject = null;
        String body = null;
        String footer = null;
        Notification notificatoin = null;

        if ("EMAIL".equals(notificationType)) {
            String[] contents = template.split("\\|");

            if (contents.length > 3) {
                throw new IdentityMgtServiceException("Contents must be 3 or less");
            }

            subject = contents[0];
            body = contents[1];
            if (contents.length > 2) {
                // Ignore footer assignment when footer value is empty in the template.
                footer = contents[2];
            }

//			Replace all the tags in the NotificationData.
            Map<String, String> tagsData = data.getTagsData();
            try {
                subject = replaceTags(tagsData, subject);
                body = replaceTags(tagsData, body);
                if (StringUtils.isNotBlank(footer)) {
                    footer = replaceTags(tagsData, footer);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IdentityMgtServiceException("Unsupported encoding while creating notification", e);
            }
            notificatoin = new EmailNotification();
            notificatoin.setSubject(subject);
            notificatoin.setBody(body);
            notificatoin.setFooter(footer);
            notificatoin.setSendFrom(data.getSendFrom());
            notificatoin.setSendTo(data.getSendTo());

        }
        return notificatoin;
    }

    private static String replaceTags(Map<String, String> tagsData, String content)
            throws UnsupportedEncodingException {
        for (Map.Entry<String, String> entry : tagsData.entrySet()) {

            String data = entry.getValue();
            String key = entry.getKey();
            if (data != null) {
                content = content.replaceAll("\\{url:" + key + "\\}",
                        URLEncoder.encode(tagsData.get(key), "UTF-8"));
                content = content.replaceAll("\\{" + key + "\\}", tagsData.get(key));
            } else {
                content = content.replaceAll("\\{url:" + key + "\\}", "");
                content = content.replaceAll("\\{" + key + "\\}", "");
            }
        }
        return content;
    }
}
