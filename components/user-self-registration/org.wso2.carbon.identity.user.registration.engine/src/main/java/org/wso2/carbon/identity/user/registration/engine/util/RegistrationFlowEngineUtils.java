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

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;

/**
 * Utility class for registration flow engine.
 */
public class RegistrationFlowEngineUtils {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowEngineUtils.class);

    /**
     * Add registration context to cache.
     *
     * @param context   Registration context.
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
     * @return  Registration context.
     * @throws RegistrationFrameworkException   Registration framework exception.
     */
    public static RegistrationContext retrieveRegContextFromCache(String contextId)
            throws RegistrationFrameworkException {

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
    }

    /**
     * Handle the registration flow engine server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationServerException.
     */
    public static RegistrationServerException handleServerException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow engine server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationServerException.
     */
    public static RegistrationServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the registration flow engine client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationClientException.
     */
    public static RegistrationClientException handleClientException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow engine client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationClientException.
     */
    public static RegistrationClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationClientException(error.getCode(), error.getMessage(), description);
    }
}
