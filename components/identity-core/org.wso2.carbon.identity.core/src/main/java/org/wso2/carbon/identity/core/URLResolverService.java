/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core;

import java.util.Map;

/**
 * This interface is defined to resolve a provided URL or a URL context by adding a proxy context paths, web context
 * root, and the tenant domain as specified. The URL would be resolved for the tenant domain by either adding the tenant
 * context to the path or adding a tenant parameter (legacy mode).
 */
public interface URLResolverService {

    /**
     * This method is used to return a URL with a proxy context path, a web context root and the tenant domain (If
     * required) when provided with a URL.
     * @param url URL.
     * @param addProxyContextPath add proxy context path to the URL.
     * @param addWebContextRoot add web context path to the URL.
     * @param properties properties.
     * @return complete URL for the given URL context.
     * @throws URLResolverException if error occurred while constructing the URL.
     */
    String resolveUrl(String url, boolean addProxyContextPath, boolean addWebContextRoot,
                      Map<String, Object> properties) throws URLResolverException;

    /**
     * This method is used to return a URL with a proxy context path, a web context root and the tenant domain (If
     * required) when provided with a URL context.
     * @param urlContext URL context.
     * @param addProxyContextPath add proxy context path to the URL.
     * @param addWebContextRoot add web context path to the URL.
     * @param addTenantParamLegacyMode add tenant parameter to the URL during legacy mode operation.
     * @param properties properties.
     * @return complete URL for the given URL context.
     * @throws URLResolverException if error occurred while constructing the URL.
     */
    String resolveUrlContext(String urlContext, boolean addProxyContextPath, boolean addWebContextRoot,
                             boolean addTenantParamLegacyMode, Map<String, Object> properties)
            throws URLResolverException;
}
