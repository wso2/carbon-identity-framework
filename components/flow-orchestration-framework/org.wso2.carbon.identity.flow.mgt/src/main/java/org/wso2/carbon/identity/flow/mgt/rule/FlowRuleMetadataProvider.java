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

package org.wso2.carbon.identity.flow.mgt.rule;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.model.ValueFieldOptions;
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Declares what a condition in an orchestration flow may be written against.
 * <p>
 * Claims are published as two qualified fields rather than one field per claim. A tenant can hold
 * hundreds of claims, and expanding them would put all of them in one dropdown and grow this
 * response with the claim count; a qualifier picks the claim instead. The two exist because a claim in
 * flight is not the claim on file -- see the field descriptions below.
 * <p>
 * Each flow type offers only the fields that mean something in it: a registration has no stored user to
 * read claims, groups or roles from, for instance.
 */
public class FlowRuleMetadataProvider implements RuleMetadataProvider {

    /**
     * The application the flow runs for.
     */
    public static final String APPLICATION = "application";

    /**
     * What is stored for the user, read from the user store. Only meaningful once the user exists,
     * which in a registration flow is not until onboarding.
     */
    public static final String USER_CLAIMS = "user.claims";

    /**
     * What this run of the flow has gathered, whether the user entered it or an executor produced it.
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
     * The roles the user is assigned, by role id.
     */
    public static final String USER_ROLES = "user.roles";

    /**
     * Fields that name a family of values and so need a qualifier to pick one.
     */
    private static final Set<String> QUALIFIED_FIELDS = Collections.unmodifiableSet(new LinkedHashSet<>(
            Arrays.asList(USER_CLAIMS, USER_COLLECTED_CLAIMS)));

    /**
     * The fields a claim condition's value may be read from instead of being given, such as a claim collected
     * in this flow compared with the one already stored for the user. Narrowed to the fields a flow offers.
     */
    private static final List<String> COMPARABLE_FIELDS = Collections.unmodifiableList(
            Arrays.asList(USER_CLAIMS, USER_COLLECTED_CLAIMS));

    /**
     * The fields each flow type offers, in the order they are published.
     */
    private static final Map<FlowType, List<String>> FIELDS_BY_FLOW_TYPE;

    static {
        Map<FlowType, List<String>> fields = new EnumMap<>(FlowType.class);
        fields.put(FlowType.REGISTRATION, Collections.unmodifiableList(Arrays.asList(
                APPLICATION, USER_COLLECTED_CLAIMS)));
        fields.put(FlowType.PASSWORD_RECOVERY, Collections.unmodifiableList(Arrays.asList(
                APPLICATION, USER_CLAIMS, USER_COLLECTED_CLAIMS, USER_DOMAIN, USER_GROUPS, USER_ROLES)));
        fields.put(FlowType.INVITED_USER_REGISTRATION, Collections.unmodifiableList(Arrays.asList(
                USER_CLAIMS, USER_COLLECTED_CLAIMS, USER_DOMAIN)));
        FIELDS_BY_FLOW_TYPE = Collections.unmodifiableMap(fields);
    }

    private static final String GET = "GET";
    private static final String VALUES = "values";
    private static final String FILTER = "filter";
    private static final String ID_ATTRIBUTE = "id";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String DISPLAY_NAME_ATTRIBUTE = "displayName";
    private static final String CLAIM_URI_ATTRIBUTE = "claimURI";

    private static final String CLAIMS_ENDPOINT = "/claim-dialects/local/claims?exclude-hidden-claims=true";
    private static final String APPLICATIONS_ENDPOINT = "/applications?excludeSystemPortals=true&offset=0&limit=10";
    private static final String APPLICATIONS_FILTER_ENDPOINT =
            "/applications?excludeSystemPortals=true&filter=&limit=10";
    private static final String GROUPS_ENDPOINT = "/scim2/Groups?offset=0&count=10";
    private static final String GROUPS_FILTER_ENDPOINT = "/scim2/Groups?filter=&count=10";
    private static final String ROLES_ENDPOINT = "/scim2/v2/Roles?offset=0&count=10";
    private static final String ROLES_FILTER_ENDPOINT = "/scim2/v2/Roles?filter=&count=10";

    private static final Operator EQUALS = new Operator("equals", "equals");
    private static final Operator NOT_EQUALS = new Operator("notEquals", "not equals");
    private static final Operator IN = new Operator("in", "in");
    private static final Operator NOT_IN = new Operator("notIn", "not in");

    /**
     * Everything a claim could ever accept. What is actually legal depends on the claim the author
     * picks, which this entry cannot know -- that is narrowed when the qualifier is chosen and enforced
     * when the flow is saved.
     */
    private static final List<Operator> CLAIM_OPERATORS = Collections.unmodifiableList(Arrays.asList(
            EQUALS,
            NOT_EQUALS,
            new Operator("contains", "contains"),
            new Operator("notContains", "not contains"),
            new Operator("startsWith", "starts with"),
            new Operator("endsWith", "ends with"),
            new Operator("greaterThan", "greater than"),
            new Operator("lessThan", "less than"),
            IN,
            NOT_IN));

    private static final List<Operator> EQUALITY_OPERATORS = Collections.unmodifiableList(
            Arrays.asList(EQUALS, NOT_EQUALS));

    private static final List<Operator> MEMBERSHIP_OPERATORS = Collections.unmodifiableList(
            Arrays.asList(IN, NOT_IN));

    @Override
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        List<String> fieldNames = FIELDS_BY_FLOW_TYPE.get(flowType);
        if (fieldNames == null) {
            return Collections.emptyList();
        }

        List<FieldDefinition> fields = new ArrayList<>();
        for (String fieldName : fieldNames) {
            fields.add(fieldDefinition(fieldName, fieldNames, tenantDomain));
        }
        return fields;
    }

    /**
     * Whether a field is published with a qualifier, and so needs one on every condition written against it.
     *
     * @param field Field name.
     * @return true when the field is qualified.
     */
    public static boolean isQualified(String field) {

        return QUALIFIED_FIELDS.contains(field);
    }

    private FieldDefinition fieldDefinition(String fieldName, List<String> flowFields, String tenantDomain)
            throws RuleMetadataException {

        switch (fieldName) {
            case APPLICATION:
                return new FieldDefinition(new Field(APPLICATION, "Application"), EQUALITY_OPERATORS,
                        referenceValue(NAME_ATTRIBUTE, APPLICATIONS_ENDPOINT, APPLICATIONS_FILTER_ENDPOINT));
            case USER_CLAIMS:
                return claimField(USER_CLAIMS, "User's claim", flowFields);
            case USER_COLLECTED_CLAIMS:
                return claimField(USER_COLLECTED_CLAIMS, "User's claim collected in the flow", flowFields);
            case USER_DOMAIN:
                return new FieldDefinition(new Field(USER_DOMAIN, "User's user store domain"), EQUALITY_OPERATORS,
                        new OptionsInputValue(Value.ValueType.STRING, userStoreDomains(tenantDomain)));
            case USER_GROUPS:
                return new FieldDefinition(new Field(USER_GROUPS, "User's assigned groups"), MEMBERSHIP_OPERATORS,
                        referenceValue(DISPLAY_NAME_ATTRIBUTE, GROUPS_ENDPOINT, GROUPS_FILTER_ENDPOINT));
            case USER_ROLES:
                return new FieldDefinition(new Field(USER_ROLES, "User's assigned roles"), MEMBERSHIP_OPERATORS,
                        referenceValue(DISPLAY_NAME_ATTRIBUTE, ROLES_ENDPOINT, ROLES_FILTER_ENDPOINT));
            default:
                throw new IllegalStateException("No definition for flow condition field: " + fieldName);
        }
    }

    /**
     * A claim field. The qualifier is picked from the claim dialect, so an author chooses a claim rather
     * than typing a URI; the value itself is free text, because a claim's value is not enumerable. Its value
     * may also be read from the other claim fields the flow offers.
     */
    private FieldDefinition claimField(String name, String displayName, List<String> flowFields) {

        Value qualifier = new OptionsReferenceValue.Builder()
                .valueReferenceAttribute(CLAIM_URI_ATTRIBUTE)
                .valueDisplayAttribute(DISPLAY_NAME_ATTRIBUTE)
                .valueType(Value.ValueType.REFERENCE)
                .links(Collections.singletonList(new Link(CLAIMS_ENDPOINT, GET, VALUES)))
                .build();

        List<String> comparable = new ArrayList<>(COMPARABLE_FIELDS);
        comparable.retainAll(flowFields);
        return new FieldDefinition(new Field(name, displayName, qualifier), CLAIM_OPERATORS,
                new InputValue(Value.ValueType.STRING), new ValueFieldOptions(comparable));
    }

    /**
     * A value picked by id from a listing, with a second link to filter that listing by name.
     */
    private static Value referenceValue(String displayAttribute, String valuesHref, String filterHref) {

        return new OptionsReferenceValue.Builder()
                .valueReferenceAttribute(ID_ATTRIBUTE)
                .valueDisplayAttribute(displayAttribute)
                .valueType(Value.ValueType.REFERENCE)
                .links(Arrays.asList(new Link(valuesHref, GET, VALUES), new Link(filterHref, GET, FILTER)))
                .build();
    }

    /**
     * The user stores of the tenant, offered as the values a domain condition may name.
     * <p>
     * The primary store is named as the deployment names it -- {@code PRIMARY} by default, {@code DEFAULT} on
     * Asgardeo -- followed by every enabled secondary store.
     *
     * @param tenantDomain Tenant domain.
     * @return The user store domains.
     * @throws RuleMetadataException If the user stores cannot be read.
     */
    private static List<OptionsValue> userStoreDomains(String tenantDomain) throws RuleMetadataException {

        RealmService realmService = FlowMgtServiceDataHolder.getInstance().getRealmService();
        if (realmService == null) {
            throw new RuleMetadataException("FLOW_RULE_METADATA_50001", "Realm service is not available.",
                    "The user stores of tenant " + tenantDomain + " cannot be read to offer as values.");
        }

        Set<String> domains = new LinkedHashSet<>();
        try {
            UserStoreManager primary = (UserStoreManager) realmService
                    .getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
            String primaryDomain = primary.getRealmConfiguration()
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            domains.add(StringUtils.isNotBlank(primaryDomain)
                    ? primaryDomain.toUpperCase() : UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
            for (UserStoreManager store = primary.getSecondaryUserStoreManager(); store != null;
                 store = store.getSecondaryUserStoreManager()) {
                String domain = store.getRealmConfiguration()
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                boolean disabled = Boolean.parseBoolean(store.getRealmConfiguration()
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED));
                if (StringUtils.isNotBlank(domain) && !disabled) {
                    domains.add(domain.toUpperCase());
                }
            }
        } catch (UserStoreException e) {
            throw new RuleMetadataException("FLOW_RULE_METADATA_50002", "Error while reading the user stores.",
                    "The user stores of tenant " + tenantDomain + " could not be read.", e);
        }

        List<OptionsValue> values = new ArrayList<>();
        for (String domain : domains) {
            values.add(new OptionsValue(domain, domain));
        }
        return values;
    }
}
