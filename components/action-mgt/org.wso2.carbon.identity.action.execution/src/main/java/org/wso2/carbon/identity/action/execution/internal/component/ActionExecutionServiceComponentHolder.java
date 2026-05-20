/*
 * Copyright (c) 2024-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.internal.component;

import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This class holds references for dependent services required for Action Execution Service to function.
 */
public class ActionExecutionServiceComponentHolder {

    private static final ActionExecutionServiceComponentHolder INSTANCE = new ActionExecutionServiceComponentHolder();

    private ActionManagementService actionManagementService;
    private RuleEvaluationService ruleEvaluationService;
    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private RealmService realmService;
    private ActionExecutorService actionExecutorService;

    private ActionExecutionServiceComponentHolder() {

    }

    public static ActionExecutionServiceComponentHolder getInstance() {

        return INSTANCE;
    }

    public ActionManagementService getActionManagementService() {

        return actionManagementService;
    }

    public void setActionManagementService(ActionManagementService actionManagementService) {

        this.actionManagementService = actionManagementService;
    }

    public RuleEvaluationService getRuleEvaluationService() {

        return ruleEvaluationService;
    }

    public void setRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        this.ruleEvaluationService = ruleEvaluationService;
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

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get the ActionExecutorService instance.
     *
     * @return ActionExecutorService instance.
     */
    public ActionExecutorService getActionExecutorService() {

        return actionExecutorService;
    }

    /**
     * Set the ActionExecutorService instance.
     *
     * @param actionExecutorService ActionExecutorService instance.
     */
    public void setActionExecutorService(ActionExecutorService actionExecutorService) {

        this.actionExecutorService = actionExecutorService;
    }
}
