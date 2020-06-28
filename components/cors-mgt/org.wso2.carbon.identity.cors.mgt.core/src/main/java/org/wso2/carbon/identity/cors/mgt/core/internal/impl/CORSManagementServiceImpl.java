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
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;
import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.CORSConfigurationToResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.CORSOriginToResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.ResourceToCORSConfiguration;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.ResourceToCORSOrigin;
import org.wso2.carbon.identity.cors.mgt.core.internal.util.CORSConfigurationUtils;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;
import org.wso2.carbon.identity.cors.mgt.core.model.ValidatedOrigin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_ADD;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_CONFIG_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_CONFIG_SET;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_DELETE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_SET;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_ORIGIN_NOT_PRESENT;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_ORIGIN_PRESENT;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_VALIDATE_APP_ID;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_CONFIGURATION_RESOURCE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_CONFIGURATION_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_TYPE_NAME;

/**
 * Implementation of the CORSService.
 */
public class CORSManagementServiceImpl implements CORSManagementService {

    private static final Log log = LogFactory.getLog(CORSManagementServiceImpl.class);

    @Override
    public List<CORSOrigin> getCORSOrigins(String tenantDomain) throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);

        List<CORSOrigin> corsOrigins;
        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            Resources resources = getResources(CORS_ORIGIN_RESOURCE_TYPE_NAME);
            corsOrigins = resources.getResources().stream().map(new ResourceToCORSOrigin())
                    .collect(Collectors.toList());
            return Collections.unmodifiableList(corsOrigins);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_CORS_RETRIEVE, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void setCORSOrigins(String tenantDomain, String appId, List<String> origins)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        validateApplicationId(tenantDomain, appId);

        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            // Convert Origins to ValidatedOrigins.
            List<ValidatedOrigin> validatedOrigins = new ArrayList<>();
            for (String origin : origins) {
                validatedOrigins.add(new ValidatedOrigin(origin));
            }

            // Delete all CORS origins from the tenant.
            for (ValidatedOrigin validatedOrigin : validatedOrigins) {
                Resource resource = getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, validatedOrigin.getValue());
                if (resource != null) {
                    CORSOrigin corsOrigin = new ResourceToCORSOrigin().apply(resource);
                    getConfigurationManager().deleteResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, corsOrigin.getOrigin());
                }
            }

            // Save ValidatedOrigins in the Configuration Management store.
            for (ValidatedOrigin validatedOrigin : validatedOrigins) {
                CORSOrigin corsOrigin = new CORSOrigin();
                corsOrigin.setOrigin(validatedOrigin.getValue());
                // Set the appId only if not null. If null then the origin belongs to the tenant rather than an app.
                if (StringUtils.isNotBlank(appId)) {
                    corsOrigin.setAppIds(Collections.singleton(appId));
                }
                ResourceAdd resourceAdd = new CORSOriginToResourceAdd().apply(corsOrigin);
                getConfigurationManager().addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, resourceAdd);
            }
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_CORS_SET, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void addCORSOrigins(String tenantDomain, String appId, List<String> origins)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        validateApplicationId(tenantDomain, appId);

        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            // Convert Origins to ValidatedOrigins.
            List<ValidatedOrigin> validatedOrigins = new ArrayList<>();
            for (String origin : origins) {
                validatedOrigins.add(new ValidatedOrigin(origin));
            }

            // Check if origins are present.
            for (ValidatedOrigin validatedOrigin : validatedOrigins) {
                Resource resource = getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, validatedOrigin.getValue());
                if (resource != null) {
                    // Origin is present.
                    CORSOrigin corsOrigin = new ResourceToCORSOrigin().apply(resource);
                    if (corsOrigin.getAppIds().contains(appId)) {
                        // Origin is already registered for the appId.
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(ERROR_CODE_ORIGIN_PRESENT.getMessage(), tenantDomain,
                                    validatedOrigin));
                        }
                        throw handleClientException(ERROR_CODE_ORIGIN_PRESENT, tenantDomain,
                                validatedOrigin.getValue());
                    }
                }
            }

            // Add CORS origins to the application.
            for (ValidatedOrigin validatedOrigin : validatedOrigins) {
                Resource resource = getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, validatedOrigin.getValue());
                if (resource == null) {
                    // Resource is null. Set the new origin with the given appId as the only value.
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setOrigin(validatedOrigin.getValue());
                    // Set the appId only if not null. If null then the origin belongs to the tenant rather than an app.
                    if (StringUtils.isNotBlank(appId)) {
                        corsOrigin.setAppIds(Collections.singleton(appId));
                    }
                    ResourceAdd resourceAdd = new CORSOriginToResourceAdd().apply(corsOrigin);
                    getConfigurationManager().addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, resourceAdd);
                } else {
                    Attribute attribute = new Attribute(appId, "");
                    getConfigurationManager().addAttribute(CORS_ORIGIN_RESOURCE_TYPE_NAME, validatedOrigin.getValue(),
                            attribute);
                }
            }
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void deleteCORSOrigins(String tenantDomain, String appId, List<String> originIds)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        validateApplicationId(tenantDomain, appId);

        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            // Check if origins are not present
            for (String originID : originIds) {
                Resource resource = getResourceById(originID);
                if (resource != null) {
                    // Origin is present.
                    CORSOrigin corsOrigin = new ResourceToCORSOrigin().apply(resource);
                    if (!corsOrigin.getAppIds().contains(appId)) {
                        // Origin is not registered for the appId.
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(ERROR_CODE_ORIGIN_NOT_PRESENT.getMessage(), tenantDomain,
                                    originID));
                        }
                        throw handleClientException(ERROR_CODE_ORIGIN_NOT_PRESENT, tenantDomain,
                                originID);
                    }
                } else {
                    // Origin is not present.
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(ERROR_CODE_ORIGIN_NOT_PRESENT.getMessage(), tenantDomain,
                                originID));
                    }
                    throw handleClientException(ERROR_CODE_ORIGIN_NOT_PRESENT, tenantDomain,
                            originID);
                }
            }

            // Delete CORS origins from the application.
            for (String originID : originIds) {
                Resource resource = getResourceById(originID);
                getConfigurationManager().deleteAttribute(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                        resource.getResourceName(), appId);
                resource = getResourceById(originID);
                // Delete the resource entirely if there are no attributes to it now.
                if (resource.getAttributes().isEmpty()) {
                    getConfigurationManager().deleteResource(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                            resource.getResourceName());
                }
            }
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_CORS_DELETE, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public CORSConfiguration getCORSConfiguration(String tenantDomain) throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);

        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            Resource resource = getResource(CORS_CONFIGURATION_RESOURCE_TYPE_NAME, CORS_CONFIGURATION_RESOURCE_NAME);
            CORSConfiguration corsConfiguration;
            if (resource == null) {
                corsConfiguration = CORSConfigurationUtils.getServerCORSConfiguration();
            } else {
                corsConfiguration = new ResourceToCORSConfiguration().apply(resource);
            }
            return corsConfiguration;
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_CORS_CONFIG_RETRIEVE, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void setCORSConfiguration(String tenantDomain, CORSConfiguration corsConfiguration)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);

        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            ResourceAdd resourceAdd = new CORSConfigurationToResourceAdd().apply(corsConfiguration);
            getConfigurationManager().replaceResource(CORS_CONFIGURATION_RESOURCE_TYPE_NAME, resourceAdd);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_CORS_CONFIG_SET, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    /**
     * Retrieve the ConfigurationManager instance from the CORSServiceHolder.
     *
     * @return ConfigurationManager The ConfigurationManager instance.
     */
    private ConfigurationManager getConfigurationManager() {

        return CORSManagementServiceHolder.getInstance().getConfigurationManager();
    }

    /**
     * Configuration Management API returns a ConfigurationManagementException with the error code CONFIGM_00017 when
     * resource is not found. This method wraps the original method and returns null if the resource is not found.
     *
     * @param resourceTypeName Resource type name.
     * @param resourceName     Resource name.
     * @return Retrieved resource from the configuration store. Returns {@code null} if the resource is not found.
     * @throws ConfigurationManagementException
     */
    private Resource getResource(String resourceTypeName, String resourceName) throws ConfigurationManagementException {

        try {
            return getConfigurationManager().getResource(resourceTypeName, resourceName);
        } catch (ConfigurationManagementException e) {
            if (e.getErrorCode().equals(ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode())) {
                return null;
            } else {
                throw e;
            }
        }
    }

    /**
     * Configuration Management API returns a ConfigurationManagementException with the error code CONFIGM_00017 when
     * resource is not found. This method wraps the original method and returns null if the resource is not found.
     *
     * @param resourceId Resource ID.
     * @return Retrieved resource from the configuration store. Returns {@code null} if the resource is not found.
     * @throws ConfigurationManagementException
     */
    private Resource getResourceById(String resourceId) throws ConfigurationManagementException {

        try {
            return getConfigurationManager().getTenantResourceById(resourceId);
        } catch (ConfigurationManagementException e) {
            if (e.getErrorCode().equals(ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode())) {
                return null;
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns the resources of a tenant with the given type.
     *
     * @param resourceTypeName
     * @return Returns an instance of {@code Resources} with the resources of given type.
     * @throws ConfigurationManagementException
     */
    private Resources getResources(String resourceTypeName) throws ConfigurationManagementException {

        return getConfigurationManager().getResourcesByType(resourceTypeName);
    }

    /**
     * Validate the tenant domain.
     *
     * @param tenantDomain The tenant domain.
     * @throws CORSManagementServiceClientException
     */
    private void validateTenantDomain(String tenantDomain) throws CORSManagementServiceClientException {

        if (IdentityTenantUtil.getTenantId(tenantDomain) == MultitenantConstants.INVALID_TENANT_ID) {
            throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_TENANT_DOMAIN, tenantDomain);
        }
    }

    /**
     * Validate the application ID.
     *
     * @param tenantDomain The tenant domain.
     * @param appId        The application ID.
     */
    private void validateApplicationId(String tenantDomain, String appId) throws CORSManagementServiceClientException {

        // If the appId is blank then the CORS origins that should be set/add/delete belong in the tenant level.
        if (StringUtils.isBlank(appId)) {
            return;
        }

        // Check whether the appId belongs to the tenant with the tenantDomain.
        try {
            ApplicationBasicInfo applicationBasicInfo = ApplicationManagementService.getInstance()
                    .getApplicationBasicInfoByResourceId(appId, tenantDomain);
            if (applicationBasicInfo == null) {
                throw handleClientException(ErrorMessages.ERROR_CODE_INVALID_APP_ID, appId);
            }
        } catch (IdentityApplicationManagementException e) {
            // Something else happened.
            log.error(String.format(ERROR_CODE_VALIDATE_APP_ID.getDescription(), appId), e);
        }
    }

    /**
     * Handle server exceptions.
     *
     * @param error The ErrorMessage.
     * @param e     Original error.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceServerException instance.
     */
    private CORSManagementServiceServerException handleServerException(ErrorMessages error, Throwable e,
                                                                       String... data) {

        return new CORSManagementServiceServerException(error.getCode(), String.format(error.getDescription(),
                data), e);
    }

    /**
     * Handle client exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceClientException instance.
     */
    private CORSManagementServiceClientException handleClientException(ErrorMessages error, String... data) {

        return new CORSManagementServiceClientException(error.getCode(), String.format(error.getDescription(), data));
    }
}
