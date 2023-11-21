package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.utils.CarbonUtils;

public class RoleCacheById extends BaseCache<RoleIdCacheKey, RoleCacheEntry> {

    private static final String CACHE_NAME = "RoleCacheById";
    private static final RoleCacheById INSTANCE =  new RoleCacheById();

    private RoleCacheById() {

        super(CACHE_NAME);
    }

    public static RoleCacheById getInstance() {

        CarbonUtils.checkSecurity();
        return INSTANCE;
    }
}
