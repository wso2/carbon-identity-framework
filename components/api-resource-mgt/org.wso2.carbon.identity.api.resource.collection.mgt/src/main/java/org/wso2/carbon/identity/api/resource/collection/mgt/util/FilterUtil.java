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

package org.wso2.carbon.identity.api.resource.collection.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtClientException;
import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtServerException;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil.handleClientException;
import static org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil.handleServerException;

/**
 * Utility class for filter operations.
 */
public class FilterUtil {

    /**
     * Get the filter node as a list.
     *
     * @param filter Filter string.
     * @throws APIResourceCollectionMgtClientException Error when validate filters.
     */
    private static List<ExpressionNode> getExpressionNodes(String filter)
            throws APIResourceCollectionMgtClientException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        filter = StringUtils.isBlank(filter) ? StringUtils.EMPTY : filter;
        try {
            if (StringUtils.isNotBlank(filter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(filter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
            return expressionNodes;
        } catch (IOException | IdentityException e) {
            throw handleClientException(
                    APIResourceCollectionManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT);
        }
    }

    /**
     * Set the node values as a list of expression nodes.
     *
     * @param node       Filter node.
     * @param expression List of expression nodes.
     */
    private static void setExpressionNodeList(Node node, List<ExpressionNode> expression)
            throws APIResourceCollectionMgtClientException {

        if (node instanceof ExpressionNode) {
            if (StringUtils.isNotBlank(((ExpressionNode) node).getAttributeValue())) {
                expression.add((ExpressionNode) node);
            }
        } else if (node instanceof OperationNode) {
            OperationNode operationNode = (OperationNode) node;
            // Throw error if the operation is OR.
            if (APIResourceCollectionManagementConstants.OR.equalsIgnoreCase(operationNode.getOperation())) {
                throw handleClientException(
                        APIResourceCollectionManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_OPERATOR);
            }
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }

    /**
     * Filter API resource collections.
     *
     * @param apiResourceCollectionsMap API resource collection map.
     * @param filter                    Filter string.
     * @return Map of API resource collection.
     * @throws APIResourceCollectionMgtClientException Error when validate filters.
     */
    public static Map<String, APIResourceCollection> filterAPIResourceCollections(
            Map<String, APIResourceCollection> apiResourceCollectionsMap, String filter)
            throws APIResourceCollectionMgtClientException, APIResourceCollectionMgtServerException {

        try {
            List<ExpressionNode> expressionNodes = getExpressionNodes(filter);
            return apiResourceCollectionsMap.entrySet().stream()
                    .filter(entry -> {
                        try {
                            return matchesFilter(entry.getValue(), expressionNodes);
                        } catch (APIResourceCollectionMgtClientException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof APIResourceCollectionMgtClientException) {
                throw (APIResourceCollectionMgtClientException) e.getCause();
            } else {
                throw handleServerException(
                        APIResourceCollectionManagementConstants.ErrorMessages
                                .ERROR_CODE_WHILE_FILTERING_API_RESOURCE_COLLECTIONS, e);
            }
        }
    }

    /**
     * Match the filter with the API resource collection.
     *
     * @param apiResourceCollection API resource collection basic info object.
     * @param expressionNodes       List of expression nodes.
     * @return True if the filter matches with the API resource collection object.
     * @throws APIResourceCollectionMgtClientException Error when validate filters.
     */
    private static boolean matchesFilter(APIResourceCollection apiResourceCollection,
                                         List<ExpressionNode> expressionNodes)
            throws APIResourceCollectionMgtClientException {

        for (ExpressionNode node : expressionNodes) {
            String attribute = node.getAttributeValue();
            String value = node.getValue();
            String operation = node.getOperation();
            String apiResourceCollectionAttributeValue = getAttributeValue(apiResourceCollection, attribute);
            if (apiResourceCollectionAttributeValue == null) {
                return false;
            }
            switch (operation) {
                case APIResourceCollectionManagementConstants.EQ:
                    if (!apiResourceCollectionAttributeValue.equals(value)) {
                        return false;
                    }
                    break;
                case APIResourceCollectionManagementConstants.CO:
                    if (!apiResourceCollectionAttributeValue.contains(value)) {
                        return false;
                    }
                    break;
                case APIResourceCollectionManagementConstants.SW:
                    if (!apiResourceCollectionAttributeValue.startsWith(value)) {
                        return false;
                    }
                    break;
                case APIResourceCollectionManagementConstants.EW:
                    if (!apiResourceCollectionAttributeValue.endsWith(value)) {
                        return false;
                    }
                    break;
                default:
                    throw handleClientException(
                            APIResourceCollectionManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_OPERATION);
            }
        }
        return true;
    }

    /**
     * Get the attribute value of the API resource collection.
     *
     * @param apiResourceCollection API resource collection.
     * @param attributeName         Attribute name.
     * @return Attribute value.
     */
    private static String getAttributeValue(APIResourceCollection apiResourceCollection, String attributeName)
            throws APIResourceCollectionMgtClientException {

        switch (attributeName) {
            case APIResourceCollectionManagementConstants.NAME:
                return apiResourceCollection.getName();
            case APIResourceCollectionManagementConstants.DISPLAY_NAME:
                return apiResourceCollection.getDisplayName();
            case APIResourceCollectionManagementConstants.TYPE:
                return apiResourceCollection.getType();
            default:
                throw handleClientException(
                        APIResourceCollectionManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_ATTRIBUTE);
        }
    }
}
