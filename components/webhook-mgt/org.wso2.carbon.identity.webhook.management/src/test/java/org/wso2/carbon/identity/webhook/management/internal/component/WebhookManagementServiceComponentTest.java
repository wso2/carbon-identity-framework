/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.management.internal.component;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.handler.OrganizationEventSubscriptionEventHandler;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link WebhookManagementServiceComponent}.
 */
public class WebhookManagementServiceComponentTest {

    private WebhookManagementServiceComponent component;
    private ComponentContext componentContext;
    private BundleContext bundleContext;
    private EventAdapterMetadataService originalMetadataService;
    private Adapter originalAdapter;

    @BeforeMethod
    public void setUp() {

        component = new WebhookManagementServiceComponent();
        bundleContext = mock(BundleContext.class);
        componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        WebhookManagementComponentServiceHolder holder = WebhookManagementComponentServiceHolder.getInstance();
        originalMetadataService = holder.getEventAdapterMetadataService();
        originalAdapter = holder.getWebhookAdapter();
    }

    @AfterMethod
    public void tearDown() {

        // The holder is a singleton shared with the rest of the suite, so what was found is put back. The
        // adapter only when there was one: its setter dereferences the argument, so null cannot be passed.
        WebhookManagementComponentServiceHolder holder = WebhookManagementComponentServiceHolder.getInstance();
        holder.setEventAdapterMetadataService(originalMetadataService);
        if (originalAdapter != null) {
            holder.setWebhookAdapter(originalAdapter);
        }
    }

    /**
     * Activation resolves the adapter in use and registers the service, which is what makes the webhook APIs
     * reachable in the runtime.
     */
    @Test
    public void testActivateRegistersTheServiceAndResolvesTheAdapter() throws Exception {

        Adapter adapter = mock(Adapter.class);
        EventAdapterMetadataService metadataService = mock(EventAdapterMetadataService.class);
        when(metadataService.getCurrentActiveAdapter()).thenReturn(adapter);
        WebhookManagementComponentServiceHolder.getInstance().setEventAdapterMetadataService(metadataService);

        component.activate(componentContext);

        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getWebhookAdapter(), adapter);
        verify(bundleContext).registerService(eq(WebhookManagementService.class.getName()), any(), any());
        verify(bundleContext).registerService(eq(AbstractEventHandler.class.getName()),
                any(OrganizationEventSubscriptionEventHandler.class), any());
    }

    /**
     * Activation failure is logged rather than raised: a component that throws from activate leaves the
     * bundle in a state the runtime cannot report on.
     */
    @Test
    public void testActivateSurvivesAnAdapterThatCannotBeResolved() {

        WebhookManagementComponentServiceHolder.getInstance().setEventAdapterMetadataService(null);
        Adapter adapterBeforeActivation =
                WebhookManagementComponentServiceHolder.getInstance().getWebhookAdapter();

        component.activate(componentContext);

        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getWebhookAdapter(),
                adapterBeforeActivation);
        verify(bundleContext, never()).registerService(anyString(), any(), any());
    }

    @Test
    public void testDeactivate() {

        component.deactivate(componentContext);
    }

    @Test
    public void testOrganizationManagerIsBoundAndUnbound() {

        OrganizationManager organizationManager = mock(OrganizationManager.class);

        component.setOrganizationManager(organizationManager);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getOrganizationManager(),
                organizationManager);

        component.unsetOrganizationManager(organizationManager);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getOrganizationManager());
    }

    @Test
    public void testResourceSharingPolicyHandlerServiceIsBoundAndUnbound() {

        ResourceSharingPolicyHandlerService sharingService = mock(ResourceSharingPolicyHandlerService.class);

        component.setResourceSharingPolicyHandlerService(sharingService);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getResourceSharingPolicyHandlerService(),
                sharingService);

        component.unsetResourceSharingPolicyHandlerService(sharingService);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getResourceSharingPolicyHandlerService());
    }

    @Test
    public void testTopicManagementServiceIsBoundAndUnbound() {

        TopicManagementService topicManagementService = mock(TopicManagementService.class);

        component.setTopicManagementService(topicManagementService);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService(),
                topicManagementService);

        component.unsetTopicManagementService(topicManagementService);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService());
    }

    @Test
    public void testSubscriptionManagementServiceIsBoundAndUnbound() {

        SubscriptionManagementService subscriptionManagementService = mock(SubscriptionManagementService.class);

        component.setSubscriptionManagementService(subscriptionManagementService);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getSubscriptionManagementService(),
                subscriptionManagementService);

        component.unsetSubscriptionManagementService(subscriptionManagementService);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getSubscriptionManagementService());
    }

    @Test
    public void testWebhookMetadataServiceIsBoundAndUnbound() {

        WebhookMetadataService webhookMetadataService = mock(WebhookMetadataService.class);

        component.setWebhookMetadataService(webhookMetadataService);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getWebhookMetadataService(),
                webhookMetadataService);

        component.unsetWebhookMetadataService(webhookMetadataService);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getWebhookMetadataService());
    }

    @Test
    public void testEventAdapterMetadataServiceIsBoundAndUnbound() {

        EventAdapterMetadataService metadataService = mock(EventAdapterMetadataService.class);

        component.setEventAdapterMetadataService(metadataService);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getEventAdapterMetadataService(),
                metadataService);

        component.unsetEventAdapterMetadataService(metadataService);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getEventAdapterMetadataService());
    }

    @Test
    public void testSecretManagerIsBoundAndUnbound() {

        SecretManager secretManager = mock(SecretManager.class);

        component.setSecretManager(secretManager);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getSecretManager(), secretManager);

        component.unsetSecretManager(secretManager);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getSecretManager());
    }

    @Test
    public void testSecretResolveManagerIsBoundAndUnbound() {

        SecretResolveManager secretResolveManager = mock(SecretResolveManager.class);

        component.setSecretResolveManager(secretResolveManager);
        assertEquals(WebhookManagementComponentServiceHolder.getInstance().getSecretResolveManager(),
                secretResolveManager);

        component.unsetSecretResolveManager(secretResolveManager);
        assertNull(WebhookManagementComponentServiceHolder.getInstance().getSecretResolveManager());
    }
}
