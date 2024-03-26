/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.idp.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class IdPMgtValidationListener extends AbstractIdentityProviderMgtListener {

    private static final Log log = LogFactory.getLog(IdPMgtValidationListener.class);

    @Override
    public int getDefaultOrderId() {
        return 30;
    }

    @Override
    public boolean doPreDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        if (StringUtils.isEmpty(idPName)) {
            throw new IllegalArgumentException("Invalid argument: Identity Provider Name value is empty");
        }

        String loggedInTenant = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
            if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenantDomain)) {
                throw new IdentityProviderManagementException("Cannot delete Resident Identity Provider of Super " +
                        "Tenant");
            } else if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(loggedInTenant)) {
                throw new IdentityProviderManagementException("Tenant user of " + loggedInTenant + " cannot delete " +
                        "Resident Identity Provider of tenant " + tenantDomain);
            } else {
                log.warn("Deleting Resident Identity Provider for tenant " + tenantDomain);
            }
        }

        return true;
    }

    /**
     * Pre delete of IDP.
     *
     * @param tenantDomain Tenant domain to delete IdPs
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public boolean doPreDeleteIdPs(String tenantDomain) throws IdentityProviderManagementException {
        
        return super.doPreDeleteIdPs(tenantDomain);
    }

    public boolean doPreAddIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        if (identityProvider == null) {
            throw new IllegalArgumentException("Identity provider cannot be null when adding an IdP");
        } else if (StringUtils.isEmpty(identityProvider.getIdentityProviderName())) {
            throw new IllegalArgumentException("Invalid argument: Identity Provider Name value is empty");
        }
        return true;
    }

    public boolean doPreUpdateResidentIdP(IdentityProvider identityProvider,
                                          String tenantDomain)
            throws IdentityProviderManagementException {

        if (identityProvider == null) {
            throw new IllegalArgumentException("Identity provider is null");
        }
        if (StringUtils.isEmpty(identityProvider.getHomeRealmId())) {
            String msg = "Invalid argument: Resident Identity Provider Home Realm Identifier value is empty";
            throw new IllegalArgumentException(msg);
        }
        return true;
    }

    public boolean doPreUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (identityProvider == null) {
            throw new IllegalArgumentException("Invalid argument: 'newIdentityProvider' is NULL'");
        }
        if (StringUtils.isEmpty(oldIdPName)) {
            throw new IllegalArgumentException("The IdP name which need to be updated is empty");
        }

        if (StringUtils.isEmpty(identityProvider.getIdentityProviderName())) {
            String msg = "Invalid argument: The new value of the identity provider name is empty.";
            throw new IdentityProviderManagementException(msg);
        }

        //Updating a non-shared IdP's name to have shared prefix is not allowed
        if (!oldIdPName.startsWith(IdPManagementConstants.SHARED_IDP_PREFIX) &&
                identityProvider.getIdentityProviderName() != null && identityProvider
                .getIdentityProviderName().startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
            throw new IdentityProviderManagementException("Cannot update Idp name to have '" +
                    IdPManagementConstants.SHARED_IDP_PREFIX + "' as a prefix (previous name:" + oldIdPName + ", " +
                    "New name: " + identityProvider.getIdentityProviderName() + ")");
        }
        return true;
    }

    public boolean doPreAddResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        if (StringUtils.isEmpty(identityProvider.getHomeRealmId())) {
            String msg = "Invalid argument: Resident Identity Provider Home Realm Identifier value is empty";
            throw new IdentityProviderManagementException(msg);
        }
        return true;
    }

}
