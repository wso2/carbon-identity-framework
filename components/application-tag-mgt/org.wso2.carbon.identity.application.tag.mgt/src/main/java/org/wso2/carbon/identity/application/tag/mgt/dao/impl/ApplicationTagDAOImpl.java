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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtException;
import org.wso2.carbon.identity.application.tag.mgt.constant.ApplicationTagManagementConstants;
import org.wso2.carbon.identity.application.tag.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.application.tag.mgt.dao.ApplicationTagDAO;
import org.wso2.carbon.identity.application.tag.mgt.util.ApplicationTagManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class implements the {@link ApplicationTagDAO} interface.
 */
public class ApplicationTagDAOImpl implements ApplicationTagDAO {

    private static final Log log = LogFactory.getLog(ApplicationTagDAOImpl.class);

    @Override
    public String createApplicationTag(ApplicationTag applicationTagDTO, Integer tenantID)
            throws ApplicationTagMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Creating Application Tag of " + applicationTagDTO.getName());
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

                String applicationTagId = null;
                if (results.next()) {
                    applicationTagId = results.getString(1);
                }
                // some JDBC Drivers returns this in the result, some don't
                if (applicationTagId == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("JDBC Driver did not return the tag id, executing Select operation");
                    }
                    applicationTagId = getApplicationTagByName(applicationTagDTO.getName(), tenantID, dbConnection);
                }

                IdentityDatabaseUtil.commitTransaction(dbConnection);
                return applicationTagId;
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                if (e.getMessage().toLowerCase().contains(SQLConstants.APP_TAG_UNIQUE_CONSTRAINT.toLowerCase())) {
                    throw ApplicationTagManagementUtil.handleClientException(
                            ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_APP_TAG_ALREADY_EXISTS,
                            String.valueOf(tenantID));
                }
                // Handle the DB2 database unique constraint violation error.
                if (dbConnection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    if (e.getMessage().contains(SQLConstants.DB2_SQL_ERROR_CODE_UNIQUE_CONSTRAINT)) {
                        throw ApplicationTagManagementUtil.handleClientException(
                                ApplicationTagManagementConstants.ErrorMessages.ERROR_CODE_APP_TAG_ALREADY_EXISTS,
                                String.valueOf(tenantID));
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
    public List<ApplicationTagsItem> getAllApplicationTags(Integer tenantID) throws ApplicationTagMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Reading all Application Tags of Tenant " + tenantID);
        }
        ResultSet applicationTagsResultSet = null;
        List<ApplicationTagsItem> applicationTagsList = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(SQLConstants.GET_ALL_APP_TAGS)) {
            preparedStatement.setInt(1, tenantID);

            applicationTagsResultSet = preparedStatement.executeQuery();

            while (applicationTagsResultSet.next()) {
                ApplicationTagsItem.ApplicationTagsListItemBuilder appTagBuilder =
                        new ApplicationTagsItem.ApplicationTagsListItemBuilder()
                                .id(applicationTagsResultSet.getString(SQLConstants.APP_TAG_ID_COLUMN_NAME))
                                .name(applicationTagsResultSet.getString(SQLConstants.APP_TAG_NAME_COLUMN_NAME))
                                .colour(applicationTagsResultSet.getString(SQLConstants.APP_TAG_COLOUR_COLUMN_NAME));
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

        if (log.isDebugEnabled()) {
            log.debug("Reading Application Tag " + applicationTagId);
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
                    ApplicationTagsItem.ApplicationTagsListItemBuilder appTagBuilder =
                            new ApplicationTagsItem.ApplicationTagsListItemBuilder()
                                    .id(resultSet.getString(SQLConstants.APP_TAG_ID_COLUMN_NAME))
                                    .name(resultSet.getString(SQLConstants.APP_TAG_NAME_COLUMN_NAME))
                                    .colour(resultSet.getString(SQLConstants.APP_TAG_COLOUR_COLUMN_NAME));
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

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application Tag " + applicationTagId);
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

        if (log.isDebugEnabled()) {
            log.debug("Updating Application Tag " + applicationTagId);
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

    /**
     * Returns the application tag Id for a given application tag name
     *
     * @param applicationTagName    Application Tag Name.
     * @param tenantId              Tenant ID.
     * @param connection            DB Connection.
     * @return Application Tag ID.
     * @throws SQLException If an error occurs while retrieving the Application tag by Name.
     */
    private String getApplicationTagByName(String applicationTagName, int tenantId, Connection connection)
            throws SQLException {

        String applicationTagId = null;
        ResultSet appTagIdResult = null;
        try (PreparedStatement getAppTagIDPrepStmt = connection.prepareStatement(
                SQLConstants.LOAD_APP_TAG_ID_BY_TAG_NAME)) {
            getAppTagIDPrepStmt.setString(1, applicationTagName);
            getAppTagIDPrepStmt.setInt(2, tenantId);
            appTagIdResult = getAppTagIDPrepStmt.executeQuery();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            if (appTagIdResult.next()) {
                applicationTagId = appTagIdResult.getString(1);
            }
        } finally {
            IdentityDatabaseUtil.closeResultSet(appTagIdResult);
        }
        return applicationTagId;
    }
}
