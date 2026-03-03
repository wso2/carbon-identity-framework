/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.ai.service.mgt.token;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_TOKEN_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME;

/**
 * Test class for AIAccessTokenManager.
 */
public class AIAccessTokenManagerTest {

    private WireMockServer wireMockServer;
    private AIAccessTokenManager tokenManager;
    private MockedStatic<IdentityUtil> mockedStatic;

    @BeforeClass
    public void init() {

        mockedStatic = Mockito.mockStatic(IdentityUtil.class);
        mockedStatic.when(() -> IdentityUtil.getProperty(AI_TOKEN_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME))
                .thenReturn("2000");
    }

    @BeforeMethod
    public void setUp() throws Exception {

        // Reset the singleton instance.
        resetSingletonInstance(AIAccessTokenManager.class, "instance");
    }

    private void startWireMockServer() throws Exception {

        // Start WireMock server.
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();

        // Set the AI_TOKEN_ENDPOINT to WireMock's base URL.
        setStaticField(AIAccessTokenManager.class, "AI_TOKEN_ENDPOINT", wireMockServer.baseUrl() + "/token");
    }

    private void setAiServiceKey(String key) throws Exception {

        String aiServiceKey = Base64.getEncoder().encodeToString((key).getBytes());
        setStaticField(AIAccessTokenManager.class, "AI_KEY", aiServiceKey);
    }

    private void resetSingletonInstance(Class<?> clazz, String fieldName) throws Exception {

        java.lang.reflect.Field instanceField = clazz.getDeclaredField(fieldName);
        instanceField.setAccessible(true);
        instanceField.set(null, null); // Reset the static field to null
    }

    @Test
    public void testGetAccessTokenSuccess() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        // Mock a successful token response
        String expectedAccessToken = "mockedAccessToken";
        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"" + expectedAccessToken + "\"}")));

        String accessToken = tokenManager.getAccessToken(false);

        Assert.assertEquals(accessToken, expectedAccessToken, "Access token should match the mocked value.");
        Assert.assertEquals(tokenManager.getClientId(), "testClientId", "Client ID should match the mocked value.");
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testGetAccessTokenUnauthorized() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Unauthorized\"}")));

        tokenManager.getAccessToken(false);
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testGetAccessTokenServerError() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        // Arrange: Mock a 500 Internal Server Error response
        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        tokenManager.getAccessToken(false);
    }

    @Test
    public void testGetAccessTokenRenewal() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        // Arrange: Mock a successful token response for renewal.
        String newAccessToken = "newMockedAccessToken";
        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"" + newAccessToken + "\"}")));

        String accessToken = tokenManager.getAccessToken(true);

        Assert.assertEquals(accessToken, newAccessToken, "Access token should match the renewed mocked value.");
    }

    @Test
    public void testGetAccessTokenExistingTokenReturnsWithoutRenewal() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        // Arrange: Mock a successful token response.
        String existingAccessToken = "existingMockedAccessToken";
        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"" + existingAccessToken + "\"}")));

        // Act (1): First call to getAccessToken. This should fetch the token from the server.
        String firstCallToken = tokenManager.getAccessToken(false);
        Assert.assertEquals(firstCallToken, existingAccessToken,
                "First call should retrieve the newly obtained token.");

        // Reset WireMock’s request history to track subsequent calls.
        wireMockServer.resetRequests();

        // Act (2): Second call with renewAccessToken = false and an existing token.
        // This should NOT call the token endpoint again; it should return the cached token.
        String secondCallToken = tokenManager.getAccessToken(false);

        Assert.assertEquals(secondCallToken, existingAccessToken,
                "Second call should return the same token without making a new request.");
        // Verify that no new requests to the token endpoint were made after the first call.
        wireMockServer.verify(0, postRequestedFor(urlEqualTo("/token")));
    }

    @Test(expectedExceptions = AIServerException.class,
            expectedExceptionsMessageRegExp = "Failed to obtain access token after.*attempts.*")
    public void testGetAccessTokenMaxRetriesExceeded() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        // Stub the /token endpoint to always return a non-200 status (e.g., 500). This simulates repeated failures.
        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));
        tokenManager.getAccessToken(false);
    }

    @Test(expectedExceptions = AIServerException.class,
            expectedExceptionsMessageRegExp = "Error executing token request:.*")
    public void testGetAccessTokenIOException() throws Exception {

        startWireMockServer();
        setAiServiceKey("testClientId:testClientSecret");
        tokenManager = AIAccessTokenManager.getInstance();

        // Configure WireMock to cause a network-level fault that should trigger an IOException.
        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        // This simulates a situation where the connection is abruptly reset.
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // Act: When getAccessToken calls the endpoint, the client should throw an IOException,
        // causing the catch block to throw an AIServerException with "Error executing token request: ...".
        tokenManager.getAccessToken(false);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAIServiceKey() throws Exception {

        setAiServiceKey("invalidKey");

        // Act: Attempt to get the instance, which should throw an exception.
        AIAccessTokenManager.getInstance();
    }

    @Test
    public void testGetInstanceFirstTimeCreation() throws Exception {

        setAiServiceKey("testClientId:testClientSecret");
        setStaticField(AIAccessTokenManager.class, "AI_TOKEN_ENDPOINT", "http://localhost.com/token");
        AIAccessTokenManager firstCallInstance = AIAccessTokenManager.getInstance();
        Assert.assertNotNull(firstCallInstance, "First call to getInstance() should create a new instance.");
    }

    @Test
    public void testGetInstanceSubsequentCallsReturnSameInstance() throws Exception {

        setAiServiceKey("testClientId:testClientSecret");
        setStaticField(AIAccessTokenManager.class, "AI_TOKEN_ENDPOINT", "http://localhost.com/token");

        // Reset the singleton to ensure it's null, then create it once.
        AIAccessTokenManager firstCallInstance = AIAccessTokenManager.getInstance();

        AIAccessTokenManager secondCallInstance = AIAccessTokenManager.getInstance();

        // Verify that the second call did NOT re-create the object.
        Assert.assertNotNull(secondCallInstance, "Second call should still return an instance.");
        Assert.assertEquals(secondCallInstance, firstCallInstance,
                "Both calls should return the exact same singleton instance.");
    }

    /**
     * Set a static final field using reflection (compatible with Java 12+).
     * Uses Unsafe API to modify static final fields.
     *
     * @param clazz     The class containing the field.
     * @param fieldName The field name.
     * @param value     The value to set.
     * @throws Exception If an error occurs.
     */
    private void setStaticField(Class<?> clazz, String fieldName, String value) throws Exception {

        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        // Use Unsafe to modify static final fields in Java 12+
        java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Object fieldBase = unsafe.staticFieldBase(field);
        long fieldOffset = unsafe.staticFieldOffset(field);
        unsafe.putObject(fieldBase, fieldOffset, value);
    }

    @AfterMethod
    public void tearDown() {

        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @AfterClass
    public void destroy() {

        mockedStatic.close();
    }
}
