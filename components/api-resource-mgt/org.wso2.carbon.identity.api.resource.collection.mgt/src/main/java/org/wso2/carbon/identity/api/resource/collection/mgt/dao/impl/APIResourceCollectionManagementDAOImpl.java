/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.collection.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.wso2.carbon.identity.api.resource.collection.mgt.dao.APIResourceCollectionManagementDAO;
import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtException;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionBasicInfo;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.FilterQueryBuilder;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.List;
import java.util.Map;

public class APIResourceCollectionManagementDAOImpl implements APIResourceCollectionManagementDAO {

    /**
     * Get API resource collections under given tenantId.
     *
     * @param expressionNode Expression nodes.
     * @return List of <code>APIResourceCollection</code>
     * @throws APIResourceCollectionMgtException If an error occurs while retrieving the API resource collections.
     */
    @Override
    public List<APIResourceCollectionBasicInfo> getAPIResourceCollections(List<ExpressionNode> expressionNode,
                                                                       Integer tenantId)
            throws APIResourceCollectionMgtException {

        return getAPIResourceCollectionsList(expressionNode);
    }

    /**
     * Get API resource collection by collectionId.
     *
     * @param collectionId ID of the API resource collection.
     * @return APIResourceCollection.
     * @throws APIResourceCollectionMgtException If an error occurs while retrieving the API resource collection.
     */
    @Override
    public APIResourceCollection getAPIResourceCollectionById(String collectionId, Integer tenantId)
            throws APIResourceCollectionMgtException {

        return null;
    }

    private List<APIResourceCollectionBasicInfo> getAPIResourceCollectionsList(List<ExpressionNode> expressionNodes)
            throws APIResourceCollectionMgtException {

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNodes, filterQueryBuilder);
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();

        return null;
    }

    /**
     * Append the filter query to the query builder.
     *
     * @param expressionNodes    List of expression nodes.
     * @param filterQueryBuilder Filter query builder.
     * @throws APIResourceCollectionMgtException If an error occurs while appending the filter query.
     */
    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder)
            throws APIResourceCollectionMgtException {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeValue = expressionNode.getAttributeValue();
                String attributeName = APIResourceCollectionManagementConstants.ATTRIBUTE_COLUMN_MAP.get(attributeValue);

                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    switch (operation) {
                        case APIResourceCollectionManagementConstants.EQ: {
                            equalFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.SW: {
                            startWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.EW: {
                            endWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.CO: {
                            containsFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.GE: {
                            greaterThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.LE: {
                            lessThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.GT: {
                            greaterThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceCollectionManagementConstants.LT: {
                            lessThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } else {
                    throw APIResourceCollectionManagementUtil.handleClientException(
                            APIResourceCollectionManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_VALUE);
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
            } else {
                filterQueryBuilder.setFilterQuery(filter.toString());
            }
        }
    }

    private void equalFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = " = ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private void startWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                        FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value + "%");
    }

    private void endWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                      FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, "%" + value);
    }

    private void containsFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, "%" + value + "%");
    }

    private void greaterThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder) {

        String filterString = " >= ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder) {

        String filterString = " <= ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder) {

        String filterString = " > ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " < ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }
}
