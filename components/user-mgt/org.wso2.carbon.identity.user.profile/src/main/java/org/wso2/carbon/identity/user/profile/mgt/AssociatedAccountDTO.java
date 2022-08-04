/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.identity.user.profile.mgt;

public class AssociatedAccountDTO {

    private String id;
    private int identityProviderId;
    private String identityProviderName;
    private String username;

    public AssociatedAccountDTO(String identityProviderName, String username) {
        this.identityProviderName = identityProviderName;
        this.username = username;
    }

    public AssociatedAccountDTO(String id, String identityProviderName, String username) {

        this.id = id;
        this.identityProviderName = identityProviderName;
        this.username = username;
    }

    public AssociatedAccountDTO(String id, int identityProviderId, String identityProviderName, String username) {

        this.id = id;
        this.identityProviderId = identityProviderId;
        this.identityProviderName = identityProviderName;
        this.username = username;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getIdentityProviderId() {

        return identityProviderId;
    }
}
