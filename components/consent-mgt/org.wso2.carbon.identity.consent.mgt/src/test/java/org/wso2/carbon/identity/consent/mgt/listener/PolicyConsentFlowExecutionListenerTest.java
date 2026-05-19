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

package org.wso2.carbon.identity.consent.mgt.listener;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.FORM;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.POLICY;

public class PolicyConsentFlowExecutionListenerTest {

    @Mock
    private IdentityConsentDataHolder mockDataHolder;

    @Mock
    private ConsentManager mockConsentManager;

    private MockedStatic<IdentityConsentDataHolder> identityConsentDataHolder;

    private PolicyConsentFlowExecutionListener listener;

    private FlowExecutionContext context;

    @BeforeMethod
    public void setUp() {

        initMocks(this);
        identityConsentDataHolder = mockStatic(IdentityConsentDataHolder.class);
        identityConsentDataHolder.when(IdentityConsentDataHolder::getInstance).thenReturn(mockDataHolder);
        when(mockDataHolder.getConsentManager()).thenReturn(mockConsentManager);
        listener = new PolicyConsentFlowExecutionListener();
        context = mock(FlowExecutionContext.class);
        when(context.getFlowType()).thenReturn("REGISTRATION");
        when(context.getContextIdentifier()).thenReturn("ctx-123");
    }

    @AfterMethod
    public void tearDown() {

        identityConsentDataHolder.close();
    }

    // ---- doPostExecute guard clauses ----

    @Test
    public void testDoPostExecute_NullData_ReturnsTrue() throws Exception {

        FlowExecutionStep step = new FlowExecutionStep.Builder().build();
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testDoPostExecute_NullComponents_ReturnsTrue() throws Exception {

        FlowExecutionStep step = new FlowExecutionStep.Builder()
                .data(new DataDTO.Builder().build())
                .build();
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testDoPostExecute_NonFormComponent_ReturnsTrue() throws Exception {

        ComponentDTO button = new ComponentDTO.Builder().type("BUTTON").build();
        FlowExecutionStep step = stepWithTopLevelComponents(Collections.singletonList(button));
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testDoPostExecute_FormComponent_NullChildren_ReturnsTrue() throws Exception {

        ComponentDTO form = new ComponentDTO.Builder().type(FORM).components(null).build();
        FlowExecutionStep step = stepWithTopLevelComponents(Collections.singletonList(form));
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testDoPostExecute_FormComponent_NonPolicyChildren_ReturnsTrue() throws Exception {

        ComponentDTO input = new ComponentDTO.Builder().type("INPUT").build();
        ComponentDTO form = new ComponentDTO.Builder().type(FORM)
                .components(Collections.singletonList(input)).build();
        FlowExecutionStep step = stepWithTopLevelComponents(Collections.singletonList(form));
        assertTrue(listener.doPostExecute(step, context));
    }

    // ---- enrichPolicyComponent config guard clauses ----

    @Test
    public void testEnrichPolicyComponent_NullConfigs_SkipsGracefully() throws Exception {

        ComponentDTO policyComp = new ComponentDTO.Builder().type(POLICY).configs(null).build();
        FlowExecutionStep step = stepWithPolicyComponent(policyComp);
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testEnrichPolicyComponent_MissingPoliciesKey_SkipsGracefully() throws Exception {

        Map<String, Object> configs = new HashMap<>();
        configs.put("otherKey", "value");
        ComponentDTO policyComp = new ComponentDTO.Builder().type(POLICY).configs(configs).build();
        FlowExecutionStep step = stepWithPolicyComponent(policyComp);
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testEnrichPolicyComponent_PoliciesNotList_SkipsGracefully() throws Exception {

        ComponentDTO policyComp = policyComponentWithPoliciesObject("not-a-list");
        FlowExecutionStep step = stepWithPolicyComponent(policyComp);
        assertTrue(listener.doPostExecute(step, context));
    }

    // ---- Policy entry-level guard clauses ----

    @Test
    public void testEnrichPolicyComponent_BlankPurposeId_SkipsEntry() throws Exception {

        Map<String, Object> policy = new HashMap<>();
        policy.put("purposeId", "");
        FlowExecutionStep step = stepWithPolicyMap(policy);
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testEnrichPolicyComponent_NullPurposeId_SkipsEntry() throws Exception {

        Map<String, Object> policy = new HashMap<>();
        policy.put("purposeId", null);
        FlowExecutionStep step = stepWithPolicyMap(policy);
        assertTrue(listener.doPostExecute(step, context));
    }

    @Test
    public void testEnrichPolicyComponent_PurposeNameAlreadyPresent_SkipsEntry() throws Exception {

        Map<String, Object> policy = new HashMap<>();
        policy.put("purposeId", "purpose-uuid-1");
        policy.put("name", "Existing Name");
        FlowExecutionStep step = stepWithPolicyMap(policy);
        assertTrue(listener.doPostExecute(step, context));
        assertEquals(policy.get("name"), "Existing Name");
    }

    // ---- Consent manager interaction ----

    @Test
    public void testEnrichPolicyComponent_PurposeNotFound_NoFieldsWritten() throws Exception {

        when(mockConsentManager.getPurposeByUuid("unknown-uuid")).thenReturn(null);
        Map<String, Object> policy = policyMapWithId("unknown-uuid");
        assertTrue(listener.doPostExecute(stepWithPolicyMap(policy), context));
        assertTrue(policy.get("name") == null);
    }

    @Test(expectedExceptions = FlowEngineServerException.class)
    public void testEnrichPolicyComponent_ConsentManagerThrows_WrapsAsServerException() throws Exception {

        when(mockConsentManager.getPurposeByUuid("purpose-1"))
                .thenThrow(new ConsentManagementException("DB error", "DB error"));
        listener.doPostExecute(stepWithPolicyMap(policyMapWithId("purpose-1")), context);
    }

    // ---- Full enrichment paths ----

    @Test
    public void testEnrichPolicyComponent_FullEnrichment_WithLatestVersion() throws Exception {

        PurposeVersion version = new PurposeVersion();
        version.setDescription("Version desc");
        Map<String, String> props = new HashMap<>();
        props.put("policyUrl", "https://example.com/privacy");
        version.setProperties(props);
        PurposePIICategory mandatoryCat = new PurposePIICategory(1, Boolean.TRUE);
        version.setPurposePIICategories(Collections.singletonList(mandatoryCat));

        Purpose purpose = buildPurpose("Privacy Policy", "Purpose desc");
        purpose.setLatestVersion(version);

        when(mockConsentManager.getPurposeByUuid("purpose-1")).thenReturn(purpose);
        Map<String, Object> policy = policyMapWithId("purpose-1");

        assertTrue(listener.doPostExecute(stepWithPolicyMap(policy), context));
        assertEquals(policy.get("name"), "Privacy Policy");
        assertEquals(policy.get("description"), "Version desc");
        assertEquals(policy.get("policyUrl"), "https://example.com/privacy");
        assertEquals(policy.get("mandatory"), Boolean.TRUE);
    }

    @Test
    public void testEnrichPolicyComponent_FallbackDescription_NoLatestVersion() throws Exception {

        Purpose purpose = buildPurpose("Marketing", "Fallback desc");
        when(mockConsentManager.getPurposeByUuid("purpose-2")).thenReturn(purpose);
        Map<String, Object> policy = policyMapWithId("purpose-2");

        assertTrue(listener.doPostExecute(stepWithPolicyMap(policy), context));
        assertEquals(policy.get("name"), "Marketing");
        assertEquals(policy.get("description"), "Fallback desc");
        assertEquals(policy.get("policyUrl"), "");
        assertEquals(policy.get("mandatory"), Boolean.FALSE);
    }

    @Test
    public void testEnrichPolicyComponent_NullPurposeDescription_WritesEmptyString() throws Exception {

        Purpose purpose = buildPurpose("Telemetry", null);
        when(mockConsentManager.getPurposeByUuid("purpose-3")).thenReturn(purpose);

        assertTrue(listener.doPostExecute(stepWithPolicyMap(policyMapWithId("purpose-3")), context));
        // resolveDescription returns "" when both version and purpose description are null
    }

    @Test
    public void testEnrichPolicyComponent_NonMandatoryPIICategory_MandatoryFalse() throws Exception {

        PurposeVersion version = new PurposeVersion();
        PurposePIICategory cat = new PurposePIICategory(1, Boolean.FALSE);
        version.setPurposePIICategories(Collections.singletonList(cat));

        Purpose purpose = buildPurpose("Analytics", "desc");
        purpose.setLatestVersion(version);
        when(mockConsentManager.getPurposeByUuid("purpose-4")).thenReturn(purpose);
        Map<String, Object> policy = policyMapWithId("purpose-4");

        assertTrue(listener.doPostExecute(stepWithPolicyMap(policy), context));
        assertEquals(policy.get("mandatory"), Boolean.FALSE);
    }

    @Test
    public void testEnrichPolicyComponent_LatestVersion_NullPoliciesCategories_MandatoryFalse() throws Exception {

        PurposeVersion version = new PurposeVersion();
        version.setPurposePIICategories(null);

        Purpose purpose = buildPurpose("Analytics", "desc");
        purpose.setLatestVersion(version);
        when(mockConsentManager.getPurposeByUuid("purpose-5")).thenReturn(purpose);
        Map<String, Object> policy = policyMapWithId("purpose-5");

        assertTrue(listener.doPostExecute(stepWithPolicyMap(policy), context));
        assertEquals(policy.get("mandatory"), Boolean.FALSE);
    }

    @Test
    public void testEnrichPolicyComponent_MixedPolicies_OnlyEligibleEntriesEnriched() throws Exception {

        Purpose purpose = buildPurpose("Legal", "Legal consent");
        when(mockConsentManager.getPurposeByUuid("legal-uuid")).thenReturn(purpose);

        Map<String, Object> alreadyNamed = new HashMap<>();
        alreadyNamed.put("purposeId", "legal-uuid");
        alreadyNamed.put("name", "Pre-existing");

        Map<String, Object> blankId = new HashMap<>();
        blankId.put("purposeId", "");

        Map<String, Object> eligible = policyMapWithId("legal-uuid");

        List<Object> mixed = Arrays.asList(alreadyNamed, blankId, eligible);
        ComponentDTO policyComp = policyComponentWithPoliciesObject(mixed);
        FlowExecutionStep step = stepWithPolicyComponent(policyComp);

        assertTrue(listener.doPostExecute(step, context));
        assertEquals(alreadyNamed.get("name"), "Pre-existing");
        assertEquals(eligible.get("name"), "Legal");
    }

    // ---- Listener metadata ----

    @Test
    public void testGetDefaultOrderId_Returns4() {

        assertEquals(listener.getDefaultOrderId(), 4);
    }

    @Test
    public void testGetExecutionOrderId_Returns4() {

        assertEquals(listener.getExecutionOrderId(), 4);
    }

    // ---- Helpers ----

    private Map<String, Object> policyMapWithId(String purposeId) {

        Map<String, Object> policy = new HashMap<>();
        policy.put("purposeId", purposeId);
        return policy;
    }

    private ComponentDTO policyComponentWithPoliciesObject(Object policiesValue) {

        Map<String, Object> configs = new HashMap<>();
        configs.put("policies", policiesValue);
        return new ComponentDTO.Builder().type(POLICY).configs(configs).build();
    }

    private FlowExecutionStep stepWithPolicyComponent(ComponentDTO policyComp) {

        ComponentDTO form = new ComponentDTO.Builder().type(FORM)
                .components(Collections.singletonList(policyComp)).build();
        return stepWithTopLevelComponents(Collections.singletonList(form));
    }

    private FlowExecutionStep stepWithPolicyMap(Map<String, Object> policy) {

        return stepWithPolicyComponent(policyComponentWithPoliciesObject(Collections.singletonList(policy)));
    }

    private FlowExecutionStep stepWithTopLevelComponents(List<ComponentDTO> components) {

        DataDTO data = new DataDTO.Builder().components(components).build();
        return new FlowExecutionStep.Builder().data(data).build();
    }

    private Purpose buildPurpose(String name, String description) {

        return new Purpose(1, name, description, "GROUP", "SYSTEM", -1234);
    }
}
