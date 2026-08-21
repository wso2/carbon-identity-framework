/*
 * Copyright (c) 2019-2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
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
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link UserSessionDAO}. This handles {@link UserSession} related DB operations.
 * <p>
 * Constructing this class uses the relational store regardless of the configured one. Use
 * {@code UserSessionDAOFactory.getUserSessionDAO()} to obtain the DAO of the configured store.
 */
public class UserSessionDAOImpl implements UserSessionDAO {

    private static final Log log = LogFactory.getLog(UserSessionDAOImpl.class);

    public static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";

    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";
    private static final String DELETE_CHUNK_SIZE_PROPERTY = "JDBCPersistenceManager.SessionDataPersist" +
            ".UserSessionMapping.DeleteChunkSize";
    private static final String IDN_AUTH_USER_SESSION_MAPPING_TABLE = "IDN_AUTH_USER_SESSION_MAPPING";
    private static final String IDN_AUTH_SESSION_APP_INFO_TABLE = "IDN_AUTH_SESSION_APP_INFO_TABLE";
    private static final String IDN_AUTH_SESSION_META_DATA_TABLE = "IDN_AUTH_SESSION_META_DATA";

    private static final int DEFAULT_DELETE_CHUNK_SIZE = 10000;

    private static volatile Integer deleteChunkSize;

    @Override
    public UserSession getSession(String sessionId) throws SessionManagementServerException {

        HashMap<String, String> propertiesMap = new HashMap<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);

        try {
            List<Application> applicationList = getApplicationsForSessionID(sessionId);
            SessionMgtUtils.setApplicationDetails(applicationList);
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
            SessionMgtUtils.setApplicationDetails(applicationList);

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
                appDetails = SessionMgtUtils.getApplicationsByFilter(filterBuilder, tenantId);
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
                    Map<String, Application> applicationMap = SessionMgtUtils
                            .getApplicationsByIds(appIdList);
                    Map<String, String> userIdpMap = SessionMgtUtils.getIdpIdsByUserIds(userIdList);

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
                    Map<String, String> userIdpMap = SessionMgtUtils.getIdpIdsByUserIds(userIdList);

                    for (UserSession userSession : userSessionsList) {
                        userSession.setIdpId(userIdpMap.get(userSession.getUserId()));
                    }
                }
            }
        } catch (DataAccessException e) {
            throw new UserSessionException(String.format("Error while retrieving sessions from the database for the " +
                    "tenant with id: %s.", tenantId), e);
        }

        return userSessionsList;
    }

    /**
     * Returns the configured number of session records removed per batch.
     *
     * @return the delete chunk size.
     */
    private static int getDeleteChunkSize() {

        if (deleteChunkSize == null) {
            int resolved = DEFAULT_DELETE_CHUNK_SIZE;
            String deleteChunkSizeString = IdentityUtil.getProperty(DELETE_CHUNK_SIZE_PROPERTY);
            if (StringUtils.isNotBlank(deleteChunkSizeString)) {
                try {
                    resolved = Integer.parseInt(deleteChunkSizeString);
                } catch (NumberFormatException e) {
                    log.error("Error while parsing the delete chunk size: " + deleteChunkSizeString
                            + ". Proceeding with the default value: " + resolved + ".", e);
                }
            }
            deleteChunkSize = resolved;
        }
        return deleteChunkSize;
    }

    private List<Application> getApplicationsForSessionID(String sessionId) throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        return jdbcTemplate.executeQuery(SQLQueries.SQL_GET_APPS_FOR_SESSION_ID,
                (resultSet, rowNumber) ->
                        new Application(resultSet.getString("SUBJECT"),
                                null, resultSet.getString("APP_ID"), null),
                preparedStatement -> preparedStatement.setString(1, sessionId));
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

    /**
     * Method to store user id and session id mapping in the database table IDN_AUTH_USER_SESSION_MAPPING.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @throws UserSessionException if an error occurs when storing the mapping in the database
     */
    @Override
    public void storeUserSessionData(String userId, String sessionId) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_INSERT_USER_SESSION_STORE_OPERATION)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, sessionId);
                preparedStatement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Stored user session data for user " + userId + " with session id: " + sessionId);
                }
            } catch (SQLIntegrityConstraintViolationException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new DuplicatedAuthUserException("Mapping between user Id: " + userId + " and session Id: "
                        + sessionId + " already exists in the database.", e1);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                // Handle constrain violation issue in JDBC drivers which does not throw
                // SQLIntegrityConstraintViolationException
                if (StringUtils.containsIgnoreCase(e1.getMessage(), "USER_SESSION_STORE_CONSTRAINT")) {
                    throw new DuplicatedAuthUserException("Mapping between user Id: " + userId + " and session Id: "
                            + sessionId + " already exists in the database.", e1);
                } else {
                    throw new UserSessionException("Error while storing mapping between user Id: " + userId +
                            " and session Id: " + sessionId, e1);
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while storing mapping between user Id: " + userId +
                    " and session Id: " + sessionId, e);
        }
    }

    /**
     * Method to check whether the user id and session id mapping is already exists in the database.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    @Override
    public boolean isExistingMapping(String userId, String sessionId) throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_USER_SESSION_MAP)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, sessionId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isExisting = true;
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving existing mapping between user Id: " + userId
                        + " and session Id: " + sessionId, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user Id: " + userId
                    + " and session Id: " + sessionId, e);
        }
        return isExisting;
    }

    /**
     * Method to get session Id list of a given user Id.
     *
     * @param userId id of the user
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    @Override
    public List<String> getSessionId(String userId) throws UserSessionException {

        List<String> sessionIdList = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_SESSION_ID_OF_USER_ID)) {
                preparedStatement.setString(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        sessionIdList.add(resultSet.getString(1));
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving session Id of user Id: " + userId, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving session Id of user Id: " + userId, e);
        }
        return sessionIdList;
    }

    /**
     * Removes all the expired session records from relevant tables.
     */
    @Override
    public void removeExpiredSessionRecords() {

        if (log.isDebugEnabled()) {
            log.debug("Removing information of expired and deleted sessions.");
        }

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            Set<String> terminatedAuthSessionIds = getSessionsTerminated(connection);
            String[] sessionsToRemove = new String[terminatedAuthSessionIds.size()];
            terminatedAuthSessionIds.toArray(sessionsToRemove);

            if (!terminatedAuthSessionIds.isEmpty()) {

                if (log.isDebugEnabled()) {
                    log.debug(terminatedAuthSessionIds.size() + " number of sessions should be removed from the " +
                            "database. Removing in " + getDeleteChunkSize() + " size batches.");
                }

                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_USER_SESSION_MAPPING_TABLE,
                        SQLQueries.SQL_DELETE_TERMINATED_SESSION_DATA);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_APP_INFO_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_APP_INFO);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_META_DATA_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_META_DATA);

                IdentityDatabaseUtil.commitTransaction(connection);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No expired sessions found to remove.");
                }
            }
        } catch (SQLException e) {
            log.error("Error while removing expired session information from the database.", e);
        }
    }

    /**
     * Remove the session information records of a given set of session IDs from the relevant tables.
     *
     * @param sessionIdList list of terminated session IDs
     */
    @Override
    public void removeTerminatedSessionRecords(List<String> sessionIdList) {

        String[] sessionsToRemove = sessionIdList.toArray(new String[0]);

        if (log.isDebugEnabled()) {
            log.debug("Removing meta information of the deleted sessions.");
        }

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            try {
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_USER_SESSION_MAPPING_TABLE,
                        SQLQueries.SQL_DELETE_TERMINATED_SESSION_DATA);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_APP_INFO_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_APP_INFO);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_META_DATA_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_META_DATA);
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                log.error("Error while removing the terminated session information from the database.", e1);
            }
        } catch (SQLException e) {
            log.error("Error while obtaining the db connection to remove terminated session information", e);
        }
    }

    /**
     * Method to store app session data.
     *
     * @param sessionId   id of the authenticated session
     * @param subject     username in application
     * @param appID       id of the application
     * @param inboundAuth protocol used in app
     * @throws DataAccessException if an error occurs when storing the authenticated user details to the database
     */
    @Override
    public void storeAppSessionData(String sessionId, String subject, int appID, String inboundAuth) throws
            DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            jdbcTemplate.withTransaction(template -> {
                String query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_H2;
                if (JdbcUtils.isOracleDB(JdbcUtils.Database.SESSION)) {
                    query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE;
                    template.executeUpdate(query, preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setString(4, inboundAuth);
                        preparedStatement.setString(5, sessionId);
                        preparedStatement.setString(6, subject);
                        preparedStatement.setInt(7, appID);
                        preparedStatement.setString(8, inboundAuth);
                    });
                } else {
                    if (JdbcUtils.isMSSqlDB(JdbcUtils.Database.SESSION)) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MSSQL;
                    } else if (JdbcUtils.isDB2DB(JdbcUtils.Database.SESSION)) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_DB2;
                    } else if (JdbcUtils.isMySQLDB(JdbcUtils.Database.SESSION) ||
                            JdbcUtils.isMariaDB(JdbcUtils.Database.SESSION)) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB;
                    } else if (JdbcUtils.isPostgreSQLDB(JdbcUtils.Database.SESSION)) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_POSTGRES;
                    } else if (JdbcUtils.isOracleDB(JdbcUtils.Database.SESSION)) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE;
                    }
                    template.executeUpdate(query, preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setString(4, inboundAuth);
                    });
                }
                return null;
            });
        } catch (TransactionException e) {
            throw new DataAccessException("Error while storing application data of session id: " +
                    sessionId + ", subject: " + subject + ", app Id: " + appID + ", protocol: " + inboundAuth + ".", e);
        }
    }

    /**
     * Method to check whether the particular app session is already exists in the database.
     *
     * @param sessionId   id of the authenticated session
     * @param subject     user name of app
     * @param appID       id of application
     * @param inboundAuth protocol used in app
     * @return whether the app session is already available or not
     * @throws UserSessionException while retrieving existing session data
     */
    @Override
    public boolean isExistingAppSession(String sessionId, String subject, int appID, String inboundAuth) throws
            UserSessionException {

        Integer recordCount;

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            recordCount = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_CHECK_IDN_AUTH_SESSION_APP_INFO,
                    (resultSet, rowNumber) -> resultSet.getInt(1),
                    preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setString(4, inboundAuth);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving application data of session id: " +
                    sessionId + ", subject: " + subject + ", app Id: " + appID + ", protocol: " + inboundAuth + ".", e);
        }
        return recordCount != null;
    }

    /**
     * Method to store session meta data as a batch.
     *
     * @param sessionId id of the authenticated session
     * @param metaData  map of metadata type and value of the session
     * @throws UserSessionException while storing session meta data
     */
    @Override
    public void storeSessionMetaData(String sessionId, Map<String, String> metaData) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            String sqlStmt = JdbcUtils.isH2DB(JdbcUtils.Database.SESSION) ?
                    SQLQueries.SQL_INSERT_SESSION_META_DATA_H2 : SQLQueries.SQL_INSERT_SESSION_META_DATA;
            jdbcTemplate.executeBatchInsert(sqlStmt, (preparedStatement -> {
                for (Map.Entry<String, String> entry : metaData.entrySet()) {
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, entry.getKey());
                    preparedStatement.setString(3, entry.getValue());
                    preparedStatement.addBatch();
                }
            }), sessionId);
            if (log.isDebugEnabled()) {
                log.debug("Inserted metadata for session id: " + sessionId);
            }
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while storing metadata of session:" + sessionId +
                    " in table " + IDN_AUTH_SESSION_META_DATA_TABLE + ".", e);
        }
    }

    /**
     * Update session meta data.
     *
     * @param sessionId    id of the authenticated session
     * @param propertyType type of the meta data
     * @param value        value of the meta data
     * @throws UserSessionException if the meta data update in the database fails.
     */
    @Override
    public void updateSessionMetaData(String sessionId, String propertyType, String value) throws
            UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            String sqlStmt = JdbcUtils.isH2DB(JdbcUtils.Database.SESSION) ?
                    SQLQueries.SQL_UPDATE_SESSION_META_DATA_H2 : SQLQueries.SQL_UPDATE_SESSION_META_DATA;
            jdbcTemplate.executeUpdate(sqlStmt, preparedStatement -> {
                preparedStatement.setString(1, value);
                preparedStatement.setString(2, sessionId);
                preparedStatement.setString(3, propertyType);
            });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while updating " + propertyType + " of session:" + sessionId +
                    " in table " + IDN_AUTH_SESSION_META_DATA_TABLE + ".", e);
        }
    }

    /**
     * Method to get session Id list of a given user.
     *
     * @param user  user object
     * @param idpId id of the user's idp
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    @Override
    public List<String> getSessionId(User user, int idpId) throws UserSessionException {

        List<String> sessionIdList = new ArrayList<>();
        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_GET_SESSIONS_BY_USER)) {
                preparedStatement.setString(1, user.getUserName());
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (user.getUserStoreDomain() == null) ? FEDERATED_USER_DOMAIN :
                        user.getUserStoreDomain().toUpperCase());
                preparedStatement.setInt(4, idpId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        sessionIdList.add(resultSet.getString(1));
                    }
                }
            } catch (SQLException ex) {
                throw new UserSessionException("Error while retrieving session IDs of user: " +
                        user.getLoggableUserId() + ".", ex);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving session IDs of user: " +
                    user.getLoggableUserId() + ".", e);
        }
        return sessionIdList;
    }

    /**
     * Method to check whether a given user already has a mapping with a given session id.
     *
     * @param user      user object
     * @param sessionId id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    @Override
    public boolean isExistingMapping(User user, int idpId, String sessionId) throws UserSessionException {

        boolean isExisting = false;

        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_GET_SESSION_MAPPING_BY_USER)) {
                preparedStatement.setString(1, sessionId);
                preparedStatement.setString(2, user.getUserName());
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, (user.getUserStoreDomain() == null) ? FEDERATED_USER_DOMAIN :
                        user.getUserStoreDomain().toUpperCase());
                preparedStatement.setInt(5, idpId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isExisting = true;
                    }
                }
            } catch (SQLException ex) {
                throw new UserSessionException("Error while retrieving existing mapping between user : " + user
                        .getLoggableUserId() + " and session Id: " + sessionId + ".", ex);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user : " + user
                    .getLoggableUserId() + " and session Id: " + sessionId + ".", e);
        }
        return isExisting;
    }

    /**
     * Store session details of a given session context key to map the session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory)
            throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt
                         = connection.prepareStatement(SQLQueries.SQL_STORE_FEDERATED_AUTH_SESSION_INFO)) {
                prepStmt.setString(1, authHistory.getIdpSessionIndex());
                prepStmt.setString(2, sessionContextKey);
                prepStmt.setString(3, authHistory.getIdpName());
                prepStmt.setString(4, authHistory.getAuthenticatorName());
                prepStmt.setString(5, authHistory.getRequestType());
                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserSessionException("Error while adding session details of the session index:"
                        + sessionContextKey + ", IdP:" + authHistory.getIdpName(), e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while adding session details of the session index:"
                    + sessionContextKey + ", IdP:" + authHistory.getIdpName(), e);
        }
    }

    /**
     * Store session details with the given session context key for the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId)
            throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement prepStmt = connection
                     .prepareStatement(SQLQueries.SQL_STORE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT)) {
            prepStmt.setString(1, authHistory.getIdpSessionIndex());
            prepStmt.setString(2, sessionContextKey);
            prepStmt.setString(3, authHistory.getIdpName());
            prepStmt.setString(4, authHistory.getAuthenticatorName());
            prepStmt.setString(5, authHistory.getRequestType());
            prepStmt.setInt(6, tenantId);
            prepStmt.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            String msg = String.format("Error while adding session details of the session index: %s, IdP: %s " +
                    "and tenant id: %s.", sessionContextKey, authHistory.getIdpName(), tenantId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Store session details with the given session context key for the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param idpId             Federated IDP ID.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void storeFederatedAuthSessionInfoWithIdpId(String sessionContextKey, AuthHistory authHistory, int idpId)
            throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement prepStmt = connection
                     .prepareStatement(SQLQueries.SQL_STORE_FEDERATED_AUTH_SESSION_INFO_WITH_IDP_ID)) {
            prepStmt.setString(1, authHistory.getIdpSessionIndex());
            prepStmt.setString(2, sessionContextKey);
            prepStmt.setString(3, authHistory.getIdpName());
            prepStmt.setString(4, authHistory.getAuthenticatorName());
            prepStmt.setString(5, authHistory.getRequestType());
            prepStmt.setInt(6, idpId);
            prepStmt.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            String msg = String.format("Error while adding session details of the session index: %s, IdP: %s " +
                    "and IdP ID: %s.", sessionContextKey, authHistory.getIdpName(), idpId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Store session details if not exist of a given session context key to map the session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @param idpId             Federated IdP id.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId,
                                              int idpId) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection
                     .prepareStatement(SQLQueries.SQL_STORE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT_AND_IDP_ID)) {
            prepStmt.setString(1, authHistory.getIdpSessionIndex());
            prepStmt.setString(2, sessionContextKey);
            prepStmt.setString(3, authHistory.getIdpName());
            prepStmt.setString(4, authHistory.getAuthenticatorName());
            prepStmt.setString(5, authHistory.getRequestType());
            prepStmt.setInt(6, tenantId);
            prepStmt.setInt(7, idpId);
            prepStmt.execute();
        } catch (SQLException e) {
            String msg = String.format("Error while adding session details of the session index: %s, IdP: %s " +
                    "and tenant id: %s.", sessionContextKey, authHistory.getIdpName(), tenantId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory) throws
            UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO, preparedStatement -> {
                preparedStatement.setString(1, sessionContextKey);
                preparedStatement.setString(2, authHistory.getIdpSessionIndex());
            });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while updating " + sessionContextKey + " of session:" +
                    authHistory.getIdpSessionIndex() + " in table " + IDN_AUTH_SESSION_META_DATA_TABLE + ".", e);
        }
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId) throws
            UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(
                    SQLQueries.SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT, preparedStatement -> {
                        preparedStatement.setString(1, sessionContextKey);
                        preparedStatement.setString(2, authHistory.getIdpSessionIndex());
                        preparedStatement.setInt(3, tenantId);
                    });
        } catch (DataAccessException e) {
            String msg = String.format("Error while updating %s of session: %s in table " +
                            "IDN_FED_AUTH_SESSION_MAPPING for tenant id %s.", sessionContextKey,
                    authHistory.getIdpSessionIndex(), tenantId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param idpId             Federated IDP id.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void updateFederatedAuthSessionInfoWithIdpId(String sessionContextKey, AuthHistory authHistory, int idpId)
            throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(
                    SQLQueries.SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO_WITH_IDP_ID, preparedStatement -> {
                        preparedStatement.setString(1, sessionContextKey);
                        preparedStatement.setString(2, authHistory.getIdpSessionIndex());
                        preparedStatement.setInt(3, idpId);
                    });
        } catch (DataAccessException e) {
            String msg = String.format("Error while updating %s of session: %s in table " +
                            "IDN_FED_AUTH_SESSION_MAPPING for idp id %s.", sessionContextKey,
                    authHistory.getIdpSessionIndex(), idpId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @param idpId             Federated IdP id.
     * @throws UserSessionException Error while storing session details.
     */
    @Override
    public void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId,
            int idpId) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(
                    SQLQueries.SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT_AND_IDP_ID, preparedStatement -> {
                        preparedStatement.setString(1, sessionContextKey);
                        preparedStatement.setString(2, authHistory.getIdpSessionIndex());
                        preparedStatement.setInt(3, tenantId);
                        preparedStatement.setInt(4, idpId);
                    });
        } catch (DataAccessException e) {
            String msg = String.format("Error while updating %s of session: %s in table " +
                            "IDN_FED_AUTH_SESSION_MAPPING for tenant id %s and idp id %s", sessionContextKey,
                    authHistory.getIdpSessionIndex(), tenantId, idpId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Check whether there is already existing federated auth session with the given session index.
     *
     * @param idpSessionIndex IDP session index.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    @Override
    public boolean hasExistingFederatedAuthSession(String idpSessionIndex) throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt
                     = connection.prepareStatement(SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID)) {
            prepStmt.setString(1, idpSessionIndex);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error occurred while checking for an federated auth session " +
                    "with session index: " + idpSessionIndex, e);
        }
        return isExisting;
    }

    /**
     * Check whether there is already existing federated auth session with the given session index and the tenant id.
     *
     * @param idpSessionIndex IDP session index.
     * @param tenantId        Tenant id.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    @Override
    public boolean isExistingFederatedAuthSessionAvailable(String idpSessionIndex, int tenantId)
            throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(
                     SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID_WITH_TENANT)) {
            prepStmt.setString(1, idpSessionIndex);
            prepStmt.setInt(2, tenantId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }
        } catch (SQLException e) {
            String msg = String.format("Error occurred while checking for a federated auth session with " +
                    "session index: %s and tenant id: %s", idpSessionIndex, tenantId);
            throw new UserSessionException(msg, e);
        }
        return isExisting;
    }

    /**
     * Check whether there is already existing federated auth session with the given session index and the idp id.
     *
     * @param idpSessionIndex IDP session index.
     * @param idpId           Federated IDP ID.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    @Override
    public boolean hasExistingFederatedAuthSessionWithIdpId(String idpSessionIndex, int idpId)
            throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(
                     SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID_WITH_IDP_ID)) {
            prepStmt.setString(1, idpSessionIndex);
            prepStmt.setInt(2, idpId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }
        } catch (SQLException e) {
            String msg = String.format("Error occurred while checking for a federated auth session with " +
                    "session index: %s and idp id: %s", idpSessionIndex, idpId);
            throw new UserSessionException(msg, e);
        }
        return isExisting;
    }

    /**
     * Check whether there is already existing federated auth session with the given session index, tenant id and
     * idp id.
     *
     * @param idpSessionIndex IDP session index.
     * @param tenantId        Tenant id.
     * @param idpId           Federated IDP id.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    @Override
    public boolean hasExistingFederatedAuthSession(String idpSessionIndex, int tenantId, int idpId)
            throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(
                     SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID_WITH_TENANT_AND_IDP_ID)) {
            prepStmt.setString(1, idpSessionIndex);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, idpId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }
        } catch (SQLException e) {
            String msg = String.format("Error occurred while checking for a federated auth session with " +
                    "session index: %s ,tenant id: %s and idp id: %s", idpSessionIndex, tenantId, idpId);
            throw new UserSessionException(msg, e);
        }
        return isExisting;
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey Session Context Key.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    @Override
    public void removeFederatedAuthSessionInfo(String sessionContextKey) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt
                         = connection.prepareStatement(SQLQueries.SQL_DELETE_FEDERATED_AUTH_SESSION_INFO)) {
                prepStmt.setString(1, sessionContextKey);
                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserSessionException("Error while removing federated authentication session details of " +
                        "the session index:" + sessionContextKey, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while removing federated authentication session details of " +
                    "the session index:" + sessionContextKey, e);
        }
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey     Session Context Key.
     * @param idpId                 ID of the federated IdP.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    @Override
    public void removeFederatedAuthSessionInfo(String sessionContextKey, int idpId) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt
                         = connection.prepareStatement(SQLQueries.SQL_DELETE_FEDERATED_AUTH_SESSION_INFO_WITH_IDP_ID)) {
                prepStmt.setString(1, sessionContextKey);
                prepStmt.setInt(2, idpId);
                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserSessionException("Error while removing federated authentication session details of " +
                        "the session index:" + sessionContextKey, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while removing federated authentication session details of " +
                    "the session index:" + sessionContextKey, e);
        }
    }

    /**
     * Counts the number of active sessions of the given tenant domain. For a session to be active, the last access
     * time of the session should not be earlier than the session timeout time.
     *
     * @param tenantDomain tenant domain
     * @return number of active sessions of the given tenant domain
     * @throws UserSessionException if something goes wrong
     */
    @Override
    public int getActiveSessionCount(String tenantDomain) throws UserSessionException {

        Set<String> activeSessionIds = new HashSet<>();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        long idleSessionTimeOut = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));
        long currentTime = System.currentTimeMillis();
        long minIdleTimestamp = currentTime - idleSessionTimeOut;

        Optional<Integer> maxSessionTimeout = IdPManagementUtil.getMaximumSessionTimeout(tenantDomain);
        Long minSessionTimestamp = maxSessionTimeout.isPresent()
                ? currentTime - TimeUnit.SECONDS.toMillis(maxSessionTimeout.get()) : null;

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            String sqlStmt;
            if (minSessionTimestamp != null) {
                sqlStmt = JdbcUtils.isH2DB(JdbcUtils.Database.SESSION)
                        ? SQLQueries.SQL_GET_SESSION_OPS_BY_TENANT_WITH_IDLE_AND_MAX_TIMEOUT_H2
                        : SQLQueries.SQL_GET_SESSION_OPS_BY_TENANT_WITH_IDLE_AND_MAX_TIMEOUT;
            } else {
                sqlStmt = JdbcUtils.isH2DB(JdbcUtils.Database.SESSION)
                        ? SQLQueries.SQL_GET_SESSION_OPERATIONS_WITHIN_IDLE_SESSION_TIMEOUT_BY_TENANT_H2
                        : SQLQueries.SQL_GET_SESSION_OPERATIONS_WITHIN_IDLE_SESSION_TIMEOUT_BY_TENANT;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStmt)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, SessionMgtConstants.LAST_ACCESS_TIME);
                preparedStatement.setString(3, String.valueOf(minIdleTimestamp));
                preparedStatement.setString(4, String.valueOf(currentTime));
                if (minSessionTimestamp != null) {
                    preparedStatement.setString(5, SessionMgtConstants.LOGIN_TIME);
                    preparedStatement.setString(6, String.valueOf(minSessionTimestamp));
                    preparedStatement.setString(7, String.valueOf(currentTime));
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String sessionId = resultSet.getString(1);
                        String operation = resultSet.getString(2);

                        if (StringUtils.equalsIgnoreCase(operation, "DELETE")) {
                            // If the session is already logged out, remove it from the active session set.
                            activeSessionIds.remove(sessionId);
                            continue;
                        }
                        activeSessionIds.add(sessionId);
                    }
                }
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (DataAccessException | SQLException e) {
            throw new UserSessionException("Error while retrieving active session count of the tenant domain, " +
                    tenantDomain, e);
        }
        return activeSessionIds.size();
    }

    private Set<String> getSessionsTerminated(Connection connection) throws SQLException {

        Set<String> terminatedSessionIds = new HashSet<>();

        /**
         * Retrieve only sessions which have an expiry time less than the current time.
         * As the session cleanup task deletes only entries matching the same condition, in case sessions that are
         * being marked as deleted are also retrieved that might load a huge amount of entries to the memory all the
         * time. Yet those entries will be removed from the IDN_AUTH_USER_SESSION_MAPPING_TABLE table on the first
         * execution, and there after every time the loop will be executed and the table will be scanned for a non
         * existing entry.
         */
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries
                .SQL_SELECT_TERMINATED_SESSION_IDS)) {
            preparedStatement.setLong(1, FrameworkUtils.getCurrentStandardNano());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    terminatedSessionIds.add(resultSet.getString(1));
                }
            }
        }

        return terminatedSessionIds;
    }

    /**
     * This method is used to chunk-wise deletion of records of a given table.
     *
     * @param sessionsToRemove array of session ids which should be removed
     * @param connection       db connection
     * @param tableName        table name from which the records are removed
     * @param deleteQuery      delete query for the relevant table
     * @throws SQLException if the DB execution fails
     */
    private void deleteSessionDataFromTable(String[] sessionsToRemove, Connection connection, String tableName,
                                            String deleteQuery) throws SQLException {

        int chunkSize = getDeleteChunkSize();
        int totalSessionsToRemove = sessionsToRemove.length;
        int iterations = (totalSessionsToRemove / chunkSize) + 1;
        int startCount = 0;
        for (int i = 0; i < iterations; i++) {

            int endCount = (i + 1) * chunkSize;
            if (totalSessionsToRemove < endCount) {
                endCount = totalSessionsToRemove;
            }

            try (PreparedStatement preparedStatementForDelete = connection.prepareStatement(deleteQuery)) {

                for (int j = startCount; j < endCount; j++) {
                    preparedStatementForDelete.setString(1, sessionsToRemove[j]);
                    preparedStatementForDelete.addBatch();
                }
                preparedStatementForDelete.executeBatch();

                if (log.isDebugEnabled()) {
                    log.debug("Removed  " + (endCount - startCount) + " records from " + tableName + ".");
                }
            }
            startCount = endCount;
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed total " + totalSessionsToRemove + " records from " + tableName + ".");
        }
    }
}
