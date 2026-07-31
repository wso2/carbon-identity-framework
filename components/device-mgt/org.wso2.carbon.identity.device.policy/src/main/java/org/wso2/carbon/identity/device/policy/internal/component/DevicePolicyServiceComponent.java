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

package org.wso2.carbon.identity.device.policy.internal.component;

import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DeviceDataResolver;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.api.service.DeviceTokenService;
import org.wso2.carbon.identity.device.policy.internal.cleanup.DeviceTokenJtiCleanupService;
import org.wso2.carbon.identity.device.policy.internal.js.DevicePolicyJsFunction;
import org.wso2.carbon.identity.device.policy.internal.resolver.DeviceDataResolverImpl;
import org.wso2.carbon.identity.device.policy.internal.rule.DevicePolicyEvaluationDataProvider;
import org.wso2.carbon.identity.device.policy.internal.service.IntegrityDataEnricher;
import org.wso2.carbon.identity.device.policy.internal.service.impl.DevicePolicyEvaluatorImpl;
import org.wso2.carbon.identity.device.policy.internal.service.impl.DeviceTokenServiceImpl;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;

/**
 * OSGi DS component for the device policy bundle.
 * Registers DevicePolicyEvaluator, RuleEvaluationDataProvider,
 * and the isDevicePolicyCompliant JS function.
 */
@Component(
        name = "device.policy.service.component",
        immediate = true
)
public class DevicePolicyServiceComponent {

    private static final Log LOG = LogFactory.getLog(DevicePolicyServiceComponent.class);
    private static final String JTI_CLEANUP_ENABLE_PROPERTY =
            "JDBCPersistenceManager.DeviceTokenJtiCleanUp.Enable";
    private static final String JTI_CLEANUP_PERIOD_PROPERTY =
            "JDBCPersistenceManager.DeviceTokenJtiCleanUp.CleanUpPeriod";
    private static final long DEFAULT_JTI_CLEANUP_PERIOD_MINUTES = 15;

    private DeviceTokenJtiCleanupService deviceTokenJtiCleanupService;

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            DevicePolicyEvaluator devicePolicyEvaluator = new DevicePolicyEvaluatorImpl();
            IntegrityDataEnricher integrityDataEnricher = new IntegrityDataEnricher();

            bundleCtx.registerService(DevicePolicyEvaluator.class.getName(), devicePolicyEvaluator, null);
            bundleCtx.registerService(RuleEvaluationDataProvider.class.getName(),
                    new DevicePolicyEvaluationDataProvider(), null);
            bundleCtx.registerService(DeviceDataResolver.class.getName(),
                    new DeviceDataResolverImpl(), null);
            bundleCtx.registerService(DeviceTokenService.class.getName(),
                    new DeviceTokenServiceImpl(), null);

            DevicePolicyComponentServiceHolder holder = DevicePolicyComponentServiceHolder.getInstance();
            holder.setDevicePolicyEvaluator(devicePolicyEvaluator);
            holder.setIntegrityDataEnricher(integrityDataEnricher);

            holder.getJsFunctionRegistry().register(
                    JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER,
                    "isDevicePolicyCompliant",
                    new DevicePolicyJsFunction());

            startDeviceTokenJtiCleanup();

            LOG.debug("Device policy bundle activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing device policy service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (deviceTokenJtiCleanupService != null) {
            deviceTokenJtiCleanupService.shutdown();
        }
        LOG.debug("Device policy bundle deactivated.");
    }

    /**
     * Starts the scheduled task that removes expired device token jti records from the replay store.
     * Controlled by the {@code DeviceTokenJtiCleanUp} properties; enabled with a default period when unset.
     */
    private void startDeviceTokenJtiCleanup() {

        boolean cleanupEnabled = true;
        String cleanupEnabledValue = IdentityUtil.getProperty(JTI_CLEANUP_ENABLE_PROPERTY);
        if (StringUtils.isNotBlank(cleanupEnabledValue)) {
            cleanupEnabled = Boolean.parseBoolean(cleanupEnabledValue);
        }
        if (!cleanupEnabled) {
            LOG.debug("Device token jti cleanup task is disabled.");
            return;
        }

        long cleanupPeriod = DEFAULT_JTI_CLEANUP_PERIOD_MINUTES;
        String cleanupPeriodValue = IdentityUtil.getProperty(JTI_CLEANUP_PERIOD_PROPERTY);
        if (StringUtils.isNotBlank(cleanupPeriodValue) && StringUtils.isNumeric(cleanupPeriodValue)) {
            long configuredPeriod = Long.parseLong(cleanupPeriodValue);
            if (configuredPeriod > 0) {
                cleanupPeriod = configuredPeriod;
            } else {
                LOG.warn("Configured " + JTI_CLEANUP_PERIOD_PROPERTY
                        + " must be greater than zero; using default: "
                        + DEFAULT_JTI_CLEANUP_PERIOD_MINUTES + " minutes.");
            }
        }
        deviceTokenJtiCleanupService = new DeviceTokenJtiCleanupService(cleanupPeriod / 4, cleanupPeriod);
        deviceTokenJtiCleanupService.activateCleanUp();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device token jti cleanup task enabled to run every " + cleanupPeriod + " minutes.");
        }
    }

    @Reference(
            name = "policy.evaluation.service",
            service = PolicyEvaluationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPolicyEvaluationService"
    )
    protected void setPolicyEvaluationService(PolicyEvaluationService policyEvaluationService) {

        DevicePolicyComponentServiceHolder.getInstance().setPolicyEvaluationService(policyEvaluationService);
        LOG.debug("PolicyEvaluationService set in Device Policy component.");
    }

    protected void unsetPolicyEvaluationService(PolicyEvaluationService policyEvaluationService) {

        DevicePolicyComponentServiceHolder.getInstance().setPolicyEvaluationService(null);
        LOG.debug("PolicyEvaluationService unset in Device Policy component.");
    }

    @Reference(
            name = "policy.management.service",
            service = PolicyManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPolicyManagementService"
    )
    protected void setPolicyManagementService(PolicyManagementService policyManagementService) {

        DevicePolicyComponentServiceHolder.getInstance().setPolicyManagementService(policyManagementService);
        LOG.debug("PolicyManagementService set in Device Policy component.");
    }

    protected void unsetPolicyManagementService(PolicyManagementService policyManagementService) {

        DevicePolicyComponentServiceHolder.getInstance().setPolicyManagementService(null);
        LOG.debug("PolicyManagementService unset in Device Policy component.");
    }

    @Reference(
            name = "device.management.service",
            service = DeviceManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService"
    )
    protected void setDeviceManagementService(DeviceManagementService deviceManagementService) {

        DevicePolicyComponentServiceHolder.getInstance().setDeviceManagementService(deviceManagementService);
        LOG.debug("DeviceManagementService set in Device Policy component.");
    }

    protected void unsetDeviceManagementService(DeviceManagementService deviceManagementService) {

        DevicePolicyComponentServiceHolder.getInstance().setDeviceManagementService(null);
        LOG.debug("DeviceManagementService unset in Device Policy component.");
    }

    @Reference(
            name = "js.function.registry",
            service = JsFunctionRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetJsFunctionRegistry"
    )
    protected void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        DevicePolicyComponentServiceHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistry);
        LOG.debug("JsFunctionRegistry set in Device Policy component.");
    }

    protected void unsetJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        DevicePolicyComponentServiceHolder.getInstance().setJsFunctionRegistry(null);
        LOG.debug("JsFunctionRegistry unset in Device Policy component.");
    }

    @Reference(
            name = "client.attestation.service",
            service = ClientAttestationService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClientAttestationService"
    )
    protected void setClientAttestationService(ClientAttestationService clientAttestationService) {

        DevicePolicyComponentServiceHolder.getInstance().setClientAttestationService(clientAttestationService);
        LOG.debug("ClientAttestationService set in Device Policy component.");
    }

    protected void unsetClientAttestationService(ClientAttestationService clientAttestationService) {

        DevicePolicyComponentServiceHolder.getInstance().setClientAttestationService(null);
        LOG.debug("ClientAttestationService unset in Device Policy component.");
    }
}
