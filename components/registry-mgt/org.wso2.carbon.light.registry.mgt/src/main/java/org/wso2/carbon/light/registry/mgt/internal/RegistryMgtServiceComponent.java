/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.light.registry.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.light.registry.mgt.service.LightRegistryMgtService;
import org.wso2.carbon.light.registry.mgt.service.LightRegistryMgtServiceImpl;

@Component(
        name = "registry.mgt.component",
        immediate = true
)
public class RegistryMgtServiceComponent {

    private static final Log log = LogFactory.getLog(RegistryMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        try {
            bundleContext.registerService(LightRegistryMgtService.class.getName(),
                    new LightRegistryMgtServiceImpl(), null);
            log.debug("Light registry management resource bundle is activated");
        } catch (Exception e) {
            log.error("Failed to activate light registry management bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("Light registry management resource bundle is deactivated");
    }
}
