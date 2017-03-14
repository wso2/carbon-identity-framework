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
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;


@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)

public class GatewayAuthenticatorTests {

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticatorTests.class);

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

    /**
     * Test a sample local authenticator
     */
    @Test
    public void testLocalAuthenticatorInit() {

        LocalApplicationAuthenticator localApplicationAuthenticator = new LocalApplicationAuthenticator() {
            @Override
            public boolean canHandle(AuthenticationContext authenticationContext) {
                if (authenticationContext.getParameter("localAuth") != null) {
                    return true;
                }
                return false;
            }

            @Override
            public AuthenticationResponse process(AuthenticationContext authenticationContext) throws
                    AuthenticationHandlerException {
                return new AuthenticationResponse(AuthenticationResponse.Status.AUTHENTICATED);
            }

            @Override
            public String getName() {
                return "SampleLocalAuthenticator";
            }

            @Override
            public String getFriendlyName() {
                return "SampleLocalAuthenticator";
            }

        };
        Assert.assertEquals(localApplicationAuthenticator.getName(), "SampleLocalAuthenticator");
        AuthenticationContext authenticationContext = new AuthenticationContext(null);
        authenticationContext.addParameter("localAuth", true);
        Assert.assertTrue(localApplicationAuthenticator.canHandle(authenticationContext));
    }



    /**
     * Test a sample request path authenticator
     */
    @Test
    public void testRequestPathAuthenticatorInit() {
        RequestPathApplicationAuthenticator localApplicationAuthenticator = new RequestPathApplicationAuthenticator() {
            @Override
            public boolean canHandle(AuthenticationContext authenticationContext) {
                if (authenticationContext.getParameter("requestPath") != null) {
                    return true;
                }
                return false;
            }

            @Override
            public AuthenticationResponse process(AuthenticationContext authenticationContext) throws AuthenticationHandlerException {
                return new AuthenticationResponse(AuthenticationResponse.Status.AUTHENTICATED);
            }

            @Override
            public String getName() {
                return "SampleRequestPathAuthenticator";
            }

            @Override
            public String getFriendlyName() {
                return "SampleRequestPathAuthenticator";
            }



        };
        Assert.assertEquals(localApplicationAuthenticator.getName(), "SampleRequestPathAuthenticator");
        AuthenticationContext authenticationContext = new AuthenticationContext(null);
        authenticationContext.addParameter("requestPath", true);
        Assert.assertTrue(localApplicationAuthenticator.canHandle(authenticationContext));
    }

}
