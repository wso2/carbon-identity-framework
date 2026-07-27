/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.service;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementClientException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.internal.dao.RuleManagementDAO;
import org.wso2.carbon.identity.rule.management.internal.service.impl.RuleManagementServiceImpl;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class RuleManagementServiceImplTest {

    private RuleManagementServiceImpl ruleManagementService;
    private RuleManagementDAO ruleManagementDAO;

    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;

    public static final String RULE_ID = "ruleId";
    public static final String TENANT_DOMAIN = "test.com";
    public static final int TENANT_ID = 1;

    @BeforeClass
    public void setUpClass() {

        ruleManagementService = RuleManagementServiceImpl.getInstance();

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMockedStatic.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        ruleManagementDAO = mock(RuleManagementDAO.class);
        Field daoField = RuleManagementServiceImpl.class.getDeclaredField("ruleManagementDAO");
        daoField.setAccessible(true);
        daoField.set(ruleManagementService, ruleManagementDAO);
    }

    @Test
    public void testAddRule() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);

        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(rule);

        Rule result = ruleManagementService.addRule(rule, TENANT_DOMAIN);

        verify(ruleManagementDAO).addRule(rule, TENANT_ID);
        verify(ruleManagementDAO).getRuleByRuleId(RULE_ID, TENANT_ID);

        assertEquals(result, rule);
    }

    @Test
    public void testUpdateRule() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(rule);

        Rule result = ruleManagementService.updateRule(rule, TENANT_DOMAIN);

        verify(ruleManagementDAO).updateRule(rule, TENANT_ID);
        verify(ruleManagementDAO, times(2)).getRuleByRuleId(RULE_ID, TENANT_ID);
        assertEquals(result, rule);
    }

    @Test
    public void testDeleteRule() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(rule);

        ruleManagementService.deleteRule(RULE_ID, TENANT_DOMAIN);

        verify(ruleManagementDAO).deleteRule(RULE_ID, TENANT_ID);
    }

    @Test
    public void testGetRuleByRuleId() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(rule);

        Rule result = ruleManagementService.getRuleByRuleId(RULE_ID, TENANT_DOMAIN);

        verify(ruleManagementDAO).getRuleByRuleId(RULE_ID, TENANT_ID);
        assertEquals(result, rule);
    }

    @Test
    public void testDeactivateRule() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(rule);

        Rule result = ruleManagementService.deactivateRule(RULE_ID, TENANT_DOMAIN);

        verify(ruleManagementDAO).deactivateRule(RULE_ID, TENANT_ID);
        verify(ruleManagementDAO, times(2)).getRuleByRuleId(RULE_ID, TENANT_ID);
        assertEquals(result, rule);
    }

    @Test(expectedExceptions = RuleManagementClientException.class, expectedExceptionsMessageRegExp =
            "Rule not found for the given rule id: " + RULE_ID)
    public void testUpdateIfRuleNotExist() throws RuleManagementException {

        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(null);

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        ruleManagementService.updateRule(rule, TENANT_DOMAIN);
    }
}
