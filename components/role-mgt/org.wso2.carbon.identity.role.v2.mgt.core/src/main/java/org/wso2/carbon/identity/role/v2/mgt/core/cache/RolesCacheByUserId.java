package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

public class RolesCacheByUserId extends BaseCache<UserIdCacheKey, RolesCacheEntry> {

    private static final String CACHE_NAME = "RoleIdCacheByName";
    private static volatile RolesCacheByUserId instance;

    private RolesCacheByUserId() {

        super(CACHE_NAME);
    }

    public static RolesCacheByUserId getInstance() {

        if (instance == null) {
            synchronized (RolesCacheByUserId.class) {
                if (instance == null) {
                    instance = new RolesCacheByUserId();
                }
            }
        }
        return instance;
    }
}
