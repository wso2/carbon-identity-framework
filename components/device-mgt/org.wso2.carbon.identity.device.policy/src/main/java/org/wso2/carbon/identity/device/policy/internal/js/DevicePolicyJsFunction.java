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

package org.wso2.carbon.identity.device.policy.internal.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.HostAccess;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyDiagnosticLogger;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * JS function for device policy compliance check, registered as {@code isDevicePolicyCompliant}.
 * The verified device payload is captured and stored on the authentication context at initiation
 * (see {@code DeviceDataResolverImpl}); this function reads it from the context under
 * {@link FrameworkConstants#DEVICE_DATA}.
 */
public class DevicePolicyJsFunction implements BiFunction<JsBaseAuthenticationContext, String, String> {

    private static final Log LOG = LogFactory.getLog(DevicePolicyJsFunction.class);

    private final DevicePolicyDiagnosticLogger diagnosticLogger = new DevicePolicyDiagnosticLogger();

    @Override
    @HostAccess.Export
    public String apply(JsBaseAuthenticationContext context, String policyName) {

        try {
            String tenantDomain = context.getWrapped().getTenantDomain();

            Object data = context.getWrapped().getProperty(FrameworkConstants.DEVICE_DATA);
            if (!(data instanceof Map)) {
                diagnosticLogger.logMissingDeviceData(policyName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No device data on the authentication context for policy: " + policyName);
                }
                return policyName + ":device token is missing or validation failed";
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> deviceData = new HashMap<>((Map<String, Object>) data);

            String appId = context.getWrapped().getServiceProviderResourceId();
            DevicePolicyComponentServiceHolder holder = DevicePolicyComponentServiceHolder.getInstance();

            return holder.getDevicePolicyEvaluator().evaluate(policyName, deviceData, appId, tenantDomain);

        } catch (PolicyManagementException e) {
            LOG.error("Error while evaluating device policy: " + policyName, e);
            return policyName + ":policy_error";
        } catch (RuleEvaluationException e) {
            LOG.error("Rule evaluation failed for device policy: " + policyName, e);
            return policyName + ":evaluation_error";
        } catch (PolicyEvaluationException e) {
            LOG.error("Policy evaluation failed for device policy: " + policyName, e);
            return policyName + ":evaluation_error";
        }
    }

}
