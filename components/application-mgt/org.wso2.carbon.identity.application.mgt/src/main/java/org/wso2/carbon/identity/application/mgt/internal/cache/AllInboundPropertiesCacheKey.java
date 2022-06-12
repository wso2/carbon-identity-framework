package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for AllInboundPropertiesByClientTypeCache.
 */
public class AllInboundPropertiesCacheKey extends CacheKey {
    private static final long serialVersionUID = 3858842289779509622L;
    private String clientType;

    public AllInboundPropertiesCacheKey(String clientType) {
        this.clientType = clientType;
    }
}
