/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.template.mgt.endpoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.endpoint.TemplatesApiService;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.AddTemplateResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.GetTemplatesResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateRequestDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.UpdateSuccessResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.UpdateTemplateRequestDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.util.TemplateEndpointUtils;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_NO_AUTH_USER_FOUND;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_ALREADY_EXIST;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_ID_INVALID;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NAME_INVALID;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TENANT_ID_INVALID;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_UNEXPECTED;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED;

public class
TemplatesApiServiceImpl extends TemplatesApiService {

    private static final Log LOG = LogFactory.getLog(TemplatesApiServiceImpl.class);

    @Override
    public Response addTemplate(TemplateRequestDTO template) {

        try {
            AddTemplateResponseDTO response = postTemplate(template);
            return Response.ok()
                    .entity(response)
                    .location(getTemplateLocationURI(response))
                    .build();
        } catch (TemplateManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (TemplateManagementException e) {
            return handleServerErrorResponse(e);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable);
        }
    }

    @Override
    public Response updateTemplate(String templateName, UpdateTemplateRequestDTO updateTemplateRequestDTO) {

        try {
            UpdateSuccessResponseDTO response = putTemplate(templateName, updateTemplateRequestDTO);
            return Response.ok()
                    .entity(response)
                    .location(getUpdatedTemplateLocationURI(response))
                    .build();
        } catch (TemplateManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (TemplateManagementException e) {
            return handleServerErrorResponse(e);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable);
        }
    }

    @Override
    public Response getTemplateByName(String templateName) {

        try {
            Template template = getTemplate(templateName);
            return Response.ok()
                    .entity(template)
                    .build();
        } catch (TemplateManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (TemplateManagementException e) {
            return handleServerErrorResponse(e);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable);
        }
    }

    @Override
    public Response deleteTemplate(String templateName) {

        try {
            TemplateEndpointUtils.getTemplateManager().deleteTemplate(templateName);
            return Response.ok()
                    .build();
        } catch (TemplateManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (TemplateManagementException e) {
            return handleServerErrorResponse(e);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable);
        }
    }

    @Override
    public Response getTemplates(Integer limit, Integer offset) {

        try {
            List<GetTemplatesResponseDTO> getTemplatesResponseDTOS = getTemplatesList(limit, offset);
            return Response.ok()
                    .entity(getTemplatesResponseDTOS)
                    .build();
        } catch (TemplateManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (TemplateManagementException e) {
            return handleServerErrorResponse(e);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable);
        }
    }

    private List<GetTemplatesResponseDTO> getTemplatesList(Integer limit, Integer offset)
            throws TemplateManagementException {

        if (limit == null) {
            limit = 0;
        }

        if (offset == null) {
            offset = 0;
        }
        List<TemplateInfo> templates = TemplateEndpointUtils.getTemplateManager().listTemplates(limit, offset);
        return TemplateEndpointUtils.getTemplatesResponseDTOList(templates);
    }

    private AddTemplateResponseDTO postTemplate(TemplateRequestDTO templateDTO) throws TemplateManagementException {

        Template templateRequest = TemplateEndpointUtils.getTemplateRequest(templateDTO);
        TemplateInfo templateResponse = TemplateEndpointUtils.getTemplateManager().addTemplate(templateRequest);

        AddTemplateResponseDTO responseDTO = new AddTemplateResponseDTO();
        responseDTO.setTenantId(templateResponse.getTenantId().toString());
        responseDTO.setName(templateResponse.getTemplateName());
        return responseDTO;
    }

    private UpdateSuccessResponseDTO putTemplate(String templateName, UpdateTemplateRequestDTO updateTemplateRequestDTO)
            throws TemplateManagementException {

        Template updateTemplateRequest = TemplateEndpointUtils.getTemplateUpdateRequest(updateTemplateRequestDTO);
        TemplateInfo updateTemplateResponse = TemplateEndpointUtils.getTemplateManager()
                                                                   .updateTemplate(templateName, updateTemplateRequest);

        UpdateSuccessResponseDTO responseDTO = new UpdateSuccessResponseDTO();
        responseDTO.setName(updateTemplateResponse.getTemplateName());
        responseDTO.setTenantId(updateTemplateResponse.getTenantId().toString());
        return responseDTO;
    }

    private Template getTemplate(String templateName) throws TemplateManagementException {

        Template getTemplateResponse = TemplateEndpointUtils.getTemplateManager().getTemplateByName(templateName);
        return getTemplateResponse;
    }

    private URI getTemplateLocationURI(AddTemplateResponseDTO response) throws URISyntaxException {

        return new URI(TemplateMgtConstants.TEMPLATE_RESOURCE_PATH + response.getTenantId());
    }

    private URI getUpdatedTemplateLocationURI(UpdateSuccessResponseDTO response) throws URISyntaxException {

        return new URI(TemplateMgtConstants.TEMPLATE_RESOURCE_PATH + response.getTenantId());
    }

    private Response handleBadRequestResponse(TemplateManagementClientException e) {

        if (isConflictError(e)) {
            throw TemplateEndpointUtils.buildConflictRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
        }
        if (isForbiddenError(e)) {
            throw TemplateEndpointUtils.buildForbiddenException(e.getMessage(), e.getErrorCode(), LOG, e);
        }
        if (isNotFoundError(e)) {
            throw TemplateEndpointUtils.buildNotFoundRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
        }
        throw TemplateEndpointUtils.buildBadRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
    }

    private boolean isForbiddenError(TemplateManagementClientException e) {

        return ERROR_CODE_NO_AUTH_USER_FOUND.getCode().equals(e.getErrorCode()) ||
                                                                ERROR_CODE_USER_NOT_AUTHORIZED.getCode()
                                                                .equals(e.getErrorCode());
    }

    private boolean isNotFoundError(TemplateManagementClientException e) {

        return ERROR_CODE_TEMPLATE_ID_INVALID.getCode().equals(e.getErrorCode())
                || ERROR_CODE_TENANT_ID_INVALID.getCode().equals(e.getErrorCode())
                || ERROR_CODE_TEMPLATE_NAME_INVALID.getCode().equals(e.getErrorCode());
    }

    private boolean isConflictError(TemplateManagementClientException e) {

        return ERROR_CODE_TEMPLATE_ALREADY_EXIST.getCode().equals(e.getErrorCode());
    }

    private Response handleServerErrorResponse(TemplateManagementException e) {

        throw TemplateEndpointUtils.buildInternalServerErrorException(e.getErrorCode(), LOG, e);
    }

    private Response handleUnexpectedServerError(Throwable e) {

        throw TemplateEndpointUtils.buildInternalServerErrorException(ERROR_CODE_UNEXPECTED.getCode(), LOG, e);
    }
}
