/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.exception.AsyncStatusMgtClientException;
import org.wso2.carbon.identity.framework.async.status.mgt.exception.AsyncStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.status.mgt.filter.FilterTreeBuilder;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.queue.AsyncOperationDataBuffer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.AND;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.ASC_SORT_ORDER;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.DESC_SORT_ORDER;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.ErrorMessages
        .ERROR_CODE_INVALID_REQUEST_BODY;
import static org.wso2.carbon.identity.framework.async.status.mgt.util.Utils.handleClientException;

/**
 * Implementation of the {@link AsyncStatusMgtService} interface that manages asynchronous operation statuses.
 * This service interacts with the {@link AsyncStatusMgtDAO} to perform persistence operations and
 * uses an in-memory buffer to temporarily store unit operation records before batch processing.
 */
@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {

    private static final  AsyncStatusMgtServiceImpl INSTANCE = new AsyncStatusMgtServiceImpl();
    private static final AsyncStatusMgtDAO asyncStatusMgtDAO = new AsyncStatusMgtDAOImpl();
    private static final AsyncOperationDataBuffer operationDataBuffer =
            new AsyncOperationDataBuffer(asyncStatusMgtDAO, 100, 5);

    public static AsyncStatusMgtServiceImpl getInstance() {

        return INSTANCE;
    }

    @Override
    public String registerOperationStatus(OperationRecord record, boolean updateIfExists) {

        if (updateIfExists) {
            return asyncStatusMgtDAO.registerAsyncOperationWithUpdate(record);
        }
        return asyncStatusMgtDAO.registerAsyncOperationWithoutUpdate(record);
    }

    @Override
    public void updateOperationStatus(String operationId, String status) {

        asyncStatusMgtDAO.updateAsyncOperationStatus(operationId, status);
    }

    @Override
    public void registerUnitOperationStatus(UnitOperationRecord unitOperationRecord) {

        operationDataBuffer.add(unitOperationRecord);
    }

    @Override
    public List<ResponseOperationRecord> getOperationStatusRecords(String operationType, String operationSubjectId) {

        return asyncStatusMgtDAO.getOperationStatusByOperationTypeAndOperationSubjectId(operationType,
                operationSubjectId);
    }

    @Override
    public List<ResponseOperationRecord> getOperationStatusRecords(String operationSubjectType,
                                                                   String operationSubjectId,
                                                                   String operationType, String after, String before,
                                                                   Integer limit, String filter)
            throws AsyncStatusMgtClientException, AsyncStatusMgtServerException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, DESC_SORT_ORDER);
        return asyncStatusMgtDAO.getOperationRecords(operationSubjectType, operationSubjectId, operationType,
                limit, expressionNodes);
    }

    @Override
    public List<ResponseUnitOperationRecord> getUnitOperationStatusRecords(String operationId, String after,
                                                                           String before, Integer limit, String filter)
            throws AsyncStatusMgtClientException, AsyncStatusMgtServerException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, DESC_SORT_ORDER);
        return asyncStatusMgtDAO.getUnitOperationRecordsForOperationId(operationId, limit, expressionNodes);
    }

    // Helper methods for curser-based pagination and filtering.
    private List<ExpressionNode> getExpressionNodes(String filter, String after, String before,
                                                    String paginationSortOrder)
            throws AsyncStatusMgtClientException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        if (StringUtils.isBlank(filter)) {
            filter = StringUtils.EMPTY;
        }
        String paginatedFilter = paginationSortOrder.equals(ASC_SORT_ORDER) ?
                getPaginatedFilterForAscendingOrder(filter, after, before) :
                getPaginatedFilterForDescendingOrder(filter, after, before);
        try {
            if (StringUtils.isNotBlank(paginatedFilter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(paginatedFilter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
        } catch (IOException e) {
            throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY);
        }
        return expressionNodes;
    }

    private String getPaginatedFilterForAscendingOrder(String paginatedFilter, String after, String before)
            throws AsyncStatusMgtClientException {

        try {
            if (StringUtils.isNotBlank(before)) {
                String decodedString = new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and before lt "
                        + decodedString : "before lt " + decodedString;
            } else if (StringUtils.isNotBlank(after)) {
                String decodedString = new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and after gt "
                        + decodedString : "after gt " + decodedString;
            }
        } catch (IllegalArgumentException e) {
            throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY);
        }
        return paginatedFilter;
    }

    private String getPaginatedFilterForDescendingOrder(String paginatedFilter, String after, String before)
            throws AsyncStatusMgtClientException {

        try {
            if (StringUtils.isNotBlank(before)) {
                String decodedString = new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8);
                Timestamp.valueOf(decodedString);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and before gt " + decodedString :
                        "before gt " + decodedString;
            } else if (StringUtils.isNotBlank(after)) {
                String decodedString = new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8);
                Timestamp.valueOf(decodedString);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and after lt " + decodedString :
                        "after lt " + decodedString;
            }
        } catch (IllegalArgumentException e) {
            throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY);
        }
        return paginatedFilter;
    }

    /**
     * Sets the expression nodes required for the retrieval of organizations from the database.
     *
     * @param node       The node.
     * @param expression The list of expression nodes.
     */
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression)
            throws AsyncStatusMgtClientException {

        if (node instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) node;
            String attributeValue = expressionNode.getAttributeValue();
            if (StringUtils.isNotBlank(attributeValue)) {
                if (attributeValue.startsWith("attributes.")) {
                    attributeValue = "attributes";
                }
//                if (isFilteringAttributeNotSupported(attributeValue)) {
//                    throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY, attributeValue);
//                }
                expression.add(expressionNode);
            }
        } else if (node instanceof OperationNode) {
            String operation = ((OperationNode) node).getOperation();
            if (!StringUtils.equalsIgnoreCase(AND, operation)) {
                throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY);
            }
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }

}
