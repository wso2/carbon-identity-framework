/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.core;

import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;

/**
 * URL Builder service interface.
 */
public interface ServiceURLBuilder {

    static ServiceURLBuilder create() {

        if (IdentityCoreServiceComponent.getServiceURLBuilderFactory() != null) {
            return IdentityCoreServiceComponent.getServiceURLBuilderFactory().createServiceURLBuilder();
        } else {
            return new DefaultServiceURLBuilder();
        }
    }

    /**
     * Returns {@link ServiceURLBuilder} appended the URL path.
     *
     * @param paths Context paths. Can provide multiple context paths with a comma separated string.
     * @return {@link ServiceURLBuilder}.
     */
    ServiceURLBuilder addPath(String ... paths);

    /**
     * Returns {@link ServiceURLBuilder} appended with other parameters. Such parameters should be
     * entered as <k,v> pairs.
     *
     * @param key   Key.
     * @param value Value.
     * @return {@link ServiceURLBuilder}.
     */
    ServiceURLBuilder addParameter(String key, String value);

    /**
     * Returns {@link ServiceURLBuilder} appended with a fragment.
     *
     * @param fragment Fragment.
     * @return {@link ServiceURLBuilder}.
     */
    ServiceURLBuilder setFragment(String fragment);

    /**
     * Returns {@link ServiceURLBuilder} appended with parameters. Such parameters should be
     * entered as <k,v> pairs. These parameters will get appended with an "&".
     *
     * @param key   Key.
     * @param value Value.
     * @return {@link ServiceURLBuilder}.
     */
    ServiceURLBuilder addFragmentParameter(String key, String value);


    /**
     * Returns {@link ServiceURLBuilder} with tenant domain set for the context.
     *
     * @param tenantDomain Tenant domain
     * @return {@link ServiceURLBuilder}.
     */
    default ServiceURLBuilder setTenant(String tenantDomain) {

        return this;
    }

    /**
     * This is an overload of method setTenant(String tenantDomain). This method has an additional param to mandate
     * tenanted path appending for URLs.
     *
     * @param tenantDomain          Tenant domain.
     * @param mandateTenantedPath   Mandate tenanted path appending for the URL.
     * @return  {@link ServiceURLBuilder}
     */
    default ServiceURLBuilder setTenant(String tenantDomain, boolean mandateTenantedPath) {

        return this;
    }

    /**
     * Returns a ServiceURL with the protocol, hostname, port, proxy context path, a web context
     * root and the tenant domain (appended if required).
     *
     * @return {@link ServiceURL}.
     * @throws URLBuilderException If error occurred while constructing the URL.
     */
    ServiceURL build() throws URLBuilderException;
}
