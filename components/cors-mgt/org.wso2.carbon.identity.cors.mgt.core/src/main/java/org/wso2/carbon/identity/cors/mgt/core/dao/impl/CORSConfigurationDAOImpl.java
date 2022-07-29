/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSConfigurationDAO;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.CORSConfigurationToResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.internal.function.ResourceToCORSConfiguration;
import org.wso2.carbon.identity.cors.mgt.core.internal.util.CORSConfigurationUtils;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_CONFIG_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_CONFIG_SET;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_CONFIGURATION_RESOURCE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_CONFIGURATION_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.util.ErrorUtils.handleServerException;

/**
 * {@link CORSConfigurationDAO} implementation.
 */
public class CORSConfigurationDAOImpl implements CORSConfigurationDAO {

    private static final Log log = LogFactory.getLog(CORSConfigurationDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {

        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CORSConfiguration getCORSConfigurationByTenantDomain(String tenantDomain)
            throws CORSManagementServiceServerException {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCORSConfigurationByTenantDomain(CORSConfiguration corsConfiguration, String tenantDomain)
            throws CORSManagementServiceServerException {

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
}
