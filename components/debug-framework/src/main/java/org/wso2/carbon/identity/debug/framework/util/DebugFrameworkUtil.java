/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic utility class for the Debug Framework.
 * Provides common utility methods used across the framework.
 */
public final class DebugFrameworkUtil {

    private static final Log LOG = LogFactory.getLog(DebugFrameworkUtil.class);

    private DebugFrameworkUtil() {
    }

    /**
     * Checks if a string is null or empty.
     *
     * @param value String to check.
     * @return true if null or empty, false otherwise.
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Checks if a string has meaningful content.
     *
     * @param value String to check.
     * @return true if not null and not empty, false otherwise.
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Generates a unique session identifier for debug flows.
     *
     * @return UUID-based session identifier.
     */
    public static String generateSessionId() {
        return "debug-" + java.util.UUID.randomUUID().toString();
    }

    /**
     * Generates a random state parameter for OAuth2 flows.
     *
     * @return Random state string.
     */
    public static String generateStateParameter() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * URL-encodes a parameter value.
     *
     * @param param Parameter to encode.
     * @return URL-encoded parameter.
     * @throws IllegalArgumentException If encoding fails.
     */
    public static String encodeUrlParameter(String param) {
        if (param == null) {
            return null;
        }
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            LOG.error("Error encoding parameter: " + e.getMessage(), e);
            throw new IllegalArgumentException("Failed to URL-encode parameter", e);
        }
    }

    /**
     * URL-decodes a parameter value.
     *
     * @param param Parameter to decode.
     * @return URL-decoded parameter.
     * @throws IllegalArgumentException If decoding fails.
     */
    public static String decodeUrlParameter(String param) {
        if (param == null) {
            return null;
        }
        try {
            return java.net.URLDecoder.decode(param, "UTF-8");
        } catch (Exception e) {
            LOG.error("Error decoding parameter: " + e.getMessage(), e);
            throw new IllegalArgumentException("Failed to URL-decode parameter", e);
        }
    }

    /**
     * Validates an email address format.
     *
     * @param email Email to validate.
     * @return true if valid email format, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Validates a UUID format.
     *
     * @param uuid UUID string to validate.
     * @return true if valid UUID format, false otherwise.
     */
    public static boolean isValidUUID(String uuid) {
        if (isBlank(uuid)) {
            return false;
        }
        String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidRegex);
    }

    /**
     * Safely converts an object to string.
     *
     * @param obj Object to convert.
     * @return String representation or null.
     */
    public static String toSafeString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    /**
     * Logs debug information conditionally.
     *
     * @param log    Logger instance.
     * @param msg    Message to log.
     * @param params Optional parameters for message formatting.
     */
    public static void debugLog(Log log, String msg, Object... params) {
        if (log.isDebugEnabled()) {
            if (params != null && params.length > 0) {
                log.debug(String.format(msg, params));
            } else {
                log.debug(msg);
            }
        }
    }

    /**
     * Gets current timestamp in milliseconds.
     *
     * @return Current time.
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Calculates elapsed time since a start time.
     *
     * @param startTime Start time in milliseconds.
     * @return Elapsed time in milliseconds.
     */
    public static long getElapsedTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }
}
