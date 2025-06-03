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

package org.wso2.carbon.identity.user.registration.engine.util;

import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineClientException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

import java.util.UUID;

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME;
import static org.wso2.carbon.identity.user.registration.engine.Constants.DEFAULT_REGISTRATION_CALLBACK;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_GET_APP_CONFIG_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REG_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_RESOLVE_DEFAULT_CALLBACK_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;

/**
 * Utility class for registration flow engine.
 */
public class RegistrationFlowEngineUtils {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowEngineUtils.class);
    private static final String USER_TENANT_HINT_PLACE_HOLDER = "${UserTenantHint}";
    private static final String SUPER_TENANT = "carbon.super";

    /**
     * Add registration context to cache.
     *
     * @param context Registration context.
     */
    public static void addRegContextToCache(RegistrationContext context) {

        RegistrationContextCacheEntry cacheEntry = new RegistrationContextCacheEntry(context);
        RegistrationContextCacheKey cacheKey = new RegistrationContextCacheKey(context.getContextIdentifier());
        RegistrationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registration context added to cache for context id: " + context.getContextIdentifier());
        }
    }

    /**
     * Retrieve registration context from cache.
     *
     * @param contextId Context identifier.
     * @return Registration context.
     * @throws RegistrationEngineException Registration framework exception.
     */
    public static RegistrationContext retrieveRegContextFromCache(String contextId)
            throws RegistrationEngineException {

        if (contextId == null) {
            throw handleClientException(ERROR_CODE_UNDEFINED_FLOW_ID);
        }
        RegistrationContextCacheEntry entry =
                RegistrationContextCache.getInstance().getValueFromCache(new RegistrationContextCacheKey(contextId));
        if (entry == null) {
            throw handleClientException(ERROR_CODE_INVALID_FLOW_ID, contextId);
        }
        return entry.getContext();
    }

    /**
     * Remove registration context from cache.
     *
     * @param contextId Context identifier.
     */
    public static void removeRegContextFromCache(String contextId) {

        if (contextId == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context id is null. Hence skipping removing the registration context from cache.");
            }
            return;
        }
        RegistrationContextCache.getInstance().clearCacheEntry(new RegistrationContextCacheKey(contextId));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registration context removed from cache for context id: " + contextId + ".");
        }
    }

    /**
     * Initiate the registration context.
     *
     * @param tenantDomain  Tenant domain.
     * @param callbackUrl   Callback URL.
     * @param applicationId Application ID.
     * @return Registration context.
     * @throws RegistrationEngineException Registration framework exception.
     */
    public static RegistrationContext initiateContext(String tenantDomain, String callbackUrl, String applicationId)
            throws RegistrationEngineException {

        try {
            RegistrationContext context = new RegistrationContext();
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            RegistrationGraphConfig graphConfig =
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationFlowMgtService()
                            .getRegistrationGraphConfig(tenantId);

            if (graphConfig == null) {
                throw handleServerException(ERROR_CODE_REG_FLOW_NOT_FOUND, tenantDomain);
            }
            context.setTenantDomain(tenantDomain);
            context.setRegGraph(graphConfig);
            context.setContextIdentifier(UUID.randomUUID().toString());
            context.setApplicationId(applicationId);
            if (StringUtils.isNotEmpty(callbackUrl)) {
                context.setCallbackUrl(callbackUrl);
            } else {
                context.setCallbackUrl(ServiceURLBuilder.create().addPath(DEFAULT_REGISTRATION_CALLBACK).build()
                                               .getAbsolutePublicURL());
            }
            return context;
        } catch (URLBuilderException e) {
            throw handleServerException(ERROR_CODE_RESOLVE_DEFAULT_CALLBACK_FAILURE, tenantDomain);
        } catch (IdentityRuntimeException e) {
            throw handleServerException(ERROR_CODE_TENANT_RESOLVE_FAILURE, tenantDomain);
        } catch (RegistrationFrameworkException e) {
            throw handleServerException(ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE, tenantDomain);
        }
    }

    /**
     * Rollback the registration context.
     *
     * @param contextId Context identifier.
     */
    public static void rollbackContext(String contextId) {

        if (StringUtils.isBlank(contextId)) {
            LOG.debug("Context id is null or empty. Hence skipping rollback of the flow context.");
            return;
        }
        try {
            RegistrationContext context = retrieveRegContextFromCache(contextId);
            if (context != null) {
                context.getCompletedNodes().forEach((config) -> {
                    if (Constants.NodeTypes.TASK_EXECUTION.equals(config.getType())) {
                        try {
                            new TaskExecutionNode().rollback(context, config);
                        } catch (RegistrationEngineException ex) {
                            LOG.error("Error occurred while executing rollback for node: " + config.getId(), ex);
                        }
                    }
                });
            }
        } catch (RegistrationEngineException e) {
            LOG.error("Error occurred while retrieving the flow context with flow id: " + contextId, e);
        }
    }

    /**
     * Handle the registration flow engine server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationEngineServerException.
     */
    public static RegistrationEngineServerException handleServerException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationEngineServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow engine server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationEngineServerException.
     */
    public static RegistrationEngineServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationEngineServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the registration flow engine client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationEngineClientException.
     */
    public static RegistrationEngineClientException handleClientException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationEngineClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow engine client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationEngineClientException.
     */
    public static RegistrationEngineClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationEngineClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Build the MyAccount access URL.
     *
     * @param tenantDomain Tenant domain.
     * @return MyAccount Access URL.
     */
    public static String buildMyAccountAccessURL(String tenantDomain) throws RegistrationEngineServerException {

        String myAccountAccessUrl = getApplicationAccessUrlByAppName(tenantDomain, MY_ACCOUNT_APPLICATION_NAME);
        if (StringUtils.isBlank(myAccountAccessUrl)) {
            myAccountAccessUrl = ApplicationMgtUtil.getMyAccountAccessUrlFromServerConfig(tenantDomain);
        }
        return replaceUserTenantHintPlaceholder(myAccountAccessUrl, tenantDomain);
    }

    /**
     * Resolve the registration completion redirection URL.
     *
     * @param context   Registration context.
     * @return  Redirection URL.
     * @throws RegistrationEngineServerException    Registration framework exception.
     */
    public static String resolveCompletionRedirectionUrl(RegistrationContext context)
            throws RegistrationEngineServerException {

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
     * @throws RegistrationEngineServerException Registration framework exception.
     */
    private static String getApplicationAccessUrlByAppId(String tenantDomain, String applicationId)
            throws RegistrationEngineServerException {

        ApplicationBasicInfo application;
        ApplicationManagementService applicationManagementService =
                RegistrationFlowEngineDataHolder.getInstance().getApplicationManagementService();
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
     * @throws RegistrationEngineServerException Registration framework exception.
     */
    private static String getApplicationAccessUrlByAppName(String tenantDomain, String appName)
            throws RegistrationEngineServerException {

        ApplicationBasicInfo application;
        ApplicationManagementService applicationManagementService =
                RegistrationFlowEngineDataHolder.getInstance().getApplicationManagementService();
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

    /**
     * Replace the ${UserTenantHint} placeholder in the url with the tenant domain.
     *
     * @param url           Url with the placeholder.
     * @param tenantDomain  Tenant Domain.
     * @return              Processed url.
     */
    private static String replaceUserTenantHintPlaceholder(String url, String tenantDomain) {

        if (StringUtils.isBlank(url)) {
            return url;
        }
        if (!url.contains(USER_TENANT_HINT_PLACE_HOLDER)) {
            return url;
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = SUPER_TENANT;
        }
        return url.replaceAll(Pattern.quote(USER_TENANT_HINT_PLACE_HOLDER), tenantDomain)
                .replaceAll(Pattern.quote("/t/" + SUPER_TENANT), "");
    }
}
