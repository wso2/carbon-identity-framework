/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.loader;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import static org.testng.Assert.assertNotNull;


@Test
public class UIBasedConfigurationLoaderTest extends AbstractFrameworkTest {

    private UIBasedConfigurationLoader loader = new UIBasedConfigurationLoader();

    public void testGetSequence_Deprecated() throws Exception {
        ServiceProvider testSp1 = new ServiceProvider();
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
        testSp1.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

        AuthenticationStep step1 = new AuthenticationStep();
        step1.setStepOrder(1);
        AuthenticationStep step2 = new AuthenticationStep();
        step1.setStepOrder(2);

        AuthenticationStep[] authenticationSteps = new AuthenticationStep[] { step1, step2 };

        localAndOutboundAuthenticationConfig.setAuthenticationSteps(authenticationSteps);

        SequenceConfig sequenceConfig = loader.getSequence(testSp1, "test_domain");
        assertNotNull(sequenceConfig);

        assertNotNull(sequenceConfig.getStepMap());

        assertNotNull(sequenceConfig.getStepMap().get(1));
        assertNotNull(sequenceConfig.getStepMap().get(2));
    }

}