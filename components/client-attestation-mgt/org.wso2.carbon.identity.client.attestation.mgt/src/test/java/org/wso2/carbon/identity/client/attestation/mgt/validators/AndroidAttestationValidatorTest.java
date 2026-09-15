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

package org.wso2.carbon.identity.client.attestation.mgt.validators;

import com.google.api.services.playintegrity.v1.model.AppIntegrity;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenResponse;
import com.google.api.services.playintegrity.v1.model.DeviceIntegrity;
import com.google.api.services.playintegrity.v1.model.RequestDetails;
import com.google.api.services.playintegrity.v1.model.TokenPayloadExternal;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ClientAttestationMetaData;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the device recognition verdict extraction added to
 * {@link AndroidAttestationValidator#validateAttestation}.
 *
 * <p>The extraction lives in the private {@code validateIntegrityResponse}, invoked here by
 * reflection. Going through the public {@code validateAttestation} is not viable: it first calls
 * {@code decodeIntegrityToken}, which opens a real HTTPS transport to the Google Play Integrity
 * service.
 */
public class AndroidAttestationValidatorTest {

    private static final String PACKAGE_NAME = "com.wso2.sample.android";
    private static final String OTHER_PACKAGE_NAME = "com.attacker.other";
    private static final String PLAY_RECOGNIZED = "PLAY_RECOGNIZED";
    private static final String UNRECOGNIZED_VERSION = "UNRECOGNIZED_VERSION";
    private static final String APP_ID = "app-resource-id";
    private static final String TENANT_DOMAIN = "carbon.super";

    private static final String MEETS_DEVICE_INTEGRITY = "MEETS_DEVICE_INTEGRITY";
    private static final String MEETS_STRONG_INTEGRITY = "MEETS_STRONG_INTEGRITY";

    private AndroidAttestationValidator validator;
    private MockedStatic<IdentityUtil> mockedIdentityUtil;

    @BeforeMethod
    public void setUp() {

        ClientAttestationMetaData metaData = new ClientAttestationMetaData();
        metaData.setAndroidPackageName(PACKAGE_NAME);
        validator = new AndroidAttestationValidator(APP_ID, TENANT_DOMAIN, metaData);

        // No allowed-window configured, so validateRequestDetails takes its "details are valid" branch
        // and the tests stay independent of wall-clock time.
        mockedIdentityUtil = mockStatic(IdentityUtil.class);
        mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);
    }

    @AfterMethod
    public void tearDown() {

        mockedIdentityUtil.close();
    }

    @Test
    public void testVerdictsAreExtractedWhenPresent() throws Exception {

        DecodeIntegrityTokenResponse response = buildResponse(PACKAGE_NAME, PLAY_RECOGNIZED,
                deviceIntegrity(Arrays.asList(MEETS_DEVICE_INTEGRITY, MEETS_STRONG_INTEGRITY)));
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertEquals(context.getDeviceIntegrityVerdicts(),
                Arrays.asList(MEETS_DEVICE_INTEGRITY, MEETS_STRONG_INTEGRITY));
    }

    @Test
    public void testVerdictsAreEmptyListWhenDeviceIntegrityIsAbsent() throws Exception {

        DecodeIntegrityTokenResponse response = buildResponse(PACKAGE_NAME, PLAY_RECOGNIZED, null);
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertNotNull(context.getDeviceIntegrityVerdicts(),
                "An absent deviceIntegrity block must yield an empty list, never null.");
        assertTrue(context.getDeviceIntegrityVerdicts().isEmpty());
    }

    @Test
    public void testVerdictsAreEmptyListWhenRecognitionVerdictIsAbsent() throws Exception {

        DecodeIntegrityTokenResponse response = buildResponse(PACKAGE_NAME, PLAY_RECOGNIZED,
                deviceIntegrity(null));
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertNotNull(context.getDeviceIntegrityVerdicts());
        assertTrue(context.getDeviceIntegrityVerdicts().isEmpty());
    }

    @Test
    public void testEmptyVerdictListFromGoogleIsPreserved() throws Exception {

        DecodeIntegrityTokenResponse response = buildResponse(PACKAGE_NAME, PLAY_RECOGNIZED,
                deviceIntegrity(Collections.emptyList()));
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertNotNull(context.getDeviceIntegrityVerdicts());
        assertTrue(context.getDeviceIntegrityVerdicts().isEmpty());
    }

    @Test
    public void testVerdictsAreExtractedEvenWhenPackageNameDoesNotMatch() throws Exception {

        // A device that fails attestation still reports verdicts; the device policy component needs
        // them precisely in this case, so extraction must sit outside the isAttested branch.
        DecodeIntegrityTokenResponse response = buildResponse(OTHER_PACKAGE_NAME, PLAY_RECOGNIZED,
                deviceIntegrity(Collections.singletonList(MEETS_DEVICE_INTEGRITY)));
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertFalse(context.isAttested(), "Package name mismatch must fail attestation.");
        assertEquals(context.getDeviceIntegrityVerdicts(),
                Collections.singletonList(MEETS_DEVICE_INTEGRITY),
                "Verdicts must be extracted regardless of the attestation outcome.");
    }

    @Test
    public void testVerdictsAreExtractedEvenWhenAppIsNotPlayRecognized() throws Exception {

        DecodeIntegrityTokenResponse response = buildResponse(PACKAGE_NAME, UNRECOGNIZED_VERSION,
                deviceIntegrity(Collections.singletonList(MEETS_STRONG_INTEGRITY)));
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertFalse(context.isAttested(), "An unrecognised app must fail attestation.");
        assertEquals(context.getDeviceIntegrityVerdicts(),
                Collections.singletonList(MEETS_STRONG_INTEGRITY));
    }

    @Test
    public void testAttestationStillSucceedsForAValidResponse() throws Exception {

        // Guards the pre-existing isAttested behaviour against regression from the new extraction.
        DecodeIntegrityTokenResponse response = buildResponse(PACKAGE_NAME, PLAY_RECOGNIZED,
                deviceIntegrity(Collections.singletonList(MEETS_DEVICE_INTEGRITY)));
        ClientAttestationContext context = new ClientAttestationContext(true);

        invokeValidateIntegrityResponse(response, context);

        assertTrue(context.isAttested());
        assertNull(context.getValidationFailureMessage());
    }

    /*
     * The Play Integrity model classes are Google API generated types whose constructors reach into
     * Guava, which is not on this module's test classpath. Mockito instantiates via Objenesis without
     * running constructors, so mocks sidestep that without adding a test-only dependency.
     */
    private DeviceIntegrity deviceIntegrity(List<String> verdicts) {

        DeviceIntegrity deviceIntegrity = mock(DeviceIntegrity.class);
        when(deviceIntegrity.getDeviceRecognitionVerdict()).thenReturn(verdicts);
        return deviceIntegrity;
    }

    private DecodeIntegrityTokenResponse buildResponse(String requestPackageName, String appRecognitionVerdict,
                                                       DeviceIntegrity deviceIntegrity) {

        RequestDetails requestDetails = mock(RequestDetails.class);
        when(requestDetails.getRequestPackageName()).thenReturn(requestPackageName);
        when(requestDetails.getTimestampMillis()).thenReturn(System.currentTimeMillis());

        AppIntegrity appIntegrity = mock(AppIntegrity.class);
        when(appIntegrity.getAppRecognitionVerdict()).thenReturn(appRecognitionVerdict);

        TokenPayloadExternal payload = mock(TokenPayloadExternal.class);
        when(payload.getRequestDetails()).thenReturn(requestDetails);
        when(payload.getAppIntegrity()).thenReturn(appIntegrity);
        when(payload.getDeviceIntegrity()).thenReturn(deviceIntegrity);

        DecodeIntegrityTokenResponse response = mock(DecodeIntegrityTokenResponse.class);
        when(response.getTokenPayloadExternal()).thenReturn(payload);
        return response;
    }

    private void invokeValidateIntegrityResponse(DecodeIntegrityTokenResponse response,
                                                 ClientAttestationContext context) throws Exception {

        Method method = AndroidAttestationValidator.class.getDeclaredMethod("validateIntegrityResponse",
                DecodeIntegrityTokenResponse.class, ClientAttestationContext.class);
        method.setAccessible(true);
        method.invoke(validator, response, context);
    }
}
