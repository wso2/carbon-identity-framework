package org.wso2.carbon.identity.api.resource.mgt.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtClientException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.model.FilterQueryBuilder;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class FilterQueriesUtil {

    public static FilterQueryBuilder getScopeFilterQueryBuilder(List<ExpressionNode> expressionNodes)
            throws APIResourceMgtClientException {

        return getFilterQueryBuilder(expressionNodes, APIResourceManagementConstants.SCOPE_ATTRIBUTE_COLUMN_MAP);
    }

    public static FilterQueryBuilder getApiResourceFilterQueryBuilder(List<ExpressionNode> expressionNodes)
            throws APIResourceMgtClientException {

        return getFilterQueryBuilder(expressionNodes, APIResourceManagementConstants.ATTRIBUTE_COLUMN_MAP);
    }

    public static FilterQueryBuilder getAuthorizationDetailsTypesFilterQueryBuilder(
            List<ExpressionNode> expressionNodes) throws APIResourceMgtClientException {

        return getFilterQueryBuilder(expressionNodes,
                APIResourceManagementConstants.AUTHORIZATION_DETAILS_TYPES_ATTRIBUTE_COLUMN_MAP);
    }

    /**
     * Append the filter query to the query builder.
     *
     * @param expressionNodes    List of expression nodes.
     * @param supportedAttributeColumns supported attribute columns map.
     * @throws APIResourceMgtClientException If an error occurs while appending the filter query.
     */
    public static FilterQueryBuilder getFilterQueryBuilder(List<ExpressionNode> expressionNodes,
                                                           Map<String, String> supportedAttributeColumns)
            throws APIResourceMgtClientException {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        final FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeValue = expressionNode.getAttributeValue();
                String attributeName = supportedAttributeColumns.get(attributeValue);

                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    switch (operation) {
                        case APIResourceManagementConstants.EQ: {
                            equalFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.NE: {
                            notEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.SW: {
                            startWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.EW: {
                            endWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.CO: {
                            containsFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.GE: {
                            greaterThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.LE: {
                            lessThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.GT: {
                            greaterThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        case APIResourceManagementConstants.LT: {
                            lessThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            ++count;
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } else {
                    throw APIResourceManagementUtil.handleClientException(
                            APIResourceManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_VALUE);
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
            } else {
                filterQueryBuilder.setFilterQuery(filter.toString());
            }
        }
        return filterQueryBuilder;
    }


    private static void equalFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = " = ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private static void notEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " <> ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private static void startWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                        FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value + "%");
    }

    private static void endWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                      FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, "%" + value);
    }

    private static void containsFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, "%" + value + "%");
    }

    private static void greaterThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder) {

        String filterString = " >= ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private static void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder) {

        String filterString = " <= ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private static void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder) {

        String filterString = " > ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    private static void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " < ? AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(count, value);
    }

    /**
     * Get the filter node as a list.
     *
     * @param filter Filter string.
     * @param after  After cursor.
     * @param before Before cursor.
     * @throws APIResourceMgtClientException Error when validate filters.
     */
    public static List<ExpressionNode> getExpressionNodes(String filter, String after, String before)
            throws APIResourceMgtClientException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        filter = StringUtils.isBlank(filter) ? StringUtils.EMPTY : filter;
        String paginatedFilter = getPaginatedFilter(filter, after, before);
        try {
            if (StringUtils.isNotBlank(paginatedFilter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(paginatedFilter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
            return expressionNodes;
        } catch (IOException | IdentityException e) {
            throw APIResourceManagementUtil.handleClientException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT);
        }
    }

    /**
     * Get pagination filter.
     *
     * @param paginatedFilter Filter string.
     * @param after           After cursor.
     * @param before          Before cursor.
     * @return Filter string.
     * @throws APIResourceMgtClientException Error when validate filters.
     */
    private static String getPaginatedFilter(String paginatedFilter, String after, String before) throws
            APIResourceMgtClientException {

        try {
            if (StringUtils.isNotBlank(before)) {
                String decodedString = new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and " +
                        APIResourceManagementConstants.BEFORE_GT + decodedString :
                        APIResourceManagementConstants.BEFORE_GT + decodedString;
            } else if (StringUtils.isNotBlank(after)) {
                String decodedString = new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and " +
                        APIResourceManagementConstants.AFTER_LT + decodedString :
                        APIResourceManagementConstants.AFTER_LT + decodedString;
            }
        } catch (IllegalArgumentException e) {
            throw APIResourceManagementUtil.handleClientException(
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION);
        }
        return paginatedFilter;
    }

    /**
     * Set the node values as list of expression.
     *
     * @param node       filter node.
     * @param expression list of expression.
     */
    private static void setExpressionNodeList(Node node, List<ExpressionNode> expression) {

        if (node instanceof ExpressionNode) {
            if (StringUtils.isNotBlank(((ExpressionNode) node).getAttributeValue())) {
                expression.add((ExpressionNode) node);
            }
        } else if (node instanceof OperationNode) {
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }
}
