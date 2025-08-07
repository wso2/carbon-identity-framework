/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.action.api.model;

import org.wso2.carbon.identity.action.execution.api.model.Organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the response object for user related operations to be populated by user actions.
 */
public class UserActionResponseDTO {

    private final String userId;
    private final char[] password;
    private List<String> roles;
    private List<String> groups;
    private final Map<String, Object> claims;
    private final String userStoreDomain;
    private final Organization residentOrganization;
    private final String sharedUserId;

    public UserActionResponseDTO(UserActionRequestDTO userActionRequestDTO) {

        this.userId = userActionRequestDTO.getUserId();
        this.password = userActionRequestDTO.getPassword();
        this.userStoreDomain = userActionRequestDTO.getUserStoreDomain();
        this.claims = new HashMap<>(userActionRequestDTO.getClaims());
        this.roles = new ArrayList<>(userActionRequestDTO.getRoles());
        this.groups = new ArrayList<>(userActionRequestDTO.getGroups());
        this.residentOrganization = userActionRequestDTO.getResidentOrganization();
        this.sharedUserId = userActionRequestDTO.getSharedUserId();
    }

    public String getUserId() {

        return userId;
    }

    public char[] getPassword() {

        return password;
    }

    public List<String> getRoles() {

        return roles;
    }

    public List<String> getGroups() {

        return groups;
    }

    public Map<String, Object> getClaims() {

        return claims;
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    public Organization getResidentOrganization() {

        return residentOrganization;
    }

    public String getSharedUserId() {

        return sharedUserId;
    }

    public void addRole(String role) {

        this.roles.add(role);
    }

    public void removeRole(String role) {

        this.roles.remove(role);
    }

    public void setRoles(List<String> roles) {

        this.roles = roles;
    }

    public void addGroup(String group) {

        this.groups.add(group);
    }

    public void removeGroup(String group) {

        this.groups.remove(group);
    }

    public void setGroups(List<String> groups) {

        this.groups = groups;
    }

    public void addClaim(String key, String value) {

        this.claims.put(key, value);
    }

    public void addClaim(String key, String[] value) {

        this.claims.put(key, value);
    }

    public void removeClaim(String key) {

        this.claims.remove(key);
    }
}
