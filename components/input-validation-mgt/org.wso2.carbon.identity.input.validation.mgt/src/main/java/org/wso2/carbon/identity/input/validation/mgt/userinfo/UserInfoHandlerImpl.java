/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.input.validation.mgt.userinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.EmailFormatValidator;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementServiceImpl;
import org.wso2.carbon.utils.multitenancy.userinfo.UserInfoHandler;

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;

/**
 * This class implemented to use username input validation.
 * TenantQualifiedURl option should be enabled along with this implementation.
 */
public class UserInfoHandlerImpl implements UserInfoHandler {

    private static final Log LOG = LogFactory.getLog(UserInfoHandlerImpl.class);
    private static final String TENANT_NAME_FROM_CONTEXT = "TenantNameFromContext";
    private final InputValidationManagementService inputValidationMgtService =
            new InputValidationManagementServiceImpl();

    /**
     * Since thread local properties has tenant domain context, we can retrieve the tenant from the context.
     * If the username is tenant qualified, tenant domain will be filtered out from username.
     *
     * @param username Username.
     * @return TenantAwareUsername.
     */
    @Override
    public String getTenantAwareUsername(String username) {

        String tenantDomain = getTenantDomainFromContext();
        String emailDomain = username.substring(username.lastIndexOf('@') + 1);

        // Check if the tenant domain matches the email domain
        if (tenantDomain.equalsIgnoreCase(emailDomain)) {
            boolean isEmailTypeUserName = isEmailAsUserName(tenantDomain);
            int lastAtSymbolIndex = username.lastIndexOf('@');

            // If it's not an email type username or there are multiple '@' symbols, return the modified username
            if (!isEmailTypeUserName || username.indexOf('@') != lastAtSymbolIndex) {
                return username.substring(0, lastAtSymbolIndex);
            }
        }
        // Return the username as is
        return username;
    }

    /**
     * Since thread local properties has tenant domain context, we can retrieve the tenant from the context.
     *
     * @param username Username.
     * @return TenantAwareUsername.
     */
    @Override
    public String getTenantDomain(String username) {

        return getTenantDomainFromContext().toLowerCase();
    }

    /**
     * Retrieves loaded tenant domain from carbon context.
     *
     * @return tenant domain of the request is being served.
     */
    private String getTenantDomainFromContext() {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT) != null) {
            tenantDomain = (String) IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT);
        }
        return tenantDomain;
    }

    private boolean isEmailAsUserName(String tenantDomain) {

        List<ValidationConfiguration> configurations;
        boolean isEmailAsUsername = false;
        try {
            configurations = inputValidationMgtService.getInputValidationConfiguration(tenantDomain);
            ValidationConfiguration configurationList = configurations.stream().filter(config ->
                    USERNAME.equalsIgnoreCase(config.getField())).collect(Collectors.toList()).get(0);

            /* If configuration for username field is found in Input Validation Mgt service, validate against them,
             if not validate against the regex from the userStore. */
            if (configurationList != null && configurationList.getRules() != null &&
                    !configurationList.getRules().isEmpty()) {
                for (RulesConfiguration configuration : configurationList.getRules()) {
                    if (EmailFormatValidator.class.getSimpleName().equals(configuration.getValidatorName())) {
                        isEmailAsUsername = true;
                        break;
                    }
                }
            }
            return isEmailAsUsername;

        } catch (InputValidationMgtException e) {
            LOG.error(new InputValidationMgtException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(), e.getMessage(),
                    e.getDescription()), e);
            return false;
        }
    }
}
