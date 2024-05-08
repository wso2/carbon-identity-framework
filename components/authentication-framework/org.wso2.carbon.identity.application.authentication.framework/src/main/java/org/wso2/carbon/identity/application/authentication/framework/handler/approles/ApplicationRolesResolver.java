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

package org.wso2.carbon.identity.application.authentication.framework.handler.approles;

import org.wso2.carbon.identity.application.authentication.framework.handler.approles.exception.ApplicationRolesException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Application associated roles resolver interface.
 */
public interface ApplicationRolesResolver {

    /**
     * Priority of the Application Roles Resolver. Application roles resolvers will be sorted based on their priority
     * value and by default only the resolver with the highest priority will be executed.
     *
     * @return priority of the application roles resolver.
     */
    int getPriority();

    /**
     * Get the application associated roles of the authenticated user.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID of the application.
     * @return Array of application associated roles of the authenticated user.
     * @throws ApplicationRolesException If an error occurs while getting app associated roles.
     */
    String[] getRoles(AuthenticatedUser authenticatedUser, String applicationId) throws ApplicationRolesException;

    /**
     * Get the application associated roles of the authenticated local user.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID of the application.
     * @return Array of application associated roles of the authenticated local user.
     * @throws ApplicationRolesException If an error occurs while getting app associated roles.
     */
    default String[] getAppAssociatedRolesOfLocalUser(AuthenticatedUser authenticatedUser, String applicationId)
            throws ApplicationRolesException {

        return getRoles(authenticatedUser, applicationId);
    }

    /**
     * Get the application associated roles of the authenticated federated user.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID of the application.
     * @param idpGroupClaimURI  IDP group claim URI.
     * @return Array of application associated roles of the authenticated federated user.
     * @throws ApplicationRolesException If an error occurs while getting app associated roles.
     */
    default String[] getAppAssociatedRolesOfFederatedUser(AuthenticatedUser authenticatedUser, String applicationId,
                                                          String idpGroupClaimURI) throws ApplicationRolesException {

        return getRoles(authenticatedUser, applicationId);
    }
}
