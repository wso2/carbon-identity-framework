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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;

/**
 * Default implementation of the ActionTriggerEvaluatorForVersion which decide whether action can be trigger based
 * flow context for the action version.
 */
public class ActionTriggerEvaluatorForVersion {

    private static final ActionTriggerEvaluatorForVersion instance = new ActionTriggerEvaluatorForVersion();

    private static final Log LOG = LogFactory.getLog(ActionTriggerEvaluatorForVersion.class);

    public static ActionTriggerEvaluatorForVersion getInstance() {

        return instance;
    }

    /**
     * Evaluate whether action can be trigger based on flow context for the given action version.
     *
     * @param actionType  Action type.
     * @param action      Action.
     * @param flowContext Flow context.
     * @return True if action can be trigger based on the flow context.
     */
    public boolean isTriggerableForRegistrationFlow(ActionType actionType, Action action, FlowContext flowContext)
            throws ActionExecutionException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getCurrentFlow();
        if (flow == null || flow.getName() == null) {
            throw new ActionExecutionException("Error occurred while checking action is executable for registration " +
                    "flow. Flow or required attributes are null.");
        }
        if (Flow.Name.REGISTER.equals(flow.getName())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action id: " + action.getId() + " is not triggerable for registration flow.");
            }
            return false;
        }
        return true;
    }
}

