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

package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.exception.ApplicationRolesException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Application roles resolver interface. Any application roles resolver should implement this interface.
 */
public interface ApplicationRolesResolver {

    /**
     * Get the application roles for the authenticated user.
     *
     * @param authenticatedUser Authenticated user to get the application roles for.
     * @param applicationId     Application ID of the application.
     * @return Array of application roles.
     * @throws ApplicationRolesException ApplicationRolesException when an error occurs while getting the application roles.
     */
    String[] getRoles(AuthenticatedUser authenticatedUser, String applicationId) throws ApplicationRolesException;

}
