/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.role.mgt.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RoleBean {

	private String domain = "";
	private String roleName = "";
	private String sharedRole = "";
	private String[] roleUsers = new String[0];
	private String[] selectedPermissions = new String[0];
	private String[] shownUsers = new String[0];

	private String storeType = "";
	private String roleType = "";

	public String getStoreType() {
		return storeType;
	}

	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	public String getRoleName() {
        if (!roleName.contains(UserAdminUIConstants.DOMAIN_SEPARATOR) && domain != null && domain.trim().length() > 0) {
            return domain + UserAdminUIConstants.DOMAIN_SEPARATOR + roleName;
        }

        return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getSharedRole() {
		return sharedRole;
	}

	public void setSharedRole(String sharedRole) {
		this.sharedRole = sharedRole;
	}

	public String[] getRoleUsers() {
		return Arrays.copyOf(roleUsers, roleUsers.length);
	}

	public void setRoleUsers(String[] selectedUsers) {
		this.roleUsers = Arrays.copyOf(selectedUsers, selectedUsers.length);
	}

	public String[] getSelectedPermissions() {
		return Arrays.copyOf(selectedPermissions, selectedPermissions.length);
	}

	public void setSelectedPermissions(String[] selectedPermissions) {
		this.selectedPermissions = Arrays.copyOf(selectedPermissions, selectedPermissions.length);
	}

	public String[] getShownUsers() {
		return Arrays.copyOf(shownUsers, shownUsers.length);
	}

	public void setShownUsers(String[] shownUsers) {
		this.shownUsers = Arrays.copyOf(shownUsers, shownUsers.length);
	}

	public void cleanup() {

		if (!UserAdminUIConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
			domain = "";
		}
		roleName = "";
		sharedRole = "";
		roleUsers = new String[0];
		selectedPermissions = new String[0];
		shownUsers = new String[0];
		storeType = "";
		roleType = "";
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		if (!UserAdminUIConstants.PRIMARY_DOMAIN_NAME_NOT_DEFINED.equalsIgnoreCase(domain)) {
			this.domain = domain;
		}
	}

	public void addRoleUsers(Map<String, Boolean> checkedUsersMap) {

		if (checkedUsersMap == null) {
			return;
		}
		List<String> roleUsersList = new ArrayList<String>();

		for (Map.Entry<String, Boolean> entry : checkedUsersMap.entrySet()) {
			if (entry.getValue()) {
				roleUsersList.add(entry.getKey());
			}
		}

		for (String role : roleUsers) {
			if (!roleUsersList.contains(role)) {
				roleUsersList.add(role);
			}
		}
		roleUsers = roleUsersList.toArray(new String[roleUsersList.size()]);
	}
}
