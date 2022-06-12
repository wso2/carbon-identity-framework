package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.cache.CacheEntry;

import java.util.HashMap;
import java.util.List;

/**
 * Cache entry for AllInboundPropertiesByClientTypeCache.
 */
public class AllInboundPropertiesCacheEntry extends CacheEntry {
    private static final long serialVersionUID = -9187424083900196903L;
    private HashMap<String, List<Property>> properties;

    public AllInboundPropertiesCacheEntry(HashMap<String, List<Property>> properties) {
        this.properties = properties;
    }

    public HashMap<String, List<Property>> getProperties() {
        return properties;
    }
}
