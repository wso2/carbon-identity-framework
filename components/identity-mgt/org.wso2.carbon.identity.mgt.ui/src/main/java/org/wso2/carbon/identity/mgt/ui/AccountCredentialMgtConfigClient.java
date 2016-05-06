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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceStub;
import org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO;

import java.util.HashMap;
import java.util.Map;


public class AccountCredentialMgtConfigClient {

    protected static Log log = LogFactory
            .getLog(AccountCredentialMgtConfigClient.class);
    protected AccountCredentialMgtConfigServiceStub stub;

    public AccountCredentialMgtConfigClient(String url,
                                            ConfigurationContext configContext) throws AxisFault{
        try {
            stub = new AccountCredentialMgtConfigServiceStub(configContext, url
                    + "AccountCredentialMgtConfigService");
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    public AccountCredentialMgtConfigClient(String cookie, String url,
                                            ConfigurationContext configContext) throws AxisFault {
        try {
            stub = new AccountCredentialMgtConfigServiceStub(configContext, url
                    + "AccountCredentialMgtConfigService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(
                    org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                    cookie);
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    private String[] handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public void saveEmailConfig(EmailConfigDTO emailConfig) throws AxisFault {
        try {
            EmailTemplateDTO[] emailTemplates = emailConfig.getTemplates();
            stub.saveEmailConfig(emailTemplates);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public EmailConfigDTO loadEmailConfig() throws AxisFault {
        EmailTemplateDTO[] emailTemplates = null;
        EmailConfigDTO emailConfig = new EmailConfigDTO();
        try {
            emailTemplates = stub.getEmailConfig();
            Map<String, String> emailTypes = new HashMap<String, String>();
            for (int i = 0; i < emailTemplates.length; i++) {
                emailTypes.put(emailTemplates[i].getName(), emailTemplates[i].getDisplayName());
            }
            emailConfig.setEmailTypes(emailTypes);
            emailConfig.setTemplates(emailTemplates);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return emailConfig;
    }
}
