/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.circuitbreaker;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link RuntimePolicyResolver}.
 */
public class RuntimePolicyResolverTest {

    private static final IdentityCoreServiceDataHolder DATA_HOLDER =
            IdentityCoreServiceDataHolder.getInstance();
    private static final RuntimePolicyResolver RESOLVER = RuntimePolicyResolver.getInstance();

    private static final TenantService SERVICE = TenantService.EMAIL_NOTIFICATION;
    private static final String TENANT_DOMAIN = "resolver-test.example.com";
    private static final String TENANT_KEY =
            TenantKeyUtil.buildTenantServiceKey(TENANT_DOMAIN, SERVICE.name());

    private RuntimePolicy defaultPolicy;
    private RuntimePolicy loaderPolicy;
    private RuntimePolicy extenderPolicy;

    @BeforeMethod
    public void setUp() {

        defaultPolicy = policy(5);
        loaderPolicy = policy(10);
        extenderPolicy = policy(20);
    }

    @AfterMethod
    public void tearDown() {

        DATA_HOLDER.removeRuntimePolicyLoader(SERVICE);
        DATA_HOLDER.setRuntimePolicyExtender(null);
    }

    // ─────────────────────────── No loader, no extender ───────────────────────────

    @Test
    public void testResolveReturnsDefaultWhenNoLoaderAndNoExtender() {

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), defaultPolicy);
    }

    // ─────────────────────────── Loader only ───────────────────────────

    @Test
    public void testResolveReturnsLoadedPolicyWhenLoaderReturnsNonNull() {

        registerLoader(loaderPolicy);

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), loaderPolicy);
    }

    @Test
    public void testResolveReturnsDefaultWhenLoaderReturnsNull() {

        registerLoader(null);

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), defaultPolicy);
    }

    @Test
    public void testLoaderIsInvokedWithCorrectArguments() {

        RuntimePolicyLoader loader = registerLoader(null);

        RESOLVER.resolve(TENANT_KEY, defaultPolicy);

        verify(loader).load(TENANT_DOMAIN, SERVICE, defaultPolicy);
    }

    // ─────────────────────────── Extender only ───────────────────────────

    @Test
    public void testResolveReturnsExtendedPolicyWhenExtenderReturnsNonNull() {

        registerExtender(extenderPolicy);

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), extenderPolicy);
    }

    @Test
    public void testResolveReturnsDefaultWhenExtenderReturnsNull() {

        registerExtender(null);

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), defaultPolicy);
    }

    @Test
    public void testExtenderIsInvokedWithCorrectArgumentsWhenNoLoader() {

        RuntimePolicyExtender extender = registerExtender(null);

        RESOLVER.resolve(TENANT_KEY, defaultPolicy);

        verify(extender).extend(TENANT_DOMAIN, SERVICE, defaultPolicy);
    }

    // ─────────────────────────── Loader + extender interaction ───────────────────────────

    @Test
    public void testExtenderResultTakesPrecedenceOverLoaderResult() {

        registerLoader(loaderPolicy);
        registerExtender(extenderPolicy);

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), extenderPolicy);
    }

    @Test
    public void testLoaderResultIsReturnedWhenExtenderReturnsNull() {

        registerLoader(loaderPolicy);
        registerExtender(null);

        assertSame(RESOLVER.resolve(TENANT_KEY, defaultPolicy), loaderPolicy);
    }

    @Test
    public void testExtenderReceivesLoadedPolicyWhenLoaderReturnsNonNull() {

        registerLoader(loaderPolicy);
        RuntimePolicyExtender extender = registerExtender(null);

        RESOLVER.resolve(TENANT_KEY, defaultPolicy);

        verify(extender).extend(TENANT_DOMAIN, SERVICE, loaderPolicy);
    }

    @Test
    public void testExtenderReceivesDefaultPolicyWhenLoaderReturnsNull() {

        registerLoader(null);
        RuntimePolicyExtender extender = registerExtender(null);

        RESOLVER.resolve(TENANT_KEY, defaultPolicy);

        verify(extender).extend(TENANT_DOMAIN, SERVICE, defaultPolicy);
    }

    // ─────────────────────────── Singleton ───────────────────────────

    @Test
    public void testGetInstanceReturnsSameInstance() {

        assertSame(RuntimePolicyResolver.getInstance(), RuntimePolicyResolver.getInstance());
    }

    // ─────────────────────────── Helpers ───────────────────────────

    private RuntimePolicyLoader registerLoader(RuntimePolicy returnValue) {

        RuntimePolicyLoader loader = mock(RuntimePolicyLoader.class);
        when(loader.getService()).thenReturn(SERVICE);
        when(loader.load(any(), any(), any())).thenReturn(returnValue);
        DATA_HOLDER.addRuntimePolicyLoader(loader);
        return loader;
    }

    private RuntimePolicyExtender registerExtender(RuntimePolicy returnValue) {

        RuntimePolicyExtender extender = mock(RuntimePolicyExtender.class);
        when(extender.extend(any(), any(), any())).thenReturn(returnValue);
        DATA_HOLDER.setRuntimePolicyExtender(extender);
        return extender;
    }

    private static RuntimePolicy policy(int windowSize) {

        return RuntimePolicy.builder()
                .setWindowSize(windowSize).setMinCallsToEvaluate(windowSize)
                .setFailureRateThreshold(0.5).setOpenDuration(60000).setMaxInFlight(5)
                .build();
    }
}
