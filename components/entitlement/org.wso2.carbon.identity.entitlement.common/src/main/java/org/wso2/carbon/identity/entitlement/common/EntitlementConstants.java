/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common;

/**
 *
 */
public class EntitlementConstants {

    public static final String ENTITLEMENT_REGISTRY_PATH = "repository/identity/entitlement";

    public static final String ENTITLEMENT_POLICY_EDITOR_REGISTRY_PATH =
            ENTITLEMENT_REGISTRY_PATH + "/policyEditor";

    public static final String ENTITLEMENT_POLICY_BASIC_EDITOR_CONFIG_FILE_REGISTRY_PATH =
            ENTITLEMENT_REGISTRY_PATH + "/policyEditor/config/basic/config.xml";
    public static final String ENTITLEMENT_POLICY_STANDARD_EDITOR_CONFIG_FILE_REGISTRY_PATH =
            ENTITLEMENT_REGISTRY_PATH + "/policyEditor/config/standard/config.xml";
    public static final String ENTITLEMENT_POLICY_RBAC_EDITOR_CONFIG_FILE_REGISTRY_PATH =
            ENTITLEMENT_REGISTRY_PATH + "/policyEditor/config/rbac/config.xml";
    public static final String ENTITLEMENT_POLICY_SET_EDITOR_CONFIG_FILE_REGISTRY_PATH =
            ENTITLEMENT_REGISTRY_PATH + "/policyEditor/config/set/config.xml";

    public static final String PDP_SUBSCRIBER_ID = "PDP Subscriber";

    public static final String PROP_USE_LAST_STATUS_ONLY = "EntitlementSettings.XacmlPolicyStatus.UseLastStatusOnly";

    public static final class PolicyPublish {

        public static final String ACTION_CREATE = "CREATE";

        public static final String ACTION_UPDATE = "UPDATE";

        public static final String ACTION_DELETE = "DELETE";

        public static final String ACTION_ENABLE = "ENABLE";

        public static final String ACTION_DISABLE = "DISABLE";

        public static final String ACTION_ORDER = "ORDER";
    }

    public static final class StatusTypes {

        public static final String ADD_POLICY = "ADD_POLICY";

        public static final String UPDATE_POLICY = "UPDATE_POLICY";

        public static final String GET_POLICY = "GET_POLICY";

        public static final String DELETE_POLICY = "DELETE_POLICY";

        public static final String ENABLE_POLICY = "ENABLE_POLICY";

        public static final String PUBLISH_POLICY = "PUBLISH_POLICY";

        public static final String ROLLBACK_POLICY = "ROLLBACK_POLICY";

        public static final String[] ALL_TYPES = new String[]{ADD_POLICY, UPDATE_POLICY, GET_POLICY,
                DELETE_POLICY, ENABLE_POLICY, PUBLISH_POLICY, ROLLBACK_POLICY};
    }

    public static final class Status {

        public static final String ABOUT_POLICY = "POLICY";

        public static final String ABOUT_SUBSCRIBER = "SUBSCRIBER";

    }

    public static final class PolicyEditor {

        public static final String BASIC = "BASIC";

        public static final String STANDARD = "STANDARD";

        public static final String RBAC = "RBAC";

        public static final String SET = "SET";

        public static final String[] EDITOR_TYPES = new String[]{BASIC, STANDARD, RBAC, SET};

        public static final String BASIC_CATEGORY_SUBJECT = "Subject";

        public static final String BASIC_CATEGORY_ACTION = "Action";

        public static final String BASIC_CATEGORY_ENVIRONMENT = "Environment";

        public static final String BASIC_CATEGORY_RESOURCE = "Resource";

        public static final String[] BASIC_CATEGORIES = new String[]{BASIC_CATEGORY_SUBJECT,
                BASIC_CATEGORY_RESOURCE, BASIC_CATEGORY_ACTION, BASIC_CATEGORY_ENVIRONMENT};

    }

    public static final class PolicyType {

        public static final String POLICY_ENABLED = "PDP_ENABLED";

        public static final String POLICY_DISABLED = "PDP_DISABLED";

        public static final String POLICY_ALL = "ALL";

    }
}
