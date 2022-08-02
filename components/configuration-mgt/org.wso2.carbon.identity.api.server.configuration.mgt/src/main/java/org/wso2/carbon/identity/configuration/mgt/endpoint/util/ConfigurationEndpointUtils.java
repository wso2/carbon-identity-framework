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

package org.wso2.carbon.identity.configuration.mgt.endpoint.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.search.PrimitiveStatement;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.impl.UriInfoImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.slf4j.MDC;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.ComplexCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.configuration.mgt.core.search.PrimitiveCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType;
import org.wso2.carbon.identity.configuration.mgt.core.search.exception.PrimitiveConditionValidationException;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.LinkDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceAddDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceFileDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeAddDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourcesDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.exception.BadRequestException;
import org.wso2.carbon.identity.configuration.mgt.endpoint.exception.ConflictRequestException;
import org.wso2.carbon.identity.configuration.mgt.endpoint.exception.ForbiddenException;
import org.wso2.carbon.identity.configuration.mgt.endpoint.exception.InternalServerErrorException;
import org.wso2.carbon.identity.configuration.mgt.endpoint.exception.NotFoundException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCES_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_SEARCH_QUERY_PROPERTY_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_SEARCH_QUERY_SQL_PARSE_ERROR;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_SEARCH_QUERY_SQL_PROPERTY_PARSE_ERROR;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_UNEXPECTED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.FILE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_PATH;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.SERVER_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.TENANT_CONTEXT_PATH_COMPONENT;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.buildURIForBody;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.getTenantDomainFromContext;

/**
 * Utility functions required for configuration endpoint
 */
public class ConfigurationEndpointUtils {

    private static Log log = LogFactory.getLog(ConfigurationEndpointUtils.class);

    public static ConfigurationManager getConfigurationManager() {

        return (ConfigurationManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ConfigurationManager.class, null);
    }

    public static ResourceDTO getResourceDTO(Resource resource) {

        ResourceDTO resourceDTO = new ResourceDTO();
        resourceDTO.setResourceName(resource.getResourceName());
        resourceDTO.setResourceType(resource.getResourceType());
        resourceDTO.setCreated(resource.getCreatedTime());
        resourceDTO.setLastModified(resource.getLastModified());
        resourceDTO.setTenantDomain(resource.getTenantDomain());
        resourceDTO.setResourceId(resource.getResourceId());
        resourceDTO.setTenantDomain(resource.getTenantDomain());
        resourceDTO.setAttributes(
                resource.getAttributes() != null ?
                        resource.getAttributes()
                                .stream()
                                .map(ConfigurationEndpointUtils::getAttributeDTO)
                                .collect(Collectors.toList())
                        : new ArrayList<>(0)
        );
        resourceDTO.setFiles(geFileLinkDTOList(resource));
        return resourceDTO;
    }

    private static List<LinkDTO> geFileLinkDTOList(Resource resource) {

        LinkDTO linkDTO = new LinkDTO();
        List<LinkDTO> linkDTOList = new ArrayList<>();
        linkDTO.setRel(FILE);
        linkDTO.setHref(getFileURI(resource.getResourceName(), resource.getResourceType()));
        linkDTOList.add(linkDTO);
        return linkDTOList;
    }

    public static ResourcesDTO getResourcesDTO(Resources resources) {

        ResourcesDTO resourcesDTO = new ResourcesDTO();
        resourcesDTO.setResources(resources.getResources()
                .stream()
                .map(ConfigurationEndpointUtils::getResourceDTO)
                .collect(Collectors.toList())
        );
        return resourcesDTO;
    }

    public static ResourceTypeDTO getResourceTypeDTO(ResourceType resourceType)
            throws ConfigurationManagementException {

        ResourceTypeDTO resourceTypeDTO = new ResourceTypeDTO();
        resourceTypeDTO.setName(resourceType.getName());
        resourceTypeDTO.setId(resourceType.getId());
        resourceTypeDTO.setDescription(resourceType.getDescription());
        resourceTypeDTO.setLinks(getResourceLinkList(resourceType.getName(),
                getConfigurationManager().getResourcesByType(resourceType.getName()).getResources()));
        return resourceTypeDTO;
    }

    public static ResourceFileDTO getResourceFileDTO(ResourceFile resourceFile) {

        ResourceFileDTO resourceFileDTO = new ResourceFileDTO();
        resourceFileDTO.setFile(buildURIForBody(resourceFile.getPath()));
        resourceFileDTO.setName(resourceFile.getName());
        return resourceFileDTO;
    }

    public static ResourceAdd getResourceAddFromDTO(ResourceAddDTO resourceAddDTO) {

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(resourceAddDTO.getName());
        resourceAdd.setAttributes(resourceAddDTO.getAttributes()
                .stream()
                .map(ConfigurationEndpointUtils::getAttributeFromDTO)
                .collect(Collectors.toList()));
        return resourceAdd;
    }

    public static ResourceTypeAdd getResourceTypeAddFromDTO(ResourceTypeAddDTO resourceTypeAddDTO) {

        ResourceTypeAdd resourceTypeAdd = new ResourceTypeAdd();
        resourceTypeAdd.setName(resourceTypeAddDTO.getName());
        resourceTypeAdd.setDescription(resourceTypeAddDTO.getDescription());
        return resourceTypeAdd;
    }

    public static Attribute getAttributeFromDTO(AttributeDTO attributeDTO) {

        return new Attribute(attributeDTO.getKey(), attributeDTO.getValue());
    }

    public static AttributeDTO getAttributeDTO(Attribute attribute) {

        AttributeDTO attributeDTO = new AttributeDTO();
        attributeDTO.setKey(attribute.getKey());
        attributeDTO.setValue(attribute.getValue());
        return attributeDTO;
    }

    /**
     * Check whether correlation id present in the log MDC
     *
     * @return whether the correlation id is present
     */
    public static boolean isCorrelationIDPresent() {
        return MDC.get(ConfigurationConstants.CORRELATION_ID_MDC) != null;
    }

    /**
     * Get correlation id of current thread
     *
     * @return correlation-id
     */
    public static String getCorrelation() {
        String ref = null;
        if (isCorrelationIDPresent()) {
            ref = MDC.get(ConfigurationConstants.CORRELATION_ID_MDC);
        }
        return ref;
    }

    private static ErrorDTO getErrorDTO(String message, String description, String code) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        errorDTO.setRef(getCorrelation());
        return errorDTO;
    }

    public static Response handleSearchQueryParseError(PrimitiveConditionValidationException e, Log LOG) {

        String searchQueryErrorMessage = e.getMessage();
        String message = String.format(ERROR_CODE_SEARCH_QUERY_SQL_PROPERTY_PARSE_ERROR.getMessage(),
                searchQueryErrorMessage);
        throw ConfigurationEndpointUtils.buildBadRequestException(
                message, ERROR_CODE_SEARCH_QUERY_SQL_PROPERTY_PARSE_ERROR.getCode(), LOG, e);
    }

    public static Response handleSearchQueryParseError(RuntimeException e, Log LOG) {

        throw ConfigurationEndpointUtils.buildBadRequestException(
                ERROR_CODE_SEARCH_QUERY_SQL_PARSE_ERROR.getMessage(),
                ERROR_CODE_SEARCH_QUERY_SQL_PARSE_ERROR.getCode(), LOG, e);
    }

    public static Response handleBadRequestResponse(ConfigurationManagementClientException e, Log LOG) {

        if (isNotFoundError(e)) {
            throw ConfigurationEndpointUtils.buildNotFoundRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
        }

        if (isConflictError(e)) {
            throw ConfigurationEndpointUtils.buildConflictRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
        }

        if (isForbiddenError(e)) {
            throw ConfigurationEndpointUtils.buildForbiddenException(e.getMessage(), e.getErrorCode(), LOG, e);
        }
        throw ConfigurationEndpointUtils.buildBadRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
    }

    public static Response handleServerErrorResponse(ConfigurationManagementException e, Log LOG) {

        throw ConfigurationEndpointUtils.buildInternalServerErrorException(e.getErrorCode(), LOG, e);
    }

    public static Response handleUnexpectedServerError(Throwable e, Log LOG) {

        throw ConfigurationEndpointUtils.buildInternalServerErrorException(ERROR_CODE_UNEXPECTED.getCode(), LOG, e);
    }

    private static boolean isNotFoundError(ConfigurationManagementClientException e) {

        return ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                ERROR_CODE_RESOURCES_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                ERROR_CODE_SEARCH_QUERY_PROPERTY_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) ||
                ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }

    private static boolean isConflictError(ConfigurationManagementClientException e) {

        return ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS.getCode().equals(e.getErrorCode()) ||
                ERROR_CODE_RESOURCE_ALREADY_EXISTS.getCode().equals(e.getErrorCode()) ||
                ERROR_CODE_ATTRIBUTE_ALREADY_EXISTS.getCode().equals(e.getErrorCode());
    }

    private static boolean isForbiddenError(ConfigurationManagementClientException e) {

        return ConfigurationConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND.getCode().equals(e.getErrorCode());
    }

    public static NotFoundException buildNotFoundRequestException(String description, String code,
                                                                  Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(ConfigurationConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new NotFoundException(errorDTO);
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

        ErrorDTO errorDTO = getErrorDTO(ConfigurationConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
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

        ErrorDTO errorDTO = getErrorDTO(ConfigurationConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new ConflictRequestException(errorDTO);
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

        ErrorDTO errorDTO = getErrorDTO(ConfigurationConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new ForbiddenException(errorDTO);
    }

    /**
     * This method is used to create an InternalServerErrorException with the known errorCode.
     *
     * @param code Error Code.
     * @return a new InternalServerErrorException with default details.
     */
    public static InternalServerErrorException buildInternalServerErrorException(String code,
                                                                                 Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(
                ConfigurationConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                ConfigurationConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                code
        );
        logError(log, e);
        return new InternalServerErrorException(errorDTO);
    }

    public static Condition buildSearchCondition(SearchCondition searchCondition) {

        if (!(searchCondition.getStatement() == null)) {
            PrimitiveStatement primitiveStatement = searchCondition.getStatement();
            if (!(primitiveStatement.getProperty() == null)) {
                return new PrimitiveCondition(
                        primitiveStatement.getProperty(),
                        getPrimitiveOperatorFromOdata(primitiveStatement.getCondition()),
                        primitiveStatement.getValue()
                );
            }
            return null;
        } else {
            List<Condition> conditions = new ArrayList<>();
            for (Object condition : searchCondition.getSearchConditions()) {
                Condition buildCondition = buildSearchCondition((SearchCondition) condition);
                if (buildCondition != null) {
                    conditions.add(buildCondition);
                }
            }
            return new ComplexCondition(
                    getComplexOperatorFromOdata(searchCondition.getConditionType()),
                    conditions
            );
        }
    }

    private static ConditionType.PrimitiveOperator getPrimitiveOperatorFromOdata(
            org.apache.cxf.jaxrs.ext.search.ConditionType odataConditionType) {

        ConditionType.PrimitiveOperator primitiveConditionType = null;
        switch (odataConditionType) {
            case EQUALS:
                primitiveConditionType = ConditionType.PrimitiveOperator.EQUALS;
                break;
            case GREATER_OR_EQUALS:
                primitiveConditionType = ConditionType.PrimitiveOperator.GREATER_OR_EQUALS;
                break;
            case LESS_OR_EQUALS:
                primitiveConditionType = ConditionType.PrimitiveOperator.LESS_OR_EQUALS;
                break;
            case GREATER_THAN:
                primitiveConditionType = ConditionType.PrimitiveOperator.GREATER_THAN;
                break;
            case NOT_EQUALS:
                primitiveConditionType = ConditionType.PrimitiveOperator.NOT_EQUALS;
                break;
            case LESS_THAN:
                primitiveConditionType = ConditionType.PrimitiveOperator.LESS_THAN;
                break;
        }
        return primitiveConditionType;
    }

    private static ConditionType.ComplexOperator getComplexOperatorFromOdata(
            org.apache.cxf.jaxrs.ext.search.ConditionType odataConditionType) {

        ConditionType.ComplexOperator complexConditionType = null;
        switch (odataConditionType) {
            case OR:
                complexConditionType = ConditionType.ComplexOperator.OR;
                break;
            case AND:
                complexConditionType = ConditionType.ComplexOperator.AND;
                break;
        }
        return complexConditionType;
    }

    private static  List<LinkDTO> getResourceLinkList(String resourceType, List<Resource> resources) {

        List<LinkDTO> linkDTOList = new ArrayList<>();
        for (Resource resource : resources) {
            LinkDTO linkDTO = new LinkDTO();
            linkDTO.setHref(getResourceURI(resourceType, resource));
            linkDTO.setRel("resource");
            linkDTOList.add(linkDTO);
        }
        return linkDTOList;
    }

    /**
     * Creates the Resource URI using the parameters.
     *
     * @param resourceType resourceType name.
     * @param resource resource name.
     * @return URI of the resource example: /t/<tenant-domain>/api/identity/config-mgt/v1
     * .0/resource/<resource-type-name>/<resource-name>
     */
    private static String getResourceURI(String resourceType, Resource resource) {

        return buildURIForBody(RESOURCE_PATH + '/' + resourceType + '/' + resource.getResourceName());
    }

    /**
     * Creates the URI of the files that belongs to resource using the parameters.
     *
     * @param resourceType resourceType name.
     * @param resourceName resource name.
     * @return URI of the files example: /t/<tenant-domain>/api/identity/config-mgt/v1
     * .0/resource/<resource-type-name>/<resource-name>/file
     */
    private static String getFileURI(String resourceName, String resourceType) {

        return buildURIForBody(RESOURCE_PATH + '/' + resourceType + '/' + resourceName + '/' + FILE);
    }

    private static void logDebug(Log log, Throwable throwable) {

        if (log.isDebugEnabled()) {
            log.debug(ConfigurationConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, throwable);
        }
    }

    private static void logError(Log log, Throwable throwable) {

        log.error(throwable.getMessage(), throwable);
    }

    public static ErrorDTO getOperationNotSupportedDTO() {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode("CONFIGM_00000");
        errorDTO.setMessage("Operation is not supported.");
        errorDTO.setDescription("Operation is not supported.");

        return errorDTO;
    }

    /**
     * Build the complete URI prepending the user API context without the proxy context path, to the endpoint.
     * Ex: https://localhost:9443/t/<tenant-domain>/api/users/<endpoint>
     *
     * @param endpoint relative endpoint path.
     * @return Fully qualified and complete URI.
     */
    public static URI buildURIForHeader(String endpoint) {

        String url = null;
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            try {
                url = ServiceURLBuilder.create().addPath(SERVER_API_PATH_COMPONENT + endpoint).build()
                        .getRelativePublicURL();
            } catch (URLBuilderException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while building URI for header.", e);
                }
            }
        }
        if (StringUtils.isBlank(url)) {
            String tenantQualifiedRelativePath =
                    String.format(TENANT_CONTEXT_PATH_COMPONENT, getTenantDomainFromContext()) + SERVER_API_PATH_COMPONENT;
            url = IdentityUtil.getEndpointURIPath(tenantQualifiedRelativePath + endpoint, false, true);
        }
        URI loc = URI.create(url);
        if (!loc.isAbsolute()) {
            Message currentMessage = PhaseInterceptorChain.getCurrentMessage();
            if (currentMessage != null) {
                UriInfo ui = new UriInfoImpl(currentMessage.getExchange().getInMessage(), null);
                try {
                    return new URI(ui.getBaseUri().getScheme(), ui.getBaseUri().getAuthority(), url, null, null);
                } catch (URISyntaxException e) {
                    log.error("Server encountered an error while building the location URL with scheme: " +
                            ui.getBaseUri().getScheme() + ", authority: " + ui.getBaseUri().getAuthority() +
                            ", url: " + url, e);
                }
            }
        }
        return loc;
    }
}
