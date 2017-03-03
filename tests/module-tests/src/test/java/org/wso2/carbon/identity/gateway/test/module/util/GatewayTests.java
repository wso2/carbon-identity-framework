package org.wso2.carbon.identity.gateway.test.module.util;

import com.google.common.net.HttpHeaders;
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
import org.wso2.carbon.identity.gateway.common.util.Utils;
import org.wso2.carbon.identity.gateway.deployer.IdentityProviderDeployer;
import org.wso2.carbon.identity.gateway.deployer.ServiceProviderDeployer;
import org.wso2.carbon.identity.gateway.service.GatewayClaimResolverService;
import org.wso2.carbon.identity.gateway.store.IdentityProviderConfigStore;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;
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
 * Tests the TestService.
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
    public void testFederatedAuthentication() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request(GatewayTestConstants.GATEWAY_ENDPOINT + "?" +
                    GatewayTestConstants.SAMPLE_PROTOCOL + "=true", HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader(HttpHeaders.LOCATION, urlConnection);
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.RELAY_STATE));
            Assert.assertTrue(locationHeader.contains(GatewayTestConstants.EXTERNAL_IDP));

            String relayState = locationHeader.split(GatewayTestConstants.RELAY_STATE + "=")[1];
            relayState = relayState.split(GatewayTestConstants.QUERY_PARAM_SEPARATOR)[0];
            System.out.println(relayState);

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
            log.error("Error while running federated authentication test case", e);
        }
    }


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

    @Test
    public void testUtils() {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put("param1", new String[]{"value1", "value2"});
        map.put("param1", new String[]{"value3", "value4"});
        String queryString = Utils.buildQueryString(map);
        Assert.assertTrue(queryString.contains("?param1=value3&param1=value4"));
    }

    @Test
    public void testClaimService() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://org.harsha/claims", "http://org.harsha/claims/email", "harsha@wso2.com");
        claims.add(claim);
        Set<Claim> transformedClaims = claimResolverService.transformToNativeDialect(claims, "http://org.harsha/claims",
                Optional.<String>empty());
        Assert.assertTrue(!transformedClaims.isEmpty());
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://wso2.org/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://wso2.org/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    @Test
    public void testClaimServiceTransformToCustomDialect() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "harsha@wso2.com");
        claims.add(claim);
        Set<Claim> transformedClaims = claimResolverService.transformToOtherDialect(claims, "http://org.harsha/claims",
                Optional.<String>empty());
        Assert.assertTrue(!transformedClaims.isEmpty());
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://org.harsha/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://org.harsha/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    @Test
    public void testClaimServiceTransformToCustomDialectWithProfile() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", "harsha@wso2.com");
        claims.add(claim);
        Set<Claim> transformedClaims = claimResolverService.transformToOtherDialect(claims, "http://org.harsha/claims",
                Optional.of("default"));
        Assert.assertTrue(!transformedClaims.isEmpty());
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://org.harsha/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://org.harsha/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    @Test
    public void testClaimServiceWithProfile() {
        GatewayClaimResolverService claimResolverService = this.bundleContext.getService(bundleContext
                .getServiceReference(GatewayClaimResolverService.class));
        Set<Claim> claims = new HashSet<Claim>();
        Claim claim = new Claim("http://org.harsha/claims", "http://org.harsha/claims/email", "harsha@wso2.com");
        claims.add(claim);
        Set<Claim> transformedClaims = claimResolverService.transformToNativeDialect(claims, "http://org.harsha/claims",
                Optional.of("default"));
        Assert.assertTrue(!transformedClaims.isEmpty());
        Claim responseClaim = transformedClaims.iterator().next();
        Assert.assertEquals("http://wso2.org/claims/email", responseClaim.getClaimUri());
        Assert.assertEquals("http://wso2.org/claims", responseClaim.getDialectUri());
        Assert.assertEquals("harsha@wso2.com", responseClaim.getValue());
    }

    @Test
    public void testIdentityProviderUpdate() {
        IdentityProviderDeployer identityProviderDeployer = new IdentityProviderDeployer();
        Artifact artifact = new Artifact(new File(GatewayOSGiTestUtils.getCarbonHome() + File.separator +
                "deployment" + File.separator + "identityprovider" + File.separator + "myidp_dummy.yaml"));
        artifact.setType(new ArtifactType("identityprovider"));
        try {
            identityProviderDeployer.update(artifact);
        } catch (CarbonDeploymentException e) {
            Assert.fail();
        }
    }

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

    @Test
    public void testIdentityProviderUnDeploy() {
        IdentityProviderDeployer identityProviderDeployer = new IdentityProviderDeployer();
        try {
            identityProviderDeployer.undeploy("myidp_dummy.yaml");
        } catch (CarbonDeploymentException e) {
            Assert.fail();
        }
    }

    @Test
    public void testServiceProviderUnDeploy() {
        ServiceProviderDeployer serviceProviderDeployer = new ServiceProviderDeployer();
        try {
            serviceProviderDeployer.undeploy("sample_dummy.yaml");
        } catch (CarbonDeploymentException e) {
            Assert.fail();
        }
    }


}
