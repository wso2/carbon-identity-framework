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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link Decision}.
 */
public class DecisionTest {

    // ─────────────────────────── allowed() ───────────────────────────

    @Test
    public void testAllowedIsAllowed() {

        assertTrue(Decision.allowed().isAllowed());
    }

    @Test
    public void testAllowedIsNotSkip() {

        assertFalse(Decision.allowed().isSkip());
    }

    @Test
    public void testAllowedGetRejectReasonIsNull() {

        assertNull(Decision.allowed().getRejectReason());
    }

    @Test
    public void testAllowedReturnsSameInstance() {

        assertSame(Decision.allowed(), Decision.allowed());
    }

    // ─────────────────────────── skip() ───────────────────────────

    @Test
    public void testSkipIsAllowed() {

        assertTrue(Decision.skip().isAllowed());
    }

    @Test
    public void testSkipIsSkip() {

        assertTrue(Decision.skip().isSkip());
    }

    @Test
    public void testSkipGetRejectReasonIsNull() {

        assertNull(Decision.skip().getRejectReason());
    }

    @Test
    public void testSkipReturnsSameInstance() {

        assertSame(Decision.skip(), Decision.skip());
    }

    // ─────────────────────────── rejected(CIRCUIT_OPEN) ───────────────────────────

    @Test
    public void testRejectedCircuitOpenIsNotAllowed() {

        assertFalse(Decision.rejected(RejectReason.CIRCUIT_OPEN).isAllowed());
    }

    @Test
    public void testRejectedCircuitOpenIsNotSkip() {

        assertFalse(Decision.rejected(RejectReason.CIRCUIT_OPEN).isSkip());
    }

    @Test
    public void testRejectedCircuitOpenGetRejectReason() {

        assertEquals(Decision.rejected(RejectReason.CIRCUIT_OPEN).getRejectReason(), RejectReason.CIRCUIT_OPEN);
    }

    @Test
    public void testRejectedCircuitOpenReturnsSameInstance() {

        assertSame(Decision.rejected(RejectReason.CIRCUIT_OPEN), Decision.rejected(RejectReason.CIRCUIT_OPEN));
    }

    // ─────────────────────────── rejected(BULKHEAD_FULL) ───────────────────────────

    @Test
    public void testRejectedBulkheadFullIsNotAllowed() {

        assertFalse(Decision.rejected(RejectReason.BULKHEAD_FULL).isAllowed());
    }

    @Test
    public void testRejectedBulkheadFullIsNotSkip() {

        assertFalse(Decision.rejected(RejectReason.BULKHEAD_FULL).isSkip());
    }

    @Test
    public void testRejectedBulkheadFullGetRejectReason() {

        assertEquals(Decision.rejected(RejectReason.BULKHEAD_FULL).getRejectReason(), RejectReason.BULKHEAD_FULL);
    }

    @Test
    public void testRejectedBulkheadFullReturnsSameInstance() {

        assertSame(Decision.rejected(RejectReason.BULKHEAD_FULL), Decision.rejected(RejectReason.BULKHEAD_FULL));
    }

    // ─────────────────────────── rejected(INVALID_DATA) ───────────────────────────

    @Test
    public void testRejectedInvalidDataIsNotAllowed() {

        assertFalse(Decision.rejected(RejectReason.INVALID_DATA).isAllowed());
    }

    @Test
    public void testRejectedInvalidDataIsNotSkip() {

        assertFalse(Decision.rejected(RejectReason.INVALID_DATA).isSkip());
    }

    @Test
    public void testRejectedInvalidDataGetRejectReason() {

        assertEquals(Decision.rejected(RejectReason.INVALID_DATA).getRejectReason(), RejectReason.INVALID_DATA);
    }

    @Test
    public void testRejectedInvalidDataReturnsSameInstance() {

        assertSame(Decision.rejected(RejectReason.INVALID_DATA), Decision.rejected(RejectReason.INVALID_DATA));
    }

    // ─────────────────────────── cross-type isolation ───────────────────────────

    @Test
    public void testAllowedAndSkipAreDistinctInstances() {

        Decision allowed = Decision.allowed();
        Decision skip = Decision.skip();

        assertFalse(allowed == skip);
    }

    @Test
    public void testRejectedInstancesAreDistinctFromEachOther() {

        Decision circuitOpen = Decision.rejected(RejectReason.CIRCUIT_OPEN);
        Decision bulkheadFull = Decision.rejected(RejectReason.BULKHEAD_FULL);
        Decision invalidData = Decision.rejected(RejectReason.INVALID_DATA);

        assertFalse(circuitOpen == bulkheadFull);
        assertFalse(circuitOpen == invalidData);
        assertFalse(bulkheadFull == invalidData);
    }
}
