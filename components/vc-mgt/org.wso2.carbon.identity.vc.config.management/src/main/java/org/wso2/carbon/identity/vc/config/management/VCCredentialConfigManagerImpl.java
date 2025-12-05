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

package org.wso2.carbon.identity.vc.config.management;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.VCConfigMgtDAO;
import org.wso2.carbon.identity.vc.config.management.dao.impl.VCConfigMgtDAOImpl;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtServerException;
import org.wso2.carbon.identity.vc.config.management.internal.VCConfigManagementServiceDataHolder;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.APIResourceTypes.VC;
import static org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants.DEFAULT_SIGNING_ALGORITHM;
import static org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants.VC_DIALECT;

/**
 * Implementation of {@link VCCredentialConfigManager}.
 */
public class VCCredentialConfigManagerImpl implements VCCredentialConfigManager {

    private static final Log LOG = LogFactory.getLog(VCCredentialConfigManagerImpl.class);
    private static final VCCredentialConfigManager INSTANCE = new VCCredentialConfigManagerImpl();
    private final VCConfigMgtDAO dao = new VCConfigMgtDAOImpl();

    private VCCredentialConfigManagerImpl() {

    }

    public static VCCredentialConfigManager getInstance() {

        return INSTANCE;
    }

    @Override
    public List<VCCredentialConfiguration> list(String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving VC credential configurations for tenant: %s", tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.list(tenantId);
    }

    @Override
    public VCCredentialConfiguration get(String id, String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving VC credential configuration with id: %s for tenant: %s", id,
                    tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.get(id, tenantId);
    }

    @Override
    public VCCredentialConfiguration getByIdentifier(String identifier, String tenantDomain)
            throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving VC credential configuration with identifier: %s for tenant: %s",
                    identifier, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getByIdentifier(identifier, tenantId);
    }

    @Override
    public VCCredentialConfiguration getByOfferId(String offerId, String tenantDomain)
            throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving VC credential configuration with offer ID: %s for tenant: %s",
                    offerId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getByOfferId(offerId, tenantId);
    }

    @Override
    public VCCredentialConfiguration add(VCCredentialConfiguration configuration, String tenantDomain)
            throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Adding new VC credential configuration for tenant: %s", tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        checkIdentifierExists(configuration, tenantId);
        validateDisplayName(configuration, tenantId);
        validateFormat(configuration);
        configuration.setSigningAlgorithm(DEFAULT_SIGNING_ALGORITHM);
        validateCredentialType(configuration.getType());
        validateExpiry(configuration.getExpiresIn());
        validateScope(configuration.getScope(), tenantDomain);
        validateClaims(configuration.getClaims(), tenantDomain);
        return dao.add(configuration, tenantId);
    }

    @Override
    public VCCredentialConfiguration update(String id, VCCredentialConfiguration configuration,
                                            String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating VC credential configuration with id: %s for tenant: %s", id,
                    tenantDomain));
        }
        if (configuration.getId() != null && !StringUtils.equals(id, configuration.getId())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_ID_MISMATCH.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_ID_MISMATCH.getMessage());
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Validate identifier uniqueness if changed.
        VCCredentialConfiguration existing = dao.get(id, tenantId);
        if (existing == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
        }

        if (!StringUtils.isBlank(configuration.getIdentifier())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Identifier cannot be updated.");
        }

        // Preserve identifier from existing configuration.
        configuration.setIdentifier(existing.getIdentifier());
        validateDisplayName(configuration, tenantId);
        validateFormat(configuration);
        validateCredentialType(configuration.getType());
        validateExpiry(configuration.getExpiresIn());
        validateScope(configuration.getScope(), tenantDomain);
        validateClaims(configuration.getClaims(), tenantDomain);
        return dao.update(id, configuration, tenantId);
    }

    @Override
    public void delete(String id, String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting VC credential configuration with id: %s for tenant: %s", id,
                    tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        dao.delete(id, tenantId);
    }

    /**
     * Check if a configuration with the given identifier already exists.
     *
     * @param configuration VC credential configuration.
     * @param tenantId      Tenant ID.
     * @throws VCConfigMgtException on validation errors.
     */
    private void checkIdentifierExists(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        if (StringUtils.isBlank(configuration.getIdentifier())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Identifier cannot be empty.");
        }
        if (dao.existsByIdentifier(configuration.getIdentifier(), tenantId)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_IDENTIFIER_ALREADY_EXISTS.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_IDENTIFIER_ALREADY_EXISTS.getMessage());
        }
    }

    /**
     * Validate display name.
     *
     * @param configuration VC credential configuration.
     * @param tenantId      Tenant ID.
     * @throws VCConfigMgtException on validation errors.
     */
    private void validateDisplayName(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        if (StringUtils.isBlank(configuration.getDisplayName())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Display name cannot be empty.");
        }
    }

    /**
     * Validate format.
     *
     * @param configuration VC credential configuration.
     * @throws VCConfigMgtClientException on validation errors.
     */
    private void validateFormat(VCCredentialConfiguration configuration) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(configuration.getFormat())) {
            configuration.setFormat(VCConfigManagementConstants.DEFAULT_VC_FORMAT);
        } else {
            // Currently only default format is supported.
            if (!StringUtils.equals(configuration.getFormat(),
                    VCConfigManagementConstants.DEFAULT_VC_FORMAT)) {
                throw new VCConfigMgtClientException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_VC_FORMAT.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_VC_FORMAT.getMessage());
            }
        }
    }

    /**
     * Validate credential type.
     *
     * @param credentialType Credential type.
     * @throws VCConfigMgtClientException on validation errors.
     */
    private void validateCredentialType(String credentialType) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(credentialType)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Credential type cannot be empty.");
        }
    }

    /**
     * Validate expiry.
     *
     * @param expiryInSeconds Expiry in seconds.
     * @throws VCConfigMgtClientException on validation errors.
     */
    private void validateExpiry(Integer expiryInSeconds) throws VCConfigMgtClientException {

        if (expiryInSeconds == null || expiryInSeconds < VCConfigManagementConstants.MIN_EXPIRES_IN_SECONDS) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    String.format("Expiry must be at least %d seconds.",
                            VCConfigManagementConstants.MIN_EXPIRES_IN_SECONDS));
        }
    }

    /**
     * Validate that the scope exists and belongs to a VC-type API resource.
     *
     * @param scope        Scope name to validate.
     * @param tenantDomain Tenant domain.
     * @throws VCConfigMgtException on validation errors.
     */
    private void validateScope(String scope, String tenantDomain) throws VCConfigMgtException {


        if (StringUtils.isBlank(scope)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Scope cannot be empty.");
        }

        APIResourceManager apiResourceManager = VCConfigManagementServiceDataHolder.getInstance()
                .getAPIResourceManager();
        try {
            Scope existingScope = apiResourceManager.getScopeByName(scope, tenantDomain);
            if (existingScope == null) {
                throw new VCConfigMgtClientException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                        "Scope does not exist: " + scope);
            }

            // Verify the scope belongs to a VC resource type
            APIResource apiResource = apiResourceManager.getAPIResourceById(existingScope.getApiID(),
                    tenantDomain);
            if (apiResource == null || !VC.equals(apiResource.getType())) {
                throw new VCConfigMgtClientException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                        "Scope must belong to a VC resource type: " + scope);
            }
        } catch (APIResourceMgtException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                    "Error while validating scope: " + scope, e);
        }
    }

    /**
     * Validate claims.
     *
     * @param claims       List of claim URIs.
     * @param tenantDomain Tenant domain.
     * @throws VCConfigMgtException on validation errors.
     */
    private void validateClaims(List<String> claims, String tenantDomain) throws VCConfigMgtException {

        if (claims != null && !claims.isEmpty()) {
            Set<ExternalClaim> vcClaims;
            try {
                vcClaims = ClaimMetadataHandler.getInstance()
                        .getMappingsFromOtherDialectToCarbon(VC_DIALECT, null, tenantDomain);
            } catch (ClaimMetadataException e) {
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        "Error while validating claims.");
            }

            // Build a map for efficient claim lookup.
            List<String> vcClaimURIs = new ArrayList<>();
            for (ExternalClaim externalClaim : vcClaims) {
                vcClaimURIs.add(externalClaim.getClaimURI());
            }

            for (String claim : claims) {
                if (StringUtils.isBlank(claim) || !vcClaimURIs.contains(claim)) {
                    throw new VCConfigMgtClientException(
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                            "Invalid claim: " + claim);
                }
            }
        }
    }

    @Override
    public VCCredentialConfiguration generateOffer(String configId, String tenantDomain)
            throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Generating or regenerating credential offer for configuration: %s for tenant: %s",
                    configId, tenantDomain));
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Check if configuration exists.
        VCCredentialConfiguration existing = dao.get(configId, tenantId);
        if (existing == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
        }

        // Generate new offer ID (regardless of whether one exists - handles both generation and regeneration).
        String offerId = java.util.UUID.randomUUID().toString();
        dao.updateOfferId(configId, offerId, tenantId);

        return dao.get(configId, tenantId);
    }

    @Override
    public VCCredentialConfiguration revokeOffer(String configId, String tenantDomain)
            throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Revoking credential offer for configuration: %s for tenant: %s",
                    configId, tenantDomain));
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Check if configuration exists.
        VCCredentialConfiguration existing = dao.get(configId, tenantId);
        if (existing == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
        }

        // Check if offer exists.
        if (existing.getOfferId() == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_NOT_FOUND.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_NOT_FOUND.getMessage());
        }

        // Revoke offer by setting offerId to null.
        dao.updateOfferId(configId, null, tenantId);

        return dao.get(configId, tenantId);
    }
}
