/*
 * Copyright (c) 2022-2026, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.application.authentication.framework.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.AuthUserDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility methods for session store related operations.
 */
public class SessionMgtUtils {

    private static final String APP_FILTER_TENANT_CONDITION = " AND (TENANT_ID = ? OR IS_SAAS_APP = 1)";

    public static final String SQL_QUERY_APPLICATIONS_SPLIT_CHARACTER = "|";
    public static final String SQL_QUERY_APPLICATION_DETAILS_SPLIT_CHARACTER = ":";

    public static final String DEFAULT_SESSION_STORE_NAME = "jdbc";

    private static final String SESSION_STORE_IMPL_TYPE_PROPERTY = "SessionStoreImplType";

    /**
     * Returns the name of the configured session store, normalized to lower case.
     *
     * @return the configured store name, or the default store name if none is configured.
     */
    public static String getConfiguredSessionStoreName() {

        String storeName = IdentityUtil.getProperty(SESSION_STORE_IMPL_TYPE_PROPERTY);
        return StringUtils.isBlank(storeName) ? DEFAULT_SESSION_STORE_NAME
                : storeName.trim().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Transform a list of filter expressions into SQL query strings.
     *
     * @param expressionNodes list of filter expressions.
     * @return SQL query strings map.
     * @throws UserSessionException if an error occurs while parsing the filter criteria.
     */
    public static Map<SessionMgtConstants.FilterType, String> getSQLFiltersFromExpressionNodes(
            List<ExpressionNode> expressionNodes) throws UserSessionException {

        Map<SessionMgtConstants.FilterType, String> filterMap = new HashMap<>();
        String appFilter = "";
        StringJoiner sessionJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());
        StringJoiner appJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());
        StringJoiner userJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());
        StringJoiner mainJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());

        for (ExpressionNode expressionNode : expressionNodes) {
            String operation = expressionNode.getOperation();
            String value = expressionNode.getValue();
            String attribute = expressionNode.getAttributeValue();
            SessionMgtConstants.FilterType filterType = SessionMgtConstants.FilterType.DEFAULT;

            StringBuilder filter = new StringBuilder();
            boolean isString = true;

            switch (attribute.toLowerCase()) {
                case SessionMgtConstants.FLD_SESSION_ID_LOWERCASE:
                    attribute = SessionMgtConstants.COL_SESSION_ID;
                    filterType = SessionMgtConstants.FilterType.SESSION;
                    break;
                case SessionMgtConstants.FLD_APPLICATION_LOWERCASE:
                    attribute = SessionMgtConstants.COL_APPLICATION;
                    value = value.toLowerCase();
                    filterType = SessionMgtConstants.FilterType.APPLICATION;
                    break;
                case SessionMgtConstants.FLD_LOGIN_ID_LOWERCASE:
                    attribute = SessionMgtConstants.COL_LOGIN_ID;
                    value = value.toLowerCase();
                    filterType = SessionMgtConstants.FilterType.USER;
                    break;
                case SessionMgtConstants.FLD_IP_ADDRESS_LOWERCASE:
                    attribute = SessionMgtConstants.COL_IP_ADDRESS;
                    break;
                case SessionMgtConstants.FLD_USER_AGENT_LOWERCASE:
                    attribute = SessionMgtConstants.COL_USER_AGENT;
                    value = value.toLowerCase();
                    break;
                case SessionMgtConstants.FLD_LOGIN_TIME_LOWERCASE:
                    attribute = SessionMgtConstants.COL_LOGIN_TIME;
                    break;
                case SessionMgtConstants.FLD_LAST_ACCESS_TIME_LOWERCASE:
                    attribute = SessionMgtConstants.COL_LAST_ACCESS_TIME;
                    break;
                case SessionMgtConstants.FLD_TIME_CREATED_SINCE:
                case SessionMgtConstants.FLD_TIME_CREATED_UNTIL:
                    attribute = SessionMgtConstants.COL_TIME_CREATED;
                    isString = false;
                    break;
                default:
                    throw new UserSessionException("Invalid filter attribute: " + attribute);
            }

            switch (operation.toLowerCase()) {
                case SessionMgtConstants.EQ:
                    filter.append(attribute).append(" = ").append(isString ? "'" : "").append(value)
                            .append(isString ? "'" : "");
                    break;
                case SessionMgtConstants.SW:
                    value = value.replace("_", "\\_").replace("%", "\\%");
                    filter.append(attribute).append(" LIKE '").append(value).append("%' ESCAPE '\\'");
                    break;
                case SessionMgtConstants.EW:
                    value = value.replace("_", "\\_").replace("%", "\\%");
                    filter.append(attribute).append(" LIKE '%").append(value).append("' ESCAPE '\\'");
                    break;
                case SessionMgtConstants.CO:
                    value = value.replace("_", "\\_").replace("%", "\\%");
                    filter.append(attribute).append(" LIKE '%").append(value).append("%' ESCAPE '\\'");
                    break;
                case SessionMgtConstants.LE:
                    filter.append(attribute).append(" <= ").append(isString ? "'" : "").append(value)
                            .append(isString ? "'" : "");
                    break;
                case SessionMgtConstants.LT:
                    filter.append(attribute).append(" < ").append(isString ? "'" : "").append(value)
                            .append(isString ? "'" : "");
                    break;
                case SessionMgtConstants.GE:
                    filter.append(attribute).append(" >= ").append(isString ? "'" : "").append(value)
                            .append(isString ? "'" : "");
                    break;
                case SessionMgtConstants.GT:
                    filter.append(attribute).append(" > ").append(isString ? "'" : "").append(value)
                            .append(isString ? "'" : "");
                    break;
                default:
                    throw new UserSessionException("Invalid filter operation: " + operation);
            }

            switch (filterType) {
                case SESSION:
                    sessionJoiner.add(filter.toString());
                    break;
                case APPLICATION:
                    appJoiner.add(filter.toString());
                    break;
                case USER:
                    userJoiner.add(filter.toString());
                    break;
                default:
                    mainJoiner.add(filter.toString());
            }
        }

        if (sessionJoiner.length() > 0) {
            filterMap.put(SessionMgtConstants.FilterType.SESSION,
                    SessionMgtConstants.QueryOperations.AND.getQueryString() + sessionJoiner);
        } else {
            filterMap.put(SessionMgtConstants.FilterType.SESSION, "");
        }
        if (appJoiner.length() > 0) {
            appFilter = MessageFormat.format(SessionMgtConstants.QueryOperations.WHERE.getQueryString(),
                    appJoiner.toString());
            filterMap.put(SessionMgtConstants.FilterType.APPLICATION, MessageFormat.format(
                    SessionMgtConstants.QueryOperations.WHERE.getQueryString(), appJoiner.toString()));
        } else {
            filterMap.put(SessionMgtConstants.FilterType.APPLICATION, "");
        }
        if (userJoiner.length() > 0) {
            if (StringUtils.isEmpty(appFilter)) {
                filterMap.put(SessionMgtConstants.FilterType.USER, MessageFormat.format(
                        SessionMgtConstants.QueryOperations.WHERE.getQueryString(), userJoiner.toString()));
            } else {
                filterMap.put(SessionMgtConstants.FilterType.USER,
                        SessionMgtConstants.QueryOperations.AND.getQueryString() + userJoiner);
            }
        } else {
            filterMap.put(SessionMgtConstants.FilterType.USER, "");
        }
        if (mainJoiner.length() > 0) {
            filterMap.put(SessionMgtConstants.FilterType.MAIN,
                    SessionMgtConstants.QueryOperations.AND.getQueryString() + mainJoiner);
        } else {
            filterMap.put(SessionMgtConstants.FilterType.MAIN, "");
        }

        return filterMap;
    }

    /**
     * Transform a list of filter expressions into parameterized SQL fragments with bound values.
     * Unlike {@link #getSQLFiltersFromExpressionNodes(List)}, each SQL fragment uses {@code ?}
     * placeholders and values are stored separately in the returned {@link SessionFilterQueryBuilder},
     * preventing SQL injection through filter parameters.
     *
     * @param expressionNodes list of filter expressions.
     * @return {@link SessionFilterQueryBuilder} containing SQL fragments and ordered parameter values.
     * @throws UserSessionException if an error occurs while parsing the filter criteria.
     */
    public static SessionFilterQueryBuilder getSQLFilterQueryBuilder(
            List<ExpressionNode> expressionNodes) throws UserSessionException {

        SessionFilterQueryBuilder builder = new SessionFilterQueryBuilder();
        StringJoiner sessionJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());
        StringJoiner appJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());
        StringJoiner userJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());
        StringJoiner mainJoiner = new StringJoiner(SessionMgtConstants.QueryOperations.AND.getQueryString());

        if (expressionNodes != null) {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attribute = expressionNode.getAttributeValue();

                if (StringUtils.isBlank(attribute) || StringUtils.isBlank(operation) || value == null) {
                    throw new UserSessionException(
                            "Invalid filter node: attribute, operation, and value must not be null or blank.");
                }

                SessionMgtConstants.FilterType filterType = SessionMgtConstants.FilterType.DEFAULT;

                StringBuilder filterSQL = new StringBuilder();
                boolean isString = true;

                switch (attribute.toLowerCase()) {
                    case SessionMgtConstants.FLD_SESSION_ID_LOWERCASE:
                        attribute = SessionMgtConstants.COL_SESSION_ID;
                        filterType = SessionMgtConstants.FilterType.SESSION;
                        break;
                    case SessionMgtConstants.FLD_APPLICATION_LOWERCASE:
                        attribute = SessionMgtConstants.COL_APPLICATION;
                        value = value.toLowerCase();
                        filterType = SessionMgtConstants.FilterType.APPLICATION;
                        break;
                    case SessionMgtConstants.FLD_LOGIN_ID_LOWERCASE:
                        attribute = SessionMgtConstants.COL_LOGIN_ID;
                        value = value.toLowerCase();
                        filterType = SessionMgtConstants.FilterType.USER;
                        break;
                    case SessionMgtConstants.FLD_IP_ADDRESS_LOWERCASE:
                        attribute = SessionMgtConstants.COL_IP_ADDRESS;
                        break;
                    case SessionMgtConstants.FLD_USER_AGENT_LOWERCASE:
                        attribute = SessionMgtConstants.COL_USER_AGENT;
                        value = value.toLowerCase();
                        break;
                    case SessionMgtConstants.FLD_LOGIN_TIME_LOWERCASE:
                        attribute = SessionMgtConstants.COL_LOGIN_TIME;
                        break;
                    case SessionMgtConstants.FLD_LAST_ACCESS_TIME_LOWERCASE:
                        attribute = SessionMgtConstants.COL_LAST_ACCESS_TIME;
                        break;
                    case SessionMgtConstants.FLD_TIME_CREATED_SINCE:
                    case SessionMgtConstants.FLD_TIME_CREATED_UNTIL:
                        attribute = SessionMgtConstants.COL_TIME_CREATED;
                        isString = false;
                        break;
                    default:
                        throw new UserSessionException("Invalid filter attribute: " + attribute);
                }

                Object paramValue;
                switch (operation.toLowerCase()) {
                    case SessionMgtConstants.EQ:
                        filterSQL.append(attribute).append(" = ?");
                        paramValue = isString ? value : parseNumericFilterValue(attribute, value);
                        break;
                    case SessionMgtConstants.SW:
                        filterSQL.append(attribute).append(" LIKE ? ESCAPE '\\'");
                        paramValue = escapeLikeChars(value) + "%";
                        break;
                    case SessionMgtConstants.EW:
                        filterSQL.append(attribute).append(" LIKE ? ESCAPE '\\'");
                        paramValue = "%" + escapeLikeChars(value);
                        break;
                    case SessionMgtConstants.CO:
                        filterSQL.append(attribute).append(" LIKE ? ESCAPE '\\'");
                        paramValue = "%" + escapeLikeChars(value) + "%";
                        break;
                    case SessionMgtConstants.LE:
                        filterSQL.append(attribute).append(" <= ?");
                        paramValue = isString ? value : parseNumericFilterValue(attribute, value);
                        break;
                    case SessionMgtConstants.LT:
                        filterSQL.append(attribute).append(" < ?");
                        paramValue = isString ? value : parseNumericFilterValue(attribute, value);
                        break;
                    case SessionMgtConstants.GE:
                        filterSQL.append(attribute).append(" >= ?");
                        paramValue = isString ? value : parseNumericFilterValue(attribute, value);
                        break;
                    case SessionMgtConstants.GT:
                        filterSQL.append(attribute).append(" > ?");
                        paramValue = isString ? value : parseNumericFilterValue(attribute, value);
                        break;
                    default:
                        throw new UserSessionException("Invalid filter operation: " + operation);
                }

                // Route the SQL fragment and its bound value to the correct per-type joiner/builder.
                // DEFAULT maps to the MAIN filter (outer WHERE clause predicates).
                switch (filterType) {
                    case SESSION:
                        sessionJoiner.add(filterSQL.toString());
                        builder.addFilterParam(SessionMgtConstants.FilterType.SESSION, paramValue);
                        break;
                    case APPLICATION:
                        appJoiner.add(filterSQL.toString());
                        builder.addFilterParam(SessionMgtConstants.FilterType.APPLICATION, paramValue);
                        break;
                    case USER:
                        userJoiner.add(filterSQL.toString());
                        builder.addFilterParam(SessionMgtConstants.FilterType.USER, paramValue);
                        break;
                    default:
                        mainJoiner.add(filterSQL.toString());
                        builder.addFilterParam(SessionMgtConstants.FilterType.MAIN, paramValue);
                }
            }
        }

        if (sessionJoiner.length() > 0) {
            builder.setFilterQuery(SessionMgtConstants.FilterType.SESSION,
                    SessionMgtConstants.QueryOperations.AND.getQueryString() + sessionJoiner);
        } else {
            builder.setFilterQuery(SessionMgtConstants.FilterType.SESSION, "");
        }

        boolean hasAppFilter = appJoiner.length() > 0;
        if (hasAppFilter) {
            builder.setFilterQuery(SessionMgtConstants.FilterType.APPLICATION,
                    MessageFormat.format(SessionMgtConstants.QueryOperations.WHERE.getQueryString(), appJoiner));
        } else {
            builder.setFilterQuery(SessionMgtConstants.FilterType.APPLICATION, "");
        }

        if (userJoiner.length() > 0) {
            if (!hasAppFilter) {
                builder.setFilterQuery(SessionMgtConstants.FilterType.USER, MessageFormat.format(
                        SessionMgtConstants.QueryOperations.WHERE.getQueryString(), userJoiner));
            } else {
                builder.setFilterQuery(SessionMgtConstants.FilterType.USER,
                        SessionMgtConstants.QueryOperations.AND.getQueryString() + userJoiner);
            }
        } else {
            builder.setFilterQuery(SessionMgtConstants.FilterType.USER, "");
        }

        if (mainJoiner.length() > 0) {
            builder.setFilterQuery(SessionMgtConstants.FilterType.MAIN,
                    SessionMgtConstants.QueryOperations.AND.getQueryString() + mainJoiner);
        } else {
            builder.setFilterQuery(SessionMgtConstants.FilterType.MAIN, "");
        }

        return builder;
    }

    /**
     * Transform a result set record into a search result object.
     *
     * @param record result set object.
     * @return a SessionSearchResult object.
     * @throws SQLException if an error occurs while parsing the result set.
     */
    public static UserSession parseSessionSearchResult(ResultSet record, Map<String, Application> applications)
            throws SQLException {

        UserSession result = new UserSession();
        List<Application> apps = Arrays.stream(record.getString(8)
                .split(Pattern.quote(SQL_QUERY_APPLICATIONS_SPLIT_CHARACTER)))
                .map(appInfo -> SessionMgtUtils.parseApplication(appInfo, applications))
                .collect(Collectors.toList());

        result.setSessionId(record.getString(1));
        result.setCreationTime(record.getLong(2));
        result.setUserId(record.getString(3));
        result.setIp(record.getString(4));
        result.setLoginTime(record.getString(5));
        result.setLastAccessTime(record.getString(6));
        result.setUserAgent(record.getString(7));
        result.setApplications(apps);

        return result;
    }

    /**
     * Parses application data into an application object.
     *
     * @param appInfo application data string.
     * @return an Application object.
     */
    private static Application parseApplication(String appInfo, Map<String, Application> applications) {

        String[] data = appInfo.split(SQL_QUERY_APPLICATION_DETAILS_SPLIT_CHARACTER);
        Application application = applications.get(data[0]);
        if (application == null) {
            return new Application(data[1], null, data[0]);
        }
        return new Application(data[1], application.getAppName(), application.getAppId(), application.getResourceId());
    }

    private static Long parseNumericFilterValue(String attribute, String value) throws UserSessionException {

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new UserSessionException(
                    "Invalid numeric filter value for attribute '" + attribute + "': " + value);
        }
    }

    private static String escapeLikeChars(String value) {

        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
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
        int[] parsedIds = new int[appIds.size()];
        int index = 0;
        try {
            for (String appId : appIds) {
                parsedIds[index++] = Integer.parseInt(appId);
            }
        } catch (NumberFormatException e) {
            throw new DataAccessException("Invalid application ID found in session data: " + appIds, e);
        }
        try {
            List<ApplicationBasicInfo> infos = FrameworkServiceDataHolder.getInstance()
                    .getApplicationManagementService()
                    .getApplicationBasicInfosByIds(parsedIds);
            for (ApplicationBasicInfo info : infos) {
                String appId = String.valueOf(info.getApplicationId());
                applications.put(appId,
                        new Application(null, info.getApplicationName(), appId, info.getApplicationResourceId()));
            }
        } catch (IdentityApplicationManagementException e) {
            throw new DataAccessException("Error while retrieving application information by IDs.", e);
        }
        return applications;
    }

    /**
     * Sets the name and the resource identifier of the given applications from their application records, and
     * removes the ones that have no record.
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
     * @return the matching applications by their string encoded numeric identifier.
     * @throws DataAccessException if the applications could not be retrieved.
     */
    public static Map<String, Application> getApplicationsByFilter(SessionFilterQueryBuilder filterBuilder,
                                                                   int tenantId) throws DataAccessException {

        String filterClause = filterBuilder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION)
                + APP_FILTER_TENANT_CONDITION;
        List<Object> filterParams = new ArrayList<>(
                filterBuilder.getFilterParams(SessionMgtConstants.FilterType.APPLICATION));
        filterParams.add(tenantId);
        Map<String, Application> applications = new HashMap<>();
        try {
            for (ApplicationBasicInfo applicationInfo : FrameworkServiceDataHolder.getInstance()
                    .getApplicationManagementService().getApplicationBasicInfos(filterClause, filterParams)) {
                String appId = String.valueOf(applicationInfo.getApplicationId());
                applications.put(appId, new Application(null, applicationInfo.getApplicationName(), appId,
                        applicationInfo.getApplicationResourceId()));
            }
        } catch (IdentityApplicationManagementException e) {
            throw new DataAccessException("Error while retrieving applications by the application filter.", e);
        }
        return applications;
    }

    /**
     * Retrieves the identity provider of each of the given users.
     *
     * @param userIds User identifiers.
     * @return the identity provider identifier by user identifier, without the users that have no record.
     * @throws DataAccessException if the identity providers could not be retrieved.
     */
    public static Map<String, String> getIdpIdsByUserIds(Set<String> userIds) throws DataAccessException {

        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return AuthUserDAO.getInstance().getIdpIdsByUserIds(new ArrayList<>(userIds));
        } catch (UserSessionException e) {
            throw new DataAccessException("Error while retrieving IDP IDs for user IDs.", e);
        }
    }
}
