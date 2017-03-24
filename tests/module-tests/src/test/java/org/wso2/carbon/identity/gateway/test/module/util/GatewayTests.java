/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.test.module.util;

import com.google.common.net.HttpHeaders;
import org.apache.commons.io.Charsets;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.common.util.Constants;
import org.wso2.carbon.identity.gateway.store.deployer.IdentityProviderDeployer;
import org.wso2.carbon.identity.gateway.store.deployer.ServiceProviderDeployer;
import org.wso2.carbon.identity.gateway.service.GatewayClaimResolverService;
import org.wso2.carbon.identity.gateway.store.IdentityProviderConfigStore;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * General Tests For Gateway.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class GatewayTests {

    private static final Logger log = LoggerFactory.getLogger(GatewayTests.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;


    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = GatewayOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(CoreOptions.systemProperty("java.security.auth.login.config")
                .value(Paths.get(GatewayOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }




    @Test
    public void testClientExceptionInProcessor() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true&generateClientException=true", HttpMethod.GET, false);
            int responseCode = urlConnection.getResponseCode();
            Assert.assertTrue(responseCode==500);

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    @Test
    public void testGatewayRuntimeExceptionInProcessor() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true&generateGatewayRuntimeException=true", HttpMethod.GET, false);
            int responseCode = urlConnection.getResponseCode();
            Assert.assertTrue(responseCode==500);

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }


    @Test
    public void testClientExceptionInCanHandleInResponse() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL +
                    "=true&generateGatewayRuntimeException=true&exceptionInCanHandle=true", HttpMethod
                    .GET, false);
            int responseCode = urlConnection.getResponseCode();
            Assert.assertTrue(responseCode==500);

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    @Test
    public void testRuntimeExceptionInCanHandleInResponse() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL +
                            "=true&generateClientException=true&exceptionInCanHandle=true", HttpMethod.GET,
                    false);
            int responseCode = urlConnection.getResponseCode();
            Assert.assertTrue(responseCode==500);

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    @Test
    public void testRuntimeExceptionInGatewayResource() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +"generateRuntimeException=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            int responseCode = urlConnection.getResponseCode();
            Assert.assertTrue(responseCode==500);

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }


    @Test
    public void testFederatedAuthenticationForMultiOption() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                            GatewayTestConstants.SAMPLE_PROTOCOL + "=true&uniqueID=travelocity-multi.com", HttpMethod.GET,
                    false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains("idplist"));
            Assert.assertTrue(locationHeader.contains("https://localhost:9292/gateway/endpoint"));

            String relayState = locationHeader.split("state=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?state="
                            + relayState + "&authenticator=SampleFederatedAuthenticator&idp=myidp", HttpMethod.GET, false);

            locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));

            relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?" + GatewayTestConstants.RELAY_STATE + "="
                            + relayState + GatewayTestConstants.QUERY_PARAM_SEPARATOR + GatewayTestConstants
                            .ASSERTION + "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME, HttpMethod.GET, false);

            locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RESPONSE_CONTEXT));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.AUTHENTICATED_USER +
                    "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME));
            String cookie = GatewayTestUtils.getResponseHeader(HttpHeaders.SET_COOKIE, urlConnection);
            Assert.assertNotNull(cookie);
            cookie = cookie.split(Constants.GATEWAY_COOKIE + "=")[1];
            Assert.assertNotNull(cookie);

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }


    /**
     * Testing overall federated authentication with sample protocol
     * Asserting on redirection and cookies we get
     */
    @Test
    public void testFederatedAuthentication() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));

            String relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?" + GatewayTestConstants.RELAY_STATE + "="
                            + relayState + GatewayTestConstants.QUERY_PARAM_SEPARATOR + GatewayTestConstants
                            .ASSERTION + "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME, HttpMethod.GET, false);

            locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RESPONSE_CONTEXT));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.AUTHENTICATED_USER +
                    "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME));
            String cookie = GatewayTestUtils.getResponseHeader(HttpHeaders.SET_COOKIE, urlConnection);
            Assert.assertNotNull(cookie);
            cookie = cookie.split(Constants.GATEWAY_COOKIE + "=")[1];
            Assert.assertNotNull(cookie);
        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    /**
     * Testing overall federated authentication with sample protocol and a post request
     * Asserting on redirection and cookies we get
     */
    @Test
    public void testFederatedAuthenticationWithPostRequest() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT,
                    HttpMethod.POST, true);
            String postData = GatewayTestConstants.SAMPLE_PROTOCOL + "=true";
            urlConnection.setDoOutput(true);
            urlConnection.getOutputStream().write(postData.toString().getBytes(Charsets.UTF_8));
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));

            String relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?" + GatewayTestConstants.RELAY_STATE + "="
                            + relayState + GatewayTestConstants.QUERY_PARAM_SEPARATOR + GatewayTestConstants
                            .ASSERTION + "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME, HttpMethod.GET, false);

            locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RESPONSE_CONTEXT));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.AUTHENTICATED_USER +
                    "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME));
            String cookie = GatewayTestUtils.getResponseHeader(HttpHeaders.SET_COOKIE, urlConnection);
            Assert.assertNotNull(cookie);
            cookie = cookie.split(Constants.GATEWAY_COOKIE + "=")[1];
            Assert.assertNotNull(cookie);
        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case with post body");
        }
    }


    /**
     * Testing overall federated authentication, while sending out response to federated request a wrong relay state
     * will be used
     * Asserting on errors we get.
     */
    @Test
    public void illegalRelayState() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));

            String relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?" + GatewayTestConstants.RELAY_STATE + "="
                            + relayState + "randomString" + GatewayTestConstants.QUERY_PARAM_SEPARATOR +
                            GatewayTestConstants
                                    .ASSERTION + "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME, HttpMethod.GET, false);

            Assert.assertEquals(500, urlConnection.getResponseCode());


        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    /**
     * Testing scenario where request factory Can handle fails with a client Exception.
     */
    @Test
    public void requestFactoryCanHandleClientFail() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                            GatewayTestConstants.SAMPLE_PROTOCOL + "=true" + GatewayTestConstants.QUERY_PARAM_SEPARATOR +
                            "canHandleErrorClient=true",
                    HttpMethod.GET, false);
            Assert.assertEquals(500, urlConnection.getResponseCode());

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }


    /**
     * Testing scenario where request factory Can handle fails with a server Exception.
     */
    @Test
    public void requestFactoryCanHandleServerFail() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                            GatewayTestConstants.SAMPLE_PROTOCOL + "=true" + GatewayTestConstants.QUERY_PARAM_SEPARATOR +
                            "canHandleErrorServer=true",
                    HttpMethod.GET, false);
            Assert.assertEquals(500, urlConnection.getResponseCode());

        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    /**
     * Tests for a scenario where processResponse will throw an exception from the federated authenticator.
     */
    @Test
    public void failFederatedProcessResponse() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));

            String relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?" + GatewayTestConstants.RELAY_STATE + "="
                            + relayState + GatewayTestConstants.QUERY_PARAM_SEPARATOR +
                            GatewayTestConstants
                                    .ASSERTION + "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME +
                            GatewayTestConstants.QUERY_PARAM_SEPARATOR + "validation=false", HttpMethod
                            .GET, false);

            Assert.assertEquals(500, urlConnection.getResponseCode());


        } catch (IOException e) {
            Assert.fail("Error while running federated authentication test case", e);
        }
    }

    /**
     * Request validation failure response test case.
     */
    @Test
    public void testRequestValidationFailure() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true&NotProtocolCompliant=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertNull(locationHeader);
            Assert.assertEquals(urlConnection.getResponseCode(), 500);
        } catch (IOException e) {
            log.error("Error while running federated authentication test case", e);
        }
    }

    /**
     * A request which is not getting picked by any of the requestFactories.
     */
    @Test
    public void testNonExistingProtocolRequest() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.NON_EXISTING_PROTOCOL + "=true&NotProtocolCompliant=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertNull(locationHeader);
            Assert.assertTrue(urlConnection.getResponseCode() == 500);
        } catch (IOException e) {
            log.error("Error while running federated authentication test case", e);
        }
    }


    /**
     * Test authentication with cookie after successfully authenticating at the first time.
     */
    @Test
    public void testSingleSignOnWithCookie() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));


            String relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];

            urlConnection = GatewayTestUtils.request
                    (GatewayTestConstants.GATEWAY_ENDPOINT + "?" + GatewayTestConstants.RELAY_STATE + "=" + relayState +
                            "&" + GatewayTestConstants.ASSERTION + "=" +
                            GatewayTestConstants.AUTHENTICATED_USER_NAME, HttpMethod.GET, false);

            String cookie = GatewayTestUtils.getResponseHeader(HttpHeaders.SET_COOKIE, urlConnection);
            cookie = cookie.split(Constants.GATEWAY_COOKIE + "=")[1];

            urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true", HttpMethod.GET, false);

            urlConnection.setRequestProperty(HttpHeaders.COOKIE, Constants.GATEWAY_COOKIE + "=" + cookie);
            locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.AUTHENTICATED_USER +
                    "=" + GatewayTestConstants.AUTHENTICATED_USER_NAME));

        } catch (IOException e) {
            log.error("Error while running sso cookie authentication test case", e);
        }
    }

    /**
     * Test the content of deployed SP yaml file.
     */
    @Test
    public void testSPYAMLValidation() {
        ServiceProviderConfigStore serviceProviderConfigStore = this.bundleContext.getService(bundleContext
                .getServiceReference(ServiceProviderConfigStore.class));
        Assert.assertNotNull(serviceProviderConfigStore);
        ServiceProviderConfig serviceProviderConfig = serviceProviderConfigStore.getServiceProvider
                (GatewayTestConstants.SAMPLE_ISSUER_NAME);
        Assert.assertNotNull(serviceProviderConfig);
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig());
        Assert.assertNotNull(serviceProviderConfig.getClaimConfig());
        Assert.assertEquals(GatewayTestConstants.SAMPLE_SP_NAME, serviceProviderConfig.getName());

    }

    /**
     * Tests for the content of deployed IDP yaml file.
     */
    @Test
    public void testIDPYAMLValidation() {
        IdentityProviderConfigStore identityProviderConfigStore = this.bundleContext.getService(bundleContext
                .getServiceReference(IdentityProviderConfigStore.class));
        Assert.assertNotNull(identityProviderConfigStore);
        IdentityProviderConfig identityProviderConfig = identityProviderConfigStore.getIdentityProvider("myidp");
        Assert.assertNotNull(identityProviderConfig.getAuthenticationConfig());
        Assert.assertNotNull(identityProviderConfig.getIdpMetaData());
        Assert.assertNotNull(identityProviderConfig.getProvisioningConfig());
        Assert.assertEquals(identityProviderConfig.getName(), "myidp");
    }

    /**
     * Test util method to build query string.
     */
    @Test
    public void testUtils() {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put("param1", new String[]{"value1", "value2"});
        map.put("param1", new String[]{"value3", "value4"});
        String queryString = GatewayUtil.buildQueryString(map);
        Assert.assertTrue(queryString.contains("?param1=value3&param1=value4"));
    }

    /**
     * Testing gateway claim resolving service. Transforming another dialect to a root dialect
     */
    @Test
    public void testClaimServiceTransformToNativeDialect() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://org.sample.idp/claims", "http://org.sample.idp/claims/email", "harsha@wso2.com");
        claims.add(claim);
        Set<Claim> transformedClaims = claimResolverService.transformToNativeDialect(claims, "http://org.sample.idp/claims",
                Optional.<String>empty());
        Assert.assertTrue(!transformedClaims.isEmpty());
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://wso2.org/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://wso2.org/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    /**
     * Testing gateway claim resolving service. Transforming from root dielect to root dialect.
     */
    @Test
    public void testClaimServiceTransformToCustomDialect() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "harsha@wso2.com");
        claims.add(claim);
        Set<Claim> transformedClaims = claimResolverService.transformToOtherDialect(claims, "http://org.sample.idp/claims",
                Optional.<String>empty());
        Assert.assertTrue(!transformedClaims.isEmpty());
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://org.sample.idp/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://org.sample.idp/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    /**
     * Testing gateway claim resolving service. Transforming to other dialect with a profile.
     */
    @Test
    public void testClaimServiceTransformToCustomDialectWithProfile() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "harsha@wso2.com");
        claims.add(claim);

        Claim claim2 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/customclaim", "customValue");
        claims.add(claim2);

        Set<Claim> transformedClaims = claimResolverService.transformToOtherDialect(claims, "http://org.sample.idp/claims",
                Optional.of("default"));
        Assert.assertTrue(!transformedClaims.isEmpty());
        Assert.assertTrue(transformedClaims.size() == 1);
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://org.sample.idp/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://org.sample.idp/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    /**
     * Testing gateway claim resolving service. Transforming to native dialect with a profile.
     */
    @Test
    public void testClaimServiceTransformToNativeDialectWithProfile() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://org.sample.idp/claims", "http://org.sample.idp/claims/email", "harsha@wso2" +
                ".com");
        claims.add(claim);
        Claim claim2 = new Claim("http://org.sample.idp/claims", "http://org.sample.idp/claims/customClaim" ,
                "customValue");
        claims.add(claim2);
        Set<Claim> transformedClaims = claimResolverService.transformToNativeDialect(claims, "http://org.sample.idp/claims",
                Optional.of("default"));
        Assert.assertTrue(!transformedClaims.isEmpty());
        Assert.assertTrue(transformedClaims.size() == 1);
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://wso2.org/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://wso2.org/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    /**
     * Update identity provider artifact and see whether it is getting deployed without error.
     */
    @Test
    public void testIdentityProviderUpdate() {
        IdentityProviderDeployer identityProviderDeployer = new IdentityProviderDeployer();
        Artifact artifact = new Artifact(new File(GatewayOSGiTestUtils.getCarbonHome() + File.separator +
                "deployment" + File.separator + "identityprovider" + File.separator + "myidp_dummy.yaml"));
        artifact.setType(new ArtifactType("identityprovider"));
        try {
            identityProviderDeployer.update(artifact);
        } catch (CarbonDeploymentException e) {
            Assert.fail("Error while running update identity provider test case");
        }
    }

    /**
     * Update identity provider artifact and see whether it is getting deployed without error.
     */
    @Test
    public void testServiceProviderUpdate() {
        ServiceProviderDeployer serviceProviderDeployer = new ServiceProviderDeployer();
        Artifact artifact = new Artifact(new File(GatewayOSGiTestUtils.getCarbonHome() + File.separator +
                "deployment" + File.separator + "serviceprovider" + File.separator + "sample_dummy.yaml"));
        artifact.setType(new ArtifactType("serviceprovider"));
        try {
            serviceProviderDeployer.update(artifact);
        } catch (CarbonDeploymentException e) {
            Assert.fail();
        }
    }

    /**
     * Update a non existing service provider.
     */
    @Test
    public void testWrongServiceProviderUpdate() {
        ServiceProviderDeployer serviceProviderDeployer = new ServiceProviderDeployer();
        Artifact artifact = new Artifact(new File(GatewayOSGiTestUtils.getCarbonHome() + File.separator +
                "deployment" + File.separator + "serviceprovider" + File.separator + "sample_dummy_nonExisting.yaml"));
        artifact.setType(new ArtifactType("serviceprovider"));
        try {
            serviceProviderDeployer.update(artifact);
            Assert.fail("An error should occur while trying to update wrong artifact");
        } catch (CarbonDeploymentException e) {
            log.info("failed service provider deployment from non existing file");
        }
    }

    /**
     * Un-deploy service provider.
     */
    @Test
    public void testIdentityProviderUnDeploy() {
        IdentityProviderDeployer identityProviderDeployer = new IdentityProviderDeployer();
        try {
            identityProviderDeployer.undeploy("myidp_dummy.yaml");
        } catch (CarbonDeploymentException e) {
            Assert.fail("An error occured while un-deploying sidentity provider");
        }
    }

    /**
     * Trying to update non existing identity provider
     */
    @Test
    public void testWrongIdentityProviderUpdate() {
        IdentityProviderDeployer identityProviderDeployer = new IdentityProviderDeployer();
        Artifact artifact = new Artifact(new File(GatewayOSGiTestUtils.getCarbonHome() + File.separator +
                "deployment" + File.separator + "identityprovider" + File.separator + "myidp_dummy_non_existing.yaml"));
        artifact.setType(new ArtifactType("identityprovider"));
        try {
            identityProviderDeployer.update(artifact);
            Assert.fail("Expected an error while updating non existing identity provider");
        } catch (CarbonDeploymentException e) {
            log.info("Non existing idp deployment failed");
        }
    }

    /**
     * Un-deploy a service provider
     */
    @Test
    public void testServiceProviderUnDeploy() {
        ServiceProviderDeployer serviceProviderDeployer = new ServiceProviderDeployer();
        try {
            serviceProviderDeployer.undeploy("sample_dummy.yaml");
        } catch (CarbonDeploymentException e) {
            Assert.fail("Error occured while un-deploying service provider artifact");
        }
    }



}
