package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;

public class RoleCacheEntry extends CacheEntry {

    private Role role;

    public RoleCacheEntry(Role role) {

        this.role = role;
    }

    public Role getRole() {

        return role;
    }

    public void setRole(Role role) {

        this.role = role;
    }
}
