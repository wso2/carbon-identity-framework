/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.subscription.management.api.model;

/**
 * Enum for subscriber status.
 * This enum defines the possible statuses of a subscriber in the pubsub hub.
 */
public enum SubscriptionStatus {

    /**
     * Subscription is accepted by the pubsub hub.
     */
    SUBSCRIPTION_ACCEPTED,
    /**
     * Subscription is not tried yet.
     */
    SUBSCRIPTION_PENDING,
    /**
     * Subscriber is not accepted by the pubsub hub due to an error (e.g., invalid endpoint or network issue).
     */
    SUBSCRIPTION_ERROR,
    /**
     * Unsubscription is accepted by the pubsub hub.
     */
    UNSUBSCRIPTION_ACCEPTED,
    /**
     * Unsubscription is not tried yet.
     */
    UNSUBSCRIPTION_PENDING,
    /**
     * Unsubscription failed due to an error (e.g., invalid state or network issue).
     */
    UNSUBSCRIPTION_ERROR,
}
