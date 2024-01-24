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

package org.wso2.carbon.role.mgt.ui;

import org.wso2.carbon.CarbonConstants;

/**
 * 
 */
public class UserAdminUIConstants {

    public static final String USER_STORE_INFO = "org.wso2.carbon.userstore.info";

    public static final String DOMAIN_SEPARATOR = CarbonConstants.DOMAIN_SEPARATOR;

    public static final String MULTIPLE_USER_STORE = "multiple.user.store";
    public static final String USER_DISPLAY_NAME = "user.display.name";
    public static final String ROLE_READ_ONLY = "org.wso2.carbon.role.read.only";
    public static final String ALL_ITEMS_IN_PAGE = "pages-Items";
    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static final String ASK_PASSWORD_CLAIM_URI = "http://wso2.org/claims/identity/askPassword";
    public static final String ACCOUNT_LOCKED_CLAIM_URI = "http://wso2.org/claims/identity/accountLocked";

    public static final String PRIMARY_DOMAIN_NAME_NOT_DEFINED = "PRIMARY-DOMAIN [Domain name is not defined]";

    public static final String ALL_DOMAINS = "ALL-USER-STORE-DOMAINS";

    public static final String INTERNAL_ROLE = "Internal";
    public static final String INTERNAL_DOMAIN = "Internal";
    public static final String APPLICATION_DOMAIN = "Application";
    public static final String WORKFLOW_DOMAIN = "Workflow";
    public static final String EXTERNAL_ROLE = "External";

    // errors
    public static final String DO_USER_LIST = "org.wso2.carbon.user.list.error";
    public static final String DO_ROLE_LIST = "org.wso2.carbon.role.list.error";

    // filters
    public static final String USER_LIST_FILTER = "org.wso2.carbon.user.filter";
    public static final String USER_COUNT_FILTER = "org.wso2.carbon.user.count.filter";
    public static final String USER_COUNT = "org.wso2.carbon.user.count";
    public static final String USER_LIST_UNASSIGNED_ROLE_FILTER = "org.wso2.carbon.user.unassigned.role.filter";
    public static final String USER_LIST_VIEW_ROLE_FILTER = "org.wso2.carbon.user.view.role.filter";
    public static final String USER_LIST_ASSIGN_ROLE_FILTER = "org.wso2.carbon.user.assign.filter";

    public static final String USER_LIST_DOMAIN_FILTER = "org.wso2.carbon.user.domain.filter";
    public static final String USER_LIST_COUNT_DOMAIN_FILTER = "org.wso2.carbon.user.count.domain.filter";
    public static final String USER_LIST_DOMAIN_UNASSIGNED_ROLE_FILTER = "org.wso2.carbon.user.domain.unassigned.role.filter";
    public static final String USER_LIST_DOMAIN_VIEW_ROLE_FILTER = "org.wso2.carbon.user.domain.view.role.filter";
    public static final String USER_LIST_DOMAIN_ASSIGN_ROLE_FILTER = "org.wso2.carbon.user.domain.assign.filter";
    public static final String USER_CLAIM_FILTER = "org.wso2.carbon.user.claim.filter";
    public static final String USER_CLAIM_COUNT_FILTER = "org.wso2.carbon.user.claim.count.filter";

    public static final String ROLE_LIST_FILTER = "org.wso2.carbon.role.filter";
    public static final String ROLE_LIST_UNASSIGNED_USER_FILTER = "org.wso2.carbon.role.unassigned.user.filter";
    public static final String ROLE_LIST_VIEW_USER_FILTER = "org.wso2.carbon.role.view.user.filter";
    public static final String ROLE_LIST_ASSIGN_USER_FILTER = "org.wso2.carbon.role.assign.filter";
    public static final String ROLE_COUNT_FILTER = "org.wso2.carbon.role.count.filter";
    public static final String ROLE_COUNT = "org.wso2.carbon.role.count";
    public static final String ROLE_LIST_COUNT_DOMAIN_FILTER = "org.wso2.carbon.role.count.domain.filter";


    public static final String ROLE_LIST_DOMAIN_FILTER = "org.wso2.carbon.role.domain.filter";
    public static final String ROLE_LIST_DOMAIN_UNASSIGNED_USER_FILTER = "org.wso2.carbon.role.domain.unassigned.user.filter";
    public static final String ROLE_LIST_DOMAIN_VIEW_USER_FILTER = "org.wso2.carbon.role.domain.view.user.filter";
    public static final String ROLE_LIST_DOMAIN_ASSIGN_USER_FILTER = "org.wso2.carbon.role.domain.assign.filter";    
    // caches
    public static final String USER_LIST_CACHE = "user.list.cache";
    public static final String USER_LIST_CACHE_EXCEEDED = "user.list.cache.exceeded.domains";

    public static final String USER_LIST_UNASSIGNED_ROLE_CACHE = "user.list.unassigned.role.cache";
    public static final String USER_LIST_UNASSIGNED_ROLE_CACHE_EXCEEDED = "user.list.unassigned.role.cache.exceeded.domains";

    public static final String USER_LIST_ASSIGNED_ROLE_CACHE = "user.list.assigned.role.cache";
    public static final String USER_LIST_ASSIGNED_ROLE_CACHE_EXCEEDED = "user.list.assigned.role.cache.exceeded.domains";

    public static final String USER_LIST_ADD_USER_ROLE_CACHE = "user.list.add.user.cache";
    public static final String USER_LIST_ADD_USER_ROLE_CACHE_EXCEEDED = "user.list.add.user.cache.exceeded.domains";

    public static final String ROLE_LIST_CACHE = "role.list.cache";
    public static final String ROLE_LIST = "role.list";
    public static final String ROLE_LIST_CACHE_EXCEEDED = "role.list.cache.exceeded.domains";

    public static final String ROLE_LIST_UNASSIGNED_USER_CACHE = "role.list.unassigned.user.cache";
    public static final String ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED = "role.list.unassigned.user.cache.exceeded.domains";

    public static final String ROLE_LIST_ASSIGNED_USER_CACHE = "role.list.assigned.user.cache";
    public static final String ROLE_LIST_ASSIGNED_USER_CACHE_EXCEEDED = "role.list.assigned.user.cache.exceeded.domains";

    public static final String ROLE_LIST_ADD_ROLE_USER_CACHE = "role.list.add.role.cache";
    public static final String ROLE_LIST_ADD_ROLE_USER_CACHE_EXCEEDED = "role.list.add.role.cache.exceeded.domains";
    
    public static final String SHARED_ROLE_ENABLED = "shared.role.enabled";
    public static final String SELECT = "Select";

    public static final String CONFIG_DISALLOWED_CHARACTER_REGEX = "UserIdentifier.DisallowedCharacters";

    private UserAdminUIConstants(){

    }
}
