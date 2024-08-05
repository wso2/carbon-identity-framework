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

import org.apache.commons.lang.ArrayUtils;
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

/**
 * This class handles the JDBC operations of the subscribers in the data store.
 */
public class JDBCSubscriberDAOImpl implements SubscriberDAO {

    private static final Log LOG = LogFactory.getLog(JDBCSubscriberDAOImpl.class);
    private static final String ERROR_SUBSCRIBER_ID_NULL = "Subscriber Id can not be null";

    /**
     * Gets the requested subscriber.
     *
     * @param subscriberId         subscriber ID.
     * @param shouldDecryptSecrets whether the subscriber should get returned with secret(decrypted) values or not.
     * @return publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, boolean shouldDecryptSecrets)
            throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PublisherDataHolder publisherDataHolder = getSubscriber(subscriberId, tenantId);
        if (publisherDataHolder == null) {
            return null;
        }
        if (shouldDecryptSecrets) {
            decryptSecretProperties(publisherDataHolder.getPropertyDTOs());
        }
        return publisherDataHolder;
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
        List<String> subscriberIdList = getSubscriberIds(tenantId);
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
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        insertSubscriber(subscriberId, holder, tenantId);
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

        if (isSubscriberExists(subscriberId)) {

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            PublisherDataHolder oldHolder = getSubscriber(subscriberId, false);
            String updatedModuleName = getUpdatedModuleName(holder, oldHolder);
            PublisherPropertyDTO[] updatedPropertyDTOs = getUpdatedPropertyDTOs(holder, oldHolder);
            updatedPropertyDTOs = encryptUpdatedSecretProperties(updatedPropertyDTOs);
            updateSubscriber(subscriberId, updatedModuleName, updatedPropertyDTOs, tenantId);
        } else {
            throw new EntitlementException("Subscriber ID does not exist; update cannot be done");
        }
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

        deleteSubscriber(subscriberId, tenantId);
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
        return isSubscriberExists(subscriberId, tenantId);
    }

    /**
     * DAO method to get the requested subscriber.
     *
     * @param subscriberId subscriber ID.
     * @param tenantId     tenant ID.
     * @return publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    private PublisherDataHolder getSubscriber(String subscriberId, int tenantId)
            throws EntitlementException {

        List<PublisherPropertyDTO> propertyDTOList = new ArrayList<>();
        String moduleName = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement preparedStmt = new NamedPreparedStatement(connection, GET_SUBSCRIBER_SQL)) {

            preparedStmt.setString(SUBSCRIBER_ID, subscriberId);
            preparedStmt.setInt(TENANT_ID, tenantId);

            try (ResultSet resultSet = preparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    do {
                        PublisherPropertyDTO dto = new PublisherPropertyDTO();

                        dto.setId(resultSet.getString(PROPERTY_ID));
                        dto.setValue(resultSet.getString(PROPERTY_VALUE));
                        dto.setDisplayName(resultSet.getString(DISPLAY_NAME));
                        dto.setDisplayOrder(resultSet.getInt(DISPLAY_ORDER));
                        dto.setRequired(resultSet.getBoolean(IS_REQUIRED));
                        dto.setSecret(resultSet.getBoolean(IS_SECRET));
                        dto.setModule(resultSet.getString(MODULE));
                        propertyDTOList.add(dto);

                        if (StringUtils.isBlank(moduleName)) {
                            moduleName = resultSet.getString(ENTITLEMENT_MODULE_NAME);
                        }

                    } while (resultSet.next());
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException(String.format("Error while retrieving subscriber details of id : %s",
                    subscriberId), e);
        }

        return new PublisherDataHolder(propertyDTOList, moduleName);
    }

    /**
     * DAO method to get all subscriber IDs.
     *
     * @param tenantId tenant ID.
     * @return list of subscriber IDs.
     * @throws EntitlementException If an error occurs.
     */
    private List<String> getSubscriberIds(int tenantId) throws EntitlementException {

        List<String> subscriberIdList = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement preparedStmt = new NamedPreparedStatement(connection, GET_SUBSCRIBER_IDS_SQL)) {

            preparedStmt.setInt(TENANT_ID, tenantId);
            try (ResultSet subscriberIds = preparedStmt.executeQuery()) {
                while (subscriberIds.next()) {
                    subscriberIdList.add(subscriberIds.getString(SUBSCRIBER_ID));
                }
            }

        } catch (SQLException e) {
            throw new EntitlementException("Error while retrieving subscriber ids", e);
        }
        return subscriberIdList;
    }

    /**
     * DAO method to insert a subscriber.
     *
     * @param subscriberId subscriber ID.
     * @param holder       publisher data holder.
     * @param tenantId     tenant ID.
     * @throws EntitlementException If an error occurs.
     */
    private void insertSubscriber(String subscriberId, PublisherDataHolder holder, int tenantId)
            throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement createSubscriberPrepStmt = new NamedPreparedStatement(connection,
                CREATE_SUBSCRIBER_SQL);
             NamedPreparedStatement createSubscriberPropertiesPrepStmt = new NamedPreparedStatement(connection,
                     CREATE_SUBSCRIBER_PROPERTIES_SQL)) {

            createSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
            createSubscriberPrepStmt.setString(ENTITLEMENT_MODULE_NAME, holder.getModuleName());
            createSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
            createSubscriberPrepStmt.executeUpdate();

            for (PublisherPropertyDTO dto : holder.getPropertyDTOs()) {
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

    /**
     * DAO method to update a subscriber.
     *
     * @param subscriberId        subscriber ID.
     * @param updatedModuleName   updated module name.
     * @param updatedPropertyDTOS updated property DTOs.
     * @param tenantId            tenant ID.
     * @throws EntitlementException If an error occurs.
     */
    private void updateSubscriber(String subscriberId, String updatedModuleName,
                                  PublisherPropertyDTO[] updatedPropertyDTOS, int tenantId)
            throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            // Update the module name of an existing subscriber
            if (StringUtils.isNotBlank(updatedModuleName)) {
                try (NamedPreparedStatement updateSubscriberPrepStmt = new NamedPreparedStatement(connection,
                        UPDATE_SUBSCRIBER_MODULE_SQL)) {
                    updateSubscriberPrepStmt.setString(ENTITLEMENT_MODULE_NAME, updatedModuleName);
                    updateSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                    updateSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
                    updateSubscriberPrepStmt.executeUpdate();
                }
            }

            // Update the property values of an existing subscriber
            if (ArrayUtils.isNotEmpty(updatedPropertyDTOS)) {
                try (NamedPreparedStatement updateSubscriberPropertiesPrepStmt = new NamedPreparedStatement(connection,
                        UPDATE_SUBSCRIBER_PROPERTIES_SQL)) {
                    for (PublisherPropertyDTO dto : updatedPropertyDTOS) {
                        updateSubscriberPropertiesPrepStmt.setString(PROPERTY_VALUE, dto.getValue());
                        updateSubscriberPropertiesPrepStmt.setString(PROPERTY_ID, dto.getId());
                        updateSubscriberPropertiesPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                        updateSubscriberPropertiesPrepStmt.setInt(TENANT_ID, tenantId);
                        updateSubscriberPropertiesPrepStmt.addBatch();
                    }
                    updateSubscriberPropertiesPrepStmt.executeBatch();
                }
            }
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while updating subscriber details", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * DAO method to delete a subscriber.
     *
     * @param subscriberId subscriber ID.
     * @param tenantId     tenant ID.
     * @throws EntitlementException If an error occurs.
     */
    private void deleteSubscriber(String subscriberId, int tenantId) throws EntitlementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement preparedStmt = new NamedPreparedStatement(connection, DELETE_SUBSCRIBER_SQL)) {

            preparedStmt.setString(SUBSCRIBER_ID, subscriberId);
            preparedStmt.setInt(TENANT_ID, tenantId);
            preparedStmt.executeUpdate();

        } catch (SQLException e) {
            throw new EntitlementException("Error while deleting subscriber details", e);
        }
    }

    /**
     * DAO method to check whether a subscriber exists.
     *
     * @param subscriberId subscriber ID.
     * @param tenantId     tenant ID.
     * @return whether the subscriber exists or not.
     * @throws EntitlementException If an error occurs.
     */
    private boolean isSubscriberExists(String subscriberId, int tenantId) throws EntitlementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement findSubscriberExistencePrepStmt = new NamedPreparedStatement(connection,
                    GET_SUBSCRIBER_EXISTENCE_SQL)) {
                findSubscriberExistencePrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                findSubscriberExistencePrepStmt.setInt(TENANT_ID, tenantId);

                try (ResultSet resultSet = findSubscriberExistencePrepStmt.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while checking subscriber existence", e);
        }
    }

    private String getUpdatedModuleName(PublisherDataHolder holder, PublisherDataHolder oldHolder) {

        if (holder == null || oldHolder == null) {
            return null;
        }
        if (!oldHolder.getModuleName().equalsIgnoreCase(holder.getModuleName())) {
            return holder.getModuleName();
        }
        return null;
    }

    private PublisherPropertyDTO[] getUpdatedPropertyDTOs(PublisherDataHolder holder, PublisherDataHolder oldHolder) {

        if (holder == null || oldHolder == null) {
            return new PublisherPropertyDTO[0];
        }
        List<PublisherPropertyDTO> updatedPropertyDTOs = new ArrayList<>();
        for (PublisherPropertyDTO newPropertyDTO : holder.getPropertyDTOs()) {
            if (StringUtils.isNotBlank(newPropertyDTO.getId()) && StringUtils.isNotBlank(newPropertyDTO.getValue())) {

                PublisherPropertyDTO oldPropertyDTO = oldHolder.getPropertyDTO(newPropertyDTO.getId());
                if (oldPropertyDTO == null || !oldPropertyDTO.getValue().equalsIgnoreCase(newPropertyDTO.getValue())) {
                    updatedPropertyDTOs.add(newPropertyDTO);
                }
            }
        }
        return updatedPropertyDTOs.toArray(new PublisherPropertyDTO[0]);
    }

    /**
     * Sets the base64 encoded secret value of the secret subscriber properties, if it has been updated.
     *
     * @param propertyDTOs list of subscriber properties
     */
    private PublisherPropertyDTO[] encryptUpdatedSecretProperties(PublisherPropertyDTO[] propertyDTOs)
            throws EntitlementException {

        if (propertyDTOs == null) {
            return new PublisherPropertyDTO[0];
        }
        List<PublisherPropertyDTO> updatedPropertyDTOs = new ArrayList<>();
        for (PublisherPropertyDTO propertyDTO : propertyDTOs) {
            if (propertyDTO.isSecret()) {
                try {
                    String encryptedValue = CryptoUtil.getDefaultCryptoUtil()
                            .encryptAndBase64Encode(propertyDTO.getValue().getBytes());
                    propertyDTO.setValue(encryptedValue);
                } catch (CryptoException e) {
                    throw new EntitlementException("Error while encrypting secret value of subscriber. Update cannot " +
                            "proceed.", e);
                }
            }
            updatedPropertyDTOs.add(propertyDTO);
        }
        return updatedPropertyDTOs.toArray(new PublisherPropertyDTO[0]);
    }

    /**
     * Decrypts the secret values of the subscriber properties.
     *
     * @param properties list of subscriber properties
     */
    // TODO: check if we can use common secret table or a separate table
    private void decryptSecretProperties(PublisherPropertyDTO[] properties) {

        for (PublisherPropertyDTO dto : properties) {
            if (dto.isSecret()) {
                try {
                    String password = new String(CryptoUtil.getDefaultCryptoUtil()
                            .base64DecodeAndDecrypt(dto.getValue()));
                    dto.setValue(password);
                } catch (CryptoException e) {
                    LOG.error("Error while decrypting secret value of subscriber.", e);
                }
            }
        }
    }
}
