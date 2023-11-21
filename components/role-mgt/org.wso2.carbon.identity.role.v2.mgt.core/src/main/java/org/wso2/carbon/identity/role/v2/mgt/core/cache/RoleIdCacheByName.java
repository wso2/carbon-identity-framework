package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

public class RoleIdCacheByName extends BaseCache<RoleNameCacheKey, RoleIdCacheEntry> {

    private static final String CACHE_NAME = "RoleCacheById";
    private static volatile RoleIdCacheByName instance;

    private RoleIdCacheByName() {

        super(CACHE_NAME);
    }

    public static RoleIdCacheByName getInstance() {

        if (instance == null) {
            synchronized (RoleIdCacheByName.class) {
                if (instance == null) {
                    instance = new RoleIdCacheByName();
                }
            }
        }
        return instance;
    }
}
