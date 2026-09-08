/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.internal.rule;

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
import java.util.Map;

/**
 * Provides device attribute data for rule evaluation in the DEVICE_POLICY flow.
 */
public class DevicePolicyEvaluationDataProvider implements RuleEvaluationDataProvider {

    @Override
    public FlowType getSupportedFlowType() {

        return FlowType.DEVICE_POLICY;
    }

    @Override
    public List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext,
                                              FlowContext flowContext, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        Map<String, Object> deviceData = flowContext.getContextData();
        List<FieldValue> fieldValues = new ArrayList<>();

        for (Field field : ruleEvaluationContext.getFields()) {
            Object rawValue = deviceData.get(field.getName());
            if (rawValue == null) {
                continue;
            }
            String value = String.valueOf(rawValue);
            if (ValueType.NUMBER.equals(field.getValueType())) {
                try {
                    fieldValues.add(new FieldValue(field.getName(), Double.parseDouble(value)));
                } catch (NumberFormatException e) {
                    throw new RuleEvaluationDataProviderException(
                            "Invalid numeric value for field '" + field.getName() + "': " + value);
                }
            } else {
                fieldValues.add(new FieldValue(field.getName(), value, ValueType.STRING));
            }
        }

        return fieldValues;
    }
}
