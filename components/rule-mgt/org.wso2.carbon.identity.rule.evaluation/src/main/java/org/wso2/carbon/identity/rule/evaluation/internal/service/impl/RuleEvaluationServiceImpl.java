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

package org.wso2.carbon.identity.rule.evaluation.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of RuleEvaluationService.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService",
                "service.scope=singleton"
        }
)
public class RuleEvaluationServiceImpl implements RuleEvaluationService {

    private static final Log LOG = LogFactory.getLog(RuleEvaluationServiceImpl.class);

    @Override
    public RuleEvaluationResult evaluate(String ruleId, FlowContext flowContext, String tenantDomain)
            throws RuleEvaluationException {

        Rule rule = getRuleFromRuleManagementService(ruleId, tenantDomain);

        if (!rule.isActive()) {
            LOG.debug("Rule: " + rule.getId() + " is inactive. Skip evaluation of rule.");
            return new RuleEvaluationResult(ruleId, false);
        }

        LOG.debug("Starting to evaluate rule: " + rule.getId() + ".");

        FieldExtractor fieldExtractor =
                new FieldExtractor(getRuleMetaFromRuleMetadataService(flowContext.getFlowType(), tenantDomain));
        List<Field> fieldsInRule = fieldExtractor.extractFields(rule);

        Map<String, FieldValue> evaluationData =
                getEvaluationData(ruleId, flowContext, tenantDomain, fieldsInRule);

        RuleEvaluator ruleEvaluator = new RuleEvaluator(RuleEvaluationComponentServiceHolder.getInstance()
                .getOperatorRegistry());
        boolean evaluationStatus = ruleEvaluator.evaluate(rule, evaluationData);
        LOG.debug("Evaluated rule: " + rule.getId() + " to: " + evaluationStatus + ".");

        return new RuleEvaluationResult(ruleId, evaluationStatus);
    }

    private Map<String, FieldValue> getEvaluationData(String ruleId, FlowContext flowContext,
                                                      String tenantDomain, List<Field> fieldsInRule)
            throws RuleEvaluationException {

        RuleEvaluationDataManager ruleEvaluationDataProviderManager = RuleEvaluationDataManager.getInstance();

        List<FieldValue> evaluationDataList = ruleEvaluationDataProviderManager.getEvaluationData(
                new RuleEvaluationContext(ruleId, fieldsInRule), flowContext, tenantDomain);

        return (evaluationDataList == null || evaluationDataList.isEmpty())
                ? Collections.emptyMap()
                : evaluationDataList.stream().collect(Collectors.toMap(FieldValue::getName, fieldValue -> fieldValue));
    }

    private Rule getRuleFromRuleManagementService(String ruleId, String tenantDomain)
            throws RuleEvaluationException {

        try {
            Rule rule = RuleEvaluationComponentServiceHolder.getInstance()
                    .getRuleManagementService()
                    .getRuleByRuleId(ruleId, tenantDomain);
            if (rule == null) {
                throw new RuleEvaluationException("Rule not found for the given ruleId: " + ruleId);
            }

            return rule;
        } catch (RuleManagementException e) {
            throw new RuleEvaluationException("Error while retrieving the Rule.", e);
        }
    }

    private List<FieldDefinition> getRuleMetaFromRuleMetadataService(FlowType flowType, String tenantDomain)
            throws RuleEvaluationException {

        try {
            List<FieldDefinition> fieldDefinitionList =
                    RuleEvaluationComponentServiceHolder.getInstance().getRuleMetadataService()
                            .getExpressionMeta(
                                    org.wso2.carbon.identity.rule.metadata.api.model.FlowType.valueOf(flowType.name()),
                                    tenantDomain);

            if (fieldDefinitionList == null || fieldDefinitionList.isEmpty()) {
                throw new RuleEvaluationException(
                        "Expression metadata from RuleMetadataService is null or empty.");
            }
            return fieldDefinitionList;
        } catch (RuleMetadataException e) {
            throw new RuleEvaluationException("Error while retrieving expression metadata from RuleMetadataService.",
                    e);
        }
    }
}
