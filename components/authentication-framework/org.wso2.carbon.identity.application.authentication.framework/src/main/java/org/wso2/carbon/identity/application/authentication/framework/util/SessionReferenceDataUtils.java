/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads the records a session refers to but does not own: the applications of {@code SP_APP} and the identity
 * providers of {@code IDN_AUTH_USER}.
 * <p>
 * These records stay in the relational store regardless of the configured session store, so every
 * {@link org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO} implementation reads
 * them the same way. Session data itself is not read here; that belongs to the DAO of the configured store.
 * <p>
 * Every method reports failures as a {@link DataAccessException}, which the caller translates into the exception
 * of the operation it is serving.
 */
public class SessionReferenceDataUtils {

    /** Placeholder the shared queries use for a generated list of parameters. */
    private static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";
    private static final String APP_FILTER_TENANT_CONDITION = " AND (TENANT_ID = ? OR IS_SAAS_APP = '1')";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_APP_NAME = "APP_NAME";
    private static final String COLUMN_UUID = "UUID";
    private static final String COLUMN_USER_ID = "USER_ID";
    private static final String COLUMN_IDP_ID = "IDP_ID";

    private SessionReferenceDataUtils() {

    }

    /**
     * Retrieves the applications of the given identifiers.
     *
     * @param appIds Application identifiers.
     * @return the applications by identifier, without the identifiers that have no application record.
     * @throws DataAccessException if the applications could not be retrieved.
     */
    public static Map<String, Application> getApplicationsByIds(Set<String> appIds) throws DataAccessException {

        Map<String, Application> applications = new HashMap<>();
        if (appIds == null || appIds.isEmpty()) {
            return applications;
        }
        int[] parsedAppIds = new int[appIds.size()];
        int index = 0;
        try {
            for (String appId : appIds) {
                parsedAppIds[index++] = Integer.parseInt(appId);
            }
        } catch (NumberFormatException e) {
            throw new DataAccessException("Invalid application ID found in session data: " + appIds, e);
        }
        String placeholder = String.join(", ", Collections.nCopies(parsedAppIds.length, "?"));
        // TODO:: Get applications using application-mgt services and remove component unrelated queries.
        String query = SQLQueries.SQL_GET_APPLICATION.replace(SCOPE_LIST_PLACEHOLDER, placeholder);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        List<Application> applicationList = jdbcTemplate.executeQuery(query,
                (resultSet, rowNumber) -> new Application(null, resultSet.getString(COLUMN_APP_NAME),
                        resultSet.getString(COLUMN_ID), resultSet.getString(COLUMN_UUID)),
                preparedStatement -> {
                    for (int position = 0; position < parsedAppIds.length; position++) {
                        preparedStatement.setInt(position + 1, parsedAppIds[position]);
                    }
                });
        for (Application application : applicationList) {
            applications.put(application.getAppId(), application);
        }
        return applications;
    }

    /**
     * Sets the name and the resource identifier of the given applications from their application records, and
     * removes the ones that have no record. An application that has a session but no application record is not
     * reported as part of that session.
     *
     * @param applications Applications to complete, modified in place.
     * @throws DataAccessException if the application records could not be retrieved.
     */
    public static void setApplicationDetails(List<Application> applications) throws DataAccessException {

        if (applications == null || applications.isEmpty()) {
            return;
        }
        Set<String> appIds = new HashSet<>();
        for (Application application : applications) {
            appIds.add(application.getAppId());
        }
        Map<String, Application> records = getApplicationsByIds(appIds);
        for (Application application : applications) {
            Application record = records.get(application.getAppId());
            if (record != null) {
                application.setAppName(record.getAppName());
                application.setResourceId(record.getResourceId());
            }
        }
        applications.removeIf(application -> application.getAppName() == null);
    }

    /**
     * Retrieves the applications matching the application filter of a session search. The applications of the
     * given tenant and the SaaS applications are matched, as a session of either can be searched.
     *
     * @param filterBuilder Filter query builder of the search, holding the application filter and its values.
     * @param tenantId      Tenant identifier.
     * @return the matching applications by identifier.
     * @throws DataAccessException if the applications could not be retrieved.
     */
    public static Map<String, Application> getApplicationsByFilter(SessionFilterQueryBuilder filterBuilder,
                                                                   int tenantId) throws DataAccessException {

        List<Object> params = new ArrayList<>(
                filterBuilder.getFilterParams(SessionMgtConstants.FilterType.APPLICATION));
        params.add(tenantId);
        // TODO:: Get applications using application-mgt services and remove component unrelated queries.
        String query = MessageFormat.format(SQLQueries.SQL_GET_APPLICATIONS_BY_FILTER_AND_TENANT,
                filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION)
                        + APP_FILTER_TENANT_CONDITION);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        List<Application> applicationList = jdbcTemplate.executeQuery(query,
                (resultSet, rowNumber) -> new Application(null, resultSet.getString(COLUMN_APP_NAME),
                        resultSet.getString(COLUMN_ID), resultSet.getString(COLUMN_UUID)),
                preparedStatement -> bindFilterParams(preparedStatement, params));
        return applicationList.stream().collect(Collectors.toMap(Application::getAppId, application -> application));
    }

    /**
     * Retrieves the identity provider of each of the given users.
     *
     * @param userIds User identifiers.
     * @return the identity provider identifier by user identifier, without the users that have no record.
     * @throws DataAccessException if the identity providers could not be retrieved.
     */
    public static Map<String, String> getIdpIdsByUserIds(Set<String> userIds) throws DataAccessException {

        Map<String, String> identityProviders = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return identityProviders;
        }
        List<String> userIdList = new ArrayList<>(userIds);
        String placeholder = String.join(", ", Collections.nCopies(userIdList.size(), "?"));
        String query = SQLQueries.SQL_GET_IDP_IDS_BY_USER_ID_LIST.replace(SCOPE_LIST_PLACEHOLDER, placeholder);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        jdbcTemplate.executeQuery(query,
                (resultSet, rowNumber) -> identityProviders.put(resultSet.getString(COLUMN_USER_ID),
                        Integer.toString(resultSet.getInt(COLUMN_IDP_ID))),
                preparedStatement -> {
                    for (int position = 0; position < userIdList.size(); position++) {
                        preparedStatement.setString(position + 1, userIdList.get(position));
                    }
                });
        return identityProviders;
    }

    /**
     * Binds the application filter values to the given statement, in the order they are held.
     *
     * @param preparedStatement statement to bind the values to.
     * @param params            filter parameter values.
     * @throws SQLException if a value could not be bound.
     */
    private static void bindFilterParams(PreparedStatement preparedStatement, List<Object> params)
            throws SQLException {

        int index = 1;
        for (Object param : params) {
            if (param instanceof Long) {
                preparedStatement.setLong(index++, (Long) param);
            } else if (param instanceof Integer) {
                preparedStatement.setInt(index++, (Integer) param);
            } else {
                preparedStatement.setString(index++, (String) param);
            }
        }
    }
}
