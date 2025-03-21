/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.models.FilterQueryBuilder;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.ATTRIBURE_COLUMN_MAP;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.EQ;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.FILTER_PLACEHOLDER_PREFIX;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.GE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.GT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.LE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.LT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.SW;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CREATED_AT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtDbUtil.isMSSqlDB;

/**
 * Filter query builder util class.
 */
public class FilterQueryBuilderUtil {

    public static FilterQueryBuilder buildFilterQuery(List<ExpressionNode> expressionNodes)
            throws AsyncOperationStatusMgtServerException {

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNodes, filterQueryBuilder);
        return filterQueryBuilder;
    }

    private static void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeValue = expressionNode.getAttributeValue();
                String attributeName = ATTRIBURE_COLUMN_MAP.get(attributeValue);

                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    if (CREATED_AT.equals(attributeName)) {
                        filterQueryBuilder.addTimestampFilterAttributes(FILTER_PLACEHOLDER_PREFIX);
                    }
                    switch (operation) {
                        case EQ: {
                            equalFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case SW: {
                            startWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case GE: {
                            greaterThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case LE: {
                            lessThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case GT: {
                            greaterThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case LT: {
                            lessThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
            } else {
                if (filter.toString().endsWith("AND ")) {
                    String filterString = filter.toString();
                    filterQueryBuilder.setFilterQuery(filterString.substring(0, filterString.length() - 4));
                } else {
                    filterQueryBuilder.setFilterQuery(filter.toString());
                }
            }
        }
    }

    private static void equalFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" = :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private static void startWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value + "%");
    }

    private static void greaterThanOrEqualFilterBuilder(int count, String value, String attributeName,
                                                       StringBuilder filter,
                                                       FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " >= CAST(:%s%s; AS DATETIME) AND "
                : " >= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private static void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                    FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " <= CAST(:%s%s; AS DATETIME) AND "
                : " <= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private static void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " > CAST(:%s%s; AS DATETIME) AND "
                : " > :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private static void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                             FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " < CAST(:%s%s; AS DATETIME) AND "
                : " < :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private static boolean isDateTimeAndMSSql(String attributeName) throws AsyncOperationStatusMgtServerException {

        return (CREATED_AT.equals(attributeName)) && isMSSqlDB();
    }
}
