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

package org.wso2.carbon.identity.flow.mgt.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.flow.mgt.model.FlowConfigDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.FLOW_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.IS_ENABLED;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.RESOURCE_NAME_PREFIX;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.RESOURCE_TYPE;

/**
 * Utility class for managing flow configurations.
 */
public class FlowMgtConfigUtils {

    private static final Log LOG = LogFactory.getLog(FlowMgtConfigUtils.class);

    private FlowMgtConfigUtils() {

    }

    private static ConfigurationManager getConfigurationManager() {

        return FlowMgtServiceDataHolder.getInstance().getConfigurationManager();
    }

    /**
     * Add or update the flow configuration.
     *
     * @param flowConfigDTO The flow configuration to be added or updated.
     * @param tenantDomain  The tenant domain.
     * @return FlowConfigDTO
     * @throws FlowMgtServerException If an error occurs while adding or updating the flow configuration.
     */
    public static FlowConfigDTO addFlowConfig(FlowConfigDTO flowConfigDTO, String tenantDomain) throws FlowMgtServerException {

        String resourceName = RESOURCE_NAME_PREFIX + flowConfigDTO.getFlowType();
        Resource existingResource = getResource(resourceName, tenantDomain);
        Resource newResource = buildResourceFromFlowConfig(flowConfigDTO);

        Resource updatedResource;
        if (existingResource == null ||
                (tenantDomain != null && !tenantDomain.equals(existingResource.getTenantDomain()))) {
            LOG.debug("Adding new flow configuration for flow type: " + flowConfigDTO.getFlowType());
            updatedResource = addResource(newResource, tenantDomain);
        } else {
            LOG.debug("Updating existing flow configuration for flow type: " + flowConfigDTO.getFlowType());
            newResource.setResourceName(existingResource.getResourceName());
            updatedResource = updateResource(newResource, tenantDomain);
        }
        return buildAndPopulateFlowConfigFromResource(updatedResource);
    }

    /**
     * Get the flow configuration for the given flow type and tenant domain.
     *
     * @param flowType     The type of the flow.
     * @param tenantDomain The tenant domain.
     * @return FlowConfigDTO
     * @throws FlowMgtServerException If an error occurs while retrieving the flow configuration.
     */
    public static FlowConfigDTO getFlowConfig(String flowType, String tenantDomain) throws FlowMgtServerException {

        return buildAndPopulateFlowConfigFromResource(flowType, tenantDomain);
    }

    /**
     * Get all flow configurations for the given tenant domain.
     *
     * @param tenantDomain The tenant domain.
     * @return List of FlowConfigDTO
     * @throws FlowMgtServerException If an error occurs while retrieving the flow configurations.
     */
    public static List<FlowConfigDTO> getFlowConfigs(String tenantDomain) throws FlowMgtServerException {

        try {
            List<FlowConfigDTO> flowMgtConfigs = new ArrayList<>();
            Resources resources = getConfigurationManager().getResourcesByType(RESOURCE_TYPE);
            if (resources == null || CollectionUtils.isEmpty(resources.getResources())) {
                return getDefaultFlowConfigs();
            }
            resources.getResources()
                    .forEach(resource -> {
                        FlowConfigDTO flowConfigDTO = buildAndPopulateFlowConfigFromResource(resource);
                        flowMgtConfigs.add(flowConfigDTO);
                    });
            Arrays.stream(Constants.FlowTypes.values()).map(
                    Constants.FlowTypes::getType).forEach(flowType -> {
                if (flowMgtConfigs.stream().noneMatch(config -> config.getFlowType().equals(flowType))) {
                    FlowConfigDTO defaultConfig = getDefaultConfig(flowType);
                    flowMgtConfigs.add(defaultConfig);
                }
            });
            return flowMgtConfigs;
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) &&
                    !ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw FlowMgtUtils.handleServerException(Constants.ErrorMessages.ERROR_CODE_GETTING_FLOW_CONFIG, e,
                        tenantDomain);
            }
        }
        return getDefaultFlowConfigs();
    }

    /**
     * Get the default flow configurations.
     *
     * @return FlowMgtConfig
     */
    private static List<FlowConfigDTO> getDefaultFlowConfigs() {

        List<FlowConfigDTO> flowConfigDTOS = new ArrayList<>();
        Arrays.stream(Constants.FlowTypes.values()).map(
                Constants.FlowTypes::getType).forEach(flowType -> {
            FlowConfigDTO flowConfigDTO = getDefaultConfig(flowType);
            flowConfigDTOS.add(flowConfigDTO);
        });
        return flowConfigDTOS;
    }

    /**
     * Build a flow configuration from the given flow type and tenant domain.
     *
     * @param flowType     The type of the flow.
     * @param tenantDomain The tenant domain.
     * @return FlowConfigDTO
     * @throws FlowMgtServerException If an error occurs while retrieving the flow configuration.
     */
    private static FlowConfigDTO buildAndPopulateFlowConfigFromResource(String flowType, String tenantDomain)
            throws FlowMgtServerException {

        String resourceName = RESOURCE_NAME_PREFIX + flowType;
        Resource resource = getResource(resourceName, tenantDomain);
        return buildAndPopulateFlowConfigFromResource(flowType, resource);
    }

    /**
     * Set default configuration for the flow type.
     *
     * @param flowType The type of the flow.
     * @return FlowConfigDTO
     */
    private static FlowConfigDTO getDefaultConfig(String flowType) {

        LOG.debug("Returning default flow configuration for flow type: " + flowType);
        Constants.FlowTypes requestedFlowType = Constants.FlowTypes.valueOf(flowType);
        FlowConfigDTO flowConfigDTO = new FlowConfigDTO();
        flowConfigDTO.setFlowType(flowType);
        flowConfigDTO.setIsEnabled(false);
        flowConfigDTO.addAllFlowCompletionConfigs(requestedFlowType.getSupportedFlowCompletionConfigs());
        return flowConfigDTO;
    }

    /**
     * Build a flow configuration from the given resource.
     *
     * @param resource The resource containing flow configuration attributes.
     * @return FlowConfigDTO
     */
    private static FlowConfigDTO buildAndPopulateFlowConfigFromResource(String flowType, Resource resource) {

        if (resource == null || CollectionUtils.isEmpty(resource.getAttributes())) {
            return getDefaultConfig(flowType);
        }
        return buildAndPopulateFlowConfigFromResource(resource);
    }

    private static FlowConfigDTO buildAndPopulateFlowConfigFromResource(Resource resource) {

        List<Attribute> attributes = resource.getAttributes();
        FlowConfigDTO flowConfigDTO = new FlowConfigDTO();
        Constants.FlowTypes flowType = null;
        for (Attribute attribute : attributes) {
            switch (attribute.getKey()) {
                case FLOW_TYPE:
                    flowConfigDTO.setFlowType(attribute.getValue());
                    flowType = Constants.FlowTypes.valueOf(attribute.getValue());
                    break;
                case IS_ENABLED:
                    flowConfigDTO.setIsEnabled(Boolean.parseBoolean(attribute.getValue()));
                    break;
                default:
                    flowConfigDTO.addFlowCompletionConfig(Constants.FlowCompletionConfig.fromConfig(attribute.getKey()),
                            attribute.getValue());
                    break;
            }
        }

        // Add any missing supported configs with default values.
        if (flowType != null) {
            for (Constants.FlowCompletionConfig config : flowType.getSupportedFlowCompletionConfigs()) {
                if (!flowConfigDTO.isFlowCompletionConfigPresent(config)) {
                    flowConfigDTO.addFlowCompletionConfig(config, config.getDefaultValue());
                }
            }
        }
        return flowConfigDTO;
    }

    /**
     * Build a resource from the given flow configuration.
     *
     * @param flowConfigDTO The flow configuration.
     * @return The built resource.
     */
    private static Resource buildResourceFromFlowConfig(FlowConfigDTO flowConfigDTO) {

        String flowType = flowConfigDTO.getFlowType();
        Attribute flowTypeAttribute = new Attribute(FLOW_TYPE, flowType);
        Attribute flowIsEnabledAttribute = new Attribute(IS_ENABLED, String.valueOf(flowConfigDTO.getIsEnabled()));
        Map<Constants.FlowCompletionConfig, String> supportedConfigs = flowConfigDTO.getFlowCompletionConfigs(
                Constants.FlowTypes.valueOf(flowType).getSupportedFlowCompletionConfigs());

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(flowTypeAttribute);
        attributes.add(flowIsEnabledAttribute);

        // Add any additional supported configs as attributes.
        if (!supportedConfigs.isEmpty()) {
            supportedConfigs.forEach((key, value) -> {
                Attribute attribute = new Attribute(key.getConfig(), value);
                attributes.add(attribute);
            });
        }

        Resource resource = new Resource();
        resource.setResourceType(RESOURCE_TYPE);
        resource.setResourceName(RESOURCE_NAME_PREFIX + flowConfigDTO.getFlowType());
        resource.setAttributes(attributes);
        return resource;
    }

    private static Resource getResource(String resourceName, String tenantDomain) throws FlowMgtServerException {

        Resource resource = null;
        try {
            resource = getConfigurationManager().getResource(RESOURCE_TYPE, resourceName, true);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) &&
                    !ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw FlowMgtUtils.handleServerException(Constants.ErrorMessages.ERROR_CODE_GETTING_FLOW_CONFIG, e,
                        tenantDomain);
            }
        }
        return resource;
    }

    /**
     * Add a new resource to the configuration management.
     *
     * @param newResource  The new resource to be added.
     * @param tenantDomain The tenant domain.
     * @return The added resource.
     * @throws FlowMgtServerException If an error occurs while adding the resource.
     */
    private static Resource addResource(Resource newResource, String tenantDomain) throws FlowMgtServerException {

        try {
            return getConfigurationManager().addResource(RESOURCE_TYPE, newResource);
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()
                    .equals(e.getErrorCode())) {
                // If the insert is failing due to the relevant resource-type is not existing in the database, create
                // the resource-type and retry the configuration addition.
                createResourceType(tenantDomain);
                return addResource(newResource, tenantDomain);
            }
            throw FlowMgtUtils.handleServerException(Constants.ErrorMessages.ERROR_CODE_ADDING_FLOW_CONFIG, e,
                    tenantDomain);
        }
    }

    private static void createResourceType(String tenantDomain) throws FlowMgtServerException {

        try {
            ResourceTypeAdd resourceType = new ResourceTypeAdd();
            resourceType.setName(RESOURCE_TYPE);
            resourceType.setDescription("Resource type for " + RESOURCE_TYPE);
            getConfigurationManager().addResourceType(resourceType);
        } catch (ConfigurationManagementException e) {
            throw FlowMgtUtils.handleServerException(Constants.ErrorMessages.ERROR_CODE_ADDING_FLOW_CONFIG, e,
                    tenantDomain);
        }
    }

    /**
     * Update an existing resource in the configuration management.
     *
     * @param newResource  The new resource to be updated.
     * @param tenantDomain The tenant domain.
     * @return The updated resource.
     * @throws FlowMgtServerException If an error occurs while updating the resource.
     */
    private static Resource updateResource(Resource newResource, String tenantDomain) throws FlowMgtServerException {

        Resource updatedResource;
        try {
            updatedResource = getConfigurationManager().replaceResource(RESOURCE_TYPE, newResource);
        } catch (ConfigurationManagementException e) {
            throw FlowMgtUtils.handleServerException(Constants.ErrorMessages.ERROR_CODE_UPDATING_FLOW_CONFIG, e,
                    tenantDomain);
        }
        return updatedResource;
    }
}
