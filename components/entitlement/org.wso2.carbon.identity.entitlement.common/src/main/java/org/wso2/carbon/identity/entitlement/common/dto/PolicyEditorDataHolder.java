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

package org.wso2.carbon.identity.entitlement.common.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class PolicyEditorDataHolder {

    private Map<String, String> categoryMap = new HashMap<String, String>();

    private Map<String, String> attributeIdMap = new HashMap<String, String>();

    private Map<String, String> dataTypeMap = new HashMap<String, String>();

    private Map<String, String> functionMap = new HashMap<String, String>();

    private Map<String, String> preFunctionMap = new HashMap<String, String>();

    private Map<String, String> ruleEffectMap = new HashMap<String, String>();

    private Map<String, Set<String>> categoryAttributeIdMap = new HashMap<String, Set<String>>();

    private Map<String, String> categoryDefaultAttributeIdMap = new HashMap<String, String>();

    private Map<String, Set<String>> categoryDataTypeMap = new HashMap<String, Set<String>>();

    private Map<String, Set<String>> categoryFunctionMap = new HashMap<String, Set<String>>();

    private Map<String, String> attributeIdDataTypeMap = new HashMap<String, String>();

    private Set<String> ruleFunctions = new HashSet<String>();

    private Set<String> targetFunctions = new HashSet<String>();

    private Set<String> preFunctions = new HashSet<String>();

    private Map<String, String> ruleCombiningAlgorithms = new HashMap<String, String>();

    private Map<String, String> policyCombiningAlgorithms = new HashMap<String, String>();

    private String defaultDataType;

    private boolean showRuleAlgorithms;

    private boolean showPolicyAlgorithms;

    private boolean showPreFunctions;

    private boolean showPolicyDescription;

    private boolean showRuleId;

    private boolean showRuleDescription;

    private boolean showRuleEffect;

    private boolean addLastRule;

    private String lastRuleEffect;

    private String defaultEffect;

    private String defaultRuleAlgorithm;

    private String defaultPolicyAlgorithm;

    public String getCategoryUri(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        return categoryMap.get(categoryName);
    }

    public String getAttributeIdUri(String attributeId) {
        if (attributeId == null) {
            return null;
        }
        return attributeIdMap.get(attributeId);
    }

    public String getDataTypeUri(String dataType) {
        if (dataType == null) {
            return null;
        }
        return dataTypeMap.get(dataType);
    }

    public String getFunctionUri(String function) {
        if (function == null) {
            return null;
        }
        return functionMap.get(function);
    }

    public String getPreFunctionUri(String function) {
        if (function == null) {
            return null;
        }
        return preFunctionMap.get(function);
    }

    public String getRuleAlgorithmUri(String algorithm) {
        if (algorithm == null) {
            return null;
        }
        return ruleCombiningAlgorithms.get(algorithm);
    }

    public String getPolicyAlgorithmUri(String algorithm) {
        if (algorithm == null) {
            return null;
        }
        return policyCombiningAlgorithms.get(algorithm);
    }

    public String getDataTypeUriForAttribute(String attributeId) {
        if (attributeId == null) {
            return null;
        }
        return attributeIdDataTypeMap.get(attributeId);
    }

    public String getRuleEffectUri(String effect) {
        if (effect == null) {
            return null;
        }
        return ruleEffectMap.get(effect);
    }

    public String getCategoryDefaultAttributeId(String category) {
        if (category == null) {
            return null;
        }
        return categoryDefaultAttributeIdMap.get(category);
    }

    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public void setCategoryMap(Map<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public Map<String, String> getAttributeIdMap() {
        return attributeIdMap;
    }

    public void setAttributeIdMap(Map<String, String> attributeIdMap) {
        this.attributeIdMap = attributeIdMap;
    }

    public Map<String, String> getDataTypeMap() {
        return dataTypeMap;
    }

    public void setDataTypeMap(Map<String, String> dataTypeMap) {
        this.dataTypeMap = dataTypeMap;
    }

    public Map<String, String> getFunctionMap() {
        return functionMap;
    }

    public void setFunctionMap(Map<String, String> functionMap) {
        this.functionMap = functionMap;
    }

    public Map<String, Set<String>> getCategoryAttributeIdMap() {
        return categoryAttributeIdMap;
    }

    public void setCategoryAttributeIdMap(Map<String, Set<String>> categoryAttributeIdMap) {
        this.categoryAttributeIdMap = categoryAttributeIdMap;
    }

    public Map<String, Set<String>> getCategoryDataTypeMap() {
        return categoryDataTypeMap;
    }

    public void setCategoryDataTypeMap(Map<String, Set<String>> categoryDataTypeMap) {
        this.categoryDataTypeMap = categoryDataTypeMap;
    }

    public Map<String, String> getAttributeIdDataTypeMap() {
        return attributeIdDataTypeMap;
    }

    public void setAttributeIdDataTypeMap(Map<String, String> attributeIdDataTypeMap) {
        this.attributeIdDataTypeMap = attributeIdDataTypeMap;
    }

    public Set<String> getRuleFunctions() {
        return ruleFunctions;
    }

    public void setRuleFunctions(Set<String> ruleFunctions) {
        this.ruleFunctions = ruleFunctions;
    }

    public Set<String> getTargetFunctions() {
        return targetFunctions;
    }

    public void setTargetFunctions(Set<String> targetFunctions) {
        this.targetFunctions = targetFunctions;
    }

    public String getDefaultDataType() {
        return defaultDataType;
    }

    public void setDefaultDataType(String defaultDataType) {
        this.defaultDataType = defaultDataType;
    }

    public Map<String, String> getRuleCombiningAlgorithms() {
        return ruleCombiningAlgorithms;
    }

    public void setRuleCombiningAlgorithms(Map<String, String> ruleCombiningAlgorithms) {
        this.ruleCombiningAlgorithms = ruleCombiningAlgorithms;
    }

    public boolean isShowRuleAlgorithms() {
        return showRuleAlgorithms;
    }

    public void setShowRuleAlgorithms(boolean showRuleAlgorithms) {
        this.showRuleAlgorithms = showRuleAlgorithms;
    }

    public String getDefaultRuleAlgorithm() {
        return defaultRuleAlgorithm;
    }

    public void setDefaultRuleAlgorithm(String defaultRuleAlgorithm) {
        this.defaultRuleAlgorithm = defaultRuleAlgorithm;
    }

    public Map<String, Set<String>> getCategoryFunctionMap() {
        return categoryFunctionMap;
    }

    public void setCategoryFunctionMap(Map<String, Set<String>> categoryFunctionMap) {
        this.categoryFunctionMap = categoryFunctionMap;
    }

    public Set<String> getPreFunctions() {
        return preFunctions;
    }

    public void setPreFunctions(Set<String> preFunctions) {
        this.preFunctions = preFunctions;
    }

    public boolean isShowPreFunctions() {
        return showPreFunctions;
    }

    public void setShowPreFunctions(boolean showPreFunctions) {
        this.showPreFunctions = showPreFunctions;
    }

    public boolean isShowPolicyDescription() {
        return showPolicyDescription;
    }

    public void setShowPolicyDescription(boolean showPolicyDescription) {
        this.showPolicyDescription = showPolicyDescription;
    }

    public boolean isShowRuleId() {
        return showRuleId;
    }

    public void setShowRuleId(boolean showRuleId) {
        this.showRuleId = showRuleId;
    }

    public boolean isShowRuleDescription() {
        return showRuleDescription;
    }

    public void setShowRuleDescription(boolean showRuleDescription) {
        this.showRuleDescription = showRuleDescription;
    }

    public boolean isShowRuleEffect() {
        return showRuleEffect;
    }

    public void setShowRuleEffect(boolean showRuleEffect) {
        this.showRuleEffect = showRuleEffect;
    }

    public boolean isAddLastRule() {
        return addLastRule;
    }

    public void setAddLastRule(boolean addLastRule) {
        this.addLastRule = addLastRule;
    }

    public String getLastRuleEffect() {
        return lastRuleEffect;
    }

    public void setLastRuleEffect(String lastRuleEffect) {
        this.lastRuleEffect = lastRuleEffect;
    }

    public String getDefaultEffect() {
        return defaultEffect;
    }

    public void setDefaultEffect(String defaultEffect) {
        this.defaultEffect = defaultEffect;
    }

    public Map<String, String> getPreFunctionMap() {
        return preFunctionMap;
    }

    public void setPreFunctionMap(Map<String, String> preFunctionMap) {
        this.preFunctionMap = preFunctionMap;
    }

    public Map<String, String> getRuleEffectMap() {
        return ruleEffectMap;
    }

    public void setRuleEffectMap(Map<String, String> ruleEffectMap) {
        this.ruleEffectMap = ruleEffectMap;
    }

    public Map<String, String> getCategoryDefaultAttributeIdMap() {
        return categoryDefaultAttributeIdMap;
    }

    public Map<String, String> getPolicyCombiningAlgorithms() {
        return policyCombiningAlgorithms;
    }

    public void setPolicyCombiningAlgorithms(Map<String, String> policyCombiningAlgorithms) {
        this.policyCombiningAlgorithms = policyCombiningAlgorithms;
    }

    public String getDefaultPolicyAlgorithm() {
        return defaultPolicyAlgorithm;
    }

    public void setDefaultPolicyAlgorithm(String defaultPolicyAlgorithm) {
        this.defaultPolicyAlgorithm = defaultPolicyAlgorithm;
    }

    public boolean isShowPolicyAlgorithms() {
        return showPolicyAlgorithms;
    }

    public void setShowPolicyAlgorithms(boolean showPolicyAlgorithms) {
        this.showPolicyAlgorithms = showPolicyAlgorithms;
    }
}
