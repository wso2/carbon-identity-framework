/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.common.internal;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.util.IdentityUtilService;


/**
 * Identity provider service component.
 */

@Component(
        name = "identity.util.dscomponent",
        immediate = true)
public class IdentityCommonServiceComponent {

    private static Logger logger = LoggerFactory.getLogger(IdentityCommonServiceComponent.class);

    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            // preload configurations
            org.wso2.carbon.identity.common.internal.config.ConfigParser.getInstance();

            serviceRegistration = componentContext.getBundleContext().registerService(IdentityUtilService.class
                    .getName(), new IdentityUtilServiceImpl(), null);

        } catch (Throwable e) {
            logger.error("Error while initiating IdentityMgtService.", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Identity Management Listener is enabled.");
        }
    }


    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Identity Management bundle is deactivated.");
        }
    }
}
