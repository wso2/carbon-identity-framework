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

package org.wso2.carbon.identity.api.resource.collection.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.api.resource.collection.mgt.APIResourceCollectionManager;
import org.wso2.carbon.identity.api.resource.collection.mgt.APIResourceCollectionManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;

/**
 * Service component for the API resource management.
 */
@Component(
        name = "api.resource.collection.mgt.service.component",
        immediate = true
)
public class APIResourceCollectionMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(APIResourceCollectionMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(APIResourceCollectionManager.class,
                    APIResourceCollectionManagerImpl.getInstance(), null);

            LOG.debug("API resource collection management bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing API resource collection management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(APIResourceCollectionManager.class));
            LOG.debug("API resource collection management bundle is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating API resource collection management component.", e);
        }
    }

    @Reference(
            name = "apiResourceManagementService",
            service = APIResourceManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIResourceManagementService"
    )
    protected void setAPIResourceManagementService(APIResourceManager apiResourceManagementService) {

        APIResourceCollectionMgtServiceDataHolder.getInstance()
                .setAPIResourceManagementService(apiResourceManagementService);
    }

    protected void unsetAPIResourceManagementService(APIResourceManager apiResourceManagementService) {

        APIResourceCollectionMgtServiceDataHolder.getInstance().setAPIResourceManagementService(null);
    }
}
