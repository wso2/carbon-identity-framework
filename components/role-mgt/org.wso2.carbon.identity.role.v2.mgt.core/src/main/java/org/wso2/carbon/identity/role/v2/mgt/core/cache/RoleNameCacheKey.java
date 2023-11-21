package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

public class RoleNameCacheKey extends CacheKey {

    private final String roleName;
    private final String audience;
    private final String audienceId;
    private final String tenantDomain;

    public RoleNameCacheKey(String roleName, String audience, String audienceId, String tenantDomain) {

        this.roleName = roleName;
        this.audience = audience;
        this.audienceId = audienceId;
        this.tenantDomain = tenantDomain;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof RoleNameCacheKey)) {
            return false;
        }

        RoleNameCacheKey that = (RoleNameCacheKey) o;

        if (roleName == null || audience == null || audienceId == null || tenantDomain == null) {
            return false;
        }
        if (!roleName.equals(that.roleName)) {
            return false;
        }
        if (!audience.equals(that.audience)) {
            return false;
        }
        if (!audienceId.equals(that.audienceId)) {
            return false;
        }
        return tenantDomain.equals(that.tenantDomain);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + roleName.hashCode();
        result = 31 * result + audience.hashCode();
        result = 31 * result + audienceId.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }

    @Override
    public String toString() {

        return roleName + " : " + audience + " : " + audienceId + " : " + tenantDomain;
    }
}
