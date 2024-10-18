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

package org.wso2.carbon.identity.entitlement.persistence;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.wso2.carbon.identity.entitlement.PDPConstants.SUBSCRIBER_ID;

/**
 * This implementation handles the subscriber management in the Registry.
 */
public class RegistrySubscriberPersistenceManager implements SubscriberPersistenceManager {

    // The logger that is used for all messages
    private static final Log LOG = LogFactory.getLog(RegistrySubscriberPersistenceManager.class);
    private final Registry registry;

    public RegistrySubscriberPersistenceManager() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        this.registry = EntitlementServiceComponent.getGovernanceRegistry(tenantId);
    }

    /**
     * Adds a subscriber.
     *
     * @param holder publisher data holder
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void addSubscriber(PublisherDataHolder holder) throws EntitlementException {

        persistSubscriber(holder, false);
    }

    /**
     * Gets the requested subscriber.
     *
     * @param subscriberId         subscriber ID
     * @param shouldDecryptSecrets whether the subscriber should get returned with secret(decrypted) values or not
     * @return publisher data holder
     * @throws EntitlementException If an error occurs
     */
    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, boolean shouldDecryptSecrets)
            throws EntitlementException {

        try {
            if (registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                    RegistryConstants.PATH_SEPARATOR + subscriberId)) {
                Resource resource = registry.get(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                        RegistryConstants.PATH_SEPARATOR + subscriberId);

                return getPublisherDataHolder(resource, shouldDecryptSecrets);
            }
        } catch (RegistryException e) {
            throw new EntitlementException("Error while retrieving subscriber detail of id : " + subscriberId, e);
        }

        throw new EntitlementException("No Subscriber is defined for the given Id");

    }

    /**
     * Gets all subscriber IDs.
     *
     * @param filter search string
     * @return list of subscriber IDs
     * @throws EntitlementException If an error occurs
     */
    @Override
    public List<String> listSubscriberIds(String filter) throws EntitlementException {

        try {
            if (registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                    RegistryConstants.PATH_SEPARATOR)) {
                Resource resource = registry.get(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                        RegistryConstants.PATH_SEPARATOR);
                Collection collection = (Collection) resource;
                List<String> list = new ArrayList<>();
                if (collection.getChildCount() > 0) {
                    for (String path : collection.getChildren()) {
                        Resource childResource = registry.get(path);
                        if (childResource != null && childResource.getProperty(SUBSCRIBER_ID) != null) {
                            list.add(childResource.getProperty(SUBSCRIBER_ID));
                        }
                    }
                }
                return EntitlementUtil.filterSubscribers(list, filter);
            }
        } catch (RegistryException e) {
            throw new EntitlementException("Error while retrieving subscriber ids", e);
        }
        return Collections.emptyList();
    }

    /**
     * Updates a subscriber.
     *
     * @param holder publisher data holder
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void updateSubscriber(PublisherDataHolder holder) throws EntitlementException {

        persistSubscriber(holder, true);
    }

    /**
     * Removes the subscriber of the given subscriber ID.
     *
     * @param subscriberId subscriber ID
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void removeSubscriber(String subscriberId) throws EntitlementException {

        String subscriberPath;

        if (subscriberId == null) {
            throw new EntitlementException("Subscriber Id can not be null");
        }

        if (EntitlementConstants.PDP_SUBSCRIBER_ID.equals(subscriberId.trim())) {
            throw new EntitlementException("Can not delete PDP publisher");
        }

        try {
            subscriberPath = PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                    RegistryConstants.PATH_SEPARATOR + subscriberId;

            if (registry.resourceExists(subscriberPath)) {
                registry.delete(subscriberPath);
            }
        } catch (RegistryException e) {
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

        try {
            return registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                    RegistryConstants.PATH_SEPARATOR + subscriberId);
        } catch (RegistryException e) {
            throw new EntitlementException("Error while checking subscriber existence", e);
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

        Collection policyCollection;
        String subscriberPath;
        String subscriberId = EntitlementUtil.resolveSubscriberId(holder);
        if (subscriberId == null) {
            throw new EntitlementException("Subscriber Id can not be null");
        }

        try {
            if (registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER)) {
                policyCollection = registry.newCollection();
                registry.put(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER, policyCollection);
            }

            subscriberPath = PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                    RegistryConstants.PATH_SEPARATOR + subscriberId;

            Resource resource;

            PublisherDataHolder oldHolder = null;
            if (registry.resourceExists(subscriberPath)) {
                if (isUpdate) {
                    resource = registry.get(subscriberPath);
                    oldHolder = getPublisherDataHolder(resource, false);
                } else {
                    throw new EntitlementException("Subscriber ID already exists");
                }
            } else {
                resource = registry.newResource();
            }

            populateProperties(holder, oldHolder, resource);
            registry.put(subscriberPath, resource);

        } catch (RegistryException e) {
            throw new EntitlementException("Error while persisting subscriber details", e);
        }
    }

    /**
     * Populate subscriber properties.
     *
     * @param holder    subscriber data holder
     * @param oldHolder old publisher data holder
     * @param resource  registry resource
     */
    private void populateProperties(PublisherDataHolder holder, PublisherDataHolder oldHolder, Resource resource)
            throws EntitlementException {

        PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();
        for (PublisherPropertyDTO dto : propertyDTOs) {
            if (StringUtils.isNotBlank(dto.getId()) && StringUtils.isNotBlank(dto.getValue())) {
                ArrayList<String> list = new ArrayList<>();
                if (dto.isSecret()) {
                    PublisherPropertyDTO propertyDTO = null;
                    if (oldHolder != null) {
                        propertyDTO = oldHolder.getPropertyDTO(dto.getId());
                    }
                    if (propertyDTO == null || !propertyDTO.getValue().equalsIgnoreCase(dto.getValue())) {
                        try {
                            String encryptedValue = CryptoUtil.getDefaultCryptoUtil().
                                    encryptAndBase64Encode(dto.getValue().getBytes());
                            dto.setValue(encryptedValue);
                        } catch (CryptoException e) {
                            throw new EntitlementException("Error while encrypting secret value of subscriber. Update" +
                                    " cannot proceed.", e);
                        }
                    }
                }
                list.add(dto.getValue());
                list.add(dto.getDisplayName());
                list.add(Integer.toString(dto.getDisplayOrder()));
                list.add(Boolean.toString(dto.isRequired()));
                list.add(Boolean.toString(dto.isSecret()));
                resource.setProperty(dto.getId(), list);
            }
        }
        resource.setProperty(PublisherDataHolder.MODULE_NAME, holder.getModuleName());
    }

    private PublisherDataHolder getPublisherDataHolder(Resource resource, boolean returnSecrets) {

        List<PublisherPropertyDTO> propertyDTOs = new ArrayList<>();
        String moduleName = null;
        if (resource != null && resource.getProperties() != null) {
            Properties properties = resource.getProperties();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                PublisherPropertyDTO dto = new PublisherPropertyDTO();
                dto.setId((String) entry.getKey());
                Object value = entry.getValue();
                if (value instanceof ArrayList) {
                    List list = (ArrayList) entry.getValue();
                    if (!list.isEmpty() && list.get(0) != null) {
                        dto.setValue((String) list.get(0));

                        if (list.size() > 1 && list.get(1) != null) {
                            dto.setDisplayName((String) list.get(1));
                        }
                        if (list.size() > 2 && list.get(2) != null) {
                            dto.setDisplayOrder(Integer.parseInt((String) list.get(2)));
                        }
                        if (list.size() > 3 && list.get(3) != null) {
                            dto.setRequired(Boolean.parseBoolean((String) list.get(3)));
                        }
                        if (list.size() > 4 && list.get(4) != null) {
                            dto.setSecret(Boolean.parseBoolean((String) list.get(4)));
                        }

                        if (dto.isSecret() && returnSecrets) {
                            String password = dto.getValue();
                            try {
                                password = new String(CryptoUtil.getDefaultCryptoUtil().
                                        base64DecodeAndDecrypt(dto.getValue()));
                            } catch (CryptoException e) {
                                LOG.error(e);
                                // ignore
                            }
                            dto.setValue(password);
                        }
                    }
                }
                if (PublisherDataHolder.MODULE_NAME.equals(dto.getId())) {
                    moduleName = dto.getValue();
                    continue;
                }

                propertyDTOs.add(dto);
            }
        }
        return new PublisherDataHolder(propertyDTOs, moduleName);
    }
}
