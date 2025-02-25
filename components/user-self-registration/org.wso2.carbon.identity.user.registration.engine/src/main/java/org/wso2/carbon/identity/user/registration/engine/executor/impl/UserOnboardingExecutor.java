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

package org.wso2.carbon.identity.user.registration.engine.executor.impl;

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_USERSTORE_MANAGER_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleClientException;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_USERNAME_NOT_PROVIDED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_LOADING_USERSTORE_MANAGER;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_USER_ONBOARD_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ExecutorStatus.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.PASSWORD;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.registration.engine.executor.Executor;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegisteringUser;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;

public class UserOnboardingExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(UserOnboardingExecutor.class);

    @Override
    public ExecutorResponse execute(RegistrationContext context) throws RegistrationFrameworkException {

        String tenantDomain = context.getTenantDomain();
        updateUserProfile(context);
        RegisteringUser user = context.getRegisteringUser();
        UserStoreManager userStoreManager = getUserstoreManager(tenantDomain, context.getContextIdentifier());

        Map<String, String> credentials = user.getUserCredentials();

        String password =
                credentials.getOrDefault(PASSWORD, String.valueOf(new DefaultPasswordGenerator().generatePassword()));

        Map<String, Object> claims = user.getClaims();
        Map<String, String> userClaims = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            userClaims.put(entry.getKey(), entry.getValue().toString());
        }

        String username = Optional.ofNullable(user.getUsername()).orElseGet(() -> userClaims.get(USERNAME_CLAIM_URI));
        if (username == null) {
            throw handleClientException(ERROR_CODE_USERNAME_NOT_PROVIDED, context.getContextIdentifier());
        }
        user.setUsername(username);

        // TODO:  Identify the userdomain properly.
        try {
            userStoreManager.addUser(IdentityUtil.addDomainToName(user.getUsername(), "PRIMARY"), password, null,
                                     userClaims, null);
            String userid = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
            context.setUserId(userid);
            return new ExecutorResponse(STATUS_USER_CREATED);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_USER_ONBOARD_FAILURE, e, context.getContextIdentifier());
        }
    }

    private UserStoreManager getUserstoreManager(String tenantDomain, String flowId) throws RegistrationFrameworkException {

        RealmService realmService = RegistrationFlowEngineDataHolder.getInstance().getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_USERSTORE_MANAGER_FAILURE, e, tenantDomain, flowId);
        }
    }

    private void updateUserProfile(RegistrationContext context) {

        context.getUserInputData().forEach((key, value) -> {
            if (key.startsWith("http://wso2.org/claims/")) {
                if (!context.getRegisteringUser().getClaims().containsKey(key)) {
                    context.getRegisteringUser().addClaim(key, value);
                }
            }
        });
    }

    @Override
    public String getName() {

        return Constants.EXECUTOR_FOR_USER_ONBOARDING;
    }

    @Override
    public List<String> getInitiationData() {

        LOG.debug("Initiation data is not required for the executor: " + getName());
        return null;
    }
}
