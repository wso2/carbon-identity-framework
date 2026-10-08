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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Supplies the values a flow condition is evaluated against.
 * <p>
 * The providers are registered one per flow type, because the evaluation data manager keys them that
 * way. Each concrete provider says which flow type it serves and may answer differently; everything
 * they share lives here.
 */
public abstract class AbstractFlowRuleEvaluationDataProvider implements RuleEvaluationDataProvider {

    private static final Log LOG = LogFactory.getLog(AbstractFlowRuleEvaluationDataProvider.class);

    /**
     * What is stored for the user. Keyed by claim URI. Reachable only once the user exists, which in
     * a registration flow is not until onboarding.
     */
    public static final String USER_CLAIMS = "user.claims";

    /**
     * What this run of the flow has gathered so far, whether the user entered it or an executor
     * produced it. Keyed by claim URI.
     */
    public static final String USER_COLLECTED_CLAIMS = "user.collectedClaims";

    /**
     * Values executors put on the flow context. Keyed by property name, which nothing declares in
     * advance, so the qualifier is free text.
     */
    public static final String FLOW_PROPERTIES = "flow.properties";

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
     * <p>
     * Every requested field gets an answer, even when there is no value to give: evaluation treats a
     * missing entry as an error, whereas a present but empty one simply does not satisfy the
     * expression. A condition placed before the step that produces its value should send the flow
     * down the default branch, not fail it.
     *
     * @param field        Field to resolve, carrying its qualifier where it has one.
     * @param context      Flow execution context.
     * @param tenantDomain Tenant domain.
     * @return The value, never null.
     */
    protected FieldValue resolve(Field field, FlowExecutionContext context, String tenantDomain) {

        switch (field.getName()) {
            case USER_COLLECTED_CLAIMS:
                return collectedClaim(field, context);
            case USER_CLAIMS:
                return persistedClaim(field, context, tenantDomain);
            case FLOW_PROPERTIES:
                return flowProperty(field, context);
            default:
                LOG.debug("No value available for condition field: " + field.getName() + ".");
                return absent(field);
        }
    }

    private FieldValue collectedClaim(Field field, FlowExecutionContext context) {

        FlowUser user = context.getFlowUser();
        if (user == null || user.getClaims() == null) {
            return absent(field);
        }
        return claimValue(field, user.getClaims().get(field.getQualifier()), user.getUserStoreDomain(),
                context.getTenantDomain());
    }

    private FieldValue flowProperty(Field field, FlowExecutionContext context) {

        if (context.getProperties() == null) {
            return absent(field);
        }
        Object value = context.getProperties().get(field.getQualifier());
        return stringValue(field, value == null ? null : String.valueOf(value));
    }

    /**
     * Read a claim from the user store. Yields nothing until the user exists, which is why a
     * condition on stored claims placed before onboarding takes the default branch rather than
     * failing.
     */
    private FieldValue persistedClaim(Field field, FlowExecutionContext context, String tenantDomain) {

        FlowUser user = context.getFlowUser();
        if (user == null || StringUtils.isBlank(user.getUserId())) {
            LOG.debug("No user exists yet in the flow. Stored claims resolve to no value.");
            return absent(field);
        }

        try {
            AbstractUserStoreManager userStoreManager =
                    (AbstractUserStoreManager) FlowExecutionEngineDataHolder.getInstance().getRealmService()
                            .getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
            Map<String, String> claims = userStoreManager.getUserClaimValuesWithID(
                    user.getUserId(), new String[]{field.getQualifier()}, null);
            return claimValue(field, claims == null ? null : claims.get(field.getQualifier()),
                    user.getUserStoreDomain(), tenantDomain);
        } catch (UserStoreException e) {
            // Failing soft: a store that cannot answer should not take the whole flow down.
            LOG.warn("Could not read claim " + field.getQualifier() + " for the user in tenant " + tenantDomain
                    + ". The condition on it will not hold.", e);
            return absent(field);
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
     * A claim value, as a set when the claim holds more than one value.
     * <p>
     * A multi-valued claim is stored joined on the multi-attribute separator. Compared as one string,
     * {@code in} would test the joined form for membership and never match, so it is split here and
     * evaluated as a set. A missing value stays a missing scalar whatever the claim, so that no
     * operator -- {@code notIn} included -- holds on it.
     *
     * @param field           Field being resolved.
     * @param rawValue        Value as stored or collected.
     * @param userStoreDomain User store the separator is configured on, or null for the primary.
     * @param tenantDomain    Tenant domain.
     * @return The value.
     */
    private FieldValue claimValue(Field field, String rawValue, String userStoreDomain, String tenantDomain) {

        if (StringUtils.isEmpty(rawValue) || !isMultiValued(field.getQualifier(), tenantDomain)) {
            return stringValue(field, rawValue);
        }

        String separator = FrameworkUtils.getMultiAttributeSeparator(userStoreDomain);
        List<String> values = new ArrayList<>();
        for (String value : rawValue.split(Pattern.quote(separator))) {
            if (StringUtils.isNotEmpty(value)) {
                values.add(value);
            }
        }
        return new FieldValue(field.getName(), field.getQualifier(), values);
    }

    private boolean isMultiValued(String claimUri, String tenantDomain) {

        ClaimMetadataManagementService claimService =
                FlowExecutionEngineDataHolder.getInstance().getClaimMetadataManagementService();
        if (claimService == null || StringUtils.isBlank(claimUri)) {
            return false;
        }
        try {
            Optional<LocalClaim> claim = claimService.getLocalClaim(claimUri, tenantDomain);
            return claim.isPresent() && Boolean.parseBoolean(
                    claim.get().getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
        } catch (ClaimMetadataException e) {
            LOG.warn("Could not read the metadata of claim " + claimUri + " in tenant " + tenantDomain
                    + ". Its value is compared as a single value.", e);
            return false;
        }
    }

    private FieldValue stringValue(Field field, String value) {

        return new FieldValue(field.getName(), field.getQualifier(), value, ValueType.STRING);
    }

    private FieldValue absent(Field field) {

        return stringValue(field, null);
    }
}
