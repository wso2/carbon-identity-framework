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
package org.wso2.carbon.identity.fraud.detection.core.service;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants;
import org.wso2.carbon.identity.fraud.detection.core.exception.FraudDetectionConfigServerException;
import org.wso2.carbon.identity.fraud.detection.core.internal.IdentityFraudDetectionDataHolder;
import org.wso2.carbon.identity.fraud.detection.core.model.EventConfigDTO;
import org.wso2.carbon.identity.fraud.detection.core.model.FraudDetectionConfigDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ErrorMessages.ERROR_CODE_ADDING_FRAUD_DETECTOR_CONFIG;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ErrorMessages.ERROR_CODE_GETTING_FRAUD_DETECTOR_CONFIG;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.ErrorMessages.ERROR_CODE_UPDATING_FRAUD_DETECTOR_CONFIG;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.LOGIN_EVENT_PROP_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.LOGOUT_EVENT_PROP_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.LOG_REQUEST_PAYLOAD_ATTR_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.PUBLISH_DEVICE_METADATA_ATTR_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.PUBLISH_USER_INFO_ATTR_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RESOURCE_NAME;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.RESOURCE_TYPE;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.UPDATE_CREDENTIAL_EVENT_PROP_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.UPDATE_PROFILE_EVENT_PROP_KEY;
import static org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants.USER_REGISTRATION_EVENT_PROP_KEY;

/**
 * Service class for managing Fraud Detection configurations.
 */
public class FraudDetectionConfigsService {

    private static final List<String> allowedEvents = Arrays.asList(USER_REGISTRATION_EVENT_PROP_KEY,
            UPDATE_CREDENTIAL_EVENT_PROP_KEY, UPDATE_PROFILE_EVENT_PROP_KEY,
            NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY, LOGIN_EVENT_PROP_KEY, LOGOUT_EVENT_PROP_KEY);

    private static final Log LOG = LogFactory.getLog(FraudDetectionConfigsService.class);

    private static final FraudDetectionConfigsService instance = new FraudDetectionConfigsService();

    /**
     * Returns the singleton instance of FraudDetectionConfigsService.
     *
     * @return Instance of FraudDetectionConfigsService.
     */
    public static FraudDetectionConfigsService getInstance() {

        return instance;
    }

    /**
     * Private constructor to enforce singleton pattern.
     */
    private FraudDetectionConfigsService() {

    }

    /**
     * Retrieves the Fraud Detection configurations for the given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return FraudDetectionConfigDTO containing the configurations.
     * @throws FraudDetectionConfigServerException If an error occurs while retrieving the configurations.
     */
    public FraudDetectionConfigDTO getFraudDetectionConfigs(String tenantDomain)
            throws FraudDetectionConfigServerException {

        Resource resource = getResource(tenantDomain);
        return buildDTOFromResource(resource);
    }

    /**
     * Updates the Fraud Detection configurations for the given tenant domain.
     *
     * @param dto          FraudDetectionConfigDTO containing the new configurations.
     * @param tenantDomain Tenant domain.
     * @return Updated FraudDetectionConfigDTO.
     * @throws FraudDetectionConfigServerException If an error occurs while updating the configurations.
     */
    public FraudDetectionConfigDTO updateFraudDetectionConfigs(FraudDetectionConfigDTO dto, String tenantDomain)
            throws FraudDetectionConfigServerException {

        Resource existingResource = getResource(tenantDomain);
        Resource newResource = buildResourceFromDTO(dto);

        Resource updatedResource;
        if (existingResource == null
                || (StringUtils.isNotEmpty(tenantDomain) && !tenantDomain.equals(existingResource.getTenantDomain()))) {
            LOG.debug("Creating new Fraud Detection config resource for tenant: " + tenantDomain);
            updatedResource = addResource(newResource, tenantDomain);
        } else {
            LOG.debug("Updating existing Fraud Detection config resource for tenant: " + tenantDomain);
            newResource.setResourceName(existingResource.getResourceName());
            updatedResource = updateResource(newResource, tenantDomain);
        }
        return buildDTOFromResource(updatedResource);
    }

    /**
     * Retrieves the Resource object for the Fraud Detection configurations of the given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return Resource object containing the configurations.
     * @throws FraudDetectionConfigServerException If an error occurs while retrieving the resource.
     */
    private Resource getResource(String tenantDomain) throws FraudDetectionConfigServerException {

        Resource resource = null;
        try {
            resource = IdentityFraudDetectionDataHolder.getInstance().getConfigurationManager()
                    .getResource(RESOURCE_TYPE, RESOURCE_NAME, true);
        } catch (ConfigurationManagementException e) {
            if (!ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) &&
                    !ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                throw handleServerException(ERROR_CODE_GETTING_FRAUD_DETECTOR_CONFIG, e, tenantDomain);
            }
        }
        return resource;
    }

    /**
     * Adds a new Resource object for the Fraud Detection configurations of the given tenant domain.
     *
     * @param resource     Resource object to be added.
     * @param tenantDomain Tenant domain.
     * @return Added Resource object.
     * @throws FraudDetectionConfigServerException If an error occurs while adding the resource.
     */
    private Resource addResource(Resource resource, String tenantDomain)
            throws FraudDetectionConfigServerException {

        try {
            return IdentityFraudDetectionDataHolder.getInstance().getConfigurationManager()
                    .addResource(RESOURCE_TYPE, resource);
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()
                    .equals(e.getErrorCode())) {
                // If the insert is failing due to the relevant resource-type is not existing in the database, create
                // the resource-type and retry the configuration addition.
                createResourceType(tenantDomain);
                return addResource(resource, tenantDomain);
            }
            throw handleServerException(ERROR_CODE_ADDING_FRAUD_DETECTOR_CONFIG, e, tenantDomain);
        }
    }

    /**
     * Creates the Resource Type for Fraud Detection configurations.
     *
     * @param tenantDomain Tenant domain.
     * @throws FraudDetectionConfigServerException If an error occurs while creating the resource type.
     */
    private void createResourceType(String tenantDomain) throws FraudDetectionConfigServerException {

        try {
            ResourceTypeAdd resourceType = new ResourceTypeAdd();
            resourceType.setName(RESOURCE_TYPE);
            resourceType.setDescription("Resource type for " + RESOURCE_TYPE);
            IdentityFraudDetectionDataHolder.getInstance().getConfigurationManager().addResourceType(resourceType);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_ADDING_FRAUD_DETECTOR_CONFIG, e, tenantDomain);
        }
    }

    /**
     * Updates the existing Resource object for the Fraud Detection configurations of the given tenant domain.
     *
     * @param resource     Resource object to be updated.
     * @param tenantDomain Tenant domain.
     * @return Updated Resource object.
     * @throws FraudDetectionConfigServerException If an error occurs while updating the resource.
     */
    private Resource updateResource(Resource resource, String tenantDomain)
            throws FraudDetectionConfigServerException {

        try {
            return IdentityFraudDetectionDataHolder.getInstance().getConfigurationManager()
                    .replaceResource(RESOURCE_TYPE, resource);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_UPDATING_FRAUD_DETECTOR_CONFIG, e, tenantDomain);
        }
    }

    /**
     * Builds a FraudDetectionConfigDTO from the given Resource object.
     *
     * @param resource Resource object containing the configurations.
     * @return FraudDetectionConfigDTO.
     */
    private FraudDetectionConfigDTO buildDTOFromResource(Resource resource) {

        if (resource == null || resource.getAttributes() == null || resource.getAttributes().isEmpty()) {
            return buildDefaultConfigs();
        }

        Map<String, String> attributes = resource.getAttributes().stream()
                .collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
        FraudDetectionConfigDTO dto = new FraudDetectionConfigDTO();
        attributes.forEach((key, value) -> {
            if (key.equals(PUBLISH_USER_INFO_ATTR_KEY)) {
                dto.setPublishUserInfo(Boolean.parseBoolean(value));
            } else if (key.equals(PUBLISH_DEVICE_METADATA_ATTR_KEY)) {
                dto.setPublishDeviceMetadata(Boolean.parseBoolean(value));
            } else if (key.equals(LOG_REQUEST_PAYLOAD_ATTR_KEY)) {
                dto.setLogRequestPayload(Boolean.parseBoolean(value));
            } else {
                // Event specific configs.
                if (allowedEvents.contains(key)) {
                    EventConfigDTO eventConfigDTO = EventConfigDTO.fromJson(value) != null ?
                            EventConfigDTO.fromJson(value) : new EventConfigDTO(false);
                    dto.addEventConfig(key, eventConfigDTO);
                }
            }
        });

        return dto;
    }

    /**
     * Builds a Resource object from the given FraudDetectionConfigDTO.
     *
     * @param dto FraudDetectionConfigDTO containing the configurations.
     * @return Resource object.
     */
    private Resource buildResourceFromDTO(FraudDetectionConfigDTO dto) {

        Resource resource = new Resource();
        resource.setResourceType(RESOURCE_TYPE);
        resource.setResourceName(RESOURCE_NAME);

        Map<String, String> attributes = new HashMap<>();
        attributes.put(PUBLISH_USER_INFO_ATTR_KEY, String.valueOf(dto.isPublishUserInfo()));
        attributes.put(PUBLISH_DEVICE_METADATA_ATTR_KEY, String.valueOf(dto.isPublishDeviceMetadata()));
        attributes.put(LOG_REQUEST_PAYLOAD_ATTR_KEY, String.valueOf(dto.isLogRequestPayload()));
        dto.getEvents().forEach((key, value) -> attributes.put(key, value.toString()));

        List<Attribute> attributeList = attributes.entrySet().stream()
                .map(entry -> new Attribute(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        resource.setAttributes(attributeList);
        return resource;
    }

    /**
     * Builds default Fraud Detection configurations.
     *
     * @return FraudDetectionConfigDTO with default configurations.
     */
    private FraudDetectionConfigDTO buildDefaultConfigs() {

        FraudDetectionConfigDTO fraudDetectionConfigDTO = new FraudDetectionConfigDTO();
        fraudDetectionConfigDTO.setEvents(buildDefaultEventConfigs());
        return fraudDetectionConfigDTO;
    }

    /**
     * Builds default event configurations with all events disabled.
     *
     * @return Map of event keys to EventConfigDTOs.
     */
    private Map<String, EventConfigDTO> buildDefaultEventConfigs() {

        Map<String, EventConfigDTO> eventConfigMap = new HashMap<>();
        eventConfigMap.put(USER_REGISTRATION_EVENT_PROP_KEY, new EventConfigDTO(false));
        eventConfigMap.put(UPDATE_CREDENTIAL_EVENT_PROP_KEY, new EventConfigDTO(false));
        eventConfigMap.put(UPDATE_PROFILE_EVENT_PROP_KEY, new EventConfigDTO(false));
        eventConfigMap.put(NOTIFICATION_BASED_VERIFICATION_EVENT_PROP_KEY, new EventConfigDTO(false));
        eventConfigMap.put(LOGIN_EVENT_PROP_KEY, new EventConfigDTO(false));
        eventConfigMap.put(LOGOUT_EVENT_PROP_KEY, new EventConfigDTO(false));

        return eventConfigMap;
    }

    /**
     * Handles server exceptions by creating a FraudDetectionConfigServerException with appropriate error code
     * and description.
     *
     * @param error Error message enum.
     * @param e     Original throwable.
     * @param data  Additional data for formatting the error description.
     * @return FraudDetectionConfigServerException.
     */
    private FraudDetectionConfigServerException handleServerException(FraudDetectionConstants.ErrorMessages error,
                                                                      Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FraudDetectionConfigServerException(error.getCode(), description, e);
    }
}
