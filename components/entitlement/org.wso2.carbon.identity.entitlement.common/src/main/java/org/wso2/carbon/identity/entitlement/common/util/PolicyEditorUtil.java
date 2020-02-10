/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.utils.Constants.PolicyConstants;
import org.wso2.balana.utils.exception.PolicyBuilderException;
import org.wso2.balana.utils.policy.PolicyBuilder;
import org.wso2.balana.utils.policy.dto.AllOfElementDTO;
import org.wso2.balana.utils.policy.dto.AnyOfElementDTO;
import org.wso2.balana.utils.policy.dto.ApplyElementDTO;
import org.wso2.balana.utils.policy.dto.AttributeAssignmentElementDTO;
import org.wso2.balana.utils.policy.dto.AttributeDesignatorDTO;
import org.wso2.balana.utils.policy.dto.AttributeSelectorDTO;
import org.wso2.balana.utils.policy.dto.AttributeValueElementDTO;
import org.wso2.balana.utils.policy.dto.BasicPolicyDTO;
import org.wso2.balana.utils.policy.dto.BasicRuleDTO;
import org.wso2.balana.utils.policy.dto.BasicTargetDTO;
import org.wso2.balana.utils.policy.dto.ConditionElementDT0;
import org.wso2.balana.utils.policy.dto.MatchElementDTO;
import org.wso2.balana.utils.policy.dto.ObligationElementDTO;
import org.wso2.balana.utils.policy.dto.PolicyElementDTO;
import org.wso2.balana.utils.policy.dto.RuleElementDTO;
import org.wso2.balana.utils.policy.dto.TargetElementDTO;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementPolicyCreationException;
import org.wso2.carbon.identity.entitlement.common.PolicyEditorConstants;
import org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine;
import org.wso2.carbon.identity.entitlement.common.PolicyEditorException;
import org.wso2.carbon.identity.entitlement.common.dto.ExtendAttributeDTO;
import org.wso2.carbon.identity.entitlement.common.dto.ObligationDTO;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyRefIdDTO;
import org.wso2.carbon.identity.entitlement.common.dto.PolicySetDTO;
import org.wso2.carbon.identity.entitlement.common.dto.RowDTO;
import org.wso2.carbon.identity.entitlement.common.dto.RuleDTO;
import org.wso2.carbon.identity.entitlement.common.dto.SimplePolicyEditorDTO;
import org.wso2.carbon.identity.entitlement.common.dto.SimplePolicyEditorElementDTO;
import org.wso2.carbon.identity.entitlement.common.dto.TargetDTO;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Util class that helps to create the XACML policy which is defined by the XACML basic policy editor
 */
public class PolicyEditorUtil {

    private static Log log = LogFactory.getLog(PolicyEditorUtil.class);

    /**
     * map of apply element w.r.t identifier
     */
    private static Map<String, ApplyElementDTO> applyElementMap = new HashMap<String, ApplyElementDTO>();

    /**
     * Create XACML policy with the simplest input attributes
     *
     * @param policyEditorDTO
     * @return
     * @throws PolicyEditorException
     */
    public static String createSOAPolicy(SimplePolicyEditorDTO policyEditorDTO) throws PolicyEditorException {

        BasicPolicyDTO basicPolicyDTO = new BasicPolicyDTO();
        BasicTargetDTO basicTargetDTO = null;
        List<BasicRuleDTO> ruleElementDTOs = new ArrayList<BasicRuleDTO>();

        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.RBAC);

        //create policy element
        basicPolicyDTO.setPolicyId(policyEditorDTO.getPolicyId());
        // setting rule combining algorithm
        basicPolicyDTO.setRuleAlgorithm(PolicyConstants.RuleCombiningAlog.FIRST_APPLICABLE_ID);
        basicPolicyDTO.setDescription(policyEditorDTO.getDescription());

        if (PolicyEditorConstants.SOA_CATEGORY_USER.equals(policyEditorDTO.getAppliedCategory())) {

            if (policyEditorDTO.getUserAttributeValue() != null &&
                    !PolicyEditorConstants.FunctionIdentifier.ANY.
                            equals(policyEditorDTO.getUserAttributeValue().trim())) {

                basicTargetDTO = new BasicTargetDTO();
                String selectedDataType = null;

                if (policyEditorDTO.getUserAttributeId() == null) {
                    basicTargetDTO.setSubjectId(PolicyEditorConstants.SUBJECT_ID_DEFAULT);
                } else {
                    basicTargetDTO.setSubjectId(holder.getAttributeIdUri(policyEditorDTO.getUserAttributeId()));
                    if ((selectedDataType = holder.getDataTypeUriForAttribute(policyEditorDTO.getUserAttributeId())) != null) {
                        basicTargetDTO.setSubjectDataType(selectedDataType);
                    }
                }

                if (basicTargetDTO.getSubjectDataType() == null) {
                    basicTargetDTO.setSubjectDataType(PolicyConstants.DataType.STRING);
                }

                String function = findFunction(policyEditorDTO.getUserAttributeValue(),
                        basicTargetDTO.getSubjectDataType());
                String value = findAttributeValue(policyEditorDTO.getUserAttributeValue());
                basicTargetDTO.setSubjectList(value);
                basicTargetDTO.setFunctionOnSubjects(function);
            }

            List<SimplePolicyEditorElementDTO> elementDTOs = policyEditorDTO.getSimplePolicyEditorElementDTOs();

            if (elementDTOs != null) {
                int ruleNo = 1;
                for (SimplePolicyEditorElementDTO dto : elementDTOs) {
                    BasicRuleDTO ruleElementDTO = new BasicRuleDTO();

                    if (dto.getResourceValue() != null && dto.getResourceValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getResourceValue().trim())) {
                        addResourceElement(ruleElementDTO, dto);
                    }

                    if (dto.getActionValue() != null && dto.getActionValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getActionValue().trim())) {
                        addActionElement(ruleElementDTO, dto);
                    }

                    if (dto.getEnvironmentValue() != null && dto.getEnvironmentValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getEnvironmentValue().trim())) {
                        addEnvironmentElement(ruleElementDTO, dto);
                    }

                    ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_PERMIT);
                    ruleElementDTO.setRuleId("Rule-" + ruleNo);
                    ruleElementDTOs.add(ruleElementDTO);
                    ruleNo++;
                }

                BasicRuleDTO ruleElementDTO = new BasicRuleDTO();
                ruleElementDTO.setRuleId("Deny-Rule");
                ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_DENY);
                ruleElementDTOs.add(ruleElementDTO);
            }
        } else if (PolicyEditorConstants.SOA_CATEGORY_RESOURCE.equals(policyEditorDTO.getAppliedCategory())) {

            if (policyEditorDTO.getResourceValue() != null &&
                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(policyEditorDTO.getResourceValue().trim())) {
                basicTargetDTO = new BasicTargetDTO();

                basicTargetDTO.setResourceId(PolicyEditorConstants.RESOURCE_ID_DEFAULT);
                basicTargetDTO.setResourceDataType(PolicyConstants.DataType.STRING);

                String function = findFunction(policyEditorDTO.getResourceValue(),
                        basicTargetDTO.getResourceDataType());
                String value = findAttributeValue(policyEditorDTO.getResourceValue());
                basicTargetDTO.setResourceList(value);
                basicTargetDTO.setFunctionOnResources(function);
            }

            List<SimplePolicyEditorElementDTO> elementDTOs = policyEditorDTO.getSimplePolicyEditorElementDTOs();

            if (elementDTOs != null) {
                int ruleNo = 1;
                for (SimplePolicyEditorElementDTO dto : elementDTOs) {
                    BasicRuleDTO ruleElementDTO = new BasicRuleDTO();

                    if (dto.getResourceValue() != null && dto.getResourceValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getResourceValue().trim())) {

                        addResourceElement(ruleElementDTO, dto);
                    }

                    if (dto.getUserAttributeValue() != null && dto.getUserAttributeValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getUserAttributeValue().trim())) {

                        addSubjectElement(ruleElementDTO, dto);
                    }

                    if (dto.getActionValue() != null && dto.getActionValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getActionValue().trim())) {

                        addActionElement(ruleElementDTO, dto);
                    }

                    if (dto.getEnvironmentValue() != null && dto.getEnvironmentValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getEnvironmentValue().trim())) {

                        addEnvironmentElement(ruleElementDTO, dto);
                    }

                    ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_PERMIT);
                    ruleElementDTO.setRuleId("Rule-" + ruleNo);
                    ruleElementDTOs.add(ruleElementDTO);
                    ruleNo++;
                }

                BasicRuleDTO ruleElementDTO = new BasicRuleDTO();
                ruleElementDTO.setRuleId("Deny-Rule");
                ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_DENY);
                ruleElementDTOs.add(ruleElementDTO);
            }
        } else if (PolicyEditorConstants.SOA_CATEGORY_ACTION.equals(policyEditorDTO.getAppliedCategory())) {

            if (policyEditorDTO.getActionValue() != null &&
                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(policyEditorDTO.getActionValue().trim())) {

                basicTargetDTO = new BasicTargetDTO();

                basicTargetDTO.setActionId(PolicyEditorConstants.ACTION_ID_DEFAULT);
                basicTargetDTO.setActionDataType(PolicyConstants.DataType.STRING);

                String function = findFunction(policyEditorDTO.getActionValue(),
                        basicTargetDTO.getActionDataType());
                String value = findAttributeValue(policyEditorDTO.getActionValue());
                basicTargetDTO.setActionList(value);
                basicTargetDTO.setFunctionOnActions(function);

            }
            List<SimplePolicyEditorElementDTO> elementDTOs = policyEditorDTO.getSimplePolicyEditorElementDTOs();

            if (elementDTOs != null) {
                int ruleNo = 1;
                for (SimplePolicyEditorElementDTO dto : elementDTOs) {
                    BasicRuleDTO ruleElementDTO = new BasicRuleDTO();

                    if (dto.getResourceValue() != null && dto.getResourceValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getResourceValue().trim())) {
                        addResourceElement(ruleElementDTO, dto);
                    }

                    if (dto.getUserAttributeValue() != null && dto.getUserAttributeValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getUserAttributeValue().trim())) {
                        addSubjectElement(ruleElementDTO, dto);
                    }

                    if (dto.getEnvironmentValue() != null && dto.getEnvironmentValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getEnvironmentValue().trim())) {
                        addEnvironmentElement(ruleElementDTO, dto);
                    }

                    ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_PERMIT);
                    ruleElementDTO.setRuleId("Rule-" + ruleNo);
                    ruleElementDTOs.add(ruleElementDTO);
                    ruleNo++;
                }

                BasicRuleDTO ruleElementDTO = new BasicRuleDTO();
                ruleElementDTO.setRuleId("Deny-Rule");
                ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_DENY);
                ruleElementDTOs.add(ruleElementDTO);
            }
        } else if (PolicyEditorConstants.SOA_CATEGORY_ENVIRONMENT.equals(policyEditorDTO.getAppliedCategory())) {

            if (policyEditorDTO.getEnvironmentValue() != null &&
                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(policyEditorDTO.getEnvironmentValue().trim())) {

                basicTargetDTO = new BasicTargetDTO();

                String selectedDataType = null;

                if (policyEditorDTO.getEnvironmentId() == null) {
                    basicTargetDTO.setEnvironmentId(PolicyEditorConstants.ENVIRONMENT_ID_DEFAULT);
                } else {
                    basicTargetDTO.setEnvironmentId(holder.getAttributeIdUri(policyEditorDTO.getEnvironmentId()));
                    if ((selectedDataType = holder.getDataTypeUriForAttribute(policyEditorDTO.getEnvironmentId())) != null) {
                        basicTargetDTO.setEnvironmentDataType(selectedDataType);
                    }
                }

                if (basicTargetDTO.getEnvironmentDataType() == null) {
                    basicTargetDTO.setEnvironmentDataType(PolicyConstants.DataType.STRING);
                }


                String function = findFunction(policyEditorDTO.getEnvironmentValue(),
                        basicTargetDTO.getEnvironmentDataType());
                String value = findAttributeValue(policyEditorDTO.getEnvironmentValue());
                basicTargetDTO.setEnvironmentList(value);
                basicTargetDTO.setFunctionOnEnvironment(function);

            }
            List<SimplePolicyEditorElementDTO> elementDTOs = policyEditorDTO.getSimplePolicyEditorElementDTOs();

            if (elementDTOs != null) {
                int ruleNo = 1;
                for (SimplePolicyEditorElementDTO dto : elementDTOs) {
                    BasicRuleDTO ruleElementDTO = new BasicRuleDTO();

                    if (dto.getResourceValue() != null && dto.getResourceValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getResourceValue().trim())) {
                        addResourceElement(ruleElementDTO, dto);
                    }

                    if (dto.getUserAttributeValue() != null && dto.getUserAttributeValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getUserAttributeValue().trim())) {
                        addSubjectElement(ruleElementDTO, dto);
                    }

                    if (dto.getActionValue() != null && dto.getActionValue().trim().length() > 0 &&
                            !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getActionValue().trim())) {
                        addActionElement(ruleElementDTO, dto);
                    }

                    ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_PERMIT);
                    ruleElementDTO.setRuleId("Rule-" + ruleNo);
                    ruleElementDTOs.add(ruleElementDTO);
                    ruleNo++;
                }

                BasicRuleDTO ruleElementDTO = new BasicRuleDTO();
                ruleElementDTO.setRuleId("Deny-Rule");
                ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_DENY);
                ruleElementDTOs.add(ruleElementDTO);
            }
        }

        if (basicTargetDTO != null) {
            basicPolicyDTO.setTargetDTO(basicTargetDTO);
        }

        if (ruleElementDTOs.size() > 0) {
            basicPolicyDTO.setBasicRuleDTOs(ruleElementDTOs);
        }

        try {
            return PolicyBuilder.getInstance().build(basicPolicyDTO);
        } catch (PolicyBuilderException e) {
            log.error(e);
            throw new PolicyEditorException("Error while building policy");
        }
    }

    /**
     * Helper method to create SOA policy
     *
     * @param ruleElementDTO
     * @param editorElementDTO
     */
    private static void addResourceElement(BasicRuleDTO ruleElementDTO,
                                           SimplePolicyEditorElementDTO editorElementDTO) {


        ruleElementDTO.setResourceId(PolicyEditorConstants.RESOURCE_ID_DEFAULT);
        ruleElementDTO.setResourceDataType(PolicyConstants.DataType.STRING);
        String function = findFunction(editorElementDTO.getResourceValue(),
                ruleElementDTO.getResourceDataType());
        String value = findAttributeValue(editorElementDTO.getResourceValue());
        ruleElementDTO.setResourceList(value);
        ruleElementDTO.setFunctionOnResources(function);
    }

    /**
     * Helper method to create SOA policy
     *
     * @param ruleElementDTO
     * @param editorElementDTO
     */
    private static void addSubjectElement(BasicRuleDTO ruleElementDTO,
                                          SimplePolicyEditorElementDTO editorElementDTO) {

        String selectedDataType = null;
        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.RBAC);

        if (editorElementDTO.getUserAttributeId() == null) {
            ruleElementDTO.setSubjectId(PolicyEditorConstants.SUBJECT_ID_DEFAULT);
        } else {
            ruleElementDTO.setSubjectId(holder.getAttributeIdUri(editorElementDTO.getUserAttributeId()));
            if ((selectedDataType = holder.getDataTypeUriForAttribute(editorElementDTO.getUserAttributeId())) != null) {
                ruleElementDTO.setSubjectDataType(selectedDataType);
            }
        }

        if (ruleElementDTO.getSubjectDataType() == null) {
            ruleElementDTO.setSubjectDataType(PolicyConstants.DataType.STRING);
        }
        String function = findFunction(editorElementDTO.getUserAttributeValue(),
                ruleElementDTO.getSubjectDataType());
        String value = findAttributeValue(editorElementDTO.getUserAttributeValue());
        ruleElementDTO.setSubjectList(value);
        ruleElementDTO.setFunctionOnSubjects(function);
    }

    /**
     * Helper method to create SOA policy
     *
     * @param ruleElementDTO
     * @param editorElementDTO
     */
    private static void addActionElement(BasicRuleDTO ruleElementDTO,
                                         SimplePolicyEditorElementDTO editorElementDTO) {

        ruleElementDTO.setActionId(PolicyEditorConstants.ACTION_ID_DEFAULT);
        ruleElementDTO.setActionDataType(PolicyConstants.DataType.STRING);

        String function = findFunction(editorElementDTO.getActionValue(),
                ruleElementDTO.getActionDataType());
        String value = findAttributeValue(editorElementDTO.getActionValue());
        ruleElementDTO.setActionList(value);
        ruleElementDTO.setFunctionOnActions(function);
    }

    /**
     * Helper method to create SOA policy
     *
     * @param ruleElementDTO
     * @param editorElementDTO
     */
    private static void addEnvironmentElement(BasicRuleDTO ruleElementDTO,
                                              SimplePolicyEditorElementDTO editorElementDTO) {

        String selectedDataType = null;
        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.RBAC);
        if (editorElementDTO.getEnvironmentId() == null) {
            ruleElementDTO.setEnvironmentId(PolicyEditorConstants.ENVIRONMENT_ID_DEFAULT);
        } else {
            ruleElementDTO.setEnvironmentId(holder.getAttributeIdUri(editorElementDTO.getEnvironmentId()));
            if ((selectedDataType = holder.getDataTypeUriForAttribute(editorElementDTO.getEnvironmentId())) != null) {
                ruleElementDTO.setEnvironmentDataType(selectedDataType);
            }
        }

        if (ruleElementDTO.getEnvironmentDataType() == null) {
            ruleElementDTO.setEnvironmentDataType(PolicyConstants.DataType.STRING);
        }

        String function = findFunction(editorElementDTO.getEnvironmentValue(),
                ruleElementDTO.getEnvironmentDataType());
        String value = findAttributeValue(editorElementDTO.getEnvironmentValue());
        ruleElementDTO.setEnvironmentDataType(ruleElementDTO.getEnvironmentDataType());
        ruleElementDTO.setEnvironmentList(value);
        ruleElementDTO.setFunctionOnEnvironment(function);

    }

    /**
     * Helper method to create SOA policy
     *
     * @param value
     * @param dataType
     * @return
     */
    private static String findFunction(String value, String dataType) {

        if (value == null) {
            return PolicyConstants.Functions.FUNCTION_EQUAL;
        }

        value = value.replace("&gt;", ">");
        value = value.replace("&lt;", "<");

        // only time range finction are valid for following data types
        if (PolicyConstants.DataType.DATE.equals(dataType) ||
                PolicyConstants.DataType.INT.equals(dataType) ||
                PolicyConstants.DataType.TIME.equals(dataType) ||
                PolicyConstants.DataType.DATE_TIME.equals(dataType) ||
                PolicyConstants.DataType.DOUBLE.equals(dataType) ||
                PolicyConstants.DataType.STRING.equals(dataType)) {

            if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.EQUAL_RANGE)) {
                if (value.contains(PolicyEditorConstants.FunctionIdentifier.RANGE_CLOSE)) {
                    return PolicyConstants.Functions.FUNCTION_GREATER_EQUAL_AND_LESS;
                } else {
                    return PolicyConstants.Functions.FUNCTION_GREATER_EQUAL_AND_LESS_EQUAL;
                }
            }

            if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.RANGE)) {
                if (value.contains(PolicyEditorConstants.FunctionIdentifier.EQUAL_RANGE_CLOSE)) {
                    return PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS_EQUAL;
                } else {
                    return PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS;
                }
            }

            if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.GREATER)) {
                return PolicyConstants.Functions.FUNCTION_GREATER;
            } else if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.GREATER_EQUAL)) {
                return PolicyConstants.Functions.FUNCTION_GREATER_EQUAL;
            } else if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.LESS)) {
                return PolicyConstants.Functions.FUNCTION_LESS;
            } else if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.LESS_EQUAL)) {
                return PolicyConstants.Functions.FUNCTION_LESS_EQUAL;
            }
        }

        if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.REGEX)) {
            return PolicyConstants.Functions.FUNCTION_EQUAL_MATCH_REGEXP;
        }

        if (value.contains(PolicyEditorConstants.FunctionIdentifier.OR)) {
            return PolicyConstants.Functions.FUNCTION_AT_LEAST_ONE;
        }

        if (value.contains(PolicyEditorConstants.FunctionIdentifier.AND)) {
            return PolicyConstants.Functions.FUNCTION_SET_EQUALS;
        }

        return PolicyConstants.Functions.FUNCTION_EQUAL;
    }

    /**
     * Helper method to create SOA policy
     *
     * @param value
     * @return
     */
    private static String findAttributeValue(String value) {

        if (value == null) {
            return null;
        }

        value = value.replace("&gt;", ">");
        value = value.replace("&lt;", "<");

        if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.EQUAL_RANGE) ||
                value.startsWith(PolicyEditorConstants.FunctionIdentifier.RANGE) ||
                value.startsWith(PolicyEditorConstants.FunctionIdentifier.REGEX)) {

            return value.substring(1, value.length() - 1).trim();

        } else if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.GREATER) ||
                value.startsWith(PolicyEditorConstants.FunctionIdentifier.LESS)) {
            return value.substring(1).trim();
        } else if (value.startsWith(PolicyEditorConstants.FunctionIdentifier.GREATER_EQUAL) ||
                value.startsWith(PolicyEditorConstants.FunctionIdentifier.LESS_EQUAL)) {
            return value.substring(2).trim();
        }

        if (value.contains(PolicyEditorConstants.FunctionIdentifier.AND)) {
            value = value.replace(PolicyEditorConstants.FunctionIdentifier.AND,
                    PolicyEditorConstants.ATTRIBUTE_SEPARATOR);
        }

        if (value.contains(PolicyEditorConstants.FunctionIdentifier.OR)) {
            value = value.replace(PolicyEditorConstants.FunctionIdentifier.OR,
                    PolicyEditorConstants.ATTRIBUTE_SEPARATOR);
        }

        return value.trim();
    }


// TODO for what?
//    public static String createRules(List<SimplePolicyEditorElementDTO> elementDTOs, Document doc)
//                                                                    throws  PolicyEditorException {
//
//        List<BasicRuleDTO> ruleElementDTOs = new ArrayList<BasicRuleDTO>();
//        if(elementDTOs != null){
//            int ruleNo = 1;
//            for(SimplePolicyEditorElementDTO dto : elementDTOs){
//                BasicRuleDTO ruleElementDTO = new BasicRuleDTO();
//
//                if(dto.getResourceValue() != null && dto.getResourceValue().trim().length() > 0 &&
//                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getResourceValue().trim())){
//                    ruleElementDTO.setResourceDataType(PolicyEditorConstants.DataType.STRING);
//                    ruleElementDTO.setResourceId(PolicyEditorConstants.RESOURCE_ID_DEFAULT);
//                    ruleElementDTO.setResourceList(dto.getResourceValue());
//                    ruleElementDTO.setFunctionOnResources(getBasicPolicyEditorFunction(dto.
//                                                                    getFunctionOnResources()));
//                }
//
//                if(dto.getUserAttributeValue() != null && dto.getUserAttributeValue().trim().length() > 0 &&
//                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getUserAttributeValue().trim())){
//                    ruleElementDTO.setSubjectDataType(PolicyEditorConstants.DataType.STRING);
//                    ruleElementDTO.setSubjectId(dto.getUserAttributeId());
//                    ruleElementDTO.setSubjectList(dto.getUserAttributeValue());
//                    ruleElementDTO.setFunctionOnSubjects(getBasicPolicyEditorFunction(dto.
//                                                                        getFunctionOnUsers()));
//                }
//
//                if(dto.getActionValue() != null && dto.getActionValue().trim().length() > 0 &&
//                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getActionValue().trim())){
//                    ruleElementDTO.setActionDataType(PolicyEditorConstants.DataType.STRING);
//                    ruleElementDTO.setActionList(dto.getActionValue());
//                    ruleElementDTO.setActionId(PolicyEditorConstants.ACTION_ID_DEFAULT);
//                    ruleElementDTO.setFunctionOnActions(getBasicPolicyEditorFunction(dto.
//                                                                    getFunctionOnActions()));
//                }
//
//                if(dto.getEnvironmentValue() != null && dto.getEnvironmentValue().trim().length() > 0 &&
//                    !PolicyEditorConstants.FunctionIdentifier.ANY.equals(dto.getEnvironmentValue().trim())){
//                    ruleElementDTO.setEnvironmentId(dto.getEnvironmentId());
//                    ruleElementDTO.setEnvironmentList(dto.getEnvironmentValue());
//                    ruleElementDTO.setEnvironmentDataType(PolicyEditorConstants.DataType.STRING);
//                    ruleElementDTO.setFunctionOnEnvironment(getBasicPolicyEditorFunction(dto.
//                                                                getFunctionOnEnvironments()));
//                }
//
//                if(dto.getOperationType() != null && PolicyEditorConstants.PreFunctions.CAN_DO.
//                                                        equals(dto.getOperationType().trim())){
//                    ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_PERMIT);
//                } else {
//                    ruleElementDTO.setRuleEffect(PolicyEditorConstants.RULE_EFFECT_DENY);
//                }
//                ruleElementDTO.setRuleId("Rule-" + System.currentTimeMillis() + "-" + ruleNo);
//                ruleElementDTOs.add(ruleElementDTO);
//                ruleNo ++;
//            }
//        }
//
//         if(ruleElementDTOs.size() > 0){
//            for(BasicRuleDTO dto : ruleElementDTOs){
//                Element rule = null;
//                try {
//                    rule = BasicPolicyHelper.createRuleElement(dto, doc);
//                } catch (PolicyBuilderException e) {
//                    throw new PolicyEditorException("Error while creating rule element");
//                }
//                doc.appendChild(rule);
//            }
//        }
//
//        return PolicyCreatorUtil.getStringFromDocument(doc);
//    }


    /**
     * Creates DOM representation of the XACML rule element.
     *
     * @param ruleDTO RuleDTO
     * @return
     * @throws PolicyEditorException throws
     */
    public static RuleElementDTO createRuleElementDTO(RuleDTO ruleDTO) throws PolicyEditorException {

        RuleElementDTO ruleElementDTO = new RuleElementDTO();

        ruleElementDTO.setRuleId(ruleDTO.getRuleId());
        ruleElementDTO.setRuleEffect(ruleDTO.getRuleEffect());
        TargetDTO targetDTO = ruleDTO.getTargetDTO();
        List<ExtendAttributeDTO> dynamicAttributeDTOs = ruleDTO.getAttributeDTOs();
        List<ObligationDTO> obligationDTOs = ruleDTO.getObligationDTOs();

        if (dynamicAttributeDTOs != null && dynamicAttributeDTOs.size() > 0) {
            Map<String, ExtendAttributeDTO> dtoMap = new HashMap<String, ExtendAttributeDTO>();
            //1st creating map of dynamic attribute elements
            for (ExtendAttributeDTO dto : dynamicAttributeDTOs) {
                dtoMap.put("${" + dto.getId().trim() + "}", dto);
            }
            //creating map of apply element with identifier
            for (ExtendAttributeDTO dto : dynamicAttributeDTOs) {
                ApplyElementDTO applyElementDTO = createApplyElement(dto, dtoMap);
                if (applyElementDTO == null) {
                    continue;
                }
                applyElementMap.put("${" + dto.getId().trim() + "}", applyElementDTO);
            }
        }

        if (targetDTO != null && targetDTO.getRowDTOList() != null && targetDTO.getRowDTOList().size() > 0) {
            TargetElementDTO targetElementDTO = createTargetElementDTO(ruleDTO.getTargetDTO());
            if (targetElementDTO != null) {
                ruleElementDTO.setTargetElementDTO(targetElementDTO);
            }
        }

        if (ruleDTO.getRowDTOList() != null && ruleDTO.getRowDTOList().size() > 0) {
            ConditionElementDT0 conditionElementDT0 = createConditionDTO(ruleDTO.getRowDTOList());
            if (conditionElementDT0 != null) {
                ruleElementDTO.setConditionElementDT0(conditionElementDT0);
            }
        }

        if (obligationDTOs != null && obligationDTOs.size() > 0) {
            for (ObligationDTO obligationDTO : obligationDTOs) {
                ObligationElementDTO elementDTO = createObligationElement(obligationDTO);
                if (elementDTO != null) {
                    ruleElementDTO.addObligationElementDTO(elementDTO);
                }
            }
        }

        return ruleElementDTO;
    }

    /**
     * creates DOM representation of the XACML obligation/advice element.
     *
     * @param obligationDTOs List  of ObligationDTO
     * @return
     * @throws PolicyEditorException throws
     */
    public static List<ObligationElementDTO> createObligation(List<ObligationDTO> obligationDTOs)
            throws PolicyEditorException {

        List<ObligationElementDTO> obligationElementDTOs = new ArrayList<ObligationElementDTO>();

        if (obligationDTOs != null) {
            for (ObligationDTO obligationDTO : obligationDTOs) {
                ObligationElementDTO elementDTO = createObligationElement(obligationDTO);
                if (elementDTO != null) {
                    obligationElementDTOs.add(elementDTO);
                }
            }
        }

        return obligationElementDTOs;
    }


    /**
     * @param dynamicAttributeDTO
     * @param map
     * @return
     */
    private static ApplyElementDTO createApplyElement(ExtendAttributeDTO dynamicAttributeDTO,
                                                      Map<String, ExtendAttributeDTO> map) {

        if (PolicyEditorConstants.DYNAMIC_SELECTOR_CATEGORY.equals(dynamicAttributeDTO.getSelector())) {

            String category = dynamicAttributeDTO.getCategory();
            String attributeId = dynamicAttributeDTO.getAttributeId();
            String attributeDataType = dynamicAttributeDTO.getDataType();

            if (category != null && category.trim().length() > 0 && attributeDataType != null &&
                    attributeDataType.trim().length() > 0) {
                AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
                designatorDTO.setCategory(category);
                designatorDTO.setAttributeId(attributeId);
                designatorDTO.setDataType(attributeDataType);
                designatorDTO.setMustBePresent("true");

                ApplyElementDTO applyElementDTO = new ApplyElementDTO();
                applyElementDTO.setAttributeDesignators(designatorDTO);
                applyElementDTO.setFunctionId(processFunction("bag", attributeDataType));
                return applyElementDTO;
            }

        } else {

            String function = dynamicAttributeDTO.getFunction();
            String attributeValue = dynamicAttributeDTO.getAttributeValue();
            String attributeDataType = dynamicAttributeDTO.getDataType();

            if (attributeValue != null && function != null) {
                String[] values = attributeValue.split(",");

                if (values != null && values.length > 0) {

                    if (function.contains("concatenate")) {
                        ApplyElementDTO applyElementDTO = new ApplyElementDTO();
                        applyElementDTO.setFunctionId(processFunction(function, attributeDataType, "2.0"));
                        // there can be any number of inputs
                        for (String value : values) {
                            if (map.containsKey(value)) {
                                applyElementDTO.setApplyElement(createApplyElement(map.get(value), map));
                            } else {
                                AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                                valueElementDTO.setAttributeDataType(attributeDataType);
                                valueElementDTO.setAttributeValue(value);
                                applyElementDTO.setAttributeValueElementDTO(valueElementDTO);
                            }
                        }

                        return applyElementDTO;
                    }
                }
            }
        }

        return null;
    }


    private static ObligationElementDTO createObligationElement(ObligationDTO obligationDTO) {

        String id = obligationDTO.getObligationId();
        String effect = obligationDTO.getEffect();
        String type = obligationDTO.getType();

        if (id != null && id.trim().length() > 0 && effect != null) {

            ObligationElementDTO elementDTO = new ObligationElementDTO();
            elementDTO.setId(id);
            elementDTO.setEffect(effect);
            if ("Advice".equals(type)) {
                elementDTO.setType(ObligationElementDTO.ADVICE);
            } else {
                elementDTO.setType(ObligationElementDTO.OBLIGATION);
            }

            String attributeValue = obligationDTO.getAttributeValue();
            String attributeDataType = obligationDTO.getAttributeValueDataType();
            String resultingAttributeId = obligationDTO.getResultAttributeId();

            if (attributeValue != null && attributeValue.trim().length() > 0 &&
                    resultingAttributeId != null && resultingAttributeId.trim().length() > 0) {

                AttributeAssignmentElementDTO assignmentElementDTO = new
                        AttributeAssignmentElementDTO();
                assignmentElementDTO.setAttributeId(resultingAttributeId);
                if (attributeValue.contains(",")) {
                    String[] values = attributeValue.split(",");
                    ApplyElementDTO applyElementDTO = new ApplyElementDTO();
                    applyElementDTO.setFunctionId(processFunction("bag", attributeDataType));
                    for (String value : values) {
                        if (applyElementMap.containsKey(value)) {
                            applyElementDTO.setApplyElement(applyElementMap.get(value));
                        } else {
                            AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                            valueElementDTO.setAttributeDataType(attributeDataType);
                            valueElementDTO.setAttributeValue(value);
                            applyElementDTO.setAttributeValueElementDTO(valueElementDTO);
                        }
                    }
                    assignmentElementDTO.setApplyElementDTO(applyElementDTO);
                } else {
                    if (applyElementMap.containsKey(attributeValue)) {
                        assignmentElementDTO.setApplyElementDTO(applyElementMap.get(attributeValue));
                    } else {
                        AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                        valueElementDTO.setAttributeDataType(attributeDataType);
                        valueElementDTO.setAttributeValue(attributeValue);
                        assignmentElementDTO.setValueElementDTO(valueElementDTO);
                    }
                }

                elementDTO.addAssignmentElementDTO(assignmentElementDTO);
            }

            return elementDTO;
        }

        return null;
    }

    /**
     * Creates <code>ConditionElementDT0</code> Object that represents the XACML Condition element
     *
     * @param rowDTOs
     * @return
     * @throws PolicyEditorException
     */
    public static ConditionElementDT0 createConditionDTO(List<RowDTO> rowDTOs) throws PolicyEditorException {

        ConditionElementDT0 rootApplyDTO = new ConditionElementDT0();

        ArrayList<RowDTO> temp = new ArrayList<RowDTO>();
        Set<ArrayList<RowDTO>> listSet = new HashSet<ArrayList<RowDTO>>();

        for (int i = 0; i < rowDTOs.size(); i++) {

            if (i == 0) {
                temp.add(rowDTOs.get(0));
                continue;
            }

            String combineFunction = rowDTOs.get(i - 1).getCombineFunction();

            if (PolicyEditorConstants.COMBINE_FUNCTION_AND.equals(combineFunction)) {
                temp.add(rowDTOs.get(i));
            }

            if (PolicyEditorConstants.COMBINE_FUNCTION_OR.equals(combineFunction)) {
                listSet.add(temp);
                temp = new ArrayList<RowDTO>();
                temp.add(rowDTOs.get(i));
            }
        }

        listSet.add(temp);

        if (listSet.size() > 1) {
            ApplyElementDTO orApplyDTO = new ApplyElementDTO();
            orApplyDTO.setFunctionId(processFunction("or"));
            for (ArrayList<RowDTO> rowDTOArrayList : listSet) {
                if (rowDTOArrayList.size() > 1) {
                    ApplyElementDTO andApplyDTO = new ApplyElementDTO();
                    andApplyDTO.setFunctionId(processFunction("and"));
                    for (RowDTO rowDTO : rowDTOArrayList) {
                        ApplyElementDTO applyElementDTO = createApplyElement(rowDTO);
                        andApplyDTO.setApplyElement(applyElementDTO);
                    }
                    orApplyDTO.setApplyElement(andApplyDTO);

                } else if (rowDTOArrayList.size() == 1) {
                    RowDTO rowDTO = rowDTOArrayList.get(0);
                    ApplyElementDTO andApplyDTO = createApplyElement(rowDTO);
                    orApplyDTO.setApplyElement(andApplyDTO);
                }
            }
            rootApplyDTO.setApplyElement(orApplyDTO);
        } else if (listSet.size() == 1) {
            ArrayList<RowDTO> rowDTOArrayList = listSet.iterator().next();
            if (rowDTOArrayList.size() > 1) {
                ApplyElementDTO andApplyDTO = new ApplyElementDTO();
                andApplyDTO.setFunctionId(processFunction("and"));
                for (RowDTO rowDTO : rowDTOArrayList) {
                    ApplyElementDTO applyElementDTO = createApplyElement(rowDTO);
                    andApplyDTO.setApplyElement(applyElementDTO);
                }
                rootApplyDTO.setApplyElement(andApplyDTO);
            } else if (rowDTOArrayList.size() == 1) {
                RowDTO rowDTO = rowDTOArrayList.get(0);
                ApplyElementDTO andApplyDTO = createApplyElement(rowDTO);
                rootApplyDTO.setApplyElement(andApplyDTO);
            }
        }

        return rootApplyDTO;
    }

    /**
     * Creates <code>ApplyElementDTO</code> Object that represents the XACML Apply element
     *
     * @param rowDTO
     * @return
     * @throws PolicyEditorException
     */
    public static ApplyElementDTO createApplyElement(RowDTO rowDTO) throws PolicyEditorException {

        String preFunction = rowDTO.getPreFunction();
        String function = rowDTO.getFunction();
        String dataType = rowDTO.getAttributeDataType();
        String attributeValue = rowDTO.getAttributeValue();

        if (function == null || function.trim().length() < 1) {
            throw new PolicyEditorException("Can not create Apply element:" +
                    "Missing required function Id");
        }

        if (attributeValue == null || attributeValue.trim().length() < 1) {
            throw new PolicyEditorException("Can not create Apply element:" +
                    "Missing required attribute value");
        }

        ApplyElementDTO applyElementDTO = null;

        AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
        designatorDTO.setCategory(rowDTO.getCategory());
        designatorDTO.setAttributeId(rowDTO.getAttributeId());
        designatorDTO.setDataType(dataType);
        designatorDTO.setMustBePresent("true");


        if (rowDTO.getFunction().contains("less") || rowDTO.getFunction().contains("greater")) {
            applyElementDTO = processGreaterLessThanFunctions(function, dataType, attributeValue,
                    designatorDTO);
        } else if (PolicyConstants.Functions.FUNCTION_EQUAL.equals(rowDTO.getFunction())) {
            applyElementDTO = processEqualFunctions(function, dataType, attributeValue, designatorDTO);
        } else if (PolicyConstants.Functions.FUNCTION_EQUAL_MATCH_REGEXP.equals(rowDTO.getFunction())) {
            applyElementDTO = processRegexpFunctions(function, dataType, attributeValue, designatorDTO);
        } else {
            applyElementDTO = processBagFunction(function, dataType, attributeValue, designatorDTO);
        }


        if (PolicyConstants.PreFunctions.PRE_FUNCTION_NOT.equals(preFunction)) {
            ApplyElementDTO notApplyElementDTO = new ApplyElementDTO();
            notApplyElementDTO.setFunctionId(processFunction("not"));
            notApplyElementDTO.setApplyElement(applyElementDTO);
            applyElementDTO = notApplyElementDTO;
        }

        return applyElementDTO;
    }

    /**
     * Creates <code>TargetElementDTO</code> Object that represents the XACML Target element
     *
     * @param targetDTO
     * @return
     */
    public static TargetElementDTO createTargetElementDTO(TargetDTO targetDTO) {

        AllOfElementDTO allOfElementDTO = new AllOfElementDTO();
        AnyOfElementDTO anyOfElementDTO = new AnyOfElementDTO();
        TargetElementDTO targetElementDTO = new TargetElementDTO();

        List<RowDTO> rowDTOs = targetDTO.getRowDTOList();
        ArrayList<RowDTO> tempRowDTOs = new ArrayList<RowDTO>();

        // pre function processing
        for (RowDTO rowDTO : rowDTOs) {
            if (PolicyEditorConstants.PreFunctions.PRE_FUNCTION_ARE.equals(rowDTO.getPreFunction())) {
                String[] attributeValues = rowDTO.getAttributeValue().split(PolicyEditorConstants.ATTRIBUTE_SEPARATOR);
                allOfElementDTO = new AllOfElementDTO();
                for (int j = 0; j < attributeValues.length; j++) {
                    RowDTO newDto = new RowDTO(rowDTO);
                    newDto.setAttributeValue(attributeValues[j]);
                    if (j != attributeValues.length - 1) {
                        newDto.setCombineFunction(PolicyEditorConstants.COMBINE_FUNCTION_AND);
                    }
                    tempRowDTOs.add(newDto);
                }
            } else {
                tempRowDTOs.add(rowDTO);
            }
        }

        if (tempRowDTOs.size() > 0) {
            for (int i = 0; i < tempRowDTOs.size(); i++) {
                if (i == 0) {
                    MatchElementDTO matchElementDTO = createTargetMatch(tempRowDTOs.get(0));
                    if (matchElementDTO != null) {
                        allOfElementDTO.addMatchElementDTO(matchElementDTO);
                    }
                    continue;
                }

                String combineFunction = tempRowDTOs.get(i - 1).getCombineFunction();

                if (PolicyEditorConstants.COMBINE_FUNCTION_AND.equals(combineFunction)) {
                    MatchElementDTO matchElementDTO = createTargetMatch(tempRowDTOs.get(i));
                    if (matchElementDTO != null) {
                        allOfElementDTO.addMatchElementDTO(matchElementDTO);
                    }

                }

                if (PolicyEditorConstants.COMBINE_FUNCTION_OR.equals(combineFunction)) {
                    anyOfElementDTO.addAllOfElementDTO(allOfElementDTO);
                    allOfElementDTO = new AllOfElementDTO();
                    MatchElementDTO matchElementDTO = createTargetMatch(tempRowDTOs.get(i));
                    if (matchElementDTO != null) {
                        allOfElementDTO.addMatchElementDTO(matchElementDTO);
                    }
                }
            }
            anyOfElementDTO.addAllOfElementDTO(allOfElementDTO);
            targetElementDTO.addAnyOfElementDTO(anyOfElementDTO);
        }
        return targetElementDTO;
    }


    /**
     * process Bag functions
     *
     * @param function
     * @param dataType
     * @param attributeValue
     * @param designatorDTO
     * @return
     */
    public static ApplyElementDTO processBagFunction(String function, String dataType,
                                                     String attributeValue, AttributeDesignatorDTO designatorDTO) {

        if (PolicyConstants.Functions.FUNCTION_IS_IN.equals(function)) {
            ApplyElementDTO applyElementDTO = new ApplyElementDTO();
            applyElementDTO.setFunctionId(processFunction("is-in", dataType));
            if (applyElementMap.containsKey(attributeValue)) {
                applyElementDTO.setApplyElement(applyElementMap.get(attributeValue));
            } else {
                AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                valueElementDTO.setAttributeDataType(dataType);
                valueElementDTO.setAttributeValue(attributeValue);
                applyElementDTO.setAttributeValueElementDTO(valueElementDTO);
            }

            applyElementDTO.setAttributeDesignators(designatorDTO);
            return applyElementDTO;

        } else if (PolicyConstants.Functions.FUNCTION_AT_LEAST_ONE.equals(function) ||
                PolicyConstants.Functions.FUNCTION_SET_EQUALS.equals(function)) {

            ApplyElementDTO applyElementDTO = new ApplyElementDTO();
            if (PolicyConstants.Functions.FUNCTION_AT_LEAST_ONE.equals(function)) {
                applyElementDTO.setFunctionId(processFunction("at-least-one-member-of", dataType));
            } else {
                applyElementDTO.setFunctionId(processFunction("set-equals", dataType));
            }

            String[] values = attributeValue.split(PolicyEditorConstants.ATTRIBUTE_SEPARATOR);

            ApplyElementDTO applyBagElementDTO = new ApplyElementDTO();
            applyBagElementDTO.setFunctionId(processFunction("bag", dataType));
            for (String value : values) {
                if (applyElementMap.containsKey(value)) {
                    applyBagElementDTO.setApplyElement(applyElementMap.get(value));
                } else {
                    AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                    valueElementDTO.setAttributeDataType(dataType);
                    valueElementDTO.setAttributeValue(value);
                    applyBagElementDTO.setAttributeValueElementDTO(valueElementDTO);
                }
            }

            applyElementDTO.setAttributeDesignators(designatorDTO);
            applyElementDTO.setApplyElement(applyBagElementDTO);

            return applyElementDTO;
        }

        return null;
    }

    /**
     * Process  equal function
     *
     * @param function
     * @param dataType
     * @param attributeValue
     * @param designatorDTO
     * @return
     */
    public static ApplyElementDTO processEqualFunctions(String function, String dataType,
                                                        String attributeValue, AttributeDesignatorDTO designatorDTO) {

        if (PolicyConstants.Functions.FUNCTION_EQUAL.equals(function)) {

            ApplyElementDTO applyElementDTO = new ApplyElementDTO();
            if (PolicyEditorConstants.DataType.DAY_TIME_DURATION.equals(dataType) ||
                    PolicyEditorConstants.DataType.YEAR_MONTH_DURATION.equals(dataType)) {
                applyElementDTO.setFunctionId(processFunction("equal", dataType, "3.0"));
            } else {
                applyElementDTO.setFunctionId(processFunction("equal", dataType));
            }

            ApplyElementDTO oneAndOnlyApplyElement = new ApplyElementDTO();
            oneAndOnlyApplyElement.setFunctionId(processFunction("one-and-only", dataType));
            oneAndOnlyApplyElement.setAttributeDesignators(designatorDTO);

            if (applyElementMap.containsKey(attributeValue)) {
                applyElementDTO.setApplyElement(applyElementMap.get(attributeValue));
            } else {
                AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                valueElementDTO.setAttributeDataType(dataType);
                valueElementDTO.setAttributeValue(attributeValue);
                applyElementDTO.setAttributeValueElementDTO(valueElementDTO);
            }

            applyElementDTO.setApplyElement(oneAndOnlyApplyElement);

            return applyElementDTO;
        }

        return null;
    }

    /**
     * Process less than and greater than functions
     *
     * @param function
     * @param dataType
     * @param attributeValue
     * @param designatorDTO
     * @return
     * @throws PolicyEditorException
     */
    public static ApplyElementDTO processGreaterLessThanFunctions(String function, String dataType,
                                                                  String attributeValue, AttributeDesignatorDTO designatorDTO)
            throws PolicyEditorException {

        String[] values = attributeValue.split(PolicyEditorConstants.ATTRIBUTE_SEPARATOR);


        if (PolicyConstants.Functions.FUNCTION_GREATER_EQUAL_AND_LESS_EQUAL.equals(function) ||
                PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS_EQUAL.equals(function) ||
                PolicyConstants.Functions.FUNCTION_GREATER_EQUAL_AND_LESS.equals(function) ||
                PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS.equals(function)) {

            String leftValue;
            String rightValue;

            if (values.length == 2) {
                leftValue = values[0].trim();
                rightValue = values[1].trim();
            } else {
                throw new PolicyEditorException("Can not create Apply element:" +
                        "Missing required attribute values for function : " + function);
            }

            ApplyElementDTO andApplyElement = new ApplyElementDTO();

            andApplyElement.setFunctionId(processFunction("and"));

            ApplyElementDTO greaterThanApplyElement = new ApplyElementDTO();
            if (PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS.equals(function) ||
                    PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS_EQUAL.equals(function)) {
                greaterThanApplyElement.setFunctionId(processFunction("greater-than", dataType));
            } else {
                greaterThanApplyElement.setFunctionId(processFunction("greater-than-or-equal", dataType));
            }


            ApplyElementDTO lessThanApplyElement = new ApplyElementDTO();
            if (PolicyConstants.Functions.FUNCTION_GREATER_AND_LESS.equals(function) ||
                    PolicyConstants.Functions.FUNCTION_GREATER_EQUAL_AND_LESS.equals(function)) {
                lessThanApplyElement.setFunctionId(processFunction("less-than", dataType));
            } else {
                lessThanApplyElement.setFunctionId(processFunction("less-than-or-equal", dataType));
            }

            ApplyElementDTO oneAndOnlyApplyElement = new ApplyElementDTO();
            oneAndOnlyApplyElement.setFunctionId(processFunction("one-and-only", dataType));
            oneAndOnlyApplyElement.setAttributeDesignators(designatorDTO);

            AttributeValueElementDTO leftValueElementDTO = new AttributeValueElementDTO();
            leftValueElementDTO.setAttributeDataType(dataType);
            leftValueElementDTO.setAttributeValue(leftValue);

            AttributeValueElementDTO rightValueElementDTO = new AttributeValueElementDTO();
            rightValueElementDTO.setAttributeDataType(dataType);
            rightValueElementDTO.setAttributeValue(rightValue);

            greaterThanApplyElement.setApplyElement(oneAndOnlyApplyElement);
            greaterThanApplyElement.setAttributeValueElementDTO(leftValueElementDTO);

            lessThanApplyElement.setApplyElement(oneAndOnlyApplyElement);
            lessThanApplyElement.setAttributeValueElementDTO(rightValueElementDTO);

            andApplyElement.setApplyElement(greaterThanApplyElement);
            andApplyElement.setApplyElement(lessThanApplyElement);

            return andApplyElement;

        } else {

            ApplyElementDTO applyElementDTO = new ApplyElementDTO();

            if (PolicyConstants.Functions.FUNCTION_GREATER.equals(function)) {
                applyElementDTO.setFunctionId(processFunction("greater-than", dataType));
            } else if (PolicyConstants.Functions.FUNCTION_GREATER_EQUAL.equals(function)) {
                applyElementDTO.setFunctionId(processFunction("greater-than-or-equal", dataType));
            } else if (PolicyConstants.Functions.FUNCTION_LESS.equals(function)) {
                applyElementDTO.setFunctionId(processFunction("less-than", dataType));
            } else if (PolicyConstants.Functions.FUNCTION_LESS_EQUAL.equals(function)) {
                applyElementDTO.setFunctionId(processFunction("less-than-or-equal", dataType));
            } else {
                throw new PolicyEditorException("Can not create Apply element:" +
                        "Invalid function : " + function);
            }

            ApplyElementDTO oneAndOnlyApplyElement = new ApplyElementDTO();
            oneAndOnlyApplyElement.setFunctionId(processFunction("one-and-only", dataType));
            oneAndOnlyApplyElement.setAttributeDesignators(designatorDTO);

            AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
            valueElementDTO.setAttributeDataType(dataType);
            valueElementDTO.setAttributeValue(values[0]);

            applyElementDTO.setApplyElement(oneAndOnlyApplyElement);
            applyElementDTO.setAttributeValueElementDTO(valueElementDTO);

            return applyElementDTO;

        }
    }

    /**
     * Process regexp-match functions.
     *
     * @param function       Function name.
     * @param dataType       Data type.
     * @param attributeValue Attribute Value.
     * @param designatorDTO  AttributeDesignator information.
     * @return ApplyElementDTO.
     */
    public static ApplyElementDTO processRegexpFunctions(String function, String dataType, String attributeValue,
                                                         AttributeDesignatorDTO designatorDTO) {

        if (PolicyConstants.Functions.FUNCTION_EQUAL_MATCH_REGEXP.equals(function)) {
            ApplyElementDTO applyElementDTO = new ApplyElementDTO();
            applyElementDTO.setFunctionId(PolicyConstants.XACMLData.FUNCTION_ANY_OF);
            if (applyElementMap.containsKey(attributeValue)) {
                applyElementDTO.setApplyElement(applyElementMap.get(attributeValue));
            } else {
                AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
                valueElementDTO.setAttributeDataType(dataType);
                valueElementDTO.setAttributeValue(attributeValue);
                applyElementDTO.setAttributeValueElementDTO(valueElementDTO);
            }
            applyElementDTO.setFunctionFunctionId(
                    processFunction(PolicyConstants.Functions.FUNCTION_EQUAL_MATCH_REGEXP, dataType));
            applyElementDTO.setAttributeDesignators(designatorDTO);
            return applyElementDTO;
        }
        return null;
    }

    /**
     * Helper method to create full XACML function URI
     *
     * @param function
     * @param type
     * @param version
     * @return
     */
    private static String processFunction(String function, String type, String version) {
        return "urn:oasis:names:tc:xacml:" + version + ":function:" + getDataTypePrefix(type) +
                "-" + function;
    }

    /**
     * Helper method to create full XACML function URI
     *
     * @param function
     * @return
     */
    private static String processFunction(String function) {
        return "urn:oasis:names:tc:xacml:1.0:function:" + function;
    }

    /**
     * Helper method to create full XACML function URI
     *
     * @param function
     * @param type
     * @return
     */
    private static String processFunction(String function, String type) {
        return "urn:oasis:names:tc:xacml:1.0:function:" + getDataTypePrefix(type) + "-" + function;
    }
//
//    /**
//     * Helper method to check whether attribute value is pre-defined one
//     *
//     * @param value
//     * @return
//     */
//    private static boolean isPreDefinedValue(String value){
//
//        if(value != null && applyElementMap != null && applyElementMap.size() > 0){
//            value = value.trim();
//            if(value.startsWith("${") && value.endsWith("}")){
//                value = value.substring(value.indexOf("{") + 1, value.indexOf("}"));
//                return applyElementMap.containsKey(value);
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * Helper method to check whether attribute value is pre-defined one
//     *
//     * @param value
//     * @param map
//     * @return
//     */
//    private static boolean isPreDefinedValue(String value, Map<String, ExtendAttributeDTO> map){
//
//        if(value != null && map != null && map.size() > 0){
//            value = value.trim();
//            if(value.startsWith("${") && value.endsWith("}")){
//                value = value.substring(value.indexOf("{") + 1, value.indexOf("}"));
//                return map.containsKey(value);
//            }
//        }
//
//        return false;
//    }

    /**
     * Helper method to create full XACML function URI
     *
     * @param dataTypeUri
     * @return
     */
    private static String getDataTypePrefix(String dataTypeUri) {

        if (dataTypeUri != null) {
            if (dataTypeUri.contains("#")) {
                return dataTypeUri.substring(dataTypeUri.indexOf("#") + 1);
            } else if (dataTypeUri.contains(":")) {
                String[] stringArray = dataTypeUri.split(":");
                if (stringArray != null && stringArray.length > 0) {
                    return stringArray[stringArray.length - 1];
                }
            }
        }
        return dataTypeUri;
    }

    /**
     * Creates match element
     *
     * @param rowDTO
     * @return
     */
    public static MatchElementDTO createTargetMatch(RowDTO rowDTO) {


        String category = rowDTO.getCategory();
        String functionId = rowDTO.getFunction();
        String attributeValue = rowDTO.getAttributeValue();
        String attributeId = rowDTO.getAttributeId();
        String dataType = rowDTO.getAttributeDataType();
        MatchElementDTO matchElementDTO;

        if (functionId != null && functionId.trim().length() > 0 && attributeValue != null &&
                attributeValue.trim().length() > 0 && category != null &&
                category.trim().length() > 0 && attributeId != null &&
                attributeId.trim().length() > 0 && dataType != null &&
                dataType.trim().length() > 0) {

            functionId = processFunction(functionId, dataType);

            matchElementDTO = new MatchElementDTO();

            AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
            attributeValueElementDTO.setAttributeDataType(dataType);
            attributeValueElementDTO.setAttributeValue(attributeValue.trim());

            AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
            attributeDesignatorDTO.setDataType(dataType);
            attributeDesignatorDTO.setAttributeId(attributeId);
            attributeDesignatorDTO.setCategory(category);

            matchElementDTO.setMatchId(functionId);
            matchElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
            matchElementDTO.setAttributeDesignatorDTO(attributeDesignatorDTO);
        } else {
            return null; // TODO
        }

        return matchElementDTO;
    }


    /**
     * This method creates a match element (such as subject,action,resource or environment) of the XACML policy
     *
     * @param matchElementDTO match element data object
     * @param doc             XML document
     * @return match Element
     * @throws PolicyEditorException if any error occurs
     */
    public static Element createMatchElement(MatchElementDTO matchElementDTO, Document doc)
            throws PolicyEditorException {

        Element matchElement;

        if (matchElementDTO.getMatchId() != null && matchElementDTO.getMatchId().trim().length() > 0) {

            matchElement = doc.createElement(PolicyEditorConstants.MATCH_ELEMENT);

            matchElement.setAttribute(PolicyEditorConstants.MATCH_ID,
                    matchElementDTO.getMatchId());

            if (matchElementDTO.getAttributeValueElementDTO() != null) {
                Element attributeValueElement = createAttributeValueElement(matchElementDTO.
                        getAttributeValueElementDTO(), doc);
                matchElement.appendChild(attributeValueElement);
            }

            if (matchElementDTO.getAttributeDesignatorDTO() != null) {
                Element attributeDesignatorElement = createAttributeDesignatorElement(matchElementDTO.
                        getAttributeDesignatorDTO(), doc);
                matchElement.appendChild(attributeDesignatorElement);
            } else if (matchElementDTO.getAttributeSelectorDTO() != null) {
                Element attributeSelectorElement = createAttributeSelectorElement(matchElementDTO.
                        getAttributeSelectorDTO(), doc);
                matchElement.appendChild(attributeSelectorElement);
            }
        } else {
            throw new PolicyEditorException("Can not create Match element:" +
                    " Required Attributes are missing");
        }
        return matchElement;
    }

    /**
     * This method creates attribute value DOM element
     *
     * @param attributeValueElementDTO attribute value element data object
     * @param doc                      XML document
     * @return attribute value element as DOM
     */
    public static Element createAttributeValueElement(AttributeValueElementDTO
                                                              attributeValueElementDTO, Document doc) {

        Element attributeValueElement = doc.createElement(EntitlementPolicyConstants.ATTRIBUTE_VALUE);

        if (attributeValueElementDTO.getAttributeValue() != null && attributeValueElementDTO.
                getAttributeValue().trim().length() > 0) {

            attributeValueElement.setTextContent(attributeValueElementDTO.getAttributeValue().trim());

            if (attributeValueElementDTO.getAttributeDataType() != null && attributeValueElementDTO.
                    getAttributeDataType().trim().length() > 0) {
                attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                        attributeValueElementDTO.getAttributeDataType());
            } else {
                attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                        EntitlementPolicyConstants.STRING_DATA_TYPE);
            }

        }

        return attributeValueElement;
    }

    /**
     * This method creates attribute designator DOM element
     *
     * @param attributeDesignatorDTO attribute designator data object
     * @param doc                    XML document
     * @return attribute designator element as DOM
     * @throws PolicyEditorException throws if missing required data
     */
    public static Element createAttributeDesignatorElement(AttributeDesignatorDTO
                                                                   attributeDesignatorDTO, Document doc) throws PolicyEditorException {

        Element attributeDesignatorElement;

        if (attributeDesignatorDTO != null && doc != null) {

            String category = attributeDesignatorDTO.getCategory();
            String attributeId = attributeDesignatorDTO.getAttributeId();
            String dataType = attributeDesignatorDTO.getDataType();
            String mustBe = attributeDesignatorDTO.getMustBePresent();

            if (category != null && category.trim().length() > 0 && attributeId != null &&
                    attributeId.trim().length() > 0 && dataType != null && dataType.trim().length() > 0 &&
                    mustBe != null && mustBe.trim().length() > 0) {

                attributeDesignatorElement = doc.
                        createElement(PolicyEditorConstants.ATTRIBUTE_DESIGNATOR);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.ATTRIBUTE_ID,
                        attributeId);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.CATEGORY, category);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.DATA_TYPE, dataType);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.MUST_BE_PRESENT, mustBe);

                if (attributeDesignatorDTO.getIssuer() != null && attributeDesignatorDTO.getIssuer().
                        trim().length() > 0) {
                    attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.ISSUER,
                            attributeDesignatorDTO.getIssuer());
                }
            } else {
                throw new PolicyEditorException("Can not create AttributeDesignator element:" +
                        " Required Attributes are missing");
            }
        } else {
            throw new PolicyEditorException("Can not create AttributeDesignator element:" +
                    " A Null object is received");
        }
        return attributeDesignatorElement;
    }

    /**
     * This method creates attribute selector DOM element
     *
     * @param attributeSelectorDTO attribute selector data object
     * @param doc                  xML document
     * @return attribute selector element as DOM
     */
    public static Element createAttributeSelectorElement(AttributeSelectorDTO attributeSelectorDTO,
                                                         Document doc) {

        Element attributeSelectorElement = doc.createElement(EntitlementPolicyConstants.
                ATTRIBUTE_SELECTOR);

        if (attributeSelectorDTO.getAttributeSelectorRequestContextPath() != null &&
                attributeSelectorDTO.getAttributeSelectorRequestContextPath().trim().length() > 0) {

            attributeSelectorElement.setAttribute(EntitlementPolicyConstants.REQUEST_CONTEXT_PATH,
                    EntitlementPolicyConstants.ATTRIBUTE_NAMESPACE + attributeSelectorDTO.
                            getAttributeSelectorRequestContextPath());

            if (attributeSelectorDTO.getAttributeSelectorDataType() != null &&
                    attributeSelectorDTO.getAttributeSelectorDataType().trim().length() > 0) {
                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                        attributeSelectorDTO.getAttributeSelectorDataType());
            } else {
                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                        EntitlementPolicyConstants.STRING_DATA_TYPE);
            }

            if (attributeSelectorDTO.getAttributeSelectorMustBePresent() != null &&
                    attributeSelectorDTO.getAttributeSelectorMustBePresent().trim().length() > 0) {
                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
                        attributeSelectorDTO.getAttributeSelectorMustBePresent());
            }

        }

        return attributeSelectorElement;
    }

    /**
     * Modifies the user data that are got from policy editor. If there are null values for required
     * things, replace them with default values
     */
    public static String[] processPolicySetData(PolicySetDTO policyDTO) {

        TargetDTO targetDTO = policyDTO.getTargetDTO();
        List<ObligationDTO> obligationDTOs = policyDTO.getObligations();
        List<PolicyRefIdDTO> policyRefIdDTOs = policyDTO.getPolicyRefIdDTOs();
        String policyOrder = policyDTO.getPolicyOrder();


        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.SET);

        List<String> policyMetaDataList = new ArrayList<String>();

        List<PolicyRefIdDTO> arrangedRefIdDTOs = new ArrayList<PolicyRefIdDTO>();

        if (policyOrder != null && policyOrder.trim().length() > 0) {
            String[] ruleIds = policyOrder.
                    split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
            for (String ruleId : ruleIds) {
                for (PolicyRefIdDTO dto : policyRefIdDTOs) {
                    if (ruleId.equals(dto.getId())) {
                        arrangedRefIdDTOs.add(dto);
                    }
                }
            }
            policyRefIdDTOs = arrangedRefIdDTOs;
        }
        createMetaDataFromPolicySet("policy", policyDTO, policyMetaDataList);
        String algorithm = policyDTO.getPolicyCombiningAlgId();
        if (algorithm != null && algorithm.trim().length() > 0) {
            policyDTO.setPolicyCombiningAlgId(holder.getPolicyAlgorithmUri(algorithm));
        } else {
            policyDTO.setPolicyCombiningAlgId(holder.getDefaultPolicyAlgorithm());
        }

        if (targetDTO != null && targetDTO.getRowDTOList() != null) {
            List<RowDTO> newRowDTOs = new ArrayList<RowDTO>();
            for (RowDTO rowDTO : targetDTO.getRowDTOList()) {
                createMetaDataFromRowDTO("target", rowDTO, policyMetaDataList);
                String category = rowDTO.getCategory();

                if (category == null) {
                    continue;
                }

                String attributeValue = rowDTO.getAttributeValue();
                if (attributeValue == null || attributeValue.trim().length() < 1) {
                    continue;
                }
                rowDTO.setCategory(holder.getCategoryUri(category));

                if (rowDTO.getAttributeDataType() == null ||
                        rowDTO.getAttributeDataType().trim().length() < 1 ||
                        rowDTO.getAttributeDataType().trim().equals("null")) {

                    if (holder.getDefaultDataType() != null) {
                        rowDTO.setAttributeDataType(holder.getDefaultDataType());
                    } else {
                        rowDTO.setAttributeDataType(PolicyEditorConstants.DataType.STRING);
                    }
                } else {
                    if (holder.getDataTypeUri(rowDTO.getAttributeDataType()) != null) {
                        rowDTO.setAttributeDataType(holder.getDataTypeUri(rowDTO.getAttributeDataType()));
                    }
                }

                String attributeId = rowDTO.getAttributeId();
                if (attributeId == null || attributeId.trim().length() < 1 ||
                        attributeId.trim().equals("null")) {
                    attributeId = holder.getCategoryDefaultAttributeId(category);
                }
                rowDTO.setAttributeId(holder.getAttributeIdUri(attributeId));
                rowDTO.setFunction(holder.getFunctionUri(rowDTO.getFunction()));
                rowDTO.setPreFunction(holder.getPreFunctionUri(rowDTO.getPreFunction()));
                newRowDTOs.add(rowDTO);
            }
            targetDTO.setRowDTOList(newRowDTOs);
            policyDTO.setTargetDTO(targetDTO);
        }

        if (policyRefIdDTOs != null) {
            policyDTO.setPolicyRefIdDTOs(policyRefIdDTOs);
            for (PolicyRefIdDTO dto : policyRefIdDTOs) {
                createMetaDataFromReference("reference", dto, policyMetaDataList);
            }
        }

        if (obligationDTOs != null) {
            for (ObligationDTO dto : obligationDTOs) {
                createMetaDataFromObligation("obligation", dto, policyMetaDataList);
                if (dto.getAttributeValueDataType() == null ||
                        dto.getAttributeValueDataType().trim().length() == 0 ||
                        dto.getAttributeValueDataType().trim().equals("null")) {
                    dto.setAttributeValueDataType(PolicyEditorConstants.DataType.STRING);
                }
                if (dto.getResultAttributeId() == null ||
                        dto.getResultAttributeId().trim().length() == 0 ||
                        dto.getResultAttributeId().trim().equals("null")) {
                    // setting obligation id
                    dto.setResultAttributeId(dto.getObligationId());
                }
            }
            policyDTO.setObligations(obligationDTOs);
        }

        return policyMetaDataList.toArray(new String[policyMetaDataList.size()]);
    }


    /**
     * Modifies the user data that are got from policy editor. If there are null values for required
     * things, replace them with default values
     */
    public static String[] processPolicyData(PolicyDTO policyDTO) {

        TargetDTO targetDTO = policyDTO.getTargetDTO();
        List<RuleDTO> ruleDTOs = policyDTO.getRuleDTOs();
        List<ObligationDTO> obligationDTOs = policyDTO.getObligationDTOs();
        String ruleElementOrder = policyDTO.getRuleOrder();


        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.STANDARD);

        List<String> policyMetaDataList = new ArrayList<String>();

        List<RuleDTO> arrangedRules = new ArrayList<RuleDTO>();

        if (ruleElementOrder != null && ruleElementOrder.trim().length() > 0) {
            String[] ruleIds = ruleElementOrder.
                    split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
            for (String ruleId : ruleIds) {
                for (RuleDTO ruleDTO : ruleDTOs) {
                    if (ruleId.equals(ruleDTO.getRuleId())) {
                        arrangedRules.add(ruleDTO);
                    }
                }
            }
            ruleDTOs = arrangedRules;
        }
        createMetaDataFromPolicy("policy", policyDTO, policyMetaDataList);
        String algorithm = policyDTO.getRuleAlgorithm();
        if (algorithm != null && algorithm.trim().length() > 0) {
            policyDTO.setRuleAlgorithm(holder.getRuleAlgorithmUri(algorithm));
        } else {
            policyDTO.setRuleAlgorithm(holder.getDefaultRuleAlgorithm());
        }

        if (targetDTO != null && targetDTO.getRowDTOList() != null) {
            List<RowDTO> newRowDTOs = new ArrayList<RowDTO>();
            for (RowDTO rowDTO : targetDTO.getRowDTOList()) {
                createMetaDataFromRowDTO("target", rowDTO, policyMetaDataList);
                String category = rowDTO.getCategory();

                if (category == null) {
                    continue;
                }

                String attributeValue = rowDTO.getAttributeValue();
                if (attributeValue == null || attributeValue.trim().length() < 1) {
                    continue;
                }
                rowDTO.setCategory(holder.getCategoryUri(category));

                if (rowDTO.getAttributeDataType() == null ||
                        rowDTO.getAttributeDataType().trim().length() < 1 ||
                        rowDTO.getAttributeDataType().trim().equals("null")) {

                    if (holder.getDefaultDataType() != null) {
                        rowDTO.setAttributeDataType(holder.getDefaultDataType());
                    } else {
                        rowDTO.setAttributeDataType(PolicyEditorConstants.DataType.STRING);
                    }
                } else {
                    if (holder.getDataTypeUri(rowDTO.getAttributeDataType()) != null) {
                        rowDTO.setAttributeDataType(holder.getDataTypeUri(rowDTO.getAttributeDataType()));
                    }
                }

                String attributeId = rowDTO.getAttributeId();
                if (attributeId == null || attributeId.trim().length() < 1 ||
                        attributeId.trim().equals("null")) {
                    attributeId = holder.getCategoryDefaultAttributeId(category);
                }
                rowDTO.setAttributeId(holder.getAttributeIdUri(attributeId));
                rowDTO.setFunction(holder.getFunctionUri(rowDTO.getFunction()));
                rowDTO.setPreFunction(holder.getPreFunctionUri(rowDTO.getPreFunction()));
                newRowDTOs.add(rowDTO);
            }
            targetDTO.setRowDTOList(newRowDTOs);
            policyDTO.setTargetDTO(targetDTO);
        }

        if (ruleDTOs != null) {
            for (RuleDTO ruleDTO : ruleDTOs) {
                createMetaDataFromRule("rule", ruleDTO, policyMetaDataList);
                List<RowDTO> newRowDTOs = new ArrayList<RowDTO>();
                for (RowDTO rowDTO : ruleDTO.getRowDTOList()) {
                    createMetaDataFromRowDTO("ruleRow" + ruleDTO.getRuleId(), rowDTO, policyMetaDataList);
                    String category = rowDTO.getCategory();

                    if (category == null) {
                        continue;
                    }

                    String attributeValue = rowDTO.getAttributeValue();
                    if (attributeValue == null || attributeValue.trim().length() < 1) {
                        continue;
                    }
                    rowDTO.setCategory(holder.getCategoryUri(category));

                    if (rowDTO.getAttributeDataType() == null ||
                            rowDTO.getAttributeDataType().trim().length() < 1 ||
                            rowDTO.getAttributeDataType().trim().equals("null")) {

                        if (holder.getDefaultDataType() != null) {
                            rowDTO.setAttributeDataType(holder.getDefaultDataType());
                        } else {
                            rowDTO.setAttributeDataType(PolicyEditorConstants.DataType.STRING);
                        }
                    } else {
                        if (holder.getDataTypeUri(rowDTO.getAttributeDataType()) != null) {
                            rowDTO.setAttributeDataType(holder.getDataTypeUri(rowDTO.getAttributeDataType()));
                        }
                    }

                    String attributeId = rowDTO.getAttributeId();
                    if (attributeId == null || attributeId.trim().length() < 1 ||
                            attributeId.trim().equals("null")) {
                        attributeId = holder.getCategoryDefaultAttributeId(category);
                    }
                    rowDTO.setAttributeId(holder.getAttributeIdUri(attributeId));
                    rowDTO.setFunction(holder.getFunctionUri(rowDTO.getFunction()));
                    rowDTO.setPreFunction(holder.getPreFunctionUri(rowDTO.getPreFunction()));
                    newRowDTOs.add(rowDTO);
                }

                ruleDTO.setRowDTOList(newRowDTOs);

                TargetDTO ruleTargetDTO = ruleDTO.getTargetDTO();

                if (ruleTargetDTO == null) {
                    continue;
                }

                List<RowDTO> newTargetRowDTOs = new ArrayList<RowDTO>();

                for (RowDTO rowDTO : ruleTargetDTO.getRowDTOList()) {
                    createMetaDataFromRowDTO("ruleTarget" + ruleDTO.getRuleId(), rowDTO, policyMetaDataList);
                    String category = rowDTO.getCategory();

                    if (category == null) {
                        continue;
                    }

                    String attributeValue = rowDTO.getAttributeValue();
                    if (attributeValue == null || attributeValue.trim().length() < 1) {
                        continue;
                    }
                    rowDTO.setCategory(holder.getCategoryUri(category));

                    if (rowDTO.getAttributeDataType() == null ||
                            rowDTO.getAttributeDataType().trim().length() < 1 ||
                            rowDTO.getAttributeDataType().trim().equals("null")) {

                        if (holder.getDefaultDataType() != null) {
                            rowDTO.setAttributeDataType(holder.getDefaultDataType());
                        } else {
                            rowDTO.setAttributeDataType(PolicyEditorConstants.DataType.STRING);
                        }
                    } else {
                        if (holder.getDataTypeUri(rowDTO.getAttributeDataType()) != null) {
                            rowDTO.setAttributeDataType(holder.getDataTypeUri(rowDTO.getAttributeDataType()));
                        }
                    }

                    String attributeId = rowDTO.getAttributeId();
                    if (attributeId == null || attributeId.trim().length() < 1 ||
                            attributeId.trim().equals("null")) {
                        attributeId = holder.getCategoryDefaultAttributeId(category);
                    }
                    rowDTO.setAttributeId(holder.getAttributeIdUri(attributeId));
                    rowDTO.setFunction(holder.getFunctionUri(rowDTO.getFunction()));
                    rowDTO.setPreFunction(holder.getPreFunctionUri(rowDTO.getPreFunction()));
                    newTargetRowDTOs.add(rowDTO);
                }

                ruleTargetDTO.setRowDTOList(newTargetRowDTOs);

                List<ObligationDTO> ruleObligationDTOs = ruleDTO.getObligationDTOs();

                if (ruleObligationDTOs != null) {
                    for (ObligationDTO dto : ruleObligationDTOs) {
                        createMetaDataFromObligation("ruleObligation" + ruleDTO.getRuleId(),
                                dto, policyMetaDataList);
                        if (dto.getAttributeValueDataType() == null ||
                                dto.getAttributeValueDataType().trim().length() < 1 ||
                                dto.getAttributeValueDataType().trim().equals("null")) {
                            dto.setAttributeValueDataType(PolicyEditorConstants.DataType.STRING);
                        }
                        if (dto.getResultAttributeId() == null ||
                                dto.getResultAttributeId().trim().length() == 0 ||
                                dto.getResultAttributeId().trim().equals("null")) {
                            // setting obligation id
                            dto.setResultAttributeId(dto.getObligationId());
                        }
                    }
                    ruleDTO.setObligationDTOs(ruleObligationDTOs);
                }

                ruleDTO.setTargetDTO(ruleTargetDTO);
            }

            policyDTO.setRuleDTOs(ruleDTOs);
        }

        if (obligationDTOs != null) {
            for (ObligationDTO dto : obligationDTOs) {
                createMetaDataFromObligation("obligation", dto, policyMetaDataList);
                if (dto.getAttributeValueDataType() == null ||
                        dto.getAttributeValueDataType().trim().length() == 0 ||
                        dto.getAttributeValueDataType().trim().equals("null")) {
                    dto.setAttributeValueDataType(PolicyEditorConstants.DataType.STRING);
                }
                if (dto.getResultAttributeId() == null ||
                        dto.getResultAttributeId().trim().length() == 0 ||
                        dto.getResultAttributeId().trim().equals("null")) {
                    // setting obligation id
                    dto.setResultAttributeId(dto.getObligationId());
                }
            }
            policyDTO.setObligationDTOs(obligationDTOs);
        }

//        for(ExtendAttributeDTO attributeDTO : ruleDTO.getAttributeDTOs()){
//
//            String id = attributeDTO.getId();
//            String selector = attributeDTO.getSelector();
//            String category = null;
//            String function = null;
//
//            if(id == null){
//                continue;
//            }
//
//            if(PolicyEditorConstants.DYNAMIC_SELECTOR_FUNCTION.equals(selector)){
//
//                String attributeValue = attributeDTO.getAttributeValue();
//                if(attributeValue == null || attributeValue.trim().length() < 1){
//                    continue;
//                }
//                function = attributeDTO.getFunction();
//                if(function != null){
//                    function = function.replace("&gt;", ">");
//                    function = function.replace("&lt;", "<");
//
//                    if(ruleFunctionMap.get(function) != null){// TODO
//                        attributeDTO.setFunction(ruleFunctionMap.get(function));
//                    }
//                }
//
//                if(attributeDTO.getDataType() == null ||
//                        attributeDTO.getDataType().trim().length() < 1 ||
//                        attributeDTO.getDataType().trim().equals("null")) {
//
//                    if(category != null && defaultDataTypeMap.get(category) != null){
//                        attributeDTO.setDataType((defaultDataTypeMap.
//                                            get(category).iterator().next()));
//                    } else {
//                        attributeDTO.setDataType(PolicyEditorConstants.DataType.STRING);
//                    }
//                }
//
//            } else {
//
//                category = attributeDTO.getCategory();
//
//                if(category == null || category.trim().length() < 1){
//                    continue;
//                }
//
//                if(categoryMap.get(category) != null){
//                    attributeDTO.setCategory(categoryMap.get(category));
//                }
//
//                if(attributeDTO.getDataType() == null ||
//                        attributeDTO.getDataType().trim().length() < 1 ||
//                        attributeDTO.getDataType().trim().equals("null")) {
//
//                    if(defaultDataTypeMap.get(category) != null){
//                        attributeDTO.setDataType((defaultDataTypeMap.
//                                            get(category).iterator().next()));
//                    } else {
//                        attributeDTO.setDataType(PolicyEditorConstants.DataType.STRING);
//                    }
//                }
//
//                if(attributeDTO.getAttributeId() == null ||
//                        attributeDTO.getAttributeId().trim().length() < 1 ||
//                        attributeDTO.getAttributeId().trim().equals("null")) {
//                    if(defaultAttributeIdMap.get(category) != null){
//                        attributeDTO.setAttributeId((defaultAttributeIdMap.
//                                            get(category).iterator().next()));
//                    }
//                }
//            }
//
//
//            ExtendAttributeDTO odlRowDTO = new ExtendAttributeDTO(attributeDTO);
//            odlRowDTO.setCategory(category);
//            odlRowDTO.setFunction(function);
//            createMetaDataFromDynamicAttribute("targetRule" + odlRowDTO.getId(), odlRowDTO,
//                                                policyMetaDataList);
//            //newDynamicAttributeDTOs.add(attributeDTO);
//        }

        return policyMetaDataList.toArray(new String[policyMetaDataList.size()]);
    }

    private static void createMetaDataFromPolicy(String prefix, PolicyDTO policyDTO, List<String> metaDataList) {
        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + policyDTO.getPolicyId());
            metaDataList.add(prefix + "|" + policyDTO.getRuleAlgorithm());
            if (policyDTO.getDescription() == null) {
                policyDTO.setDescription("");
            }
            metaDataList.add(prefix + "|" + policyDTO.getDescription());
            metaDataList.add(prefix + "|" + policyDTO.getVersion());
        }
    }

    private static void createMetaDataFromPolicySet(String prefix, PolicySetDTO policyDTO, List<String> metaDataList) {
        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + policyDTO.getPolicySetId());
            metaDataList.add(prefix + "|" + policyDTO.getPolicyCombiningAlgId());
            if (policyDTO.getDescription() == null) {
                policyDTO.setDescription("");
            }
            metaDataList.add(prefix + "|" + policyDTO.getDescription());
            metaDataList.add(prefix + "|" + policyDTO.getVersion());
        }
    }

    private static void createMetaDataFromRule(String prefix, RuleDTO ruleDTO, List<String> metaDataList) {
        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + ruleDTO.getRuleId());
            metaDataList.add(prefix + "|" + ruleDTO.getRuleEffect());
            metaDataList.add(prefix + "|" + ruleDTO.getRuleDescription());
        }
    }

    private static void createMetaDataFromRowDTO(String prefix, RowDTO rowDTO, List<String> metaDataList) {

        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + rowDTO.getCategory());
            metaDataList.add(prefix + "|" + rowDTO.getPreFunction());
            metaDataList.add(prefix + "|" + rowDTO.getFunction());
            metaDataList.add(prefix + "|" + rowDTO.getAttributeValue());
            metaDataList.add(prefix + "|" + rowDTO.getAttributeId());
            metaDataList.add(prefix + "|" + rowDTO.getAttributeDataType());
            metaDataList.add(prefix + "|" + rowDTO.getCombineFunction());
        }
    }

    private static void createMetaDataFromDynamicAttribute(String prefix, ExtendAttributeDTO dto,
                                                           List<String> metaDataList) {

        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + dto.getCategory());
            metaDataList.add(prefix + "|" + dto.getSelector());
            metaDataList.add(prefix + "|" + dto.getFunction());
            metaDataList.add(prefix + "|" + dto.getAttributeValue());
            metaDataList.add(prefix + "|" + dto.getAttributeId());
            metaDataList.add(prefix + "|" + dto.getDataType());
            metaDataList.add(prefix + "|" + dto.getId());
        }
    }

    private static void createMetaDataFromObligation(String prefix, ObligationDTO dto,
                                                     List<String> metaDataList) {

        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + dto.getType());
            metaDataList.add(prefix + "|" + dto.getObligationId());
            metaDataList.add(prefix + "|" + dto.getEffect());
            metaDataList.add(prefix + "|" + dto.getAttributeValue());
            metaDataList.add(prefix + "|" + dto.getResultAttributeId());
            metaDataList.add(prefix + "|" + dto.getAttributeValueDataType());
        }
    }

    private static void createMetaDataFromReference(String prefix, PolicyRefIdDTO dto,
                                                    List<String> metaDataList) {
        if (metaDataList != null) {
            metaDataList.add(prefix + "|" + dto.getId());
            metaDataList.add(prefix + "|" + dto.isPolicySet());
            metaDataList.add(prefix + "|" + dto.isReferenceOnly());
        }
    }

    public static String[] createBasicPolicyData(SimplePolicyEditorDTO policyEditorDTO) {

        List<String> metaDataList = new ArrayList<String>();

        metaDataList.add("policyId|" + policyEditorDTO.getPolicyId());
        metaDataList.add("category|" + policyEditorDTO.getAppliedCategory());
        metaDataList.add("policyDescription|" + policyEditorDTO.getDescription());
        metaDataList.add("userAttributeId|" + policyEditorDTO.getUserAttributeId());
        metaDataList.add("userAttributeValue|" + policyEditorDTO.getUserAttributeValue());
        metaDataList.add("function|" + policyEditorDTO.getFunction());
        metaDataList.add("actionValue|" + policyEditorDTO.getActionValue());
        metaDataList.add("resourceValue|" + policyEditorDTO.getResourceValue());
        metaDataList.add("category|" + policyEditorDTO.getAppliedCategory());
        metaDataList.add("environmentValue|" + policyEditorDTO.getEnvironmentValue());
        metaDataList.add("environmentId|" + policyEditorDTO.getEnvironmentId());

        List<SimplePolicyEditorElementDTO> elementDTOs = policyEditorDTO.getSimplePolicyEditorElementDTOs();

        if (elementDTOs != null && elementDTOs.size() > 0) {
            for (int i = 0; i < elementDTOs.size(); i++) {
                SimplePolicyEditorElementDTO dto = elementDTOs.get(i);
                if (dto.getResourceValue() != null) {
                    metaDataList.add("resourceValue" + i + "|" + dto.getResourceValue());
                } else {
                    metaDataList.add("resourceValue" + i);
                }
                if (dto.getEnvironmentValue() != null) {
                    metaDataList.add("environmentValue" + i + "|" + dto.getEnvironmentValue());
                } else {
                    metaDataList.add("environmentValue" + i);
                }
                if (dto.getActionValue() != null) {
                    metaDataList.add("actionValue" + i + "|" + dto.getActionValue());
                } else {
                    metaDataList.add("actionValue" + i);
                }
                if (dto.getOperationType() != null) {
                    metaDataList.add("operationValue" + i + "|" + dto.getOperationType());
                } else {
                    metaDataList.add("operationValue" + i);
                }
                if (dto.getUserAttributeId() != null) {
                    metaDataList.add("userAttributeId" + i + "|" + dto.getUserAttributeId());
                } else {
                    metaDataList.add("userAttributeId" + i);
                }
                if (dto.getUserAttributeValue() != null) {
                    metaDataList.add("userAttributeValue" + i + "|" + dto.getUserAttributeValue());
                } else {
                    metaDataList.add("userAttributeValue" + i);
                }
                if (dto.getEnvironmentId() != null) {
                    metaDataList.add("environmentId" + i + "|" + dto.getEnvironmentId());
                } else {
                    metaDataList.add("environmentId" + i);
                }
                if (dto.getFunctionOnResources() != null) {
                    metaDataList.add("functionOnResources" + i + "|" + dto.getFunctionOnResources());
                } else {
                    metaDataList.add("functionOnResources" + i);
                }
                if (dto.getFunctionOnActions() != null) {
                    metaDataList.add("functionOnActions" + i + "|" + dto.getFunctionOnActions());
                } else {
                    metaDataList.add("functionOnActions" + i);
                }
                if (dto.getFunctionOnUsers() != null) {
                    metaDataList.add("functionOnUsers" + i + "|" + dto.getFunctionOnUsers());
                } else {
                    metaDataList.add("functionOnUsers" + i);
                }
                if (dto.getFunctionOnEnvironments() != null) {
                    metaDataList.add("functionOnEnvironments" + i + "|" + dto.getFunctionOnEnvironments());
                } else {
                    metaDataList.add("functionOnEnvironments" + i);
                }

            }
        }
        return metaDataList.toArray(new String[metaDataList.size()]);
    }

////////////////////////////////////// Simple Policy Editor data ////////////////////////////////////


    public static SimplePolicyEditorDTO createSimplePolicyEditorDTO(String[] policyEditorData) {

        Map<String, String> metaDataMap = new HashMap<String, String>();
        List<SimplePolicyEditorElementDTO> SimplePolicyEditorElementDTOs = new ArrayList<SimplePolicyEditorElementDTO>();

        int i = 0;

        if (policyEditorData != null) {
            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    String value = data.substring(data.indexOf("|") + 1);
                    metaDataMap.put(identifier, value);
                }
                i++;
            }
        }

        SimplePolicyEditorDTO policyEditorDTO = new SimplePolicyEditorDTO();
        policyEditorDTO.setPolicyId(metaDataMap.get("policyId"));
        policyEditorDTO.setAppliedCategory(metaDataMap.get("policyId"));
        policyEditorDTO.setFunction(metaDataMap.get("function"));
        policyEditorDTO.setActionValue(metaDataMap.get("actionValue"));
        policyEditorDTO.setDescription(metaDataMap.get("policyDescription"));
        policyEditorDTO.setUserAttributeId(metaDataMap.get("userAttributeId"));
        policyEditorDTO.setUserAttributeValue(metaDataMap.get("userAttributeValue"));
        policyEditorDTO.setResourceValue(metaDataMap.get("resourceValue"));
        policyEditorDTO.setEnvironmentValue(metaDataMap.get("environmentValue"));
        policyEditorDTO.setEnvironmentId(metaDataMap.get("environmentId"));
        policyEditorDTO.setAppliedCategory(metaDataMap.get("category"));

        i = (i - 11) / 11;

        for (int j = 0; j < i; j++) {

            SimplePolicyEditorElementDTO elementDTO = new SimplePolicyEditorElementDTO();

            elementDTO.setResourceValue(metaDataMap.get("resourceValue" + j));
            elementDTO.setEnvironmentValue(metaDataMap.get("environmentValue" + j));
            if (metaDataMap.get("actionValue" + j) != null) {
                elementDTO.setActionValue(metaDataMap.get("actionValue" + j));
            }
            elementDTO.setOperationType(metaDataMap.get("operationValue" + j));
            elementDTO.setUserAttributeId(metaDataMap.get("userAttributeId" + j));
            elementDTO.setUserAttributeValue(metaDataMap.get("userAttributeValue" + j));
            elementDTO.setEnvironmentId(metaDataMap.get("environmentId" + j));
            elementDTO.setFunctionOnResources(metaDataMap.get("functionOnResources" + j));
            elementDTO.setFunctionOnActions(metaDataMap.get("functionOnActions" + j));
            elementDTO.setFunctionOnUsers(metaDataMap.get("functionOnUsers" + j));
            elementDTO.setFunctionOnEnvironments(metaDataMap.get("functionOnEnvironments" + j));

            SimplePolicyEditorElementDTOs.add(elementDTO);
        }

        if (SimplePolicyEditorElementDTOs.size() > 0) {
            policyEditorDTO.setSimplePolicyEditorElementDTOs(SimplePolicyEditorElementDTOs);
        }

        return policyEditorDTO;
    }


///////////////////////////////// policy Set ///////////////////////////////////////////////////////

//    public static PolicyElementDTO createPolicySetElementDTO(String policy)
//            throws EntitlementPolicyCreationException {
//
//        PolicySetDTO policyElementDTO = new PolicySetDTO();
//        OMElement omElement;
//        try {
//            omElement = AXIOMUtil.stringToOM(policy);
//        } catch (XMLStreamException e) {
//            throw new EntitlementPolicyCreationException("Policy can not be converted to OMElement");
//        }
//
//        if (omElement != null) {
//
//            policyElementDTO.setPolicySetId(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_SET_ID)));
//
//            String ruleCombiningAlgorithm = omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_ALGORITHM));
//
//            try{
//                policyElementDTO.setRuleCombiningAlgorithms(ruleCombiningAlgorithm.
//                        split(PolicyEditorConstants.RULE_ALGORITHM_IDENTIFIER_3)[1]);
//            } catch (Exception ignore){
//                policyElementDTO.setRuleCombiningAlgorithms(ruleCombiningAlgorithm.
//                        split(PolicyEditorConstants.RULE_ALGORITHM_IDENTIFIER_1)[1]);
//                // if this is also fails, can not edit the policy
//            }
//
//            Iterator iterator = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    DESCRIPTION_ELEMENT);
//
//            if(iterator.hasNext()){
//                OMElement descriptionElement = (OMElement) iterator.next();
//                if(descriptionElement != null && descriptionElement.getText() != null){
//                    policyElementDTO.setPolicyDescription(descriptionElement.getText().trim());
//                }
//            }
//
//        }
//        return policyElementDTO;
//    }

//////////////////////////////// Standard policy editor/////////////////////////////////////////////////////

    public static PolicyElementDTO createPolicyElementDTO(String policy)
            throws EntitlementPolicyCreationException {

        PolicyElementDTO policyElementDTO = new PolicyElementDTO();
        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(policy);
        } catch (XMLStreamException e) {
            throw new EntitlementPolicyCreationException("Policy can not be converted to OMElement");
        }

        if (omElement != null) {

            policyElementDTO.setPolicyName(omElement.
                    getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_ID)));

            String ruleCombiningAlgorithm = omElement.
                    getAttributeValue(new QName(EntitlementPolicyConstants.RULE_ALGORITHM));

            try {
                policyElementDTO.setRuleCombiningAlgorithms(ruleCombiningAlgorithm.
                        split(PolicyEditorConstants.RULE_ALGORITHM_IDENTIFIER_3)[1]);
            } catch (Exception ignore) {
                policyElementDTO.setRuleCombiningAlgorithms(ruleCombiningAlgorithm.
                        split(PolicyEditorConstants.RULE_ALGORITHM_IDENTIFIER_1)[1]);
                // if this is also fails, can not edit the policy
            }

            Iterator iterator = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
                    DESCRIPTION_ELEMENT);

            if (iterator.hasNext()) {
                OMElement descriptionElement = (OMElement) iterator.next();
                if (descriptionElement != null && descriptionElement.getText() != null) {
                    policyElementDTO.setPolicyDescription(descriptionElement.getText().trim());
                }
            }

        }
        return policyElementDTO;
    }

    public static List<RuleElementDTO> createRuleElementDTOs(String policy)
            throws EntitlementPolicyCreationException {

        List<RuleElementDTO> ruleElementDTOs = new ArrayList<RuleElementDTO>();
        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(policy);
        } catch (XMLStreamException e) {
            throw new EntitlementPolicyCreationException("Policy can not be converted to OMElement");
        }

        if (omElement != null) {
            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
                    RULE_ELEMENT);
            while (iterator2.hasNext()) {
                OMElement ruleElement = (OMElement) iterator2.next();
                ruleElementDTOs.add(createRuleDTO(ruleElement));
            }
        }
        return ruleElementDTOs;
    }


    public static RuleElementDTO createRuleDTO(OMElement omElement) {
        RuleElementDTO ruleElementDTO = new RuleElementDTO();

        if (omElement != null) {
            ruleElementDTO.setRuleId(omElement.
                    getAttributeValue(new QName(EntitlementPolicyConstants.RULE_ID)).trim());
            ruleElementDTO.setRuleEffect(omElement.
                    getAttributeValue(new QName(EntitlementPolicyConstants.RULE_EFFECT)).trim());

            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementPolicyConstants.DESCRIPTION_ELEMENT);

            while (iterator1.hasNext()) {
                OMElement descriptionElement = (OMElement) iterator1.next();
                if (descriptionElement != null && descriptionElement.getText() != null) {
                    ruleElementDTO.setRuleDescription(descriptionElement.getText().trim());
                }
            }
        }

        return ruleElementDTO;
    }


    public static void processRuleRowPolicyEditorData(List<RuleDTO> rules, String[] policyEditorData) {

        for (RuleDTO ruleDTO : rules) {
            List<String> ruleList = new ArrayList<String>();
            List<String> ruleTargetList = new ArrayList<String>();
            List<String> obligationList = new ArrayList<String>();

            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    if (identifier.startsWith("ruleTarget")) {
                        String ruleId = identifier.substring(10);
                        if (ruleId != null && ruleId.contains(ruleDTO.getRuleId())) {
                            ruleTargetList.add(data.substring(data.indexOf("|") + 1));
                        }
                    } else if (identifier.startsWith("ruleObligation")) {
                        String ruleId = identifier.substring(14);
                        if (ruleId != null && ruleId.equals(ruleDTO.getRuleId())) {
                            obligationList.add(data.substring(data.indexOf("|") + 1));
                        }
                    } else if (identifier.startsWith("ruleRow")) {
                        String ruleId = identifier.substring(7);
                        if (ruleId != null && ruleId.equals(ruleDTO.getRuleId())) {
                            ruleList.add(data.substring(data.indexOf("|") + 1));
                        }
                    }
                }
            }

            ruleDTO.setRowDTOList(createRowDTO(ruleList));
            ruleDTO.getTargetDTO().setRowDTOList(createRowDTO(ruleTargetList));
            ruleDTO.setObligationDTOs(createObligationDTO(obligationList));
            ruleDTO.setCompletedRule(true);
        }
    }

    public static void processTargetPolicyEditorData(TargetDTO targetDTO, String[] policyEditorData) {

        List<String> targetList = new ArrayList<String>();

        if (policyEditorData != null) {
            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    if (("target").equals(identifier)) {
                        targetList.add(data.substring(data.indexOf("|") + 1));
                    }
                }
            }

            targetDTO.setRowDTOList(createRowDTO(targetList));
        }
    }

    public static void processPolicyEditorData(PolicyElementDTO policyElementDTO, String[] policyEditorData) {

        List<String> targetList = new ArrayList<String>();

        if (policyEditorData != null) {
            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    if (("policy").equals(identifier)) {
                        targetList.add(data.substring(data.indexOf("|") + 1));
                    }
                }
            }

            policyElementDTO.setPolicyName(targetList.get(0));
            policyElementDTO.setRuleCombiningAlgorithms(targetList.get(1));
            if (targetList.get(2) != null) {
                policyElementDTO.setPolicyDescription(targetList.get(2));
            }
            policyElementDTO.setVersion(targetList.get(3));
        }
    }

    public static void processObligationPolicyEditorData(List<ObligationDTO> obligationDTOs,
                                                         String[] policyEditorData) {

        List<String> targetList = new ArrayList<String>();

        if (policyEditorData != null) {
            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    if (("obligation").equals(identifier)) {
                        targetList.add(data.substring(data.indexOf("|") + 1));
                    }
                }
            }

            obligationDTOs.addAll(createObligationDTO(targetList));
        }
    }

    public static void processRulePolicyEditorData(List<RuleDTO> ruleDTOs,
                                                   String[] policyEditorData) {
        List<String> targetList = new ArrayList<String>();
        if (policyEditorData != null) {
            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    if (("rule").equals(identifier)) {
                        targetList.add(data.substring(data.indexOf("|") + 1));
                    }
                }
            }
            ruleDTOs.addAll(createRuleDTO(targetList));
            if (ruleDTOs.size() > 0) {
                processRuleRowPolicyEditorData(ruleDTOs, policyEditorData);
            }
        }
    }

    public static void processReferencePolicyEditorData(List<PolicyRefIdDTO> policyRefIdDTOs,
                                                        String[] policyEditorData) {

        List<String> targetList = new ArrayList<String>();
        if (policyEditorData != null) {
            for (String data : policyEditorData) {
                if (data.contains("|")) {
                    String identifier = data.substring(0, data.indexOf("|"));
                    if (("reference").equals(identifier)) {
                        targetList.add(data.substring(data.indexOf("|") + 1));
                    }
                }
            }

            policyRefIdDTOs.addAll(createReferenceDTO(targetList));
        }
    }

    private static List<RowDTO> createRowDTO(List<String> list) {
        List<RowDTO> rowDTOs = new ArrayList<RowDTO>();
        for (int i = 0; i < list.size(); i = i + 7) {
            List<String> newList = list.subList(i, i + 7);
            if (newList != null) {
                RowDTO rowDTO = new RowDTO();
                rowDTO.setCategory(newList.get(0));
                rowDTO.setPreFunction(newList.get(1));
                rowDTO.setFunction(newList.get(2));
                rowDTO.setAttributeValue(newList.get(3));
                rowDTO.setAttributeId(newList.get(4));
                rowDTO.setAttributeDataType(newList.get(5));
                rowDTO.setCombineFunction(newList.get(6));
                rowDTOs.add(rowDTO);
            }
        }
        return rowDTOs;
    }

    private static List<ObligationDTO> createObligationDTO(List<String> list) {
        List<ObligationDTO> rowDTOs = new ArrayList<ObligationDTO>();
        for (int i = 0; i < list.size(); i = i + 6) {
            List<String> newList = list.subList(i, i + 6);
            if (newList != null) {
                ObligationDTO rowDTO = new ObligationDTO();
                rowDTO.setType(newList.get(0));
                rowDTO.setObligationId(newList.get(1));
                rowDTO.setEffect(newList.get(2));
                rowDTO.setAttributeValue(newList.get(3));
                rowDTO.setResultAttributeId(newList.get(4));
                rowDTO.setAttributeValueDataType(newList.get(5));
                rowDTOs.add(rowDTO);
            }
        }
        return rowDTOs;
    }

    private static List<RuleDTO> createRuleDTO(List<String> list) {
        List<RuleDTO> rowDTOs = new ArrayList<RuleDTO>();
        for (int i = 0; i < list.size(); i = i + 3) {
            List<String> newList = list.subList(i, i + 3);
            if (newList != null) {
                RuleDTO rowDTO = new RuleDTO();
                rowDTO.setRuleId(newList.get(0));
                rowDTO.setRuleEffect(newList.get(1));
                rowDTO.setRuleDescription(newList.get(2));
                rowDTOs.add(rowDTO);
            }
        }
        return rowDTOs;
    }

    private static List<PolicyRefIdDTO> createReferenceDTO(List<String> list) {
        List<PolicyRefIdDTO> rowDTOs = new ArrayList<PolicyRefIdDTO>();
        for (int i = 0; i < list.size(); i = i + 3) {
            List<String> newList = list.subList(i, i + 3);
            if (newList != null) {
                PolicyRefIdDTO rowDTO = new PolicyRefIdDTO();
                rowDTO.setId(newList.get(0));
                rowDTO.setPolicySet(Boolean.parseBoolean(newList.get(1)));
                rowDTO.setReferenceOnly(Boolean.parseBoolean(newList.get(2)));
                rowDTOs.add(rowDTO);
            }
        }
        return rowDTOs;
    }

///////////////////////////////////////// Basic Policy Editor ///////////////////////////////////////

    /**
     * create policy meta data that helps to edit the policy using basic editor
     *
     * @param basicPolicyDTO   BasicPolicyDTO
     * @param ruleElementOrder String
     * @return String Array to dent to back end
     */
    public static String[] generateBasicPolicyEditorData(BasicPolicyDTO basicPolicyDTO,
                                                         String ruleElementOrder) {

        List<BasicRuleDTO> basicRuleDTOs = basicPolicyDTO.getBasicRuleDTOs();
        BasicTargetDTO basicTargetDTO = basicPolicyDTO.getTargetDTO();

        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.BASIC);
        List<BasicRuleDTO> arrangedRules = new ArrayList<BasicRuleDTO>();

        if (ruleElementOrder != null && ruleElementOrder.trim().length() > 0) {
            String[] ruleIds = ruleElementOrder.
                    split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
            for (String ruleId : ruleIds) {
                for (BasicRuleDTO ruleDTO : basicRuleDTOs) {
                    if (ruleId.equals(ruleDTO.getRuleId())) {
                        arrangedRules.add(ruleDTO);
                    }
                }
            }
            basicRuleDTOs = arrangedRules;
        }

        int ruleEditorDataConstant = EntitlementPolicyConstants.BASIC_POLICY_EDITOR_RULE_DATA_AMOUNT;
        int targetEditorDataConstant = EntitlementPolicyConstants.BASIC_POLICY_EDITOR_TARGET_DATA_AMOUNT;

        int i = 0;
        String selectedDataType;
        String[] policyEditorData;
        if (basicRuleDTOs != null) {
            policyEditorData = new String[targetEditorDataConstant +
                    (basicRuleDTOs.size() * ruleEditorDataConstant)];
        } else {
            policyEditorData = new String[targetEditorDataConstant];
        }

        policyEditorData[i++] = basicPolicyDTO.getPolicyId();
        policyEditorData[i++] = basicPolicyDTO.getRuleAlgorithm();
        String algorithm = basicPolicyDTO.getRuleAlgorithm();
        if (algorithm != null && algorithm.trim().length() > 0) {
            basicPolicyDTO.setRuleAlgorithm(holder.getRuleAlgorithmUri(algorithm));
        } else {
            basicPolicyDTO.setRuleAlgorithm(holder.getRuleAlgorithmUri(holder.getDefaultRuleAlgorithm()));
        }
        policyEditorData[i++] = basicPolicyDTO.getVersion();
        policyEditorData[i++] = basicPolicyDTO.getDescription();

        policyEditorData[i++] = basicTargetDTO.getFunctionOnResources();
        policyEditorData[i++] = basicTargetDTO.getResourceList();
        policyEditorData[i++] = basicTargetDTO.getResourceId();
        String resourceId = basicTargetDTO.getResourceId();
        policyEditorData[i++] = basicTargetDTO.getResourceDataType();
        basicTargetDTO.setFunctionOnResources(holder.getFunctionUri(basicTargetDTO.getFunctionOnResources()));
        basicTargetDTO.setResourceId(holder.getAttributeIdUri(resourceId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(resourceId)) != null) {
            basicTargetDTO.setResourceDataType(selectedDataType);
        }

        policyEditorData[i++] = basicTargetDTO.getFunctionOnSubjects();
        policyEditorData[i++] = basicTargetDTO.getSubjectList();
        policyEditorData[i++] = basicTargetDTO.getSubjectId();
        policyEditorData[i++] = basicTargetDTO.getSubjectDataType();
        String subjectId = basicTargetDTO.getSubjectId();
        basicTargetDTO.setFunctionOnSubjects(holder.getFunctionUri(basicTargetDTO.getFunctionOnSubjects()));
        basicTargetDTO.setSubjectId(holder.getAttributeIdUri(subjectId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(subjectId)) != null) {
            basicTargetDTO.setSubjectDataType(selectedDataType);
        }

        policyEditorData[i++] = basicTargetDTO.getFunctionOnActions();
        policyEditorData[i++] = basicTargetDTO.getActionList();
        policyEditorData[i++] = basicTargetDTO.getActionId();
        String actionId = basicTargetDTO.getActionId();
        policyEditorData[i++] = basicTargetDTO.getActionDataType();
        basicTargetDTO.setFunctionOnActions(holder.getFunctionUri(basicTargetDTO.getFunctionOnActions()));
        basicTargetDTO.setActionId(holder.getAttributeIdUri(actionId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(actionId)) != null) {
            basicTargetDTO.setActionDataType(selectedDataType);
        }

        policyEditorData[i++] = basicTargetDTO.getFunctionOnEnvironment();
        policyEditorData[i++] = basicTargetDTO.getEnvironmentList();
        policyEditorData[i++] = basicTargetDTO.getEnvironmentId();
        policyEditorData[i++] = basicTargetDTO.getEnvironmentDataType();
        String environmentId = basicTargetDTO.getEnvironmentId();
        basicTargetDTO.setFunctionOnEnvironment(holder.getFunctionUri(basicTargetDTO.getFunctionOnEnvironment()));
        basicTargetDTO.setEnvironmentId(holder.getAttributeIdUri(environmentId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(environmentId)) != null) {
            basicTargetDTO.setEnvironmentDataType(selectedDataType);
        }


        if (basicRuleDTOs != null && basicRuleDTOs.size() > 0) {
            for (BasicRuleDTO basicRuleDTO : basicRuleDTOs) {
                generateBasicPolicyEditorDataForRule(basicRuleDTO, policyEditorData, i);
                i = i + ruleEditorDataConstant;

                if (basicRuleDTO.getRuleId() == null || basicRuleDTO.getRuleId().trim().length() == 0) {
                    basicRuleDTO.setRuleId(UUID.randomUUID().toString());
                }

                if (basicRuleDTO.getRuleEffect() == null || basicRuleDTO.getRuleEffect().trim().length() == 0) {
                    basicRuleDTO.setRuleEffect(holder.getDefaultEffect());
                }
            }
        }

        if (holder.isAddLastRule()) {

            if (basicRuleDTOs == null) {
                basicRuleDTOs = new ArrayList<BasicRuleDTO>();
            }

            BasicRuleDTO basicRuleDTO = new BasicRuleDTO();
            basicRuleDTO.setRuleId(UUID.randomUUID().toString());
            if (holder.getLastRuleEffect() != null) {
                basicRuleDTO.setRuleEffect(holder.getLastRuleEffect());
            } else {
                basicRuleDTO.setRuleEffect(holder.getDefaultEffect());
            }
            basicRuleDTOs.add(basicRuleDTO);
        }

        //as we have rearrage the rules
        basicPolicyDTO.setBasicRuleDTOs(basicRuleDTOs);

        return policyEditorData;
    }

    public static String[] generateBasicPolicyEditorDataForRule(BasicRuleDTO basicRuleDTO,
                                                                String[] policyEditorData, int currentArrayIndex) {
        int i = currentArrayIndex;
        String selectedDataType;
        PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                getPolicyEditorData(EntitlementConstants.PolicyEditor.BASIC);

        policyEditorData[i++] = basicRuleDTO.getRuleId();
        policyEditorData[i++] = basicRuleDTO.getRuleEffect();
        policyEditorData[i++] = basicRuleDTO.getRuleDescription();
        basicRuleDTO.setRuleEffect(holder.getRuleEffectUri(basicRuleDTO.getRuleEffect()));

        policyEditorData[i++] = basicRuleDTO.getPreFunctionOnResources();
        policyEditorData[i++] = basicRuleDTO.getFunctionOnResources();
        policyEditorData[i++] = basicRuleDTO.getResourceList();
        policyEditorData[i++] = basicRuleDTO.getResourceId();
        String resourceId = basicRuleDTO.getResourceId();
        policyEditorData[i++] = basicRuleDTO.getResourceDataType();
        basicRuleDTO.setPreFunctionOnResources(holder.getPreFunctionUri(basicRuleDTO.getPreFunctionOnResources()));
        basicRuleDTO.setFunctionOnResources(holder.getFunctionUri(basicRuleDTO.getFunctionOnResources()));
        basicRuleDTO.setResourceId(holder.getAttributeIdUri(resourceId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(resourceId)) != null) {
            basicRuleDTO.setResourceDataType(selectedDataType);
        }

        policyEditorData[i++] = basicRuleDTO.getPreFunctionOnSubjects();
        policyEditorData[i++] = basicRuleDTO.getFunctionOnSubjects();
        policyEditorData[i++] = basicRuleDTO.getSubjectList();
        policyEditorData[i++] = basicRuleDTO.getSubjectId();
        policyEditorData[i++] = basicRuleDTO.getSubjectDataType();
        String subjectId = basicRuleDTO.getSubjectId();
        basicRuleDTO.setPreFunctionOnSubjects(holder.getPreFunctionUri(basicRuleDTO.getPreFunctionOnSubjects()));
        basicRuleDTO.setFunctionOnSubjects(holder.getFunctionUri(basicRuleDTO.getFunctionOnSubjects()));
        basicRuleDTO.setSubjectId(holder.getAttributeIdUri(subjectId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(subjectId)) != null) {
            basicRuleDTO.setSubjectDataType(selectedDataType);
        }

        policyEditorData[i++] = basicRuleDTO.getPreFunctionOnActions();
        policyEditorData[i++] = basicRuleDTO.getFunctionOnActions();
        policyEditorData[i++] = basicRuleDTO.getActionList();
        policyEditorData[i++] = basicRuleDTO.getActionId();
        String actionId = basicRuleDTO.getActionId();
        policyEditorData[i++] = basicRuleDTO.getActionDataType();
        basicRuleDTO.setPreFunctionOnActions(holder.getPreFunctionUri(basicRuleDTO.getPreFunctionOnActions()));
        basicRuleDTO.setFunctionOnActions(holder.getFunctionUri(basicRuleDTO.getFunctionOnActions()));
        basicRuleDTO.setActionId(holder.getAttributeIdUri(actionId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(actionId)) != null) {
            basicRuleDTO.setActionDataType(selectedDataType);
        }

        policyEditorData[i++] = basicRuleDTO.getPreFunctionOnEnvironment();
        policyEditorData[i++] = basicRuleDTO.getFunctionOnEnvironment();
        policyEditorData[i++] = basicRuleDTO.getEnvironmentList();
        policyEditorData[i++] = basicRuleDTO.getEnvironmentId();
        policyEditorData[i++] = basicRuleDTO.getEnvironmentDataType();
        String environmentId = basicRuleDTO.getSubjectId();
        basicRuleDTO.setPreFunctionOnEnvironment(holder.getPreFunctionUri(basicRuleDTO.getPreFunctionOnEnvironment()));
        basicRuleDTO.setFunctionOnEnvironment(holder.getFunctionUri(basicRuleDTO.getFunctionOnEnvironment()));
        basicRuleDTO.setEnvironmentId(holder.getAttributeIdUri(environmentId));
        if ((selectedDataType = holder.getDataTypeUriForAttribute(environmentId)) != null) {
            basicRuleDTO.setEnvironmentDataType(selectedDataType);
        }

        return policyEditorData;
    }


    public static BasicPolicyDTO createBasicPolicyDTO(String[] policyEditorData) {

        BasicPolicyDTO basicPolicyDTO = new BasicPolicyDTO();
        int i = 0;

        if (policyEditorData[i] != null) {
            basicPolicyDTO.setPolicyId(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicPolicyDTO.setRuleAlgorithm(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicPolicyDTO.setVersion(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicPolicyDTO.setDescription(policyEditorData[i]);
        }
        i++;

        BasicTargetDTO basicTargetDTO = new BasicTargetDTO();

        if (policyEditorData[i] != null) {
            basicTargetDTO.setFunctionOnResources(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setResourceList(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setResourceId(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setResourceDataType(policyEditorData[i]);
        }
        i++;

        if (policyEditorData[i] != null) {
            basicTargetDTO.setFunctionOnSubjects(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setSubjectList(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setSubjectId(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setSubjectDataType(policyEditorData[i]);
        }
        i++;

        if (policyEditorData[i] != null) {
            basicTargetDTO.setFunctionOnActions(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setActionList(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setActionId(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setActionDataType(policyEditorData[i]);
        }
        i++;

        if (policyEditorData[i] != null) {
            basicTargetDTO.setFunctionOnEnvironment(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setEnvironmentList(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setEnvironmentId(policyEditorData[i]);
        }
        i++;
        if (policyEditorData[i] != null) {
            basicTargetDTO.setEnvironmentDataType(policyEditorData[i]);
        }
        i++;

        basicPolicyDTO.setTargetDTO(basicTargetDTO);
        List<BasicRuleDTO> basicRuleDTOs = createBasicRuleDTOs(policyEditorData, i);
        if (basicRuleDTOs != null && basicRuleDTOs.size() > 0) {
            basicPolicyDTO.setBasicRuleDTOs(basicRuleDTOs);
        }

        return basicPolicyDTO;
    }

    public static List<BasicRuleDTO> createBasicRuleDTOs(String[] policyEditorData, int nextIndex) {

        List<BasicRuleDTO> basicRuleDTOs = new ArrayList<BasicRuleDTO>();
        if (policyEditorData != null) {
            while (true) {
                if (policyEditorData.length == nextIndex) {
                    break;
                }
                BasicRuleDTO basicRuleDTO = createBasicRuleDTO(policyEditorData, nextIndex);
                nextIndex = nextIndex + EntitlementPolicyConstants.BASIC_POLICY_EDITOR_RULE_DATA_AMOUNT;
                basicRuleDTO.setCompletedRule(true);
                basicRuleDTOs.add(basicRuleDTO);
            }
        }
        return basicRuleDTOs;
    }

    public static BasicRuleDTO createBasicRuleDTO(String[] policyEditorDataForRule, int nextIndex) {

        BasicRuleDTO basicRuleDTO = new BasicRuleDTO();
        int i = nextIndex;

        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setRuleId(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setRuleEffect(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setRuleDescription(policyEditorDataForRule[i]);
        }
        i++;

        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setPreFunctionOnResources(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setFunctionOnResources(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setResourceList(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setResourceId(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setResourceDataType(policyEditorDataForRule[i]);
        }
        i++;

        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setPreFunctionOnSubjects(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setFunctionOnSubjects(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setSubjectList(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setSubjectId(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setSubjectDataType(policyEditorDataForRule[i]);
        }
        i++;


        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setPreFunctionOnActions(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setFunctionOnActions(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setActionList(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setActionId(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setActionDataType(policyEditorDataForRule[i]);
        }
        i++;

        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setPreFunctionOnEnvironment(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setFunctionOnEnvironment(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setEnvironmentList(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setEnvironmentId(policyEditorDataForRule[i]);
        }
        i++;
        if (policyEditorDataForRule[i] != null) {
            basicRuleDTO.setEnvironmentDataType(policyEditorDataForRule[i]);
        }

        return basicRuleDTO;
    }
}
