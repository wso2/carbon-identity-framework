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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;
import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSConfigurationDAO;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;
import org.wso2.carbon.identity.cors.mgt.core.internal.util.CORSConfigurationUtils;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSApplication;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;
import org.wso2.carbon.identity.cors.mgt.core.model.Origin;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.exception.OrgResourceHierarchyTraverseException;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.strategy.MergeAllAggregationStrategy;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_CONFIG_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_DUPLICATE_ORIGINS;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_ORIGIN_NOT_PRESENT;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_ORIGIN_PRESENT;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_VALIDATE_APP_ID;
import static org.wso2.carbon.identity.cors.mgt.core.internal.util.ErrorUtils.handleClientException;

/**
 * Implementation of the CORSService.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.cors.mgt.core.CORSManagementService",
                "service.scope=singleton"
        }
)
public class CORSManagementServiceImpl implements CORSManagementService {

    private static final Log log = LogFactory.getLog(CORSManagementServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getTenantCORSOrigins(String tenantDomain) throws CORSManagementServiceException {

        List<CORSOrigin> corsOrigins;
        OrganizationManager organizationManager = CORSManagementServiceHolder.getInstance().getOrganizationManager();
        try {
            String organizationId = organizationManager.resolveOrganizationId(tenantDomain);
            OrgResourceResolverService orgResourceManagementService =
                    CORSManagementServiceHolder.getInstance().getOrgResourceResolverService();
            corsOrigins = orgResourceManagementService.getResourcesFromOrgHierarchy(
                    organizationId,
                    LambdaExceptionUtils.rethrowFunction(this::getCorsOrigins),
                    new MergeAllAggregationStrategy<>(this::mergeAndRemoveDuplicates)
            );
        } catch (OrganizationManagementException | OrgResourceHierarchyTraverseException e) {
            throw new CORSManagementServiceException(
                    String.format(ERROR_CODE_CORS_CONFIG_RETRIEVE.getDescription(), tenantDomain),
                    ERROR_CODE_CORS_CONFIG_RETRIEVE.getCode(), e);
        }

        return Collections.unmodifiableList(corsOrigins);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getApplicationCORSOrigins(String applicationId, String tenantDomain)
            throws CORSManagementServiceException {

        int tenantId = getTenantId(tenantDomain);
        ApplicationBasicInfo applicationBasicInfo = getApplicationBasicInfo(applicationId, tenantDomain);

        return Collections.unmodifiableList(getCORSOriginDAO().getCORSOriginsByApplicationId(
                applicationBasicInfo.getApplicationId(), tenantId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCORSOrigins(String applicationId, List<String> origins, String tenantDomain)
            throws CORSManagementServiceException {

        int tenantId = getTenantId(tenantDomain);
        ApplicationBasicInfo applicationBasicInfo = getApplicationBasicInfo(applicationId, tenantDomain);

        // Check for duplicate entries.
        if (CORSConfigurationUtils.hasDuplicates(origins)) {
            throw handleClientException(ERROR_CODE_DUPLICATE_ORIGINS);
        }

        List<Origin> originList = CORSConfigurationUtils.createOriginList(origins);

        // Set the CORS origins.
        getCORSOriginDAO().setCORSOrigins(applicationBasicInfo.getApplicationId(),
                originList.stream().map(origin -> {
                    // Create the CORS origin.
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setOrigin(origin.getValue());
                    return corsOrigin;
                }).collect(Collectors.toList()), tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCORSOrigins(String applicationId, List<String> origins, String tenantDomain)
            throws CORSManagementServiceException {

        int tenantId = getTenantId(tenantDomain);
        ApplicationBasicInfo applicationBasicInfo = getApplicationBasicInfo(applicationId, tenantDomain);
        List<Origin> originList = CORSConfigurationUtils.createOriginList(origins);

        // Check if the CORS origins are already present.
        List<CORSOrigin> existingCORSOrigins = getCORSOriginDAO().getCORSOriginsByApplicationId(
                applicationBasicInfo.getApplicationId(), tenantId);

        List<String> corsOriginIdList = existingCORSOrigins.stream().map(CORSOrigin::getId)
                .collect(Collectors.toList());
        for (Origin origin : originList) {
            if (corsOriginIdList.contains(origin.getValue())) {
                // CORS origin is already registered for the application.
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Duplicate addition of existing CORS Origin (%s) for the " +
                            "application id: %s, tenant domain: %s", origin, applicationId, tenantDomain));
                }
                throw handleClientException(ERROR_CODE_ORIGIN_PRESENT, tenantDomain, origin.getValue());
            }
        }

        // Add the CORS origins.
        getCORSOriginDAO().addCORSOrigins(applicationBasicInfo.getApplicationId(),
                originList.stream().map(origin -> {
                    // Create the CORS origin.
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setOrigin(origin.getValue());
                    return corsOrigin;
                }).collect(Collectors.toList()), tenantId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCORSOrigins(String applicationId, List<String> originIds, String tenantDomain)
            throws CORSManagementServiceException {

        int tenantId = getTenantId(tenantDomain);
        ApplicationBasicInfo applicationBasicInfo = getApplicationBasicInfo(applicationId, tenantDomain);

        // Check if the CORS origins are not in the system.
        List<CORSOrigin> existingCORSOrigins = getCORSOriginDAO().getCORSOriginsByApplicationId(
                applicationBasicInfo.getApplicationId(), tenantId);
        for (String originId : originIds) {
            if (!existingCORSOrigins.stream().map(CORSOrigin::getId).collect(Collectors.toList()).contains(originId)) {
                // CORS origin is not registered for the application.
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Application %s of the tenant %s doesn't have a CORS Origin with " +
                            "the ID of %s.", applicationId, tenantDomain, originId));
                }
                throw handleClientException(ERROR_CODE_ORIGIN_NOT_PRESENT, tenantDomain, originId);
            }
        }

        // Delete the CORS origin application associations.
        getCORSOriginDAO().deleteCORSOrigins(applicationBasicInfo.getApplicationId(), originIds, tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSApplication> getCORSApplicationsByCORSOriginId(String corsOriginId, String tenantDomain)
            throws CORSManagementServiceException {

        // DAO layer throws an exception if CORSApplications cannot be retrieved for the corsOriginId.
        // i.e The corsOriginId is invalid.
        return Collections.unmodifiableList(getCORSOriginDAO().getCORSOriginApplications(corsOriginId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CORSConfiguration getCORSConfiguration(String tenantDomain) throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);

        return getCORSConfigurationDAO().getCORSConfigurationByTenantDomain(tenantDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCORSConfiguration(CORSConfiguration corsConfiguration, String tenantDomain)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);

        getCORSConfigurationDAO().setCORSConfigurationByTenantDomain(corsConfiguration, tenantDomain);
    }

    /**
     * Returns a CORSOriginDAO instance.
     *
     * @return A CORSOriginDAO instance.
     */
    private CORSOriginDAO getCORSOriginDAO() {

        return CORSManagementServiceHolder.getInstance().getCorsOriginDAO();
    }

    /**
     * Returns a CORSConfigurationDAO instance.
     *
     * @return A CORSConfigurationDAO instance.
     */
    private CORSConfigurationDAO getCORSConfigurationDAO() {

        return CORSManagementServiceHolder.getInstance().getCorsConfigurationDAO();
    }

    /**
     * Returns the tenant ID from the tenant domain.
     *
     * @param tenantDomain The tenant domain.
     * @return The tenant ID.
     * @throws CORSManagementServiceClientException
     */
    private int getTenantId(String tenantDomain) throws CORSManagementServiceClientException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_TENANT_DOMAIN, tenantDomain);
        } else {
            return tenantId;
        }
    }

    /**
     * Validate the tenant domain.
     *
     * @param tenantDomain The tenant domain.
     * @throws CORSManagementServiceClientException
     */
    private void validateTenantDomain(String tenantDomain) throws CORSManagementServiceClientException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_TENANT_DOMAIN, tenantDomain);
        }
    }

    /**
     * Get an {code ApplicationBasicInfo} instance belonging to the application with the resource ID {@code appId}.
     *
     * @param applicationId The application resource ID.
     * @param tenantDomain  The tenant domain.
     * @return An {@code ApplicationBasicInfo} instance.
     * @throws CORSManagementServiceClientException
     */
    private ApplicationBasicInfo getApplicationBasicInfo(String applicationId, String tenantDomain)
            throws CORSManagementServiceClientException {

        // If the appId is blank then throw an exception.
        if (StringUtils.isBlank(applicationId)) {
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_APP_ID, applicationId);
        }

        // Check whether the appId belongs to the tenant with the tenantDomain.
        try {
            ApplicationBasicInfo applicationBasicInfo = ApplicationManagementService.getInstance()
                    .getApplicationBasicInfoByResourceId(applicationId, tenantDomain);
            if (applicationBasicInfo == null) {
                throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_APP_ID, applicationId);
            } else {
                return applicationBasicInfo;
            }
        } catch (IdentityApplicationManagementException e) {
            // Something else happened.
            log.error(String.format(ERROR_CODE_VALIDATE_APP_ID.getDescription(), applicationId), e);
            throw handleClientException(ERROR_CODE_VALIDATE_APP_ID, applicationId);
        }
    }

    private Optional<List<CORSOrigin>> getCorsOrigins(String orgId)
            throws OrganizationManagementException, CORSManagementServiceServerException {

        List<CORSOrigin> corsOrigins = getCORSOriginDAO().getCORSOriginsByTenantDomain(
                CORSManagementServiceHolder.getInstance().getOrganizationManager().resolveTenantDomain(orgId));
        return Optional.ofNullable(corsOrigins);
    }

    private List<CORSOrigin> mergeAndRemoveDuplicates(
            List<CORSOrigin> corsOrigins, List<CORSOrigin> newCorsOrigins) {

        Set<String> existingCorsOrigins = corsOrigins.stream()
                .map(CORSOrigin::getId)
                .collect(Collectors.toSet());
        List<CORSOrigin> mergedList = new ArrayList<>(corsOrigins);
        for (CORSOrigin corsOrigin : newCorsOrigins) {
            if (!existingCorsOrigins.contains(corsOrigin.getId())) {
                mergedList.add(corsOrigin);
            }
        }
        return mergedList;
    }
}
