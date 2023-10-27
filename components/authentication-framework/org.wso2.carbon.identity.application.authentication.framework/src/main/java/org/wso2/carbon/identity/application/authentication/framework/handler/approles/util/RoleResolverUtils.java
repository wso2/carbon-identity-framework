/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.approles.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.constant.AppRolesConstants;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.exception.ApplicationRolesClientException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.exception.ApplicationRolesServerException;

/**
 * Utility class for Roles Resolver.
 */
public class RoleResolverUtils {

    /**
     * Throw an ApplicationRolesServerException with the given error message.
     *
     * @param error The error enum.
     * @param e     The throwable.
     * @param data  The error message data.
     * @return ApplicationRolesServerException.
     */
    public static ApplicationRolesServerException handleServerException(
            AppRolesConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new ApplicationRolesServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Throw an ApplicationRolesClientException with the given error message.
     *
     * @param error The error enum.
     * @param data The error message data.
     * @return ApplicationRolesClientException.
     */
    public static ApplicationRolesClientException handleClientException(
            AppRolesConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new ApplicationRolesClientException(error.getMessage(), description, error.getCode());
    }
}
