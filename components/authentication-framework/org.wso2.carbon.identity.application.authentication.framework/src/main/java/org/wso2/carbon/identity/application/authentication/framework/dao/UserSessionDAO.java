/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.dao;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.FederatedUserSession;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.util.List;

/**
 * Perform operations for {@link UserSession}.
 */
public interface UserSessionDAO {

    /**
     * Method to retrieve session information for a given session id.
     *
     * @param sessionId Id of the session.
     * @return User session.
     * @throws SessionManagementServerException
     */
    UserSession getSession(String sessionId) throws SessionManagementServerException;

    /**
     * Method to retrieve session information for a given user and session id.
     *
     * @param userId Id of the user.
     * @param sessionId Id of the session.
     * @return User session.
     * @throws SessionManagementServerException
     */
    UserSession getSession(String userId, String sessionId) throws SessionManagementServerException;

    /**
     * Method to search active sessions on the system.
     *
     * @param tenantId  Context tenant ID.
     * @param filter    Filter expression nodes.
     * @param limit     Limit.
     * @param sortOrder Order direction (ASC, DESC).
     * @return The list of sessions found.
     * @throws UserSessionException if an error occurs when retrieving the sessions from the database.
     */
    List<UserSession> getSessions(int tenantId, List<ExpressionNode> filter, Integer limit, String sortOrder)
            throws UserSessionException;

    /**
     * Get federated user session details mapped for federated IDP sessionId.
     *
     * @param fedIdpSessionId sid claim in the logout token of the federated idp.
     * @return A FederatedUserSession containing federated authentication session details.
     * @throws SessionManagementServerException
     */
    default FederatedUserSession getFederatedAuthSessionDetails(String fedIdpSessionId)
            throws SessionManagementServerException {

        FederatedUserSession federatedUserSession;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            federatedUserSession = jdbcTemplate
                    .fetchSingleRecord(SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_INFO_BY_SESSION_ID,
                            (resultSet, rowNumber) -> new FederatedUserSession(
                                    resultSet.getString(SessionMgtConstants.FEDERATED_IDP_SESSION_ID),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_SESSION_ID),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_IDP_NAME),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_AUTHENTICATOR_ID),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_PROTOCOL_TYPE)),
                            preparedStatement -> preparedStatement.setString(1, fedIdpSessionId));
            return federatedUserSession;
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION,
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION.getDescription(), e);
        }
    }
}
