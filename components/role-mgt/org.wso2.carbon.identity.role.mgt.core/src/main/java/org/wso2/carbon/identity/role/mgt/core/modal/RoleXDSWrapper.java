package org.wso2.carbon.identity.role.mgt.core.modal;

import java.util.List;

/**
 * Represents the role.
 */
public class RoleXDSWrapper {

    String roleID;
    String newRoleName;
    List<String> newUserIDList;
    List<String> deletedUserIDList;
    List<String> newGroupIDList;
    List<String> deletedGroupIDList;
    List<String> newPermissions;

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public String getNewRoleName() {
        return newRoleName;
    }

    public void setNewRoleName(String newRoleName) {
        this.newRoleName = newRoleName;
    }

    public List<String> getNewUserIDList() {
        return newUserIDList;
    }

    public void setNewUserIDList(List<String> newUserIDList) {
        this.newUserIDList = newUserIDList;
    }

    public List<String> getDeletedUserIDList() {
        return deletedUserIDList;
    }

    public void setDeletedUserIDList(List<String> deletedUserIDList) {
        this.deletedUserIDList = deletedUserIDList;
    }

    public List<String> getNewGroupIDList() {
        return newGroupIDList;
    }

    public void setNewGroupIDList(List<String> newGroupIDList) {
        this.newGroupIDList = newGroupIDList;
    }

    public List<String> getDeletedGroupIDList() {
        return deletedGroupIDList;
    }

    public void setDeletedGroupIDList(List<String> deletedGroupIDList) {
        this.deletedGroupIDList = deletedGroupIDList;
    }

    public List<String> getNewPermissions() {
        return newPermissions;
    }

    public void setNewPermissions(List<String> newPermissions) {
        this.newPermissions = newPermissions;
    }

}
