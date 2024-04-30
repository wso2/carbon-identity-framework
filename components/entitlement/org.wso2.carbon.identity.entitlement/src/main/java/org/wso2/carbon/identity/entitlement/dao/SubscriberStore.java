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

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;


/**
 * This interface supports the management of subscribers.
 */
public interface SubscriberStore {


    /**
     * Adds a subscriber
     */
    void addSubscriber(PublisherDataHolder holder) throws EntitlementException;


    /**
     * Gets the requested subscriber
     */
    PublisherDataHolder getSubscriber(String subscriberId, boolean returnSecrets) throws EntitlementException;


    /**
     * Gets all subscriber IDs
     */
    String[] getSubscriberIds(String filter) throws EntitlementException;


    /**
     * Updates a subscriber
     */
    void updateSubscriber(PublisherDataHolder holder) throws EntitlementException;


    /**
     * Removes the given subscriber
     */
    void removeSubscriber(String subscriberId) throws EntitlementException;

}
