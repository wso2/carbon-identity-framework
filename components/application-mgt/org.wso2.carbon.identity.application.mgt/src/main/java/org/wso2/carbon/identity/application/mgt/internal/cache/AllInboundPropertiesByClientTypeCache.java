package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache to maintain the inbound authentication config properties for all applications by client type.
 */
public class AllInboundPropertiesByClientTypeCache
        extends BaseCache<AllInboundPropertiesCacheKey, AllInboundPropertiesCacheEntry> {

    private static final String ALL_INBOUND_PROPERTIES_CACHE_NAME = "AllInboundProperties.InboundAuthType";

    private static volatile AllInboundPropertiesByClientTypeCache instance;

    private AllInboundPropertiesByClientTypeCache() {
        super(ALL_INBOUND_PROPERTIES_CACHE_NAME);
    }

    public static AllInboundPropertiesByClientTypeCache getInstance() {

        if (instance == null) {
            synchronized (AllInboundPropertiesByClientTypeCache.class) {
                if (instance == null) {
                    instance = new AllInboundPropertiesByClientTypeCache();
                }
            }
        }
        return instance;
    }
}
