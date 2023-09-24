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

package org.wso2.carbon.identity.application.tag.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem.ApplicationTagsItemBuilder;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem.ApplicationTagsListItemBuilder;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtClientException;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtException;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtServerException;
import org.wso2.carbon.identity.application.tag.mgt.constant.ApplicationTagManagementConstants;
import org.wso2.carbon.identity.application.tag.mgt.constant.ApplicationTagManagementConstants.ErrorMessages;
import org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.application.tag.mgt.dao.ApplicationTagDAO;
import org.wso2.carbon.identity.application.tag.mgt.util.ApplicationTagManagementUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterData;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.identity.application.tag.mgt.constant.ApplicationTagManagementConstants.APPLICATION_TAG_PROPERTY_NAME;

/**
 * This class implements the {@link ApplicationTagDAO} interface.
 */
public class ApplicationTagDAOImpl implements ApplicationTagDAO {

    private static final String PERCENT_SIGN = "%";
    private static final String FILTER_STARTS_WITH = "sw";
    private static final String FILTER_ENDS_WITH = "ew";
    private static final String FILTER_EQUALS = "eq";
    private static final String FILTER_CONTAINS = "co";

    private static final Log LOG = LogFactory.getLog(ApplicationTagDAOImpl.class);
    private static final Map<String, String> SUPPORTED_SEARCH_ATTRIBUTE_MAP = new HashMap<>();

    static {
        SUPPORTED_SEARCH_ATTRIBUTE_MAP.put(APPLICATION_TAG_PROPERTY_NAME, "TAG.NAME");
    }

    @Override
    public ApplicationTagsItem createApplicationTag(ApplicationTag applicationTagDTO, Integer tenantID)
            throws ApplicationTagMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Application Tag of " + applicationTagDTO.getName());
        }
        ResultSet results = null;
        String generatedAppTagId = UUID.randomUUID().toString();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.ADD_APPLICATION_TAG);
                prepStmt.setString(1, generatedAppTagId);
                prepStmt.setString(2, applicationTagDTO.getName());
                prepStmt.setString(3, applicationTagDTO.getColour());
                prepStmt.setInt(4, tenantID);
                prepStmt.execute();

                results = prepStmt.getGeneratedKeys();

                ApplicationTagsItem applicationTag = null;
                if (results.next()) {
                    ApplicationTagsItemBuilder appTagBuilder =
                            new ApplicationTagsItemBuilder()
                                    .id(results.getString(SQLConstants.TAG_ID_COLUMN_NAME))
                                    .name(results.getString(SQLConstants.TAG_NAME_COLUMN_NAME))
                                    .colour(results.getString(SQLConstants.TAG_COLOUR_COLUMN_NAME));
                    applicationTag = appTagBuilder.build();
                }
                // some JDBC Drivers returns this in the result, some don't
                if (applicationTag == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("JDBC Driver did not return the tag id, executing Select operation");
                    }
                    applicationTag = getApplicationTagByName(applicationTagDTO.getName(), tenantID, dbConnection);
                }

                IdentityDatabaseUtil.commitTransaction(dbConnection);
                return applicationTag;
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                if (e.getMessage().toLowerCase().contains(SQLConstants.APP_TAG_UNIQUE_CONSTRAINT.toLowerCase())) {
                    throw ApplicationTagManagementUtil.handleClientException(
                            ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_APP_TAG_ALREADY_EXISTS,
                            IdentityTenantUtil.getTenantDomain(tenantID));
                }
                // Handle the DB2 database unique constraint violation error.
                if (dbConnection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    if (e.getMessage().contains(SQLConstants.DB2_SQL_ERROR_CODE_UNIQUE_CONSTRAINT)) {
                        throw ApplicationTagManagementUtil.handleClientException(
                                ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_APP_TAG_ALREADY_EXISTS,
                                IdentityTenantUtil.getTenantDomain(tenantID));
                    }
                }
                throw e;
            }
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_ADDING_APP_TAG, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(results);
        }
    }

    @Override
    public List<ApplicationTagsListItem> getAllApplicationTags(Integer tenantID, Integer offset, Integer limit,
                                                               String filter) throws ApplicationTagMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading all Application Tags of Tenant " + tenantID);
        }

        validateAttributesForPagination(offset, limit);

        if (StringUtils.isBlank(filter) || filter.equals(PERCENT_SIGN)) {
            return getAllApplicationTagsInfo(tenantID, offset, limit);
        }

        FilterData filterData = getFilterDataForDBQuery(filter);

        ResultSet applicationTagsResultSet = null;
        List<ApplicationTagsListItem> applicationTagsList = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(
                     String.format(SQLConstants.GET_ALL_APP_TAGS_WITH_FILTER, filterData.getFilterString()))) {
            
            preparedStatement.setInt(1, tenantID);

            int index = 2;
            for (int i = 0; i < filterData.getFilterValues().size(); i++) {
                preparedStatement.setString(index, filterData.getFilterValues().get(i));
                index++;
            }

            preparedStatement.setInt(index, offset);
            preparedStatement.setInt(index + 1, limit);

            applicationTagsResultSet = preparedStatement.executeQuery();

            while (applicationTagsResultSet.next()) {
                ApplicationTagsListItemBuilder appTagBuilder =
                        new ApplicationTagsListItemBuilder()
                                .id(applicationTagsResultSet.getString(SQLConstants.TAG_ID_COLUMN_NAME))
                                .name(applicationTagsResultSet.getString(SQLConstants.TAG_NAME_COLUMN_NAME))
                                .colour(applicationTagsResultSet.getString(SQLConstants.TAG_COLOUR_COLUMN_NAME))
                                .associatedAppsCount(applicationTagsResultSet.getInt(
                                        SQLConstants.TAG_APP_COUNT_COLUMN_NAME));
                applicationTagsList.add(appTagBuilder.build());
            }
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_APP_TAGS, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(applicationTagsResultSet);
        }
        return applicationTagsList;
    }

    @Override
    public ApplicationTagsItem getApplicationTagById(String applicationTagId, Integer tenantID)
            throws ApplicationTagMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading Application Tag " + applicationTagId);
        }
        ResultSet resultSet = null;
        ApplicationTagsItem applicationTag = null;
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_APP_TAG_BY_ID)) {
            preparedStatement.setString(1, applicationTagId);
            preparedStatement.setInt(2, tenantID);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                if (applicationTag == null) {
                    ApplicationTagsItemBuilder appTagBuilder =
                            new ApplicationTagsItemBuilder()
                                    .id(resultSet.getString(SQLConstants.TAG_ID_COLUMN_NAME))
                                    .name(resultSet.getString(SQLConstants.TAG_NAME_COLUMN_NAME))
                                    .colour(resultSet.getString(SQLConstants.TAG_COLOUR_COLUMN_NAME));
                    applicationTag = appTagBuilder.build();
                }
            }
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_APP_TAGS, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
        }
        return applicationTag;
    }

    @Override
    public void deleteApplicationTagById(String applicationTagId, Integer tenantID)
            throws ApplicationTagMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting Application Tag " + applicationTagId);
        }
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                PreparedStatement prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_APP_TAG);
                prepStmt.setString(1, applicationTagId);
                prepStmt.setInt(2, tenantID);
                prepStmt.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_APP_TAG, e);
        }
    }

    @Override
    public void updateApplicationTag(ApplicationTag applicationTagPatch, String applicationTagId,
                                     Integer tenantID) throws ApplicationTagMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating Application Tag " + applicationTagId);
        }
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.UPDATE_APP_TAG)) {
            try {
                preparedStatement.setString(1, applicationTagPatch.getName());
                preparedStatement.setString(2, applicationTagPatch.getColour());
                preparedStatement.setString(3, applicationTagId);
                preparedStatement.setInt(4, tenantID);
                preparedStatement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_APP_TAG, e);
        }
    }

    @Override
    public int getCountOfApplicationTags(String filter, Integer tenantID) throws ApplicationTagMgtException {

        int count;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting the count of all application Tags for the tenantID: " + tenantID);
        }

        FilterData filterData = getFilterDataForDBQuery(filter);
        ResultSet appTagNameResultSet = null;

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(String.format(
                     SQLConstants.LOAD_APP_TAG_COUNT_BY_TENANT_AND_FILTER, filterData.getFilterString()))) {
            preparedStatement.setInt(1, tenantID);

            for (int i = 0; i < filterData.getFilterValues().size(); i++) {
                preparedStatement.setString(2 + i, filterData.getFilterValues().get(i));
            }

            appTagNameResultSet = preparedStatement.executeQuery();
            appTagNameResultSet.next();
            count = Integer.parseInt(appTagNameResultSet.getString(1));
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(ApplicationTagManagementConstants
                            .ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_APP_TAGS_COUNT, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(appTagNameResultSet);
        }
        return count;
    }

    /**
     * Returns the application tag Id for a given application tag name
     *
     * @param applicationTagName    Application Tag Name.
     * @param tenantId              Tenant ID.
     * @param connection            DB Connection.
     * @return Application Tag.
     * @throws SQLException If an error occurs while retrieving the Application tag by Name.
     */
    private ApplicationTagsItem getApplicationTagByName(String applicationTagName, int tenantId, Connection connection)
            throws SQLException {

        ApplicationTagsItem applicationTag = null;
        ResultSet appTagResult = null;
        try (PreparedStatement getAppTagIDPrepStmt = connection.prepareStatement(
                SQLConstants.LOAD_APP_TAG_ID_BY_TAG_NAME)) {
            getAppTagIDPrepStmt.setString(1, applicationTagName);
            getAppTagIDPrepStmt.setInt(2, tenantId);
            appTagResult = getAppTagIDPrepStmt.executeQuery();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            if (appTagResult.next()) {
                ApplicationTagsItemBuilder appTagBuilder =
                        new ApplicationTagsItemBuilder()
                                .id(appTagResult.getString(SQLConstants.TAG_ID_COLUMN_NAME))
                                .name(appTagResult.getString(SQLConstants.TAG_NAME_COLUMN_NAME))
                                .colour(appTagResult.getString(SQLConstants.TAG_COLOUR_COLUMN_NAME));
                applicationTag = appTagBuilder.build();
            }
        } finally {
            IdentityDatabaseUtil.closeResultSet(appTagResult);
        }
        return applicationTag;
    }

    private List<ApplicationTagsListItem> getAllApplicationTagsInfo(Integer tenantID, Integer offset, Integer limit)
            throws ApplicationTagMgtServerException {

        ResultSet applicationTagsResultSet = null;
        List<ApplicationTagsListItem> applicationTagsList = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_ALL_APP_TAGS)) {
            preparedStatement.setInt(1, tenantID);
            preparedStatement.setInt(2, offset);
            preparedStatement.setInt(3, limit);

            applicationTagsResultSet = preparedStatement.executeQuery();

            while (applicationTagsResultSet.next()) {
                ApplicationTagsListItemBuilder appTagBuilder =
                        new ApplicationTagsListItemBuilder()
                                .id(applicationTagsResultSet.getString(SQLConstants.TAG_ID_COLUMN_NAME))
                                .name(applicationTagsResultSet.getString(SQLConstants.TAG_NAME_COLUMN_NAME))
                                .colour(applicationTagsResultSet.getString(SQLConstants.TAG_COLOUR_COLUMN_NAME))
                                .associatedAppsCount(applicationTagsResultSet.getInt(
                                        SQLConstants.TAG_APP_COUNT_COLUMN_NAME));
                applicationTagsList.add(appTagBuilder.build());
            }
        } catch (SQLException e) {
            throw ApplicationTagManagementUtil.handleServerException(
                    ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_APP_TAGS, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(applicationTagsResultSet);
        }
        return applicationTagsList;
    }

    /**
     * Validates the offset and limit values for pagination.
     *
     * @param offset Starting index.
     * @param limit  Count value.
     * @throws ApplicationTagMgtException If an error occurs while validating offset & limit for pagination.
     */
    private void validateAttributesForPagination(int offset, int limit) throws ApplicationTagMgtException {

        if (offset < 0) {
            throw ApplicationTagManagementUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_OFFSET_FOR_PAGINATION);
        }

        if (limit <= 0) {
            throw ApplicationTagManagementUtil.handleClientException(
                    ErrorMessages.ERROR_CODE_INVALID_LIMIT_FOR_PAGINATION);
        }
    }

    private FilterData getFilterDataForDBQuery(String filter) throws ApplicationTagMgtClientException {

        FilterData filterData = new FilterData();

        if (StringUtils.isBlank(filter) || filter.equals(PERCENT_SIGN)) {
            filterData.setFilterString("TAG.NAME LIKE ?");
            filterData.addFilterValue("%");
        } else if (!(SUPPORTED_SEARCH_ATTRIBUTE_MAP.containsKey(filter.trim().split(" ")[0]))) {
            // This formatting is to facilitate search without operators.
            if (filter.contains(PERCENT_SIGN)) {
                filterData.setFilterString("TAG.NAME LIKE ?");
            } else {
                filterData.setFilterString("TAG.NAME = ?");
            }
            filterData.addFilterValue(filter);
        } else {
            try {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(filter);
                Node rootNode = filterTreeBuilder.buildTree();
                filterData = getFilterDataFromFilterTree(rootNode);
            } catch (IOException | IdentityException e) {
                throw ApplicationTagManagementUtil.handleClientException(ErrorMessages.ERROR_CODE_INVALID_FILTER);
            }
        }
        return filterData;
    }

    private FilterData getFilterDataFromFilterTree(Node rootNode) throws ApplicationTagMgtClientException {

        FilterData filterData = new FilterData();

        if (rootNode instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) rootNode;
            if (!SUPPORTED_SEARCH_ATTRIBUTE_MAP.containsKey(expressionNode.getAttributeValue())) {
                throw ApplicationTagManagementUtil.handleClientException(
                        ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, expressionNode.getAttributeValue());
            }

            filterData.setFilterString(generateFilterString(expressionNode.getAttributeValue(),
                    expressionNode.getOperation()));
            filterData.addFilterValue(generateFilterValue(expressionNode.getOperation(), expressionNode.getValue()));
        } else if (rootNode instanceof OperationNode) {
            OperationNode operationNode = (OperationNode) rootNode;

            if (operationNode.getOperation().equals("not")) {
                throw ApplicationTagManagementUtil.handleClientException(
                        ErrorMessages.ERROR_CODE_INVALID_FILTER_OPERATION, operationNode.getOperation());
            }

            Node leftNode = rootNode.getLeftNode();
            Node rightNode = rootNode.getRightNode();

            FilterData leftNodeFilterData = getFilterDataFromFilterTree(leftNode);
            FilterData rightNodeFilterData = getFilterDataFromFilterTree(rightNode);

            filterData.setFilterString(leftNodeFilterData.getFilterString() + " " + operationNode.getOperation()
                    + " " + rightNodeFilterData.getFilterString());
            filterData.setFilterValues(
                    Stream.of(leftNodeFilterData.getFilterValues(), rightNodeFilterData.getFilterValues())
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
        } else {
            throw ApplicationTagManagementUtil.handleClientException(ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT);
        }
        return filterData;
    }

    private String generateFilterString(String searchField, String searchOperation) {

        // Format the filter attribute and condition to fit in a SQL where clause.
        String formattedFilterString;
        String realSearchField = SUPPORTED_SEARCH_ATTRIBUTE_MAP.get(searchField);
        if (searchOperation.equals(FILTER_EQUALS)) {
            formattedFilterString = realSearchField + " = ?";
        } else {
            formattedFilterString = realSearchField + " LIKE ?";
        }

        return formattedFilterString;
    }

    private String generateFilterValue(String searchOperation, String searchValue) {

        // Format the filter value to fit in a SQL where clause.
        String formattedFilterValue;
        switch (searchOperation) {
            case FILTER_STARTS_WITH:
                formattedFilterValue = searchValue + PERCENT_SIGN;
                break;
            case FILTER_ENDS_WITH:
                formattedFilterValue = PERCENT_SIGN + searchValue;
                break;
            case FILTER_CONTAINS:
                formattedFilterValue = PERCENT_SIGN + searchValue + PERCENT_SIGN;
                break;
            default:
                formattedFilterValue = searchValue;
        }

        return formattedFilterValue;
    }
}
