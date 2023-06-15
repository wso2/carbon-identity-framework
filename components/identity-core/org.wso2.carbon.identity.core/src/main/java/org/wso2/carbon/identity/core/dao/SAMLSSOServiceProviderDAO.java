/*
 * Copyright (c) (2005-2023), WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.dao;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;

public interface SAMLSSOServiceProviderDAO {

    /**
     * Add the service provider information to the registry.
     *
     * @param serviceProviderDO Service provider information object.
     * @return True if addition successful.
     * @throws IdentityException Error while persisting to the registry.
     */
    boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException;

    /**
     * Update the service provider if it exists.
     *
     * @param serviceProviderDO     Service provider to be updated.
     * @param currentIssuer         Issuer of the service provider before the update.
     * @return True if the update is successful.
     * @throws IdentityException If an error occurs while updating the service provider.
     */
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer)
            throws IdentityException;

    /**
     * Get all the service providers.
     *
     * @return Array of service providers.
     * @throws IdentityException Error occurred while retrieving the service providers from registry.
     */
    SAMLSSOServiceProviderDO[] getServiceProviders() throws IdentityException;

    /**
     * Remove the service provider with the given name.
     *
     * @return True if deletion success.
     * @param issuer Name of the SAML issuer.
     * @throws IdentityException Error occurred while removing the SAML service provider from registry.
     */
    boolean removeServiceProvider(String issuer) throws IdentityException;

    /**
     * Get the service provider.
     *
     * @param issuer Name of the SAML issuer.
     * @return Service provider information object.
     * @throws IdentityException Error occurred while retrieving the SAML service provider from registry.
     */
    SAMLSSOServiceProviderDO getServiceProvider(String issuer) throws IdentityException;

    /**
     * Check whether the service provider exists.
     *
     * @param issuer Name of the SAML issuer.
     * @return True if service provider exists.
     * @throws IdentityException Error occurred while checking the existence of the SAML service provider.
     */
    boolean isServiceProviderExists(String issuer) throws IdentityException;

    /**
     * Upload service Provider using metadata file.
     *
     * @param serviceProviderDO Service provider information object.
     * @return True if upload success.
     * @throws IdentityException Error occurred while adding the information to registry.
     */
    SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws
            IdentityException;
}
