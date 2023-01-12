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

    private static final long serialVersionUID = 5197091237662341491L;
    private String applicationCacheInboundAuthKey;
    private String applicationCacheInboundAuthType;

    public ApplicationResourceIDCacheInboundAuthKey(String applicationCacheInboundAuthKey, String
            applicationCacheInboundAuthType) {

        this.applicationCacheInboundAuthKey = applicationCacheInboundAuthKey;
        this.applicationCacheInboundAuthType = applicationCacheInboundAuthType;
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

        if (!applicationCacheInboundAuthKey.equals(that.applicationCacheInboundAuthKey)) {
            return false;
        }
        return applicationCacheInboundAuthType.equals(that.applicationCacheInboundAuthType);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + applicationCacheInboundAuthKey.hashCode();
        result = 31 * result + applicationCacheInboundAuthType.hashCode();
        return result;
    }
}
