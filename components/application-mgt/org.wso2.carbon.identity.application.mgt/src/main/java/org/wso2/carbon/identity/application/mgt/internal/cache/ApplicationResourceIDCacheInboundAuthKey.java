/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;


/**
 * Cache key used to access application based on authenticated application information for the request.
 */
public class ApplicationResourceIDCacheInboundAuthKey extends CacheKey {

    private static final long serialVersionUID = -2977524029670977142L;
    private String serviceProvideCacheInboundAuthKey;
    private String serviceProvideCacheInboundAuthType;

    public ApplicationResourceIDCacheInboundAuthKey(String serviceProvideCacheInboundAuthKey, String
            serviceProvideCacheInboundAuthType) {

        this.serviceProvideCacheInboundAuthKey = serviceProvideCacheInboundAuthKey;
        this.serviceProvideCacheInboundAuthType = serviceProvideCacheInboundAuthType;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ApplicationResourceIDCacheInboundAuthKey that = (ApplicationResourceIDCacheInboundAuthKey) o;

        if (!serviceProvideCacheInboundAuthKey.equals(that.serviceProvideCacheInboundAuthKey)) {
            return false;
        }
        return serviceProvideCacheInboundAuthType.equals(that.serviceProvideCacheInboundAuthType);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + serviceProvideCacheInboundAuthKey.hashCode();
        result = 31 * result + serviceProvideCacheInboundAuthType.hashCode();
        return result;
    }
}
