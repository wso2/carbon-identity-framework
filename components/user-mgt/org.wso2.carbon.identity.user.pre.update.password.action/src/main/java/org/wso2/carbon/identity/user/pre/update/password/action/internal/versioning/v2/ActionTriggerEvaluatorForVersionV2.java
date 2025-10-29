/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v2;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.ActionTriggerEvaluatorForVersion;

/**
 * Implementation of the ActionTriggerEvaluatorForVersion for version v2.
 */
public class ActionTriggerEvaluatorForVersionV2 extends ActionTriggerEvaluatorForVersion {

    private static final ActionTriggerEvaluatorForVersionV2 instance = new ActionTriggerEvaluatorForVersionV2();

    public static ActionTriggerEvaluatorForVersionV2 getInstance() {

        return instance;
    }

    public boolean isTriggerableForRegistrationFlow(ActionType actionType, Action action, FlowContext flowContext)
            throws ActionExecutionException {

        return true;
    }
}
