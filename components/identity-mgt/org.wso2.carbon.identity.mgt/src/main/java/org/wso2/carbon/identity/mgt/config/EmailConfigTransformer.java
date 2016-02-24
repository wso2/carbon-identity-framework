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

package org.wso2.carbon.identity.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.EmailTemplateDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This class is used to transform <code>Properties</code> to <code>EmailTemplateDTO</code>
 * and vice versa.
 */
public class EmailConfigTransformer {

    private EmailConfigTransformer() {
    }

    private static final Log log = LogFactory.getLog(EmailConfigTransformer.class);
    
    public static EmailTemplateDTO[] transform(Properties props) throws IdentityException {

        List<EmailTemplateDTO> emailTemplates = new ArrayList<EmailTemplateDTO>();

        if (log.isDebugEnabled()) {
            log.debug("Transforming Properties to EmailTemplateDTO[]");
        }
        
        Set<String> keySet = props.stringPropertyNames();
        for (String key : keySet) {

            EmailTemplateDTO template = new EmailTemplateDTO();
            
            if (IdentityMgtConstants.Notification.PASSWORD_RESET_RECOVERY.equals(key)) {
                template.setDisplayName("Password Reset");
            } else if (IdentityMgtConstants.Notification.ACCOUNT_CONFORM.equals(key)) {
                template.setDisplayName("Account Confirm");
            } else if (IdentityMgtConstants.Notification.ACCOUNT_ID_RECOVERY.equals(key)) {
                template.setDisplayName("Account Id Recovery");
            } else if (IdentityMgtConstants.Notification.ACCOUNT_UNLOCK.equals(key)) {
                template.setDisplayName("Account Unlock");
            } else if (IdentityMgtConstants.Notification.ASK_PASSWORD.equals(key)) {
                template.setDisplayName("Ask Password");
            } else if (IdentityMgtConstants.Notification.OTP_PASSWORD.equals(key)) {
                template.setDisplayName("One Time Password");
            } else if (IdentityMgtConstants.Notification.TEMPORARY_PASSWORD.equals(key)) {
                template.setDisplayName("Temporary Password");
            } else {
                // Ignore all other keys in the registry mount.
                continue;
            }

            template.setName(key);

            String[] contents = props.getProperty(key).split("\\|");

            if (contents.length > 3) {
                throw IdentityException.error("Cannot have | character in the template");
            }

            String subject = contents[0];
            String body = contents[1];
            String footer = contents[2];

            if (log.isDebugEnabled()) {
                log.debug("Template info - name:" + key + " subject:" + subject + " " + "body:"
                        + body + " footer:" + footer);
            }
            
            template.setSubject(subject);
            template.setBody(body);
            template.setFooter(footer);

            emailTemplates.add(template);
        }

        return emailTemplates.toArray(new EmailTemplateDTO[emailTemplates.size()]);
    }

    public static Properties transform(EmailTemplateDTO[] templates) throws IdentityException {

        if (log.isDebugEnabled()) {
            log.debug("Transforming EmailTemplateDTO[] to Properties");
        }

        Properties props = new Properties();

        for (EmailTemplateDTO template : templates) {
            StringBuilder contents = new StringBuilder();

            if (log.isDebugEnabled()) {
                log.debug("Properties info - subject:" + template.getSubject() + " " + "body:"
                        + template.getBody() + " footer:" + template.getFooter());
            }

            contents.append(template.getSubject()).append("|").append(template.getBody())
                    .append("|").append(template.getFooter());

            props.setProperty(template.getName(), contents.toString());
        }
        return props;
    }
}
