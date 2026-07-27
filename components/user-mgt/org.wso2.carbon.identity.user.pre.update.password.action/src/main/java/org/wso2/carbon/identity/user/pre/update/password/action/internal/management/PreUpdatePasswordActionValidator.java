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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.management;

import org.wso2.carbon.identity.action.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Action.ActionTypes;
import org.wso2.carbon.identity.action.management.api.service.impl.DefaultActionValidator;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.List;

import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.ACTION_VERSION_V1;

/**
 * Validator class for Pre Update Password Action.
 */
public class PreUpdatePasswordActionValidator extends DefaultActionValidator {

    @Override
    public ActionTypes getSupportedActionType() {

        return ActionTypes.PRE_UPDATE_PASSWORD;
    }

    private final List<String> fieldValesToExcludeForV1 = List.of(
            "adminInitiatedRegistration",
            "applicationInitiatedRegistration",
            "userInitiatedRegistration"
        );

    /**
     * Validates whether rules can be applied for the given action version.
     *
     * @param action The {@link Action} object to validate.
     * @throws ActionMgtException If rule is not allowed for the given action version.
     */
    @Override
    public void isRulesApplicableForActionVersion(String actionVersion, Action action)
            throws ActionMgtException {

        if (actionVersion == null || actionVersion.isEmpty()) {
            throw new ActionMgtServerException("Error while resolving Action version for action: " + action.getName());
        }

        if (ACTION_VERSION_V1.equals(actionVersion) &&
                action.getActionRule() != null && action.getActionRule().getRule() != null) {
            Rule rule = action.getActionRule().getRule();
            for (Expression expression : rule.getExpressions()) {
                if (fieldValesToExcludeForV1.stream()
                        .anyMatch(value -> value.equals(expression.getValue().getFieldValue()))) {
                    ErrorMessage error =  ErrorMessage.ERROR_INVALID_RULE_FOR_ACTION_VERSION;
                    throw new ActionMgtClientException(error.getMessage(),
                            String.format(error.getMessage(), actionVersion), error.getCode());
                }
            }
        }
    }
}
