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
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.endpoint.ResourceTypeApiService;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeAddDTO;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_TYPE_PATH;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getConfigurationManager;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getResourceTypeAddFromDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getResourceTypeDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleBadRequestResponse;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleServerErrorResponse;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleUnexpectedServerError;

public class ResourceTypeApiServiceImpl extends ResourceTypeApiService {

    private static final Log LOG = LogFactory.getLog(ResourceTypeApiServiceImpl.class);

    @Override
    public Response resourceTypePost(ResourceTypeAddDTO resourceTypeAddDTO) {

        try {
            ResourceType resourceType = getConfigurationManager()
                    .addResourceType(getResourceTypeAddFromDTO(resourceTypeAddDTO));
            return Response.created(getResourceTypeURI(resourceType))
                    .entity(getResourceTypeDTO(resourceType)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceTypePut(ResourceTypeAddDTO resourceTypeAddDTO) {

        try {
            ResourceType resourceType =
                    getConfigurationManager().replaceResourceType(getResourceTypeAddFromDTO(resourceTypeAddDTO));
            return Response.ok().entity(getResourceTypeDTO(resourceType)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    @Override
    public Response resourceTypeResourceTypeNameDelete(String resourceTypeName) {

        try {
            getConfigurationManager().deleteResourceType(resourceTypeName);
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
    public Response resourceTypeResourceTypeNameGet(String resourceTypeName) {

        try {
            ResourceType resourceType = getConfigurationManager().getResourceType(resourceTypeName);
            return Response.ok().entity(getResourceTypeDTO(resourceType)).build();
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }

    private URI getResourceTypeURI(ResourceType resourceType) throws URISyntaxException {

        return new URI(RESOURCE_TYPE_PATH + '/' + resourceType.getId());
    }
}
