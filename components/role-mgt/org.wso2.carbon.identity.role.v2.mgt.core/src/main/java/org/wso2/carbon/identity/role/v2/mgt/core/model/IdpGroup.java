/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.v2.mgt.core.model;

import java.util.Objects;

/**
 * Represents the Idp Group.
 */
public class IdpGroup {

    private String groupId;
    private String groupName;
    private String idpId;
    private String idpName;

    /**
     * Constructs an IdP Group with the specified group ID.
     *
     * @param groupId the ID of the IdP Group.
     */
    public IdpGroup(String groupId) {

        this.groupId = groupId;
    }

    /**
     * Constructs an IdP Group with the specified group ID and IdP ID.
     *
     * @param groupId the ID of the IdP Group.
     * @param idpId the ID of the Identity Provider.
     */
    public IdpGroup(String groupId, String idpId) {

        this.groupId = groupId;
        this.idpId = idpId;
    }

    /**
     * Gets the ID of the IdP Group.
     *
     * @return the ID of the IdP Group.
     */
    public String getGroupId() {

        return groupId;
    }

    /**
     * Sets the ID for the IdP Group.
     *
     * @param groupId the ID to set for the IdP Group.
     */
    public void setGroupId(String groupId) {

        this.groupId = groupId;
    }

    /**
     * Gets the name of the IdP Group.
     *
     * @return the name of the IdP Group.
     */
    public String getGroupName() {

        return groupName;
    }

    /**
     * Sets the name for the IdP Group.
     *
     * @param groupName the name to set for the IdP Group.
     */
    public void setGroupName(String groupName) {

        this.groupName = groupName;
    }

    /**
     * Gets the ID of the Identity Provider associated with the IdP Group.
     *
     * @return the ID of the Identity Provider.
     */
    public String getIdpId() {

        return idpId;
    }

    /**
     * Sets the ID for the Identity Provider associated with the IdP Group.
     *
     * @param idpId the ID to set for the Identity Provider.
     */
    public void setIdpId(String idpId) {

        this.idpId = idpId;
    }

    /**
     * Gets the name of the Identity Provider associated with the IdP Group.
     *
     * @return the name of the Identity Provider.
     */
    public String getIdpName() {

        return idpName;
    }

    /**
     * Sets the name for the Identity Provider associated with the IdP Group.
     *
     * @param idpName the name to set for the Identity Provider.
     */
    public void setIdpName(String idpName) {

        this.idpName = idpName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdpGroup that = (IdpGroup) o;
        return Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(groupId);
    }
}
