package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

public class RoleCacheById extends BaseCache<RoleIdCacheKey, RoleCacheEntry> {

    private static final String CACHE_NAME = "RoleCacheById";
    private static volatile RoleCacheById instance;

    private RoleCacheById() {

        super(CACHE_NAME);
    }

    public static RoleCacheById getInstance() {

        if (instance == null) {
            synchronized (RoleCacheById.class) {
                if (instance == null) {
                    instance = new RoleCacheById();
                }
            }
        }
        return instance;
    }
}
