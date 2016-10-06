/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.endpoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.endpoint.auth.BasicAuthHandler;
import org.wso2.carbon.identity.entitlement.endpoint.auth.EntitlementAuthConfigReader;
import org.wso2.carbon.identity.entitlement.endpoint.auth.EntitlementAuthenticationHandler;
import org.wso2.carbon.identity.entitlement.endpoint.auth.EntitlementAuthenticatorRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;


/**
 * This performs one-time initialization tasks at the application startup.
 */
public class ApplicationInitializer implements ServletContextListener {

    private Log logger = LogFactory.getLog(ApplicationInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing Entitlement Webapp...");
        }
        try {
            //Initialize Authentication Registry
            initEntitlementAuthenticatorRegistry();
        } catch (Exception e) {
            logger.error("Error in initializing the Authentocators at the initialization of " +
                        "Entitlement webapp", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // Do nothing
    }

    private void initEntitlementAuthenticatorRegistry() {
        EntitlementAuthenticatorRegistry entitlementAuthRegistry = EntitlementAuthenticatorRegistry.getInstance();

        if (entitlementAuthRegistry != null) {
            //set authenticators after building auth config
            EntitlementAuthConfigReader configReader = new EntitlementAuthConfigReader();
            List<EntitlementAuthenticationHandler> entitlementAuthenticators
                                                    = configReader.buildEntitlementAuthenticators();

            if (entitlementAuthenticators != null && !entitlementAuthenticators.isEmpty()) {
                for (EntitlementAuthenticationHandler entitlementAuthenticator : entitlementAuthenticators) {
                    entitlementAuthRegistry.setAuthenticator(entitlementAuthenticator);
                }
            } else {
                //initialize default basic auth authenticator & OAuth authenticator and set it in the auth registry.
                BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
                basicAuthHandler.setDefaultPriority();
                entitlementAuthRegistry.setAuthenticator(basicAuthHandler);

            }
        }
    }
}
