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
package org.wso2.carbon.identity.entitlement.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.DISPLAY_NAME;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.DISPLAY_ORDER;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.ENTITLEMENT_MODULE_NAME;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.IS_REQUIRED;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.IS_SECRET;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.MODULE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.PROPERTY_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.PROPERTY_VALUE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.SUBSCRIBER_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.TENANT_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.CREATE_SUBSCRIBER_PROPERTIES_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.CREATE_SUBSCRIBER_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_SUBSCRIBER_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_SUBSCRIBER_EXISTENCE_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_SUBSCRIBER_IDS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_SUBSCRIBER_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.UPDATE_SUBSCRIBER_MODULE_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.UPDATE_SUBSCRIBER_PROPERTIES_SQL;

public class JDBCSubscriberDAOImpl implements SubscriberDAO {

    private static final Log LOG = LogFactory.getLog(JDBCSubscriberDAOImpl.class);
    private static final String ERROR_SUBSCRIBER_ID_NULL = "Subscriber Id can not be null";

    /**
     * Gets the requested subscriber.
     *
     * @param subscriberId  subscriber ID.
     * @param returnSecrets whether the subscriber should get returned with secret(decrypted) values or not.
     * @return publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, boolean returnSecrets) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement preparedStmt = new NamedPreparedStatement(connection, GET_SUBSCRIBER_SQL)) {
                preparedStmt.setString(SUBSCRIBER_ID, subscriberId);
                preparedStmt.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = preparedStmt.executeQuery()) {
                    if (resultSet.next()) {
                        return getPublisherDataHolder(resultSet, returnSecrets);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException(String.format("Error while retrieving subscriber details of id : %s",
                    subscriberId), e);
        }
    }

    /**
     * Gets all subscriber IDs.
     *
     * @param filter search string.
     * @return list of subscriber IDs.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<String> listSubscriberIds(String filter) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<String> subscriberIdList = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement preparedStmt = new NamedPreparedStatement(connection, GET_SUBSCRIBER_IDS_SQL)) {
                preparedStmt.setInt(TENANT_ID, tenantId);

                try (ResultSet subscriberIds = preparedStmt.executeQuery()) {
                    while (subscriberIds.next()) {
                        subscriberIdList.add(subscriberIds.getString(SUBSCRIBER_ID));
                    }
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while retrieving subscriber ids", e);
        }

        return EntitlementUtil.filterSubscribers(subscriberIdList, filter);
    }

    /**
     * Adds a subscriber.
     *
     * @param holder publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addSubscriber(PublisherDataHolder holder) throws EntitlementException {

        String subscriberId = EntitlementUtil.resolveSubscriberId(holder);
        if (subscriberId == null) {
            throw new EntitlementException(ERROR_SUBSCRIBER_ID_NULL);
        }

        if (isSubscriberExists(subscriberId)) {
            throw new EntitlementException("Subscriber ID already exists");
        }

        PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();
        insertSubscriber(subscriberId, holder, propertyDTOs);
    }

    /**
     * Updates a subscriber.
     *
     * @param holder publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void updateSubscriber(PublisherDataHolder holder) throws EntitlementException {

        String subscriberId = EntitlementUtil.resolveSubscriberId(holder);
        if (subscriberId == null) {
            throw new EntitlementException(ERROR_SUBSCRIBER_ID_NULL);
        }

        PublisherDataHolder oldHolder;
        if (isSubscriberExists(subscriberId)) {
            // TODO: revisit in hybrid impl
            oldHolder = getSubscriber(subscriberId, false);
            populateProperties(holder, oldHolder);
        } else {
            throw new EntitlementException("Subscriber ID does not exist; update cannot be done");
        }

        PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();
        updateSubscriber(subscriberId, holder, propertyDTOs, oldHolder);
    }

    /**
     * Removes the subscriber of the given subscriber ID.
     *
     * @param subscriberId subscriber ID.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void removeSubscriber(String subscriberId) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (StringUtils.isBlank(subscriberId)) {
            throw new EntitlementException(ERROR_SUBSCRIBER_ID_NULL);
        }

        if (EntitlementConstants.PDP_SUBSCRIBER_ID.equals(subscriberId.trim())) {
            throw new EntitlementException("Cannot delete PDP publisher");
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement preparedStmt = new NamedPreparedStatement(connection, DELETE_SUBSCRIBER_SQL)) {
                preparedStmt.setString(SUBSCRIBER_ID, subscriberId);
                preparedStmt.setInt(TENANT_ID, tenantId);
                preparedStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while deleting subscriber details", e);
        }
    }

    /**
     * Checks whether a subscriber exists.
     *
     * @param subscriberId subscriber ID.
     * @return whether the subscriber exists or not.
     * @throws EntitlementException If an error occurs.
     */
    public boolean isSubscriberExists(String subscriberId) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement findSubscriberExistencePrepStmt = new NamedPreparedStatement(connection,
                    GET_SUBSCRIBER_EXISTENCE_SQL)) {
                findSubscriberExistencePrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                findSubscriberExistencePrepStmt.setInt(TENANT_ID, tenantId);

                try (ResultSet rs1 = findSubscriberExistencePrepStmt.executeQuery()) {
                    return rs1.next();
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while checking subscriber existence", e);
        }
    }

    private void insertSubscriber(String subscriberId, PublisherDataHolder holder, PublisherPropertyDTO[] propertyDTOs)
            throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement createSubscriberPrepStmt = new NamedPreparedStatement(connection,
                CREATE_SUBSCRIBER_SQL);
             NamedPreparedStatement createSubscriberPropertiesPrepStmt = new NamedPreparedStatement(connection,
                     CREATE_SUBSCRIBER_PROPERTIES_SQL)) {
            createSubscriberPrepStmt.setString(ENTITLEMENT_MODULE_NAME, holder.getModuleName());
            createSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
            createSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
            createSubscriberPrepStmt.executeUpdate();

            for (PublisherPropertyDTO dto : propertyDTOs) {
                if (dto.getId() != null && StringUtils.isNotBlank(dto.getValue())) {

                    createSubscriberPropertiesPrepStmt.setString(PROPERTY_ID, dto.getId());
                    createSubscriberPropertiesPrepStmt.setString(DISPLAY_NAME, dto.getDisplayName());
                    createSubscriberPropertiesPrepStmt.setString(PROPERTY_VALUE, dto.getValue());
                    createSubscriberPropertiesPrepStmt.setBoolean(IS_REQUIRED, dto.isRequired());
                    createSubscriberPropertiesPrepStmt.setInt(DISPLAY_ORDER, dto.getDisplayOrder());
                    createSubscriberPropertiesPrepStmt.setBoolean(IS_SECRET, dto.isSecret());
                    createSubscriberPropertiesPrepStmt.setString(MODULE, dto.getModule());
                    createSubscriberPropertiesPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                    createSubscriberPropertiesPrepStmt.setInt(TENANT_ID, tenantId);

                    createSubscriberPropertiesPrepStmt.addBatch();
                }
            }
            createSubscriberPropertiesPrepStmt.executeBatch();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while inserting subscriber details", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private void updateSubscriber(String subscriberId, PublisherDataHolder holder, PublisherPropertyDTO[] propertyDTOs,
                                  PublisherDataHolder oldHolder) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement updateSubscriberPrepStmt = new NamedPreparedStatement(connection,
                UPDATE_SUBSCRIBER_MODULE_SQL);
             NamedPreparedStatement updateSubscriberPropertiesPrepStmt = new NamedPreparedStatement(connection,
                     UPDATE_SUBSCRIBER_PROPERTIES_SQL)) {

            if (!oldHolder.getModuleName().equalsIgnoreCase(holder.getModuleName())) {
                updateSubscriberPrepStmt.setString(ENTITLEMENT_MODULE_NAME, holder.getModuleName());
                updateSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                updateSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
                updateSubscriberPrepStmt.executeUpdate();
            }

            // Update the property values of an existing subscriber
            for (PublisherPropertyDTO dto : propertyDTOs) {

                if (StringUtils.isNotBlank(dto.getId()) && StringUtils.isNotBlank(dto.getValue())) {
                    PublisherPropertyDTO propertyDTO;
                    propertyDTO = oldHolder.getPropertyDTO(dto.getId());
                    if (propertyDTO != null && !propertyDTO.getValue().equalsIgnoreCase(dto.getValue())) {
                        updateSubscriberPropertiesPrepStmt.setString(PROPERTY_VALUE, dto.getValue());
                        updateSubscriberPropertiesPrepStmt.setString(PROPERTY_ID, dto.getId());
                        updateSubscriberPropertiesPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                        updateSubscriberPropertiesPrepStmt.setInt(TENANT_ID, tenantId);
                        updateSubscriberPropertiesPrepStmt.addBatch();
                    }
                }
            }
            updateSubscriberPropertiesPrepStmt.executeBatch();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while updating subscriber details", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Sets the base64 encoded secret value of the secret subscriber properties, if it has been updated.
     *
     * @param holder    publisher data holder
     * @param oldHolder existing publisher data holder
     */
    private void populateProperties(PublisherDataHolder holder, PublisherDataHolder oldHolder) {

        PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();

        for (PublisherPropertyDTO dto : propertyDTOs) {
            if (StringUtils.isNotBlank(dto.getId()) && StringUtils.isNotBlank(dto.getValue()) && dto.isSecret()) {
                PublisherPropertyDTO propertyDTO = null;
                if (oldHolder != null) {
                    propertyDTO = oldHolder.getPropertyDTO(dto.getId());
                }
                if (propertyDTO == null || !propertyDTO.getValue().equalsIgnoreCase(dto.getValue())) {
                    try {
                        String encryptedValue =
                                CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(dto.getValue().getBytes());
                        dto.setValue(encryptedValue);
                    } catch (CryptoException e) {
                        LOG.error("Error while encrypting secret value of subscriber. Secret would not be persisted.",
                                e);
                    }
                }
            }
        }
    }

    private PublisherDataHolder getPublisherDataHolder(ResultSet resultSet, boolean returnSecrets) throws SQLException {

        List<PublisherPropertyDTO> propertyDTOList = new ArrayList<>();
        String moduleName;
        do {
            PublisherPropertyDTO dto = new PublisherPropertyDTO();

            dto.setId(resultSet.getString(PROPERTY_ID));
            dto.setValue(resultSet.getString(PROPERTY_VALUE));
            dto.setDisplayName(resultSet.getString(DISPLAY_NAME));
            dto.setDisplayOrder(resultSet.getInt(DISPLAY_ORDER));
            dto.setRequired(resultSet.getBoolean(IS_REQUIRED));
            dto.setSecret(resultSet.getBoolean(IS_SECRET));
            dto.setModule(resultSet.getString(MODULE));

            if (dto.isSecret() && returnSecrets) {
                String password = dto.getValue();
                try {
                    password = new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(dto.getValue()));
                } catch (CryptoException e) {
                    LOG.error("Error while decrypting secret value of subscriber.", e);
                }
                dto.setValue(password);
            }

            moduleName = resultSet.getString(ENTITLEMENT_MODULE_NAME);
            propertyDTOList.add(dto);
        } while (resultSet.next());
        return new PublisherDataHolder(propertyDTOList, moduleName);
    }
}
