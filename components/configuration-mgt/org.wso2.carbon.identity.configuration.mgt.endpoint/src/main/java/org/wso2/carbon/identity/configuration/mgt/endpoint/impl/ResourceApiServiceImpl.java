/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.configuration.mgt.endpoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.endpoint.ResourceApiService;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceAddDTO;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_PATH;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getAttributeDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getAttributeFromDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getConfigurationManager;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getResourceAddFromDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getResourceDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleBadRequestResponse;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleServerErrorResponse;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleUnexpectedServerError;

public class ResourceApiServiceImpl extends ResourceApiService {

    private static final Log LOG = LogFactory.getLog(ResourceApiServiceImpl.class);

    @Override
    public Response resourceResourceTypePost(String resourceType, ResourceAddDTO resourceAddDTO) {

        try {
            Resource resource = getConfigurationManager()
                    .addResource(resourceType, getResourceAddFromDTO(resourceAddDTO));
            return Response.created(getResourceURI(resourceType, resource)).entity(getResourceDTO(resource)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypePut(String resourceType, ResourceAddDTO resourceAddDTO) {

        try {
            Resource resource = getConfigurationManager()
                    .replaceResource(resourceType, getResourceAddFromDTO(resourceAddDTO));
            return Response.ok().entity(getResourceDTO(resource)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypeResourceNameAttributeKeyDelete(
            String resourceName, String resourceType, String attributeKey) {

        try {
            getConfigurationManager().deleteAttribute(resourceType, resourceName, attributeKey);
            return Response.ok().build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypeResourceNameAttributeKeyGet(String resourceName, String resourceType,
                                                                    String attributeKey) {

        try {
            Attribute attribute = getConfigurationManager().getAttribute(resourceType, resourceName, attributeKey);
            return Response.ok().entity(getAttributeDTO(attribute)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypeResourceNameDelete(String resourceName, String resourceType) {

        try {
            getConfigurationManager().deleteResource(resourceType, resourceName);
            return Response.ok().build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypeResourceNameGet(String resourceName, String resourceType) {

        try {
            Resource resource = getConfigurationManager().getResource(resourceType, resourceName);
            return Response.ok().entity(getResourceDTO(resource)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypeResourceNamePost(
            String resourceName, String resourceType, AttributeDTO attributeDTO) {

        try {
            Attribute attribute = getConfigurationManager().addAttribute(
                    resourceType, resourceName, getAttributeFromDTO(attributeDTO));
            return Response.created(getAttributeLocationURI(resourceType, resourceName, attribute)).entity(
                    getAttributeDTO(attribute)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceResourceTypeResourceNamePut(
            String resourceName, String resourceType, AttributeDTO attributeDTO) {

        try {
            Attribute attribute = getConfigurationManager().replaceAttribute(
                    resourceType, resourceName, getAttributeFromDTO(attributeDTO));
            return Response.ok().entity(getAttributeDTO(attribute)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    private URI getResourceURI(String resourceType, Resource resource) throws URISyntaxException {

        return new URI(RESOURCE_PATH + '/' + resourceType + '/' + resource.getResourceId());
    }

    private URI getAttributeLocationURI(String resourceType, String resourceName, Attribute attribute)
            throws URISyntaxException {

        return new URI(RESOURCE_PATH + '/' + resourceType + '/' + resourceName + '/' + attribute.getAttributeId());
    }
}
