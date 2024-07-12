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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Log log = LogFactory.getLog(JDBCSubscriberDAOImpl.class);
    private static final String ERROR_SUBSCRIBER_ID_NULL = "Subscriber Id can not be null";

    /**
     * Adds a subscriber.
     *
     * @param holder publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addSubscriber(PublisherDataHolder holder) throws EntitlementException {

        persistSubscriber(holder, false);
    }

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

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try (NamedPreparedStatement getSubscriberPrepStmt = new NamedPreparedStatement(connection,
                GET_SUBSCRIBER_SQL)) {
            getSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
            getSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
            try (ResultSet rs1 = getSubscriberPrepStmt.executeQuery()) {
                if (rs1.next()) {
                    return getPublisherDataHolder(rs1, returnSecrets);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new EntitlementException(String.format("Error while retrieving subscriber details of id : %s",
                    subscriberId), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
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

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try (NamedPreparedStatement getSubscriberIdsPrepStmt = new NamedPreparedStatement(connection,
                GET_SUBSCRIBER_IDS_SQL)) {
            getSubscriberIdsPrepStmt.setInt(TENANT_ID, tenantId);

            List<String> subscriberIDList = new ArrayList<>();
            filter = filter.replace("*", ".*");
            Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);

            try (ResultSet subscriberIds = getSubscriberIdsPrepStmt.executeQuery()) {
                if (subscriberIds.next()) {
                    do {
                        String id = subscriberIds.getString(SUBSCRIBER_ID);
                        Matcher matcher = pattern.matcher(id);
                        if (!matcher.matches()) {
                            continue;
                        }
                        subscriberIDList.add(id);

                    } while (subscriberIds.next());

                    return subscriberIDList;

                } else {
                    return Collections.emptyList();
                }
            }

        } catch (SQLException e) {
            throw new EntitlementException("Error while retrieving subscriber ids", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Updates a subscriber.
     *
     * @param holder publisher data holder.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void updateSubscriber(PublisherDataHolder holder) throws EntitlementException {

        persistSubscriber(holder, true);
    }

    /**
     * Removes the subscriber of the given subscriber ID.
     *
     * @param subscriberId subscriber ID.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void removeSubscriber(String subscriberId) throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (subscriberId == null) {
            throw new EntitlementException(ERROR_SUBSCRIBER_ID_NULL);
        }

        if (EntitlementConstants.PDP_SUBSCRIBER_ID.equals(subscriberId.trim())) {
            throw new EntitlementException("Can not delete PDP publisher");
        }

        try (NamedPreparedStatement deleteSubscriberPrepStmt = new NamedPreparedStatement(connection,
                DELETE_SUBSCRIBER_SQL)) {

            deleteSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
            deleteSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
            deleteSubscriberPrepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while deleting subscriber details", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Adds or updates a subscriber.
     *
     * @param holder   publisher data holder
     * @param isUpdate whether the operation is an update or an addition
     * @throws EntitlementException If an error occurs
     */
    private void persistSubscriber(PublisherDataHolder holder, boolean isUpdate) throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String subscriberId = null;

        if (holder == null || holder.getPropertyDTOs() == null) {
            throw new EntitlementException("Publisher data can not be null");
        }

        for (PublisherPropertyDTO dto : holder.getPropertyDTOs()) {
            if (PDPConstants.SUBSCRIBER_ID.equals(dto.getId())) {
                subscriberId = dto.getValue();
            }
        }

        if (subscriberId == null) {
            throw new EntitlementException(ERROR_SUBSCRIBER_ID_NULL);
        }

        try {
            PublisherDataHolder oldHolder = null;
            // Find whether the subscriber already exists
            try (NamedPreparedStatement findSubscriberExistencePrepStmt = new NamedPreparedStatement(connection,
                    GET_SUBSCRIBER_EXISTENCE_SQL)) {
                findSubscriberExistencePrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                findSubscriberExistencePrepStmt.setInt(TENANT_ID, tenantId);

                try (ResultSet rs1 = findSubscriberExistencePrepStmt.executeQuery()) {
                    if (rs1.next()) {
                        if (isUpdate) {
                            // Get the existing subscriber
                            oldHolder = getSubscriber(subscriberId, false);
                        } else {
                            throw new EntitlementException("Subscriber ID already exists");
                        }
                    }
                }
            }

            populateProperties(holder, oldHolder);
            PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();

            if (!isUpdate) {
                // Create a new subscriber
                insertSubscriber(connection, subscriberId, holder, propertyDTOs);
            } else {
                // Update the module of an existing subscriber
                updateSubscriber(connection, subscriberId, holder, propertyDTOs, oldHolder);

            }
            IdentityDatabaseUtil.commitTransaction(connection);

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while persisting subscriber details", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private void insertSubscriber(Connection connection, String subscriberId,
                                  PublisherDataHolder holder, PublisherPropertyDTO[] propertyDTOs) throws SQLException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try (NamedPreparedStatement createSubscriberPrepStmt = new NamedPreparedStatement(connection,
                CREATE_SUBSCRIBER_SQL);
             NamedPreparedStatement createSubscriberPropertiesPrepStmt = new NamedPreparedStatement(connection,
                     CREATE_SUBSCRIBER_PROPERTIES_SQL)) {
            createSubscriberPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
            createSubscriberPrepStmt.setInt(TENANT_ID, tenantId);
            createSubscriberPrepStmt.setString(ENTITLEMENT_MODULE_NAME, holder.getModuleName());
            createSubscriberPrepStmt.executeUpdate();

            for (PublisherPropertyDTO dto : propertyDTOs) {
                if (dto.getId() != null && dto.getValue() != null && !dto.getValue().trim().isEmpty()) {

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
        }
    }

    private void updateSubscriber(Connection connection, String subscriberId, PublisherDataHolder holder,
                                  PublisherPropertyDTO[] propertyDTOs, PublisherDataHolder oldHolder)
            throws SQLException {

        if (oldHolder == null) {
            return;
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
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

                if (dto.getId() != null && dto.getValue() != null && !dto.getValue().trim().isEmpty()) {
                    PublisherPropertyDTO propertyDTO;
                    propertyDTO = oldHolder.getPropertyDTO(dto.getId());
                    if (propertyDTO != null && !propertyDTO.getValue().equalsIgnoreCase(dto.getValue())) {
                        updateSubscriberPropertiesPrepStmt.setString(PROPERTY_VALUE, dto.getValue());
                        updateSubscriberPropertiesPrepStmt.setString(SUBSCRIBER_ID, subscriberId);
                        updateSubscriberPropertiesPrepStmt.setInt(TENANT_ID, tenantId);
                        updateSubscriberPropertiesPrepStmt.setString(PROPERTY_ID, dto.getId());
                        updateSubscriberPropertiesPrepStmt.addBatch();
                    }
                }
            }
            updateSubscriberPropertiesPrepStmt.executeBatch();
        }
    }

    private void populateProperties(PublisherDataHolder holder, PublisherDataHolder oldHolder) {

        PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();

        for (PublisherPropertyDTO dto : propertyDTOs) {
            if (dto.getId() != null && dto.getValue() != null && !dto.getValue().trim().isEmpty() && (dto.isSecret())) {
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
                        log.error("Error while encrypting secret value of subscriber. Secret would not be persisted.",
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

            if (dto.isSecret() && (returnSecrets)) {
                String password = dto.getValue();
                try {
                    password = new String(CryptoUtil.getDefaultCryptoUtil().
                            base64DecodeAndDecrypt(dto.getValue()));
                } catch (CryptoException e) {
                    log.error(e);
                    // ignore
                }
                dto.setValue(password);
            }

            moduleName = resultSet.getString(ENTITLEMENT_MODULE_NAME);
            propertyDTOList.add(dto);
        } while (resultSet.next());
        return new PublisherDataHolder(propertyDTOList, moduleName);
    }
}
