/*
 * Copyright (c) 2007 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.common;

/**
 * Problem this class solves : Reduce the number of WS calls and amount of data
 * transfered between FE and BE.
 * <p/>
 * This is a classic sample of DTOs. An instance of this class can represent
 * either an user item or role item. This is used to represent items in lists of
 * users and roles sent to the Carbon FE. It contains information to indicate
 * whether the representing item is editable or selected within the given
 * context.
 * <p/>
 * Here are the use cases. Usecase 1 : When listing roles we should not show the
 * editable link if it is a external role.
 * <p/>
 * Before FlaggedName 1) Send the complete role list (hybrid + external) to FE
 * 2) Send the role list that are external to FE
 * <p/>
 * When listing roles in FE go through both lists, and do not show edit links
 * for external roles. This logic in JSPs could not be reused. Idea was to cut
 * down the number of calls. It was a feedback given in a review.
 * <p/>
 * After FlaggedNames 1) Send the list of FlaggedNames
 * <p/>
 * Usecase 2 : When user clicks on edit users of a Role
 * <p/>
 * Before FlaggedName 1) Send the list of users that are already in the role so
 * that I can show the ticks in the GUI 2) Send the complete requested user list
 * to FE (selected by *) so I can show them unchecked
 * <p/>
 * After FlaggedName 1)Cut down the nubmer of calls going back and forth
 */
public class FlaggedName {

	private String itemName;
	private String itemDisplayName;
	private boolean isSelected;
	private boolean isEditable;
	private String roleType;
	private String domainName;
	private boolean readOnly;
	private String dn;
    private boolean isShared;

	public String getItemName() {
		return itemName;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getItemDisplayName() {
		return itemDisplayName;
	}

	public void setItemDisplayName(String itemDisplayName) {
		this.itemDisplayName = itemDisplayName;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }
}
