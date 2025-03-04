package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;

public class RoleIdCacheEntry extends CacheEntry {

    private String roleId;

    public RoleIdCacheEntry(String roleId) {

        this.roleId = roleId;
    }

    public String getRoleId() {

        return roleId;
    }

    public void setRoleId(String roleId) {

        this.roleId = roleId;
    }
}
