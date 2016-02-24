/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.ui.util;

import org.wso2.balana.utils.policy.dto.AttributeElementDTO;
import org.wso2.balana.utils.policy.dto.AttributesElementDTO;
import org.wso2.balana.utils.policy.dto.RequestElementDTO;
import org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.ui.dto.RequestDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.RowDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is Util class which help to create a XACML policy
 */
public class PolicyCreatorUtil {
//
//    /**
//     * This method creates a policy element of the XACML policy
//     * @param policyElementDTO  policy element data object
//     * @param doc XML document
//     * @return policyElement
//     */
//
//    public static Element createPolicyElement(PolicyElementDTO policyElementDTO, Document doc)  {
//
//        Element policyElement = doc.createElement(EntitlementPolicyConstants.POLICY_ELEMENT);
//
//        policyElement.setAttribute("xmlns", EntitlementPolicyConstants.XACML3_POLICY_NAMESPACE);
//
//        if(policyElementDTO.getPolicyName() != null && policyElementDTO.getPolicyName().trim().length() > 0) {
//            policyElement.setAttribute(EntitlementPolicyConstants.POLICY_ID, policyElementDTO.
//                    getPolicyName());
//        } else {
//            return null;
//        }
//
//        if(policyElementDTO.getRuleCombiningAlgorithms() != null && policyElementDTO.
//                getRuleCombiningAlgorithms().trim().length() > 0) {
//            if(PolicyEditorConstants.CombiningAlog.FIRST_APPLICABLE_ID.equals(policyElementDTO.
//                    getRuleCombiningAlgorithms().trim())){
//                policyElement.setAttribute(EntitlementPolicyConstants.RULE_ALGORITHM,
//                        PolicyEditorConstants.RULE_ALGORITHM_IDENTIFIER_1 + policyElementDTO.
//                                getRuleCombiningAlgorithms());
//            } else {
//                policyElement.setAttribute(EntitlementPolicyConstants.RULE_ALGORITHM,
//                        PolicyEditorConstants.RULE_ALGORITHM_IDENTIFIER_3 + policyElementDTO.
//                                getRuleCombiningAlgorithms());
//            }
//        } else {
//            return null;
//        }
//
//        if(policyElementDTO.getVersion() != null && policyElementDTO.getVersion().trim().length() > 0){
//            policyElement.setAttribute(EntitlementPolicyConstants.POLICY_VERSION,
//                                                                    policyElementDTO.getVersion());
//        } else {
//            // policy version is handled by wso2 registry.  therefore we can ignore it, although it
//            // is a required attribute
//            policyElement.setAttribute(EntitlementPolicyConstants.POLICY_VERSION, "1.0");
//        }
//
//        if(policyElementDTO.getPolicyDescription() != null && policyElementDTO.
//                getPolicyDescription().trim().length() > 0) {
//
//            Element descriptionElement = doc.createElement(EntitlementPolicyConstants.
//                    DESCRIPTION_ELEMENT);
//            descriptionElement.setTextContent(policyElementDTO.getPolicyDescription());
//            policyElement.appendChild(descriptionElement);
//        }
//
//        return policyElement;
//    }
//
//    ////XACML3
//
//    /**
//     * This method creates a match element (subject,action,resource or environment) of the XACML policy
//     * @param matchElementDTO match element data object
//     * @param doc XML document
//     * @return match Element
//     */
//    public static Element createMatchElement(MatchElementDTO matchElementDTO, Document doc)  {
//
//        Element matchElement = null;
//        if(matchElementDTO.getMatchId() != null && matchElementDTO.getMatchId().trim().length() > 0) {
//
//            matchElement = doc.createElement(EntitlementPolicyConstants.MATCH_ELEMENT);
//
//            matchElement.setAttribute(EntitlementPolicyConstants.MATCH_ID,
//                    matchElementDTO.getMatchId());
//
//            if(matchElementDTO.getAttributeValueElementDTO() != null) {
//                Element attributeValueElement = createAttributeValueElement(matchElementDTO.
//                        getAttributeValueElementDTO(), doc);
//                matchElement.appendChild(attributeValueElement);
//            }
//
//            if(matchElementDTO.getAttributeDesignatorDTO() != null ) {
//                Element attributeDesignatorElement = createAttributeDesignatorElement(matchElementDTO.
//                        getAttributeDesignatorDTO(), doc);
//                matchElement.appendChild(attributeDesignatorElement);
//            }
//
//            if(matchElementDTO.getAttributeSelectorDTO() != null ) {
//                Element attributeSelectorElement = createAttributeSelectorElement(matchElementDTO.
//                        getAttributeSelectorDTO(), doc);
//                matchElement.appendChild(attributeSelectorElement);
//            }
//        }
//        return matchElement;
//    }
//
//    /**
//     * This method creates the attribute value element
//     * @param attributeValueElementDTO attribute value element data object
//     * @param doc XML document
//     * @return attribute value element
//     */
//    public static Element createAttributeValueElement(AttributeValueElementDTO
//            attributeValueElementDTO, Document doc) {
//
//        Element attributeValueElement = doc.createElement(EntitlementPolicyConstants.ATTRIBUTE_VALUE);
//
//        if(attributeValueElementDTO.getAttributeValue() != null && attributeValueElementDTO.
//                getAttributeValue().trim().length() > 0) {
//
//            attributeValueElement.setTextContent(attributeValueElementDTO.getAttributeValue().trim());
//
//            if(attributeValueElementDTO.getAttributeDataType()!= null && attributeValueElementDTO.
//                    getAttributeDataType().trim().length() > 0){
//                attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                                attributeValueElementDTO.getAttributeDataType());
//            } else {
//                attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                                EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//
//        }
//
//        return attributeValueElement;
//
//    }
//
//    /**
//     * This creates XML representation of Attributes Element using AttributesElementDTO object
//     *
//     * @param elementDTO  AttributesElementDTO
//     * @param doc Document
//     * @return DOM element
//     */
//    public static Element createAttributesElement(AttributesElementDTO elementDTO, Document doc){
//
//        Element attributesElement = doc.createElement(EntitlementPolicyConstants.ATTRIBUTES);
//
//        attributesElement.setAttribute(EntitlementPolicyConstants.CATEGORY, elementDTO.getCategory());
//
//        List<AttributeElementDTO> attributeElementDTOs = elementDTO.getAttributeElementDTOs();
//        if(attributeElementDTOs != null && attributeElementDTOs.size() > 0){
//            for(AttributeElementDTO attributeElementDTO : attributeElementDTOs){
//                Element attributeElement = doc.createElement(EntitlementPolicyConstants.ATTRIBUTE);
//                attributeElement.setAttribute(EntitlementPolicyConstants.ATTRIBUTE_ID,
//                                              attributeElementDTO.getAttributeId());
//                attributeElement.setAttribute(EntitlementPolicyConstants.INCLUDE_RESULT,
//                                  Boolean.toString(attributeElementDTO.isIncludeInResult()));
//
//                if(attributeElementDTO.getIssuer() != null &&
//                                        attributeElementDTO.getIssuer().trim().length() > 0){
//                    attributeElement.setAttribute(EntitlementPolicyConstants.ISSUER,
//                                                            attributeElementDTO.getIssuer());
//                }
//
//                List<String> values = attributeElementDTO.getAttributeValues();
//                for(String value : values){
//                    Element attributeValueElement = doc.createElement(EntitlementPolicyConstants.
//                            ATTRIBUTE_VALUE);
//                    attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                                                            attributeElementDTO.getDataType());
//                    attributeValueElement.setTextContent(value.trim());
//                    attributeElement.appendChild(attributeValueElement);
//                }
//                attributesElement.appendChild(attributeElement);
//            }
//        }
//        return attributesElement;
//    }
//
//
//    public static Element createFunctionElement(FunctionDTO functionDTO, Document doc) {
//
//        Element functionElement = doc.createElement(EntitlementPolicyConstants.FUNCTION);
//
//        if(functionDTO.getFunctionId() != null && functionDTO.getFunctionId().trim().length() > 0) {
//            functionElement.setAttribute(EntitlementPolicyConstants.FUNCTION_ID,
//                    functionDTO.getFunctionId());
//        }
//
//        return functionElement;
//    }
//
////    public static Element createAttributeDesignatorElement(AttributeDesignatorDTO
////            attributeDesignatorDTO, Document doc) {
////
////        String attributeDesignatorElementName =  attributeDesignatorDTO.getElementName() +
////                EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR;
////
////        Element attributeDesignatorElement = doc.createElement(attributeDesignatorElementName);
////
////        if(attributeDesignatorDTO.getAttributeId() != null && attributeDesignatorDTO.
////                getAttributeId().trim().length() > 0 ){
////
////            attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.ATTRIBUTE_ID,
////                    attributeDesignatorDTO.getAttributeId());
////
////            if(attributeDesignatorDTO.getDataType() != null && attributeDesignatorDTO.
////                    getDataType().trim().length() > 0) {
////                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
////                        attributeDesignatorDTO.getDataType());
////            } else {
////                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
////                        EntitlementPolicyConstants.STRING_DATA_TYPE);
////            }
////
////            if(attributeDesignatorDTO.getIssuer() != null && attributeDesignatorDTO.getIssuer().
////                    trim().length() > 0) {
////                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.ISSUER,
////                        attributeDesignatorDTO.getIssuer());
////            }
////
////            if(attributeDesignatorDTO.getMustBePresent() != null && attributeDesignatorDTO.
////                    getMustBePresent().trim().length() > 0){
////                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
////                        attributeDesignatorDTO.getMustBePresent());
////            }
////
////            if(attributeDesignatorDTO.getSubjectCategory() != null){
////                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
////                        attributeDesignatorDTO.getSubjectCategory());
////            }
////
////        }
////
////        return attributeDesignatorElement;
////    }
//
//
//    public static Element createAttributeDesignatorElement(AttributeDesignatorDTO
//            attributeDesignatorDTO, Document doc) {
//
//        String attributeDesignatorElementName =
//                EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR;
//
//        Element attributeDesignatorElement = doc.createElement(attributeDesignatorElementName);
//
//        String attributeId = attributeDesignatorDTO.getAttributeId();
//        String category = attributeDesignatorDTO.getCategory();
//
//        if(attributeId != null && attributeId.trim().length() > 0 && category != null &&
//                category.trim().length() > 0){
//
//            attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.ATTRIBUTE_ID,
//                    attributeDesignatorDTO.getAttributeId());
//
//            attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.CATEGORY,
//                    attributeDesignatorDTO.getCategory());
//
//            if(attributeDesignatorDTO.getDataType() != null && attributeDesignatorDTO.
//                    getDataType().trim().length() > 0) {
//                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                        attributeDesignatorDTO.getDataType());
//            } else {
//                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                        EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//
//            if(attributeDesignatorDTO.getIssuer() != null && attributeDesignatorDTO.getIssuer().
//                    trim().length() > 0) {
//                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.ISSUER,
//                        attributeDesignatorDTO.getIssuer());
//            }
//
//            if(attributeDesignatorDTO.getMustBePresent() != null && attributeDesignatorDTO.
//                    getMustBePresent().trim().length() > 0){
//                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
//                        attributeDesignatorDTO.getMustBePresent());
//            } else {
//                attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
//                        "true");
//            }
//
//        }
//
//        return attributeDesignatorElement;
//    }
//
//
//    public static Element createAttributeSelectorElement(AttributeSelectorDTO attributeSelectorDTO,
//                                                         Document doc)  {
//
//        Element attributeSelectorElement = doc.createElement(EntitlementPolicyConstants.
//                ATTRIBUTE_SELECTOR);
//
//        if(attributeSelectorDTO.getAttributeSelectorRequestContextPath() != null &&
//                attributeSelectorDTO.getAttributeSelectorRequestContextPath().trim().length() > 0) {
//
//            attributeSelectorElement.setAttribute(EntitlementPolicyConstants.REQUEST_CONTEXT_PATH,
//                    EntitlementPolicyConstants.ATTRIBUTE_NAMESPACE + attributeSelectorDTO.
//                            getAttributeSelectorRequestContextPath());
//
//            if(attributeSelectorDTO.getAttributeSelectorDataType() != null &&
//                    attributeSelectorDTO.getAttributeSelectorDataType().trim().length() > 0) {
//                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                        attributeSelectorDTO.getAttributeSelectorDataType());
//            } else {
//                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
//                        EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//
//            if(attributeSelectorDTO.getAttributeSelectorMustBePresent() != null &&
//                    attributeSelectorDTO.getAttributeSelectorMustBePresent().trim().length() > 0) {
//                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
//                        attributeSelectorDTO.getAttributeSelectorMustBePresent());
//            }
//
//        }
//
//        return attributeSelectorElement;
//    }
//
//    public static Element createObligationsElement(List<ObligationElementDTO> obligationElementDTOs,
//                                                                                    Document doc){
//
//
//        Element obligationExpressions = null;
//        Element adviceExpressions = null;
//
//        if(obligationElementDTOs != null && obligationElementDTOs.size() > 0){
//
//            for(ObligationElementDTO dto : obligationElementDTOs){
//                String id = dto.getId();
//                String effect = dto.getEffect();
//
//                if(id != null && id.trim().length() > 0 && effect != null){
//                    if(dto.getType() == ObligationElementDTO.ADVICE){
//                        if(adviceExpressions == null){
//                             adviceExpressions = doc.
//                                        createElement(EntitlementPolicyConstants.ADVICE_EXPRESSIONS);
//                        }
//
//                        Element adviceExpression = doc.
//                                    createElement(EntitlementPolicyConstants.ADVICE_EXPRESSION);
//                        adviceExpression.setAttribute(EntitlementPolicyConstants.ADVICE_ID, id);
//                        adviceExpression.setAttribute(EntitlementPolicyConstants.ADVICE_EFFECT, effect);
//                        List<AttributeAssignmentElementDTO> elementDTOs = dto.getAssignmentElementDTOs();
//                        if(elementDTOs != null){
//                            for(AttributeAssignmentElementDTO elementDTO : elementDTOs){
//                                Element element = createAttributeAssignmentElement(elementDTO, doc);
//                                if(element != null){
//                                    adviceExpression.appendChild(element);
//                                }
//                            }
//                        }
//                        adviceExpressions.appendChild(adviceExpression);
//                    } else {
//
//                        if(obligationExpressions == null){
//                             obligationExpressions = doc.
//                                        createElement(EntitlementPolicyConstants.OBLIGATION_EXPRESSIONS);
//                        }
//
//                        Element obligationExpression = doc.
//                                    createElement(EntitlementPolicyConstants.OBLIGATION_EXPRESSION);
//                        obligationExpression.setAttribute(EntitlementPolicyConstants.OBLIGATION_ID, id);
//                        obligationExpression.setAttribute(EntitlementPolicyConstants.OBLIGATION_EFFECT,
//                                                                                            effect);
//                        List<AttributeAssignmentElementDTO> elementDTOs = dto.getAssignmentElementDTOs();
//                        if(elementDTOs != null){
//                            for(AttributeAssignmentElementDTO elementDTO : elementDTOs){
//                                Element element = createAttributeAssignmentElement(elementDTO, doc);
//                                if(element != null){
//                                    obligationExpression.appendChild(element);
//                                }
//                            }
//                        }
//                        obligationExpressions.appendChild(obligationExpression);
//                    }
//                }
//            }
//        }
//
//        if(adviceExpressions != null){
//            return adviceExpressions;
//        }
//
//        return obligationExpressions;
//    }
//
//    public static Element createAttributeAssignmentElement(AttributeAssignmentElementDTO assignmentElementDTO,
//                                                                                    Document doc){
//
//        String attributeId = assignmentElementDTO.getAttributeId();
//
//        if(attributeId != null && attributeId.trim().length() > 0){
//
//            String category = assignmentElementDTO.getCategory();
//            String issuer = assignmentElementDTO.getIssuer();
//            ApplyElementDTO applyElementDTO = assignmentElementDTO.getApplyElementDTO();
//            AttributeDesignatorDTO designatorDTO = assignmentElementDTO.getDesignatorDTO();
//            AttributeValueElementDTO valueElementDTO = assignmentElementDTO.getValueElementDTO();
//
//            Element attributeAssignment = doc.
//                                    createElement(EntitlementPolicyConstants.ATTRIBUTE_ASSIGNMENT);
//            attributeAssignment.setAttribute(EntitlementPolicyConstants.ATTRIBUTE_ID,
//                            attributeId);
//            if(category != null && category.trim().length() > 0){
//                attributeAssignment.setAttribute(EntitlementPolicyConstants.CATEGORY, category);
//            }
//
//            if(issuer != null && issuer.trim().length() > 0){
//                attributeAssignment.setAttribute(EntitlementPolicyConstants.ISSUER, issuer);
//            }
//
//            if(applyElementDTO != null){
//                attributeAssignment.appendChild(createApplyElement(applyElementDTO, doc));
//            }
//
//            if(designatorDTO != null){
//                attributeAssignment.appendChild(createAttributeDesignatorElement(designatorDTO, doc));
//            }
//
//            if(valueElementDTO != null){
//                attributeAssignment.appendChild(createAttributeValueElement(valueElementDTO, doc));
//            }
//
//            return attributeAssignment;
//        }
//
//        return null;
//    }
//
//    public static Element createSubElement(SubElementDTO subElementDTO, Document doc) {
//
//        String subElementName = subElementDTO.getElementName();
//
//        Element subElement = doc.createElement(subElementName);
//
//        for( MatchElementDTO matchElementDTO : subElementDTO.getMatchElementDTOs()) {
//            Element matchElement = createMatchElement(matchElementDTO, doc);
//            if(matchElement != null) {
//                subElement.appendChild(matchElement);
//            }
//        }
//
//        return subElement;
//    }
//
//    public static Element createTargetElement(List<SubElementDTO> subElementDTOs, Document doc) {
//
//        Element targetElement = doc.createElement(EntitlementPolicyConstants.TARGET_ELEMENT);
//        String subjectElementName = EntitlementPolicyConstants.SUBJECT_ELEMENT + "s";
//        String actionElementName = EntitlementPolicyConstants.ACTION_ELEMENT + "s";
//        String resourceElementName = EntitlementPolicyConstants.RESOURCE_ELEMENT + "s";
//        String enviornementElementName = EntitlementPolicyConstants.ENVIRONMENT_ELEMENT + "s";
//
//        Element subjectElement = doc.createElement(subjectElementName);
//        Element actionElement = doc.createElement(actionElementName);
//        Element resourceElement = doc.createElement(resourceElementName);
//        Element enviornementElement = doc.createElement(enviornementElementName);
//
//
//        for(SubElementDTO subElementDTO : subElementDTOs) {
//
//            if(subElementDTO.getElementName().equals(EntitlementPolicyConstants.SUBJECT_ELEMENT)) {
//                Element subParentElement = createSubElement(subElementDTO, doc);
//                subjectElement.appendChild(subParentElement);
//            }
//
//            if(subElementDTO.getElementName().equals(EntitlementPolicyConstants.ACTION_ELEMENT)) {
//                Element subParentElement = createSubElement(subElementDTO, doc);
//                actionElement.appendChild(subParentElement);
//            }
//
//            if(subElementDTO.getElementName().equals(EntitlementPolicyConstants.RESOURCE_ELEMENT)) {
//                Element subParentElement = createSubElement(subElementDTO, doc);
//                resourceElement.appendChild(subParentElement);
//            }
//
//            if(subElementDTO.getElementName().equals(EntitlementPolicyConstants.ENVIRONMENT_ELEMENT)) {
//                Element subParentElement = createSubElement(subElementDTO, doc);
//                enviornementElement.appendChild(subParentElement);
//            }
//        }
//
//        targetElement.appendChild(subjectElement);
//        targetElement.appendChild(actionElement);
//        targetElement.appendChild(resourceElement);
//        targetElement.appendChild(enviornementElement);
//
//        return targetElement;
//    }
//
//
//    public static Element createRuleElement(RuleElementDTO ruleElementDTO, Document doc) {
//
//        TargetElementDTO targetElementDTO = ruleElementDTO.getTargetElementDTO();
//        ConditionElementDT0 conditionElementDT0 = ruleElementDTO.getConditionElementDT0();
//        List<ObligationElementDTO> obligationElementDTOs = ruleElementDTO.getObligationElementDTOs();
//
//        Element ruleElement = doc.createElement(EntitlementPolicyConstants.RULE_ELEMENT);
//
//        if(ruleElementDTO.getRuleId() != null && ruleElementDTO.getRuleId().trim().length() > 0){
//            ruleElement.setAttribute(EntitlementPolicyConstants.RULE_ID, ruleElementDTO.getRuleId());
//        }
//
//        if(ruleElementDTO.getRuleEffect() != null && ruleElementDTO.getRuleEffect().trim().length() > 0){
//            ruleElement.setAttribute(EntitlementPolicyConstants.RULE_EFFECT,
//                    ruleElementDTO.getRuleEffect());
//        }
//
//        if(ruleElementDTO.getRuleDescription() != null && ruleElementDTO.getRuleDescription().
//                trim().length() > 0){
//            Element descriptionElement = doc.createElement(EntitlementPolicyConstants.
//                    DESCRIPTION_ELEMENT);
//            descriptionElement.setTextContent(ruleElementDTO.getRuleDescription());
//            ruleElement.appendChild(descriptionElement);
//        }
//
//        if(targetElementDTO != null ){
//            Element targetElement = PolicyEditorUtil.createTargetElement(targetElementDTO, doc);
//            ruleElement.appendChild(targetElement);
//        }
//
//        if(conditionElementDT0 != null){
//            ruleElement.appendChild(createConditionElement(conditionElementDT0, doc));
//        }
//
//
//        if(obligationElementDTOs != null && obligationElementDTOs.size() > 0){
//            List<ObligationElementDTO> obligations = new ArrayList<ObligationElementDTO>();
//            List<ObligationElementDTO> advices = new ArrayList<ObligationElementDTO>();
//            for(ObligationElementDTO obligationElementDTO : obligationElementDTOs){
//                if(obligationElementDTO.getType() == ObligationElementDTO.ADVICE){
//                    advices.add(obligationElementDTO);
//                } else {
//                    obligations.add(obligationElementDTO);
//                }
//            }
//            Element obligation = createObligationsElement(obligations, doc);
//            Element advice = createObligationsElement(advices, doc);
//            if(obligation != null){
//                ruleElement.appendChild(obligation);
//            }
//            if(advice != null){
//                ruleElement.appendChild(advice);
//            }
//        }
//
//        return ruleElement;
//    }
//
//
//    public static Element createConditionElement(ConditionElementDT0 conditionElementDT0, Document doc) {
//
//        Element conditionElement = doc.createElement(EntitlementPolicyConstants.CONDITION_ELEMENT);
//
//        if(conditionElementDT0.getApplyElement() != null){
//            conditionElement.appendChild(createApplyElement(conditionElementDT0.getApplyElement(), doc));
//
//        } else if(conditionElementDT0.getAttributeValueElementDTO() != null) {
//            Element attributeValueElement = createAttributeValueElement(conditionElementDT0.
//                    getAttributeValueElementDTO(), doc);
//            conditionElement.appendChild(attributeValueElement);
//
//        } else if(conditionElementDT0.getAttributeDesignator() != null) {
//            AttributeDesignatorDTO attributeDesignatorDTO = conditionElementDT0.getAttributeDesignator();
//            conditionElement.appendChild(createAttributeDesignatorElement(attributeDesignatorDTO, doc));
//
//        } else if(conditionElementDT0.getFunctionFunctionId() != null) {
//            Element functionElement = doc.createElement(EntitlementPolicyConstants.FUNCTION_ELEMENT);
//            functionElement.setAttribute(EntitlementPolicyConstants.FUNCTION_ID,
//                    conditionElementDT0.getFunctionFunctionId());
//            conditionElement.appendChild(functionElement);
//        } else if(conditionElementDT0.getVariableId() != null){
//            Element variableReferenceElement = doc.createElement(EntitlementPolicyConstants.
//                    VARIABLE_REFERENCE);
//            variableReferenceElement.setAttribute(EntitlementPolicyConstants.VARIABLE_ID,
//                    conditionElementDT0.getVariableId());
//            conditionElement.appendChild(variableReferenceElement);
//        }
//
//        return conditionElement;
//
//    }
//
//    public static Element createApplyElement(ApplyElementDTO applyElementDTO, Document doc) {
//
//        Element applyElement = doc.createElement(EntitlementPolicyConstants.APPLY_ELEMENT);
//
//        if(applyElementDTO.getFunctionId() != null && applyElementDTO.getFunctionId().trim().length() > 0){
//            applyElement.setAttribute(EntitlementPolicyConstants.FUNCTION_ID,
//                    applyElementDTO.getFunctionId());
//        }
//
//        if(applyElementDTO.getFunctionFunctionId() != null && applyElementDTO.
//                getFunctionFunctionId().trim().length() > 0){
//            FunctionDTO functionDTO = new FunctionDTO();
//            functionDTO.setFunctionId(applyElementDTO.getFunctionFunctionId());
//            Element functionElement = createFunctionElement(functionDTO, doc);
//            applyElement.appendChild(functionElement);
//        }
//
//        List<ApplyElementDTO> applyElementDTOs = applyElementDTO.getApplyElements();
//
//        if(applyElementDTOs != null && applyElementDTOs.size() > 0) {
//
//            for(ApplyElementDTO elementDTO : applyElementDTOs) {
//                Element subApplyElement = createApplyElement(elementDTO, doc);
//                applyElement.appendChild(subApplyElement);
//            }
//        }
//
//        List<AttributeValueElementDTO> attributeValueElementDTOs = applyElementDTO.
//                getAttributeValueElementDTOs();
//        if(attributeValueElementDTOs != null && attributeValueElementDTOs.size() > 0) {
//
//            for(AttributeValueElementDTO attributeValueElementDTO : attributeValueElementDTOs) {
//                Element attributeValueElement = createAttributeValueElement(attributeValueElementDTO,
//                        doc);
//
//                applyElement.appendChild(attributeValueElement);
//            }
//        }
//
//        List<AttributeDesignatorDTO> attributeDesignatorDTOs = applyElementDTO.getAttributeDesignators();
//        if(attributeDesignatorDTOs != null && attributeDesignatorDTOs.size() > 0) {
//
//            for(AttributeDesignatorDTO attributeDesignatorDTO : attributeDesignatorDTOs) {
//                Element attributeDesignatorElement =
//                        createAttributeDesignatorElement(attributeDesignatorDTO, doc);
//                applyElement.appendChild(attributeDesignatorElement);
//            }
//        }
//
//        List<AttributeSelectorDTO> attributeSelectorDTOs = applyElementDTO.getAttributeSelectors();
//        if(attributeSelectorDTOs != null && attributeSelectorDTOs.size() > 0) {
//
//            for(AttributeSelectorDTO attributeSelectorDTO : attributeSelectorDTOs) {
//                Element attributeSelectorElement = createAttributeSelectorElement(attributeSelectorDTO,
//                        doc);
//                applyElement.appendChild(attributeSelectorElement);
//            }
//        }
//        return applyElement;
//    }
//
//    ///////
//    public static ApplyElementDTO createApplyElementForBagFunctions(String functionId,
//                                                                    String category,
//                                                                    String attributeId,
//                                                                    String[] attributeValues,
//                                                                    String dataType){
//
//        ApplyElementDTO applyElementDTO = new ApplyElementDTO();
//
//        if(attributeValues != null && functionId != null && functionId.trim().length() > 0 &&
//                category != null && category.trim().length() > 0 &&
//                attributeId != null && attributeId.trim().length() > 0){
//
//            ApplyElementDTO applyElementDTOBag = new ApplyElementDTO();
//            for(String attributeValue :attributeValues){
//                attributeValue = attributeValue.trim();
//                AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
//                if(dataType != null && dataType.trim().length() > 0){
//                    attributeValueElementDTO.setAttributeDataType(dataType);
//                } else {
//                    attributeValueElementDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//                }
//                attributeValueElementDTO.setAttributeValue(attributeValue.trim());
//                applyElementDTOBag.setAttributeValueElementDTO(attributeValueElementDTO);
//            }
//
//            applyElementDTOBag.setFunctionId(EntitlementPolicyConstants.FUNCTION_BAG);
//
//            AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
//            if(dataType != null && dataType.trim().length() > 0){
//                attributeDesignatorDTO.setDataType(dataType);
//            } else {
//                attributeDesignatorDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//            attributeDesignatorDTO.setAttributeId(attributeId);
//            attributeDesignatorDTO.setCategory(category);
//
//            applyElementDTO.setApplyElement(applyElementDTOBag);
//            applyElementDTO.setAttributeDesignators(attributeDesignatorDTO);
//            applyElementDTO.setFunctionId(functionId);
//
//        }
//
//        return applyElementDTO;
//    }
//
//    public static ApplyElementDTO createApplyElementForNonBagFunctions(String functionId,
//                                                                    String category,
//                                                                    String attributeId,
//                                                                    String attributeValue,
//                                                                    String dataType){
//
//        ApplyElementDTO applyElementDTO = new ApplyElementDTO();
//
//        if(attributeValue != null && attributeValue.trim().length() > 0 && functionId != null &&
//                functionId.trim().length() > 0 && category != null &&
//                category.trim().length() > 0 && attributeId != null &&
//                attributeId.trim().length() > 0) {
//
//            AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
//            if(dataType != null && dataType.trim().length() > 0){
//                attributeValueElementDTO.setAttributeDataType(dataType);
//            } else {
//                attributeValueElementDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//            attributeValueElementDTO.setAttributeValue(attributeValue.trim());
//
//            AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
//            if(dataType != null && dataType.trim().length() > 0){
//                attributeDesignatorDTO.setDataType(dataType);
//            } else {
//                attributeDesignatorDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//            attributeDesignatorDTO.setAttributeId(attributeId);
//            attributeDesignatorDTO.setCategory(category);
//
//            applyElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
//            applyElementDTO.setAttributeDesignators(attributeDesignatorDTO);
//            applyElementDTO.setFunctionId(functionId);
//
//        }
//
//        return applyElementDTO;
//    }
//
//    public static ApplyElementDTO createApplyElementForNonBagFunctionsWithAnyOf(String functionId,
//                                                                    String attributeDesignatorType,
//                                                                    String attributeDesignatorId,
//                                                                    String attributeValue){
//
//        ApplyElementDTO applyElementDTO = new ApplyElementDTO();
//
//        if(attributeValue != null && attributeValue.trim().length() > 0 && functionId != null &&
//                functionId.trim().length() > 0 && attributeDesignatorType != null &&
//                attributeDesignatorType.trim().length() > 0 && attributeDesignatorId != null &&
//                attributeDesignatorId.trim().length() > 0) {
//
//            AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
//            attributeValueElementDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            attributeValueElementDTO.setAttributeValue(attributeValue.trim());
//
//            AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
//            attributeDesignatorDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            attributeDesignatorDTO.setAttributeId(attributeDesignatorId);
//            attributeDesignatorDTO.setCategory(attributeDesignatorType);
//
//            applyElementDTO.setFunctionFunctionId(functionId);
//            applyElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
//            applyElementDTO.setAttributeDesignators(attributeDesignatorDTO);
//            applyElementDTO.setFunctionId(EntitlementPolicyConstants.FUNCTION_ANY_OF);
//
//        }
//
//        return applyElementDTO;
//    }
//
//
//    public static MatchElementDTO createMatchElementForNonBagFunctions(String functionId,
//                                                                       String attributeValue,
//                                                                       String category,
//                                                                       String attributeId,
//                                                                       String dataType) {
//        MatchElementDTO matchElementDTO = new MatchElementDTO();
//
//        if(functionId != null && functionId.trim().length() > 0 && attributeValue != null &&
//                attributeValue.trim().length() > 0&& category != null &&
//                category.trim().length() > 0 && attributeId != null &&
//                attributeId.trim().length() > 0) {
//            AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
//            if(dataType != null && dataType.trim().length() > 0){
//                attributeValueElementDTO.setAttributeDataType(dataType);
//            } else {
//                attributeValueElementDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//            attributeValueElementDTO.setAttributeValue(attributeValue.trim());
//
//            AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
//            if(dataType != null && dataType.trim().length() > 0){
//                attributeValueElementDTO.setAttributeDataType(dataType);
//            } else {
//                attributeValueElementDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
//            }
//            attributeDesignatorDTO.setAttributeId(attributeId);
//            attributeDesignatorDTO.setCategory(category);
//
//            matchElementDTO.setMatchId(functionId);
//            matchElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
//            matchElementDTO.setAttributeDesignatorDTO(attributeDesignatorDTO);
//        }
//
//        return matchElementDTO;
//    }
//
//    public static Element createBasicRuleElementDTO(BasicRuleDTO basicRuleDTO,
//                                                            Document doc) {
//
//        String functionOnResources =  basicRuleDTO.getFunctionOnResources();
//        String functionOnSubjects = basicRuleDTO.getFunctionOnSubjects();
//        String functionOnActions = basicRuleDTO.getFunctionOnActions();
//        String functionOnEnvironment = basicRuleDTO.getFunctionOnEnvironment();
//        String resourceNames = basicRuleDTO.getResourceList();
//        String actionNames = basicRuleDTO.getActionList();
//        String subjectNames = basicRuleDTO.getSubjectList();
//        String environmentNames = basicRuleDTO.getEnvironmentList();
//        String resourceId = basicRuleDTO.getResourceId();
//        String subjectId = basicRuleDTO.getSubjectId();
//        String actionId = basicRuleDTO.getActionId();
//        String environmentId = basicRuleDTO.getEnvironmentId();
//        String resourceDataType = basicRuleDTO.getResourceDataType();
//        String subjectDataType = basicRuleDTO.getSubjectDataType();
//        String actionDataType = basicRuleDTO.getActionDataType();
//        String environmentDataType = basicRuleDTO.getEnvironmentDataType();
//
//
//        Element resourcesElement = null;
//        Element actionsElement = null;
//        Element subjectsElement = null;
//        Element environmentsElement = null;
//        Element targetElement = null;
//        Element applyElement = null;
//        Element conditionElement = null;
//        Element ruleElement =  null ;
//
//        ApplyElementDTO applyElementDTO = new ApplyElementDTO();
//
//        if(resourceNames != null  && resourceNames.trim().length() > 0) {
//            String[] resources = resourceNames.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
//            if(resourceId == null || resourceId.trim().length() < 1){
//                resourceId = EntitlementPolicyConstants.RESOURCE_ID;
//            }
//            if(functionOnResources.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnResources.equals(EntitlementPolicyConstants.REGEXP_MATCH) ) {
//                resourcesElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//                Element resourceElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnResources),
//                        resources[0].trim(), PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resourceDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    resourceElement.appendChild(matchElement);
//                }
//                resourcesElement.appendChild(resourceElement);
//
//            } else if(functionOnResources.contains("less") || functionOnResources.contains("greater")){
//
//                AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
//                designatorDTO.setCategory(PolicyEditorConstants.RESOURCE_CATEGORY_URI);
//                designatorDTO.setAttributeId(resourceId);
//                designatorDTO.setDataType(resourceDataType);
//                designatorDTO.setMustBePresent("true");
//                try {
//                    ApplyElementDTO elementDTO = PolicyEditorUtil.
//                            processGreaterLessThanFunctions(functionOnResources, resourceDataType,
//                                                                    resourceNames, designatorDTO);
//                    applyElementDTO.setApplyElement(elementDTO);
//                } catch (PolicyEditorException e) {
//                    //ignore TODO
//                }
//            } else if(functionOnResources.equals(EntitlementPolicyConstants.IS_IN)) {
//                ApplyElementDTO elementDTO = createApplyElementForNonBagFunctions(
//                        getFunctionId(functionOnResources),
//                        PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resources[0].trim(), resourceDataType);
//                applyElementDTO.setApplyElement(elementDTO);
//            } else {
//                ApplyElementDTO elementDTO = createApplyElementForBagFunctions(
//                        getFunctionId(functionOnResources),
//                        PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resources, resourceDataType);
//                applyElementDTO.setApplyElement(elementDTO);
//            }
//        }
//
//        if(actionNames != null  && actionNames.trim().length() > 0) {
//            String[] actions = actionNames.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
//            if(actionId == null || actionId.trim().length() < 1){
//                actionId = EntitlementPolicyConstants.ACTION_ID;
//            }
//            if(functionOnActions.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnActions.equals(EntitlementPolicyConstants.REGEXP_MATCH)) {
//                actionsElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//                Element actionElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnActions),
//                        actions[0].trim(), PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actionDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    actionElement.appendChild(matchElement);
//                }
//                actionsElement.appendChild(actionElement);
//            } else if(functionOnActions.contains("less") || functionOnActions.contains("greater")){
//
//                AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
//                designatorDTO.setCategory(PolicyEditorConstants.ACTION_CATEGORY_URI);
//                designatorDTO.setAttributeId(actionId);
//                designatorDTO.setDataType(actionDataType);
//                designatorDTO.setMustBePresent("true");
//                try {
//                    ApplyElementDTO elementDTO = PolicyEditorUtil.
//                            processGreaterLessThanFunctions(functionOnActions, actionDataType,
//                                                                    actionNames, designatorDTO);
//                    applyElementDTO.setApplyElement(elementDTO);
//                } catch (PolicyEditorException e) {
//                    //ignore TODO
//                }
//            } else if(functionOnActions.equals(EntitlementPolicyConstants.IS_IN)) {
//                ApplyElementDTO elementDTO = createApplyElementForNonBagFunctions(
//                        getFunctionId(functionOnActions),
//                        PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actions[0].trim(), actionDataType);
//                applyElementDTO.setApplyElement(elementDTO);
//            } else {
//                ApplyElementDTO elementDTO = createApplyElementForBagFunctions(
//                        getFunctionId(functionOnActions),
//                        EntitlementPolicyConstants.ACTION_ELEMENT, actionId, actions, actionDataType);
//                applyElementDTO.setApplyElement(elementDTO);
//            }
//        }
//
//        if(environmentNames != null  && environmentNames.trim().length() > 0) {
//            String[] environments = environmentNames.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
//            if(environmentId == null || environmentId.trim().length() < 1){
//                environmentId = EntitlementPolicyConstants.ENVIRONMENT_ID;
//            }
//            if(functionOnEnvironment.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnEnvironment.equals(EntitlementPolicyConstants.REGEXP_MATCH)) {
//                environmentsElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//                Element environmentElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnEnvironment),
//                        environments[0].trim(), PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environmentDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    environmentElement.appendChild(matchElement);
//                }
//                environmentsElement.appendChild(environmentElement);
//            } else if(functionOnEnvironment.contains("less") || functionOnEnvironment.contains("greater")){
//
//                AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
//                designatorDTO.setCategory(PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI);
//                designatorDTO.setAttributeId(environmentId);
//                designatorDTO.setDataType(environmentDataType);
//                designatorDTO.setMustBePresent("true");
//                try {
//                    ApplyElementDTO elementDTO = PolicyEditorUtil.
//                            processGreaterLessThanFunctions(functionOnEnvironment, environmentDataType,
//                                                                    environmentNames, designatorDTO);
//                    applyElementDTO.setApplyElement(elementDTO);
//                } catch (PolicyEditorException e) {
//                    //ignore TODO
//                }
//            } else if(functionOnEnvironment.equals(EntitlementPolicyConstants.IS_IN)) {
//                ApplyElementDTO elementDTO = createApplyElementForNonBagFunctions(
//                        getFunctionId(functionOnEnvironment),
//                        PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environments[0].trim(), environmentDataType);
//                applyElementDTO.setApplyElement(elementDTO);
//            } else {
//                ApplyElementDTO elementDTO = createApplyElementForBagFunctions(
//                        getFunctionId(functionOnEnvironment),
//                        PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environments, environmentDataType);
//                applyElementDTO.setApplyElement(elementDTO);
//            }
//        }
//
//        if(subjectNames != null  && subjectNames.trim().length() > 0) {
//            String[] subjects = subjectNames.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
//            if(subjectId == null || subjectId.trim().length() < 1){
//                subjectId = EntitlementPolicyConstants.SUBJECT_ID_DEFAULT;
//            }
//
//            ApplyElementDTO elementDTO = null;
//            if(functionOnSubjects.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnSubjects.equals(EntitlementPolicyConstants.REGEXP_MATCH)) {
//                elementDTO = createApplyElementForNonBagFunctionsWithAnyOf(
//                        getFunctionId(functionOnSubjects),
//                        PolicyEditorConstants.SUBJECT_CATEGORY_URI,subjectId, subjects[0].trim());
//
//            } else if(functionOnSubjects.contains("less") || functionOnSubjects.contains("greater")){
//
//                AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
//                designatorDTO.setCategory(PolicyEditorConstants.ACTION_CATEGORY_URI);
//                designatorDTO.setAttributeId(subjectId);
//                designatorDTO.setDataType(subjectDataType);
//                designatorDTO.setMustBePresent("true");
//                try {
//                    elementDTO = PolicyEditorUtil.
//                            processGreaterLessThanFunctions(functionOnSubjects, subjectDataType,
//                                                                    subjectNames, designatorDTO);
//                    applyElementDTO.setApplyElement(elementDTO);
//                } catch (PolicyEditorException e) {
//                    //ignore TODO
//                }
//            } else if(functionOnSubjects.equals(EntitlementPolicyConstants.IS_IN)) {
//                elementDTO = createApplyElementForNonBagFunctions(
//                        getFunctionId(functionOnSubjects),
//                        PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjects[0].trim(), subjectDataType);
//            } else {
//                elementDTO = createApplyElementForBagFunctions(
//                        getFunctionId(functionOnSubjects),
//                        PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjects, subjectDataType);
//            }
//
//            if(elementDTO != null){
//                applyElementDTO.setApplyElement(elementDTO);
//            }
//        }
//
//        List<ApplyElementDTO> applyElementDTOs = applyElementDTO.getApplyElements();
//
//        if(applyElementDTOs.size() > 1) {
//            applyElementDTO.setFunctionId(EntitlementPolicyConstants.FUNCTION_AND);
//            applyElement = createApplyElement(applyElementDTO, doc);
//        } else if(applyElementDTOs.size() == 1){
//            applyElement = createApplyElement(applyElementDTOs.get(0), doc);
//        }
//
//        if(resourcesElement != null || actionsElement != null || subjectsElement != null ||
//                environmentsElement != null) {
//           targetElement = doc.createElement(EntitlementPolicyConstants.TARGET_ELEMENT);
//            if(resourcesElement != null) {
//                targetElement.appendChild(resourcesElement);
//            }
//            if(actionsElement != null) {
//                targetElement.appendChild(actionsElement);
//            }
//            if(subjectsElement != null) {
//                targetElement.appendChild(subjectsElement);
//            }
//
//            if(environmentsElement != null){
//                targetElement.appendChild(environmentsElement);
//            }
//        }
//
//        if(applyElement != null) {
//            conditionElement = doc.createElement(EntitlementPolicyConstants.CONDITION_ELEMENT);
//            conditionElement.appendChild(applyElement);
//        }
//
//        if(basicRuleDTO.getRuleId() != null && basicRuleDTO.getRuleId().trim().length() > 0 &&
//                basicRuleDTO.getRuleEffect() != null && basicRuleDTO.getRuleEffect().
//                trim().length() > 0){
//
//            ruleElement = doc.createElement(EntitlementPolicyConstants.RULE_ELEMENT);
//            ruleElement.setAttribute(EntitlementPolicyConstants.RULE_ID, basicRuleDTO.
//                    getRuleId());
//            ruleElement.setAttribute(EntitlementPolicyConstants.RULE_EFFECT,
//                    basicRuleDTO.getRuleEffect());
//
//            if(basicRuleDTO.getRuleDescription() != null && basicRuleDTO.
//                    getRuleDescription().trim().length() > 0){
//                ruleElement.setAttribute(EntitlementPolicyConstants.RULE_DESCRIPTION,
//                        basicRuleDTO.getRuleDescription());
//            }
//
//            if(targetElement != null) {
//                ruleElement.appendChild(targetElement);
//            }
//            if(conditionElement != null) {
//                ruleElement.appendChild(conditionElement);
//            }
//        }
//
//        return ruleElement;
//
//    }
//
//
//
//    public static Element createBasicTargetElementDTO(BasicTargetDTO basicTargetDTO,
//                                                            Document doc) {
//
//        //TODO
//        String functionOnResources =  basicTargetDTO.getFunctionOnResources();
//        String functionOnSubjects = basicTargetDTO.getFunctionOnSubjects();
//        String functionOnActions = basicTargetDTO.getFunctionOnActions();
//        String functionOnEnvironment = basicTargetDTO.getFunctionOnEnvironment();
//        String resourceNames = basicTargetDTO.getResourceList();
//        String actionNames = basicTargetDTO.getActionList();
//        String subjectNames = basicTargetDTO.getSubjectList();
//        String environmentNames = basicTargetDTO.getEnvironmentList();
//        String resourceId = basicTargetDTO.getResourceId();
//        String subjectId = basicTargetDTO.getSubjectId();
//        String actionId = basicTargetDTO.getActionId();
//        String environmentId = basicTargetDTO.getEnvironmentId();
//        String resourceDataType = basicTargetDTO.getResourceDataType();
//        String subjectDataType = basicTargetDTO.getSubjectDataType();
//        String actionDataType = basicTargetDTO.getActionDataType();
//        String environmentDataType = basicTargetDTO.getResourceDataType();
//
//        Element resourcesElement = null;
//        Element actionsElement = null;
//        Element subjectsElement = null;
//        Element environmentsElement = null;
//        Element targetElement = doc.createElement(EntitlementPolicyConstants.TARGET_ELEMENT);
//
//        if(resourceNames != null  && resourceNames.trim().length() > 0) {
//            resourcesElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//            Element resourceElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//            String[] resources = resourceNames.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
//            if(resourceId == null || resourceId.trim().length() < 1) {
//                resourceId = EntitlementPolicyConstants.RESOURCE_ID;
//            }
//            if(functionOnResources.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnResources.equals(EntitlementPolicyConstants.REGEXP_MATCH) ) {
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnResources),
//                        resources[0].trim(), PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resourceDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    resourceElement.appendChild(matchElement);
//                }
//                resourcesElement.appendChild(resourceElement);
//            } else if(functionOnResources.equals(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH)) {
//                for(String resource : resources){
//                    resource = resource.trim();
//                    Element resourceEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            resource, PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resourceDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        resourceEle.appendChild(matchElement);
//                    }
//                    resourcesElement.appendChild(resourceEle);
//                }
//            } else if(functionOnResources.equals(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP)) {
//                for(String resource : resources){
//                    resource = resource.trim();
//                    Element resourceEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            resource, PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resourceDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        resourceEle.appendChild(matchElement);
//                    }
//                    resourcesElement.appendChild(resourceEle);
//                }
//            } else if(functionOnResources.equals(EntitlementPolicyConstants.MATCH_REGEXP_SET_OF)) {
//                for(String resource : resources){
//                    resource = resource.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            resource, PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resourceDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        resourceElement.appendChild(matchElement);
//                    }
//                }
//                resourcesElement.appendChild(resourceElement);
//            }else if(functionOnResources.equals(EntitlementPolicyConstants.SET_OF)) {
//                for(String resource : resources){
//                    resource = resource.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            resource, PolicyEditorConstants.RESOURCE_CATEGORY_URI, resourceId, resourceDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        resourceElement.appendChild(matchElement);
//                    }
//                }
//                resourcesElement.appendChild(resourceElement);
//            }
//        }
//
//        if(actionNames != null  && actionNames.trim().length() > 0) {
//            actionsElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//            Element actionElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//            String[] actions = actionNames.split(",");
//            if(actionId == null || actionId.trim().length() < 1) {
//                actionId = EntitlementPolicyConstants.ACTION_ID;
//            }
//            if(functionOnActions.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnActions.equals(EntitlementPolicyConstants. REGEXP_MATCH)) {
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnActions),
//                        actions[0].trim(), PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actionDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    actionElement.appendChild(matchElement);
//                }
//                actionsElement.appendChild(actionElement);
//            } else if(functionOnActions.equals(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH)) {
//                for(String action : actions){
//                    action = action.trim();
//                    Element actionEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            action, PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actionDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        actionEle.appendChild(matchElement);
//                    }
//                    actionsElement.appendChild(actionEle);
//                }
//            } else if(functionOnActions.equals(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP)) {
//                for(String action : actions){
//                    action = action.trim();
//                    Element actionEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            action, PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actionDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        actionEle.appendChild(matchElement);
//                    }
//                    actionsElement.appendChild(actionEle);
//                }
//            } else if(functionOnActions.equals(EntitlementPolicyConstants.MATCH_REGEXP_SET_OF)) {
//                for(String action : actions){
//                    action = action.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            action, PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actionDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        actionElement.appendChild(matchElement);
//                    }
//                }
//                actionsElement.appendChild(actionElement);
//            } else if(functionOnActions.equals(EntitlementPolicyConstants.SET_OF)) {
//                for(String action : actions){
//                    action = action.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            action, PolicyEditorConstants.ACTION_CATEGORY_URI, actionId, actionDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        actionElement.appendChild(matchElement);
//                    }
//                }
//                actionsElement.appendChild(actionElement);
//            }
//
//        }
//
//        if(environmentNames != null  && environmentNames.trim().length() > 0) {
//            environmentsElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//            Element environmentElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//            String[] environments = environmentNames.split(",");
//            if(environmentId == null || environmentId.trim().length() < 1) {
//                environmentId = EntitlementPolicyConstants.ENVIRONMENT_ID;
//            }
//            if(functionOnEnvironment.equals(EntitlementPolicyConstants.EQUAL_TO) ||
//                    functionOnEnvironment.equals(EntitlementPolicyConstants.REGEXP_MATCH)) {
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnEnvironment),
//                        environments[0].trim(), PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environmentDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    environmentElement.appendChild(matchElement);
//                }
//                environmentsElement.appendChild(environmentElement);
//            } else if(functionOnEnvironment.equals(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH)) {
//                for(String environment : environments){
//                    environment = environment.trim();
//                    Element environmentEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            environment, PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environmentDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        environmentEle.appendChild(matchElement);
//                    }
//                    environmentsElement.appendChild(environmentEle);
//                }
//            } else if(functionOnEnvironment.equals(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP)) {
//                for(String environment : environments){
//                    environment = environment.trim();
//                    Element environmentEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            environment, PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environmentDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        environmentEle.appendChild(matchElement);
//                    }
//                    environmentsElement.appendChild(environmentEle);
//                }
//            }else if(functionOnEnvironment.equals(EntitlementPolicyConstants.MATCH_REGEXP_SET_OF)) {
//                for(String environment : environments){
//                    environment = environment.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            environment, PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environmentDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        environmentElement.appendChild(matchElement);
//                    }
//                }
//                environmentsElement.appendChild(environmentElement);
//            }else if(functionOnEnvironment.equals(EntitlementPolicyConstants.SET_OF)) {
//                for(String environment : environments){
//                    environment = environment.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            environment, PolicyEditorConstants.ENVIRONMENT_CATEGORY_URI, environmentId, environmentDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        environmentElement.appendChild(matchElement);
//                    }
//                }
//                environmentsElement.appendChild(environmentElement);
//            }
//        }
//
//        if(subjectNames != null  && subjectNames.trim().length() > 0) {
//            subjectsElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
//            Element subjectElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//            String[] subjects = subjectNames.split(",");
//            if(subjectId == null || subjectId.trim().length() < 1){
//                subjectId = EntitlementPolicyConstants.SUBJECT_ID_DEFAULT;
//            }
//
//            if(EntitlementPolicyConstants.EQUAL_TO.equals(functionOnSubjects) ||
//                    EntitlementPolicyConstants.REGEXP_MATCH.equals(functionOnSubjects)) {
//                MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                        getFunctionId(functionOnSubjects),
//                        subjects[0].trim(), PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjectDataType);
//                Element matchElement= createMatchElement(matchElementDTO, doc);
//                if(matchElement != null){
//                    subjectElement.appendChild(matchElement);
//                }
//                subjectsElement.appendChild(subjectElement);
//            } else if(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH.equals(functionOnSubjects)){
//                for(String subject : subjects){
//                    subject = subject.trim();
//                    Element subjectEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            subject, PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjectDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        subjectEle.appendChild(matchElement);
//                    }
//                    subjectsElement.appendChild(subjectEle);
//                }
//            } else if(EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP.equals(functionOnSubjects)){
//                for(String subject : subjects){
//                    subject = subject.trim();
//                    Element subjectEle = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            subject, PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjectDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        subjectEle.appendChild(matchElement);
//                    }
//                    subjectsElement.appendChild(subjectEle);
//                }
//            } else if(EntitlementPolicyConstants.SET_OF.equals(functionOnSubjects)){
//                for(String subject : subjects){
//                    subject = subject.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.EQUAL_TO),
//                            subject, PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjectDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        subjectElement.appendChild(matchElement);
//                    }
//                }
//                subjectsElement.appendChild(subjectElement);
//            } else if(EntitlementPolicyConstants.MATCH_REGEXP_SET_OF.equals(functionOnSubjects)){
//                for(String subject : subjects){
//                    subject = subject.trim();
//                    MatchElementDTO matchElementDTO = createMatchElementForNonBagFunctions(
//                            getFunctionId(EntitlementPolicyConstants.REGEXP_MATCH),
//                            subject, PolicyEditorConstants.SUBJECT_CATEGORY_URI, subjectId, subjectDataType);
//                    Element matchElement= createMatchElement(matchElementDTO, doc);
//                    if(matchElement != null){
//                        subjectElement.appendChild(matchElement);
//                    }
//                }
//                subjectsElement.appendChild(subjectElement);
//            }
//        }
//
//        if(resourcesElement != null) {
//            targetElement.appendChild(resourcesElement);
//        }
//        if(actionsElement != null) {
//            targetElement.appendChild(actionsElement);
//        }
//        if(subjectsElement != null) {
//            targetElement.appendChild(subjectsElement);
//        }
//
//        if(environmentsElement != null){
//            targetElement.appendChild(environmentsElement);
//        }
//
//        return targetElement;
//    }
//

    /**
     * Creates XML request from  RequestDTO object
     *
     * @param requestDTO
     * @return
     */
    public static RequestElementDTO createRequestElementDTO(RequestDTO requestDTO) {

        RequestElementDTO requestElement = new RequestElementDTO();

        List<RowDTO> rowDTOs = requestDTO.getRowDTOs();
        if (rowDTOs == null || rowDTOs.size() < 1) {
            return requestElement;
        }

        Map<String, AttributesElementDTO> dtoMap = new HashMap<String, AttributesElementDTO>();
        List<AttributesElementDTO> dtoList = new ArrayList<AttributesElementDTO>();

        for (RowDTO rowDTO : rowDTOs) {
            String category = rowDTO.getCategory();
            String value = rowDTO.getAttributeValue();
            String attributeId = rowDTO.getAttributeId();
            if (category != null && category.trim().length() > 0 && value != null &&
                    value.trim().length() > 0 && attributeId != null && attributeId.trim().length() > 0) {

                if (requestDTO.isMultipleRequest()) {
                    String[] values = value.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                    for (String attributeValue : values) {
                        AttributesElementDTO attributesElementDTO = new AttributesElementDTO();
                        attributesElementDTO.setCategory(category);

                        AttributeElementDTO attributeElementDTO = new AttributeElementDTO();
                        attributeElementDTO.addAttributeValue(attributeValue);
                        attributeElementDTO.setAttributeId(attributeId);
                        attributeElementDTO.setIncludeInResult(rowDTO.isNotCompleted());
                        attributesElementDTO.addAttributeElementDTO(attributeElementDTO);
                        if (rowDTO.getAttributeDataType() != null && rowDTO.
                                getAttributeDataType().trim().length() > 0) {
                            attributeElementDTO.setDataType(rowDTO.getAttributeDataType());
                        } else {
                            attributeElementDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
                        }
                        dtoList.add(attributesElementDTO);
                    }

                } else {
                    AttributesElementDTO attributesElementDTO = dtoMap.get(category);
                    if (attributesElementDTO == null) {
                        attributesElementDTO = new AttributesElementDTO();
                        attributesElementDTO.setCategory(category);
                    }

                    String[] values = value.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                    AttributeElementDTO attributeElementDTO = new AttributeElementDTO();
                    attributeElementDTO.setAttributeValues(Arrays.asList(values));
                    attributeElementDTO.setAttributeId(attributeId);
                    attributeElementDTO.setIncludeInResult(rowDTO.isNotCompleted());
                    attributesElementDTO.addAttributeElementDTO(attributeElementDTO);
                    if (rowDTO.getAttributeDataType() != null && rowDTO.
                            getAttributeDataType().trim().length() > 0) {
                        attributeElementDTO.setDataType(rowDTO.getAttributeDataType());
                    } else {
                        attributeElementDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
                    }
                    dtoMap.put(category, attributesElementDTO);
                }
            }
        }

        requestElement.setMultipleRequest(requestDTO.isMultipleRequest());
        requestElement.setCombinedDecision(requestDTO.isCombinedDecision());
        requestElement.setReturnPolicyIdList(requestDTO.isReturnPolicyIdList());
        if (!requestDTO.isMultipleRequest()) {
            dtoList = new ArrayList<AttributesElementDTO>();
            for (Map.Entry<String, AttributesElementDTO> entry : dtoMap.entrySet()) {
                dtoList.add(entry.getValue());
            }
        }
        requestElement.setAttributesElementDTOs(dtoList);
        return requestElement;
    }


//    public static TargetElementDTO createTargetElementDTOs(String policy)
//            throws EntitlementPolicyCreationException {
//
//        TargetElementDTO targetElementDTO = null;
//        OMElement omElement;
//        try {
//            omElement = AXIOMUtil.stringToOM(policy);
//        } catch (XMLStreamException e) {
//            throw new EntitlementPolicyCreationException("Policy can not be converted to OMElement");
//        }
//
//        if (omElement != null) {
//            Iterator iterator = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    TARGET_ELEMENT);
//            while(iterator.hasNext()){
//                OMElement targetElement = (OMElement)iterator.next();
//                targetElementDTO = createTargetElementDTO(targetElement, null);
//            }
//        }
//        return targetElementDTO;
//    }


//
//
//
//    public static PolicySetDTO createPolicySetDTO(String policySet)
//            throws EntitlementPolicyCreationException {
//        PolicySetDTO policySetDTO = new PolicySetDTO();
//        OMElement omElement;
//        try {
//            omElement = AXIOMUtil.stringToOM(policySet);
//        } catch (XMLStreamException e) {
//            throw new EntitlementPolicyCreationException("Policy can not be converted to OMElement");
//        }
//
//        if(omElement != null){
//            policySetDTO.setPolicySetId(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_SET_ID)));
//
//            String policyCombiningAlgorithm = omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_ALGORITHM));
//            //TODO
//
//            if(policyCombiningAlgorithm.contains(PolicyEditorConstants.POLICY_ALGORITHM_IDENTIFIER_1)){
//                policySetDTO.setPolicyCombiningAlgId(policyCombiningAlgorithm.
//                        split(PolicyEditorConstants.POLICY_ALGORITHM_IDENTIFIER_1)[1]);
//            } else {
//                policySetDTO.setPolicyCombiningAlgId(policyCombiningAlgorithm.
//                        split(PolicyEditorConstants.POLICY_ALGORITHM_IDENTIFIER_3)[1]);
//            }
//
//            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    DESCRIPTION_ELEMENT);
//
//            if(iterator1.hasNext()){
//                OMElement descriptionElement = (OMElement) iterator1.next();
//                if(descriptionElement != null && descriptionElement.getText() != null){
//                    policySetDTO.setDescription(descriptionElement.getText().trim());
//                }
//            }
//
//
//            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    POLICY_ELEMENT);
//            while(iterator2.hasNext()){
//                OMElement policyElement = (OMElement)iterator2.next();
//                if(policyElement != null){
//                    policySetDTO.setPolicyIds(policyElement.
//                            getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_ID)));
//                }
//            }
//
//            Iterator iterator3 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    POLICY_SET_ELEMENT);
//            while(iterator3.hasNext()){
//                OMElement policySetElement = (OMElement)iterator3.next();
//                if(policySetElement != null){
//                    policySetDTO.setPolicyIds(policySetElement.
//                            getAttributeValue(new QName(EntitlementPolicyConstants.POLICY_SET_ID)));
//                }
//            }
//
//            Iterator iterator4 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    POLICY_SET_REFERENCE);
//            while(iterator4.hasNext()){
//                OMElement policySetReferenceElement = (OMElement)iterator4.next();
//                if(policySetReferenceElement != null){
//                    policySetDTO.setPolicyIds(policySetReferenceElement.getText().trim());
//                }
//            }
//
//            Iterator iterator5 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    POLICY_REFERENCE);
//            while(iterator5.hasNext()){
//                OMElement policyReferenceElement = (OMElement)iterator5.next();
//                if(policyReferenceElement != null){
//                    policySetDTO.setPolicyIds(policyReferenceElement.getText().trim());
//                }
//            }
//
//        }
//
//        return policySetDTO;
//    }
//

//
//    public static ConditionElementDT0 createConditionElementDT0(OMElement omElement){
//        ConditionElementDT0 conditionElementDT0 = new ConditionElementDT0();
//        if(omElement != null){
//            Iterator iterator = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    APPLY_ELEMENT);
//            while(iterator.hasNext()){
//                OMElement applyElement = (OMElement)iterator.next();
//                ApplyElementDTO applyElementDTO = new ApplyElementDTO();
//                conditionElementDT0.setApplyElement(createApplyElementDTO(applyElementDTO,
//                                                                          applyElement, 0, 0, ""));
//            }
//        }
//        return conditionElementDT0;
//    }
//
//    public static ApplyElementDTO createApplyElementDTO(ApplyElementDTO applyElementDTO,
//                                                        OMElement omElement , int applyElementNo,
//                                                        int addApplyElementNo, String applyElementId){
//        if(applyElementDTO == null){
//            applyElementDTO = new ApplyElementDTO();
//        }
//        if(omElement != null){
//            applyElementNo ++;
//
//            applyElementId = applyElementId + "/" + applyElementNo;
//            applyElementDTO.setApplyElementNumber(applyElementNo);
////            applyElementDTO.setAddApplyElementPageNumber(addApplyElementNo);
//            applyElementDTO.setApplyElementId(applyElementId);
//            applyElementDTO.setFunctionId(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.FUNCTION_ID)));
//            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    APPLY_ELEMENT);
//            while(iterator1.hasNext()){
//                OMElement applyElement = (OMElement)iterator1.next();
//                ApplyElementDTO elementDTO = createApplyElementDTO(null, applyElement,applyElementNo,
//                                                                 addApplyElementNo, applyElementId);
//                applyElementNo = elementDTO.getApplyElementNumber() + 1;
//                applyElementDTO.setApplyElement(elementDTO);
//            }
//
//            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                   SUBJECT_ELEMENT + EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR);
//            int attributeDesignatorElementNo = 0;
//            while(iterator2.hasNext()){
//                OMElement attributeDesignatorElement = (OMElement)iterator2.next();
//                applyElementDTO.setAttributeDesignators(createAttributeDesignatorDTO(
//                        attributeDesignatorElement, addApplyElementNo,
//                        EntitlementPolicyConstants.SUBJECT_ELEMENT, attributeDesignatorElementNo, applyElementId));
//                attributeDesignatorElementNo ++;
//            }
//
//            Iterator iterator3 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    RESOURCE_ELEMENT + EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR);
//
//            while(iterator3.hasNext()){
//                OMElement attributeDesignatorElement = (OMElement)iterator3.next();
//                applyElementDTO.setAttributeDesignators(createAttributeDesignatorDTO(
//                        attributeDesignatorElement, addApplyElementNo,
//                        EntitlementPolicyConstants.RESOURCE_ELEMENT, 0, applyElementId));
//                attributeDesignatorElementNo ++;
//            }
//
//            Iterator iterator4 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    ACTION_ELEMENT + EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR);
//
//            while(iterator4.hasNext()){
//                OMElement attributeDesignatorElement = (OMElement)iterator4.next();
//                applyElementDTO.setAttributeDesignators(createAttributeDesignatorDTO(
//                        attributeDesignatorElement, addApplyElementNo,
//                        EntitlementPolicyConstants.ACTION_ELEMENT, 0, applyElementId));
//                attributeDesignatorElementNo ++;
//            }
//
//            Iterator iterator5 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    ENVIRONMENT_ELEMENT + EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR);
//
//            while(iterator5.hasNext()){
//                OMElement attributeDesignatorElement = (OMElement)iterator5.next();
//                applyElementDTO.setAttributeDesignators(createAttributeDesignatorDTO(
//                        attributeDesignatorElement, addApplyElementNo,
//                        EntitlementPolicyConstants.ENVIRONMENT_ELEMENT, 0, applyElementId));
//                attributeDesignatorElementNo ++;
//            }
//
//            Iterator iterator6 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    ATTRIBUTE_VALUE);
//            int attributeValueElementNo = 0;
//            while(iterator6.hasNext()){
//                AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
//                OMElement attributeValueElement = (OMElement)iterator6.next();
//                attributeValueElementDTO.setAttributeDataType(attributeValueElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.DATA_TYPE)));
//                attributeValueElementDTO.setAttributeValue(attributeValueElement.getText());
//                attributeValueElementDTO.setApplyElementNumber(addApplyElementNo);
//                attributeValueElementDTO.setApplyElementId(applyElementId);
//                attributeValueElementDTO.setElementId(attributeValueElementNo);
//                applyElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
//                attributeValueElementNo ++;
//            }
//
//            Iterator iterator7 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    FUNCTION);
//
//            while(iterator7.hasNext()){
//                OMElement functionElement = (OMElement)iterator7.next();
//                applyElementDTO.setFunctionFunctionId(functionElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.FUNCTION_ID)));
//            }
//
//            Iterator iterator8 = omElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                    ENVIRONMENT_ELEMENT + EntitlementPolicyConstants.ATTRIBUTE_SELECTOR);
//            int attributeSelectorElementNo = 0;
//            while(iterator8.hasNext()){
//                OMElement attributeSelectorElement = (OMElement)iterator8.next();
//                applyElementDTO.setAttributeSelectors(createAttributeSelectorDTO(
//                        attributeSelectorElement, addApplyElementNo, attributeSelectorElementNo, applyElementId));
//                attributeSelectorElementNo ++;
//            }
//
//            applyElementDTO.setAttributeValueElementCount(attributeValueElementNo);
//            applyElementDTO.setAttributeDesignatorsElementCount(attributeDesignatorElementNo);
//            applyElementDTO.setAttributeSelectorElementCount(attributeSelectorElementNo);
//        }
//        return applyElementDTO;
//    }
//
//    public static TargetElementDTO createTargetElementDTO(OMElement omElement, String ruleId){
//
//        TargetElementDTO targetElementDTO = new TargetElementDTO();
//        List<SubElementDTO> subElementDTOs = new ArrayList<SubElementDTO>();
//        int subElementId = 0;
//
//        if(omElement != null){
//            if(omElement.getChildrenWithLocalName(EntitlementPolicyConstants.RESOURCE_ELEMENT + "s").
//                    hasNext()){
//                OMElement  element = (OMElement) omElement.getChildrenWithLocalName(
//                        EntitlementPolicyConstants.RESOURCE_ELEMENT + "s").next();
//                Iterator iterator1 = element.getChildrenWithLocalName(EntitlementPolicyConstants.
//                        RESOURCE_ELEMENT);
//                while(iterator1.hasNext()){
//                    OMElement resourceElement = (OMElement)iterator1.next();
//                    subElementDTOs.add(createSubElementDTO(resourceElement, ruleId,
//                                      EntitlementPolicyConstants.RESOURCE_ELEMENT, subElementId));
//                    subElementId ++;
//                }
//            }
//
//            if(omElement.getChildrenWithLocalName(EntitlementPolicyConstants.SUBJECT_ELEMENT + "s").
//                    hasNext()){
//                    OMElement  element = (OMElement) omElement.getChildrenWithLocalName(
//                            EntitlementPolicyConstants.SUBJECT_ELEMENT + "s").next();
//                Iterator iterator2 = element.getChildrenWithLocalName(EntitlementPolicyConstants.
//                        SUBJECT_ELEMENT);
//                while(iterator2.hasNext()){
//                    OMElement resourceElement = (OMElement)iterator2.next();
//                    subElementDTOs.add(createSubElementDTO(resourceElement,ruleId,
//                                      EntitlementPolicyConstants.SUBJECT_ELEMENT, subElementId));
//                    subElementId ++;
//                }
//            }
//
//            if(omElement.getChildrenWithLocalName(EntitlementPolicyConstants.ACTION_ELEMENT + "s").
//                    hasNext()){
//                OMElement  element = (OMElement) omElement.getChildrenWithLocalName(
//                        EntitlementPolicyConstants.ACTION_ELEMENT + "s").next();
//                Iterator iterator3 = element.getChildrenWithLocalName(EntitlementPolicyConstants.
//                ACTION_ELEMENT);
//                while(iterator3.hasNext()){
//                    OMElement resourceElement = (OMElement)iterator3.next();
//                    subElementDTOs.add(createSubElementDTO(resourceElement,ruleId,
//                                      EntitlementPolicyConstants.ACTION_ELEMENT, subElementId));
//                    subElementId ++;
//                }
//            }
//
//            if(omElement.getChildrenWithLocalName(EntitlementPolicyConstants.SUBJECT_ELEMENT + "s").
//                    hasNext()){
//                OMElement  element = (OMElement) omElement.getChildrenWithLocalName(
//                                EntitlementPolicyConstants.SUBJECT_ELEMENT + "s").next();
//                Iterator iterator4 = element.getChildrenWithLocalName(EntitlementPolicyConstants.
//                        ENVIRONMENT_ELEMENT);
//                while(iterator4.hasNext()){
//                    OMElement resourceElement = (OMElement)iterator4.next();
//                    subElementDTOs.add(createSubElementDTO(resourceElement,ruleId,
//                                      EntitlementPolicyConstants.ENVIRONMENT_ELEMENT, subElementId));
//                    subElementId ++;
//                }
//            }
//        }
//
//        targetElementDTO.setSubElementDTOs(subElementDTOs);
//        targetElementDTO.setSubElementCount(subElementId);
//
//        return targetElementDTO;
//    }
//
//    public static SubElementDTO createSubElementDTO(OMElement omElement, String ruleId,
//                                                       String subElementName, int subElementId){
//
//        SubElementDTO subElementDTO = new SubElementDTO();
//        subElementDTO.setElementName(subElementName);
//        subElementDTO.setElementId(subElementId);
//        subElementDTO.setRuleId(ruleId);
//        int matchElementId = 0;
//        if(omElement != null){
//            Iterator iterator1 = omElement.getChildrenWithLocalName(subElementName +
//                                            EntitlementPolicyConstants.MATCH_ELEMENT);
//
//            while(iterator1.hasNext()){
//                MatchElementDTO matchElementDTO = new MatchElementDTO();
//                OMElement matchElement = (OMElement)iterator1.next();
//                matchElementDTO.setMatchElementName(subElementName);
//                matchElementDTO.setElementId(matchElementId);
//                matchElementDTO.setRuleElementName(ruleId);
//                matchElementDTO.setMatchId(matchElement.
//                        getAttributeValue(new QName(EntitlementPolicyConstants.MATCH_ID)));
//
//                Iterator iterator2 = matchElement.getChildrenWithLocalName(subElementName +
//                                                EntitlementPolicyConstants.ATTRIBUTE_DESIGNATOR);
//
//                while(iterator2.hasNext()){
//                    OMElement attributeDesignatorElement = (OMElement)iterator2.next();
//                    matchElementDTO.setAttributeDesignatorDTO(createAttributeDesignatorDTO(
//                            attributeDesignatorElement, 0, subElementName, 0, ""));
//                }
//
//                Iterator iterator3 = matchElement.getChildrenWithLocalName(EntitlementPolicyConstants.
//                        ATTRIBUTE_VALUE);
//
//                while(iterator3.hasNext()){
//                    AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
//                    OMElement attributeValueElement = (OMElement)iterator3.next();
//                    attributeValueElementDTO.setAttributeDataType(attributeValueElement.
//                        getAttributeValue(new QName(EntitlementPolicyConstants.DATA_TYPE)));
//                    attributeValueElementDTO.setAttributeValue(attributeValueElement.getText());
//                    matchElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
//                }
//
//                Iterator iterator4 = matchElement.getChildrenWithLocalName(subElementName +
//                                                EntitlementPolicyConstants.ATTRIBUTE_SELECTOR);
//                while(iterator4.hasNext()){
//                    OMElement attributeSelectorElement = (OMElement)iterator4.next();
//                    matchElementDTO.setAttributeSelectorDTO(createAttributeSelectorDTO(
//                            attributeSelectorElement, 0, 0, ""));
//                }
//                matchElementId ++;
//                subElementDTO.setMatchElementDTOs(matchElementDTO);
//            }
//        }
//        subElementDTO.setMatchElementCount(matchElementId);
//
//        return subElementDTO;
//    }
//
//    /**
//     * This method creates the AttributeDesignatorDTO object using matchElement
//     * @param omElement attributeDesignator OMElement
//     * @param applyElementNo if attributeDesignator element is embed in a apply element, its number
//     * @param elementName  attributeSelectorElement number to uniquely identification
//     * @param matchElementId match element id to identity the element
//     * @param applyElementId  apply element id to identity the element
//     * @return AttributeDesignatorDTO object
//     */
//    public static AttributeDesignatorDTO createAttributeDesignatorDTO(OMElement omElement,
//                                                                      int applyElementNo,
//                                                                      String elementName,
//                                                                      int matchElementId,
//                                                                      String applyElementId){
//        AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
//
//        if(omElement != null){
//            attributeDesignatorDTO.setAttributeId(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.ATTRIBUTE_ID)));
//            attributeDesignatorDTO.setDataType(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.DATA_TYPE)));
//            attributeDesignatorDTO.setIssuer(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.ISSUER)));
//            attributeDesignatorDTO.setMustBePresent(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.MUST_BE_PRESENT)));
//            attributeDesignatorDTO.setApplyElementNumber(applyElementNo);
//            attributeDesignatorDTO.setElementName(elementName);
//            attributeDesignatorDTO.setElementId(matchElementId);
//            attributeDesignatorDTO.setApplyElementId(applyElementId);
//        }
//        return attributeDesignatorDTO;
//    }
//
//    /**
//     * This method creates the AttributeSelectorDTO object using matchElement
//     * @param omElement attributeSelector OMElement
//     * @param applyElementNo if attributeSelector element is embed in a apply element, its number
//     * @param attributeSelectorElementNo  attributeSelectorElement number to uniquely identification
//     * @param applyElementId apply element id to identity the element
//     * @return AttributeSelectorDTO object
//     */
//    public static AttributeSelectorDTO createAttributeSelectorDTO(OMElement omElement,
//                                                                  int applyElementNo,
//                                                                  int attributeSelectorElementNo,
//                                                                  String applyElementId){
//        AttributeSelectorDTO attributeSelectorDTO = new AttributeSelectorDTO();
//
//        if(omElement != null){
//            attributeSelectorDTO.setAttributeSelectorDataType(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.DATA_TYPE)));
//            attributeSelectorDTO.setAttributeSelectorRequestContextPath(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.REQUEST_CONTEXT_PATH)));
//            attributeSelectorDTO.setAttributeSelectorMustBePresent(omElement.
//                    getAttributeValue(new QName(EntitlementPolicyConstants.MUST_BE_PRESENT)));
//            attributeSelectorDTO.setApplyElementNumber(applyElementNo);
//            attributeSelectorDTO.setElementNumber(attributeSelectorElementNo);
//            attributeSelectorDTO.setApplyElementId(applyElementId);
//        }
//        return attributeSelectorDTO;
//    }
//
//    /**
//     *
//     * @param applyElementDTO
//     * @param attributeValueElementNumber
//     * @return
//     */
//    public static int getAttributeValueElementCount(ApplyElementDTO applyElementDTO,
//                                                    int attributeValueElementNumber){
//        attributeValueElementNumber = applyElementDTO.getAttributeValueElementCount();
//        List<ApplyElementDTO> applyElementDTOs = applyElementDTO.getApplyElements();
//        for(ApplyElementDTO elementDTO : applyElementDTOs){
//            attributeValueElementNumber = attributeValueElementNumber +
//                getAttributeValueElementCount(elementDTO, attributeValueElementNumber);
//        }
//        return attributeValueElementNumber;
//    }
//
//    public static int getAttributeDesignatorElementCount(ApplyElementDTO applyElementDTO,
//                                                    int attributeDesignatorElementNumber){
//        attributeDesignatorElementNumber = attributeDesignatorElementNumber + applyElementDTO.
//            getAttributeDesignatorsElementCount();
//        List<ApplyElementDTO> applyElementDTOs = applyElementDTO.getApplyElements();
//        for(ApplyElementDTO elementDTO : applyElementDTOs){
//            attributeDesignatorElementNumber = attributeDesignatorElementNumber +
//                getAttributeDesignatorElementCount(elementDTO, attributeDesignatorElementNumber);
//        }
//        return attributeDesignatorElementNumber;
//    }
//
//    public static int getAttributeSelectorElementCount(ApplyElementDTO applyElementDTO,
//                                                    int attributeSelectorElementNumber){
//        attributeSelectorElementNumber = attributeSelectorElementNumber + applyElementDTO.
//            getAttributeSelectorElementCount();
//        List<ApplyElementDTO> applyElementDTOs = applyElementDTO.getApplyElements();
//        for(ApplyElementDTO elementDTO : applyElementDTOs){
//            attributeSelectorElementNumber = attributeSelectorElementNumber +
//                getAttributeSelectorElementCount(elementDTO, attributeSelectorElementNumber);
//        }
//        return attributeSelectorElementNumber;
//    }
//
//    /**
//     * This method creates policy set element
//     * @param policySetDTO PolicySetDTO
//     * @param doc Document
//     * @return  DOM Element of Policy Set
//     * @throws EntitlementPolicyCreationException  throw exception
//     */
//    public static Element createPolicySetElement(PolicySetDTO policySetDTO, Document doc)
//            throws EntitlementPolicyCreationException {
//
//        Element policySetElement = doc.createElement(EntitlementPolicyConstants.POLICY_SET_ELEMENT);
//        Element targetElement = null;
//        policySetElement.setAttribute("xmlns", EntitlementPolicyConstants.XACML3_POLICY_NAMESPACE);
//
//        if(policySetDTO.getPolicySetId() != null && policySetDTO.getPolicySetId().trim().length() > 0) {
//            policySetElement.setAttribute(EntitlementPolicyConstants.POLICY_SET_ID, policySetDTO.
//                    getPolicySetId());
//        }
//
//        String combiningAlgId = policySetDTO.getPolicyCombiningAlgId();
//        if(combiningAlgId != null && combiningAlgId.trim().length() > 0) {
//
//            if(PolicyEditorConstants.CombiningAlog.ONLY_ONE_APPLICABLE_ID.equals(combiningAlgId) ||
//                    PolicyEditorConstants.CombiningAlog.FIRST_APPLICABLE_ID.equals(combiningAlgId)){
//                policySetElement.setAttribute(EntitlementPolicyConstants.POLICY_ALGORITHM,
//                        PolicyEditorConstants.POLICY_ALGORITHM_IDENTIFIER_1 + combiningAlgId);
//            } else {
//                policySetElement.setAttribute(EntitlementPolicyConstants.POLICY_ALGORITHM,
//                        PolicyEditorConstants.POLICY_ALGORITHM_IDENTIFIER_3 + combiningAlgId);
//            }
//        }
//
//        if(policySetDTO.getVersion() != null && policySetDTO.getVersion().trim().length() > 0){
//            policySetElement.setAttribute(EntitlementPolicyConstants.POLICY_VERSION,
//                                                                    policySetDTO.getVersion());
//        } else {
//            // policy version is handled by wso2 registry.  therefore we can ignore it, although it
//            // is a required attribute
//            policySetElement.setAttribute(EntitlementPolicyConstants.POLICY_VERSION, "1.0");
//        }
//
//
//        Element descriptionElement = doc.createElement(EntitlementPolicyConstants.
//            DESCRIPTION_ELEMENT);
//        if(policySetDTO.getDescription() != null && policySetDTO.
//                getDescription().trim().length() > 0) {
//            descriptionElement.setTextContent(policySetDTO.getDescription());
//            policySetElement.appendChild(descriptionElement);
//        } else {
//            String description = "This is " + policySetDTO.getPolicySetId() + " policy set";
//            descriptionElement.setTextContent(description);
//            policySetElement.appendChild(descriptionElement);
//        }
//
////        if(policySetDTO.getTargetElementDTO() != null &&        // TODO
////                policySetDTO.getTargetElementDTO().getSubElementDTOs() != null){
////            if(policySetDTO.getTargetElementDTO().getSubElementDTOs().size() > 0){
////                targetElement = PolicyEditorUtil.createTargetElement(policySetDTO.getTargetElementDTO().
////                        getSubElementDTOs(), doc);
////            }
////        } else if(policySetDTO.getBasicTargetDTO() != null){
////                targetElement = createBasicTargetElementDTO(policySetDTO.getBasicTargetDTO(), doc);
////        }
//
//        if(targetElement != null){
//            policySetElement.appendChild(targetElement);
//        } else {
//            targetElement = doc.createElement(EntitlementPolicyConstants.TARGET_ELEMENT);
//            policySetElement.appendChild(targetElement);
//        }
//
//        if(policySetDTO.getPolicyIdReferences() != null && policySetDTO.getPolicyIdReferences().size() > 0){
//            for(String policeReferences : policySetDTO.getPolicyIdReferences()){
//                Element policeReferencesElement = doc.
//                        createElement(EntitlementPolicyConstants.POLICY_REFERENCE);
//                policeReferencesElement.setTextContent(policeReferences);
//                policySetElement.appendChild(policeReferencesElement);
//            }
//        }
//
//        if(policySetDTO.getPolicySetIdReferences() != null && policySetDTO.getPolicySetIdReferences().size() > 0){
//            for(String policeSetReferences : policySetDTO.getPolicySetIdReferences()){
//                Element policeSetReferencesElement = doc.
//                        createElement(EntitlementPolicyConstants.POLICY_SET_REFERENCE);
//                policeSetReferencesElement.setTextContent(policeSetReferences);
//                policySetElement.appendChild(policeSetReferencesElement);
//            }
//        }
//        return policySetElement;
//    }
//
//    /**
//     * Convert XACML policy Document element to a String object
//     * @param doc Document element
//     * @return String XACML policy
//     * @throws EntitlementPolicyCreationException throws when transform fails
//     */
//    public static String getStringFromDocument(Document doc) throws EntitlementPolicyCreationException {
//        try {
//
//            DOMSource domSource = new DOMSource(doc);
//            StringWriter writer = new StringWriter();
//            StreamResult result = new StreamResult(writer);
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            transformer.transform(domSource, result);
//            return writer.toString().substring(writer.toString().indexOf('>') + 1);
//
//        } catch(TransformerException e){
//            throw new EntitlementPolicyCreationException("While transforming policy element to String", e);
//        }
//    }
//
//    /**
//     * Select relavent function ID for given function name
//     * @param functionName function name as String argument
//     * @return returns function ID
//     */
//    private static String getFunctionId(String functionName){
//
//        String functionId;
//
//        if(functionName.equals(EntitlementPolicyConstants.REGEXP_MATCH)){
//            functionId = EntitlementPolicyConstants.FUNCTION_REGEXP;
//        } else if(functionName.equals(EntitlementPolicyConstants.IS_IN)){
//            functionId = EntitlementPolicyConstants.FUNCTION_IS_IN;
//        } else if(functionName.equals(EntitlementPolicyConstants.SET_OF)){
//            functionId = EntitlementPolicyConstants.FUNCTION_SET_EQUAL;
//        } else if(functionName.equals(EntitlementPolicyConstants.SUBSET_OF)){
//            functionId = EntitlementPolicyConstants.FUNCTION_SUBSET;
//        } else if(functionName.equals(EntitlementPolicyConstants.AT_LEAST)){
//            functionId = EntitlementPolicyConstants.FUNCTION_AT_LEAST;
//        } else {
//            functionId = EntitlementPolicyConstants.FUNCTION_EQUAL;
//        }
//
//        return functionId;
//    }
//
//
////    /**
////     * create policy meta data that helps to edit the policy using basic editor
////     * @param order of the rule element are decided by this
////     * @return  String Array to dent to back end
////     */
////    public static String[] generateBasicPolicyEditorData(TargetDTO basicTargetDTO,
////                                            List <RuleDTO> ruleDTOs,
////                                            String ruleElementOrder){
////
////        List<String> policyMetaDataList = new ArrayList<String>();
////
////        if(basicTargetDTO != null){
////            List<RowDTO> rowDTOs = basicTargetDTO.getRowDTOList();
////            for(RowDTO rowDTO : rowDTOs){
////                createMetaDataFromRowDTO("target", rowDTO, policyMetaDataList);
////            }
////        }
////
////        if(ruleDTOs != null && ruleDTOs.size() > 0){
////            if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
////                String[] ruleIds = ruleElementOrder.
////                        split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
////                for(String ruleId : ruleIds){
////                    for(RuleDTO ruleDTO : ruleDTOs) {
////                        if(ruleId.trim().equals(ruleDTO.getRuleId())){
////                            List<RowDTO> rowDTOs = ruleDTO.getRowDTOList();
////                            if(rowDTOs != null && rowDTOs.size() > 0){
////                                for(RowDTO rowDTO : rowDTOs){
////                                    createMetaDataFromRowDTO("rule" + ruleId, rowDTO,
////                                                                                policyMetaDataList);
////                                }
////                            }
////
////                            if(ruleDTO.getTargetDTO() != null &&
////                                                ruleDTO.getTargetDTO().getRowDTOList() != null){
////                                for(RowDTO rowDTO : ruleDTO.getTargetDTO().getRowDTOList()){
////                                    createMetaDataFromRowDTO("ruleTarget" + ruleId, rowDTO,
////                                                                                policyMetaDataList);
////                                }
////                            }
////                        }
////                    }
////                }
////            } else {
////                for(RuleDTO ruleDTO : ruleDTOs) {
////                    List<RowDTO> rowDTOs = ruleDTO.getRowDTOList();
////                    if(rowDTOs != null && rowDTOs.size() > 0){
////                        for(RowDTO rowDTO : rowDTOs){
////                            createMetaDataFromRowDTO("rule" + ruleDTO.getRuleId(), rowDTO,
////                                                                        policyMetaDataList);
////                        }
////                    }
////
////                    if(ruleDTO.getTargetDTO() != null &&
////                                        ruleDTO.getTargetDTO().getRowDTOList() != null){
////                        for(RowDTO rowDTO : ruleDTO.getTargetDTO().getRowDTOList()){
////                            createMetaDataFromRowDTO("ruleTarget" + ruleDTO.getRuleId(), rowDTO,
////                                                                        policyMetaDataList);
////                        }
////                    }
////                }
////            }
////        }
////
////        return policyMetaDataList.toArray(new String[policyMetaDataList.size()]);
////    }
//
//
//    private static void createMetaDataFromRowDTO(String prefix, RowDTO rowDTO, List<String> metaDataList){
//
//        if(metaDataList != null){
//            metaDataList.add(prefix + "|" + rowDTO.getCategory());
//            metaDataList.add(prefix + "|" + rowDTO.getPreFunction());
//            metaDataList.add(prefix + "|" + rowDTO.getFunction());
//            metaDataList.add(prefix + "|" + rowDTO.getAttributeValue());
//            metaDataList.add(prefix + "|" + rowDTO.getAttributeId());
//            metaDataList.add(prefix + "|" + rowDTO.getAttributeDataType());
//            metaDataList.add(prefix + "|" + rowDTO.getCombineFunction());
//        }
//    }

}