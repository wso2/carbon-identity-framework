/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.onboard.core.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.user.onboard.core.service.UserOnboardCoreService;
import org.wso2.carbon.identity.user.onboard.core.service.UserOnboardCoreServiceImpl;

/**
 * Service component class of the user onboard manager core service.
 */
@Component(
        name = "org.wso2.carbon.identity.api.user.onboard.core.service.component",
        immediate = true
)
public class ServiceComponent {

    private static final Log LOG = LogFactory.getLog(ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            componentContext.getBundleContext().registerService(UserOnboardCoreService.class.getName(),
                    new UserOnboardCoreServiceImpl(), null);
            LOG.info("User onboard api core service component activated successfully.");
        } catch (Throwable throwable) {
            LOG.error("Failed to activate the User onboard api core service component.", throwable);
        }
    }
}
