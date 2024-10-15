package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;

import java.util.List;

public class RolesCacheEntry extends CacheEntry {

    private List<RoleBasicInfo> roles;

    public RolesCacheEntry(List<RoleBasicInfo> roles) {

        this.roles = roles;
    }

    public List<RoleBasicInfo> getRoles() {

        return roles;
    }

    public void setRoles(List<RoleBasicInfo> roles) {

        this.roles = roles;
    }
}
