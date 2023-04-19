/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.dao;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;

import java.util.List;

/**
 * Definition of DAO for Identity Provider.
 */
public interface IdentityProviderDAO {

    /**
     * Returns the Identity provider with given IdP name.
     * May return null if there is no such IdP found for the given name.
     * @param idpName
     * @return
     * @throws IdentityApplicationManagementException
     */
    IdentityProvider getIdentityProvider(String idpName) throws IdentityApplicationManagementException;

    /**
     * Returns all the identity providers available.
     * @return
     * @throws IdentityApplicationManagementException
     */
    List<IdentityProvider> getAllIdentityProviders() throws IdentityApplicationManagementException;

    /**
     * Returns all the local authenticators available on the system.
     * @return
     * @throws IdentityApplicationManagementException
     */
    List<LocalAuthenticatorConfig> getAllLocalAuthenticators() throws IdentityApplicationManagementException;

    /**
     * Get connected applications of a local authenticator.
     *
     * @param authenticatorId   Authenticator ID.
     * @param tenantId          Tenant ID.
     * @param limit             Counting limit.
     * @param offset            Starting index of the count.
     * @return ConnectedAppsResult
     * @throws IdentityApplicationManagementException If an error occurred when retrieving connected applications.
     */
    ConnectedAppsResult getConnectedAppsOfLocalAuthenticator(String authenticatorId, int tenantId, Integer limit,
                                                             Integer offset)
            throws IdentityApplicationManagementException;

    /**
     * Returns all the request path authenticators available in the system.
     * @return
     * @throws IdentityApplicationManagementException
     */
    List<RequestPathAuthenticatorConfig> getAllRequestPathAuthenticators()
            throws IdentityApplicationManagementException;

    /**
     * Returns the default authenticator configured for the IdP.
     *
     * @param idpName
     * @return
     * @throws IdentityApplicationManagementException
     */
    String getDefaultAuthenticator(String idpName) throws IdentityApplicationManagementException;

}
