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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.rule;

import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rule evaluation data provider for pre update password flow.
 * This class provides the data required for rule evaluation in pre update password flow.
 */
public class PreUpdatePasswordActionRuleEvaluationDataProvider implements RuleEvaluationDataProvider {

    private static final String FLOW_FIELD = "flow";

    /**
     * Supported flows for pre update password action.
     */
    private enum PasswordUpdateFlowType {
        ADMIN_INITIATED_PASSWORD_RESET("adminInitiatedPasswordReset"),
        ADMIN_INITIATED_PASSWORD_UPDATE("adminInitiatedPasswordUpdate"),
        ADMIN_INITIATED_USER_INVITE_TO_SET_PASSWORD("adminInitiatedUserInviteToSetPassword"),
        APPLICATION_INITIATED_PASSWORD_UPDATE("applicationInitiatedPasswordUpdate"),
        USER_INITIATED_PASSWORD_UPDATE("userInitiatedPasswordUpdate"),
        USER_INITIATED_PASSWORD_RESET("userInitiatedPasswordReset"),
        ADMIN_INITIATED_REGISTRATION("adminInitiatedRegistration"),
        APPLICATION_INITIATED_REGISTRATION("applicationInitiatedRegistration"),
        USER_INITIATED_REGISTRATION("userInitiatedRegistration");

        final String flowName;

        PasswordUpdateFlowType(String flowName) {

            this.flowName = flowName;
        }

        public String getFlowName() {

            return flowName;
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
            if (Objects.equals(field.getName(), FLOW_FIELD)) {
                fieldValueList.add(new FieldValue(field.getName(), getFlowFromContext(), ValueType.STRING));
                break;
            } else {
                throw new RuleEvaluationDataProviderException("Unsupported field: " + field.getName());
            }
        }

        return fieldValueList;
    }

    private String getFlowFromContext() throws RuleEvaluationDataProviderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getCurrentFlow();
        if ((flow.getName() == Flow.Name.PROFILE_UPDATE || flow.getName() == Flow.Name.CREDENTIAL_UPDATE) &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            // Password update is a sub-flow of profile update.
            return PasswordUpdateFlowType.ADMIN_INITIATED_PASSWORD_UPDATE.getFlowName();
        }

        if ((flow.getName() == Flow.Name.PROFILE_UPDATE || flow.getName() == Flow.Name.CREDENTIAL_UPDATE) &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.APPLICATION) {
            // Password update is a sub-flow of profile update.
            return PasswordUpdateFlowType.APPLICATION_INITIATED_PASSWORD_UPDATE.getFlowName();
        }

        if ((flow.getName() == Flow.Name.PROFILE_UPDATE || flow.getName() == Flow.Name.CREDENTIAL_UPDATE) &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.USER) {
            // Password update is a sub-flow of profile update.
            return PasswordUpdateFlowType.USER_INITIATED_PASSWORD_UPDATE.getFlowName();
        }

        if (flow.getName() == Flow.Name.CREDENTIAL_RESET &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            return PasswordUpdateFlowType.ADMIN_INITIATED_PASSWORD_RESET.getFlowName();
        }

        if (flow.getName() == Flow.Name.CREDENTIAL_RESET &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.USER) {
            return PasswordUpdateFlowType.USER_INITIATED_PASSWORD_RESET.getFlowName();
        }

        if ((flow.getName() == Flow.Name.INVITE || flow.getName() ==
                Flow.Name.INVITED_USER_REGISTRATION) && flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            return PasswordUpdateFlowType.ADMIN_INITIATED_USER_INVITE_TO_SET_PASSWORD.getFlowName();
        }

        if (flow.getName() == Flow.Name.REGISTER &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.ADMIN) {
            return PasswordUpdateFlowType.ADMIN_INITIATED_REGISTRATION.getFlowName();
        }

        if (flow.getName() == Flow.Name.REGISTER &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.APPLICATION) {
            return PasswordUpdateFlowType.APPLICATION_INITIATED_REGISTRATION.getFlowName();
        }

        if (flow.getName() == Flow.Name.REGISTER &&
                flow.getInitiatingPersona() == Flow.InitiatingPersona.USER) {
            return PasswordUpdateFlowType.USER_INITIATED_REGISTRATION.getFlowName();
        }

        throw new RuleEvaluationDataProviderException("Unsupported flow type: " + flow.getName() +
                " for Pre Update Password Action.");
    }
}
