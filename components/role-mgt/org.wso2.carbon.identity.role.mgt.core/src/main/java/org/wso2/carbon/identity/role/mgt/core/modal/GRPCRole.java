package org.wso2.carbon.identity.role.mgt.core.modal;

import java.util.List;

/**
 * Represents the role.
 */
public class GRPCRole {

    String roleName;
    List<String> userList;
    List<String> groupList;
    List<String> permissions;
    String tenantDomain;

    String id;

    public GRPCRole(String roleName, List<String> userList, List<String> groupList, List<String> permissions,
                    String tenantDomain, String id) {

        this.roleName = roleName;
        this.userList = userList;
        this.groupList = groupList;
        this.permissions = permissions;
        this.tenantDomain = tenantDomain;
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<String> getUserList() {
        return userList;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getId() {
        return id;
    }
}
