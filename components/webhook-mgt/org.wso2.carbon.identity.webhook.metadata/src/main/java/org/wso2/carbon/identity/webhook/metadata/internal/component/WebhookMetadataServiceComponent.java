/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedWebhookMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.WebhookMetadataServiceImpl;

/**
 * Service component for the webhook metadata service.
 */
@Component(
        name = "webhook.metadata.service.component",
        immediate = true
)
public class WebhookMetadataServiceComponent {

    private static final Log LOG = LogFactory.getLog(WebhookMetadataServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            // Get the singleton instance of the DAO
            FileBasedWebhookMetadataDAOImpl webhookMetadataDAO = FileBasedWebhookMetadataDAOImpl.getInstance();
            WebhookMetadataService webhookMetadataService = new WebhookMetadataServiceImpl(webhookMetadataDAO);

            // Register the webhook metadata service
            bundleCtx.registerService(WebhookMetadataService.class.getName(), webhookMetadataService, null);

            LOG.debug("Webhook metadata bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing webhook metadata service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(WebhookMetadataService.class));
            LOG.debug("Webhook metadata bundle is deactivated.");
        } catch (Throwable e) {
            LOG.error("Error while deactivating webhook metadata service component.", e);
        }
    }
}
