/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles creating event and publishing events related to role management.
 */
public class RoleManagementEventPublisherProxy {

    private static final Log log = LogFactory.getLog(RoleManagementEventPublisherProxy.class);
    private static final RoleManagementEventPublisherProxy proxy = new RoleManagementEventPublisherProxy();


    private RoleManagementEventPublisherProxy() {

    }

    public static RoleManagementEventPublisherProxy getInstance() {

        return proxy;
    }

    /**
     * Publish event before a new role is added.
     *
     * @param roleName     The name of the role being added.
     * @param userList     A list of user IDs associated with this role.
     * @param groupList    A list of group IDs associated with this role.
     * @param permissions  A list of permissions associated with this role.
     * @param audience     The audience type for which the role is being created.
     * @param audienceId   The ID of the audience type.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs.
     */
    public void publishPreAddRoleWithException(String roleName, List<String> userList, List<String> groupList,
                                               List<Permission> permissions, String audience, String audienceId,
                                               String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_NAME, roleName);
        eventProperties.put(IdentityEventConstants.EventProperty.USER_LIST, userList);
        eventProperties.put(IdentityEventConstants.EventProperty.GROUP_LIST, groupList);
        eventProperties.put(IdentityEventConstants.EventProperty.PERMISSIONS, permissions);
        eventProperties.put(IdentityEventConstants.EventProperty.AUDIENCE, audience);
        eventProperties.put(IdentityEventConstants.EventProperty.AUDIENCE_ID, audienceId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_ADD_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after a new role is added.
     *
     * @param roleName     The name of the role being added.
     * @param userList     A list of user IDs associated with this role.
     * @param groupList    A list of group IDs associated with this role.
     * @param permissions  A list of permissions associated with this role.
     * @param audience     The audience type for which the role is being created.
     * @param audienceId   The ID of the audience type.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostAddRole(String roleId, String roleName, List<String> userList, List<String> groupList,
                                   List<Permission> permissions, String audience, String audienceId,
                                   String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_NAME, roleName);
        eventProperties.put(IdentityEventConstants.EventProperty.USER_LIST, userList);
        eventProperties.put(IdentityEventConstants.EventProperty.GROUP_LIST, groupList);
        eventProperties.put(IdentityEventConstants.EventProperty.PERMISSIONS, permissions);
        eventProperties.put(IdentityEventConstants.EventProperty.AUDIENCE, audience);
        eventProperties.put(IdentityEventConstants.EventProperty.AUDIENCE_ID, audienceId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_ADD_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Public event before retrieving a list of roles based on specified criteria.
     *
     * @param limit        The maximum number of roles to retrieve.
     * @param offset       The starting index from which to retrieve roles.
     * @param sortBy       The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder    The order in which to sort the roles.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetRolesWithException(Integer limit, Integer offset, String sortBy, String sortOrder,
                                                String tenantDomain) throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_ROLES_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Public event before retrieving a list of roles based on specified criteria.
     *
     * @param limit              The maximum number of roles to retrieve.
     * @param offset             The starting index from which to retrieve roles.
     * @param sortBy             The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder          The order in which to sort the roles.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @param requiredAttributes Required attributes.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetRolesWithException(Integer limit, Integer offset, String sortBy, String sortOrder,
                                                String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        eventProperties.put(IdentityEventConstants.EventProperty.REQUIRED_ATTRIBUTES, requiredAttributes);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_ROLES_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Public event after retrieving a list of roles based on specified criteria.
     *
     * @param limit        The maximum number of roles to retrieve.
     * @param offset       The starting index from which to retrieve roles.
     * @param sortBy       The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder    The order in which to sort the roles.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                    String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_ROLES_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Public event after retrieving a list of roles based on specified criteria.
     *
     * @param limit              The maximum number of roles to retrieve.
     * @param offset             The starting index from which to retrieve roles.
     * @param sortBy             The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder          The order in which to sort the roles.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @param requiredAttributes Required attributes.
     */
    public void publishPostGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                    String tenantDomain, List<String> requiredAttributes) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        eventProperties.put(IdentityEventConstants.EventProperty.REQUIRED_ATTRIBUTES, requiredAttributes);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_ROLES_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving a list of roles based on specified criteria.
     *
     * @param filter       The filter value.
     * @param limit        The maximum number of roles to retrieve.
     * @param offset       The starting index from which to retrieve roles.
     * @param sortBy       The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder    The order in which to sort the roles.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetRolesWithException(String filter, Integer limit, Integer offset, String sortBy,
                                                String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.FILTER, filter);
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_ROLES_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event before retrieving a list of roles based on specified criteria.
     *
     * @param filter             The filter value.
     * @param limit              The maximum number of roles to retrieve.
     * @param offset             The starting index from which to retrieve roles.
     * @param sortBy             The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder          The order in which to sort the roles.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @param requiredAttributes Required attributes.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetRolesWithException(String filter, Integer limit, Integer offset, String sortBy,
                                                String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.FILTER, filter);
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        eventProperties.put(IdentityEventConstants.EventProperty.REQUIRED_ATTRIBUTES, requiredAttributes);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_ROLES_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving a list of roles based on specified criteria.
     *
     * @param filter       The filter value.
     * @param limit        The maximum number of roles to retrieve.
     * @param offset       The starting index from which to retrieve roles.
     * @param sortBy       The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder    The order in which to sort the roles.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                    String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.FILTER, filter);
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_ROLES_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event after retrieving a list of roles based on specified criteria.
     *
     * @param filter             The filter value.
     * @param limit              The maximum number of roles to retrieve.
     * @param offset             The starting index from which to retrieve roles.
     * @param sortBy             The attribute by which the roles should be sorted (e.g., "name").
     * @param sortOrder          The order in which to sort the roles.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @param requiredAttributes Required attributes.
     */
    public void publishPostGetRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                    String tenantDomain, List<String> requiredAttributes) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.FILTER, filter);
        eventProperties.put(IdentityEventConstants.EventProperty.LIMIT, limit);
        eventProperties.put(IdentityEventConstants.EventProperty.OFFSET, offset);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_BY, sortBy);
        eventProperties.put(IdentityEventConstants.EventProperty.SORT_ORDER, sortOrder);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        eventProperties.put(IdentityEventConstants.EventProperty.REQUIRED_ATTRIBUTES, requiredAttributes);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_ROLES_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving details of a specific role.
     *
     * @param roleId       The unique identifier of the role to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetRoleWithException(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving details of a specific role.
     *
     * @param roleId       The unique identifier of the role to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetRole(String roleId, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving the list of permissions associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the permissions list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetPermissionListOfRoleWithException(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.PRE_GET_PERMISSION_LIST_OF_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving the list of permissions associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the permissions list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetPermissionListOfRole(String roleId, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.POST_GET_PERMISSION_LIST_OF_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before updating the list of permissions associated with a specific role.
     *
     * @param roleId             The unique identifier of the role for which the permissions are to be updated.
     * @param addedPermissions   A list of permissions to be newly associated with the role.
     * @param deletedPermissions A list of permissions to be disassociated from the role.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-update phase.
     */
    public void publishPreUpdatePermissionsForRoleWithException(String roleId, List<Permission> addedPermissions,
                                                                List<Permission> deletedPermissions,
                                                                String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.ADDED_PERMISSIONS, addedPermissions);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETED_PERMISSIONS, deletedPermissions);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.PRE_UPDATE_PERMISSIONS_FOR_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after updating the list of permissions associated with a specific role.
     *
     * @param roleId             The unique identifier of the role for which the permissions are to be updated.
     * @param addedPermissions   A list of permissions to be newly associated with the role.
     * @param deletedPermissions A list of permissions to be disassociated from the role.
     */
    public void publishPostUpdatePermissionsForRole(String roleId, List<Permission> addedPermissions,
                                                 List<Permission> deletedPermissions, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.ADDED_PERMISSIONS, addedPermissions);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETED_PERMISSIONS, deletedPermissions);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.POST_UPDATE_PERMISSIONS_FOR_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before deleting a specific role.
     *
     * @param roleId       The unique identifier of the role to be deleted.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-deletion phase.
     */
    public void publishPreDeleteRoleWithException(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after deleting a specific role.
     *
     * @param roleId       The unique identifier of the role to be deleted.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostDeleteRole(String roleId, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before updating the name of a specific role.
     *
     * @param roleId       The unique identifier of the role whose name is to be updated.
     * @param newRoleName  The new name intended for the role.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-update phase.
     */
    public void publishPreUpdateRoleNameWithException(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_ROLE_NAME, newRoleName);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_ROLE_V2_NAME_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after updating the name of a specific role.
     *
     * @param roleId       The unique identifier of the role whose name is to be updated.
     * @param newRoleName  The new name intended for the role.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostUpdateRoleName(String roleId, String newRoleName, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_ROLE_NAME, newRoleName);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_UPDATE_ROLE_V2_NAME_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving the list of groups associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the group list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetGroupListOfRoleWithException(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_GROUP_LIST_OF_ROLES_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving the list of groups associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the group list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetGroupListOfRole(String roleId, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_GROUP_LIST_OF_ROLES_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before updating the list of users associated with a specific role.
     *
     * @param roleId            The unique identifier of the role for which the user list is to be updated.
     * @param newUserIDList     A list of user IDs to be newly associated with the role.
     * @param deletedUserIDList A list of user IDs to be disassociated from the role.
     * @param tenantDomain      The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-update phase.
     */
    public void publishPreUpdateUserListOfRoleWithException(String roleId, List<String> newUserIDList,
                                                            List<String> deletedUserIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_USER_ID_LIST, newUserIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETE_USER_ID_LIST, deletedUserIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_USER_LIST_OF_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after updating the list of users associated with a specific role.
     *
     * @param roleId            The unique identifier of the role for which the user list is to be updated.
     * @param newUserIDList     A list of user IDs to be newly associated with the role.
     * @param deletedUserIDList A list of user IDs to be disassociated from the role.
     * @param tenantDomain      The domain in which the operation is being performed.
     */
    public void publishPostUpdateUserListOfRole(String roleId, List<String> newUserIDList,
                                                List<String> deletedUserIDList, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_USER_ID_LIST, newUserIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETE_USER_ID_LIST, deletedUserIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_UPDATE_USER_LIST_OF_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving the list of users associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the user list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetUserListOfRoleWithException(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_USER_LIST_OF_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving the list of users associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the user list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetUserListOfRole(String roleId, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_USER_LIST_OF_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before updating the list of groups associated with a specific role.
     *
     * @param roleId             The unique identifier of the role for which the group list is to be updated.
     * @param newGroupIDList     A list of group IDs to be newly associated with the role.
     * @param deletedGroupIDList A list of group IDs to be disassociated from the role.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-update phase.
     */
    public void publishPreUpdateGroupListOfRoleWithException(String roleId, List<String> newGroupIDList,
                                                             List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_GROUP_ID_LIST, newGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETE_GROUP_ID_LIST, deletedGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after updating the list of groups associated with a specific role.
     *
     * @param roleId             The unique identifier of the role for which the group list is to be updated.
     * @param newGroupIDList     A list of group IDs to be newly associated with the role.
     * @param deletedGroupIDList A list of group IDs to be disassociated from the role.
     * @param tenantDomain       The domain in which the operation is being performed.
     */
    public void publishPostUpdateGroupListOfRole(String roleId, List<String> newGroupIDList,
                                                 List<String> deletedGroupIDList, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_GROUP_ID_LIST, newGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETE_GROUP_ID_LIST, deletedGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.POST_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving the list of Identity Provider (IdP) groups associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the IdP group list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetIdpGroupListOfRoleWithException(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_GROUP_LIST_OF_ROLES_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving the list of Identity Provider (IdP) groups associated with a specific role.
     *
     * @param roleId       The unique identifier of the role for which the IdP group list is to be retrieved.
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostIdpGetGroupListOfRole(String roleId, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_GROUP_LIST_OF_ROLES_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before updating the list of Identity Provider (IdP) groups associated with a specific role.
     *
     * @param roleId             The unique identifier of the role for which the IdP group list is to be updated.
     * @param newGroupIDList     A list of IdP groups to be newly associated with the role.
     * @param deletedGroupIDList A list of IdP groups to be disassociated from the role.
     * @param tenantDomain       The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-update phase.
     */
    public void publishPreUpdateIdpGroupListOfRoleWithException(String roleId, List<IdpGroup> newGroupIDList,
                                                             List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_GROUP_ID_LIST, newGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETE_GROUP_ID_LIST, deletedGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after updating the list of Identity Provider (IdP) groups associated with a specific role.
     *
     * @param roleId             The unique identifier of the role for which the IdP group list is to be updated.
     * @param newGroupIDList     A list of IdP groups to be newly associated with the role.
     * @param deletedGroupIDList A list of IdP groups to be disassociated from the role.
     * @param tenantDomain       The domain in which the operation is being performed.
     */
    public void publishPostUpdateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupIDList,
                                                 List<IdpGroup> deletedGroupIDList, String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.ROLE_ID, roleId);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_GROUP_ID_LIST, newGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETE_GROUP_ID_LIST, deletedGroupIDList);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.POST_UPDATE_GROUP_LIST_OF_ROLE_V2_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Publish event before retrieving the count of roles within a specified tenant domain.
     *
     * @param tenantDomain The domain in which the operation is being performed.
     * @throws IdentityRoleManagementException If an error occurs during the pre-retrieval phase.
     */
    public void publishPreGetRolesCountWithException(String tenantDomain) throws IdentityRoleManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_GET_ROLES_V2_COUNT_EVENT);
        doPublishEvent(event);
    }

    /**
     * Publish event after retrieving the count of roles within a specified tenant domain.
     *
     * @param tenantDomain The domain in which the operation is being performed.
     */
    public void publishPostGetRolesCount(String tenantDomain) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_GET_ROLES_V2_COUNT_EVENT);
        try {
            doPublishEvent(event);
        } catch (IdentityRoleManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Event createEvent(Map<String, Object> eventProperties, String eventName) {

        return new Event(eventName, eventProperties);
    }

    private void doPublishEvent(Event event) throws IdentityRoleManagementException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Event: " + event.getEventName() + " is published for the role management operation in " +
                        "the tenant with the tenantId: "
                        + event.getEventProperties().get(IdentityEventConstants.EventProperty.TENANT_ID));
            }
            IdentityEventService eventService =
                    RoleManagementServiceComponentHolder.getInstance().getIdentityEventService();
            eventService.handleEvent(event);
        } catch (IdentityEventException e) {
            // Throws an excpetion if the error is a custom event handler error.
            if (RoleConstants.Error.ERROR_CODE_CUSTOM_EVENT_HANDLER_ERROR.getCode().equals(e.getErrorCode())) {
                throw new IdentityRoleManagementException(e.getErrorCode(),
                        "Error while publishing the event: " + event.getEventName() + ".", e);
            }
            log.error("Error while publishing the event: " + event.getEventName() + ".", e);
        }
    }
}
