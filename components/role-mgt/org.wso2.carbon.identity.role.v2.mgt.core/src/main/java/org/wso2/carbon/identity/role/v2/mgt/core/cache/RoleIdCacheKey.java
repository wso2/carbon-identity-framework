package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

public class RoleIdCacheKey extends CacheKey {

    private final String roleId;

    public RoleIdCacheKey(String roleId) {

        this.roleId = roleId;
    }

    public String getRoleId() {

        return roleId;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof RoleIdCacheKey)) {
            return false;
        }
        return roleId.equals(((RoleIdCacheKey) o).getRoleId());
    }

    @Override
    public int hashCode() {

        return roleId.hashCode();
    }
}
