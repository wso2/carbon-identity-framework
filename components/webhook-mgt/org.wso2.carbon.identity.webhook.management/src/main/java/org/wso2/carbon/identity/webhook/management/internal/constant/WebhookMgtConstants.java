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

package org.wso2.carbon.identity.webhook.management.internal.constant;

/**
 * Constants for Webhook Management.
 */
public class WebhookMgtConstants {

    public static final String WEBHOOK_NAME_FIELD = "Webhook name";
    public static final String ENDPOINT_URI_FIELD = "Endpoint URI";
    public static final String EVENT_PROFILE_NAME_FIELD = "Event Profile Name";
    public static final String EVENT_PROFILE_URI_FIELD = "Event Profile URI";
    public static final String CHANNELS_SUBSCRIBED_FIELD = "Channels Subscribed";
    public static final String SECRET_FIELD = "Secret";
    public static final String STATUS_FIELD = "Status";

    /**
     * Organization lifecycle events this component reacts to, and the properties they carry.
     * <p>
     * These names are the contract published by the organization management extension bundle
     * (org.wso2.carbon.identity.organization.management.ext.Constants). They are restated here
     * rather than imported so that webhook management does not take a build dependency on that
     * bundle for four string literals. The names are part of the identity event contract and are
     * matched by value at runtime, so they must not be changed independently of it.
     */
    public static final String EVENT_POST_ADD_ORGANIZATION = "POST_ADD_ORGANIZATION";
    public static final String EVENT_POST_DELETE_ORGANIZATION = "POST_DELETE_ORGANIZATION";
    public static final String EVENT_PROP_ORGANIZATION = "ORGANIZATION";
    public static final String EVENT_PROP_ORGANIZATION_ID = "ORGANIZATION_ID";

    private WebhookMgtConstants() {

    }
}
