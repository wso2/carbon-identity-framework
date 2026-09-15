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

package org.wso2.carbon.identity.client.attestation.mgt.model;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the device recognition verdicts carried on {@link ClientAttestationContext}.
 */
public class ClientAttestationContextTest {

    private static final String MEETS_DEVICE_INTEGRITY = "MEETS_DEVICE_INTEGRITY";
    private static final String MEETS_STRONG_INTEGRITY = "MEETS_STRONG_INTEGRITY";

    @Test
    public void testDeviceIntegrityVerdictsDefaultToNull() {

        ClientAttestationContext context = new ClientAttestationContext(true);

        assertNull(context.getDeviceIntegrityVerdicts(),
                "Verdicts must stay null until the validator sets them, so callers can distinguish " +
                        "'attestation never ran' from 'attestation ran and returned no verdicts'.");
    }

    @Test
    public void testSetAndGetDeviceIntegrityVerdicts() {

        ClientAttestationContext context = new ClientAttestationContext(true);
        List<String> verdicts = Arrays.asList(MEETS_DEVICE_INTEGRITY, MEETS_STRONG_INTEGRITY);

        context.setDeviceIntegrityVerdicts(verdicts);

        assertEquals(context.getDeviceIntegrityVerdicts(), verdicts);
    }

    @Test
    public void testSetDeviceIntegrityVerdictsToEmptyListIsDistinctFromNull() {

        ClientAttestationContext context = new ClientAttestationContext(true);

        context.setDeviceIntegrityVerdicts(Collections.emptyList());

        assertNotNull(context.getDeviceIntegrityVerdicts());
        assertTrue(context.getDeviceIntegrityVerdicts().isEmpty());
    }

    @Test
    public void testSetDeviceIntegrityVerdictsBackToNull() {

        ClientAttestationContext context = new ClientAttestationContext(true);
        context.setDeviceIntegrityVerdicts(Collections.singletonList(MEETS_DEVICE_INTEGRITY));

        context.setDeviceIntegrityVerdicts(null);

        assertNull(context.getDeviceIntegrityVerdicts());
    }

    @Test
    public void testToStringIncludesDeviceIntegrityVerdicts() {

        ClientAttestationContext context = new ClientAttestationContext(true);
        context.setDeviceIntegrityVerdicts(Arrays.asList(MEETS_DEVICE_INTEGRITY, MEETS_STRONG_INTEGRITY));

        String rendered = context.toString();

        assertTrue(rendered.contains("deviceIntegrityVerdicts="),
                "toString must expose the field for diagnostics: " + rendered);
        assertTrue(rendered.contains(MEETS_DEVICE_INTEGRITY), rendered);
        assertTrue(rendered.contains(MEETS_STRONG_INTEGRITY), rendered);
    }

    @Test
    public void testToStringWithNullVerdictsDoesNotThrow() {

        ClientAttestationContext context = new ClientAttestationContext(false);

        String rendered = context.toString();

        assertTrue(rendered.contains("deviceIntegrityVerdicts=null"), rendered);
    }
}
