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

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;

import java.util.List;

/**
 * HybridSubscriberPersistenceManager is a hybrid implementation of SubscriberPersistenceManager. It uses both JDBC and
 * Registry implementations. All new subscribers will be added to the database, while existing subscribers will be
 * maintained in the registry.
 */
public class HybridSubscriberPersistenceManager implements SubscriberPersistenceManager {

    private final JDBCSubscriberPersistenceManager jdbcSubscriberPersistenceManager =
            new JDBCSubscriberPersistenceManager();
    private final RegistrySubscriberPersistenceManager registrySubscriberPersistenceManager =
            new RegistrySubscriberPersistenceManager();

    @Override
    public void addSubscriber(PublisherDataHolder holder) throws EntitlementException {

        String subscriberId = EntitlementUtil.resolveSubscriberId(holder);
        if (subscriberId == null) {
            throw new EntitlementException("Subscriber Id can not be null");
        }
        if (registrySubscriberPersistenceManager.isSubscriberExists(subscriberId)) {
            throw new EntitlementException("Subscriber ID already exists");
        }
        jdbcSubscriberPersistenceManager.addSubscriber(holder);
    }

    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, boolean shouldDecryptSecrets)
            throws EntitlementException {

        if (jdbcSubscriberPersistenceManager.isSubscriberExists(subscriberId)) {
            return jdbcSubscriberPersistenceManager.getSubscriber(subscriberId, shouldDecryptSecrets);
        }
        return registrySubscriberPersistenceManager.getSubscriber(subscriberId, shouldDecryptSecrets);
    }

    @Override
    public List<String> listSubscriberIds(String filter) throws EntitlementException {

        List<String> subscriberIds = jdbcSubscriberPersistenceManager.listSubscriberIds(filter);
        List<String> registrySubscriberIds = registrySubscriberPersistenceManager.listSubscriberIds(filter);
        return EntitlementUtil.mergeLists(subscriberIds, registrySubscriberIds);
    }

    @Override
    public void updateSubscriber(PublisherDataHolder holder) throws EntitlementException {

        String subscriberId = EntitlementUtil.resolveSubscriberId(holder);
        if (jdbcSubscriberPersistenceManager.isSubscriberExists(subscriberId)) {
            jdbcSubscriberPersistenceManager.updateSubscriber(holder);
        } else {
            registrySubscriberPersistenceManager.updateSubscriber(holder);
        }
    }

    @Override
    public void removeSubscriber(String subscriberId) throws EntitlementException {

        if (jdbcSubscriberPersistenceManager.isSubscriberExists(subscriberId)) {
            jdbcSubscriberPersistenceManager.removeSubscriber(subscriberId);
        } else {
            registrySubscriberPersistenceManager.removeSubscriber(subscriberId);
        }
    }
}
