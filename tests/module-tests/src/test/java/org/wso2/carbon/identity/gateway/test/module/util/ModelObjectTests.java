
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
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.List;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)

/**
 * To test model objects
 */
public class ModelObjectTests {

    private static final Logger log = LoggerFactory.getLogger(GatewayDAOTests.class);

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
     * Test the content of deployed service provider
     */
    @Test
    public void testSPYAMLValidation() {
        ServiceProviderConfigStore serviceProviderConfigStore = this.bundleContext.getService(bundleContext
                .getServiceReference(ServiceProviderConfigStore.class));
        Assert.assertNotNull(serviceProviderConfigStore);
        ServiceProviderConfig serviceProviderConfig = serviceProviderConfigStore.getServiceProvider
                (GatewayTestConstants.SAMPLE_ISSUER_NAME);
        Assert.assertNotNull(serviceProviderConfig);
        Assert.assertNotNull(serviceProviderConfig.getClaimConfig());
        Assert.assertNotNull(serviceProviderConfig.getClaimConfig().getDialectUri());
        Assert.assertNotNull(serviceProviderConfig.getClaimConfig().getProfile());
        Assert.assertNotNull(serviceProviderConfig.getClaimConfig().getSubjectClaimUri());
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig());
        Assert.assertNotNull(serviceProviderConfig.getRequestValidationConfig().getRequestValidatorConfigs().get(0)
                .getProperties());
        Assert.assertNotNull(serviceProviderConfig.getRequestValidationConfig().getRequestValidatorConfigs().get(0)
                .getType());
        Assert.assertNotNull(serviceProviderConfig.getRequestValidationConfig().getRequestValidatorConfigs().get(0)
                .getUniquePropertyName());
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig());
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0).getAuthStrategy());
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0));
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0).getAuthenticatorName());
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0).getIdentityProviderConfig().getProvisioningConfig()
                .getJitProvisioningConfig().getProvisioningIdPs());

        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0).getIdentityProviderConfig().getProvisioningConfig()
                .getProvisionerConfigs().get(0).getProperties());

        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0).getIdentityProviderConfig().getProvisioningConfig()
                .getProvisioningClaimConfigs().get(0).getClaimId());
        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0).getIdentityProviderConfig().getProvisioningConfig()
                .getProvisioningClaimConfigs().get(0).getDefaultValue());

        Assert.assertNotNull(serviceProviderConfig.getAuthenticationConfig().getAuthenticationStepConfigs().get(0)
                .getIdentityProviders().get(0).getIdentityProviderConfig().getProvisioningConfig()
                .getProvisioningRoles().get(0));

    }

}
