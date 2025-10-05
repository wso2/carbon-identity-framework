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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.execution;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.internal.service.impl.DefaultActionVersionHandler;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.VersionTriggerEvaluator;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.VersionTriggerEvaluatorFactory;

/**
 * Implementation of the Version handler for pre update password action.
 */
public class PreUpdatePasswordActionVersionHandler extends DefaultActionVersionHandler {

    VersionTriggerEvaluatorFactory factory = VersionTriggerEvaluatorFactory.getInstance();

    @Override
    public boolean isVersionEligibleForTrigger(ActionType actionType, Action action, FlowContext flowContext)
            throws ActionExecutionException {

        VersionTriggerEvaluator versionTriggerEvaluator = factory.getVersionTriggerEvaluator(action);

        if (!versionTriggerEvaluator.isTriggerableForRegistrationFlow(actionType, action, flowContext)) {
            return false;
        }

        return true;
    }
}
