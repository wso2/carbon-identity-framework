/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;

/**
 * This class defines utility methods used within this web application.
 */
public class IdentityManagementEndpointUtil {

    private IdentityManagementEndpointUtil() {
    }

    /**
     * Reruns the full qualified username of the user in below format.
     * <user_store_domain>/<username>@<tenant_domain>
     *
     * @param username username of the user
     * @param tenantDomain tenant domain the user belongs to
     * @param userStoreDomain user store domain usee belongs to
     * @return full qualified username
     */
    public static final String getFullQualifiedUsername(String username, String tenantDomain, String userStoreDomain) {
        String fullQualifiedUsername = username;
        if (StringUtils.isNotBlank(userStoreDomain) && !IdentityManagementEndpointConstants.PRIMARY_USER_STORE_DOMAIN
                .equals(userStoreDomain)) {
            fullQualifiedUsername = userStoreDomain + IdentityManagementEndpointConstants.USER_STORE_DOMAIN_SEPARATOR
                                    + fullQualifiedUsername;
        }

        if (StringUtils.isNotBlank(tenantDomain) && !IdentityManagementEndpointConstants.SUPER_TENANT.equals
                (tenantDomain)) {
            fullQualifiedUsername = fullQualifiedUsername + IdentityManagementEndpointConstants
                    .TENANT_DOMAIN_SEPARATOR + tenantDomain;
        }

        return fullQualifiedUsername;
    }

    /**
     * Returns the error to be viewed for end user.
     *
     * @param errorMsgSummary required error message to be viewed
     * @param optionalErrorMsg optional content to be viewed
     * @param statusType status type
     * @return error message to be viewed
     */
    public static String getPrintableError(String errorMsgSummary, String optionalErrorMsg, Response.StatusType
            statusType) {

        StringBuilder errorMsg = new StringBuilder(errorMsgSummary);

        if (statusType != null && StringUtils.isNotBlank(statusType.getReasonPhrase())) {
            errorMsg.append(" ").append(statusType.getReasonPhrase());
        } else if (StringUtils.isNotBlank(optionalErrorMsg)) {
            errorMsg.append(" ").append(optionalErrorMsg);
        }

        return errorMsg.toString();
    }

    /**
     * Returns the end user portal url.
     *
     * @param userPortalUrl configured user portal url
     * @return configured url or the default url if configured url is empty
     */
    public static final String getUserPortalUrl(String userPortalUrl) {
        if (StringUtils.isNotBlank(userPortalUrl)) {
            return userPortalUrl;
        }
        return IdentityManagementEndpointConstants.DEFAULT_USER_PORTAL_URL;
    }

    /**
     * Cast the provided Object to a Boolean
     *
     * @param value Object
     * @return Boolean
     */
    public static boolean getBooleanValue(Object value) {
        if (value != null && value instanceof Boolean) {
            return (Boolean) value;
        }

        return false;
    }

    /**
     * Cast the provided Object to a String
     *
     * @param value Object
     * @return String
     */
    public static String getStringValue(Object value) {
        if (value != null && value instanceof String) {
            return (String) value;
        }

        return "";
    }

    /**
     * Cast provided Object to an Integer
     *
     * @param value Object
     * @return Integer
     */
    public static int getIntValue(Object value) {
        if (value != null && value instanceof Integer) {
            return (Integer) value;
        }

        return 0;
    }

    /**
     * Cast provided Object to a String[]
     *
     * @param value Object
     * @return String[]
     */
    public static String[] getStringArray(Object value) {
        if (value != null && value instanceof String[]) {
            return (String[]) value;
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
