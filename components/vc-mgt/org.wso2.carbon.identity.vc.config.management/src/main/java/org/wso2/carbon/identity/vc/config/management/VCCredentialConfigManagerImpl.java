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

    private static final VCCredentialConfigManager INSTANCE = new VCCredentialConfigManagerImpl();
    private final VCConfigMgtDAO dao = new VCConfigMgtDAOImpl();

    private VCCredentialConfigManagerImpl() {

    }

    public static VCCredentialConfigManager getInstance() {

        return INSTANCE;
    }

    @Override
    public List<VCCredentialConfiguration> list(String tenantDomain) throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.list(tenantId);
    }

    @Override
    public VCCredentialConfiguration get(String id, String tenantDomain) throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.get(id, tenantId);
    }

    @Override
    public VCCredentialConfiguration getByConfigId(String configId, String tenantDomain) throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getByConfigId(configId, tenantId);
    }

    @Override
    public VCCredentialConfiguration add(VCCredentialConfiguration configuration, String tenantDomain)
            throws VCConfigMgtException {

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

        String newDisplayName = configuration.getDisplayName();
        if (StringUtils.isBlank(newDisplayName)) {
            configuration.setDisplayName(existing.getDisplayName());
        }

        normalizeFormatForUpdate(configuration, existing);
        configuration.setSigningAlgorithm(DEFAULT_SIGNING_ALGORITHM);
        normalizeCredentialTypeForUpdate(configuration, existing);
        normalizeMetadataForUpdate(configuration, existing);
        normalizeExpiryInForUpdate(configuration, existing);
        normalizeScopeForUpdate(configuration, existing);
        validateScope(configuration.getScope(), tenantDomain);
        normalizeClaimsForUpdate(configuration, existing);
        validateClaims(configuration.getClaims(), tenantDomain);
        return dao.update(id, configuration, tenantId);
    }

    @Override
    public void delete(String configId, String tenantDomain) throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        dao.delete(configId, tenantId);
    }

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

    private void validateDisplayName(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        if (StringUtils.isBlank(configuration.getDisplayName())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Display name cannot be empty.");
        }
    }

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

    private void validateCredentialType(String credentialType) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(credentialType)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Credential type cannot be empty.");
        }
    }

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

    private void normalizeScopeForUpdate(VCCredentialConfiguration configuration,
                                          VCCredentialConfiguration existing) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(configuration.getScope())) {
            configuration.setScope(existing.getScope());
        }
    }

    private void normalizeFormatForUpdate(VCCredentialConfiguration configuration,
                                          VCCredentialConfiguration existing) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(configuration.getFormat())) {
            configuration.setFormat(existing.getFormat());
        }
        validateFormat(configuration);
    }

    private void normalizeCredentialTypeForUpdate(VCCredentialConfiguration configuration,
                                                  VCCredentialConfiguration existing)
            throws VCConfigMgtClientException {

        String credentialType = configuration.getType();
        if (StringUtils.isBlank(credentialType)) {
            credentialType = existing.getType();
            configuration.setType(credentialType);
        }
        validateCredentialType(credentialType);
    }

    private void normalizeMetadataForUpdate(VCCredentialConfiguration configuration,
                                            VCCredentialConfiguration existing) throws VCConfigMgtClientException {

        VCCredentialConfiguration.Metadata metadata = configuration.getMetadata();
        if (metadata == null) {
            metadata = existing.getMetadata();
            configuration.setMetadata(metadata);
        }
    }

    private void normalizeExpiryInForUpdate(VCCredentialConfiguration configuration,
                                          VCCredentialConfiguration existing) throws VCConfigMgtClientException {

        Integer expiry = configuration.getExpiresIn();
        if (expiry == null) {
            expiry = existing.getExpiresIn();
            configuration.setExpiresIn(expiry);
        }
        validateExpiry(expiry);
    }

    private void normalizeClaimsForUpdate(VCCredentialConfiguration configuration,
                                         VCCredentialConfiguration existing) {

        List<String> claims = configuration.getClaims();
        if (claims == null || claims.isEmpty()) {
            configuration.setClaims(existing.getClaims());
        }
    }
}
