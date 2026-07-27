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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.rule;

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
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Rule evaluation data provider for pre update profile flow.
 * This class provides the data required for rule evaluation in pre update profile flow.
 */
public class PreUpdateProfileActionRuleEvaluationDataProvider implements RuleEvaluationDataProvider {

    private enum RuleField {
        FLOW_FIELD("flow"),
        CLAIM_FIELD("claim");

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

    /**
     * Supported flows for pre update profile action.
     */
    private enum ProfileUpdateFlowType {
        ADMIN_INITIATED_PROFILE_UPDATE("adminInitiatedProfileUpdate"),
        APPLICATION_INITIATED_PROFILE_UPDATE("applicationInitiatedProfileUpdate"),
        USER_INITIATED_PROFILE_UPDATE("userInitiatedProfileUpdate");

        final String flowName;

        ProfileUpdateFlowType(String flowName) {

            this.flowName = flowName;
        }

        public String getFlowName() {

            return flowName;
        }
    }

    @Override
    public FlowType getSupportedFlowType() {

        return FlowType.PRE_UPDATE_PROFILE;
    }

    @Override
    public List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext,
                                              FlowContext flowContext, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        List<FieldValue> fieldValueList = new ArrayList<>();

        for (Field field : ruleEvaluationContext.getFields()) {
            switch (RuleField.valueOfFieldName(field.getName())) {
                case FLOW_FIELD:
                    fieldValueList.add(new FieldValue(field.getName(), getFlowFromContext(), ValueType.STRING));
                    break;
                case CLAIM_FIELD:
                    addClaimFieldValue(fieldValueList, field, flowContext);
                    break;
                default:
                    throw new RuleEvaluationDataProviderException("Unsupported field: " + field.getName());
            }
        }

        return fieldValueList;
    }

    private String getFlowFromContext() throws RuleEvaluationDataProviderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getCurrentFlow();

        if (flow == null || flow.getName() == null || flow.getInitiatingPersona() == null) {
            throw new RuleEvaluationDataProviderException("Flow or required attributes are null.");
        }

        if (flow.getName() != Flow.Name.PROFILE_UPDATE) {
            throw new RuleEvaluationDataProviderException("Unsupported flow type: " + flow.getName() +
                    " for Pre Update Profile Action.");
        }

        switch (flow.getInitiatingPersona()) {
            case ADMIN:
                return ProfileUpdateFlowType.ADMIN_INITIATED_PROFILE_UPDATE.getFlowName();
            case APPLICATION:
                return ProfileUpdateFlowType.APPLICATION_INITIATED_PROFILE_UPDATE.getFlowName();
            case USER:
                return ProfileUpdateFlowType.USER_INITIATED_PROFILE_UPDATE.getFlowName();
            default:
                throw new RuleEvaluationDataProviderException("Unsupported initiating persona: "
                        + flow.getInitiatingPersona());
        }
    }

    private void addClaimFieldValue(List<FieldValue> fieldValueList, Field field, FlowContext flowContext)
            throws RuleEvaluationDataProviderException {

        UserActionContext userActionContext = resolveUserActionContext(flowContext);
        Set<String> claims = userActionContext.getUserActionRequestDTO().getClaims().keySet();
        if (claims != null) {
            fieldValueList.add(new FieldValue(field.getName(), new ArrayList<>(claims)));
        }
    }

    private UserActionContext resolveUserActionContext(FlowContext flowContext)
            throws RuleEvaluationDataProviderException {

        Object userActionContext = flowContext.getContextData()
                .get(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY);
        if (!(userActionContext instanceof UserActionContext)) {
            throw new RuleEvaluationDataProviderException("Invalid context data type: " + userActionContext);
        }
        return (UserActionContext) userActionContext;
    }
}
