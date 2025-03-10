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

import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REG_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;
import java.util.UUID;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

/**
 * Utility class for registration flow engine.
 */
public class RegistrationFlowEngineUtils {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowEngineUtils.class);

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

        RegistrationContextCache.getInstance().clearCacheEntry(new RegistrationContextCacheKey(contextId));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registration context removed from cache for context id: " + contextId + ".");
        }
    }

    /**
     * Initiate the registration context.
     *
     * @param tenantDomain Tenant domain.
     * @return Registration context.
     */
    public static RegistrationContext initiateContext(String tenantDomain)
            throws RegistrationEngineException {

        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            RegistrationGraphConfig graphConfig =
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationFlowMgtService()
                            .getRegistrationGraphConfig(tenantId);

            if (graphConfig == null) {
                throw handleServerException(ERROR_CODE_REG_FLOW_NOT_FOUND, tenantDomain);
            }
            RegistrationContext context = new RegistrationContext();
            context.setTenantDomain(tenantDomain);
            context.setRegGraph(graphConfig);
            context.setContextIdentifier(UUID.randomUUID().toString());
            return context;
        } catch (IdentityRuntimeException e) {
            throw handleServerException(ERROR_CODE_TENANT_RESOLVE_FAILURE, tenantDomain);
        } catch (RegistrationFrameworkException e) {
            throw handleServerException(ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE, tenantDomain);
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
    public static String buildMyAccountAccessURL(String tenantDomain) {

        return ApplicationMgtUtil.getMyAccountAccessUrlFromServerConfig(tenantDomain);
    }
}
