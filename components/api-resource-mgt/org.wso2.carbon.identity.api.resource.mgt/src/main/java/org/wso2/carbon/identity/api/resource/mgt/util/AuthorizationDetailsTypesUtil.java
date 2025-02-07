/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Utility class for authorization details types management.
 */
public class AuthorizationDetailsTypesUtil {

    private static final Log LOG = LogFactory.getLog(AuthorizationDetailsTypesUtil.class);
    private static final Gson GSON = new Gson();
    private static final Type SCHEMA_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

    /**
     * Parses a JSON schema represented as a string and converts it into a map structure.
     *
     * @param schema the JSON schema string to be parsed. It must be a valid JSON string.
     * @return a {@code Map<String, Object>} representing the parsed JSON schema.
     */
    public static Map<String, Object> parseSchema(final String schema) {

        return GSON.fromJson(schema, SCHEMA_TYPE);
    }

    /**
     * Converts a map representing a schema into its JSON string representation.
     *
     * @param schema the schema represented as a {@code Map<String, Object>} to be converted into a JSON string.
     * @return a JSON string representation of the provided schema map.
     */
    public static String toJsonString(final Map<String, Object> schema) {

        return GSON.toJson(schema, SCHEMA_TYPE);
    }

    /**
     * Checks if the OAuth.EnableRichAuthorizationRequests configuration is enabled.
     * If not enabled, throws a client exception indicating that the authorization details type is unsupported.
     *
     * @throws APIResourceMgtException if rich authorization requests are disabled.
     */
    public static void assertRichAuthorizationRequestsEnabled() throws APIResourceMgtException {

        if (isRichAuthorizationRequestsDisabled()) {
            throw APIResourceManagementUtil.handleClientException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_PARAM_NOT_SUPPORTED,
                    APIResourceManagementConstants.AUTHORIZATION_DETAILS_TYPES
            );
        }
    }

    /**
     * Checks if the OAuth.EnableRichAuthorizationRequests configuration is enabled.
     *
     * @return <code>true</code> if Rich Authorization Requests are not enabled, <code>false</code> otherwise
     */
    public static boolean isRichAuthorizationRequestsDisabled() {

        return !isRichAuthorizationRequestsEnabled();
    }

    /**
     * Checks if the OAuth.EnableRichAuthorizationRequests configuration is enabled.
     *
     * @return <code>true</code> if Rich Authorization Requests are not enabled, <code>false</code> otherwise
     */
    public static boolean isRichAuthorizationRequestsEnabled() {

        if (APIResourceManagementServiceComponentHolder.getInstance().isRichAuthorizationRequestsEnabled()) {
            return true;
        }

        LOG.debug("Rich Authorization Requests are not enabled.");
        return false;
    }

    private AuthorizationDetailsTypesUtil() {
        // Adding a private constructor to hide the implicit public one.
    }
}
