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

package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;

import java.util.List;

/**
 * Default implementation of {@link UserSessionDAO}. This handles {@link UserSession} related DB operations.
 */
public class UserSessionDAOImpl implements UserSessionDAO {

    public UserSessionDAOImpl() {
    }

    public UserSession getSession(String sessionId) throws SessionManagementServerException {

        List<Application> applicationList;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            applicationList = jdbcTemplate.executeQuery(SQLQueries.SQL_GET_APPLICATION, (resultSet, rowNumber) ->
                            new Application(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3)),
                    preparedStatement -> preparedStatement.setString(1, sessionId));

            String userAgent = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA,
                    (resultSet, rowNumber) -> resultSet.getString(1), preparedStatement -> {
                        preparedStatement.setString(1, SessionMgtConstants.USER_AGENT);
                        preparedStatement.setString(2, sessionId);
                    });

            String ip = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA,
                    (resultSet, rowNumber) -> resultSet.getString(1), preparedStatement -> {
                        preparedStatement.setString(1, SessionMgtConstants.IP_ADDRESS);
                        preparedStatement.setString(2, sessionId);
                    });

            String loginTime = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA,
                    (resultSet, rowNumber) -> resultSet.getString(1), preparedStatement -> {
                        preparedStatement.setString(1, SessionMgtConstants.LOGIN_TIME);
                        preparedStatement.setString(2, sessionId);
                    });

            String lastAccessTime = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA,
                    (resultSet, rowNumber) -> resultSet.getString(1), preparedStatement -> {
                        preparedStatement.setString(1, SessionMgtConstants.LAST_ACCESS_TIME);
                        preparedStatement.setString(2, sessionId);
                    });

            if (!applicationList.isEmpty()) {
                UserSession userSession = new UserSession();
                userSession.setApplications(applicationList);
                userSession.setUserAgent(userAgent);
                userSession.setIp(ip);
                userSession.setLoginTime(loginTime);
                userSession.setLastAccessTime(lastAccessTime);
                userSession.setSessionId(sessionId);
                return userSession;
            }
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_GET_SESSION, "Server encountered an error while retrieving session " +
                    "information for the session " + sessionId, e);
        }
        return null;
    }
}
