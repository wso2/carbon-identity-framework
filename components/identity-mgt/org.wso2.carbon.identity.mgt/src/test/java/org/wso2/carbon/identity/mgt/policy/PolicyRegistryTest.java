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

package org.wso2.carbon.identity.mgt.policy;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class PolicyRegistryTest {

    private PolicyRegistry policyRegistry;

    @BeforeMethod
    public void setUp() {
        policyRegistry = new PolicyRegistry();
    }

    /*
    Test for the enforcePasswordPolicies method.
     */
    @Test
    public void testEnforcePasswordPoliciesSuccess() throws PolicyViolationException {

        AbstractPasswordPolicyEnforcer mockPolicy = mock(AbstractPasswordPolicyEnforcer.class);

        when(mockPolicy.enforce(any())).thenReturn(true);

        policyRegistry.addPolicy(mockPolicy);

        policyRegistry.enforcePasswordPolicies("dummyArg");

        verify(mockPolicy).enforce(any());
    }

    /*
    Test for the enforcePasswordPolicies method when a policy violation occurs.
     */
    @Test(expectedExceptions = PolicyViolationException.class)
    public void testEnforcePasswordPoliciesFailure() throws PolicyViolationException {

        AbstractPasswordPolicyEnforcer mockPolicy = mock(AbstractPasswordPolicyEnforcer.class);

        when(mockPolicy.enforce(any())).thenReturn(false);
        when(mockPolicy.getErrorMessage()).thenReturn("Policy violation occurred.");

        policyRegistry.addPolicy(mockPolicy);

        policyRegistry.enforcePasswordPolicies("dummyArg");
    }

    /*
    Test for the enforcePasswordPolicies method when no policies are added.
     */
    @Test
    public void testEnforcePasswordPoliciesNoPolicies() {

        try {
            policyRegistry.enforcePasswordPolicies("dummyArg");
        } catch (PolicyViolationException e) {
            fail("No policies added, so no exception should be thrown.");
        }
    }

    /*
    Test for the addPolicy method.
     */
    @Test
    public void testAddPolicy() {

        AbstractPasswordPolicyEnforcer mockPolicy = mock(AbstractPasswordPolicyEnforcer.class);

        policyRegistry.addPolicy(mockPolicy);

        try {
            java.lang.reflect.Field field = PolicyRegistry.class.getDeclaredField("policyCollection");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<PolicyEnforcer> policies = (List<PolicyEnforcer>) field.get(policyRegistry);

            assertEquals(policies.size(), 1);
            assertEquals(policies.get(0), mockPolicy);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failed to access private field.");
        }
    }

    /*
    Test for the enforcePasswordPolicies method with multiple policies.
     */
    @Test
    public void testEnforcePasswordPoliciesWithMultiplePolicies() throws PolicyViolationException {

        AbstractPasswordPolicyEnforcer mockPolicy1 = mock(AbstractPasswordPolicyEnforcer.class);
        AbstractPasswordPolicyEnforcer mockPolicy2 = mock(AbstractPasswordPolicyEnforcer.class);

        when(mockPolicy1.enforce(any())).thenReturn(true);
        when(mockPolicy2.enforce(any())).thenReturn(true);

        policyRegistry.addPolicy(mockPolicy1);
        policyRegistry.addPolicy(mockPolicy2);

        policyRegistry.enforcePasswordPolicies("dummyArg");

        verify(mockPolicy1).enforce(any());
        verify(mockPolicy2).enforce(any());
    }

    /*
    Test for the enforcePasswordPolicies method with multiple policies and mixed results.
     */
    @Test(expectedExceptions = PolicyViolationException.class)
    public void testEnforcePasswordPoliciesWithMixedResults() throws PolicyViolationException {

        AbstractPasswordPolicyEnforcer mockPolicy1 = mock(AbstractPasswordPolicyEnforcer.class);
        AbstractPasswordPolicyEnforcer mockPolicy2 = mock(AbstractPasswordPolicyEnforcer.class);

        when(mockPolicy1.enforce(any())).thenReturn(true);
        when(mockPolicy2.enforce(any())).thenReturn(false);
        when(mockPolicy2.getErrorMessage()).thenReturn("Second policy violated.");

        policyRegistry.addPolicy(mockPolicy1);
        policyRegistry.addPolicy(mockPolicy2);

        policyRegistry.enforcePasswordPolicies("dummyArg");
    }
}
