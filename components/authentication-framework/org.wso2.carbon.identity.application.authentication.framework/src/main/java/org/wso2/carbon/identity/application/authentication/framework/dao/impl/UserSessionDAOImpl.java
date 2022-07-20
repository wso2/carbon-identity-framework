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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtUtils;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

/**
 * Default implementation of {@link UserSessionDAO}. This handles {@link UserSession} related DB operations.
 */
public class UserSessionDAOImpl implements UserSessionDAO {

    public static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";

    public UserSessionDAOImpl() {
    }

    public UserSession getSession(String sessionId) throws SessionManagementServerException {

        HashMap<String, String> propertiesMap = new HashMap<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);

        try {
            List<Application> applicationList = getApplicationsForSessionID(sessionId);
            generateApplicationFromAppID(applicationList);
            String sqlStmt = isH2DB() ? SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA_H2 :
                    SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA;
            jdbcTemplate.executeQuery(sqlStmt, ((resultSet, rowNumber)
                    -> propertiesMap.put(resultSet.getString(1), resultSet.getString(2))), preparedStatement ->
                    preparedStatement.setString(1, sessionId));

            UserSession userSession = new UserSession();
            userSession.setSessionId(sessionId);

            propertiesMap.forEach((key, value) -> {
                switch (key) {
                    case SessionMgtConstants.USER_AGENT:
                        userSession.setUserAgent(value);
                        break;
                    case SessionMgtConstants.IP_ADDRESS:
                        userSession.setIp(value);
                        break;
                    case SessionMgtConstants.LAST_ACCESS_TIME:
                        userSession.setLastAccessTime(value);
                        break;
                    case SessionMgtConstants.LOGIN_TIME:
                        userSession.setLoginTime(value);
                        break;
                }
            });

            if (!applicationList.isEmpty()) {
                userSession.setApplications(applicationList);
                return userSession;
            }
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION,
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION.getDescription(), e);
        }
        return null;
    }

    public UserSession getSession(String userId, String sessionId) throws SessionManagementServerException {

        HashMap<String, String> propertiesMap = new HashMap<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);

        try {
            String sqlStmt = isH2DB() ? SQLQueries.SQL_GET_SESSION_META_DATA_FOR_USER_ID_AND_SESSION_ID_H2 :
                    SQLQueries.SQL_GET_SESSION_META_DATA_FOR_USER_ID_AND_SESSION_ID;
            jdbcTemplate.executeQuery(sqlStmt, (
                    (resultSet, rowNumber) -> propertiesMap.put(resultSet.getString(1), resultSet.getString(2))),
                    preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, userId);
                    });

            if (propertiesMap.isEmpty()) {
                return null;
            }

            UserSession userSession = new UserSession();
            userSession.setSessionId(sessionId);

            propertiesMap.forEach((key, value) -> {
                switch (key) {
                    case SessionMgtConstants.USER_AGENT:
                        userSession.setUserAgent(value);
                        break;
                    case SessionMgtConstants.IP_ADDRESS:
                        userSession.setIp(value);
                        break;
                    case SessionMgtConstants.LAST_ACCESS_TIME:
                        userSession.setLastAccessTime(value);
                        break;
                    case SessionMgtConstants.LOGIN_TIME:
                        userSession.setLoginTime(value);
                        break;
                }
            });

            List<Application> applicationList = getApplicationsForSessionID(sessionId);
            generateApplicationFromAppID(applicationList);

            if (!applicationList.isEmpty()) {
                userSession.setApplications(applicationList);
                return userSession;
            }
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION,
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION.getDescription(), e);
        }
        return null;
    }

    @Override
    public List<UserSession> getSessions(int tenantId, List<ExpressionNode> filter, Integer limit, String sortOrder)
            throws UserSessionException {

        List<UserSession> userSessionsList = new ArrayList<>();
        Map<String, Application> appDetails = new HashMap<>();
        String appIdFilter = "";
        String sqlOrder = StringUtils.isNotBlank(sortOrder) ? sortOrder : SessionMgtConstants.DESC;
        String sqlQuery;

        // This method returns an array of sql filters in the order of session, app, user and main filters.
        String[] sqlFilters = SessionMgtUtils.getSQLFiltersFromExpressionNodes(filter);

        try {
            if (StringUtils.isNotEmpty(sqlFilters[1])) {
                appDetails = getApplicationsForFilter(String.format("%s%s(TENANT_ID = %s OR TENANT_ID = %s)",
                        sqlFilters[1], SessionMgtConstants.AND, tenantId, MultitenantConstants.SUPER_TENANT_ID));
                appIdFilter = String.format("WHERE APP_ID IN (%s)", StringUtils.join(appDetails.keySet(), ","));
            }
        } catch (DataAccessException e) {
            throw new UserSessionException(
                    "Error while loading sessions from DB: Error while retrieving application details.", e);
        }

        try {
            if (JdbcUtils.isH2(JdbcUtils.Database.SESSION)) {
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_H2, sqlFilters[0],
                        appIdFilter, sqlFilters[2], sqlFilters[3], sqlOrder, limit);
            } else if (JdbcUtils.isMySQLDB(JdbcUtils.Database.SESSION)) {
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_MYSQL, sqlFilters[0],
                        appIdFilter, sqlFilters[2], sqlFilters[3], sqlOrder, limit);
            } else if (JdbcUtils.isOracleDB(JdbcUtils.Database.SESSION)) {
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_ORACLE, sqlFilters[0],
                        appIdFilter, sqlFilters[2], sqlFilters[3], sqlOrder, limit);
            } else if (JdbcUtils.isMSSqlDB(JdbcUtils.Database.SESSION)) {
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_MSSQL, sqlFilters[0],
                        appIdFilter, sqlFilters[2], sqlFilters[3], sqlOrder, limit);
            } else if (JdbcUtils.isPostgreSQLDB(JdbcUtils.Database.SESSION)) {
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_POSTGRESQL, sqlFilters[0],
                        appIdFilter, sqlFilters[2], sqlFilters[3], sqlOrder, limit);
            } else if (JdbcUtils.isDB2DB(JdbcUtils.Database.SESSION)) {
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_DB2, sqlFilters[0],
                        appIdFilter, sqlFilters[2], sqlFilters[3], sqlOrder, limit);
            } else {
                throw new UserSessionException("Error while loading sessions from DB: Database driver could not " +
                        "be identified or not supported.");
            }
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while loading sessions from DB: Database driver could not be " +
                    "identified or not supported.", e);
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            Map<String, Application> finalAppDetails = appDetails;
            userSessionsList = jdbcTemplate.executeQuery(
                    sqlQuery,
                    ((resultSet, rowNumber) -> SessionMgtUtils.parseSessionSearchResult(resultSet, finalAppDetails)),
                    preparedStatement -> {
                        preparedStatement.setLong(1, FrameworkUtils.getCurrentStandardNano());
                        preparedStatement.setInt(2, tenantId);
                    });

            // Application details will be incomplete if an application filter is not provided. In that case
            // requires to query for missing application details.
            if (!userSessionsList.isEmpty() && finalAppDetails.isEmpty()) {
                for (UserSession userSession : userSessionsList) {
                    generateApplicationFromAppID(userSession.getApplications());
                }
            }
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving sessions from the database.", e);
        } catch (SessionManagementServerException e) {
            throw new UserSessionException(
                    "Error while retrieving application details for the retrieved sessions.", e);
        }

        return userSessionsList;
    }

    private void generateApplicationFromAppID(List<Application> applications) throws SessionManagementServerException {

        Map<String, List<Application>> appIdMap =
                applications.stream().collect(Collectors.groupingBy(Application::getAppId));
        String placeholder = String.join(", ", Collections.nCopies(appIdMap.keySet().size(), "?"));
        String sql = SQLQueries.SQL_GET_APPLICATION.replace(SCOPE_LIST_PLACEHOLDER, placeholder);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (String appId : appIdMap.keySet()) {
                ps.setInt(index, Integer.parseInt(appId));
                index++;
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    for (Application application : appIdMap.get(rs.getString("ID"))) {
                        application.setAppName(rs.getString("APP_NAME"));
                        application.setResourceId(rs.getString("UUID"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION,
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION.getDescription(), e);
        }
        /**
         * If application is not present in the SP_APP table but has a session associated with it that application
         * should not be considered for the session object.
         */
        applications.removeIf(application -> application.getAppName() == null);
    }

    private List<Application> getApplicationsForSessionID(String sessionId) throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        return jdbcTemplate.executeQuery(SQLQueries.SQL_GET_APPS_FOR_SESSION_ID,
                (resultSet, rowNumber) ->
                        new Application(resultSet.getString("SUBJECT"),
                                null, resultSet.getString("APP_ID"), null),
                preparedStatement -> preparedStatement.setString(1, sessionId));
    }

    private Map<String, Application> getApplicationsForFilter(String appFilter)
            throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        List<Application> applicationsList = jdbcTemplate.executeQuery(
                MessageFormat.format(SQLQueries.SQL_GET_APPLICATIONS_BY_FILTER_AND_TENANT, appFilter),
                (resultSet, rowNumber) ->
                    new Application(null, resultSet.getString("APP_NAME"), resultSet.getString("ID"),
                            resultSet.getString("UUID")));

        return applicationsList.stream().collect(Collectors.toMap(Application::getAppId, app -> app));
    }
}
