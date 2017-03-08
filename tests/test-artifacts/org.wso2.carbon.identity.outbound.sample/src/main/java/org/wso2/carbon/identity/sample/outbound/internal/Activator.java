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

package org.wso2.carbon.identity.sample.outbound;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;
import org.wso2.carbon.identity.gateway.authentication.authenticator.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.sample.outbound.authenticator.SampleFederatedAuthenticator;
import org.wso2.carbon.identity.sample.outbound.request.SampleACSRequestBuilderFactory;
import org.wso2.carbon.identity.sample.outbound.response.ACSRequestResponseBuilderFactory;

@Component(
        name = "org.wso2.carbon.identity.sample.outbound.component",
        immediate = true
)
public class Activator implements BundleActivator {

    private Logger log = LoggerFactory.getLogger(Activator.class);

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        try {

            bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                    new SampleFederatedAuthenticator(), null);
            bundleContext.registerService(AbstractApplicationAuthenticator.class,
                    new SampleFederatedAuthenticator(), null);
            bundleContext.registerService(GatewayRequestBuilderFactory.class, new SampleACSRequestBuilderFactory(), null);
            bundleContext.registerService(GatewayResponseBuilderFactory.class, new ACSRequestResponseBuilderFactory(), null);

        } catch (Throwable e) {
            System.out.println("Error while activating sample outbound component");
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
