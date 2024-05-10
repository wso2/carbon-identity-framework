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

import java.util.List;


/**
 * This interface supports the management of subscribers.
 */
public interface SubscriberDAO {


    /**
     * Adds a subscriber.
     *
     * @param holder publisher data holder
     * @throws EntitlementException If an error occurs
     */
    void addSubscriber(PublisherDataHolder holder) throws EntitlementException;


    /**
     * Gets the requested subscriber.
     *
     * @param subscriberId subscriber ID
     * @param returnSecrets whether the subscriber should get returned with secret(decrypted) values or not
     * @return publisher data holder
     * @throws EntitlementException If an error occurs
     */
    PublisherDataHolder getSubscriber(String subscriberId, boolean returnSecrets) throws EntitlementException;


    /**
     * Lists all subscriber IDs.
     *
     * @param filter search string
     * @return list of subscriber IDs
     * @throws EntitlementException If an error occurs
     */
    List<String> listSubscriberIds(String filter) throws EntitlementException;


    /**
     * Updates a subscriber.
     *
     * @param holder publisher data holder
     * @throws EntitlementException If an error occurs
     */
    void updateSubscriber(PublisherDataHolder holder) throws EntitlementException;


    /**
     * Removes the subscriber of the given subscriber ID.
     *
     * @param subscriberId subscriber ID
     * @throws EntitlementException If an error occurs
     */
    void removeSubscriber(String subscriberId) throws EntitlementException;

}
