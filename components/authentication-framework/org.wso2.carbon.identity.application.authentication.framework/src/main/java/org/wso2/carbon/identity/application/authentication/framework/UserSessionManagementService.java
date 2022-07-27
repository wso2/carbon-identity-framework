/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementException;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Defines the session management service operations.
 */
public interface UserSessionManagementService {

    /**
     * Terminates all active sessions of the given user.
     *
     * @param username        Username.
     * @param userStoreDomain Userstore domain of the user.
     * @param tenantDomain    Tenant domain of the user.
     * @throws UserSessionException
     */
    void terminateSessionsOfUser(String username, String userStoreDomain, String tenantDomain) throws
            UserSessionException;

    /**
     * Get all the active sessions of the given user ID.
     *
     * @param userId Unique ID of the user.
     * @return List of user session objects. In the default method, null is returned.
     * @throws SessionManagementException if the session retrieval fails.
     */
    default List<UserSession> getSessionsByUserId(String userId) throws SessionManagementException {

        return null;
    }

    /**
     * Terminate all the active sessions of the given user ID.
     *
     * @param userId Unique ID of the user.
     * @return Whether the sessions termination is success or not. In default method, false is returned.
     * @throws SessionManagementException if the session termination fails.
     */
    default boolean terminateSessionsByUserId(String userId) throws SessionManagementException {

        return false;
    }

    /**
     * Get a specific session of the given user ID.
     *
     * @param userId    Unique ID of the user.
     * @param sessionId Unique ID of the session.
     * @return User session object. In the default method, null is returned.
     * @throws SessionManagementException if the session retrieval fails.
     */
    default Optional<UserSession> getSessionBySessionId(String userId, String sessionId)
            throws SessionManagementException {

        return Optional.empty();
    }

    /**
     * Terminate the session of the given ID.
     *
     * @param userId    Unique ID of the user.
     * @param sessionId Unique ID for the session.
     * @return Whether the session termination is success or not. In default method, false is returned.
     */
    default boolean terminateSessionBySessionId(String userId, String sessionId) throws SessionManagementException {

        return false;
    }

    /**
     * Get all the active sessions of the user relevant to the given Idp.
     *
     * @param user  User object.
     * @param idpId ID of the user's identity provider.
     * @return Whether the sessions termination is success or not. In default method, false is returned.
     * @throws SessionManagementException if the session termination fails.
     */
    default List<UserSession> getSessionsByUser(User user, int idpId) throws SessionManagementException {

        return null;
    }

    /**
     * Terminate all the active sessions of the user relevant to the given Idp.
     *
     * @param user  User object.
     * @param idpId ID of the user's identity provider.
     * @return Whether the sessions termination is success or not. In default method, false is returned.
     * @throws SessionManagementException if the session termination fails.
     */
    default boolean terminateSessionsByUser(User user, int idpId) throws SessionManagementException {

        return false;
    }

    /**
     * Terminate the session of the given ID.
     *
     * @param user      User object.
     * @param idpId     ID of the user's identity provider.
     * @param sessionId Unique ID for the session.
     * @return Whether the session termination is success or not. In default method, false is returned.
     */
    default boolean terminateSessionBySessionId(User user, int idpId, String sessionId) throws
            SessionManagementException {

        return false;
    }

    /**
     * Get active sessions that fulfill the criteria determined by the filter parameter value.
     *
     * @param tenantDomain Context tenant domain.
     * @param filter       Criteria to search for sessions.
     * @param limit        Maximum number of sessions to be returned in the result set.
     * @param sortOrder    Sort direction for results (ASC, DESC).
     * @return List of session search results. In the default method, an empty list is returned.
     * @throws SessionManagementException if the session retrieval fails.
     */
    default List<UserSession> getSessions(String tenantDomain, List<ExpressionNode> filter, Integer limit,
                                          String sortOrder) throws SessionManagementException {

        return Collections.emptyList();
    }

    /**
     * Returns the user session of the given session id.
     *
     * @param sessionId session id.
     * @return user session of the given session id.
     * @throws SessionManagementException if an error occured while retrieving the user session.
     */
    default Optional<UserSession> getUserSessionBySessionId(String sessionId) throws SessionManagementException {

        return Optional.empty();
    }
}
