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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.EventAdapterMetadataServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.WebhookMetadataServiceImpl;

/**
 * OSGi service component for webhook metadata.
 */
@Component(
        name = "identity.webhook.metadata.component",
        immediate = true
)
public class WebhookMetadataServiceComponent {

    private static final Log log = LogFactory.getLog(WebhookMetadataServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            WebhookMetadataServiceImpl webhookMetadataService = WebhookMetadataServiceImpl.getInstance();
            webhookMetadataService.init();

            context.getBundleContext().registerService(WebhookMetadataService.class.getName(),
                    webhookMetadataService, null);

            EventAdapterMetadataServiceImpl eventAdapterMetadataService = EventAdapterMetadataServiceImpl.getInstance();
            eventAdapterMetadataService.init();

            context.getBundleContext().registerService(EventAdapterMetadataService.class.getName(),
                    eventAdapterMetadataService, null);

            log.debug("Webhook Metadata component activated successfully");
        } catch (Throwable e) {
            log.error("Error activating Webhook Metadata component", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Webhook Metadata component deactivated");
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        // This is used to guarantee that IdentityCore is properly initialized before using it
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        // No action needed
    }
}
