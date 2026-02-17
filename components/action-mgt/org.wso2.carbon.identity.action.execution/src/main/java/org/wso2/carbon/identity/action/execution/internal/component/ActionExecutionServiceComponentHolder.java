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

package org.wso2.carbon.identity.action.execution.internal.component;

import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;

/**
 * This class holds references for dependent services required for Action Execution Service to function.
 */
public class ActionExecutionServiceComponentHolder {

    private static final ActionExecutionServiceComponentHolder INSTANCE = new ActionExecutionServiceComponentHolder();

    private ActionManagementService actionManagementService;
    private RuleEvaluationService ruleEvaluationService;
    private ActionExecutorService actionExecutorService;
    private ClaimMetadataManagementService claimMetadataManagementService;

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

    /**
     * Get the ClaimMetadataManagementService instance.
     *
     * @return ClaimMetadataManagementService instance.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    /**
     * Set the ClaimMetadataManagementService instance.
     *
     * @param claimMetadataManagementService ClaimMetadataManagementService instance.
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }
}
