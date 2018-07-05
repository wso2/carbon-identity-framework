/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.directory.server.manager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.directory.server.manager.DirectoryServerApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;

/**
 * Service component class for the LDAP directory server management
 */
@Component(
        name = "identity.ldap.directory.component",
        immediate = true)
public class LDAPServiceComponent {

    private static Log log = LogFactory.getLog(LDAPServiceComponent.class);
    private static ServiceRegistration<?> serviceRegistration;

    @Activate
    protected static void activate(ComponentContext ctxt) {

        serviceRegistration = ctxt.getBundleContext().registerService(
                ApplicationMgtListener.class.getName(), new DirectoryServerApplicationMgtListener(), null);
        if (serviceRegistration != null) {
            if (log.isDebugEnabled()) {
                log.debug(" LDAP directory  - ApplicationMgtListener registered.");
            }
        } else {
            log.error(" LDAP directory  - ApplicationMgtListener could not be registered.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Identity LDAP directory mgt bundle is activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        ctxt.getBundleContext().ungetService(serviceRegistration.getReference());
        if (log.isDebugEnabled()) {
            log.info("Identity  LDAP directory mgt bundle is deactivated");
        }
    }
}
