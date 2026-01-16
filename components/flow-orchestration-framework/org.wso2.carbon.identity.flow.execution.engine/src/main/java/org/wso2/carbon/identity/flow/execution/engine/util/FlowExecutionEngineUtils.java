/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowExecCtxCache;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowExecCtxCacheEntry;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowExecCtxCacheKey;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.FlowConfigDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_TYPE_NOT_PROVIDED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_APP_CONFIG_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_FLOW_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_INPUT_VALIDATION_CONFIG_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FROM_ORGANIZATION_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.EMAIL_FORMAT_VALIDATOR;

/**
 * Utility class for flow engine.
 */
public class FlowExecutionEngineUtils {

    private static final Log LOG = LogFactory.getLog(FlowExecutionEngineUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USERNAME = "username";


    /**
     * Add flow context to cache.
     *
     * @param context Flow context.
     */
    public static void addFlowContextToCache(FlowExecutionContext context) throws FlowEngineException {

        addFlowContextToCache(context.getContextIdentifier(), context);
    }

    /**
     * Add flow context to cache with provided key.
     *
     * @param cacheKeyIdentifier Cache key identifier.
     * @param context            Flow context.
     * @throws FlowEngineException Flow engine exception.
     */
    public static void addFlowContextToCache(String cacheKeyIdentifier, FlowExecutionContext context)
            throws FlowEngineException {

        if (StringUtils.isBlank(cacheKeyIdentifier)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache key identifier is blank. Skipping adding flow context to cache.");
            }
            return;
        }
        // Persist an optimized version of the context in the cache and flow context store.
        optimizeContext(context);
        FlowExecCtxCacheEntry cacheEntry = new FlowExecCtxCacheEntry(context);
        FlowExecCtxCacheKey cacheKey = new FlowExecCtxCacheKey(cacheKeyIdentifier);
        FlowExecCtxCache.getInstance().clearCacheEntry(cacheKey);
        FlowExecCtxCache.getInstance().addToCache(cacheKey, cacheEntry);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow context added to cache for context id: " + cacheKeyIdentifier);
        }
    }

    /**
     * Retrieve flow context from cache.
     *
     * @param contextId Context identifier.
     * @return Flow context.
     * @throws FlowEngineException Flow engined exception.
     */
    public static FlowExecutionContext retrieveFlowContextFromCache(String contextId)
            throws FlowEngineException {

        if (contextId == null) {
            throw handleClientException(ERROR_CODE_UNDEFINED_FLOW_ID);
        }
        FlowExecCtxCacheEntry entry =
                FlowExecCtxCache.getInstance().getValueFromCache(new FlowExecCtxCacheKey(contextId));
        if (entry == null) {
            throw handleClientException(ERROR_CODE_INVALID_FLOW_ID, contextId);
        }
        return populateOptimizedContext(entry);
    }

    /**
     * Remove flow context from cache.
     *
     * @param contextId Context identifier.
     */
    public static void removeFlowContextFromCache(String contextId) throws FlowEngineException {

        if (contextId == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context id is null. Hence skipping removing the flow context from cache.");
            }
            return;
        }
        FlowExecCtxCache.getInstance().clearCacheEntry(new FlowExecCtxCacheKey(contextId));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow context removed from cache for context id: " + contextId + ".");
        }
    }

    private static void optimizeContext(FlowExecutionContext context) {

        context.setGraphConfig(null);
    }

    private static FlowExecutionContext populateOptimizedContext(FlowExecCtxCacheEntry entry)
            throws FlowEngineServerException {

        FlowExecutionContext context = entry.getContext();
        if (context != null) {
            String flowType = context.getFlowType();
            try {
                context.setGraphConfig(
                        FlowExecutionEngineDataHolder.getInstance().getFlowMgtService()
                                .getGraphConfig(flowType, IdentityTenantUtil.getTenantId(context.getTenantDomain())));
            } catch (FlowMgtFrameworkException e) {
                throw handleServerException(flowType, ERROR_CODE_GET_DEFAULT_FLOW_FAILURE, flowType,
                        context.getTenantDomain(), e);
            }
        }
        return context;
    }

    /**
     * Initiate the flow context.
     *
     * @param tenantDomain  Tenant domain.
     * @param applicationId Application ID.
     * @return Flow context.
     * @throws FlowEngineException Flow engine exception.
     */
    public static FlowExecutionContext initiateContext(String tenantDomain, String applicationId, String flowType)
            throws FlowEngineException {

        try {
            if (StringUtils.isBlank(flowType)) {
                throw handleClientException(flowType, ERROR_CODE_FLOW_TYPE_NOT_PROVIDED);
            }
            FlowExecutionContext context = new FlowExecutionContext();
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            GraphConfig graphConfig =
                    FlowExecutionEngineDataHolder.getInstance().getFlowMgtService()
                            .getGraphConfig(flowType, tenantId);

            if (graphConfig == null) {
                throw handleServerException(flowType, ERROR_CODE_FLOW_NOT_FOUND, flowType, tenantDomain);
            }
            context.setTenantDomain(tenantDomain);
            context.setGraphConfig(graphConfig);
            context.setContextIdentifier(UUID.randomUUID().toString());
            context.setApplicationId(applicationId);
            context.setFlowType(flowType);

            FlowConfigDTO flowConfigDTO = FlowExecutionEngineDataHolder.getInstance().getFlowMgtService()
                    .getFlowConfig(flowType, IdentityTenantUtil.getTenantId(tenantDomain));
            if (flowConfigDTO != null) {
                context.setGenerateAuthenticationAssertion(Boolean.parseBoolean(
                        flowConfigDTO.getFlowCompletionConfig(Constants.FlowCompletionConfig.IS_AUTO_LOGIN_ENABLED))
                );
            }

            return context;
        } catch (IdentityRuntimeException e) {
            throw handleServerException(flowType, ERROR_CODE_TENANT_RESOLVE_FAILURE, tenantDomain);
        } catch (FlowMgtFrameworkException e) {
            throw handleServerException(flowType, ERROR_CODE_GET_DEFAULT_FLOW_FAILURE, flowType, tenantDomain);
        }
    }

    /**
     * Rollback the flow context.
     *
     * @param contextId Context identifier.
     */
    public static void rollbackContext(String flowType, String contextId) {

        if (StringUtils.isBlank(contextId)) {
            LOG.debug("Context id is null or empty. Hence skipping rollback of the flow context.");
            return;
        }
        try {
            FlowExecutionContext context = retrieveFlowContextFromCache(contextId);
            if (context != null) {
                context.getCompletedNodes().forEach((config) -> {
                    if (Constants.NodeTypes.TASK_EXECUTION.equals(config.getType())) {
                        try {
                            new TaskExecutionNode().rollback(context, config);
                        } catch (FlowEngineException ex) {
                            LOG.error("Error occurred while executing rollback for node: " + config.getId(), ex);
                        }
                    }
                });
            }
        } catch (FlowEngineException e) {
            if (e instanceof FlowEngineClientException) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Client error occurred while retrieving the flow context with flow id: " + contextId, e);
                }
            } else {
                LOG.error("Server error occurred while retrieving the flow context with flow id: " + contextId, e);
            }
        }
    }

    /**
     * Handle the flow engine server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return FlowEngineServerException.
     */
    public static FlowEngineServerException handleServerException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the flow engine server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return FlowEngineServerException.
     */
    public static FlowEngineServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the flow engine server exceptions.
     *
     * @param flowType Flow type.
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return FlowEngineServerException.
     */
    public static FlowEngineServerException handleServerException(String flowType, ErrorMessages error, Throwable e,
                                                                  Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineServerException(flowType, error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the flow engine server exceptions.
     *
     * @param flowType Flow type.
     * @param error Error message.
     * @param data  The error message data.
     * @return FlowEngineServerException.
     */
    public static FlowEngineServerException handleServerException(String flowType, ErrorMessages error,
                                                                  Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineServerException(flowType, error.getCode(), error.getMessage(), description);
    }


    /**
     * Handle the flow engine server exceptions.
     *
     * @param flowType Flow type.
     * @param response Executor response.
     * @return FlowEngineServerException.
     */
    public static FlowEngineServerException handleServerException(String flowType, ExecutorResponse response) {

        String errorMsg = response.getErrorMessage();
        String errorCode = response.getErrorCode();
        String errorDescription = response.getErrorDescription();
        Throwable throwable = response.getThrowable();
        return new FlowEngineServerException(flowType, errorCode, errorMsg, errorDescription, throwable);
    }
    /**
     * Handle the flow engine client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return FlowEngineClientException.
     */
    public static FlowEngineClientException handleClientException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the flow engine client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return FlowEngineClientException.
     */
    public static FlowEngineClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the flow engine client exceptions.
     *
     * @param flowType Flow type.
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return FlowEngineClientException.
     */
    public static FlowEngineClientException handleClientException(String flowType, ErrorMessages error, Throwable e,
                                                                  Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineClientException(flowType, error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the flow engine client exceptions.
     *
     * @param flowType Flow type.
     * @param error Error message.
     * @param data  The error message data.
     * @return FlowEngineClientException.
     */
    public static FlowEngineClientException handleClientException(String flowType, ErrorMessages error,
                                                                  Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowEngineClientException(flowType, error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the flow engine client exceptions.
     *
     * @param flowType Flow type.
     * @param response Executor response.
     * @return FlowEngineClientException.
     */
    public static FlowEngineClientException handleClientException(String flowType, ExecutorResponse response) {

        String errorMsg = response.getErrorMessage();
        String errorCode = response.getErrorCode();
        String errorDescription = response.getErrorDescription();
        Throwable throwable = response.getThrowable();
        return new FlowEngineClientException(flowType, errorCode, errorMsg, errorDescription, throwable);
    }

    /**
     * Build the MyAccount access URL.
     *
     * @param tenantDomain Tenant domain.
     * @return MyAccount Access URL.
     */
    public static String buildMyAccountAccessURL(String tenantDomain) throws FlowEngineServerException {

        String myAccountAccessUrl = getApplicationAccessUrlByAppName(tenantDomain, MY_ACCOUNT_APPLICATION_NAME);
        if (StringUtils.isBlank(myAccountAccessUrl)) {
            myAccountAccessUrl = ApplicationMgtUtil.getMyAccountAccessUrlFromServerConfig(tenantDomain);
        }
        return myAccountAccessUrl;
    }

    /**
     * Resolve the flow completion redirection URL.
     *
     * @param context Flow context.
     * @return Redirection URL.
     * @throws FlowEngineServerException Flow engine exception.
     */
    public static String resolveCompletionRedirectionUrl(FlowExecutionContext context)
            throws FlowEngineServerException {

        String redirectionUrl = getApplicationAccessUrlByAppId(context.getTenantDomain(), context.getApplicationId());

        // If the application access URL is not available, we will use the MyAccount access URL.
        if (StringUtils.isBlank(redirectionUrl)) {
            redirectionUrl = buildMyAccountAccessURL(context.getTenantDomain());
        }
        return redirectionUrl;
    }

    /**
     * Get the application access URL.
     *
     * @param tenantDomain  Tenant domain.
     * @param applicationId Application ID.
     * @return Application access URL.
     * @throws FlowEngineServerException Flow engine exception.
     */
    private static String getApplicationAccessUrlByAppId(String tenantDomain, String applicationId)
            throws FlowEngineServerException {

        ApplicationBasicInfo application;
        ApplicationManagementService applicationManagementService =
                FlowExecutionEngineDataHolder.getInstance().getApplicationManagementService();
        try {
            application = applicationManagementService.getApplicationBasicInfoByResourceId(applicationId, tenantDomain);
            if (application != null) {
                return application.getAccessUrl();
            }
        } catch (IdentityApplicationManagementException e) {
            throw handleServerException(ERROR_CODE_GET_APP_CONFIG_FAILURE, e, applicationId, tenantDomain);
        }
        return null;
    }

    /**
     * Get the application access URL by application name.
     *
     * @param tenantDomain Tenant domain.
     * @param appName      Application name.
     * @return Application access URL.
     * @throws FlowEngineServerException Flow engine framework exception.
     */
    private static String getApplicationAccessUrlByAppName(String tenantDomain, String appName)
            throws FlowEngineServerException {

        ApplicationBasicInfo application;
        ApplicationManagementService applicationManagementService =
                FlowExecutionEngineDataHolder.getInstance().getApplicationManagementService();
        try {
            application = applicationManagementService.getApplicationBasicInfoByName(appName, tenantDomain);
            if (application != null) {
                return application.getAccessUrl();
            }
        } catch (IdentityApplicationManagementException e) {
            throw handleServerException(ERROR_CODE_GET_APP_CONFIG_FAILURE, e, appName, tenantDomain);
        }
        return null;
    }

    public static Map<String, Object> getMapFromJSONString(String json) throws FlowEngineServerException {

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw handleServerException(
                    ErrorMessages.ERROR_CODE_NODE_RESPONSE_PROCESSING_FAILURE, e,
                    "JSON String to Map conversion failed.");
        }
    }

    public static boolean isEmailUsernameValidator(String tenantDomain) throws FlowEngineServerException {

        List<String> usernameValidators = getUsernameValidators(tenantDomain);
        return usernameValidators.contains(EMAIL_FORMAT_VALIDATOR);
    }

    private static List<String> getUsernameValidators(String tenantDomain) throws FlowEngineServerException {

        List<String> usernameValidators = new ArrayList<>();
        try {
            List<ValidationConfiguration> validationConfigurations = FlowExecutionEngineDataHolder.getInstance()
                    .getInputValidationManagementService().getInputValidationConfiguration(tenantDomain);
            for (ValidationConfiguration config : validationConfigurations) {
                if (!USERNAME.equals(config.getField())) {
                    continue;
                }

                if (config.getRules() != null && !config.getRules().isEmpty()) {
                    config.getRules().forEach(rule -> {
                        usernameValidators.add(rule.getValidatorName());
                    });
                }
            }
        } catch (InputValidationMgtException e) {
            LOG.error("Error while retrieving input validation configurations for tenant: " + tenantDomain, e);
            throw handleServerException(ERROR_CODE_GET_INPUT_VALIDATION_CONFIG_FAILURE, e, tenantDomain);
        }
        return usernameValidators;
    }

    /**
     * Resolve tenant domain from the current carbon context.
     *
     * @return Tenant domain.
     * @throws FlowEngineServerException Flow engine server exception.
     */
    public static String resolveTenantDomain() throws FlowEngineServerException {

        String tenantName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String appResidentOrgId = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getApplicationResidentOrganizationId();
        if (StringUtils.isNotBlank(appResidentOrgId)) {
            try {
                tenantName = FrameworkUtils.resolveTenantDomainFromOrganizationId(appResidentOrgId);
            } catch (FrameworkException e) {
                throw handleServerException(ERROR_CODE_TENANT_RESOLVE_FROM_ORGANIZATION_FAILURE, e, appResidentOrgId);
            }
        }
        return tenantName;
    }
}
