/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.api.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.FrameworkLoginResponseFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponseFactory;


@Component(
        name = "org.wso2.carbon.identity.gateway.api.internal.GatewayResourceAPIComponent",
        immediate = true
)
public class GatewayResourceAPIComponent {

    private Logger log = LoggerFactory.getLogger(GatewayResourceAPIComponent.class);


    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        bundleContext.registerService(HttpIdentityRequestFactory.class, new HttpIdentityRequestFactory(), null);
        bundleContext.registerService(HttpIdentityResponseFactory.class, new FrameworkLoginResponseFactory(), null);


        //FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);


        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    @Deactivate
    protected void stop() throws Exception {

    }






}
