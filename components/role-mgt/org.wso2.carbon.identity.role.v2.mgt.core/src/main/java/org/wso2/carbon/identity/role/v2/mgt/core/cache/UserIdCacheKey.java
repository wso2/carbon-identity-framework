package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

public class UserIdCacheKey extends CacheKey {

    private final String userId;

    public UserIdCacheKey(String userId) {

        this.userId = userId;
    }

    public String getUserId() {

        return userId;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof UserIdCacheKey)) {
            return false;
        }
        return userId.equals(((UserIdCacheKey) o).getUserId());
    }

    @Override
    public int hashCode() {

        return userId.hashCode();
    }
}
