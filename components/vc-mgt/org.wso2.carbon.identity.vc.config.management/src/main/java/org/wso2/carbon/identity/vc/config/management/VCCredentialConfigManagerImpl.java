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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.VCConfigMgtDAO;
import org.wso2.carbon.identity.vc.config.management.dao.impl.VCConfigMgtDAOImpl;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.model.ClaimMapping;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.List;

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
    public VCCredentialConfiguration get(String configId, String tenantDomain) throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getByConfigId(configId, tenantId);
    }

    @Override
    public VCCredentialConfiguration create(VCCredentialConfiguration configuration, String tenantDomain)
            throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Server controls the persisted id; clients must not supply it.
        if (StringUtils.isNotBlank(configuration.getId())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Configuration id is server generated and cannot be set in the request body.");
        }

        validateIdentifierForCreate(configuration, tenantId);
        validateConfigurationIdForCreate(configuration, tenantId);
        applyFormatDefault(configuration);
        validateSigningAlgorithm(configuration.getCredentialSigningAlgValuesSupported());
        validateCredentialType(configuration.getCredentialType());
        validateMetadata(configuration.getCredentialMetadata());
        validateExpiry(configuration.getExpiryInSeconds());
        validateClaimMappings(configuration.getClaimMappings());
        return dao.create(configuration, tenantId);
    }

    @Override
    public VCCredentialConfiguration update(String configId, VCCredentialConfiguration configuration,
                                            String tenantDomain) throws VCConfigMgtException {

        if (configuration.getId() != null && !StringUtils.equals(configId, configuration.getId())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_ID_MISMATCH.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_ID_MISMATCH.getMessage());
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Validate identifier uniqueness if changed.
        VCCredentialConfiguration existing = dao.getByConfigId(configId, tenantId);
        if (existing == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
        }
        String newIdentifier = configuration.getIdentifier();
        if (StringUtils.isBlank(newIdentifier)) {
            configuration.setIdentifier(existing.getIdentifier());
        } else if (!StringUtils.equals(newIdentifier, existing.getIdentifier()) &&
                dao.existsByIdentifier(newIdentifier, tenantId)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_IDENTIFIER_ALREADY_EXISTS.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_IDENTIFIER_ALREADY_EXISTS.getMessage());
        }

        String newConfigurationId = configuration.getConfigurationId();
        if (StringUtils.isBlank(newConfigurationId)) {
            configuration.setConfigurationId(existing.getConfigurationId());
        } else if (!StringUtils.equals(newConfigurationId, existing.getConfigurationId()) &&
                dao.existsByConfigurationId(newConfigurationId, tenantId)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIGURATION_ID_ALREADY_EXISTS.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIGURATION_ID_ALREADY_EXISTS.getMessage());
        }

        normalizeFormatForUpdate(configuration, existing);
        normalizeSigningAlgorithmForUpdate(configuration, existing);
        normalizeCredentialTypeForUpdate(configuration, existing);
        normalizeMetadataForUpdate(configuration, existing);
        normalizeExpiryForUpdate(configuration, existing);
        validateClaimMappings(configuration.getClaimMappings());
        return dao.update(configId, configuration, tenantId);
    }

    @Override
    public void delete(String configId, String tenantDomain) throws VCConfigMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        dao.delete(configId, tenantId);
    }

    private void validateIdentifierForCreate(VCCredentialConfiguration configuration, int tenantId)
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

    private void validateConfigurationIdForCreate(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        if (StringUtils.isBlank(configuration.getConfigurationId())) {
            configuration.setConfigurationId(configuration.getIdentifier());
        }
        if (dao.existsByConfigurationId(configuration.getConfigurationId(), tenantId)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIGURATION_ID_ALREADY_EXISTS.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIGURATION_ID_ALREADY_EXISTS.getMessage());
        }
    }

    private void applyFormatDefault(VCCredentialConfiguration configuration) {

        if (StringUtils.isBlank(configuration.getFormat())) {
            configuration.setFormat(VCConfigManagementConstants.DEFAULT_VC_FORMAT);
        }
    }

    private void validateSigningAlgorithm(String algorithm) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(algorithm)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Credential signing algorithm value is required.");
        }
    }

    private void validateCredentialType(String credentialType) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(credentialType)) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Credential type cannot be empty.");
        }
    }

    private void validateMetadata(VCCredentialConfiguration.CredentialMetadata metadata)
            throws VCConfigMgtClientException {

        if (metadata == null || StringUtils.isBlank(metadata.getDisplay())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Credential metadata display payload is required.");
        }
    }

    private void validateExpiry(Integer expiryInSeconds) throws VCConfigMgtClientException {

        if (expiryInSeconds == null || expiryInSeconds < VCConfigManagementConstants.MIN_EXPIRY_IN_SECONDS) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    String.format("Expiry must be at least %d seconds.",
                            VCConfigManagementConstants.MIN_EXPIRY_IN_SECONDS));
        }
    }

    private void validateClaimMappings(List<ClaimMapping> claimMappings)
            throws VCConfigMgtClientException {

        if (claimMappings == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Claim mappings cannot be null.");
        }
        for (ClaimMapping mapping : claimMappings) {
            if (mapping == null || StringUtils.isBlank(mapping.getClaimURI()) ||
                    StringUtils.isBlank(mapping.getDisplay())) {
                throw new VCConfigMgtClientException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                        "Each claim mapping must contain claimURI and display values.");
            }
        }
    }

    private void normalizeFormatForUpdate(VCCredentialConfiguration configuration,
                                          VCCredentialConfiguration existing) {

        if (StringUtils.isBlank(configuration.getFormat())) {
            configuration.setFormat(existing.getFormat());
        }
        applyFormatDefault(configuration);
    }

    private void normalizeSigningAlgorithmForUpdate(VCCredentialConfiguration configuration,
                                                    VCCredentialConfiguration existing)
            throws VCConfigMgtClientException {

        String algorithm = configuration.getCredentialSigningAlgValuesSupported();
        if (StringUtils.isBlank(algorithm)) {
            algorithm = existing.getCredentialSigningAlgValuesSupported();
            configuration.setCredentialSigningAlgValuesSupported(algorithm);
        }
        validateSigningAlgorithm(algorithm);
    }

    private void normalizeCredentialTypeForUpdate(VCCredentialConfiguration configuration,
                                                  VCCredentialConfiguration existing)
            throws VCConfigMgtClientException {

        String credentialType = configuration.getCredentialType();
        if (StringUtils.isBlank(credentialType)) {
            credentialType = existing.getCredentialType();
            configuration.setCredentialType(credentialType);
        }
        validateCredentialType(credentialType);
    }

    private void normalizeMetadataForUpdate(VCCredentialConfiguration configuration,
                                            VCCredentialConfiguration existing) throws VCConfigMgtClientException {

        VCCredentialConfiguration.CredentialMetadata metadata = configuration.getCredentialMetadata();
        if (metadata == null) {
            metadata = existing.getCredentialMetadata();
            configuration.setCredentialMetadata(metadata);
        }
        validateMetadata(metadata);
    }

    private void normalizeExpiryForUpdate(VCCredentialConfiguration configuration,
                                          VCCredentialConfiguration existing) throws VCConfigMgtClientException {

        Integer expiry = configuration.getExpiryInSeconds();
        if (expiry == null) {
            expiry = existing.getExpiryInSeconds();
            configuration.setExpiryInSeconds(expiry);
        }
        validateExpiry(expiry);
    }
}
