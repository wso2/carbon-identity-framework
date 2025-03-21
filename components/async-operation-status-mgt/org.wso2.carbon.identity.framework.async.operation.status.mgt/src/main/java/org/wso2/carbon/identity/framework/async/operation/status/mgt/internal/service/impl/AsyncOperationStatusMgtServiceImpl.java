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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtClientException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.service.AsyncOperationStatusMgtService;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.component.AsyncOperationStatusMgtDataHolder;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.AsyncOperationStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.impl.AsyncOperationOperationStatusMgtDAOImpl;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.models.dos.UnitOperationDO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.queue.AsyncOperationDataBuffer;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.FilterTreeBuilder;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_CODE_INVALID_LIMIT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_CODE_INVALID_REQUEST_BODY;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_RESOLVING_ORG_ID_FROM_TENANT_DOMAIN;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_RETRIEVING_BASIC_ORG_DETAILS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_RETRIEVING_ORG_NAME_FROM_ORG_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.AND;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.AsyncOperationStatusMgtConstants.DESC_SORT_ORDER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtExceptionHandler.handleClientException;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtExceptionHandler.handleServerException;

/**
 * Implementation of the {@link AsyncOperationStatusMgtService} interface that manages asynchronous operation statuses.
 * This service interacts with the {@link AsyncOperationStatusMgtDAO} to perform persistence operations and
 * uses an in-memory buffer to temporarily store unit operation records before batch processing.
 */
public class AsyncOperationStatusMgtServiceImpl implements AsyncOperationStatusMgtService {

    private static final Log LOG = LogFactory.getLog(AsyncOperationStatusMgtServiceImpl.class);
    private static volatile AsyncOperationStatusMgtServiceImpl instance;
    private static final AsyncOperationStatusMgtDAO
            ASYNC_OPERATION_STATUS_MGT_DAO = new AsyncOperationOperationStatusMgtDAOImpl();
    private static final AsyncOperationDataBuffer operationDataBuffer =
            new AsyncOperationDataBuffer(ASYNC_OPERATION_STATUS_MGT_DAO, 100, 3);

    public static AsyncOperationStatusMgtServiceImpl getInstance() {

        if (instance == null) {
            synchronized (AsyncOperationStatusMgtServiceImpl.class) {
                if (instance == null) {
                    instance = new AsyncOperationStatusMgtServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public String registerOperationStatus(OperationInitDTO record, boolean updateIfExists)
            throws AsyncOperationStatusMgtException {

        if (updateIfExists) {
            return ASYNC_OPERATION_STATUS_MGT_DAO.registerAsyncStatusWithUpdate(record);
        }
        return ASYNC_OPERATION_STATUS_MGT_DAO.registerAsyncStatusWithoutUpdate(record);
    }

    @Override
    public void updateOperationStatus(String operationId, OperationStatus status)
            throws AsyncOperationStatusMgtException {

        ASYNC_OPERATION_STATUS_MGT_DAO.updateAsyncStatus(operationId, status);
    }

    @Override
    public void registerUnitOperationStatus(UnitOperationInitDTO unitOperationInitDTO) throws
            AsyncOperationStatusMgtException {

        operationDataBuffer.add(unitOperationInitDTO);
    }

    @Override
    public List<OperationResponseDTO> getOperations(String tenantDomain, String after, String before, Integer limit,
                                                    String filter) throws AsyncOperationStatusMgtException {

        limit = validateLimit(limit);
        String requestInitiatedOrgId = getOrganizationId(tenantDomain);
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, DESC_SORT_ORDER);
        return ASYNC_OPERATION_STATUS_MGT_DAO.getOperations(requestInitiatedOrgId, limit,
                expressionNodes);
    }

    @Override
    public OperationResponseDTO getOperation(String operationId, String tenantDomain) throws
            AsyncOperationStatusMgtException {

        String requestInitiatedOrgId = getOrganizationId(tenantDomain);
        return ASYNC_OPERATION_STATUS_MGT_DAO.getOperation(operationId, requestInitiatedOrgId);
    }

    @Override
    public UnitOperationResponseDTO getUnitOperation(String unitOperationId, String tenantDomain)
            throws AsyncOperationStatusMgtException {

        String requestInitiatedOrgId = getOrganizationId(tenantDomain);
        UnitOperationDO unitOperationDO = ASYNC_OPERATION_STATUS_MGT_DAO.getUnitOperation(unitOperationId,
                requestInitiatedOrgId);
        if (unitOperationDO == null) {
            return null;
        }
        String targetOrgName = getOrganizationName(unitOperationDO.getTargetOrgId());

        return new UnitOperationResponseDTO.Builder()
                .unitOperationId(unitOperationDO.getUnitOperationId())
                .operationId(unitOperationDO.getOperationId())
                .operationInitiatedResourceId(unitOperationDO.getOperationInitiatedResourceId())
                .targetOrgId(unitOperationDO.getTargetOrgId())
                .targetOrgName(targetOrgName)
                .unitOperationStatus(unitOperationDO.getUnitOperationStatus())
                .statusMessage(unitOperationDO.getStatusMessage())
                .createdTime(unitOperationDO.getCreatedTime())
                .build();
    }

    @Override
    public List<UnitOperationResponseDTO> getUnitOperationStatusRecords(String operationId, String tenantDomain,
                                                                        String after, String before, Integer limit,
                                                                        String filter)
            throws AsyncOperationStatusMgtException {

        limit = validateLimit(limit);
        String requestInitiatedOrgId = getOrganizationId(tenantDomain);

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, DESC_SORT_ORDER);
        List<UnitOperationDO> unitOperations = ASYNC_OPERATION_STATUS_MGT_DAO.getUnitOperations(operationId,
                requestInitiatedOrgId, limit, expressionNodes);
        List<String> targetOrgIds = new ArrayList<>();

        for (UnitOperationDO unitOperationDO : unitOperations) {
            targetOrgIds.add(unitOperationDO.getTargetOrgId());
        }
        Map<String, BasicOrganization> orgDetails = getBasicOrganizationDetails(targetOrgIds);

        List<UnitOperationResponseDTO> unitOperationResponseDTOList = new ArrayList<>();
        if (!orgDetails.isEmpty()) {
            for (UnitOperationDO unitOperationDO : unitOperations) {
                if (StringUtils.isBlank(orgDetails.get(unitOperationDO.getTargetOrgId()).getName())
                        && LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Error while retrieving org name from org id: %s.",
                            unitOperationDO.getTargetOrgId()));
                } else {
                    UnitOperationResponseDTO dto = new UnitOperationResponseDTO.Builder()
                            .unitOperationId(unitOperationDO.getUnitOperationId())
                            .operationId(unitOperationDO.getOperationId())
                            .operationInitiatedResourceId(unitOperationDO.getOperationInitiatedResourceId())
                            .targetOrgId(unitOperationDO.getTargetOrgId())
                            .targetOrgName(orgDetails.get(unitOperationDO.getTargetOrgId()).getName())
                            .unitOperationStatus(unitOperationDO.getUnitOperationStatus())
                            .statusMessage(unitOperationDO.getStatusMessage())
                            .createdTime(unitOperationDO.getCreatedTime())
                            .build();
                    unitOperationResponseDTOList.add(dto);
                }
            }
        }
        return unitOperationResponseDTOList;
    }

    private String getOrganizationId(String tenantDomain) throws AsyncOperationStatusMgtServerException {

        try {
            return getOrganizationManager().resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            throw handleServerException(ERROR_WHILE_RESOLVING_ORG_ID_FROM_TENANT_DOMAIN, e);
        }
    }

    private Map<String, BasicOrganization> getBasicOrganizationDetails(List<String> orgIds) throws
            AsyncOperationStatusMgtServerException {

        try {
            return getOrganizationManager().getBasicOrganizationDetailsByOrgIDs(orgIds);
        } catch (OrganizationManagementException e) {
            throw handleServerException(ERROR_WHILE_RETRIEVING_BASIC_ORG_DETAILS, e);
        }
    }

    private String getOrganizationName(String orgId) throws AsyncOperationStatusMgtServerException {

        try {
            return getOrganizationManager().getOrganizationNameById(orgId);
        } catch (OrganizationManagementException e) {
            throw handleServerException(ERROR_WHILE_RETRIEVING_ORG_NAME_FROM_ORG_ID, e);
        }
    }

    // Helper methods for curser-based pagination and filtering.
    private List<ExpressionNode> getExpressionNodes(String filter, String after, String before,
                                                    String paginationSortOrder)
            throws AsyncOperationStatusMgtException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        if (StringUtils.isBlank(filter)) {
            filter = StringUtils.EMPTY;
        }
        String paginatedFilter = getPaginatedFilterForDescendingOrder(filter, after, before);
        try {
            if (StringUtils.isNotBlank(paginatedFilter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(paginatedFilter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
        } catch (IOException e) {
            throw handleServerException(ERROR_CODE_INVALID_REQUEST_BODY, e);
        }
        return expressionNodes;
    }

    private String getPaginatedFilterForDescendingOrder(String paginatedFilter, String after, String before)
            throws AsyncOperationStatusMgtClientException {

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
            throws AsyncOperationStatusMgtClientException {

        if (node instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) node;
            String attributeValue = expressionNode.getAttributeValue();
            if (StringUtils.isNotBlank(attributeValue)) {
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

    /**
     * Validate limit.
     *
     * @param limit given limit value.
     * @return validated limit value.
     * @throws AsyncOperationStatusMgtClientException AsyncStatusMgtClientException.
     */
    private int validateLimit(Integer limit) throws AsyncOperationStatusMgtClientException {

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit == null) {
            limit = IdentityUtil.getDefaultItemsPerPage();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Given limit is null. Therefore assigning the default limit: %s.", limit));
            }
        } else if (limit < 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Invalid limit. Limit value should be greater than or equal to zero." +
                        " limit: %s.", limit));
            }
            throw handleClientException(ERROR_CODE_INVALID_LIMIT);
        } else if (limit > maximumItemsPerPage) {
            limit = maximumItemsPerPage;
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Given limit exceed the maximum limit. Therefore assigning the maximum " +
                        "limit: %s.", maximumItemsPerPage));
            }
        }
        return limit;
    }

    private OrganizationManager getOrganizationManager() {

        return AsyncOperationStatusMgtDataHolder.getInstance().getOrganizationManager();
    }
}
