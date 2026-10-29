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
import org.wso2.carbon.identity.flow.execution.engine.util.FlowInputResolver;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.utils.FlowInputUtils;
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
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.flow.mgt.rule.FlowRuleMetadataProvider.REQUEST_HEADERS;
import static org.wso2.carbon.identity.flow.mgt.rule.FlowRuleMetadataProvider.REQUEST_QUERY_PARAMETERS;
import static org.wso2.carbon.identity.flow.mgt.rule.FlowRuleMetadataProvider.VARIABLES;

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
    public static final String USER_ATTRIBUTES = "user.attributes";

    /**
     * What this run of the flow has gathered so far, whether the user entered it or an executor produced it.
     */
    public static final String USER_COLLECTED_ATTRIBUTES = "user.collectedAttributes";

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
     * @return The value, never null. A value that is missing, such as an attribute not collected, resolves to no value,
     * or to an empty list for a multi-valued field.
     * @throws RuleEvaluationDataProviderException If the value cannot be known at this point of the flow, or reading
     *                                             it fails. Neither is read as an empty value.
     */
    protected FieldValue resolve(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        switch (field.getName()) {
            case APPLICATION:
                return application(field, context);
            case USER_COLLECTED_ATTRIBUTES:
                return collectedAttribute(field, context);
            case USER_ATTRIBUTES:
                return persistedAttribute(field, context, tenantDomain);
            case USER_DOMAIN:
                return userStoreDomain(field, context);
            case USER_GROUPS:
                return userGroups(field, context, tenantDomain);
            case USER_ROLES:
                return userRoles(field, context, tenantDomain);
            case VARIABLES:
                return inputValue(field, FlowInputResolver.getVariableType(context, field.getQualifier()),
                        FlowInputResolver.getVariable(context, field.getQualifier()));
            case REQUEST_HEADERS:
                return inputValue(field, FlowInputResolver.getHeaderType(context, field.getQualifier()),
                        FlowInputResolver.getHeader(context, field.getQualifier()));
            case REQUEST_QUERY_PARAMETERS:
                return inputValue(field, FlowInputResolver.getQueryParamType(context, field.getQualifier()),
                        FlowInputResolver.getQueryParam(context, field.getQualifier()));
            default:
                throw new RuleEvaluationDataProviderException(
                        "No value can be read for condition field: " + field.getName() + ".");
        }
    }

    /**
     * A header, query parameter or variable, typed as the flow defines it. Numbers are compared as decimals, and the
     * entries of a number array as their text, as a list holds text alone.
     */
    private FieldValue inputValue(Field field, FlowInputDataType dataType, Object value)
            throws RuleEvaluationDataProviderException {

        if (dataType == null) {
            throw new RuleEvaluationDataProviderException("The flow does not define " + field.getQualifier()
                    + " of " + field.getName() + ".");
        }
        switch (dataType) {
            case INTEGER, DECIMAL:
                return new FieldValue(field.getName(), field.getQualifier(),
                        value == null ? null : ((Number) value).doubleValue());
            case BOOLEAN:
                return new FieldValue(field.getName(), field.getQualifier(), (Boolean) value);
            case STRING_ARRAY, NUMBER_ARRAY:
                List<String> entries = null;
                if (value != null) {
                    entries = new ArrayList<>();
                    for (Object entry : (List<?>) value) {
                        entries.add(entry instanceof Number number
                                ? FlowInputUtils.toText(number) : String.valueOf(entry));
                    }
                }
                return new FieldValue(field.getName(), field.getQualifier(), entries);
            default:
                return new FieldValue(field.getName(), field.getQualifier(), (String) value, ValueType.STRING);
        }
    }

    private FieldValue collectedAttribute(Field field, FlowExecutionContext context)
            throws RuleEvaluationDataProviderException {

        FlowUser user = context.getFlowUser();
        String rawValue = isUpdatedInFlow(user, field.getQualifier())
                ? (String) user.getClaim(field.getQualifier()) : null;
        return attributeValue(field, rawValue, user == null ? null : user.getUserStoreDomain(),
                context.getTenantDomain());
    }

    private boolean isUpdatedInFlow(FlowUser user, String claimUri) {

        return user != null && user.getUpdatedClaimUris().contains(claimUri);
    }

    private FieldValue application(Field field, FlowExecutionContext context) {

        String applicationId = context.getApplicationId();
        return new FieldValue(field.getName(), StringUtils.isNotBlank(applicationId) ? applicationId : null,
                ValueType.REFERENCE);
    }

    private FieldValue userStoreDomain(Field field, FlowExecutionContext context) {

        FlowUser user = context.getFlowUser();
        if (user != null && StringUtils.isNotBlank(user.getUserStoreDomain())) {
            return stringValue(field, user.getUserStoreDomain().toUpperCase());
        }
        return stringValue(field, null);
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

    private FieldValue persistedAttribute(Field field, FlowExecutionContext context, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        FlowUser user = identifiedUser(field, context);
        if (!isUpdatedInFlow(user, field.getQualifier())) {
            return attributeValue(field, (String) user.getClaim(field.getQualifier()), user.getUserStoreDomain(),
                    tenantDomain);
        }

        try {
            Map<String, String> claims = userStoreManager(tenantDomain).getUserClaimValuesWithID(
                    user.getUserId(), new String[]{field.getQualifier()}, null);
            return attributeValue(field, claims == null ? null : claims.get(field.getQualifier()),
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

    private FieldValue attributeValue(Field field, String rawValue, String userStoreDomain, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        Optional<LocalClaim> claim = localClaim(field.getQualifier(), tenantDomain);
        if (!isMultiValued(claim)) {
            if (StringUtils.isEmpty(rawValue)) {
                return stringValue(field, null);
            }
            return isNumeric(claim) ? numberValue(field, rawValue) : stringValue(field, rawValue);
        }

        if (StringUtils.isEmpty(rawValue)) {
            return new FieldValue(field.getName(), field.getQualifier(), (List<String>) null);
        }
        String separator = FrameworkUtils.getMultiAttributeSeparator(userStoreDomain);
        List<String> values = Arrays.stream(rawValue.split(Pattern.quote(separator)))
                .filter(StringUtils::isNotEmpty)
                .toList();
        return new FieldValue(field.getName(), field.getQualifier(), values);
    }

    private static boolean isMultiValued(Optional<LocalClaim> claim) {

        return claim.isPresent() && Boolean.parseBoolean(
                claim.get().getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
    }

    /**
     * Whether a claim holds numbers, so its value is compared as a number rather than as text.
     */
    private static boolean isNumeric(Optional<LocalClaim> claim) {

        String dataType = claim.map(localClaim -> localClaim.getClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY))
                .orElse(null);
        return ClaimConstants.ClaimDataType.INTEGER.name().equalsIgnoreCase(dataType)
                || ClaimConstants.ClaimDataType.DECIMAL.name().equalsIgnoreCase(dataType);
    }

    /**
     * A number claim's value as a number. A value that is not a number is no value, as it cannot be compared with one.
     */
    private static FieldValue numberValue(Field field, String rawValue) {

        try {
            return new FieldValue(field.getName(), field.getQualifier(), Double.valueOf(rawValue.trim()));
        } catch (NumberFormatException e) {
            return new FieldValue(field.getName(), field.getQualifier(), (String) null, ValueType.STRING);
        }
    }

    private Optional<LocalClaim> localClaim(String claimUri, String tenantDomain)
            throws RuleEvaluationDataProviderException {

        if (StringUtils.isBlank(claimUri)) {
            return Optional.empty();
        }
        ClaimMetadataManagementService claimService =
                FlowExecutionEngineDataHolder.getInstance().getClaimMetadataManagementService();
        if (claimService == null) {
            throw new RuleEvaluationDataProviderException(
                    "Claim metadata service is not available to read claim " + claimUri + ".");
        }
        try {
            return claimService.getLocalClaim(claimUri, tenantDomain);
        } catch (ClaimMetadataException e) {
            throw new RuleEvaluationDataProviderException("Could not read the metadata of claim " + claimUri
                    + " in tenant " + tenantDomain + ".", e);
        }
    }

    protected FieldValue stringValue(Field field, String value) {

        return new FieldValue(field.getName(), field.getQualifier(), value, ValueType.STRING);
    }

    private FlowUser identifiedUser(Field field, FlowExecutionContext context)
            throws RuleEvaluationDataProviderException {

        FlowUser user = context.getFlowUser();
        if (user == null || StringUtils.isBlank(user.getUserId())) {
            throw new RuleEvaluationDataProviderException("The condition on " + field.getName()
                    + " is evaluated before the user is identified in the flow, so its value is not known yet.");
        }
        return user;
    }
}
