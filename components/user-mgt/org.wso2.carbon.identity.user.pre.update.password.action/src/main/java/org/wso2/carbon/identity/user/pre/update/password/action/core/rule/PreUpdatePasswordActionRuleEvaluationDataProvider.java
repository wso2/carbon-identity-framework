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

package org.wso2.carbon.identity.user.pre.update.password.action.core.rule;

import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.rule.evaluation.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.model.Field;
import org.wso2.carbon.identity.rule.evaluation.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.provider.RuleEvaluationDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule evaluation data provider for pre update password flow.
 * This class provides the data required for rule evaluation in pre update password flow.
 */
public class PreUpdatePasswordActionRuleEvaluationDataProvider implements RuleEvaluationDataProvider {

    /**
     * Supported flows for pre update password action.
     */
    private enum PasswordUpdateFlowType {
        ADMIN_INITIATED_PASSWORD_RESET("admin_initiated_password_reset"),
        ADMIN_INITIATED_PASSWORD_UPDATE("admin_initiated_password_update"),
        ADMIN_INITIATED_USER_INVITE_TO_SET_PASSWORD("admin_initiated_user_invite_to_set_password"),
        APPLICATION_INITIATED_PASSWORD_UPDATE("application_initiated_password_update"),
        USER_INITIATED_PASSWORD_UPDATE("user_initiated_password_update"),
        USER_INITIATED_PASSWORD_RESET("user_initiated_password_reset");

        final String flowName;

        PasswordUpdateFlowType(String flowName) {

            this.flowName = flowName;
        }

        public String getFlowName() {

            return flowName;
        }
    }

    private enum RuleField {
        FLOW("flow");

        final String fieldName;

        RuleField(String fieldName) {

            this.fieldName = fieldName;
        }

        public String getFieldName() {

            return fieldName;
        }

        public static RuleField valueOfFieldName(String fieldName) throws RuleEvaluationDataProviderException {

            for (RuleField ruleField : RuleField.values()) {
                if (ruleField.getFieldName().equals(fieldName)) {
                    return ruleField;
                }
            }

            throw new RuleEvaluationDataProviderException("Unsupported field: " + fieldName);
        }
    }

    @Override
    public FlowType getSupportedFlowType() {

        return FlowType.PRE_UPDATE_PASSWORD;
    }

    @Override
    public List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext,
                                              FlowContext flowContext, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        List<FieldValue> fieldValueList = new ArrayList<>();
        for (Field field : ruleEvaluationContext.getFields()) {
            if (RuleField.valueOfFieldName(field.getName()) == RuleField.FLOW) {
                fieldValueList.add(new FieldValue(field.getName(), getFlowFromContext(), ValueType.STRING));
                break;
            } else {
                throw new RuleEvaluationDataProviderException("Unsupported field: " + field.getName());
            }
        }

        return fieldValueList;
    }

    private String getFlowFromContext() throws RuleEvaluationDataProviderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow.getName() == Flow.Name.PASSWORD_UPDATE &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            return PasswordUpdateFlowType.ADMIN_INITIATED_PASSWORD_UPDATE.getFlowName();
        }

        if (flow.getName() == Flow.Name.PASSWORD_UPDATE &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.APPLICATION) {
            return PasswordUpdateFlowType.APPLICATION_INITIATED_PASSWORD_UPDATE.getFlowName();
        }

        if (flow.getName() == Flow.Name.PASSWORD_UPDATE &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.USER) {
            return PasswordUpdateFlowType.USER_INITIATED_PASSWORD_UPDATE.getFlowName();
        }

        if (flow.getName() == Flow.Name.PASSWORD_RESET &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            return PasswordUpdateFlowType.ADMIN_INITIATED_PASSWORD_RESET.getFlowName();
        }

        if (flow.getName() == Flow.Name.PASSWORD_RESET &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.USER) {
            return PasswordUpdateFlowType.USER_INITIATED_PASSWORD_RESET.getFlowName();
        }

        if (flow.getName() == Flow.Name.USER_REGISTRATION_INVITE_WITH_PASSWORD &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            return PasswordUpdateFlowType.ADMIN_INITIATED_USER_INVITE_TO_SET_PASSWORD.getFlowName();
        }

        throw new RuleEvaluationDataProviderException("Unsupported flow type: " + flow.getName() +
                " for Pre Update Password Action.");
    }
}
