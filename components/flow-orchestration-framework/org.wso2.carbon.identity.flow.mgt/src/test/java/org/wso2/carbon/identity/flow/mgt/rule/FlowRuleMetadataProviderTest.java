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

package org.wso2.carbon.identity.flow.mgt.rule;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests what a flow publishes for its conditions to be written against.
 */
public class FlowRuleMetadataProviderTest {

    private static final String TENANT_DOMAIN = "carbon.super";

    private final FlowRuleMetadataProvider provider = new FlowRuleMetadataProvider();
    private MockedStatic<RuleMetadataConfigFactory> configFactoryMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMock;

    @BeforeClass
    public void setUpClass() throws Exception {

        String operators = Objects.requireNonNull(
                getClass().getClassLoader().getResource("configs/valid-operators.json")).getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(operators));
        configFactoryMock = mockStatic(RuleMetadataConfigFactory.class);
        configFactoryMock.when(RuleMetadataConfigFactory::getOperatorConfig).thenReturn(operatorConfig);
        identityTenantUtilMock = mockStatic(IdentityTenantUtil.class);
        identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(-1234);
        FlowMgtServiceDataHolder.getInstance().setRealmService(
                UserStoreMocks.realmWithUserStores("PRIMARY", new String[]{"LDAP"}, "ARCHIVED"));
    }

    @AfterClass
    public void tearDownClass() {

        configFactoryMock.close();
        identityTenantUtilMock.close();
        FlowMgtServiceDataHolder.getInstance().setRealmService(null);
    }

    @Test
    public void testEachFlowPublishesItsOwnFields() throws Exception {

        assertEquals(fieldNamesOf(FlowType.REGISTRATION), Arrays.asList(
                FlowRuleMetadataProvider.APPLICATION, FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS));
        assertEquals(fieldNamesOf(FlowType.PASSWORD_RECOVERY), Arrays.asList(
                FlowRuleMetadataProvider.APPLICATION, FlowRuleMetadataProvider.USER_CLAIMS,
                FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS, FlowRuleMetadataProvider.USER_DOMAIN,
                FlowRuleMetadataProvider.USER_GROUPS, FlowRuleMetadataProvider.USER_ROLES));
        assertEquals(fieldNamesOf(FlowType.INVITED_USER_REGISTRATION), Arrays.asList(
                FlowRuleMetadataProvider.USER_CLAIMS, FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS,
                FlowRuleMetadataProvider.USER_DOMAIN));
    }

    @Test
    public void testOtherFlowsPublishNothing() throws Exception {

        assertTrue(provider.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, TENANT_DOMAIN).isEmpty());
    }

    @Test
    public void testApplicationIsPickedById() throws Exception {

        FieldDefinition field = fieldsOf(FlowType.REGISTRATION).get(FlowRuleMetadataProvider.APPLICATION);

        assertEquals(field.getField().getDisplayName(), "Application");
        assertNull(field.getField().getQualifier());
        assertEquals(operatorNames(field), Arrays.asList("equals", "notEquals"));
        assertReferenceValue(field, "name", "/applications?excludeSystemPortals=true&offset=0&limit=10",
                "/applications?excludeSystemPortals=true&filter=&limit=10");
    }

    /**
     * The claim is picked from the claims endpoint, by URI, so the console can offer a list rather than a
     * text box.
     */
    @Test
    public void testClaimFieldsArePickedFromTheClaimList() throws Exception {

        for (String name : new String[]{FlowRuleMetadataProvider.USER_CLAIMS,
                FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS}) {
            FieldDefinition field = fieldsOf(FlowType.PASSWORD_RECOVERY).get(name);
            assertTrue(field.getField().getQualifier() instanceof OptionsReferenceValue,
                    name + " should be qualified by a list.");
            OptionsReferenceValue qualifier = (OptionsReferenceValue) field.getField().getQualifier();
            assertEquals(qualifier.getValueReferenceAttribute(), "claimURI");
            assertEquals(qualifier.getValueDisplayAttribute(), "displayName");
            assertEquals(qualifier.getLinks().size(), 1);
            assertEquals(qualifier.getLinks().get(0).getRel(), "values");
            assertEquals(qualifier.getLinks().get(0).getHref(),
                    "/claim-dialects/local/claims?exclude-hidden-claims=true");
            assertTrue(operatorNames(field).contains("in"));
            assertTrue(operatorNames(field).contains("greaterThan"));
        }
    }

    /**
     * A claim may be compared with the other claim fields of the same flow, and only those: a registration has
     * no stored user to compare with.
     */
    @Test
    public void testClaimValuesMayBeReadFromTheClaimFieldsOfTheFlow() throws Exception {

        assertEquals(fieldsOf(FlowType.PASSWORD_RECOVERY).get(FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS)
                        .getValueFieldOptions().getNames(),
                Arrays.asList(FlowRuleMetadataProvider.USER_CLAIMS, FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS));
        assertEquals(fieldsOf(FlowType.REGISTRATION).get(FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS)
                        .getValueFieldOptions().getNames(),
                Collections.singletonList(FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS));
    }

    /**
     * The domains offered are the tenant's own user stores: the primary, then each enabled secondary.
     */
    @Test
    public void testUserDomainOffersTheEnabledUserStores() throws Exception {

        FieldDefinition field = fieldsOf(FlowType.PASSWORD_RECOVERY).get(FlowRuleMetadataProvider.USER_DOMAIN);

        assertEquals(operatorNames(field), Arrays.asList("equals", "notEquals"));
        assertTrue(field.getValue() instanceof OptionsInputValue);
        assertEquals(((OptionsInputValue) field.getValue()).getValues().stream().map(OptionsValue::getName)
                .collect(Collectors.toList()), Arrays.asList("PRIMARY", "LDAP"));
    }

    /**
     * Asgardeo names its primary store DEFAULT, and the domain offered follows that name.
     */
    @Test
    public void testUserDomainNamesThePrimaryStoreAsTheDeploymentDoes() throws Exception {

        FlowMgtServiceDataHolder.getInstance().setRealmService(
                UserStoreMocks.realmWithUserStores("default", new String[0], null));
        try {
            FieldDefinition field =
                    fieldsOf(FlowType.INVITED_USER_REGISTRATION).get(FlowRuleMetadataProvider.USER_DOMAIN);
            assertEquals(((OptionsInputValue) field.getValue()).getValues().stream().map(OptionsValue::getName)
                    .collect(Collectors.toList()), Collections.singletonList("DEFAULT"));
        } finally {
            FlowMgtServiceDataHolder.getInstance().setRealmService(
                    UserStoreMocks.realmWithUserStores("PRIMARY", new String[]{"LDAP"}, "ARCHIVED"));
        }
    }

    @Test
    public void testGroupsAndRolesArePickedByIdAndTakeMembershipOperators() throws Exception {

        Map<String, FieldDefinition> fields = fieldsOf(FlowType.PASSWORD_RECOVERY);

        FieldDefinition groups = fields.get(FlowRuleMetadataProvider.USER_GROUPS);
        assertEquals(operatorNames(groups), Arrays.asList("in", "notIn"));
        assertReferenceValue(groups, "displayName", "/scim2/Groups?offset=0&count=10",
                "/scim2/Groups?filter=&count=10");

        FieldDefinition roles = fields.get(FlowRuleMetadataProvider.USER_ROLES);
        assertEquals(operatorNames(roles), Arrays.asList("in", "notIn"));
        assertReferenceValue(roles, "displayName", "/scim2/v2/Roles?offset=0&count=10",
                "/scim2/v2/Roles?filter=&count=10");
    }

    @Test
    public void testOnlyTheClaimFieldsAreQualified() throws Exception {

        for (FieldDefinition field : provider.getExpressionMeta(FlowType.PASSWORD_RECOVERY, TENANT_DOMAIN)) {
            String name = field.getField().getName();
            assertEquals(FlowRuleMetadataProvider.isQualified(name), field.getField().getQualifier() != null,
                    name + " must be treated as qualified exactly when it is published with a qualifier.");
        }
        assertFalse(FlowRuleMetadataProvider.isQualified(FlowRuleMetadataProvider.USER_GROUPS));
    }

    private static void assertReferenceValue(FieldDefinition field, String displayAttribute, String valuesHref,
                                             String filterHref) {

        assertTrue(field.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue value = (OptionsReferenceValue) field.getValue();
        assertEquals(value.getValueReferenceAttribute(), "id");
        assertEquals(value.getValueDisplayAttribute(), displayAttribute);
        Map<String, String> links = value.getLinks().stream()
                .collect(Collectors.toMap(Link::getRel, Link::getHref));
        assertEquals(links.get("values"), valuesHref);
        assertEquals(links.get("filter"), filterHref);
    }

    private List<String> fieldNamesOf(FlowType flowType) throws Exception {

        return provider.getExpressionMeta(flowType, TENANT_DOMAIN).stream()
                .map(field -> field.getField().getName()).collect(Collectors.toList());
    }

    private Map<String, FieldDefinition> fieldsOf(FlowType flowType) throws Exception {

        return provider.getExpressionMeta(flowType, TENANT_DOMAIN).stream()
                .collect(Collectors.toMap(field -> field.getField().getName(), Function.identity()));
    }

    private static List<String> operatorNames(FieldDefinition field) {

        return field.getOperators().stream().map(Operator::getName).collect(Collectors.toList());
    }
}
