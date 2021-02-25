/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.model.IdpSearchResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdentityProviderManagementService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(IdentityProviderManager.class);
    private static String LOCAL_DEFAULT_CLAIM_DIALECT = "http://wso2.org/claims";

    /**
     * Retrieves resident Identity provider for the logged-in tenant
     *
     * @return <code>IdentityProvider</code>
     * @throws IdentityProviderManagementException Error when getting Resident Identity Provider
     */
    public IdentityProvider getResidentIdP() throws IdentityProviderManagementException {

        String tenantDomain = "";
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdentityProvider residentIdP = IdentityProviderManager.getInstance()
                    .getResidentIdP(tenantDomain);
            return residentIdP;
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting ResidentIdP in tenantDomain :" + tenantDomain, idpException);
            throw idpException;
        }
    }

    /**
     * Updated resident Identity provider for the logged-in tenant
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @throws IdentityProviderManagementException Error when getting Resident Identity Provider
     */
    public void updateResidentIdP(IdentityProvider identityProvider)
            throws IdentityProviderManagementException {

        String tenantDomain = "";
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdentityProviderManager.getInstance().updateResidentIdP(identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while updating ResidentIdP in tenantDomain : " + tenantDomain, idpException);
            throw idpException;
        }
    }

    /**
     * Retrieves registered Identity providers for the logged-in tenant
     *
     * @return Array of <code>IdentityProvider</code>. IdP names, primary IdP and home
     * realm identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    public IdentityProvider[] getAllIdPs() throws IdentityProviderManagementException {

        String tenantDomain = "";
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            List<IdentityProvider> identityProviders = IdentityProviderManager.getInstance().getIdPs(tenantDomain);
            for (int i = 0; i < identityProviders.size(); i++) {
                String providerName = identityProviders.get(i).getIdentityProviderName();
                if (providerName != null && providerName.startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
                    identityProviders.remove(i);
                    i--;
                }
            }
            return identityProviders.toArray(new IdentityProvider[0]);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting IdPs in tenantDomain : " + tenantDomain, idpException);
            throw idpException;
        }
    }

    /**
     * Get the total count of Idps.
     *
     * @return total count of Idps
     * @throws IdentityProviderManagementException Throw exception while get the count of Idps.
     */
    public int getAllIdpCount() throws IdentityProviderManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return IdentityProviderManager.getInstance()
                .getTotalIdPCount(IdPManagementConstants.EMPTY_STRING, tenantDomain);
    }

    /**
     * Retrieves registered Identity providers for the logged-in tenant.
     *
     * @return Array of <code>IdentityProvider</code>. IdP names, primary IdP and home.
     * realm identifiers of each IdP.
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers.
     */
    public IdentityProvider[] getAllPaginatedIdpInfo(int pageNumber) throws IdentityProviderManagementException {

        return getPaginatedIdpInfo(IdPManagementConstants.EMPTY_STRING, pageNumber);
    }

    /**
     * Retrieves Identity providers list of array for the logged-in tenant.
     *
     * @param filter     searching value.
     * @param pageNumber page number.
     * @return Identity providers list of array.
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers.
     */
    public IdentityProvider[] getPaginatedIdpInfo(String filter, int pageNumber)
            throws IdentityProviderManagementException {

        validateRequestedPageNumber(pageNumber);
        Integer limit = IdentityUtil.getDefaultItemsPerPage();
        Integer offset = getIdpPageOffset(pageNumber, limit);
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        IdpSearchResult idpSearchResult = IdentityProviderManager.getInstance()
                .getIdPs(limit, offset, filter, IdPManagementConstants.DEFAULT_SORT_ORDER,
                        IdPManagementConstants.DEFAULT_SORT_BY, tenantDomain, new ArrayList<>());
        return idpSearchResult.getIdPs().toArray(new IdentityProvider[0]);
    }

    /**
     * Retrieves Identity providers list of array for the logged-in tenant.
     *
     * @param filter searching value.
     * @return Identity providers list of array.
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers.
     */
    public int getFilteredIdpCount(String filter) throws IdentityProviderManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return IdentityProviderManager.getInstance().getTotalIdPCount(filter, tenantDomain);
    }

    /**
     * Validates whether the requested page limit and offset for pagination.
     *
     * @param pageNumber page number that needed.
     * @param limit      number of Idp display per page.
     * @return offset value.
     */
    private int getIdpPageOffset(int pageNumber, int limit) {

        int offset = 0;
        if (pageNumber > 1) {
            offset = (pageNumber - 1) * limit;
        }
        return offset;
    }

    /**
     * Validates whether the requested page number for pagination is not zero or negative.
     *
     * @param pageNumber Page number.
     */
    private void validateRequestedPageNumber(int pageNumber) throws IdentityProviderManagementException {

        if (pageNumber < 1) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_IDP,
                    "Invalid page number requested. The page number should be a value greater than 0");
        }
    }

    /**
     * Retrieves registered Identity providers for the logged-in tenant by Identity Provider Name
     *
     * @return Array of <code>IdentityProvider</code>. IdP names, primary IdP and home
     * realm identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    public IdentityProvider[] getAllIdPsSearch(String filter) throws IdentityProviderManagementException {

        String tenantDomain = "";
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext()
                    .getTenantDomain();
            List<IdentityProvider> identityProviders = IdentityProviderManager
                    .getInstance().getIdPsSearch(tenantDomain, filter);
            for (int i = 0; i < identityProviders.size(); i++) {
                String providerName = identityProviders.get(i).getIdentityProviderName();
                if (providerName != null && providerName.startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
                    identityProviders.remove(i);
                    i--;
                }
            }
            return identityProviders.toArray(new IdentityProvider[0]);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting IdPs in tenantDomain : " + tenantDomain, idpException);
            throw idpException;
        }
    }

    /**
     * Retrieves Enabled registered Identity providers for the logged-in tenant
     *
     * @return Array of <code>IdentityProvider</code>. IdP names, primary IdP and home
     * realm identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    public IdentityProvider[] getEnabledAllIdPs() throws IdentityProviderManagementException {

        String tenantDomain = "";
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            List<IdentityProvider> identityProviders = IdentityProviderManager.getInstance().getEnabledIdPs
                    (tenantDomain);
            return identityProviders.toArray(new IdentityProvider[0]);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting enabled registered Identity providers in tenantDomain : " + tenantDomain,
                    idpException);
            throw idpException;
        }
    }

    /**
     * Retrieves Identity provider information for the logged-in tenant by Identity Provider name
     *
     * @param idPName Unique name of the Identity provider of whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByName(String idPName) throws IdentityProviderManagementException {

        try {
            if (StringUtils.isBlank(idPName)) {
                throw new IllegalArgumentException("Provided IdP name is empty");
            }
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdentityProvider identityProvider =
                    IdentityProviderManager.getInstance().getIdPByName(idPName, tenantDomain, true);
            IdPManagementUtil.removeOriginalPasswords(identityProvider);
            return identityProvider;
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting Idp with name " + idPName, idpException);
            throw idpException;
        }
    }

    /**
     * Adds an Identity Provider to the logged-in tenant
     *
     * @param identityProvider <code>IdentityProvider</code> new Identity Provider information
     * @throws IdentityProviderManagementException Error when adding Identity Provider
     */
    public void addIdP(IdentityProvider identityProvider) throws IdentityProviderManagementException {
        // The following check is applicable only for the IdPs added from UI/Service call and should not be
        // applicable for IdPs added from file. hence the check is moved from listener to the service
        if (identityProvider != null && identityProvider.getIdentityProviderName() != null &&
                identityProvider.getIdentityProviderName().startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
            throw new IdentityProviderManagementException("Identity provider name cannot have " +
                    IdPManagementConstants.SHARED_IDP_PREFIX + " as prefix.");
        }
        String tenantDomain = "";
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdentityProviderManager.getInstance().addIdP(identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while adding Identity provider in tenantDomain : " + tenantDomain, idpException);
            throw idpException;
        }
    }

    /**
     * Deletes an Identity Provider from the logged-in tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     */
    public void deleteIdP(String idPName) throws IdentityProviderManagementException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdentityProviderManager.getInstance().deleteIdP(idPName, tenantDomain);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while deleting IdP with name " + idPName, idpException);
            throw idpException;
        }
    }

    /**
     * Forcefully deletes an Identity Provider from the logged-in tenant. This will remove all associations this
     * Identity Provider had with any Service Provider within the tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     */
    public void forceDeleteIdP(String idPName) throws IdentityProviderManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            IdentityProviderManager.getInstance().forceDeleteIdp(idPName, tenantDomain);
        } catch (IdentityProviderManagementException idpException) {
            log.error(String.format("Error while force deleting IdP:%s in tenantDomain:%s", idPName, tenantDomain),
                    idpException);
            throw idpException;
        }
    }

    /**
     * @return
     * @throws IdentityProviderManagementException
     */
    public String[] getAllLocalClaimUris() throws IdentityProviderManagementException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            List<LocalClaim> localClaims = IdpMgtServiceComponentHolder.getInstance()
                    .getClaimMetadataManagementService().getLocalClaims(tenantDomain);

            String[] allLocalClaimUris = getLocalClaimsArray(localClaims);

            if (ArrayUtils.isNotEmpty(allLocalClaimUris)) {
                Arrays.sort(allLocalClaimUris);
            }
            return allLocalClaimUris;
        } catch (ClaimMetadataException e) {
            String message = "Error while reading local claims";
            log.error(message, e);
            throw new IdentityProviderManagementException(message, e);
        }
    }

    /**
     * Updates a given Identity Provider's information in the logged-in tenant
     *
     * @param oldIdPName       existing Identity Provider name
     * @param identityProvider <code>IdentityProvider</code> new Identity Provider information
     * @throws IdentityProviderManagementException Error when updating Identity Provider
     */
    public void updateIdP(String oldIdPName, IdentityProvider identityProvider) throws
            IdentityProviderManagementException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdPManagementUtil.removeRandomPasswords(identityProvider, true);
            IdentityProviderManager.getInstance().updateIdP(oldIdPName, identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while updating IdP with name " + oldIdPName, idpException);
            throw idpException;
        }
    }

    /**
     * Get the authenticators registered in the system.
     *
     * @return <code>FederatedAuthenticatorConfig</code> array.
     * @throws IdentityProviderManagementException Error when getting authenticators registered in the system
     */
    public FederatedAuthenticatorConfig[] getAllFederatedAuthenticators() throws IdentityProviderManagementException {

        try {
            return IdentityProviderManager.getInstance().getAllFederatedAuthenticators();
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting Federated Authenticators", idpException);
            throw idpException;
        }
    }

    public ProvisioningConnectorConfig[] getAllProvisioningConnectors() throws IdentityProviderManagementException {

        try {
            return IdentityProviderManager.getInstance().getAllProvisioningConnectors();
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while getting provisioning connectors", idpException);
            throw idpException;
        }
    }

    public String getResidentIDPMetadata() throws IdentityProviderManagementException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            return IdentityProviderManager.getInstance().getResidentIDPMetadata(tenantDomain);
        } catch (IdentityProviderManagementException idpException) {
            log.error("Error while retrieving IDP metadata", idpException);
            throw idpException;
        }
    }

    private String[] getLocalClaimsArray(List<LocalClaim> localClaims) {

        // Using Java 8 streams to do the mapping will result in breaking at the axis level thus using the followng
        // approach.
        ArrayList<String> localClaimsArray = new ArrayList<String>();
        for (LocalClaim localClaim : localClaims) {
            localClaimsArray.add(localClaim.getClaimURI());
        }
        return localClaimsArray.toArray(new String[0]);
    }
}
