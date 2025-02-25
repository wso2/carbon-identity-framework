/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.internal.component;

import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;

/**
 * Service component Holder for the Action management.
 */
public class ActionMgtServiceComponentHolder {

    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private RuleManagementService ruleManagementService;

    public static final ActionMgtServiceComponentHolder INSTANCE = new ActionMgtServiceComponentHolder();

    private ActionMgtServiceComponentHolder() {

    }

    /**
     * Get the instance of ActionMgtServiceComponentHolder.
     *
     * @return ActionMgtServiceComponentHolder instance.
     */
    public static ActionMgtServiceComponentHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the SecretManager.
     *
     * @return SecretManager instance.
     */
    public SecretManager getSecretManager() {

        return secretManager;
    }

    /**
     * Set the SecretManager.
     *
     * @param secretManager SecretManager instance.
     */
    public void setSecretManager(SecretManager secretManager) {

        this.secretManager = secretManager;
    }

    /**
     * Get the SecretResolveManager.
     *
     * @return SecretResolveManager instance.
     */
    public SecretResolveManager getSecretResolveManager() {

        return secretResolveManager;
    }

    /**
     * Set the SecretResolveManager.
     *
     * @param secretResolveManager SecretResolveManager instance.
     */
    public void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        this.secretResolveManager = secretResolveManager;
    }

    /**
     * Get the RuleManagementService.
     *
     * @return RuleManagementService instance.
     */
    public RuleManagementService getRuleManagementService() {

        return ruleManagementService;
    }

    /**
     * Set the RuleManagementService.
     *
     * @param ruleManagementService RuleManagementService instance.
     */
    public void setRuleManagementService(RuleManagementService ruleManagementService) {

        this.ruleManagementService = ruleManagementService;
    }
}
