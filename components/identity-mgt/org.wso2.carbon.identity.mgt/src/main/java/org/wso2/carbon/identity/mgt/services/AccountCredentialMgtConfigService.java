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

package org.wso2.carbon.identity.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.config.Config;
import org.wso2.carbon.identity.mgt.config.ConfigBuilder;
import org.wso2.carbon.identity.mgt.config.ConfigType;
import org.wso2.carbon.identity.mgt.config.EmailConfigTransformer;
import org.wso2.carbon.identity.mgt.config.EmailNotificationConfig;
import org.wso2.carbon.identity.mgt.config.StorageType;
import org.wso2.carbon.identity.mgt.dto.EmailTemplateDTO;

import java.util.Properties;

/**
 * This service is to configure the Account and Credential Management
 * functionality.
 * @deprecated use the rest API implementation available in
 * org.wso2.carbon.identity.rest.api.server.email.template.v1.core.ServerEmailTemplatesService instead.
 */
public class AccountCredentialMgtConfigService {

    private static final Log log = LogFactory.getLog(AccountCredentialMgtConfigService.class);

    /**
     * This method is used to save the Email template configurations which is specific to tenant.
     *
     * @param emailTemplates - Email templates to be saved.
     * @throws IdentityMgtServiceException
     */
    public void saveEmailConfig(EmailTemplateDTO[] emailTemplates)
            throws IdentityMgtServiceException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getTenantId();
        EmailNotificationConfig emailConfig = new EmailNotificationConfig();
        ConfigBuilder configBuilder = ConfigBuilder.getInstance();

        try {
            Properties props = EmailConfigTransformer.transform(emailTemplates);
            emailConfig.setProperties(props);

            configBuilder.saveConfiguration(StorageType.REGISTRY, tenantId,
                                            emailConfig);
        } catch (Exception e) {
            log.error("Error occurred while saving email configuration", e);
            throw new IdentityMgtServiceException("Error occurred while saving email configuration");
        }
    }

    /**
     * This method is used to load the tenant specific Email template configurations.
     *
     * @return an array of templates.
     * @throws IdentityMgtServiceException
     */
    public EmailTemplateDTO[] getEmailConfig() throws IdentityMgtServiceException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getTenantId();
        Config emailConfig = null;
        EmailTemplateDTO[] templates = null;
        ConfigBuilder configBuilder = ConfigBuilder.getInstance();
        try {
            emailConfig = configBuilder.loadConfiguration(ConfigType.EMAIL,
                                                          StorageType.REGISTRY, tenantId);
            if (emailConfig != null) {

                templates = EmailConfigTransformer.transform(emailConfig.getProperties());
            }
        } catch (Exception e) {
            log.error("Error occurred while loading email configuration", e);
            throw new IdentityMgtServiceException("Error occurred while loading email configuration");
        }

        return templates;
    }
}
