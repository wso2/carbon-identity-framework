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
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_APP_CONFIG_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_FLOW_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;

/**
 * Utility class for flow engine.
 */
public class FlowExecutionEngineUtils {

    private static final Log LOG = LogFactory.getLog(FlowExecutionEngineUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Add flow context to cache.
     *
     * @param context Flow context.
     */
    public static void addFlowContextToCache(FlowExecutionContext context) {

        FlowExecCtxCacheEntry cacheEntry = new FlowExecCtxCacheEntry(context);
        FlowExecCtxCacheKey cacheKey = new FlowExecCtxCacheKey(context.getContextIdentifier());
        FlowExecCtxCache.getInstance().addToCache(cacheKey, cacheEntry);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow context added to cache for context id: " + context.getContextIdentifier());
        }
    }

    /**
     * Retrieve flow context from cache.
     *
     * @param flowType Type of the flow.
     * @param contextId Context identifier.
     * @return Flow context.
     * @throws FlowEngineException Flow engined exception.
     */
    public static FlowExecutionContext retrieveFlowContextFromCache(String flowType, String contextId) throws FlowEngineException {

        if (contextId == null) {
            throw handleClientException(ERROR_CODE_UNDEFINED_FLOW_ID, flowType);
        }
        FlowExecCtxCacheEntry entry =
                FlowExecCtxCache.getInstance().getValueFromCache(new FlowExecCtxCacheKey(contextId));
        if (entry == null) {
            throw handleClientException(ERROR_CODE_INVALID_FLOW_ID, contextId);
        }
        return entry.getContext();
    }

    /**
     * Remove flow context from cache.
     *
     * @param contextId Context identifier.
     */
    public static void removeFlowContextFromCache(String contextId) {

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
            FlowExecutionContext context = new FlowExecutionContext();
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            GraphConfig graphConfig =
                    FlowExecutionEngineDataHolder.getInstance().getFlowMgtService()
                            .getGraphConfig(flowType, tenantId);

            if (graphConfig == null) {
                throw handleServerException(ERROR_CODE_FLOW_NOT_FOUND, flowType, tenantDomain);
            }
            context.setTenantDomain(tenantDomain);
            context.setGraphConfig(graphConfig);
            context.setContextIdentifier(UUID.randomUUID().toString());
            context.setApplicationId(applicationId);
            context.setFlowType(flowType);
            return context;
        } catch (IdentityRuntimeException e) {
            throw handleServerException(ERROR_CODE_TENANT_RESOLVE_FAILURE, tenantDomain);
        } catch (FlowMgtFrameworkException e) {
            throw handleServerException(ERROR_CODE_GET_DEFAULT_FLOW_FAILURE, flowType, tenantDomain);
        }
    }

    /**
     * Rollback the flow context.
     *
     * @param flowType Type of the flow.
     * @param contextId Context identifier.
     */
    public static void rollbackContext(String flowType, String contextId) {

        if (StringUtils.isBlank(contextId)) {
            LOG.debug("Context id is null or empty. Hence skipping rollback of the flow context.");
            return;
        }
        try {
            FlowExecutionContext context = retrieveFlowContextFromCache(flowType, contextId);
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
            LOG.error("Error occurred while retrieving the flow context with flow id: " + contextId, e);
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
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw handleServerException(
                    ErrorMessages.ERROR_CODE_NODE_RESPONSE_PROCESSING_FAILURE, e,
                    "JSON String to Map conversion failed.");
        }
    }
}
