/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.workflow.mgt.rule;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The unified Data Provider for Workflow Rule Evaluations across all workflow event types.
 */
public class WorkFlowRuleEvaluationDataProvider implements RuleEvaluationDataProvider {

    private static final Log log = LogFactory.getLog(WorkFlowRuleEvaluationDataProvider.class);

    private static final String WSO2_CLAIM_URI_PREFIX = "http://wso2.org/claims/";
    private static final String USER_STORE_DOMAIN = "User Store Domain";
    private static final String USERS_TO_BE_ASSIGNED = "Users to be Added";
    private static final String USERS_TO_BE_UNASSIGNED = "Users to be Deleted";
    private static final String USERNAME = "Username";
    private static final String ROLE_ID = "Role ID";
    private static final String ROLE_AUDIENCE_ID = "Audience ID";
    private static final String EVENT_TYPE = "eventType";
    private static final String CLAIMS = "Claims";

    private static final String ADD_USER_EVENT = "ADD_USER";
    private static final String SELF_REGISTER_USER_EVENT = "SELF_REGISTER_USER";

    /**
     * Enum for supported non-claim rule fields in workflow operations.
     */
    private enum RuleField {
        USER_DOMAIN("user.domain"),
        USER_GROUPS("user.groups"),
        USER_ROLES("user.roles"),
        INITIATOR_DOMAIN("initiator.domain"),
        INITIATOR_GROUPS("initiator.groups"),
        INITIATOR_ROLES("initiator.roles"),
        ROLE_ID("role.id"),
        ROLE_AUDIENCE("role.audience"),
        ROLE_PERMISSIONS("role.permissions"),
        ROLE_HAS_ASSIGNED_USERS("role.hasAssignedUsers"),
        ROLE_HAS_UNASSIGNED_USERS("role.hasUnassignedUsers");

        private final String fieldName;

        RuleField(String fieldName) {

            this.fieldName = fieldName;
        }

        public String getFieldName() {

            return fieldName;
        }

        private static final Map<String, RuleField> FIELD_NAME_MAP;

        static {
            FIELD_NAME_MAP = new HashMap<>();
            for (RuleField ruleField : RuleField.values()) {
                FIELD_NAME_MAP.put(ruleField.getFieldName(), ruleField);
            }
        }

        /**
         * Get RuleField from field name if it's a known non-claim field.
         *
         * @param fieldName Field name.
         * @return RuleField if found, null otherwise.
         */
        public static RuleField valueOfFieldName(String fieldName) {

            return FIELD_NAME_MAP.get(fieldName);
        }
    }

    /**
     * Check if a field name is a claim URI.
     *
     * @param fieldName Field name to check.
     * @return True if the field name is a claim URI.
     */
    private boolean isClaimUri(String fieldName) {

        return fieldName != null && fieldName.startsWith(WSO2_CLAIM_URI_PREFIX);
    }

    @Override
    public FlowType getSupportedFlowType() {
        
        return FlowType.APPROVAL_WORKFLOW;
    }

    @Override
    public List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext, FlowContext flowContext,
                                              String tenantDomain) throws RuleEvaluationDataProviderException {

        List<FieldValue> fieldValues = new ArrayList<>();
        Map<String, Object> contextData = flowContext.getContextData();
        if (log.isDebugEnabled()) {
            log.debug("Processing workflow rule evaluation for tenant: " + tenantDomain +
                     " with event type: " + contextData.get(EVENT_TYPE));
        }

        List<Field> claimFields = new ArrayList<>();
        List<Field> nonClaimFields = new ArrayList<>();
        for (Field field : ruleEvaluationContext.getFields()) {
            if (isClaimUri(field.getName())) {
                claimFields.add(field);
            } else {
                nonClaimFields.add(field);
            }
        }
        if (!claimFields.isEmpty()) {
            addUserClaimFieldValues(fieldValues, claimFields, contextData, tenantDomain);
        }
        for (Field field : nonClaimFields) {
            String fieldName = field.getName();
            // Reuse already resolved field values.
            FieldValue existingFieldValue = findResolvedFieldValue(fieldValues, fieldName);
            if (existingFieldValue != null) {
                // Check the value type to create the appropriate FieldValue.
                if (existingFieldValue.getValueType() == ValueType.LIST) {
                    fieldValues.add(new FieldValue(field.getName(),
                            (List<String>) existingFieldValue.getValue()));
                } else if (existingFieldValue.getValueType() == ValueType.REFERENCE) {
                    fieldValues.add(new FieldValue(field.getName(), (String) existingFieldValue.getValue(),
                            ValueType.REFERENCE));
                } else {
                    fieldValues.add(new FieldValue(field.getName(), (String) existingFieldValue.getValue(),
                            ValueType.STRING));
                }
                continue;
            }
            try {
                RuleField ruleField = RuleField.valueOfFieldName(fieldName);
                if (ruleField == null) {
                    throw new RuleEvaluationDataProviderException("Unsupported field: " + fieldName);
                }
                resolveNonClaimFieldValue(fieldValues, field, ruleField, contextData, tenantDomain);
            } catch (RuleEvaluationDataProviderException e) {
                throw e;
            } catch (Exception e) {
                throw new RuleEvaluationDataProviderException("Error processing field: " + fieldName, e);
            }
        }

        return fieldValues;
    }

    /**
     * Find a resolved field value from the list of already processed field values.
     *
     * @param fieldValues List of already resolved field values.
     * @param fieldName Field name to search for.
     * @return Resolved FieldValue if found, null otherwise.
     */
    private FieldValue findResolvedFieldValue(List<FieldValue> fieldValues, String fieldName) {

        for (FieldValue fieldValue : fieldValues) {
            if (fieldValue.getName().equals(fieldName)) {
                return fieldValue;
            }
        }
        return null;
    }

    /**
     * Resolve and add the field value for a known non-claim workflow rule field.
     *
     * @param fieldValues  List of field values to add to.
     * @param field        Field being processed.
     * @param ruleField    Resolved RuleField enum constant.
     * @param contextData  Context data from the flow context.
     * @param tenantDomain Tenant domain.
     * @throws RuleEvaluationDataProviderException If an error occurs while resolving the field value.
     */
    private void resolveNonClaimFieldValue(List<FieldValue> fieldValues, Field field, RuleField ruleField,
                                           Map<String, Object> contextData, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        switch (ruleField) {
            case USER_DOMAIN:
                addUserDomainFieldValue(fieldValues, field, contextData);
                break;
            case USER_GROUPS:
                addUserGroupsFieldValue(fieldValues, field, contextData, tenantDomain);
                break;
            case USER_ROLES:
                addUserRolesFieldValue(fieldValues, field, contextData, tenantDomain);
                break;
            case ROLE_AUDIENCE:
                addRoleAudienceIdFieldValue(fieldValues, field, contextData, tenantDomain);
                break;
            case ROLE_ID:
                addRoleIdFieldValue(fieldValues, field, contextData);
                break;
            case ROLE_HAS_ASSIGNED_USERS:
                addRoleHasAssignedUsersFieldValue(fieldValues, field, contextData);
                break;
            case ROLE_HAS_UNASSIGNED_USERS:
                addRoleHasUnassignedUsersFieldValue(fieldValues, field, contextData);
                break;
            default:
                throw new RuleEvaluationDataProviderException(
                        "Unsupported field by workflow rule evaluation data provider: " + field.getName());
        }
    }

    /**
     * Add user domain field value from context data.
     */
    private void addUserDomainFieldValue(List<FieldValue> fieldValues, Field field, Map<String, Object> contextData) {

        String userStoreDomain = (String) contextData.get(USER_STORE_DOMAIN);
        fieldValues.add(new FieldValue(field.getName(),
                StringUtils.isNotBlank(userStoreDomain) ? userStoreDomain : null, ValueType.STRING));
    }

    /**
     * Add role ID field value from context data.
     */
    private void addRoleIdFieldValue(List<FieldValue> fieldValues, Field field, Map<String, Object> contextData) {

        String roleId = (String) contextData.get(ROLE_ID);
        fieldValues.add(new FieldValue(field.getName(),
                StringUtils.isNotBlank(roleId) ? roleId : null, ValueType.STRING));
    }

    /**
     * Add user role field value from context data or fetch from user store.
     * Retrieves role IDs (not role names) to match against rule expressions.
     */
    private void addUserRolesFieldValue(List<FieldValue> fieldValues, Field field, Map<String, Object> contextData,
                                       String tenantDomain) throws RuleEvaluationDataProviderException {

        String username = (String) contextData.get(USERNAME);
        try {
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext
                    .getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            // Get user ID from username.
            String userId = userStoreManager.getUserIDFromUserName(username);
            if (StringUtils.isBlank(userId)) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not resolve user ID for username: " + LoggerUtils.getMaskedContent(username) +
                            ". Adding empty role list.");
                }
                fieldValues.add(new FieldValue(field.getName(), Collections.emptyList()));
                return;
            }
            // Get role IDs using RoleManagementService.
            RoleManagementService roleManagementService = WorkflowServiceDataHolder.getInstance()
                    .getRoleManagementService();
            List<String> roleIdList = roleManagementService.getRoleIdListOfUser(userId, tenantDomain);
            fieldValues.add(new FieldValue(field.getName(),
                    CollectionUtils.isNotEmpty(roleIdList) ? roleIdList : Collections.emptyList()));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new RuleEvaluationDataProviderException(
                    "Error retrieving user ID for username: " + username, e);
        } catch (IdentityRoleManagementException e) {
            throw new RuleEvaluationDataProviderException(
                    "Error retrieving role IDs for username: " + username, e);
        }
    }

    /**
     * Add user groups field value from context data or fetch from user store.
     */
    private void addUserGroupsFieldValue(List<FieldValue> fieldValues, Field field, Map<String, Object> contextData,
                                        String tenantDomain) throws RuleEvaluationDataProviderException {

        String username = (String) contextData.get(USERNAME);
        try {
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext
                    .getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            // Get user ID from username.
            String userId = userStoreManager.getUserIDFromUserName(username);
            if (StringUtils.isBlank(userId)) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not resolve user ID for username: " + LoggerUtils.getMaskedContent(username) +
                            ". Adding empty group list.");
                }
                fieldValues.add(new FieldValue(field.getName(), Collections.emptyList()));
                return;
            }
            List<org.wso2.carbon.user.core.common.Group> groupList =
                    userStoreManager.getGroupListOfUser(userId, null, null);
            List<String> groupIds = CollectionUtils.isNotEmpty(groupList)
                    ? groupList.stream()
                            .map(org.wso2.carbon.user.core.common.Group::getGroupID)
                            .collect(Collectors.toList())
                    : Collections.emptyList();
            fieldValues.add(new FieldValue(field.getName(), groupIds));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new RuleEvaluationDataProviderException(
                    "Error retrieving groups for username: " + username, e);
        }
    }

    /**
     * Batch process user claim fields by fetching from user store.
     * First checks if claim values are available in context data, then fetches remaining claims in a single call.
     *
     * @param fieldValues List of field values to add to.
     * @param claimFields List of claim URI fields to process.
     * @param contextData Context data from the flow context.
     * @param tenantDomain Tenant domain.
     * @throws RuleEvaluationDataProviderException If error occurs while fetching claims.
     */
    private void addUserClaimFieldValues(List<FieldValue> fieldValues, List<Field> claimFields,
                                    Map<String, Object> contextData, String tenantDomain)
                                    throws RuleEvaluationDataProviderException {

        Map<String, String> claimValueMap = new HashMap<>();
        Set<String> claimsToFetch = new HashSet<>();
        Map<String, String> nestedClaims =
                (contextData.get(CLAIMS) instanceof Map)
                        ? (Map<String, String>) contextData.get(CLAIMS)
                        : Collections.emptyMap();

        // Check context data and collect claims to fetch.
        for (Field field : claimFields) {
            String claimUri = field.getName();
            if (claimValueMap.containsKey(claimUri)) {
                continue;
            }
            // First look in the nested claims map, then fall back to a direct context lookup.
            String claimValue = nestedClaims.get(claimUri);
            if (StringUtils.isNotBlank(claimValue)) {
                claimValueMap.put(claimUri, claimValue);
            } else {
                claimsToFetch.add(claimUri);
            }
        }
        String eventType = (String) contextData.get(EVENT_TYPE);
        // Batch fetch remaining claims from the user store.
        if (!claimsToFetch.isEmpty() &&
                (ADD_USER_EVENT.equals(eventType) || SELF_REGISTER_USER_EVENT.equals(eventType))) {
            String username = (String) contextData.get(USERNAME);
            if (StringUtils.isBlank(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot fetch claims without Username in context. Claims: " + claimsToFetch);
                }
            } else {
                try {
                    AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext
                            .getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                    Map<String, String> claims = userStoreManager.getUserClaimValues(
                            username,
                            claimsToFetch.toArray(new String[0]),
                            UserCoreConstants.DEFAULT_PROFILE
                    );
                    if (claims != null) {
                        claimValueMap.putAll(claims);
                    }
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new RuleEvaluationDataProviderException(
                            "Error retrieving user claims for username: " + username, e);
                }
            }
        }
        for (Field field : claimFields) {
            String claimUri = field.getName();
            String claimValue = claimValueMap.get(claimUri);
            fieldValues.add(new FieldValue(field.getName(),
                    StringUtils.isNotBlank(claimValue) ? claimValue : null, ValueType.STRING));
        }
    }

    /**
     * Add role audience ID field value by fetching from role management service.
     */
    private void addRoleAudienceIdFieldValue(List<FieldValue> fieldValues, Field field, Map<String, Object> contextData,
                                            String tenantDomain) throws RuleEvaluationDataProviderException {

        // First check if role audience ID is already available in context data.
        String roleAudienceId = (String) contextData.get(ROLE_AUDIENCE_ID);
        if (StringUtils.isNotBlank(roleAudienceId)) {
            fieldValues.add(new FieldValue(field.getName(), roleAudienceId, ValueType.REFERENCE));
            return;
        }

        // If not in context, fetch using Role ID from RoleManagementService.
        String roleId = (String) contextData.get(ROLE_ID);
        if (StringUtils.isBlank(roleId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot fetch role audience ID without Role ID in context. Adding null audience ID.");
            }
            fieldValues.add(new FieldValue(field.getName(), (String) null, ValueType.REFERENCE));
            return;
        }

        RoleBasicInfo roleBasicInfo = null;
        try {
            // Fetch Role Related Details using RoleManagementService.
            RoleManagementService roleManagementService = WorkflowServiceDataHolder.getInstance()
                    .getRoleManagementService();
            roleBasicInfo = roleManagementService.getRoleBasicInfoById(roleId, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new RuleEvaluationDataProviderException("Error retrieving role info for roleId: " + roleId, e);
        }
        String audienceId = (roleBasicInfo != null && StringUtils.isNotBlank(roleBasicInfo.getAudienceId()))
                ? roleBasicInfo.getAudienceId() : null;
        fieldValues.add(new FieldValue(field.getName(), audienceId, ValueType.REFERENCE));
    }

     /**
     * Add role has assigned users field value from context data.
     * Checks if the "Users to be Assigned" list in context data is non-empty.
     *
     * @param fieldValues List of field values to add to.
     * @param field       Field being processed.
     * @param contextData Context data from the flow context.
     */
    private void addRoleHasAssignedUsersFieldValue(List<FieldValue> fieldValues, Field field,
                                                   Map<String, Object> contextData) {

        List<?> usersToBeAssigned = (List<?>) contextData.get(USERS_TO_BE_ASSIGNED);
        boolean hasAssignedUsers = CollectionUtils.isNotEmpty(usersToBeAssigned);
        fieldValues.add(new FieldValue(field.getName(), String.valueOf(hasAssignedUsers), ValueType.STRING));

        if (log.isDebugEnabled()) {
            log.debug("Role has assigned users: " + hasAssignedUsers + " (users to assign count: " +
                    (usersToBeAssigned != null ? usersToBeAssigned.size() : 0) + ")");
        }
    }

    /**
     * Add role has unassigned users field value from context data.
     * Checks if the "Users to be Unassigned" list in context data is non-empty.
     *
     * @param fieldValues List of field values to add to.
     * @param field       Field being processed.
     * @param contextData Context data from the flow context.
     */
    private void addRoleHasUnassignedUsersFieldValue(List<FieldValue> fieldValues, Field field,
                                                     Map<String, Object> contextData) {

        List<?> usersToBeUnassigned = (List<?>) contextData.get(USERS_TO_BE_UNASSIGNED);
        boolean hasUnassignedUsers = CollectionUtils.isNotEmpty(usersToBeUnassigned);
        fieldValues.add(new FieldValue(field.getName(), String.valueOf(hasUnassignedUsers), ValueType.STRING));

        if (log.isDebugEnabled()) {
            log.debug("Role has unassigned users: " + hasUnassignedUsers + " (users to unassign count: " +
                    (usersToBeUnassigned != null ? usersToBeUnassigned.size() : 0) + ")");
        }
    }
}
