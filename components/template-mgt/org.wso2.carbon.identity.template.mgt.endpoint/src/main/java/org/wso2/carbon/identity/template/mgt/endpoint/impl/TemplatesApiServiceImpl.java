
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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.endpoint.TemplatesApiService;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.GetTemplatesResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.util.TemplateEndpointUtils;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_NO_AUTH_USER_FOUND;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_ALREADY_EXIST;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NAME_INVALID;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TENANT_ID_INVALID;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_UNEXPECTED;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED;

public class
TemplatesApiServiceImpl extends TemplatesApiService {

    private static final Log LOG = LogFactory.getLog(TemplatesApiServiceImpl.class);

    @Override
    public Response addTemplate(TemplateDTO template) {

        try {
            TemplateResponseDTO response = postTemplate(template);
            return Response.created(getTemplateLocationURI(response.getTemplateName()))
                    .lastModified(Calendar.getInstance().getTime())
                    .entity(response)
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
    public Response updateTemplate(String templateName, TemplateDTO updateTemplateRequestDTO) {

        try {
            TemplateResponseDTO response = postUpdatedTemplate(templateName, updateTemplateRequestDTO);
            return Response.ok()
                    .location(getTemplateLocationURI(response.getTemplateName()))
                    .lastModified(Calendar.getInstance().getTime())
                    .entity(response)
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
            TemplateDTO template = getTemplate(templateName);
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
            Date date = Calendar.getInstance().getTime();
            return Response.ok()
                    .lastModified(date)
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

    private TemplateResponseDTO postTemplate(TemplateDTO templateDTO) throws TemplateManagementException {

        Template templateRequest = TemplateEndpointUtils.getTemplateRequest(templateDTO);
        Template addTemplateResponse = TemplateEndpointUtils.getTemplateManager().addTemplate(templateRequest);

        return getResponseTemplateDTO(addTemplateResponse);
    }

    private TemplateResponseDTO postUpdatedTemplate(String templateName, TemplateDTO updateTemplateRequestDTO)
            throws TemplateManagementException {

        Template updateTemplateRequest = TemplateEndpointUtils.getTemplateRequest(updateTemplateRequestDTO);
        Template updateTemplateResponse = TemplateEndpointUtils.getTemplateManager()
                .updateTemplate(templateName, updateTemplateRequest);

        return getResponseTemplateDTO(updateTemplateResponse);
    }

    private TemplateResponseDTO getResponseTemplateDTO(Template templateResponse) {

        TemplateResponseDTO responseDTO = new TemplateResponseDTO();
        responseDTO.setTenantDomain(IdentityTenantUtil.getTenantDomain(templateResponse.getTenantId()));
        responseDTO.setTemplateName(templateResponse.getTemplateName());
        responseDTO.setDescription(templateResponse.getDescription());
        responseDTO.setTemplateScript(templateResponse.getTemplateScript());
        return responseDTO;
    }

    private TemplateDTO getTemplate(String templateName) throws TemplateManagementException {

        Template getTemplateResponse = TemplateEndpointUtils.getTemplateManager().getTemplateByName(templateName);
        TemplateDTO response = new TemplateDTO();
        response.setTemplateName(getTemplateResponse.getTemplateName());
        response.setDescription(getTemplateResponse.getDescription());
        response.setTemplateScript(getTemplateResponse.getTemplateScript());
        return response;
    }

    private URI getTemplateLocationURI(String templateName) throws URISyntaxException {

        return new URI(TemplateMgtConstants.TEMPLATE_RESOURCE_PATH + "/" + templateName);
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

        return ERROR_CODE_TENANT_ID_INVALID.getCode().equals(e.getErrorCode())
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
