/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility methods for session store related operations.
 */
public class SessionMgtUtils {

    public static final String SQL_QUERY_APPLICATIONS_SPLIT_CHARACTER = "|";
    public static final String SQL_QUERY_APPLICATION_DETAILS_SPLIT_CHARACTER = ":";

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
}
