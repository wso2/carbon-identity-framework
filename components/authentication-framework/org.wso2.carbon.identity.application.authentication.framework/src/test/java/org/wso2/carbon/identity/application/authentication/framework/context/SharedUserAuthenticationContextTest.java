/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link SharedUserAuthenticationContext}.
 */
public class SharedUserAuthenticationContextTest {

    private static final String SHARED_USER_TENANT = "sharedorg.com";
    private static final String SP_TENANT = "sporg.com";
    private static final String WRAPPED_RESIDENT_TENANT = "wrapped-resident.com";

    @Test(description = "Test getUserResidentTenantDomain returns shared user's tenant domain when " +
            "SharedUserIdentifierExecutor step is present")
    public void testGetUserResidentTenantDomainWithSharedUser() {

        AuthenticationContext wrappedContext = new AuthenticationContext();
        wrappedContext.setTenantDomain(SP_TENANT);
        wrappedContext.setUserResidentTenantDomain(WRAPPED_RESIDENT_TENANT);

        SequenceConfig sequenceConfig = new SequenceConfig();

        // Step 1: SharedUserIdentifierExecutor step with a shared user.
        StepConfig sharedStep = new StepConfig();
        AuthenticatorConfig sharedAuth = new AuthenticatorConfig();
        sharedAuth.setName(FrameworkConstants.SHARED_USER_IDENTIFIER_HANDLER);
        sharedStep.setAuthenticatedAutenticator(sharedAuth);
        AuthenticatedUser sharedUser = new AuthenticatedUser();
        sharedUser.setSharedUser(true);
        sharedUser.setTenantDomain(SHARED_USER_TENANT);
        sharedStep.setAuthenticatedUser(sharedUser);

        // Step 2: Regular authenticator step.
        StepConfig regularStep = new StepConfig();
        AuthenticatorConfig regularAuth = new AuthenticatorConfig();
        regularAuth.setName("BasicAuthenticator");
        regularStep.setAuthenticatedAutenticator(regularAuth);
        AuthenticatedUser regularUser = new AuthenticatedUser();
        regularUser.setSharedUser(false);
        regularUser.setTenantDomain(SP_TENANT);
        regularStep.setAuthenticatedUser(regularUser);

        Map<Integer, StepConfig> stepMap = new HashMap<>();
        stepMap.put(1, sharedStep);
        stepMap.put(2, regularStep);
        sequenceConfig.setStepMap(stepMap);
        wrappedContext.setSequenceConfig(sequenceConfig);

        SharedUserAuthenticationContext context = new SharedUserAuthenticationContext(wrappedContext);

        assertEquals(context.getUserResidentTenantDomain(), SHARED_USER_TENANT,
                "Should return shared user's tenant domain");
    }

    @Test(description = "Test getUserResidentTenantDomain falls back to wrapped context when no shared user step")
    public void testGetUserResidentTenantDomainWithoutSharedUser() {

        AuthenticationContext wrappedContext = new AuthenticationContext();
        wrappedContext.setTenantDomain(SP_TENANT);
        wrappedContext.setUserResidentTenantDomain(WRAPPED_RESIDENT_TENANT);

        SequenceConfig sequenceConfig = new SequenceConfig();

        StepConfig regularStep = new StepConfig();
        AuthenticatorConfig regularAuth = new AuthenticatorConfig();
        regularAuth.setName("BasicAuthenticator");
        regularStep.setAuthenticatedAutenticator(regularAuth);
        AuthenticatedUser regularUser = new AuthenticatedUser();
        regularUser.setSharedUser(false);
        regularStep.setAuthenticatedUser(regularUser);

        Map<Integer, StepConfig> stepMap = new HashMap<>();
        stepMap.put(1, regularStep);
        sequenceConfig.setStepMap(stepMap);
        wrappedContext.setSequenceConfig(sequenceConfig);

        SharedUserAuthenticationContext context = new SharedUserAuthenticationContext(wrappedContext);

        assertEquals(context.getUserResidentTenantDomain(), WRAPPED_RESIDENT_TENANT,
                "Should fall back to wrapped context's userResidentTenantDomain");
    }

    @Test(description = "Test getUserResidentTenantDomain skips step with SharedUserIdentifierExecutor " +
            "but non-shared user")
    public void testGetUserResidentTenantDomainWithHandlerButNonSharedUser() {

        AuthenticationContext wrappedContext = new AuthenticationContext();
        wrappedContext.setUserResidentTenantDomain(WRAPPED_RESIDENT_TENANT);

        SequenceConfig sequenceConfig = new SequenceConfig();

        StepConfig step = new StepConfig();
        AuthenticatorConfig auth = new AuthenticatorConfig();
        auth.setName(FrameworkConstants.SHARED_USER_IDENTIFIER_HANDLER);
        step.setAuthenticatedAutenticator(auth);
        AuthenticatedUser user = new AuthenticatedUser();
        user.setSharedUser(false);
        step.setAuthenticatedUser(user);

        Map<Integer, StepConfig> stepMap = new HashMap<>();
        stepMap.put(1, step);
        sequenceConfig.setStepMap(stepMap);
        wrappedContext.setSequenceConfig(sequenceConfig);

        SharedUserAuthenticationContext context = new SharedUserAuthenticationContext(wrappedContext);

        assertEquals(context.getUserResidentTenantDomain(), WRAPPED_RESIDENT_TENANT,
                "Should fall back when user is not shared even if handler name matches");
    }

    @Test(description = "Test that getTenantDomain delegates to wrapped context")
    public void testGetTenantDomainDelegates() {

        AuthenticationContext wrappedContext = new AuthenticationContext();
        wrappedContext.setTenantDomain(SP_TENANT);

        SharedUserAuthenticationContext context = new SharedUserAuthenticationContext(wrappedContext);

        assertEquals(context.getTenantDomain(), SP_TENANT,
                "getTenantDomain should delegate to wrapped context");
    }

    @Test(description = "Test that getWrappedContext returns the original context")
    public void testGetWrappedContext() {

        AuthenticationContext wrappedContext = new AuthenticationContext();
        SharedUserAuthenticationContext context = new SharedUserAuthenticationContext(wrappedContext);

        assertSame(context.getWrappedContext(), wrappedContext,
                "getWrappedContext should return the original context");
    }
}
