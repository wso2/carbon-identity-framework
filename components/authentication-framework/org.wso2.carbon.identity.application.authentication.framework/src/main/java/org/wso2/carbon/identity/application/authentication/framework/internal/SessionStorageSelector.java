/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.internal;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Locale;

/**
 * Selects the session store implementation to use, based on configuration.
 * <p>
 * All the pluggable session stores are selected by the same {@value #STORE_IMPL_TYPE_PROPERTY} property, so
 * that a deployment keeps its session state on a single backend. A configured non-default store must be
 * registered as an OSGi service, and callers fail rather than falling back to the default store, since that
 * would split the session data between stores.
 */
public final class SessionStorageSelector {

    public static final String STORE_IMPL_TYPE_PROPERTY = "SessionStoreImplType";
    public static final String DEFAULT_STORE_NAME = "jdbc";

    private SessionStorageSelector() {

    }

    /**
     * Normalizes a store name, so that registration and lookup are case-insensitive.
     *
     * @param storeName Name of the store.
     * @return the normalized name, or {@code null} if the given name is {@code null}.
     */
    public static String normalizeStoreName(String storeName) {

        return storeName == null ? null : storeName.trim().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns the configured store name.
     *
     * @return the configured store name, or {@code null} if none is configured.
     */
    public static String getConfiguredStoreName() {

        String configured = IdentityUtil.getProperty(STORE_IMPL_TYPE_PROPERTY);
        return StringUtils.isBlank(configured) ? null : configured.trim();
    }

    /**
     * Checks whether the default store is the selected one.
     *
     * @param configuredStoreName Configured store name.
     * @return true if nothing is configured or the default store is configured.
     */
    public static boolean isDefaultStoreConfigured(String configuredStoreName) {

        return configuredStoreName == null || DEFAULT_STORE_NAME.equalsIgnoreCase(configuredStoreName);
    }

}
