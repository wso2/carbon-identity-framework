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

package org.wso2.carbon.identity.template.mgt.endpoint.util;

import org.apache.commons.logging.Log;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.template.mgt.TemplateManager;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.GetTemplatesResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.exception.BadRequestException;
import org.wso2.carbon.identity.template.mgt.endpoint.exception.ConflictRequestException;
import org.wso2.carbon.identity.template.mgt.endpoint.exception.ForbiddenException;
import org.wso2.carbon.identity.template.mgt.endpoint.exception.InternalServerErrorException;
import org.wso2.carbon.identity.template.mgt.endpoint.exception.NotFoundException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;
import org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateEndpointUtils {

    public static TemplateManager getTemplateManager() {

        return (TemplateManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(TemplateManager.class, null);
    }

    public static Template getTemplateRequest(TemplateDTO templateDTO) {

        return new Template(TemplateMgtUtils.getTenantIdFromCarbonContext(),
                templateDTO.getTemplateName(),
                templateDTO.getDescription(),
                templateDTO.getTemplateScript());
    }

    public static List<GetTemplatesResponseDTO> getTemplatesResponseDTOList(List<TemplateInfo> templates) {

        return templates.stream()
                .map(template -> {
                    GetTemplatesResponseDTO getTemplatesResponseDTO = new GetTemplatesResponseDTO();
                    getTemplatesResponseDTO.setTemplateName(template.getTemplateName());
                    getTemplatesResponseDTO.setDescription(template.getDescription());
                    return getTemplatesResponseDTO;
                })
                .collect(Collectors.toList());
    }

    private static void logError(Log log, Throwable throwable) {

        log.error(throwable.getMessage(), throwable);
    }

    private static void logDebug(Log log, Throwable throwable) {

        if (log.isDebugEnabled()) {
            log.debug(TemplateMgtConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, throwable);
        }
    }

    private static ErrorDTO getErrorDTO(String message, String description, String code) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    /**
     * This method is used to create an InternalServerErrorException with the known error code.
     *
     * @param code Error Code.
     * @return a new InternalServerErrorException with default details.
     */
    public static InternalServerErrorException buildInternalServerErrorException(String code,
                                                                                 Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(TemplateMgtConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                TemplateMgtConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, code);
        logError(log, e);
        return new InternalServerErrorException(errorDTO);
    }

    /**
     * This method is used to create a BadRequestException with the known errorCode and message.
     *
     * @param description Error Message Desription.
     * @param code        Error Code.
     * @return BadRequestException with the given errorCode and description.
     */
    public static BadRequestException buildBadRequestException(String description, String code,
                                                               Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(TemplateMgtConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new BadRequestException(errorDTO);
    }

    /**
     * This method is used to create a ConflictRequestException with the known errorCode and message.
     *
     * @param description Error Message Description.
     * @param code        Error Code.
     * @return ConflictRequestException with the given errorCode and description.
     */
    public static ConflictRequestException buildConflictRequestException(String description, String code,
                                                                         Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(TemplateMgtConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new ConflictRequestException(errorDTO);
    }

    /**
     * This method is used to create a NotFoundException with the known errorCode and message.
     *
     * @param description Error Message Description.
     * @param code        Error Code.
     * @return NotFoundException with the given errorCode and description.
     */
    public static NotFoundException buildNotFoundRequestException(String description, String code,
                                                                  Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(TemplateMgtConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new NotFoundException(errorDTO);
    }

    /**
     * This method is used to create a Forbidden Exception with the known errorCode and message.
     *
     * @param description Error Message Description.
     * @param code        Error Code.
     * @return ForbiddenException with the given errorCode and description.
     */
    public static ForbiddenException buildForbiddenException(String description, String code,
                                                             Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(TemplateMgtConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new ForbiddenException(errorDTO);
    }

}
