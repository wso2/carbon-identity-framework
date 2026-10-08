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

import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.model.ValueFieldOptions;
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Declares what a condition in an orchestration flow may be written against.
 * <p>
 * Claims are published as two qualified fields rather than one field per claim. A tenant can hold
 * hundreds of claims, and expanding them would put all of them in one dropdown and grow this
 * response with the claim count; a qualifier picks the claim instead. The two exist because a claim in
 * flight is not the claim on file -- see the field descriptions below.
 */
public class FlowRuleMetadataProvider implements RuleMetadataProvider {

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
     * Values executors put on the flow context. Nothing declares these in advance, so unlike a claim
     * the qualifier cannot be picked from a list and is typed instead.
     */
    public static final String FLOW_PROPERTIES = "flow.properties";

    /**
     * Fields that name a family of values and so need a qualifier to pick one.
     */
    private static final Set<String> QUALIFIED_FIELDS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(USER_CLAIMS, USER_COLLECTED_CLAIMS, FLOW_PROPERTIES)));

    /**
     * The fields a condition's value may be read from instead of being given: every flow field can be compared
     * with any other, such as a claim collected in this flow with the one already stored for the user.
     */
    private static final ValueFieldOptions COMPARABLE_FIELDS = new ValueFieldOptions(
            Arrays.asList(USER_CLAIMS, USER_COLLECTED_CLAIMS, FLOW_PROPERTIES));

    private static final String CLAIMS_ENDPOINT = "/claim-dialects/local/claims?exclude-hidden-claims=true";
    private static final String CLAIM_URI_ATTRIBUTE = "claimURI";
    private static final String DISPLAY_NAME_ATTRIBUTE = "displayName";

    private static final Set<FlowType> SUPPORTED_FLOW_TYPES = Collections.unmodifiableSet(
            EnumSet.of(FlowType.REGISTRATION, FlowType.PASSWORD_RECOVERY, FlowType.INVITED_USER_REGISTRATION));

    /**
     * Everything a claim could ever accept. What is actually legal depends on the claim the author
     * picks, which this entry cannot know -- that is narrowed when the qualifier is chosen and enforced
     * when the flow is saved.
     */
    private static final List<Operator> CLAIM_OPERATORS = Collections.unmodifiableList(Arrays.asList(
            new Operator("equals", "equals"),
            new Operator("notEquals", "not equals"),
            new Operator("contains", "contains"),
            new Operator("notContains", "not contains"),
            new Operator("startsWith", "starts with"),
            new Operator("endsWith", "ends with"),
            new Operator("greaterThan", "greater than"),
            new Operator("lessThan", "less than"),
            new Operator("in", "in"),
            new Operator("notIn", "not in")));

    private static final List<Operator> PROPERTY_OPERATORS = Collections.unmodifiableList(Arrays.asList(
            new Operator("equals", "equals"),
            new Operator("notEquals", "not equals"),
            new Operator("contains", "contains"),
            new Operator("notContains", "not contains"),
            new Operator("startsWith", "starts with"),
            new Operator("endsWith", "ends with")));

    @Override
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        if (!SUPPORTED_FLOW_TYPES.contains(flowType)) {
            return Collections.emptyList();
        }

        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(claimField(USER_CLAIMS, "User's Claim"));
        fields.add(claimField(USER_COLLECTED_CLAIMS, "Claim Collected in This Flow"));
        fields.add(flowPropertyField());
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

    /**
     * A claim field. The qualifier is picked from the claim dialect, so an author chooses a claim rather
     * than typing a URI; the value itself is free text, because a claim's value is not enumerable.
     */
    private FieldDefinition claimField(String name, String displayName) {

        List<Link> links = Collections.singletonList(new Link(CLAIMS_ENDPOINT, "GET", "values"));
        Value qualifier = new OptionsReferenceValue.Builder()
                .valueReferenceAttribute(CLAIM_URI_ATTRIBUTE)
                .valueDisplayAttribute(DISPLAY_NAME_ATTRIBUTE)
                .valueType(Value.ValueType.REFERENCE)
                .links(links)
                .build();

        return new FieldDefinition(new Field(name, displayName, qualifier), CLAIM_OPERATORS,
                new InputValue(Value.ValueType.STRING), COMPARABLE_FIELDS);
    }

    private FieldDefinition flowPropertyField() {

        return new FieldDefinition(new Field(FLOW_PROPERTIES, "Flow Property", new InputValue(Value.ValueType.STRING)),
                PROPERTY_OPERATORS, new InputValue(Value.ValueType.STRING), COMPARABLE_FIELDS);
    }
}
