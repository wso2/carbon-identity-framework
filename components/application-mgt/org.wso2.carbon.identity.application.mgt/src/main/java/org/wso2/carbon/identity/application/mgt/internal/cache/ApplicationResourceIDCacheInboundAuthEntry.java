/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Cache entry for Service provider when authenticated with client ID, etc. .
 */
public class ApplicationResourceIDCacheInboundAuthEntry extends CacheEntry {

    private static final long serialVersionUID = 1551359845008531441L;
    private String resourceId;
    private String tenantDomain;

    public ApplicationResourceIDCacheInboundAuthEntry(String resourceId, String tenantDomain) {

        this.resourceId = resourceId;
        this.tenantDomain = tenantDomain;
    }

    public String getResourceId() {

        return resourceId;
    }

    public void setResourceId(String resourceId) {

        this.resourceId = resourceId;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }
}
