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
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dao.puredao.SubscriberPureDAO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the JDBC operations of the subscribers in the data store.
 */
public class JDBCSubscriberDAOImpl implements SubscriberDAO {

    private static final Log LOG = LogFactory.getLog(JDBCSubscriberDAOImpl.class);
    private static final String ERROR_SUBSCRIBER_ID_NULL = "Subscriber Id can not be null";
    private static final SubscriberPureDAO subscriberPureDAO = new SubscriberPureDAO();

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

        PublisherDataHolder publisherDataHolder = subscriberPureDAO.getSubscriber(subscriberId, tenantId);
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
        List<String> subscriberIdList = subscriberPureDAO.getSubscriberIds(tenantId);
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
        subscriberPureDAO.insertSubscriber(subscriberId, holder, tenantId);
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
            subscriberPureDAO.updateSubscriber(subscriberId, updatedModuleName, updatedPropertyDTOs, tenantId);
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

        subscriberPureDAO.deleteSubscriber(subscriberId, tenantId);
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
        return subscriberPureDAO.isSubscriberExists(subscriberId, tenantId);
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
