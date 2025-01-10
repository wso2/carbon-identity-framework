/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadWriteClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceComponent;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimMetadataMgtListener;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CLAIM_LENGTH_LIMIT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_TENANT_DOMAIN;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_ATTRIBUTE_PROFILE;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_TENANT_DOMAIN;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.getAllowedClaimProfiles;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.getServerLevelClaimUniquenessScope;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService}
 * interface.
 */
public class ClaimMetadataManagementServiceImpl implements ClaimMetadataManagementService {

    private static final Log log = LogFactory.getLog(ClaimMetadataManagementServiceImpl.class);

    private final ReadWriteClaimMetadataManager unifiedClaimMetadataManager = new UnifiedClaimMetadataManager();
    private static final int MAX_CLAIM_PROPERTY_LENGTH = 255;
    private static final int MAX_CLAIM_PROPERTY_LENGTH_LIMIT = 1024;
    private static final int MIN_CLAIM_PROPERTY_LENGTH_LIMIT = 0;

    @Override
    public List<ClaimDialect> getClaimDialects(String tenantDomain) throws ClaimMetadataException {

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ClaimDialect> claimDialects = this.unifiedClaimMetadataManager.getClaimDialects(tenantId);

        // Add listener

        return claimDialects;
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException {

        if (claimDialect == null || StringUtils.isBlank(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_CLAIM_DIALECT);
        }
        if (StringUtils.isBlank(tenantDomain)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_TENANT_DOMAIN);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_TENANT_DOMAIN.getCode(),
                    String.format(ERROR_CODE_INVALID_TENANT_DOMAIN.getMessage(), tenantDomain));
        }

        List<ClaimDialect> claimDialects = this.unifiedClaimMetadataManager.getClaimDialects(tenantId);
        Set<String> claimDialectUris = claimDialects.stream().map(ClaimDialect::getClaimDialectURI).
                collect(Collectors.toSet());

        if (claimDialectUris.contains(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_CLAIM_DIALECT.getCode(),
                    String.format(ERROR_CODE_EXISTING_CLAIM_DIALECT.getMessage(), claimDialect.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreAddClaimDialect(tenantId, claimDialect);

        this.unifiedClaimMetadataManager.addClaimDialect(claimDialect, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostAddClaimDialect(tenantId, claimDialect);

    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, String tenantDomain)
            throws ClaimMetadataException {

        if (oldClaimDialect == null || StringUtils.isBlank(oldClaimDialect.getClaimDialectURI())
                || newClaimDialect == null || StringUtils.isBlank(newClaimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_CLAIM_DIALECT);
        }

        // TODO : Validate oldClaimDialectURI is valid????

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        boolean isRenamedDialectAlreadyTaken = isExistingClaimDialect(newClaimDialect.getClaimDialectURI(), tenantId);
        if (isRenamedDialectAlreadyTaken) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_CLAIM_DIALECT.getCode(),
                    String.format(ERROR_CODE_EXISTING_CLAIM_DIALECT.getMessage(), newClaimDialect.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateClaimDialect(tenantId, oldClaimDialect, newClaimDialect);

        this.unifiedClaimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostUpdateClaimDialect(tenantId, oldClaimDialect, newClaimDialect);

    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException {

        if (claimDialect == null || StringUtils.isBlank(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_CLAIM_DIALECT.getCode(),
                    "Claim dialect URI cannot be empty");
        }

        // TODO : validate claim dialect already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        ClaimMetadataEventPublisherProxy.getInstance().publishPreDeleteClaimDialect(tenantId, claimDialect);

        this.unifiedClaimMetadataManager.removeClaimDialect(claimDialect, tenantId);
        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteClaimDialect(tenantId, claimDialect);

    }

    @Override
    public List<LocalClaim> getLocalClaims(String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<LocalClaim> localClaims = this.unifiedClaimMetadataManager.getLocalClaims(tenantId);

        // Add listener

        boolean isGroupRoleSeparationEnabled = IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled();
        List<LocalClaim> filteredLocalClaims = new ArrayList<>(localClaims.size());

        for (LocalClaim claim : localClaims) {
            if (isGroupRoleSeparationEnabled && UserCoreConstants.ROLE_CLAIM.equals(claim.getClaimURI())) {
                continue;
            }
            // Add `UniquenessScope` property for claims that only have legacy `isUnique` property.
            // `UniquenessScope` is the current property used to configure claim-wise uniqueness validation scope,
            // while `isUnique` is maintained for backward compatibility.
            Map<String, String> properties = claim.getClaimProperties();
            if (properties.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY) &&
                    !properties.containsKey(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY)) {
                updateScopeFromIsUnique(properties,
                        properties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY));
            }
            filteredLocalClaims.add(claim);
        }
        return filteredLocalClaims;
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
        } else if (localClaim.getMappedAttributes().isEmpty()) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                            .getClaimDialectURI(), localClaim.getClaimURI()));
        }
        validateClaimProperties(localClaim.getClaimProperties());

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (isExistingLocalClaim(localClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_LOCAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
        }

        validateAndSyncUniquenessClaimProperties(localClaim.getClaimProperties(), null);
        validateAndSyncAttributeProfileProperties(localClaim.getClaimProperties());

        ClaimMetadataEventPublisherProxy.getInstance().publishPreAddLocalClaim(tenantId, localClaim);

        this.unifiedClaimMetadataManager.addLocalClaim(localClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostAddLocalClaim(tenantId, localClaim);
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
        } else if (localClaim.getMappedAttributes().isEmpty()) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                            .getClaimDialectURI(), localClaim.getClaimURI()));
        }
        validateClaimProperties(localClaim.getClaimProperties());

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        Optional<LocalClaim> existingLocalClaim = getLocalClaim(localClaim.getClaimURI(), tenantId);
        if (!existingLocalClaim.isPresent()) {
            throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getMessage(), localClaim.getClaimURI()));
        }

        validateAndSyncUniquenessClaimProperties(localClaim.getClaimProperties(),
                existingLocalClaim.get().getClaimProperties());
        validateAndSyncAttributeProfileProperties(localClaim.getClaimProperties());

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateLocalClaim(tenantId, localClaim);

        this.unifiedClaimMetadataManager.updateLocalClaim(localClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostUpdateLocalClaim(tenantId, localClaim);
    }

    @Override
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, String tenantDomain, String userStoreDomain)
            throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        ClaimMetadataEventPublisherProxy claimMetadataEventPublisherProxy =
                ClaimMetadataEventPublisherProxy.getInstance();
        for (LocalClaim localClaim : localClaimList) {
            claimMetadataEventPublisherProxy.publishPreUpdateLocalClaim(tenantId, localClaim);
        }

        this.unifiedClaimMetadataManager.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);

        for (LocalClaim localClaim : localClaimList) {
            claimMetadataEventPublisherProxy.publishPostUpdateLocalClaim(tenantId, localClaim);
        }
    }

    @Override
    public void removeLocalClaim(String localClaimURI, String tenantDomain) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        boolean isMappedLocalClaim = this.unifiedClaimMetadataManager.isMappedLocalClaim(localClaimURI, tenantId);
        if (isMappedLocalClaim) {
            throw new ClaimMetadataClientException(ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM.getMessage(), localClaimURI));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreDeleteLocalClaim(tenantId, localClaimURI);
        Collection<ClaimMetadataMgtListener> listeners =
                IdentityClaimManagementServiceComponent.getClaimMetadataMgtListeners();
        for (ClaimMetadataMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteClaim(localClaimURI, tenantDomain)) {
                return;
            }
        }

        this.unifiedClaimMetadataManager.removeLocalClaim(localClaimURI, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteLocalClaim(tenantId, localClaimURI);
        for (ClaimMetadataMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteClaim(localClaimURI, tenantDomain)) {
                return;
            }
        }
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, String tenantDomain) throws
            ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ExternalClaim> externalClaims = this.unifiedClaimMetadataManager.getExternalClaims(
                externalClaimDialectURI, tenantId);

        // Add listener

        return externalClaims;
    }

    @Override
    public void addExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws ClaimMetadataException {

        if (externalClaim == null || StringUtils.isBlank(externalClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (StringUtils.isBlank(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI);
        }

        if (StringUtils.isBlank(externalClaim.getMappedLocalClaim())) {
            throw new ClaimMetadataClientException(ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        ClaimConfig claimConfig = IdentityClaimManagementServiceDataHolder.getInstance().getClaimConfig();
        String claimURIRegex = null;
        if (claimConfig != null) {
            claimURIRegex = claimConfig.getClaimUriRegex(externalClaim.getClaimDialectURI());
        }

        if (claimURIRegex != null && !externalClaim.getClaimURI().matches(claimURIRegex)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_URI);
        }

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (!isExistingClaimDialect(externalClaim.getClaimDialectURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getCode(),
                    String.format(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getMessage(),
                            externalClaim.getClaimDialectURI()));
        }

        if (isExistingExternalClaim(externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI.getMessage(), externalClaim.getClaimURI(),
                            externalClaim.getClaimDialectURI()));
        }

        if (!isExistingLocalClaim(externalClaim.getMappedLocalClaim(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI.getMessage(),
                            externalClaim.getMappedLocalClaim(), ClaimConstants.LOCAL_CLAIM_DIALECT_URI));
        }

        boolean isLocalClaimAlreadyMapped =
                this.unifiedClaimMetadataManager.isLocalClaimMappedWithinDialect(externalClaim.getMappedLocalClaim(),
                        externalClaim.getClaimDialectURI(), tenantId);

        if (isLocalClaimAlreadyMapped) {
            throw new ClaimMetadataClientException((ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getCode()),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getMessage(),
                            externalClaim.getMappedLocalClaim(), externalClaim.getClaimDialectURI()));
        }

        // Add listener

        this.unifiedClaimMetadataManager.addExternalClaim(externalClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostAddExternalClaim(tenantId, externalClaim);
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws
            ClaimMetadataException {

        if (externalClaim == null || StringUtils.isBlank(externalClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (StringUtils.isBlank(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI);
        }

        if (StringUtils.isBlank(externalClaim.getMappedLocalClaim())) {
            throw new ClaimMetadataClientException(ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (!isExistingClaimDialect(externalClaim.getClaimDialectURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getCode(),
                    String.format(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getMessage(),
                            externalClaim.getClaimDialectURI()));
        }

        if (!isExistingExternalClaim(externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI.getMessage(), externalClaim.getClaimURI(),
                            externalClaim.getClaimDialectURI()));
        }

        if (!isExistingLocalClaim(externalClaim.getMappedLocalClaim(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getMessage(), externalClaim.getMappedLocalClaim()));
        }

        boolean isLocalClaimAlreadyMapped = this.unifiedClaimMetadataManager.getMappedExternalClaims(
                externalClaim.getMappedLocalClaim(), tenantId).stream()
                .filter(claim -> claim.getClaimDialectURI().equals(externalClaim.getClaimDialectURI()))
                .anyMatch(claim -> !claim.getClaimURI().equals(externalClaim.getClaimURI()));

        if (isLocalClaimAlreadyMapped) {
            throw new ClaimMetadataClientException((ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getCode()),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getMessage(),
                            externalClaim.getMappedLocalClaim(), externalClaim.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateExternalClaim(tenantId, externalClaim);

        this.unifiedClaimMetadataManager.updateExternalClaim(externalClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostUpdateExternalClaim(tenantId, externalClaim);
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, String tenantDomain)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI.getCode(),
                    "External claim dialect URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaimURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        ClaimMetadataEventPublisherProxy.getInstance().publishPreDeleteExternalClaim(tenantId,
                externalClaimDialectURI, externalClaimURI);

        this.unifiedClaimMetadataManager.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteExternalClaim(tenantId,
                externalClaimDialectURI, externalClaimURI);
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        if (StringUtils.isEmpty(userstoreDomain)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_TENANT_DOMAIN.getCode(),
                    ERROR_CODE_EMPTY_TENANT_DOMAIN.getMessage());
        }
        this.unifiedClaimMetadataManager.removeClaimMappingAttributes(tenantId, userstoreDomain);
    }

    /**
     * Remove all claims of a given tenant.
     *
     * @param tenantId The id of the tenant.
     * @throws ClaimMetadataException throws when an error occurs in removing claims.
     */
    @Override
    public void removeAllClaims(int tenantId) throws ClaimMetadataException {

        this.unifiedClaimMetadataManager.removeAllClaimDialects(tenantId);
    }

    @Override
    public String getMaskingRegexForLocalClaim(String localClaimURI, String tenantDomain) throws
            ClaimMetadataException {

        List<LocalClaim> localClaims = getLocalClaims(tenantDomain);

        for (LocalClaim localClaim : localClaims) {
            if (localClaim.getClaimURI().equals(localClaimURI)) {
                return StringEscapeUtils.unescapeXml(localClaim.getClaimProperty(ClaimConstants
                        .MASKING_REGULAR_EXPRESSION_PROPERTY));
            }
        }
        return null;
    }

    @Override
    public void validateClaimAttributeMapping(List<LocalClaim> localClaimList, String tenantDomain)
            throws ClaimMetadataException {

        for (LocalClaim localClaim : localClaimList) {
            if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
                throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
            } else if (localClaim.getMappedAttributes().isEmpty()) {
                throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                        String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                                .getClaimDialectURI(), localClaim.getClaimURI()));
            }
            if (!isExistingLocalClaim(localClaim.getClaimURI(), IdentityTenantUtil.getTenantId(tenantDomain))) {
                throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getCode(),
                        String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
            }
        }
    }

    /**
     * Check whether the properties are valid.
     *
     * @param claimProperties List of claim properties.
     * @throws ClaimMetadataClientException If any property is not valid.
     */
    private void validateClaimProperties(Map<String, String> claimProperties) throws ClaimMetadataClientException {

        if (MapUtils.isEmpty(claimProperties)) {
            return;
        }
        for (Map.Entry<String, String> property : claimProperties.entrySet()) {
            String value = property.getValue();
            if (StringUtils.isNotBlank(value) && value.length() > MAX_CLAIM_PROPERTY_LENGTH) {
                throw new ClaimMetadataClientException(ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED.getCode(),
                        String.format(ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED.getMessage(), property.getKey(),
                                MAX_CLAIM_PROPERTY_LENGTH));
            }
            if (StringUtils.equalsIgnoreCase(ClaimConstants.MIN_LENGTH, property.getKey()) ||
                    StringUtils.equalsIgnoreCase(ClaimConstants.MAX_LENGTH, property.getKey())) {
                checkMinMaxLimit(property.getKey(), property.getValue());
            }
        }
    }

    private void checkMinMaxLimit(String property, String value) throws ClaimMetadataClientException {

        if (!NumberUtils.isNumber(value) || Integer.parseInt(value) < MIN_CLAIM_PROPERTY_LENGTH_LIMIT ||
                Integer.parseInt(value) > MAX_CLAIM_PROPERTY_LENGTH_LIMIT) {
            throw new ClaimMetadataClientException(ERROR_CODE_CLAIM_LENGTH_LIMIT.getCode(),
                    String.format(ERROR_CODE_CLAIM_LENGTH_LIMIT.getMessage(), property,
                            MIN_CLAIM_PROPERTY_LENGTH_LIMIT, MAX_CLAIM_PROPERTY_LENGTH_LIMIT));
        }
    }

    private boolean isExistingClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getClaimDialects(tenantId).stream().anyMatch(
                claimDialect -> claimDialect.getClaimDialectURI().equalsIgnoreCase(claimDialectURI));
    }

    private boolean isExistingExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getExternalClaims(externalClaimDialectURI, tenantId).stream().anyMatch(
                claim -> claim.getClaimURI().equalsIgnoreCase(externalClaimURI));
    }

    private boolean isExistingLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getLocalClaims(tenantId).stream().anyMatch(
                claim -> claim.getClaimURI().equalsIgnoreCase(localClaimURI));
    }

    private Optional<LocalClaim> getLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
    }

    @Override
    public List<Claim> getMappedExternalClaimsForLocalClaim(String localClaimURI, String tenantDomain) throws
            ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return this.unifiedClaimMetadataManager.getMappedExternalClaims(localClaimURI, tenantId);
    }

    /**
     * Validate and sync the claim profile properties.
     *
     * @param claimProperties Map of claim properties to be updated.
     * @throws ClaimMetadataClientException If an invalid profile name is found.
     */
    private void validateAndSyncAttributeProfileProperties(Map<String, String> claimProperties)
            throws ClaimMetadataClientException {

        Set<String> allowedClaimProfiles = getAllowedClaimProfiles();

        // Validate profile names and throw an exception if an invalid profile name is found.
        for (Map.Entry<String, String> entry : claimProperties.entrySet()) {
            if (!entry.getKey().startsWith(ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX)) {
                continue;
            }

           String[] profilePropertyKeyArray = entry.getKey().split(ClaimConstants.CLAIM_PROFILE_DELIMITER);
           if (profilePropertyKeyArray.length < 2 || !allowedClaimProfiles.contains(profilePropertyKeyArray[1])) {
               throw new ClaimMetadataClientException(ERROR_CODE_INVALID_ATTRIBUTE_PROFILE);
           }
        }

        String[] allowedProfilePropertyKeys = {
                ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY,
                ClaimConstants.REQUIRED_PROPERTY,
                ClaimConstants.READ_ONLY_PROPERTY
        };
        for (String propertyKey: allowedProfilePropertyKeys) {
            syncAttributeProfileProperties(claimProperties, allowedClaimProfiles, propertyKey);
        }
    }

    /**
     * Remove the profile properties if the profile property value is the same as the global property value.
     * If all the profile properties have the same value, change the global property value to the profile value.
     *
     * @param claimProperties      Map of claim properties to be updated.
     * @param allowedClaimProfiles Set of allowed claim profiles.
     * @param propertyKey          Property key to be updated.
     */
    private void syncAttributeProfileProperties(Map<String, String> claimProperties, Set<String> allowedClaimProfiles,
                                                String propertyKey) {

        String globalValue = claimProperties.get(propertyKey);
        boolean allProfileValuesMatch = true;
        String consistentProfileValue = null;

        for (String profileName : allowedClaimProfiles) {
            String profilePropertyKey = ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX + profileName + "." + propertyKey;
            String profilePropertyValue = claimProperties.get(profilePropertyKey);

            // Remove the profile property if it is the same as the global property.
            if (StringUtils.equals(globalValue, profilePropertyValue)) {
                claimProperties.remove(profilePropertyKey);
                allProfileValuesMatch = false;
                continue;
            }

            // If a mismatch is already detected, no need to check further for consistency.
            if (!allProfileValuesMatch) {
                continue;
            }

            if (StringUtils.isBlank(profilePropertyValue)) {
                allProfileValuesMatch = false;
                continue;
            }
            if (consistentProfileValue == null) {
                consistentProfileValue = profilePropertyValue;
            } else if (!StringUtils.equals(consistentProfileValue, profilePropertyValue)) {
                allProfileValuesMatch = false;
            }
        }

        // If all the profiles have the same value change the global property value to the profile value.
        if (allProfileValuesMatch && StringUtils.isNotBlank(consistentProfileValue)) {
            claimProperties.put(propertyKey, consistentProfileValue);

            // Remove the profile properties.
            allowedClaimProfiles.forEach(profile -> {
                String key = ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX + profile + "." + propertyKey;
                claimProperties.remove(key);
            });
        }
    }

    /**
     * Updates and synchronizes the claim uniqueness properties in the properties map.
     * Manages the relationship between the legacy 'isUnique' property and the newer 'UniquenessScope' property,
     * ensuring consistency between both properties based on their values.
     *
     * @param claimProperties         Map of claim properties to be updated.
     * @param existingClaimProperties Map of existing claim properties (null for new claims).
     */
    private void validateAndSyncUniquenessClaimProperties(Map<String, String> claimProperties,
                                                          Map<String, String> existingClaimProperties) {

        // Case 1: If the 'isUnique' property is not provided,
        // no property synchronization is needed.
        if (!claimProperties.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY)) {
            return;
        }

        String newUniquenessScopeValue = claimProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY);
        String newIsUniqueValue = claimProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY);

        // Case 2: If only the 'isUnique' property is provided,
        // set the 'UniquenessScope' property based on the 'isUnique' value.
        if (!claimProperties.containsKey(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY)) {
            updateScopeFromIsUnique(claimProperties, newIsUniqueValue);
            return;
        }

        // Case 3: If both 'isUnique' and 'UniquenessScope' properties are provided.
        if (existingClaimProperties == null) {
            // If there are no existing claim properties (i.e., this is a new claim),
            // prioritize 'UniquenessScope' & update the 'isUnique' property accordingly.
            updateIsUniqueFromScope(claimProperties, newUniquenessScopeValue);
            return;
        }

        String existingScopeValue = existingClaimProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY);
        String existingIsUniqueValue = existingClaimProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY);

        boolean isScopeChanged = !StringUtils.equals(newUniquenessScopeValue, existingScopeValue);
        boolean isUniqueChanged = !StringUtils.equalsIgnoreCase(newIsUniqueValue, existingIsUniqueValue);

        if (isScopeChanged) {
            // If 'UniquenessScope' has changed (regardless of 'isUnique' changes), prioritize 'UniquenessScope'.
            updateIsUniqueFromScope(claimProperties, newUniquenessScopeValue);
        } else if (isUniqueChanged) {
            // If only 'isUnique' has changed, update 'UniquenessScope' based on the new 'isUnique' value.
            updateScopeFromIsUnique(claimProperties, newIsUniqueValue);
        }
    }

    /**
     * Updates the uniqueness scope property based on the isUnique value.
     *
     * @param properties    Map of claim properties to update.
     * @param isUniqueValue String value of isUnique property ("true" or "false").
     */
    private void updateScopeFromIsUnique(Map<String, String> properties, String isUniqueValue) {

        boolean isUnique = Boolean.parseBoolean(isUniqueValue);
        properties.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                isUnique ? getServerLevelClaimUniquenessScope().toString() :
                        ClaimConstants.ClaimUniquenessScope.NONE.toString());
    }

    /**
     * Updates the isUnique property based on the uniqueness scope value.
     *
     * @param properties Map of claim properties to update.
     * @param scopeValue String value of the uniqueness scope.
     */
    private void updateIsUniqueFromScope(Map<String, String> properties, String scopeValue) {

        boolean isUnique = !ClaimConstants.ClaimUniquenessScope.NONE.toString().equals(scopeValue);
        properties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, String.valueOf(isUnique));
    }

}
