/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache to maintain the inbound auth key - application resource id.
 */
public class ApplicationResourceIDByInboundAuthCache extends
        BaseCache<ApplicationResourceIDCacheInboundAuthKey, ApplicationResourceIDCacheInboundAuthEntry> {

    public static final String SP_CACHE_NAME = "ApplicationResourceIDCache.InboundAuth";

    private static volatile ApplicationResourceIDByInboundAuthCache instance;

    private ApplicationResourceIDByInboundAuthCache() {

        super(SP_CACHE_NAME);
    }

    public static ApplicationResourceIDByInboundAuthCache getInstance() {

        if (instance == null) {
            synchronized (ApplicationResourceIDByInboundAuthCache.class) {
                if (instance == null) {
                    instance = new ApplicationResourceIDByInboundAuthCache();
                }
            }
        }
        return instance;
    }
}
