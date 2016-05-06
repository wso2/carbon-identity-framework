/*
 *
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

package org.wso2.carbon.identity.mgt.ui;

import org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO;

import java.util.Map;

/**
 * GUI class to hold the reference data for email types and templates
 */
public class EmailConfigDTO {

    private Map<String, String> emailTypes;
    private EmailTemplateDTO[] templates;

    public Map<String, String> getEmailTypes() {
        return emailTypes;
    }

    public void setEmailTypes(Map<String, String> emailTypes) {
        this.emailTypes = emailTypes;
    }

    public EmailTemplateDTO[] getTemplates() {
        return templates.clone();
    }

    //	TODO - Are these needed?
    public void setTemplates(EmailTemplateDTO[] templates) {
        this.templates = templates.clone();
    }

    /**
     * This method is used to save the current displayed email template changes to the
     * Configuration object.
     *
     * @param emailTemplate
     */
    public void setTemplate(EmailTemplateDTO emailTemplate) {

        if (emailTemplate != null) {
            for (int i = 0; i < templates.length; i++) {
                EmailTemplateDTO template = templates[i];
                if (emailTemplate.getName().equals(template.getName())) {
                    templates[i] = emailTemplate;
                }
            }
        }
    }

    /**
     * This method is used to get the email template to be displayed with the given index.
     */
    public EmailTemplateDTO getTemplate(int index) {

        EmailTemplateDTO template = null;

        if (index <= templates.length) {
            template = templates[index];
        }

        return template;
    }

}
