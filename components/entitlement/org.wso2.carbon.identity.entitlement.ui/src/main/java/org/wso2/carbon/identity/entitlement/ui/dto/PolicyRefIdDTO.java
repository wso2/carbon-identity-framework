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

/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class PolicyRefIdDTO {

    private String id;

    private boolean referenceOnly;

    private boolean policySet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPolicySet() {
        return policySet;
    }

    public void setPolicySet(boolean policySet) {
        this.policySet = policySet;
    }

    public boolean isReferenceOnly() {
        return referenceOnly;
    }

    public void setReferenceOnly(boolean referenceOnly) {
        this.referenceOnly = referenceOnly;
    }
}
