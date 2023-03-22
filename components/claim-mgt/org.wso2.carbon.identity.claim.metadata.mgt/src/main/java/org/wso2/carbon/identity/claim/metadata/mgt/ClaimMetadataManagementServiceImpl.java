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
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedLocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataServerException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceComponent;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimMetadataMgtListener;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_TENANT_DOMAIN;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService}
 * interface.
 */
public class ClaimMetadataManagementServiceImpl implements ClaimMetadataManagementService {

    private static final Log log = LogFactory.getLog(ClaimMetadataManagementServiceImpl.class);

    private ClaimDialectDAO claimDialectDAO = new CacheBackedClaimDialectDAO();
    private CacheBackedLocalClaimDAO localClaimDAO = new CacheBackedLocalClaimDAO(new LocalClaimDAO());
    private CacheBackedExternalClaimDAO externalClaimDAO = new CacheBackedExternalClaimDAO(new ExternalClaimDAO());
    private static final int MAX_CLAIM_PROPERTY_LENGTH = 255;
    private static final int MAX_CLAIM_PROPERTY_LENGTH_LIMIT = 1024;
    private static final int MIN_CLAIM_PROPERTY_LENGTH_LIMIT = 0;

    @Override
    public List<ClaimDialect> getClaimDialects(String tenantDomain) throws ClaimMetadataException {

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ClaimDialect> claimDialects = this.claimDialectDAO.getClaimDialects(tenantId);

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

        List<ClaimDialect> claimDialects = this.claimDialectDAO.getClaimDialects(tenantId);
        Set<String> claimDialectUris = claimDialects.stream().map(ClaimDialect::getClaimDialectURI).
                collect(Collectors.toSet());

        if (claimDialectUris.contains(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_CLAIM_DIALECT.getCode(),
                    String.format(ERROR_CODE_EXISTING_CLAIM_DIALECT.getMessage(), claimDialect.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreAddClaimDialect(tenantId, claimDialect);

        this.claimDialectDAO.addClaimDialect(claimDialect, tenantId);

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

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateClaimDialect(tenantId, oldClaimDialect, newClaimDialect);

        this.claimDialectDAO.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
        externalClaimDAO.removeExternalClaimCache(oldClaimDialect.getClaimDialectURI(), tenantId);

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

        this.claimDialectDAO.removeClaimDialect(claimDialect, tenantId);
        // When deleting a claim dialect the relevant external claim deletion is handled by the DB through
        // ON DELETE CASCADE. Here we are removing the relevant cache entry.
        externalClaimDAO.removeExternalClaimCache(claimDialect.getClaimDialectURI(), tenantId);
        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteClaimDialect(tenantId, claimDialect);

    }

    @Override
    public List<LocalClaim> getLocalClaims(String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<LocalClaim> localClaims = this.localClaimDAO.getLocalClaims(tenantId);

        // Add listener

        return IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled() ? localClaims.stream().filter(
                localClaim -> !UserCoreConstants.ROLE_CLAIM.equals(localClaim.getClaimURI())).collect(
                Collectors.toList()) : localClaims;
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

        if (isExistingLocalClaimURI(localClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_LOCAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreAddLocalClaim(tenantId, localClaim);

        this.localClaimDAO.addLocalClaim(localClaim, tenantId);

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

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateLocalClaim(tenantId, localClaim);

        this.localClaimDAO.updateLocalClaim(localClaim, tenantId);

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

        this.localClaimDAO.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);

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

        boolean isMappedLocalClaim = this.externalClaimDAO.isMappedLocalClaim(localClaimURI, tenantId);

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

        this.localClaimDAO.removeLocalClaim(localClaimURI, tenantId);

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

        List<ExternalClaim> externalClaims = this.externalClaimDAO.getExternalClaims(externalClaimDialectURI, tenantId);

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

        if (isExistingExternalClaimURI(externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI.getMessage(), externalClaim.getClaimURI(),
                            externalClaim.getClaimDialectURI()));
        }

        boolean isLocalClaimAlreadyMapped =
                this.externalClaimDAO.isLocalClaimMappedWithinDialect(externalClaim.getMappedLocalClaim(),
                        externalClaim.getClaimDialectURI(), tenantId);

        if (isLocalClaimAlreadyMapped) {
            throw new ClaimMetadataClientException((ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getCode()),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getMessage(),
                            externalClaim.getMappedLocalClaim(), externalClaim.getClaimDialectURI()));
        }

        // Add listener

        this.externalClaimDAO.addExternalClaim(externalClaim, tenantId);

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

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateExternalClaim(tenantId, externalClaim);

        this.externalClaimDAO.updateExternalClaim(externalClaim, tenantId);

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

        this.externalClaimDAO.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteExternalClaim(tenantId,
                externalClaimDialectURI, externalClaimURI);
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        if (StringUtils.isEmpty(userstoreDomain)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_TENANT_DOMAIN.getCode(),
                    ERROR_CODE_EMPTY_TENANT_DOMAIN.getMessage());
        }
        try {
            this.localClaimDAO.removeClaimMappingAttributes(tenantId, userstoreDomain);
        } catch (UserStoreException e) {
            String errorMessage = String.format(
                    ClaimConstants.ErrorMessage.ERROR_CODE_SERVER_ERROR_DELETING_CLAIM_MAPPINGS.getMessage(),
                    tenantId, userstoreDomain);
            throw new ClaimMetadataServerException(
                    ClaimConstants.ErrorMessage.ERROR_CODE_SERVER_ERROR_DELETING_CLAIM_MAPPINGS.getCode(),
                    errorMessage, e);
        }
    }

    /**
     * Remove all claims of a given tenant.
     *
     * @param tenantId The id of the tenant.
     * @throws ClaimMetadataException throws when an error occurs in removing claims.
     */
    @Override
    public void removeAllClaims(int tenantId) throws ClaimMetadataException {

        // The relevant external claim deletions are handled by the DB through ON DELETE CASCADE.
        this.claimDialectDAO.removeAllClaimDialects(tenantId);
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
    public void validateClaimAttributeMapping(List<LocalClaim> localClaimList,  String tenantDomain)
            throws ClaimMetadataException {

        for (LocalClaim localClaim : localClaimList) {
            if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
                throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
            } else if (localClaim.getMappedAttributes().isEmpty()) {
                throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                        String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                                .getClaimDialectURI(), localClaim.getClaimURI()));
            }
            if (!isExistingLocalClaimURI(localClaim.getClaimURI(), IdentityTenantUtil.getTenantId(tenantDomain))) {
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

    private boolean isExistingExternalClaimURI(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        return this.externalClaimDAO.getExternalClaims(externalClaimDialectURI, tenantId).stream().filter(
                claim -> claim.getClaimURI().equalsIgnoreCase(externalClaimURI)).findFirst().isPresent();
    }

    private boolean isExistingLocalClaimURI(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.localClaimDAO.getLocalClaims(tenantId).stream().filter(
                claim -> claim.getClaimURI().equalsIgnoreCase(localClaimURI)).findFirst().isPresent();
    }
}
