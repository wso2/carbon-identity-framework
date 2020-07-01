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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;
import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.CORSOriginToAttribute;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.CORSOriginToResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.ResourceToCORSOrigin;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_ADD;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_DELETE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_SET;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_EMPTY_LIST;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_INVALID_ORIGIN;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_ORIGIN_NOT_PRESENT;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_ORIGIN_PRESENT;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_TYPE_NAME;

/**
 * Implementation of the CORSService.
 */
public class CORSManagementServiceImpl implements CORSManagementService {

    private static final Log log = LogFactory.getLog(CORSManagementServiceImpl.class);

    @Override
    public List<CORSOrigin> getCORSOrigins(String tenantDomain) throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            Resource resource = getConfigurationManager().getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                    CORS_ORIGIN_RESOURCE_NAME);
            List<CORSOrigin> corsOrigins;
            if (resource == null) {
                corsOrigins = new ArrayList<>();
            } else {
                corsOrigins = new ResourceToCORSOrigin().apply(resource);
            }

            return Collections.unmodifiableList(corsOrigins);
        } catch (ConfigurationManagementException | IOException e) {
            throw handleServerException(ERROR_CODE_CORS_RETRIEVE, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void setCORSOrigins(String tenantDomain, List<CORSOrigin> corsOrigins)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        validateOrigins(corsOrigins);
        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            ResourceAdd resourceAdd = new CORSOriginToResourceAdd().apply(corsOrigins);
            getConfigurationManager().replaceResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, resourceAdd);
        } catch (ConfigurationManagementException | JsonProcessingException e) {
            throw handleServerException(ERROR_CODE_CORS_SET, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void addCORSOrigins(String tenantDomain, List<CORSOrigin> corsOrigins)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        validateOrigins(corsOrigins);
        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            // Check if origins are present
            for (CORSOrigin corsOrigin : corsOrigins) {
                if (isDefinedCORSOriginResource(corsOrigin)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(String.format(ERROR_CODE_ORIGIN_PRESENT.getMessage(),
                                tenantDomain, corsOrigin)));
                    }
                    throw handleClientException(ERROR_CODE_ORIGIN_PRESENT, tenantDomain, corsOrigin.getUrl());
                }
            }

            for (CORSOrigin corsOrigin : corsOrigins) {
                Attribute attribute = new CORSOriginToAttribute().apply(corsOrigin);
                getConfigurationManager().addAttribute(CORS_ORIGIN_RESOURCE_TYPE_NAME, CORS_ORIGIN_RESOURCE_NAME,
                        attribute);
            }
        } catch (ConfigurationManagementException | IOException e) {
            throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    @Override
    public void deleteCORSOrigins(String tenantDomain, List<CORSOrigin> corsOrigins)
            throws CORSManagementServiceException {

        validateTenantDomain(tenantDomain);
        validateOrigins(corsOrigins);
        try {
            FrameworkUtils.startTenantFlow(tenantDomain);

            // Check if origins are not present
            for (CORSOrigin corsOrigin : corsOrigins) {
                if (!isDefinedCORSOriginResource(corsOrigin)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(String.format(ERROR_CODE_ORIGIN_NOT_PRESENT.getMessage(),
                                tenantDomain, corsOrigin)));
                    }
                    throw handleClientException(ERROR_CODE_ORIGIN_NOT_PRESENT, tenantDomain, corsOrigin.getUrl());
                }
            }

            for (CORSOrigin corsOrigin : corsOrigins) {
                Attribute attribute = new CORSOriginToAttribute().apply(corsOrigin);
                getConfigurationManager().deleteAttribute(CORS_ORIGIN_RESOURCE_TYPE_NAME, CORS_ORIGIN_RESOURCE_NAME,
                        attribute.getKey());
            }
        } catch (ConfigurationManagementException | IOException e) {
            throw handleServerException(ERROR_CODE_CORS_DELETE, e, tenantDomain);
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
     * Returns true if the tenant already has a particular CORS Origin.
     *
     * @param origin The Origin to be checked against the existing Origins.
     * @return {@code true} if the tenant already have the particular CORS Origin, {@code false} otherwise.
     * @throws ConfigurationManagementException
     */
    private boolean isDefinedCORSOriginResource(CORSOrigin origin) throws ConfigurationManagementException,
            IOException {

        Resource resource = getConfigurationManager().getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                CORS_ORIGIN_RESOURCE_NAME);
        if (resource != null) {
            List<CORSOrigin> currentCORSOrigins = new ResourceToCORSOrigin().apply(resource);

            return currentCORSOrigins.contains(origin);
        } else {
            return false;
        }
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
     * Validate the CORSOrigin list.
     *
     * @param corsOrigins List of CORSOrigin instances.
     * @throws CORSManagementServiceClientException
     */
    private void validateOrigins(List<CORSOrigin> corsOrigins) throws CORSManagementServiceClientException {

        if (corsOrigins == null) {
            if (log.isDebugEnabled()) {
                log.debug(ERROR_CODE_EMPTY_LIST.getMessage());
            }
            throw handleClientException(ERROR_CODE_EMPTY_LIST);
        }

        for (CORSOrigin corsOrigin : corsOrigins) {
            if (isInvalidOrigin(corsOrigin)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format(ERROR_CODE_INVALID_ORIGIN.getMessage(), corsOrigin.getUrl()));
                }
                throw handleClientException(ERROR_CODE_INVALID_ORIGIN, corsOrigin.getUrl());
            }
        }
    }

    /**
     * Check if the format of the Origin is valid.
     *
     * @param origin Origin to be checked for validity.
     * @return {@code true} if the origin is valid, {@code false} otherwise.
     */
    private boolean isInvalidOrigin(CORSOrigin origin) {

        try {
            new URL(origin.getUrl()).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return true;
        }
        return false;
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
