/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.endpoint.util.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.endpoint.util.AdaptiveAuthUtil;
import org.wso2.carbon.identity.application.authentication.endpoint.util.MutualSSLManager;
import org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener for executing post deployment tasks of the authentication endpoint
 */
public class AuthenticationEndpointContextListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(AuthenticationEndpointContextListener.class);

    /**
     * Method for calling after context initialization
     *
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        // Initialize TenantDataManager for tenant domains dropdown feature in SSO login page
        if (log.isDebugEnabled()) {
            log.debug("Initializing TenantDataManager for tenant domains dropdown");
        }
        TenantDataManager.init();
        MutualSSLManager.init();
        AdaptiveAuthUtil.init();
    }

    /**
     * Method for calling after context destroy
     *
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        // Tasks to be done after context destroy can be written here
    }

}
