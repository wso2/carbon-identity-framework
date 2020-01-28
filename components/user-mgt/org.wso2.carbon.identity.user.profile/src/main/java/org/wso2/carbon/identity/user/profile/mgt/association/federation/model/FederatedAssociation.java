/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.profile.mgt.association.federation.model;

/**
 * Model class representing a federated user account association.
 */
public class FederatedAssociation {

    private String id;
    private AssociatedIdentityProvider idp;
    private String federatedUserId;

    public FederatedAssociation(String id, AssociatedIdentityProvider idp, String federatedUserId) {

        this.id = id;
        this.idp = idp;
        this.federatedUserId = federatedUserId;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public AssociatedIdentityProvider getIdp() {

        return idp;
    }

    public void setIdp(AssociatedIdentityProvider idp) {

        this.idp = idp;
    }

    public String getFederatedUserId() {

        return federatedUserId;
    }

    public void setFederatedUserId(String federatedUserId) {

        this.federatedUserId = federatedUserId;
    }
}
