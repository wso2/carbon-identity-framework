/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.ui.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PolicyDTO {

    private String policyId;

    private String ruleAlgorithm;

    private String description;

    private String ruleOrder;

    private String version;

    private TargetDTO targetDTO;

    private List<RuleDTO> ruleDTOs = new ArrayList<RuleDTO>();

    private List<ObligationDTO> obligationDTOs = new ArrayList<ObligationDTO>();

    public String getRuleAlgorithm() {
        return ruleAlgorithm;
    }

    public void setRuleAlgorithm(String ruleAlgorithm) {
        this.ruleAlgorithm = ruleAlgorithm;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleOrder() {
        return ruleOrder;
    }

    public void setRuleOrder(String ruleOrder) {
        this.ruleOrder = ruleOrder;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public TargetDTO getTargetDTO() {
        return targetDTO;
    }

    public void setTargetDTO(TargetDTO targetDTO) {
        this.targetDTO = targetDTO;
    }

    public List<RuleDTO> getRuleDTOs() {
        return ruleDTOs;
    }

    public void setRuleDTOs(List<RuleDTO> ruleDTOs) {
        this.ruleDTOs = ruleDTOs;
    }

    public List<ObligationDTO> getObligationDTOs() {
        return obligationDTOs;
    }

    public void setObligationDTOs(List<ObligationDTO> obligationDTOs) {
        this.obligationDTOs = obligationDTOs;
    }
}