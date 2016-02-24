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

package org.wso2.carbon.identity.entitlement.ui;

import org.wso2.balana.utils.policy.dto.BasicRuleDTO;
import org.wso2.balana.utils.policy.dto.BasicTargetDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.ExtendAttributeDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.ObligationDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.PolicyRefIdDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.PolicySetDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.RuleDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.TargetDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This Bean is used to keep the user data temporary while travelling through
 * the UI wizard
 */
public class EntitlementPolicyBean {

    public Map<String, String> functionIdMap = new HashMap<String, String>();
    public Map<String, String> functionIdElementValueMap = new HashMap<String, String>();
    private String policyName;
    private String algorithmName;
    private String policyDescription;
    private String userInputData;
    private List<String> subscribersList = new ArrayList<String>();
    private SimplePolicyEditorDTO SimplePolicyEditorDTO;
    private Map<String, String> categoryMap = new HashMap<String, String>();
    private Map<String, String> targetFunctionMap = new HashMap<String, String>();
    private Map<String, String> attributeIdMap = new HashMap<String, String>();
    private Map<String, String> ruleFunctionMap = new HashMap<String, String>();
    private boolean editPolicy;
    private String[] policyCombiningAlgorithms = new String[0];
    private Map<String, EntitlementFinderDataHolder> entitlementFinders =
            new HashMap<String, EntitlementFinderDataHolder>();
    private Map<Integer, String> selectedEntitlementData = new HashMap<Integer, String>();
    private Map<Integer, EntitlementTreeNodeDTO> entitlementLevelData =
            new HashMap<Integer, EntitlementTreeNodeDTO>();
    private BasicTargetDTO basicTargetDTO = null;
    private TargetDTO targetDTO = null;
    private PolicySetDTO policySetDTO = null;
    private List<BasicRuleDTO> basicRuleDTOs = new ArrayList<BasicRuleDTO>();

    private List<RuleDTO> ruleDTOs = new ArrayList<RuleDTO>();

    private List<ExtendAttributeDTO> extendAttributeDTOs = new ArrayList<ExtendAttributeDTO>();

    private List<ObligationDTO> obligationDTOs = new ArrayList<ObligationDTO>();

    private String ruleElementOrder;

    private String policyReferenceOrder;

    private Set<String> preFunctions = new HashSet<String>();

    private List<PolicyRefIdDTO> policyRefIds = new ArrayList<PolicyRefIdDTO>();

    /**
     * This method is temporally used to clear the entitlement bean. Need to
     * update with a method proper implementation TODO
     */
    public void cleanEntitlementPolicyBean() {

        policyName = null;

        algorithmName = null;

        policyDescription = null;

        userInputData = null;

        editPolicy = false;

        policySetDTO = null;

        functionIdMap.clear();

        functionIdElementValueMap.clear();

        basicRuleDTOs.clear();

        removeBasicTargetElementDTO();

        targetDTO = null;

        ruleDTOs.clear();

        extendAttributeDTOs.clear();

        obligationDTOs.clear();

        SimplePolicyEditorDTO = null;

        basicTargetDTO = null;

        policyReferenceOrder = null;

        policyRefIds.clear();

    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

    public String getUserInputData() {
        return userInputData;
    }

    public void setUserInputData(String userInputData) {
        this.userInputData = userInputData;
    }

    public List<BasicRuleDTO> getBasicRuleDTOs() {
        return basicRuleDTOs;
    }

    public void setBasicRuleDTOs(List<BasicRuleDTO> basicRuleDTOs) {
        this.basicRuleDTOs = basicRuleDTOs;
    }

    public void setBasicRuleElementDTOs(BasicRuleDTO basicRuleDTO) {
        if (basicRuleDTOs.size() > 0) {
            Iterator iterator = basicRuleDTOs.listIterator();
            while (iterator.hasNext()) {
                BasicRuleDTO elementDTO = (BasicRuleDTO) iterator
                        .next();
                if (elementDTO.getRuleId().equals(
                        basicRuleDTO.getRuleId())) {
                    if (elementDTO.isCompletedRule()) {
                        basicRuleDTO.setCompletedRule(true);
                    }
                    iterator.remove();
                }
            }
        }
        this.basicRuleDTOs.add(basicRuleDTO);
    }

    public BasicRuleDTO getBasicRuleElement(String ruleId) {
        if (basicRuleDTOs.size() > 0) {
            for (BasicRuleDTO basicRuleDTO : basicRuleDTOs) {
                if (basicRuleDTO.getRuleId().equals(ruleId)) {
                    return basicRuleDTO;
                }
            }
        }
        return null;
    }

    public boolean removeBasicRuleElement(String ruleId) {
        if (basicRuleDTOs.size() > 0 && ruleId != null) {
            for (BasicRuleDTO basicRuleDTO : basicRuleDTOs) {
                if (ruleId.equals(basicRuleDTO.getRuleId())) {
                    return basicRuleDTOs.remove(basicRuleDTO);
                }
            }
        }
        return false;
    }

    public void removeBasicRuleElements() {
        if (basicRuleDTOs.size() > 0) {
            Iterator iterator = basicRuleDTOs.listIterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }


/////////////////////////////////////// new

    public List<RuleDTO> getRuleDTOs() {
        return ruleDTOs;
    }

    public void setRuleDTOs(List<RuleDTO> ruleDTOs) {
        this.ruleDTOs = ruleDTOs;
    }

    public void setRuleDTO(RuleDTO ruleDTO) {
        if (ruleDTOs.size() > 0) {
            Iterator iterator = ruleDTOs.listIterator();
            while (iterator.hasNext()) {
                RuleDTO elementDTO = (RuleDTO) iterator.next();
                if (elementDTO.getRuleId().equals(
                        ruleDTO.getRuleId())) {
                    if (elementDTO.isCompletedRule()) {
                        ruleDTO.setCompletedRule(true);
                    }
                    iterator.remove();
                }
            }
        }
        this.ruleDTOs.add(ruleDTO);
    }

    public RuleDTO getRuleDTO(String ruleId) {
        if (ruleDTOs.size() > 0) {
            for (RuleDTO ruleDTO : ruleDTOs) {
                if (ruleDTO.getRuleId().equals(ruleId)) {
                    return ruleDTO;
                }
            }
        }
        return null;
    }

    public boolean removeRuleDTO(String ruleId) {
        if (ruleDTOs.size() > 0) {
            for (RuleDTO ruleDTO : ruleDTOs) {
                if (ruleDTO.getRuleId().equals(ruleId)) {
                    return ruleDTOs.remove(ruleDTO);
                }
            }
        }
        return false;
    }

    public void removeRuleDTOs() {
        if (ruleDTOs.size() > 0) {
            Iterator iterator = ruleDTOs.listIterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    public List<ExtendAttributeDTO> getExtendAttributeDTOs() {
        return extendAttributeDTOs;
    }

    public void setExtendAttributeDTOs(List<ExtendAttributeDTO> extendAttributeDTOs) {
        this.extendAttributeDTOs = extendAttributeDTOs;
    }

    public List<ObligationDTO> getObligationDTOs() {
        return obligationDTOs;
    }

    public void setObligationDTOs(List<ObligationDTO> obligationDTOs) {
        this.obligationDTOs = obligationDTOs;
    }

    public void addExtendAttributeDTO(ExtendAttributeDTO extendAttributeDTO) {
        this.extendAttributeDTOs.add(extendAttributeDTO);
    }

    ///////////////////////////    ////////
    public BasicTargetDTO getBasicTargetDTO() {
        return basicTargetDTO;
    }

    public void setBasicTargetDTO(
            BasicTargetDTO basicTargetDTO) {
        this.basicTargetDTO = basicTargetDTO;
    }

    public void removeBasicTargetElementDTO() {
        this.basicTargetDTO = null;
    }

    public boolean isEditPolicy() {
        return editPolicy;
    }

    public void setEditPolicy(boolean editPolicy) {
        this.editPolicy = editPolicy;
    }

    public String[] getPolicyCombiningAlgorithms() {
        return Arrays.copyOf(policyCombiningAlgorithms, policyCombiningAlgorithms.length);
    }

    public void setPolicyCombiningAlgorithms(String[] policyCombiningAlgorithms) {
        this.policyCombiningAlgorithms = Arrays.copyOf(policyCombiningAlgorithms, policyCombiningAlgorithms.length);
    }

    public PolicySetDTO getPolicySetDTO() {
        return policySetDTO;
    }

    public void setPolicySetDTO(PolicySetDTO policySetDTO) {
        this.policySetDTO = policySetDTO;
    }

    public String getRuleElementOrder() {
        return ruleElementOrder;
    }

    public void setRuleElementOrder(String ruleElementOrder) {
        this.ruleElementOrder = ruleElementOrder;
    }


    public TargetDTO getTargetDTO() {
        return targetDTO;
    }

    public void setTargetDTO(TargetDTO targetDTO) {
        this.targetDTO = targetDTO;
    }

    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public void setCategoryMap(Map<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public Set<String> getCategorySet() {
        return categoryMap.keySet();
    }

    public Map<String, String> getRuleFunctionMap() {
        return ruleFunctionMap;
    }

    public void setRuleFunctionMap(Map<String, String> ruleFunctionMap) {
        this.ruleFunctionMap = ruleFunctionMap;
    }

    public Map<String, String> getTargetFunctionMap() {
        return targetFunctionMap;
    }

    public void setTargetFunctionMap(Map<String, String> targetFunctionMap) {
        this.targetFunctionMap = targetFunctionMap;
    }

    public Map<String, String> getAttributeIdMap() {
        return attributeIdMap;
    }

    public void setAttributeIdMap(Map<String, String> attributeIdMap) {
        this.attributeIdMap = attributeIdMap;
    }

    public Set<String> getPreFunctions() {
        return preFunctions;
    }

    public void addPreFunction(String preFunction) {
        this.preFunctions.add(preFunction);
    }


    public SimplePolicyEditorDTO getSimplePolicyEditorDTO() {
        return SimplePolicyEditorDTO;
    }

    public void setSimplePolicyEditorDTO(SimplePolicyEditorDTO simplePolicyEditorDTO) {
        this.SimplePolicyEditorDTO = simplePolicyEditorDTO;
    }

    public Map<String, EntitlementFinderDataHolder> getEntitlementFinders() {
        return entitlementFinders;
    }

    public Set<EntitlementFinderDataHolder> getEntitlementFinders(String category) {
        Set<EntitlementFinderDataHolder> holders = new HashSet<EntitlementFinderDataHolder>();
        for (Map.Entry<String, EntitlementFinderDataHolder> entry : entitlementFinders.entrySet()) {
            EntitlementFinderDataHolder holder = entry.getValue();
            if (Arrays.asList(holder.getSupportedCategory()).contains(category)) {
                holders.add(holder);
            }
        }
        return holders;
    }

    public void setEntitlementFinders(String name, EntitlementFinderDataHolder entitlementFinders) {
        this.entitlementFinders.put(name, entitlementFinders);
    }

    public Map<Integer, String> getSelectedEntitlementData() {
        return selectedEntitlementData;
    }

    public Map<Integer, EntitlementTreeNodeDTO> getEntitlementLevelData() {
        return entitlementLevelData;
    }

    public List<PolicyRefIdDTO> getPolicyRefIds() {
        return policyRefIds;
    }

    public void setPolicyRefIds(List<PolicyRefIdDTO> policyRefIds) {
        this.policyRefIds = policyRefIds;
    }

    public void addPolicyRefId(PolicyRefIdDTO policyRefId) {
        Iterator iterator = policyRefIds.listIterator();
        while (iterator.hasNext()) {
            PolicyRefIdDTO dto = (PolicyRefIdDTO) iterator.next();
            if (policyRefId != null && dto.getId().equalsIgnoreCase(policyRefId.getId())) {
                iterator.remove();
            }
        }
        this.policyRefIds.add(policyRefId);
    }

    public void removePolicyRefId(String policyRefId) {
        Iterator iterator = policyRefIds.listIterator();
        while (iterator.hasNext()) {
            PolicyRefIdDTO dto = (PolicyRefIdDTO) iterator.next();
            if (policyRefId != null && dto.getId().equalsIgnoreCase(policyRefId)) {
                iterator.remove();
            }
        }
    }

    public String getPolicyReferenceOrder() {
        return policyReferenceOrder;
    }

    public void setPolicyReferenceOrder(String policyReferenceOrder) {
        this.policyReferenceOrder = policyReferenceOrder;
    }

    public List<String> getSubscribersList() {
        return subscribersList;
    }

    public void setSubscribersList(String[] subscribersList) {
        List<String> list = new ArrayList<String>(Arrays.asList(subscribersList));
        this.subscribersList.addAll(list);
    }
}