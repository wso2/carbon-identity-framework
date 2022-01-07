/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.branding.preference.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.branding.preference.mgt.BrandingPreferenceManager;
import org.wso2.carbon.identity.branding.preference.mgt.BrandingPreferenceManagerImpl;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;

/**
 * OSGi declarative services component which handles registration and un-registration of branding preference management
 * service.
 */
@Component(
        name = "branding.preference.mgt.component",
        immediate = true
)
public class BrandingPreferenceManagerComponent {

    private static final Log log = LogFactory.getLog(BrandingPreferenceManagerComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext()
                .registerService(BrandingPreferenceManager.class, new BrandingPreferenceManagerImpl(), null);

        log.info("BrandingPreferenceMgtServiceComponent is activated.");
    }

    @Reference(
            name = "resource.configuration.manager.service",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager"
    )

    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        BrandingPreferenceManagerComponentDataHolder.getInstance().setConfigurationManager(configurationManager);
        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationManager.");
        }
    }

    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        BrandingPreferenceManagerComponentDataHolder.getInstance().setConfigurationManager(null);
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationManager.");
        }
    }
}
