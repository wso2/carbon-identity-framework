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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.internal.component.RuleManagementComponentServiceHolder;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.expectThrows;

/**
 * Tests that a condition is checked against the claim it actually names.
 * <p>
 * The rule builder can only check an operator against what the <em>field</em> declares, and the claim
 * field declares everything any claim could accept. These cover the second pass, where the qualifier has
 * been resolved to a claim and its cardinality and type decide what is really allowed.
 */
public class FlowRuleValidatorTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String COUNTRY = "http://wso2.org/claims/country";
    private static final String EMAILS = "http://wso2.org/claims/emailAddresses";
    private static final String AGE = "http://wso2.org/claims/age";

    private MockedStatic<RuleMetadataConfigFactory> configFactoryMock;
    private MockedStatic<IdentityConfigParser> identityConfigParserMock;

    @BeforeClass
    public void setUpClass() throws Exception {

        String operators = Objects.requireNonNull(
                getClass().getClassLoader().getResource("configs/valid-operators.json")).getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(operators));
        configFactoryMock = mockStatic(RuleMetadataConfigFactory.class);
        configFactoryMock.when(RuleMetadataConfigFactory::getOperatorConfig).thenReturn(operatorConfig);

        // The rule builder reads its AND limit for the flow from identity.xml; none is set, so the default applies.
        IdentityConfigParser identityConfigParser = mock(IdentityConfigParser.class);
        when(identityConfigParser.getConfiguration()).thenReturn(new HashMap<>());
        identityConfigParserMock = mockStatic(IdentityConfigParser.class);
        identityConfigParserMock.when(IdentityConfigParser::getInstance).thenReturn(identityConfigParser);

        /*
         * The metadata the flow really publishes, so the test validates against the shipped contract.
         * Built before stubbing begins: constructing a field definition reads the mocked operator
         * config, and Mockito will not tolerate that happening inside an unfinished when().
         */
        List<org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition> publishedFields =
                new FlowRuleMetadataProvider().getExpressionMeta(
                        org.wso2.carbon.identity.rule.metadata.api.model.FlowType.REGISTRATION, TENANT_DOMAIN);

        RuleMetadataService ruleMetadataService = mock(RuleMetadataService.class);
        when(ruleMetadataService.getExpressionMeta(any(), anyString())).thenReturn(publishedFields);
        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);

        ClaimMetadataManagementService claimService = mock(ClaimMetadataManagementService.class);
        when(claimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Arrays.asList(
                claim(COUNTRY, false, "STRING"),
                claim(EMAILS, true, "STRING"),
                claim(AGE, false, "INTEGER")));
        FlowMgtServiceDataHolder.getInstance().setClaimMetadataManagementService(claimService);
    }

    @AfterClass
    public void tearDownClass() {

        configFactoryMock.close();
        identityConfigParserMock.close();
    }

    @Test
    public void testSingleValuedClaimWithATextOperatorIsAccepted() throws Exception {

        FlowRuleValidator.validate(flowWith(expression(COUNTRY, "equals", scalar("LK"))), TENANT_DOMAIN);
    }

    /**
     * The bug this whole check exists for. A multi-valued claim reaches the engine as its values
     * joined together, so contains matches a value that is merely a substring of a neighbouring one.
     */
    @Test
    public void testMultiValuedClaimRejectsSubstringOperators() {

        FlowDTO flow = flowWith(expression(EMAILS, "contains", scalar("b@x.com")));

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
    }

    @Test
    public void testMultiValuedClaimAcceptsMembership() throws Exception {

        FlowRuleValidator.validate(
                flowWith(expression(EMAILS, "in", new Value(Arrays.asList("a@x.com", "b@x.com")))),
                TENANT_DOMAIN);
    }

    @Test
    public void testMembershipWithoutAListIsRejected() {

        FlowDTO flow = flowWith(expression(EMAILS, "in", scalar("a@x.com")));

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
    }

    @Test
    public void testListValueUnderANonMembershipOperatorIsRejected() {

        FlowDTO flow = flowWith(expression(COUNTRY, "equals", new Value(Arrays.asList("LK", "IN"))));

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
    }

    @Test
    public void testNumericClaimRejectsSubstringOperators() {

        FlowDTO flow = flowWith(expression(AGE, "contains", scalar("3")));

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
    }

    @Test
    public void testNumericClaimAcceptsOrdering() throws Exception {

        FlowRuleValidator.validate(flowWith(expression(AGE, "greaterThan", scalar("18"))), TENANT_DOMAIN);
    }

    /**
     * A typo in the claim would otherwise save cleanly and produce a branch that never fires.
     */
    @Test
    public void testUnknownClaimIsRejected() {

        FlowDTO flow = flowWith(expression("http://wso2.org/claims/countryy", "equals", scalar("LK")));

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
    }

    @Test
    public void testClaimConditionWithoutAKeyIsRejected() {

        Expression noKey = new Expression.Builder()
                .field(FlowRuleMetadataProvider.USER_CLAIMS)
                .operator("equals")
                .value(scalar("LK"))
                .build();

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flowWith(noKey), TENANT_DOMAIN));
    }

    @Test
    public void testFieldTheFlowDoesNotPublishIsRejected() {

        Expression unknownField = new Expression.Builder()
                .field("user.favouriteColour")
                .operator("equals")
                .value(scalar("blue"))
                .build();

        assertThrows(FlowMgtClientException.class,
                () -> FlowRuleValidator.validate(flowWith(unknownField), TENANT_DOMAIN));
    }

    /**
     * A flow with no decisions has nothing to check, and must not be made to look up claims.
     */
    @Test
    public void testFlowWithoutDecisionsIsAccepted() throws Exception {

        FlowDTO flow = new FlowDTO();
        flow.setFlowType(Constants.FlowTypes.REGISTRATION.getType());
        StepDTO view = new StepDTO.Builder().id("step_view").type(Constants.StepTypes.VIEW)
                .data(new DataDTO.Builder().build()).build();
        flow.setSteps(Collections.singletonList(view));

        FlowRuleValidator.validate(flow, TENANT_DOMAIN);
    }

    @Test
    public void testFlowPropertyWithoutAKeyIsRejected() {

        Expression noKey = new Expression.Builder()
                .field(FlowRuleMetadataProvider.FLOW_PROPERTIES)
                .operator("equals")
                .value(scalar("high"))
                .build();

        FlowMgtClientException e = expectThrows(FlowMgtClientException.class,
                () -> FlowRuleValidator.validate(flowWith(noKey), TENANT_DOMAIN));
        assertEquals(e.getErrorCode(), code(Constants.ErrorMessages.ERROR_CODE_QUALIFIER_REQUIRED));
    }

    @Test
    public void testFlowPropertyWithAKeyIsAccepted() throws Exception {

        Expression property = new Expression.Builder()
                .field(FlowRuleMetadataProvider.FLOW_PROPERTIES)
                .fieldQualifier("riskLevel")
                .operator("equals")
                .value(scalar("high"))
                .build();

        FlowRuleValidator.validate(flowWith(property), TENANT_DOMAIN);
    }

    /**
     * The list and operator have to agree on every field, not only on claims.
     */
    @Test
    public void testListValueOnAFlowPropertyIsRejected() {

        Expression property = new Expression.Builder()
                .field(FlowRuleMetadataProvider.FLOW_PROPERTIES)
                .fieldQualifier("riskLevel")
                .operator("equals")
                .value(new Value(Arrays.asList("high", "medium")))
                .build();

        assertThrows(FlowMgtClientException.class,
                () -> FlowRuleValidator.validate(flowWith(property), TENANT_DOMAIN));
    }

    /**
     * A claim store that cannot answer is not the author's mistake, so it must not be reported as
     * an unknown claim.
     */
    @Test
    public void testUnreadableClaimsAreAServerError() throws Exception {

        ClaimMetadataManagementService original =
                FlowMgtServiceDataHolder.getInstance().getClaimMetadataManagementService();
        ClaimMetadataManagementService failing = mock(ClaimMetadataManagementService.class);
        when(failing.getLocalClaims(anyString())).thenThrow(new ClaimMetadataException("unavailable"));
        FlowMgtServiceDataHolder.getInstance().setClaimMetadataManagementService(failing);
        try {
            FlowDTO flow = flowWith(expression(COUNTRY, "equals", scalar("LK")));
            FlowMgtServerException e = expectThrows(FlowMgtServerException.class,
                    () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
            assertEquals(e.getErrorCode(), code(Constants.ErrorMessages.ERROR_CODE_GET_CLAIM_METADATA));
        } finally {
            FlowMgtServiceDataHolder.getInstance().setClaimMetadataManagementService(original);
        }
    }

    @Test
    public void testClaimsAreReadOncePerSave() throws Exception {

        ClaimMetadataManagementService original =
                FlowMgtServiceDataHolder.getInstance().getClaimMetadataManagementService();
        ClaimMetadataManagementService counting = mock(ClaimMetadataManagementService.class);
        when(counting.getLocalClaims(TENANT_DOMAIN)).thenReturn(Arrays.asList(
                claim(COUNTRY, false, "STRING"), claim(AGE, false, "INTEGER")));
        FlowMgtServiceDataHolder.getInstance().setClaimMetadataManagementService(counting);
        try {
            FlowRuleValidator.validate(flowWith(
                    expression(COUNTRY, "equals", scalar("LK")),
                    expression(AGE, "greaterThan", scalar("18"))), TENANT_DOMAIN);
            verify(counting, times(1)).getLocalClaims(TENANT_DOMAIN);
        } finally {
            FlowMgtServiceDataHolder.getInstance().setClaimMetadataManagementService(original);
        }
    }

    /**
     * A collected claim compared with the one already stored for the user: the case field references exist for.
     */
    @Test
    public void testCollectedClaimComparedWithTheStoredOneIsAccepted() throws Exception {

        Expression compared = new Expression.Builder()
                .field(FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS).fieldQualifier(COUNTRY).operator("notEquals")
                .value(new Value(new FieldReference(FlowRuleMetadataProvider.USER_CLAIMS, COUNTRY)))
                .build();

        FlowRuleValidator.validate(flowWith(compared), TENANT_DOMAIN);
    }

    @Test
    public void testFieldReferenceToAnUnknownClaimIsRejected() {

        FlowDTO flow = flowWith(expression(COUNTRY, "equals",
                new Value(new FieldReference(FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS,
                        "http://wso2.org/claims/notAClaim"))));

        FlowMgtClientException e = expectThrows(FlowMgtClientException.class,
                () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
        assertEquals(e.getErrorCode(), code(Constants.ErrorMessages.ERROR_CODE_UNKNOWN_CLAIM));
    }

    @Test
    public void testFieldReferenceWithoutAClaimKeyIsRejected() {

        FlowDTO flow = flowWith(expression(COUNTRY, "equals",
                new Value(new FieldReference(FlowRuleMetadataProvider.USER_CLAIMS, null))));

        assertThrows(FlowMgtClientException.class, () -> FlowRuleValidator.validate(flow, TENANT_DOMAIN));
    }

    /**
     * Membership against a field is allowed: whether that field holds several values is only known at evaluation.
     */
    @Test
    public void testMembershipAgainstAFieldIsAccepted() throws Exception {

        FlowRuleValidator.validate(flowWith(expression(EMAILS, "in",
                new Value(new FieldReference(FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS, EMAILS)))),
                TENANT_DOMAIN);
    }

    private static String code(Constants.ErrorMessages error) {

        return error.getCode();
    }

    private static LocalClaim claim(String uri, boolean multiValued, String dataType) {

        Map<String, String> properties = new HashMap<>();
        properties.put(ClaimConstants.MULTI_VALUED_PROPERTY, String.valueOf(multiValued));
        properties.put(ClaimConstants.DATA_TYPE_PROPERTY, dataType);
        return new LocalClaim(uri, Collections.emptyList(), properties);
    }

    private static Value scalar(String value) {

        return new Value(Value.Type.STRING, value);
    }

    private static Expression expression(String claimUri, String operator, Value value) {

        return new Expression.Builder()
                .field(FlowRuleMetadataProvider.USER_CLAIMS)
                .fieldQualifier(claimUri)
                .operator(operator)
                .value(value)
                .build();
    }

    private static FlowDTO flowWith(Expression... expressions) {

        ANDCombinedRule.Builder andRule = new ANDCombinedRule.Builder();
        for (Expression expression : expressions) {
            andRule.addExpression(expression);
        }
        ORCombinedRule rule = new ORCombinedRule.Builder().addRule(andRule.build()).build();

        List<BranchDTO> branches = new ArrayList<>();
        branches.add(new BranchDTO.Builder().id("br_guarded").name("Guarded")
                .nextId("END").rule(rule).build());
        branches.add(new BranchDTO.Builder().id("br_default").name("Everyone else")
                .nextId("END").build());

        ActionDTO action = new ActionDTO.Builder()
                .type(Constants.ActionTypes.RULE_EVALUATOR)
                .branches(branches)
                .build();

        StepDTO step = new StepDTO.Builder()
                .id("step_route")
                .type(Constants.StepTypes.RULE_EVALUATION)
                .data(new DataDTO.Builder().action(action).build())
                .build();

        FlowDTO flow = new FlowDTO();
        flow.setFlowType(Constants.FlowTypes.REGISTRATION.getType());
        flow.setSteps(Collections.singletonList(step));
        return flow;
    }
}
