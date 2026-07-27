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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.application.authentication.framework.util.SessionFilterQueryBuilder;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtUtils;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link UserSessionDAO}. This handles {@link UserSession} related DB operations.
 */
public class UserSessionDAOImpl implements UserSessionDAO {

    public static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";

    public UserSessionDAOImpl() {
    }

    @Override
    public UserSession getSession(String sessionId) throws SessionManagementServerException {

        HashMap<String, String> propertiesMap = new HashMap<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);

        try {
            List<Application> applicationList = getApplicationsForSessionID(sessionId);
            generateApplicationFromAppID(applicationList);
            String sqlStmt = JdbcUtils.isH2DB(JdbcUtils.Database.SESSION)
                    ? SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA_H2
                    : SQLQueries.SQL_GET_PROPERTIES_FROM_SESSION_META_DATA;
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

    @Override
    public Optional<UserSession> getSession(String userId, String sessionId) throws SessionManagementServerException {

        HashMap<String, String> propertiesMap = new HashMap<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);

        try {
            String sqlStmt = JdbcUtils.isH2DB(JdbcUtils.Database.SESSION) ?
                    SQLQueries.SQL_GET_SESSION_META_DATA_FOR_USER_ID_AND_SESSION_ID_H2 :
                    SQLQueries.SQL_GET_SESSION_META_DATA_FOR_USER_ID_AND_SESSION_ID;
            jdbcTemplate.executeQuery(sqlStmt, (
                    (resultSet, rowNumber) -> propertiesMap.put(resultSet.getString(1), resultSet.getString(2))),
                    preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, userId);
                    });

            if (propertiesMap.isEmpty()) {
                return Optional.empty();
            }

            UserSession userSession = new UserSession();
            userSession.setSessionId(sessionId);
            userSession.setUserId(userId);

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
                return Optional.of(userSession);
            }
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION,
                    String.format("%s userId %s",
                            SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION.getDescription(), userId
                    ), e);
        }
        return Optional.empty();
    }

    @Override
    public List<UserSession> getSessions(int tenantId, List<ExpressionNode> filter, Integer limit, String sortOrder)
            throws UserSessionException {

        List<UserSession> userSessionsList = new ArrayList<>();
        Map<String, Application> appDetails = new HashMap<>();
        String appIdFilter = "";
        if (StringUtils.isNotBlank(sortOrder) && !SessionMgtConstants.ASC.equalsIgnoreCase(sortOrder)
                && !SessionMgtConstants.DESC.equalsIgnoreCase(sortOrder)) {
            throw new UserSessionException("Invalid sort order value: " + sortOrder);
        }
        String sqlOrder = SessionMgtConstants.ASC.equalsIgnoreCase(sortOrder)
                ? SessionMgtConstants.ASC : SessionMgtConstants.DESC;
        String sqlQuery;

        SessionFilterQueryBuilder filterBuilder = SessionMgtUtils.getSQLFilterQueryBuilder(filter);

        try {
            if (StringUtils.isNotEmpty(filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION))) {
                String appSql = filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION)
                        + " AND (TENANT_ID = ? OR IS_SAAS_APP = 1)";
                List<Object> appParams = new ArrayList<>(
                        filterBuilder.getFilterParams(SessionMgtConstants.FilterType.APPLICATION));
                appParams.add(tenantId);
                appDetails = getApplicationsForFilter(appSql, appParams);
                if (appDetails.isEmpty()) {
                    return Collections.emptyList();
                }
                appIdFilter = String.format("WHERE APP_ID IN (%s)", StringUtils.join(appDetails.keySet(), ","));
            }
        } catch (DataAccessException e) {
            throw new UserSessionException(
                    String.format("Error while loading sessions from DB: Error while retrieving application details " +
                            "for the tenant with id: %s.", tenantId), e);
        }

        // Resolved once here so the same value is reused in both query-building and param-binding.
        final boolean isJoinBasedQuery;
        try {
            if (JdbcUtils.isH2DB(JdbcUtils.Database.SESSION)) {
                isJoinBasedQuery = true;
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_H2,
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                        appIdFilter, filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.USER),
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), sqlOrder, limit
                );
            } else if (JdbcUtils.isMySQLDB(JdbcUtils.Database.SESSION)) {
                isJoinBasedQuery = true;
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_MYSQL,
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                        appIdFilter, filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.USER),
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), sqlOrder, limit
                );
                sqlQuery = sqlQuery.replaceAll("\\\\", "\\\\\\\\");
            } else if (JdbcUtils.isOracleDB(JdbcUtils.Database.SESSION)) {
                isJoinBasedQuery = false;
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_ORACLE,
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                        appIdFilter, filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.USER),
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), sqlOrder, limit
                );
            } else if (JdbcUtils.isMSSqlDB(JdbcUtils.Database.SESSION)) {
                isJoinBasedQuery = false;
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_MSSQL,
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                        appIdFilter, filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.USER),
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), sqlOrder, limit
                );
            } else if (JdbcUtils.isPostgreSQLDB(JdbcUtils.Database.SESSION)) {
                isJoinBasedQuery = false;
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_POSTGRESQL,
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                        appIdFilter, filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.USER),
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), sqlOrder, limit
                );
            } else if (JdbcUtils.isDB2DB(JdbcUtils.Database.SESSION)) {
                isJoinBasedQuery = false;
                sqlQuery = MessageFormat.format(SQLQueries.SQL_LOAD_SESSIONS_DB2,
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                        appIdFilter, filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.USER),
                        filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), sqlOrder, limit
                );
            } else {
                throw new UserSessionException(String.format("Error while loading sessions from DB: Database driver " +
                        "could not be identified or not supported. TenantId: %s", tenantId));
            }
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while loading sessions from DB: Database driver could not be " +
                    "identified or not supported.", e);
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            Map<String, Application> finalAppDetails = appDetails;
            final SessionFilterQueryBuilder finalFilterBuilder = filterBuilder;
            userSessionsList = jdbcTemplate.executeQuery(
                    sqlQuery,
                    ((resultSet, rowNumber) -> SessionMgtUtils.parseSessionSearchResult(resultSet, finalAppDetails)),
                    preparedStatement -> {
                        int idx = 1;
                        if (isJoinBasedQuery) {
                            idx = bindFilterParams(preparedStatement, finalFilterBuilder, idx,
                                    SessionMgtConstants.FilterType.USER);
                            preparedStatement.setLong(idx++, FrameworkUtils.getCurrentStandardNano());
                            preparedStatement.setInt(idx++, tenantId);
                            idx = bindFilterParams(preparedStatement, finalFilterBuilder, idx,
                                    SessionMgtConstants.FilterType.SESSION);
                        } else {
                            preparedStatement.setLong(idx++, FrameworkUtils.getCurrentStandardNano());
                            preparedStatement.setInt(idx++, tenantId);
                            idx = bindFilterParams(preparedStatement, finalFilterBuilder, idx,
                                    SessionMgtConstants.FilterType.SESSION);
                            idx = bindFilterParams(preparedStatement, finalFilterBuilder, idx,
                                    SessionMgtConstants.FilterType.USER);
                        }
                        bindFilterParams(preparedStatement, finalFilterBuilder, idx,
                                SessionMgtConstants.FilterType.MAIN);
                    });

            /**
             * Application details will be incomplete if an application filter is not provided. In that case
             * requires to query for missing application details.
             * Also requires to query and set idp information. Hence, perform in the same loop to reduce number of
             * iterations.
             */
            if (!userSessionsList.isEmpty()) {
                if (finalAppDetails.isEmpty()) {
                    Set<String> appIdList = new HashSet<>();
                    Set<String> userIdList = new HashSet<>();
                    for (UserSession userSession : userSessionsList) {
                        appIdList.addAll(userSession.getApplications().stream().map(Application::getAppId)
                                .collect(Collectors.toList()));
                        userIdList.add(userSession.getUserId());
                    }
                    Map<String, Application> applicationMap = getApplicationsFromAppID(appIdList);
                    Map<String, String> userIdpMap = getIdpIdsByUserIdList(userIdList);

                    for (UserSession userSession : userSessionsList) {
                        for (Application app : userSession.getApplications()) {
                            Application appFromMap = applicationMap.get(app.getAppId());
                            if (appFromMap != null) {
                                app.setAppName(appFromMap.getAppName());
                                app.setResourceId(appFromMap.getResourceId());
                            }
                        }

                        // If application is not present in the SP_APP table but has a session associated with it,
                        // that application should not be considered for the session object.
                        userSession.getApplications().removeIf(application -> application.getAppName() == null);

                        // Add idp information to the session.
                        userSession.setIdpId(userIdpMap.get(userSession.getUserId()));
                    }
                } else {
                    // Set idp information.
                    Set<String> userIdList = new HashSet<>();
                    for (UserSession userSession : userSessionsList) {
                        userIdList.add(userSession.getUserId());
                    }
                    Map<String, String> userIdpMap = getIdpIdsByUserIdList(userIdList);

                    for (UserSession userSession : userSessionsList) {
                        userSession.setIdpId(userIdpMap.get(userSession.getUserId()));
                    }
                }
            }
        } catch (DataAccessException e) {
            throw new UserSessionException(String.format("Error while retrieving sessions from the database for the " +
                    "tenant with id: %s.", tenantId), e);
        } catch (SessionManagementServerException e) {
            throw new UserSessionException(String.format("Error while retrieving application details for the " +
                    "retrieved sessions for the tenant with id: %s.", tenantId), e);
        }

        return userSessionsList;
    }

    private void generateApplicationFromAppID(List<Application> applications) throws SessionManagementServerException {

        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        Map<String, List<Application>> appIdMap =
                applications.stream().collect(Collectors.groupingBy(Application::getAppId));
        String placeholder = String.join(", ", Collections.nCopies(appIdMap.keySet().size(), "?"));
        // TODO:: Get applications using application-mgt services and remove component unrelated queries.
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

    private Map<String, Application> getApplicationsFromAppID(Set<String> applicationIds)
            throws SessionManagementServerException, DataAccessException {

        if (CollectionUtils.isEmpty(applicationIds)) {
            return new HashMap<>();
        }

        Map<String, Application> applications = new HashMap<>();
        int[] parsedAppIds = new int[applicationIds.size()];
        int idx = 0;
        try {
            for (String appId : applicationIds) {
                parsedAppIds[idx++] = Integer.parseInt(appId);
            }
        } catch (NumberFormatException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_SESSION,
                    "Invalid application ID found in session data: " + applicationIds, e);
        }
        String placeholder = String.join(", ", Collections.nCopies(parsedAppIds.length, "?"));
        // TODO:: Get applications using application-mgt services and remove component unrelated queries.
        String sql = SQLQueries.SQL_GET_APPLICATION.replace(SCOPE_LIST_PLACEHOLDER, placeholder);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        List<Application> result = jdbcTemplate.executeQuery(sql,
                (rs, rowNumber) -> new Application(null, rs.getString("APP_NAME"), rs.getString("ID"),
                        rs.getString("UUID")),
                preparedStatement -> {
                    for (int i = 0; i < parsedAppIds.length; i++) {
                        preparedStatement.setInt(i + 1, parsedAppIds[i]);
                    }
                });
        for (Application app : result) {
            applications.put(app.getAppId(), app);
        }

        return applications;
    }

    private List<Application> getApplicationsForSessionID(String sessionId) throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        return jdbcTemplate.executeQuery(SQLQueries.SQL_GET_APPS_FOR_SESSION_ID,
                (resultSet, rowNumber) ->
                        new Application(resultSet.getString("SUBJECT"),
                                null, resultSet.getString("APP_ID"), null),
                preparedStatement -> preparedStatement.setString(1, sessionId));
    }

    private Map<String, Application> getApplicationsForFilter(String appFilterSql, List<Object> params)
            throws DataAccessException {

        // TODO:: Get applications using application-mgt services and remove component unrelated queries.
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        List<Application> applicationsList = jdbcTemplate.executeQuery(
                MessageFormat.format(SQLQueries.SQL_GET_APPLICATIONS_BY_FILTER_AND_TENANT, appFilterSql),
                (resultSet, rowNumber) ->
                        new Application(null, resultSet.getString("APP_NAME"),
                                resultSet.getString("ID"), resultSet.getString("UUID")),
                preparedStatement -> bindFilterParams(preparedStatement, params, 1));

        return applicationsList.stream().collect(Collectors.toMap(Application::getAppId, app -> app));
    }

    private Map<String, String> getIdpIdsByUserIdList(Set<String> userIdList) throws DataAccessException {

        if (userIdList == null || userIdList.isEmpty()) {
            return null;
        }
        Map<String, String> userIdpMap = new HashMap<>();
        List<String> userIds = new ArrayList<>(userIdList);
        String placeholder = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        String sql = SQLQueries.SQL_GET_IDP_IDS_BY_USER_ID_LIST.replace(SCOPE_LIST_PLACEHOLDER, placeholder);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        List<UserSession> userIdpList = jdbcTemplate.executeQuery(sql,
                ((resultSet, rowNumber) -> {
                    UserSession tempSession = new UserSession();
                    tempSession.setUserId(resultSet.getString("USER_ID"));
                    tempSession.setIdpId(Integer.toString(resultSet.getInt("IDP_ID")));
                    return tempSession;
                }),
                preparedStatement -> {
                    for (int i = 0; i < userIds.size(); i++) {
                        preparedStatement.setString(i + 1, userIds.get(i));
                    }
                });
        for (UserSession userIdpSession : userIdpList) {
            userIdpMap.put(userIdpSession.getUserId(), userIdpSession.getIdpId());
        }

        return userIdpMap;
    }

    /**
     * Binds a list of parameter values to the PreparedStatement starting at {@code startIndex},
     * and returns the next available index.
     */
    private int bindFilterParams(PreparedStatement ps, List<Object> params,
                                 int startIndex) throws SQLException {

        int idx = startIndex;
        for (Object param : params) {
            if (param instanceof Long) {
                ps.setLong(idx++, (Long) param);
            } else if (param instanceof Integer) {
                ps.setInt(idx++, (Integer) param);
            } else {
                ps.setString(idx++, (String) param);
            }
        }
        return idx;
    }

    /**
     * Binds the parameter values for a given {@link SessionMgtConstants.FilterType} to the
     * PreparedStatement starting at {@code startIndex}, and returns the next available index.
     */
    private int bindFilterParams(PreparedStatement ps, SessionFilterQueryBuilder builder,
                                 int startIndex, SessionMgtConstants.FilterType type) throws SQLException {

        return bindFilterParams(ps, builder.getFilterParams(type), startIndex);
    }
}
