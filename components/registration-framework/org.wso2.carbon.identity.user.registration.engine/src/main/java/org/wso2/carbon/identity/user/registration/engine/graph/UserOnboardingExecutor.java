/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.graph;

import static java.util.Locale.ENGLISH;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_USERNAME;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USERNAME_ALREADY_EXISTS;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USERNAME_NOT_PROVIDED;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USERSTORE_MANAGER_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_USER_ONBOARD_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.USER_ALREADY_EXISTING_USERNAME;
import static org.wso2.carbon.identity.user.registration.engine.Constants.PASSWORD_KEY;
import static org.wso2.carbon.identity.user.registration.engine.Constants.SELF_REGISTRATION_DEFAULT_USERSTORE_CONFIG;
import static org.wso2.carbon.identity.user.registration.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleClientException;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ExecutorTypes.USER_ONBOARDING;
import static org.wso2.carbon.user.core.UserCoreConstants.APPLICATION_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.WORKFLOW_DOMAIN;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegisteringUser;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;

/**
 * Implementation of the executor which handles onboarding the user to the system.
 */
public class UserOnboardingExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(UserOnboardingExecutor.class);
    private static final String WSO2_CLAIM_DIALECT = "http://wso2.org/claims/";

    @Override
    public String getName() {

        return USER_ONBOARDING;
    }

    @Override
    public List<String> getInitiationData() {

        LOG.debug("Initiation data is not required for the executor: " + getName());
        return null;
    }

    @Override
    public ExecutorResponse execute(RegistrationContext context) throws RegistrationEngineException {

        RegisteringUser user = updateUserProfile(context);
        Map<String, String> userClaims = user.getClaims();
        Map<String, char[]> credentials = user.getUserCredentials();
        char[] password =
                credentials.getOrDefault(PASSWORD_KEY, new DefaultPasswordGenerator().generatePassword());

        try {
            String userStoreDomainName = resolveUserStoreDomain(user.getUsername());
            UserStoreManager userStoreManager = getUserStoreManager(context.getTenantDomain(), userStoreDomainName,
                                                                    context.getContextIdentifier());
            userStoreManager.addUser(IdentityUtil.addDomainToName(user.getUsername(), userStoreDomainName),
                                     String.valueOf(password), null, userClaims, null);
            String userid = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
            context.setUserId(userid);
            return new ExecutorResponse(STATUS_USER_CREATED);
        } catch (UserStoreException e) {
            if (e.getMessage().contains(USER_ALREADY_EXISTING_USERNAME)) {
                throw handleClientException(ERROR_CODE_USERNAME_ALREADY_EXISTS, context.getTenantDomain());
            }
            throw handleServerException(ERROR_CODE_USER_ONBOARD_FAILURE, e, user.getUsername(),
                                        context.getContextIdentifier());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private RegisteringUser updateUserProfile(RegistrationContext context) throws RegistrationEngineException {

        RegisteringUser user = context.getRegisteringUser();
        context.getUserInputData().forEach((key, value) -> {
            if (key.startsWith(WSO2_CLAIM_DIALECT)) {
                if (!user.getClaims().containsKey(key)) {
                    user.addClaim(key, value);
                }
            }
        });
        user.setUsername(resolveUsername(user, context.getContextIdentifier()));
        return user;
    }

    private UserStoreManager getUserStoreManager(String tenantDomain, String userdomain, String flowId)
            throws RegistrationEngineException {

        RealmService realmService = RegistrationFlowEngineDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager;
        try {
            UserRealm tenantUserRealm = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain));
            if (IdentityUtil.getPrimaryDomainName().equals(userdomain)) {
                userStoreManager = (UserStoreManager) tenantUserRealm.getUserStoreManager();
            } else {
                userStoreManager =
                        ((UserStoreManager) tenantUserRealm.getUserStoreManager()).getSecondaryUserStoreManager(userdomain);
            }
            if (userStoreManager == null) {
                throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, tenantDomain, flowId);
            }
            return userStoreManager;
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, e, tenantDomain, flowId);
        }
    }

    private String resolveUserStoreDomain(String username) {

        int separatorIndex = username.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        if (separatorIndex >= 0) {
            String domain = username.substring(0, separatorIndex);
            if (INTERNAL_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)
                    || APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                return domain.substring(0, 1).toUpperCase(ENGLISH) + domain.substring(1).toLowerCase(ENGLISH);
            }
            return domain.toUpperCase(ENGLISH);
        }

        String domainName = IdentityUtil.getProperty(SELF_REGISTRATION_DEFAULT_USERSTORE_CONFIG);
        return domainName != null ? domainName.toUpperCase(ENGLISH) :
                IdentityUtil.getPrimaryDomainName().toUpperCase(ENGLISH);
    }

    private String resolveUsername(RegisteringUser user, String flowId) throws RegistrationEngineException {

        String username = Optional.ofNullable(user.getUsername())
                .orElseGet(() -> Optional.ofNullable(user.getClaims().get(USERNAME_CLAIM_URI)).orElse(""));
        if (StringUtils.isBlank(username)) {
            throw handleClientException(ERROR_CODE_USERNAME_NOT_PROVIDED, flowId);
        }
        if (IdentityUtil.isEmailUsernameEnabled() && !username.contains("@")) {
            throw handleClientException(ERROR_CODE_INVALID_USERNAME, username);
        }
        return username;
    }
}
