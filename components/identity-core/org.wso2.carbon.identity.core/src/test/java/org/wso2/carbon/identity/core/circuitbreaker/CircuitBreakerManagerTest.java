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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link CircuitBreakerManager}.
 *
 * <p>Every test that creates a breaker entry calls {@link #registerLoader} with an
 * explicit {@link RuntimePolicy} so that no test silently depends on whatever
 * {@link DefaultPolicyConfigurationLoader} reads from identity.xml at startup.
 *
 * <p>{@link #standardPolicy()} – window=2, min=2, threshold=0.5, open-duration=600 000 ms,
 * max-in-flight=2 – is used when the test only needs the circuit to open after failures.
 * {@link #largeWindowPolicy()} prevents the circuit from opening prematurely.
 * {@link #shortOpenDurationPolicy()} enables HALF_OPEN timing tests without long sleeps.
 */
public class CircuitBreakerManagerTest {

    private static final IdentityCoreServiceDataHolder dataHolder = IdentityCoreServiceDataHolder.getInstance();
    private static final long SHORT_OPEN_DURATION_MS = 10L;

    private CircuitBreakerManager manager;
    private TenantServiceBreakerObserver mockObserver;

    @BeforeClass
    public void setUpClass() {

        URL root = getClass().getClassLoader().getResource(".");
        System.setProperty("carbon.home", new File(root.getPath()).getAbsolutePath());
        IdentityConfigParser.getInstance(new File(root.getPath(), "identity.xml").getAbsolutePath());
        IdentityUtil.populateProperties();
        manager = CircuitBreakerManager.getInstance();
    }

    @BeforeMethod
    public void setUp() {

        mockObserver = mock(TenantServiceBreakerObserver.class);
        when(mockObserver.getService()).thenReturn(TenantService.EMAIL_OTP);
    }

    @AfterMethod
    public void tearDown() {

        dataHolder.removeTenantServiceBreakerObserver(TenantService.EMAIL_OTP);
        dataHolder.removeRuntimePolicyLoader(TenantService.EMAIL_OTP);
    }

    // ─────────────────────────── tryAcquire ───────────────────────────

    @Test
    public void testTryAcquireNullTenantReturnsInvalidData() {

        Decision decision = manager.tryAcquire(null, TenantService.EMAIL_OTP);

        assertFalse(decision.isAllowed());
        assertEquals(decision.getRejectReason(), RejectReason.INVALID_DATA);
    }

    @Test
    public void testTryAcquireBlankTenantReturnsInvalidData() {

        Decision decision = manager.tryAcquire("   ", TenantService.EMAIL_OTP);

        assertFalse(decision.isAllowed());
        assertEquals(decision.getRejectReason(), RejectReason.INVALID_DATA);
    }

    @Test
    public void testTryAcquireUnknownTenantReturnsSkip() {

        String tenant = "try-unknown-1.example.com";

        Decision decision = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

        assertTrue(decision.isAllowed());
        assertTrue(decision.isSkip());
    }

    @Test
    public void testTryAcquireAllowedWhenCircuitClosed() {

        String tenant = "try-closed-1.example.com";
        try {
            registerLoader(largeWindowPolicy());
            createEntry(tenant);

            Decision decision = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            assertTrue(decision.isAllowed());
            assertFalse(decision.isSkip());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testTryAcquireRejectedWhenCircuitOpen() {

        String tenant = "try-open-1.example.com";
        try {
            registerLoader(standardPolicy());
            openCircuit(tenant);

            Decision decision = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            assertFalse(decision.isAllowed());
            assertEquals(decision.getRejectReason(), RejectReason.CIRCUIT_OPEN);
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testTryAcquireRejectedWhenBulkheadFull() {

        String tenant = "try-bulkhead-1.example.com";
        try {
            registerLoader(largeWindowPolicy());
            createEntry(tenant);

            Decision d1 = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            Decision d2 = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertTrue(d1.isAllowed() && !d1.isSkip());
            assertTrue(d2.isAllowed() && !d2.isSkip());

            Decision d3 = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            assertFalse(d3.isAllowed());
            assertEquals(d3.getRejectReason(), RejectReason.BULKHEAD_FULL);
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testTryAcquireAllowsOneProbeWhenHalfOpen() throws InterruptedException {

        String tenant = "try-half-open-1.example.com";
        try {
            registerLoader(shortOpenDurationPolicy());
            openCircuit(tenant);
            Thread.sleep(SHORT_OPEN_DURATION_MS * 4);

            Decision probe = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            assertTrue(probe.isAllowed());
            assertFalse(probe.isSkip());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testTryAcquireRejectsSecondCallWhileHalfOpenProbeInFlight() throws InterruptedException {

        String tenant = "try-half-open-2.example.com";
        try {
            registerLoader(shortOpenDurationPolicy());
            openCircuit(tenant);
            Thread.sleep(SHORT_OPEN_DURATION_MS * 4);

            Decision probe = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertTrue(probe.isAllowed());

            // Second call while the probe is still in-flight.
            Decision second = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            assertFalse(second.isAllowed());
            assertEquals(second.getRejectReason(), RejectReason.CIRCUIT_OPEN);
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    // ─────────────────────────── onComplete ───────────────────────────

    @Test
    public void testOnCompleteNullTenantDoesNothing() {

        manager.onComplete(null, TenantService.EMAIL_OTP, Decision.skip(), false);
    }

    @Test
    public void testOnCompleteBlankTenantDoesNothing() {

        manager.onComplete("  ", TenantService.EMAIL_OTP, Decision.skip(), false);
    }

    @Test
    public void testOnCompleteRejectedDecisionDoesNotCreateEntry() {

        String tenant = "complete-rejected-1.example.com";

        manager.onComplete(tenant, TenantService.EMAIL_OTP,
                Decision.rejected(RejectReason.CIRCUIT_OPEN), false);

        assertTrue(manager.tryAcquire(tenant, TenantService.EMAIL_OTP).isSkip());
    }

    @Test
    public void testOnCompleteSkipWithSuccessDoesNotCreateEntry() {

        String tenant = "complete-skip-success-1.example.com";

        manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), true);

        assertTrue(manager.tryAcquire(tenant, TenantService.EMAIL_OTP).isSkip());
    }

    @Test
    public void testOnCompleteSkipWithFailureCreatesTrackingEntry() {

        String tenant = "complete-skip-failure-1.example.com";
        try {
            registerLoader(largeWindowPolicy());

            manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), false);

            Decision decision = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertTrue(decision.isAllowed());
            assertFalse(decision.isSkip());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testOnCompleteOpensCircuitAfterFailureThreshold() {

        String tenant = "complete-threshold-1.example.com";
        try {
            registerLoader(standardPolicy());
            manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), false);
            manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), false);

            Decision decision = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            assertFalse(decision.isAllowed());
            assertEquals(decision.getRejectReason(), RejectReason.CIRCUIT_OPEN);
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testOnCompleteHalfOpenSuccessClosesCircuit() throws InterruptedException {

        String tenant = "complete-half-open-close-1.example.com";
        try {
            registerLoader(shortOpenDurationPolicy());
            openCircuit(tenant);
            Thread.sleep(SHORT_OPEN_DURATION_MS * 4);

            Decision probe = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertTrue(probe.isAllowed());

            manager.onComplete(tenant, TenantService.EMAIL_OTP, probe, true);

            assertTrue(manager.tryAcquire(tenant, TenantService.EMAIL_OTP).isAllowed());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testOnCompleteHalfOpenFailureReopensCircuit() throws InterruptedException {

        String tenant = "complete-half-open-reopen-1.example.com";
        try {
            registerLoader(shortOpenDurationPolicy());
            openCircuit(tenant);
            Thread.sleep(SHORT_OPEN_DURATION_MS * 4);

            Decision probe = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertTrue(probe.isAllowed());

            manager.onComplete(tenant, TenantService.EMAIL_OTP, probe, false);

            Decision next = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertFalse(next.isAllowed());
            assertEquals(next.getRejectReason(), RejectReason.CIRCUIT_OPEN);
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testOnCompleteEntryBecomesUntrackedWhenWindowFullWithZeroFailures() {

        String tenant = "complete-untracked-1.example.com";
        try {
            registerLoader(RuntimePolicy.builder()
                    .setWindowSize(5).setMinCallsToEvaluate(5)
                    .setFailureRateThreshold(0.5).setOpenDuration(600_000).setMaxInFlight(10)
                    .build());
            createEntry(tenant);

            for (int i = 0; i < 5; i++) {
                Decision d = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
                assertTrue(d.isAllowed() && !d.isSkip(), "Expected tracked allow at iteration " + i);
                manager.onComplete(tenant, TenantService.EMAIL_OTP, d, true);
            }

            assertTrue(manager.tryAcquire(tenant, TenantService.EMAIL_OTP).isSkip());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    // ─────────────────────────── invalidateTenant ───────────────────────────

    @Test
    public void testInvalidateTenantNullDoesNothing() {

        manager.invalidateTenant(null);
    }

    @Test
    public void testInvalidateTenantBlankDoesNothing() {

        manager.invalidateTenant("  ");
    }

    @Test
    public void testInvalidateTenantRemovesAllServiceEntries() {

        String tenant = "invalidate-all-1.example.com";
        try {
            registerLoader(standardPolicy());
            openCircuit(tenant);

            manager.invalidateTenant(tenant);

            for (TenantService service : TenantService.values()) {
                assertTrue(manager.tryAcquire(tenant, service).isSkip(),
                        "Expected skip for service: " + service);
            }
        } finally {
            manager.invalidateTenant(tenant);
        }
    }

    // ─────────────────────────── invalidateTenantService ───────────────────────────

    @Test
    public void testInvalidateTenantServiceNullDoesNothing() {

        manager.invalidateTenantService(null, TenantService.EMAIL_OTP);
    }

    @Test
    public void testInvalidateTenantServiceBlankDoesNothing() {

        manager.invalidateTenantService("", TenantService.EMAIL_OTP);
    }

    @Test
    public void testInvalidateTenantServiceRemovesEntry() {

        String tenant = "invalidate-service-1.example.com";
        registerLoader(standardPolicy());
        openCircuit(tenant);

        manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);

        assertTrue(manager.tryAcquire(tenant, TenantService.EMAIL_OTP).isSkip());
    }

    @Test
    public void testInvalidateTenantServiceDoesNotAffectOtherTenants() {

        String tenant1 = "invalidate-service-only-a.example.com";
        String tenant2 = "invalidate-service-only-b.example.com";
        try {
            registerLoader(standardPolicy());
            openCircuit(tenant1);
            openCircuit(tenant2);

            manager.invalidateTenantService(tenant1, TenantService.EMAIL_OTP);

            assertTrue(manager.tryAcquire(tenant1, TenantService.EMAIL_OTP).isSkip());
            Decision d2 = manager.tryAcquire(tenant2, TenantService.EMAIL_OTP);
            assertFalse(d2.isAllowed());
            assertEquals(d2.getRejectReason(), RejectReason.CIRCUIT_OPEN);
        } finally {
            manager.invalidateTenantService(tenant1, TenantService.EMAIL_OTP);
            manager.invalidateTenantService(tenant2, TenantService.EMAIL_OTP);
        }
    }

    // ─────────────────────────── Observer notifications ───────────────────────────

    @Test
    public void testNoObserverRegisteredDoesNotThrow() {

        String tenant = "no-obs-1.example.com";
        try {
            registerLoader(standardPolicy());
            openCircuit(tenant);
            manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testObserverNotifiedOnClosedToOpenTransition() {

        String tenant = "obs-open-1.example.com";
        try {
            registerLoader(standardPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), false);
            manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), false);

            verify(mockObserver).onStateTransition(
                    eq(tenant), eq(TenantService.EMAIL_OTP),
                    eq(CircuitState.CLOSED), eq(CircuitState.OPEN),
                    anyInt(), anyInt(), anyDouble(), anyDouble());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testObserverNotifiedOnOpenToHalfOpenTransition() throws InterruptedException {

        String tenant = "obs-half-open-1.example.com";
        try {
            registerLoader(shortOpenDurationPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            openCircuit(tenant);
            Thread.sleep(SHORT_OPEN_DURATION_MS * 4);

            manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            verify(mockObserver).onStateTransition(
                    eq(tenant), eq(TenantService.EMAIL_OTP),
                    eq(CircuitState.OPEN), eq(CircuitState.HALF_OPEN),
                    anyInt(), anyInt(), anyDouble(), anyDouble());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testObserverNotifiedOnHalfOpenToClosedTransition() throws InterruptedException {

        String tenant = "obs-closed-1.example.com";
        try {
            registerLoader(shortOpenDurationPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            openCircuit(tenant);
            Thread.sleep(SHORT_OPEN_DURATION_MS * 4);

            Decision probe = manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            assertTrue(probe.isAllowed());

            manager.onComplete(tenant, TenantService.EMAIL_OTP, probe, true);

            verify(mockObserver).onStateTransition(
                    eq(tenant), eq(TenantService.EMAIL_OTP),
                    eq(CircuitState.HALF_OPEN), eq(CircuitState.CLOSED),
                    anyInt(), anyInt(), anyDouble(), anyDouble());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testObserverNotifiedOnCircuitOpenRejection() {

        String tenant = "obs-reject-open-1.example.com";
        try {
            registerLoader(standardPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            openCircuit(tenant);

            manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            verify(mockObserver).onRejection(
                    eq(tenant), eq(TenantService.EMAIL_OTP),
                    eq(RejectReason.CIRCUIT_OPEN),
                    eq(CircuitState.OPEN),
                    anyInt(), anyInt(), anyDouble(), anyInt());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testObserverNotifiedOnBulkheadRejection() {

        String tenant = "obs-reject-bulkhead-1.example.com";
        try {
            registerLoader(largeWindowPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            createEntry(tenant);

            manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            manager.tryAcquire(tenant, TenantService.EMAIL_OTP);
            manager.tryAcquire(tenant, TenantService.EMAIL_OTP);

            verify(mockObserver).onRejection(
                    eq(tenant), eq(TenantService.EMAIL_OTP),
                    eq(RejectReason.BULKHEAD_FULL),
                    eq(CircuitState.CLOSED),
                    anyInt(), anyInt(), anyDouble(), anyInt());
        } finally {
            manager.invalidateTenantService(tenant, TenantService.EMAIL_OTP);
        }
    }

    @Test
    public void testObserverNotifiedOnForcedEviction() {

        String tenant1 = "obs-evict-a-1.example.com";
        String tenant2 = "obs-evict-b-1.example.com";
        try {
            registerLoader(largeWindowPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);

            createEntry(tenant1);
            createEntry(tenant2);

            verify(mockObserver).onForcedEviction(eq(tenant1), eq(TenantService.EMAIL_OTP));
        } finally {
            manager.invalidateTenantService(tenant1, TenantService.EMAIL_OTP);
            manager.invalidateTenantService(tenant2, TenantService.EMAIL_OTP);
        }
    }

    // ─────────────────────────── Disabled policy ───────────────────────────

    @Test
    public void testTryAcquireReturnAllowedWhenCircuitBreakerDisabled() throws Exception {

        StaticPolicy original = swapStaticPolicy(disabledPolicy());
        try {
            Decision d = manager.tryAcquire("disabled-acq.example.com", TenantService.EMAIL_OTP);

            assertTrue(d.isAllowed());
            assertFalse(d.isSkip());
        } finally {
            swapStaticPolicy(original);
        }
    }

    @Test
    public void testOnCompleteEarlyReturnWhenCircuitBreakerDisabled() throws Exception {

        String tenant = "disabled-cmpl.example.com";
        StaticPolicy original = swapStaticPolicy(disabledPolicy());
        try {
            registerLoader(largeWindowPolicy());
            manager.onComplete(tenant, TenantService.EMAIL_OTP, Decision.skip(), false);
        } finally {
            swapStaticPolicy(original);
        }

        assertTrue(manager.tryAcquire(tenant, TenantService.EMAIL_OTP).isSkip());
    }

    // ─────────────────────────── Idle eviction ───────────────────────────

    @Test
    public void testEnsureCapacityEvictsIdleEntries() throws Exception {

        String tenant1 = "idle-evict-a.example.com";
        String tenant2 = "idle-evict-b.example.com";
        StaticPolicy shortIdle = StaticPolicy.builder()
                .setEnabled(true)
                .setTenantServiceCacheCapacity(2)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(1L)
                .build();
        StaticPolicy original = swapStaticPolicy(shortIdle);
        try {
            registerLoader(largeWindowPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            createEntry(tenant1);
            Thread.sleep(10);
            createEntry(tenant2);

            verify(mockObserver).onForcedEviction(eq(tenant1), eq(TenantService.EMAIL_OTP));
        } finally {
            swapStaticPolicy(original);
            manager.invalidateTenantService(tenant1, TenantService.EMAIL_OTP);
            manager.invalidateTenantService(tenant2, TenantService.EMAIL_OTP);
        }
    }

    // ─────────────────────────── oldestOverall eviction ───────────────────────────

    @Test
    public void testEnsureCapacityEvictsViaOldestOverallWhenAllEntriesHaveInFlight() {

        String tenant1 = "oldest-overall-a.example.com";
        String tenant2 = "oldest-overall-b.example.com";
        try {
            registerLoader(largeWindowPolicy());
            dataHolder.addTenantServiceBreakerObserver(mockObserver);
            createEntry(tenant1);
            Decision inFlight = manager.tryAcquire(tenant1, TenantService.EMAIL_OTP);
            assertTrue(inFlight.isAllowed() && !inFlight.isSkip());

            createEntry(tenant2);

            verify(mockObserver).onForcedEviction(eq(tenant1), eq(TenantService.EMAIL_OTP));
        } finally {
            manager.invalidateTenantService(tenant1, TenantService.EMAIL_OTP);
            manager.invalidateTenantService(tenant2, TenantService.EMAIL_OTP);
        }
    }

    // ─────────────────────────── Helpers ───────────────────────────

    private void registerLoader(RuntimePolicy policy) {

        RuntimePolicyLoader loader = mock(RuntimePolicyLoader.class);
        when(loader.getService()).thenReturn(TenantService.EMAIL_OTP);
        when(loader.load(anyString(), any(), any())).thenReturn(policy);
        dataHolder.addRuntimePolicyLoader(loader);
    }

    private void createEntry(String tenantDomain) {

        manager.onComplete(tenantDomain, TenantService.EMAIL_OTP, Decision.skip(), false);
    }

    private void openCircuit(String tenantDomain) {

        manager.onComplete(tenantDomain, TenantService.EMAIL_OTP, Decision.skip(), false);
        manager.onComplete(tenantDomain, TenantService.EMAIL_OTP, Decision.skip(), false);
    }

    private RuntimePolicy standardPolicy() {

        return RuntimePolicy.builder()
                .setWindowSize(2).setMinCallsToEvaluate(2)
                .setFailureRateThreshold(0.5)
                .setOpenDuration(600_000)
                .setMaxInFlight(2)
                .build();
    }

    private RuntimePolicy shortOpenDurationPolicy() {

        return RuntimePolicy.builder()
                .setWindowSize(2).setMinCallsToEvaluate(2)
                .setFailureRateThreshold(0.5)
                .setOpenDuration(SHORT_OPEN_DURATION_MS)
                .setMaxInFlight(2)
                .build();
    }

    private RuntimePolicy largeWindowPolicy() {

        return RuntimePolicy.builder()
                .setWindowSize(100).setMinCallsToEvaluate(100)
                .setFailureRateThreshold(0.5)
                .setOpenDuration(600_000)
                .setMaxInFlight(2)
                .build();
    }

    private StaticPolicy disabledPolicy() {

        return StaticPolicy.builder()
                .setEnabled(false)
                .setTenantServiceCacheCapacity(2)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(600_000L)
                .build();
    }

    private StaticPolicy swapStaticPolicy(StaticPolicy newPolicy) throws Exception {

        Field field = CircuitBreakerManager.class.getDeclaredField("staticPolicy");
        field.setAccessible(true);
        StaticPolicy old = (StaticPolicy) field.get(manager);
        field.set(manager, newPolicy);
        return old;
    }
}
