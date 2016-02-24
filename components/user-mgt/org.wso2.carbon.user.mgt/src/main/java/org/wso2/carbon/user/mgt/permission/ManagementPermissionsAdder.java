/*
 * Copyright (c) 2007 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;
import org.wso2.carbon.utils.component.xml.Component;
import org.wso2.carbon.utils.component.xml.ComponentConfigFactory;
import org.wso2.carbon.utils.component.xml.builder.ManagementPermissionsBuilder;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.net.URL;

/**
 * Adds management permissions declared in component.xml of each bundle.  
 */
public class ManagementPermissionsAdder implements BundleListener {
    private static final Log log = LogFactory.getLog(ManagementPermissionsAdder.class);

    @Override
    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            if (event.getType() == BundleEvent.STARTED) {
                addUIPermissionFromBundle(bundle);
            }
        } catch (Exception e) {
            log.error("Error occured when processing component xml in bundle " +
                    bundle.getSymbolicName(), e);
        }
    }

    public void addUIPermissionFromBundle(Bundle bundle) throws Exception {
        BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext == null) { // If the bundle got uninstalled, the bundleContext will be null
            return;
        }

        URL url = bundleContext.getBundle().getEntry("META-INF/component.xml");
        if (url == null) {
            return;
        }

        InputStream xmlStream = url.openStream();
        if (xmlStream == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Adding permissions in bundle" + 
                    bundle.getSymbolicName());
        }

        Component component = ComponentConfigFactory.build(xmlStream);
        ManagementPermission[] uiPermissions = null;
        if (component != null) {
            uiPermissions = (ManagementPermission[]) component
                    .getComponentConfig(ManagementPermissionsBuilder.LOCALNAME_MGT_PERMISSIONS);
        }

        if (uiPermissions != null) {
            // at the starup we are only adding permission only to tenant 0
            Registry registry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry();
            for (ManagementPermission uiPermission : uiPermissions) {
                if (registry.resourceExists(uiPermission.getResourceId())) {
                    continue;
                }
                Collection resource = registry.newCollection();
                resource.setProperty(UserMgtConstants.DISPLAY_NAME, uiPermission.getDisplayName());
                registry.put(uiPermission.getResourceId(), resource);
            }
        }
    }

}
