/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.central.log.mgt.utils;

import java.util.regex.Pattern;

/**
 * Constants used for log management.
 */
public class LogConstants {

    public static final String FAILED = "FAILED";
    public static final String SUCCESS = "SUCCESS";

    /**
     * Constants related to masking sensitive info in logs.
     */
    public static final String USER_ID_CLAIM_URI = "http://wso2.org/claims/userid";
    public static final String MASKING_CHARACTER = "*";
    public static final String ENABLE_LOG_MASKING = "MaskingLogs.Enabled";
    public static final Pattern LOG_MASKING_PATTERN = Pattern.compile("(?<=.).(?=.)");

    /**
     * Define common and reusable Input keys for diagnostic logs.
     */
    public static class InputKeys {

        public static final String SERVICE_PROVIDER = "service provider";
        public static final String TENANT_DOMAIN = "tenant domain";
        public static final String USER = "user";
        public static final String USER_ID = "user id";
        public static final String AUTHENTICATOR_NAME = "authenticator name";
        public static final String STEP = "step";
        public static final String COUNT = "count";
        public static final String IDP = "idp";
        public static final String APPLICATION_NAME = "application name";
        public static final String SUBJECT = "subject";
        public static final String CLIENT_ID = "client id";
        public static final String REDIREDCT_URI = "redirect uri";
        public static final String SCOPE = "scope";
        public static final String APPLICATION_ID = "app id";
        public static final String ERROR_MESSAGE = "error message";
        public static final String CALLER_PATH = "common auth caller path";
    }

    public static class UserManagement {
        public static final String ADD_USER_ACTION = "add-user";
        public static final String DELETE_USER_ACTION = "delete-user";
        public static final String SET_USER_CLAIM_VALUE_ACTION = "set-user-claim-value";
        public static final String SET_USER_CLAIM_VALUES_ACTION = "set-user-claim-values";
        public static final String DELETE_USER_CLAIM_VALUES_ACTION = "delete-user-claim-values";
        public static final String DELETE_USER_CLAIM_VALUE_ACTION = "delete-user-claim-value";
        public static final String CHANGE_PASSWORD_BY_USER_ACTION = "change-password-by-user";
        public static final String CHANGE_PASSWORD_BY_ADMIN_ACTION = "change-password-by-administrator";
        public static final String GET_USER_LIST_ACTION = "get-user-list";
        public static final String GET_USER_CLAIM_VALUE_ACTION = "get-user-claim-value";
        public static final String GET_USER_CLAIM_VALUES_ACTION = "get-user-claim-values";
        public static final String UPDATE_USER_GROUP_ACTION = "update-group-of-group";
        public static final String UPDATE_GROUPS_OF_USER_ACTION = "update-groups-of-user";

        public static final String ADD_GROUP_ACTION = "add-group";
        public static final String GET_GROUP_ACTION = "get-group";
        public static final String DELETE_GROUP_ACTION = "delete-group";
        public static final String UPDATE_GROUP_ACTION = "update-group";
        public static final String UPDATE_GROUP_NAME_ACTION = "update-group-name";
        public static final String GET_GROUP_LIST = "get-group-list";
        public static final String GET_USERS_OF_GROUP = "get-users-of-group";
        public static final String GET_GROUPS_OF_USERS = "get-groups-of-users";
        public static final String UPDATE_USERS_OF_GROUP_ACTION = "update-users-of-group";

        public static final String DELETE_ROLE_ACTION = "delete-role";
        public static final String ADD_ORG_ROLE_ACTION = "add-organization-role";
        public static final String ADD_APP_ROLE_ACTION = "add-application-role";
        public static final String GET_ROLE_LIST_ACTION = "get-role-list";
        public static final String GET_ROLE_ACTION = "get-role";
        public static final String GET_ROLE_BASIC_INFO_ACTION = "get-role-basicInformation";
        public static final String UPDATE_ROLE_NAME_ACTION = "update-role-name";
        public static final String UPDATE_USERS_OF_ROLE_ACTION = "update-users-of-role";
        public static final String GET_GROUP_LIST_OF_ROLE_ACTION = "get-group-list-of-role";
        public static final String UPDATE_GROUP_LIST_OF_ROLE_ACTION = "update-group-list-of-role";
        public static final String GET_IDP_GROUPS_OF_ROLES = "get-idp-groups-of-role";
        public static final String UPDATE_IDP_GROUPS_OF_ROLES = "update-idp-groups-of-role";
        public static final String GET_PERMISSIONS_OF_ROLES = "get-permissions-of-role";
        public static final String UPDATE_PERMISSIONS_OF_ROLES_ACTION = "update-permissions-of-role";
        public static final String UPDATE_ROLES_OF_USER_ACTION = "update-roles-of-user";
        public static final String GET_ROLES_OF_USER_ACTION = "get-roles-of-user";
        public static final String GET_ROLES_OF_GROUP_ACTION = "get-roles-of-group";
        public static final String GET_USERS_OF_ROLE_ACTION = "get-users-of-role";
        public static final String DELETE_ROLES_BY_APP_ACTION = "delete-application-roles";
        public static final String LOGIN = "login";
        public static final String UPDATE_PERMISSIONS_OF_ROLE_ACTION = "update-permissions-of-role";


        public static final String USERS_FIELD = "Users";
        public static final String GROUPS_FIELD = "Groups";
        public static final String ROLES_FIELD = "Roles";

        public static final String APP_ROLE_FIELD = "ApplicationRoles";
        public static final String AUDIENCE_FIELD = "Audience";
        public static final String ASSOCIATED_APPLICATIONS_FIELD = "AssociatedApplications";
        public static final String REQUIRED_ATTRIBUTES_FIELD = "RequiredAttributes";
        public static final String ORG_ROLE_FIELD = "OrganizationRoles";
        public static final String ROLE_NAME_FIELD = "RoleName";
        public static final String PERMISSIONS_FIELD = "Permissions";
        public static final String DELETED_USERS = "DeletedUsers";
        public static final String NEW_USERS = "NewUsers";
        public static final String DELETED_ROLES = "DeletedRoles";
        public static final String NEW_ROLES = "NewRoles";
        public static final String DELETED_GROUPS = "DeletedGroups";
        public static final String IDP_GROUPS_FIELD = "IdpGroups";
        public static final String ADDED_IDP_GROUPS_FIELD = "AddedIdpGroups";
        public static final String ADDED_PERMISSIONS_FIELD = "AddedPermissions";
        public static final String DELETED_PERMISSIONS_FIELD = "DeletedPermissions";
        public static final String DELETED_IDP_GROUPS_FIELD = "DeletedIdpGroups";
        public static final String ADDED_GROUPS_FIELD = "AddedGroups";
    }
}
