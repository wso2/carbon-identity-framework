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
package org.wso2.carbon.identity.entitlement.dto;

import java.util.Arrays;

/**
 * This class encapsulate the XACML policy related the data
 */
public class PolicyDTO {


    private String policy;

    private String policyId;

    private boolean active;

    private boolean promote;

    private String policyType;

    private String policyEditor;

    private String[] policyEditorData = new String[0];

    private int policyOrder;

    private String version;

    private String lastModifiedTime;

    private String lastModifiedUser;

    private AttributeDTO[] attributeDTOs = new AttributeDTO[0];

    private String[] policySetIdReferences = new String[0];

    private String[] policyIdReferences = new String[0];

    public PolicyDTO() {

    }

    public PolicyDTO(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }


    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getPolicyEditor() {
        return policyEditor;
    }

    public void setPolicyEditor(String policyEditor) {
        this.policyEditor = policyEditor;
    }

    public String[] getPolicyEditorData() {
        return Arrays.copyOf(policyEditorData, policyEditorData.length);
    }

    public void setPolicyEditorData(String[] policyEditorData) {
        this.policyEditorData = Arrays.copyOf(policyEditorData,
                                              policyEditorData.length);
    }

    public AttributeDTO[] getAttributeDTOs() {
        return Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public void setAttributeDTOs(AttributeDTO[] attributeDTOs) {
        this.attributeDTOs = Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public int getPolicyOrder() {
        return policyOrder;
    }

    public void setPolicyOrder(int policyOrder) {
        this.policyOrder = policyOrder;
    }

    public String[] getPolicySetIdReferences() {
        return Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public void setPolicySetIdReferences(String[] policySetIdReferences) {
        this.policySetIdReferences = Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public String[] getPolicyIdReferences() {
        return Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public void setPolicyIdReferences(String[] policyIdReferences) {
        this.policyIdReferences = Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isPromote() {
        return promote;
    }

    public void setPromote(boolean promote) {
        this.promote = promote;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(String lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolicyDTO)) return false;

        PolicyDTO policyDTO = (PolicyDTO) o;

        if (active != policyDTO.active) return false;
        if (policyOrder != policyDTO.policyOrder) return false;
        if (promote != policyDTO.promote) return false;
        if (!Arrays.equals(attributeDTOs, policyDTO.attributeDTOs)) return false;
        if (lastModifiedTime != null ? !lastModifiedTime.equals(policyDTO.lastModifiedTime) :
            policyDTO.lastModifiedTime != null) {
            return false;
        }
        if (lastModifiedUser != null ? !lastModifiedUser.equals(policyDTO.lastModifiedUser) :
            policyDTO.lastModifiedUser != null) {
            return false;
        }
        if (policy != null ? !policy.equals(policyDTO.policy) : policyDTO.policy != null) return false;
        if (policyEditor != null ? !policyEditor.equals(policyDTO.policyEditor) : policyDTO.policyEditor != null) {
            return false;
        }
        if (!Arrays.equals(policyEditorData, policyDTO.policyEditorData)) return false;
        if (!policyId.equals(policyDTO.policyId)) return false;
        if (!Arrays.equals(policyIdReferences, policyDTO.policyIdReferences)) return false;
        if (!Arrays.equals(policySetIdReferences, policyDTO.policySetIdReferences)) return false;
        if (policyType != null ? !policyType.equals(policyDTO.policyType) : policyDTO.policyType != null) return false;
        if (version != null ? !version.equals(policyDTO.version) : policyDTO.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = policy != null ? policy.hashCode() : 0;
        result = 31 * result + policyId.hashCode();
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (promote ? 1 : 0);
        result = 31 * result + (policyType != null ? policyType.hashCode() : 0);
        result = 31 * result + (policyEditor != null ? policyEditor.hashCode() : 0);
        result = 31 * result + (policyEditorData != null ? Arrays.hashCode(policyEditorData) : 0);
        result = 31 * result + policyOrder;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (lastModifiedTime != null ? lastModifiedTime.hashCode() : 0);
        result = 31 * result + (lastModifiedUser != null ? lastModifiedUser.hashCode() : 0);
        result = 31 * result + (attributeDTOs != null ? Arrays.hashCode(attributeDTOs) : 0);
        result = 31 * result + (policySetIdReferences != null ? Arrays.hashCode(policySetIdReferences) : 0);
        result = 31 * result + (policyIdReferences != null ? Arrays.hashCode(policyIdReferences) : 0);
        return result;
    }
}
