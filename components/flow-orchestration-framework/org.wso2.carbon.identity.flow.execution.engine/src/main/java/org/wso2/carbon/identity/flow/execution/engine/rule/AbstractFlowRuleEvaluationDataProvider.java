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

package org.wso2.carbon.identity.flow.execution.engine.rule;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Supplies the values a flow condition is evaluated against.
 */
public abstract class AbstractFlowRuleEvaluationDataProvider implements RuleEvaluationDataProvider {

    /**
     * The application the flow runs for, by id.
     */
    public static final String APPLICATION = "application";

    /**
     * What is stored for the user.
     */
    public static final String USER_CLAIMS = "user.claims";

    /**
     * What this run of the flow has gathered so far, whether the user entered it or an executor produced it.
     */
    public static final String USER_COLLECTED_CLAIMS = "user.collectedClaims";

    /**
     * The user store the user belongs to.
     */
    public static final String USER_DOMAIN = "user.domain";

    /**
     * The groups the user is assigned to, by group id.
     */
    public static final String USER_GROUPS = "user.groups";

    /**
     * The roles the user is assigned, directly or through a group, by role id.
     */
    public static final String USER_ROLES = "user.roles";

    /**
     * Key under which the engine places its execution context for evaluation.
     */
    public static final String FLOW_EXECUTION_CONTEXT = "flowExecutionContext";

    @Override
    public List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext, FlowContext flowContext,
                                              String tenantDomain) throws RuleEvaluationDataProviderException {

        FlowExecutionContext context = resolveExecutionContext(flowContext);

        List<FieldValue> fieldValues = new ArrayList<>();
        for (Field field : ruleEvaluationContext.getFields()) {
            fieldValues.add(resolve(field, context, tenantDomain));
        }
        return fieldValues;
    }

    /**
     * Resolve one field.
     *
     * @param field        Field to resolve, carrying its qualifier where it has one.
     * @param context      Flow execution context.
     * @param tenantDomain Tenant domain.
     * @return The value, never null. A value that is missing, such as a claim not collected, resolves to no value,
     * or to an empty list for a multi-valued field.
     * @throws RuleEvaluationDataProviderException If the value cannot be known at this point of the flow, or reading
     *                                             it fails. Neither is read as an empty value.
     */
    protected FieldValue resolve(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        switch (field.getName()) {
            case APPLICATION:
                return application(field, context);
            case USER_COLLECTED_CLAIMS:
                return collectedClaim(field, context);
            case USER_CLAIMS:
                return persistedClaim(field, context, tenantDomain);
            case USER_DOMAIN:
                return userDomain(field, context, tenantDomain);
            case USER_GROUPS:
                return userGroups(field, context, tenantDomain);
            case USER_ROLES:
                return userRoles(field, context, tenantDomain);
            default:
                throw new RuleEvaluationDataProviderException(
                        "No value can be read for condition field: " + field.getName() + ".");
        }
    }

    private FieldValue collectedClaim(Field field, FlowExecutionContext context)
            throws RuleEvaluationDataProviderException {

        FlowUser user = context.getFlowUser();
        String rawValue = user == null || user.getClaims() == null ? null : user.getClaims().get(field.getQualifier());
        return claimValue(field, rawValue, user == null ? null : user.getUserStoreDomain(), context.getTenantDomain());
    }

    private FieldValue application(Field field, FlowExecutionContext context) {

        String applicationId = context.getApplicationId();
        return new FieldValue(field.getName(), StringUtils.isNotBlank(applicationId) ? applicationId : null,
                ValueType.REFERENCE);
    }

    /**
     * The user store of the user, once the user exists.
     */
    private FieldValue userDomain(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        FlowUser user = identifiedUser(field, context);
        if (StringUtils.isNotBlank(user.getUserStoreDomain())) {
            return stringValue(field, user.getUserStoreDomain().toUpperCase());
        }
        try {
            String primaryDomain = userStoreManager(tenantDomain).getRealmConfiguration()
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            return stringValue(field, StringUtils.isNotBlank(primaryDomain)
                    ? primaryDomain.toUpperCase() : UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        } catch (UserStoreException e) {
            throw new RuleEvaluationDataProviderException("Could not read the primary user store of tenant "
                    + tenantDomain + " for the condition on the user's domain.", e);
        }
    }

    /**
     * The ids of the groups the user is assigned to.
     */
    private FieldValue userGroups(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        FlowUser user = identifiedUser(field, context);
        try {
            return new FieldValue(field.getName(), groupIds(user.getUserId(), tenantDomain));
        } catch (UserStoreException e) {
            throw new RuleEvaluationDataProviderException("Could not read the groups of the user in tenant "
                    + tenantDomain + ".", e);
        }
    }

    /**
     * The ids of the roles the user is assigned, whether directly or through one of the user's groups.
     */
    private FieldValue userRoles(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        FlowUser user = identifiedUser(field, context);
        RoleManagementService roleManagementService =
                FlowExecutionEngineDataHolder.getInstance().getRoleManagementService();
        if (roleManagementService == null) {
            throw new RuleEvaluationDataProviderException(
                    "Role management service is not available to read the roles of the user.");
        }
        try {
            Set<String> roleIds = new LinkedHashSet<>(
                    roleManagementService.getRoleIdListOfUser(user.getUserId(), tenantDomain));
            List<String> groupIds = groupIds(user.getUserId(), tenantDomain);
            if (!groupIds.isEmpty()) {
                roleIds.addAll(roleManagementService.getRoleIdListOfGroups(groupIds, tenantDomain));
            }
            return new FieldValue(field.getName(), new ArrayList<>(roleIds));
        } catch (UserStoreException | IdentityRoleManagementException e) {
            throw new RuleEvaluationDataProviderException("Could not read the roles of the user in tenant "
                    + tenantDomain + ".", e);
        }
    }

    private List<String> groupIds(String userId, String tenantDomain) throws UserStoreException {

        List<Group> groups = userStoreManager(tenantDomain).getGroupListOfUser(userId, null, null);
        List<String> groupIds = new ArrayList<>();
        if (groups != null) {
            for (Group group : groups) {
                if (StringUtils.isNotBlank(group.getGroupID())) {
                    groupIds.add(group.getGroupID());
                }
            }
        }
        return groupIds;
    }

    private AbstractUserStoreManager userStoreManager(String tenantDomain) throws UserStoreException {

        return (AbstractUserStoreManager) FlowExecutionEngineDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
    }

    /**
     * Read a claim from the user store.
     */
    private FieldValue persistedClaim(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        FlowUser user = identifiedUser(field, context);

        try {
            Map<String, String> claims = userStoreManager(tenantDomain).getUserClaimValuesWithID(
                    user.getUserId(), new String[]{field.getQualifier()}, null);
            return claimValue(field, claims == null ? null : claims.get(field.getQualifier()),
                    user.getUserStoreDomain(), tenantDomain);
        } catch (UserStoreException e) {
            throw new RuleEvaluationDataProviderException("Could not read claim " + field.getQualifier()
                    + " for the user in tenant " + tenantDomain + ".", e);
        }
    }

    private FlowExecutionContext resolveExecutionContext(FlowContext flowContext)
            throws RuleEvaluationDataProviderException {

        Object context = flowContext.getContextData() == null
                ? null : flowContext.getContextData().get(FLOW_EXECUTION_CONTEXT);
        if (!(context instanceof FlowExecutionContext)) {
            throw new RuleEvaluationDataProviderException(
                    "Flow execution context is not available for rule evaluation.");
        }
        return (FlowExecutionContext) context;
    }

    /**
     * A claim value, as a set when the claim holds more than one value. An empty value counts as no value: a set
     * that is not there rather than an empty one, so isBlank and isEmpty hold on it but notIn does not.
     *
     * @param field           Field being resolved.
     * @param rawValue        Value as stored or collected.
     * @param userStoreDomain User store the separator is configured on, or null for the primary.
     * @param tenantDomain    Tenant domain.
     * @return The value.
     */
    private FieldValue claimValue(Field field, String rawValue, String userStoreDomain, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        if (!isMultiValued(field.getQualifier(), tenantDomain)) {
            return stringValue(field, StringUtils.isEmpty(rawValue) ? null : rawValue);
        }

        if (StringUtils.isEmpty(rawValue)) {
            return new FieldValue(field.getName(), field.getQualifier(), (List<String>) null);
        }
        List<String> values = new ArrayList<>();
        String separator = FrameworkUtils.getMultiAttributeSeparator(userStoreDomain);
        for (String value : rawValue.split(Pattern.quote(separator))) {
            if (StringUtils.isNotEmpty(value)) {
                values.add(value);
            }
        }
        return new FieldValue(field.getName(), field.getQualifier(), values);
    }

    private boolean isMultiValued(String claimUri, String tenantDomain) throws RuleEvaluationDataProviderException {

        if (StringUtils.isBlank(claimUri)) {
            return false;
        }
        ClaimMetadataManagementService claimService =
                FlowExecutionEngineDataHolder.getInstance().getClaimMetadataManagementService();
        if (claimService == null) {
            throw new RuleEvaluationDataProviderException(
                    "Claim metadata service is not available to read claim " + claimUri + ".");
        }
        try {
            Optional<LocalClaim> claim = claimService.getLocalClaim(claimUri, tenantDomain);
            return claim.isPresent() && Boolean.parseBoolean(
                    claim.get().getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
        } catch (ClaimMetadataException e) {
            throw new RuleEvaluationDataProviderException("Could not read the metadata of claim " + claimUri
                    + " in tenant " + tenantDomain + ".", e);
        }
    }

    private FieldValue stringValue(Field field, String value) {

        return new FieldValue(field.getName(), field.getQualifier(), value, ValueType.STRING);
    }

    /**
     * The user, once identified. Before that, what is stored for the user cannot be known, which is not the same as
     * the user having nothing stored, so a condition on it is an error rather than a check of an empty value.
     */
    private static FlowUser identifiedUser(Field field, FlowExecutionContext context)
            throws RuleEvaluationDataProviderException {

        FlowUser user = context.getFlowUser();
        if (user == null || StringUtils.isBlank(user.getUserId())) {
            throw new RuleEvaluationDataProviderException("The condition on " + field.getName()
                    + " is evaluated before the user is identified in the flow, so its value is not known yet.");
        }
        return user;
    }
}
