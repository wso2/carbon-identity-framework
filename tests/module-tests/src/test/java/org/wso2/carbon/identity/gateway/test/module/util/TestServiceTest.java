package org.wso2.carbon.identity.gateway.test.module.util;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Tests the TestService.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class TestServiceTest {

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
    public void testSampleProtocol() {
        try {
            HttpURLConnection urlConnection = GatewayTestUtils.request("http://localhost:8080/gateway?sampleProtocol=true",
                    HttpMethod.GET, false);
            String locationHeader = GatewayTestUtils.getResponseHeader("location", urlConnection);
            Assert.assertTrue(locationHeader.contains("RelayState"));
            Assert.assertTrue(locationHeader.contains("externalIDP"));


            String relayState = locationHeader.split("RelayState=")[1];
            relayState = relayState.split("&")[0];
            System.out.println(relayState);

            urlConnection = GatewayTestUtils.request
                    ("http://localhost:8080/gateway?RelayState=" + relayState + "&Assertion=" +
                            "ExternalAuthenticatedUser",HttpMethod.GET, false);

            locationHeader = GatewayTestUtils.getResponseHeader("location", urlConnection);
            Assert.assertTrue(locationHeader.contains("/response"));
            Assert.assertTrue(locationHeader.contains("authenticatedUser=ExternalAuthenticatedUser"));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
