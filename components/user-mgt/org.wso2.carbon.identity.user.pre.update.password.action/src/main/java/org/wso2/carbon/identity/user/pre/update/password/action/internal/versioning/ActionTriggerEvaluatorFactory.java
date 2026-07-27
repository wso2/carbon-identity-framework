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

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v2.ActionTriggerEvaluatorForVersionV2;

/**
 * Factory class for getting the ActionTriggerEvaluatorForVersion for Action version.
 */
public class ActionTriggerEvaluatorFactory {

    private static final ActionTriggerEvaluatorFactory instance = new ActionTriggerEvaluatorFactory();

    public static ActionTriggerEvaluatorFactory getInstance() {

        return instance;
    }

    public ActionTriggerEvaluatorForVersion getVersionTriggerEvaluator(Action action) throws ActionExecutionException {

        switch (action.getActionVersion()) {
            case PreUpdatePasswordActionConstants.ACTION_VERSION_V1:
                return ActionTriggerEvaluatorForVersion.getInstance();
            case PreUpdatePasswordActionConstants.ACTION_VERSION_V2:
                return ActionTriggerEvaluatorForVersionV2.getInstance();
            default:
                throw new ActionExecutionException("Unsupported action version: " + action.getActionVersion());
        }
    }
}
