package org.wso2.carbon.identity.claim.mgt.cache;

import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;
import org.wso2.carbon.identity.application.common.cache.CacheKey;


public class ClaimCache extends BaseCache<ClaimCacheKey, CacheEntry> {

    private static final String Claim_CACHE_NAME = "ClaimCache";

    private static final ClaimCache instance = new ClaimCache(Claim_CACHE_NAME);

    private ClaimCache(String cacheName) {
        super(cacheName);
    }

    public static ClaimCache getInstance() {
        CarbonUtils.checkSecurity();
        return instance;
    }

    @Override
    public void addToCache(ClaimCacheKey key, CacheEntry entry) {
        super.addToCache(key, entry);
    }

    @Override
    public CacheEntry getValueFromCache(ClaimCacheKey key) {
        return super.getValueFromCache(key);
    }

    @Override
    public void clearCacheEntry(ClaimCacheKey key) {
        super.clearCacheEntry(key);
    }
}
