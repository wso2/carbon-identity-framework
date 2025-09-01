/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.webhook.management.api.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

import java.util.Objects;

/**
 * Cache key for active webhooks by profile, version, channel, and tenant.
 */
public class ActiveWebhooksCacheKey extends CacheKey {

    private static final long serialVersionUID = 202506120153L;
    private final String eventProfileName;
    private final String eventProfileVersion;
    private final String channelUri;
    private final int tenantId;

    /**
     * Constructor.
     *
     * @param eventProfileName    Event profile name.
     * @param eventProfileVersion Event profile version.
     * @param channelUri          Channel URI.
     * @param tenantId            Tenant ID.
     */
    public ActiveWebhooksCacheKey(String eventProfileName, String eventProfileVersion,
                                  String channelUri, int tenantId) {

        this.eventProfileName = eventProfileName;
        this.eventProfileVersion = eventProfileVersion;
        this.channelUri = channelUri;
        this.tenantId = tenantId;
    }

    public String getEventProfileName() {

        return eventProfileName;
    }

    public String getEventProfileVersion() {

        return eventProfileVersion;
    }

    public String getChannelUri() {

        return channelUri;
    }

    public int getTenantId() {

        return tenantId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof ActiveWebhooksCacheKey)) {
            return false;
        }
        ActiveWebhooksCacheKey that = (ActiveWebhooksCacheKey) o;
        return tenantId == that.tenantId &&
                Objects.equals(eventProfileName, that.eventProfileName) &&
                Objects.equals(eventProfileVersion, that.eventProfileVersion) &&
                Objects.equals(channelUri, that.channelUri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }
}
