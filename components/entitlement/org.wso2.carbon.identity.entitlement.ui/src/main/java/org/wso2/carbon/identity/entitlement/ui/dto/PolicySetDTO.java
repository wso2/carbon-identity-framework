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

package org.wso2.carbon.identity.entitlement.ui.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class PolicySetDTO {

    private String policySetId;

    private String policyCombiningAlgId;

    private String version;

    private TargetDTO targetDTO;

    private String description;

    private List<String> policySets = new ArrayList<String>();

    private List<String> policies = new ArrayList<String>();

    private List<String> policySetIdReferences = new ArrayList<String>();

    private List<String> PolicyIdReferences = new ArrayList<String>();

    private List<ObligationDTO> obligations = new ArrayList<ObligationDTO>();

    private List<PolicyRefIdDTO> policyRefIdDTOs = new ArrayList<PolicyRefIdDTO>();

    private String policyOrder;

    public String getPolicySetId() {
        return policySetId;
    }

    public void setPolicySetId(String policySetId) {
        this.policySetId = policySetId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPolicyCombiningAlgId() {
        return policyCombiningAlgId;
    }

    public void setPolicyCombiningAlgId(String policyCombiningAlgId) {
        this.policyCombiningAlgId = policyCombiningAlgId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPolicySets() {
        return policySets;
    }

    public void setPolicySets(List<String> policySets) {
        this.policySets = policySets;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicy(String policy) {
        this.policies.add(policy);
    }

    public List<String> getPolicySetIdReferences() {
        return policySetIdReferences;
    }

    public void setPolicySetIdReferences(List<String> policySetIdReferences) {
        this.policySetIdReferences = policySetIdReferences;
    }

    public List<String> getPolicyIdReferences() {
        return PolicyIdReferences;
    }

    public void setPolicyIdReferences(List<String> policyIdReferences) {
        PolicyIdReferences = policyIdReferences;
    }

    public List<ObligationDTO> getObligations() {
        return obligations;
    }

    public void setObligations(List<ObligationDTO> obligations) {
        this.obligations = obligations;
    }

    public TargetDTO getTargetDTO() {
        return targetDTO;
    }

    public void setTargetDTO(TargetDTO targetDTO) {
        this.targetDTO = targetDTO;
    }

    public String getPolicyOrder() {
        return policyOrder;
    }

    public void setPolicyOrder(String policyOrder) {
        this.policyOrder = policyOrder;
    }

    public List<PolicyRefIdDTO> getPolicyRefIdDTOs() {
        return policyRefIdDTOs;
    }

    public void setPolicyRefIdDTOs(List<PolicyRefIdDTO> policyRefIdDTOs) {
        this.policyRefIdDTOs = policyRefIdDTOs;
    }
}
