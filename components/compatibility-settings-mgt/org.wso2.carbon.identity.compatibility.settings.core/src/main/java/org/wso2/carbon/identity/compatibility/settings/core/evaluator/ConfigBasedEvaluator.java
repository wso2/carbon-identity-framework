/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;

import java.util.Map;

/**
 * Evaluator that returns compatibility settings directly from the context.
 * This evaluator acts as a configuration-based evaluator that simply returns
 * the compatibility settings provided in the context without any transformation.
 */
public class ConfigBasedEvaluator extends AbstractCompatibilitySettingEvaluator {

    private static final Log LOG = LogFactory.getLog(ConfigBasedEvaluator.class);
    private static final int EXECUTION_ORDER = 20;

    @Override
    public int getExecutionOrderId() {

        return EXECUTION_ORDER;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    @Override
    public boolean canHandle(CompatibilitySettingContext context) throws CompatibilitySettingException {

        if (context == null) {
            return false;
        }

        // Check if compatibility settings are available in the context.
        CompatibilitySetting compatibilitySetting = context.getCompatibilitySettings();
        return compatibilitySetting != null && !compatibilitySetting.getCompatibilitySettings().isEmpty();
    }

    @Override
    public CompatibilitySetting evaluate(CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        CompatibilitySetting compatibilitySetting = context.getCompatibilitySettings();
        if (compatibilitySetting == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No compatibility settings found in context for tenant: " + context.getTenantDomain());
            }
            return new CompatibilitySetting();
        }
        return compatibilitySetting;
    }

    @Override
    public CompatibilitySetting evaluateByGroup(String settingGroup, CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        CompatibilitySetting contextSettings = context.getCompatibilitySettings();
        if (contextSettings == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No compatibility settings found in context for tenant: " +
                        context.getTenantDomain());
            }
            return new CompatibilitySetting();
        }

        Map<String, CompatibilitySettingGroup> settingsMap = contextSettings.getCompatibilitySettings();
        CompatibilitySettingGroup group = settingsMap.get(settingGroup);
        if (group == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No compatibility settings found for group: " + settingGroup);
            }
            return new CompatibilitySetting();
        }

        CompatibilitySetting result = new CompatibilitySetting();
        result.addCompatibilitySetting(group);
        return result;
    }

    @Override
    public CompatibilitySetting evaluateByGroupAndSetting(String settingGroup, String setting,
                                                          CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        CompatibilitySetting contextSettings = context.getCompatibilitySettings();
        if (contextSettings == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No compatibility settings found in context for tenant: " +
                        context.getTenantDomain());
            }
            return new CompatibilitySetting();
        }

        Map<String, CompatibilitySettingGroup> settingsMap = contextSettings.getCompatibilitySettings();
        CompatibilitySettingGroup group = settingsMap.get(settingGroup);
        if (group == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No compatibility settings found for group: " + settingGroup);
            }
            return new CompatibilitySetting();
        }

        String value = group.getSettingValue(setting);
        if (value == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No compatibility setting found for: " + settingGroup + "." + setting);
            }
            return new CompatibilitySetting();
        }

        CompatibilitySettingGroup resultGroup = new CompatibilitySettingGroup();
        resultGroup.setSettingGroup(settingGroup);
        resultGroup.addSetting(setting, value);

        CompatibilitySetting result = new CompatibilitySetting();
        result.addCompatibilitySetting(resultGroup);
        return result;
    }
}
