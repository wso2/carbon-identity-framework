package org.wso2.carbon.identity.claim.mgt.cache;

import org.wso2.carbon.identity.application.common.cache.CacheKey;

public class ClaimCacheKey extends CacheKey {

    private String cacheKeyString;

    public ClaimCacheKey(String cacheKeyString) {
        this.cacheKeyString = cacheKeyString;
    }

    public String getClaimCacheKeyString() {
        return cacheKeyString;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClaimCacheKey)) {
            return false;
        }
        return this.cacheKeyString.equals(((ClaimCacheKey) o).getClaimCacheKeyString());
    }

    @Override
    public int hashCode() {
        return cacheKeyString.hashCode();
    }
}
