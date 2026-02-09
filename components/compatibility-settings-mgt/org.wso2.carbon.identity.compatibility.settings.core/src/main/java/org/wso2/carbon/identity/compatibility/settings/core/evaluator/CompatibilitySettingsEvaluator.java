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

import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;

/**
 * Interface for compatibility settings evaluators.
 */
public interface CompatibilitySettingsEvaluator {

    /**
     * Get the execution order identifier for this evaluator.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Check whether the evaluator is enabled or not.
     *
     * @return True if enabled.
     */
    boolean isEnabled();

    /**
     * Check whether the evaluator can handle the given context.
     *
     * @param context Compatibility setting context.
     * @return True if the evaluator can handle the context.
     * @throws CompatibilitySettingException If an error occurs during the check.
     */
    boolean canHandle(CompatibilitySettingContext context) throws CompatibilitySettingException;

    /**
     * Get the name of the evaluator.
     *
     * @return Name of the evaluator.
     */
    String getName();

    /**
     * Evaluate the compatibility settings.
     *
     * @param context Compatibility setting context.
     * @return Compatibility settings DTO.
     * @throws CompatibilitySettingException If an error occurs during evaluation.
     */
    CompatibilitySetting evaluate(CompatibilitySettingContext context) throws CompatibilitySettingException;

    /**
     * Evaluate the compatibility settings for a specific setting group.
     *
     * @param settingGroup Setting group.
     * @param context      Compatibility setting context.
     * @return Compatibility settings DTO.
     * @throws CompatibilitySettingException If an error occurs during evaluation.
     */
    CompatibilitySetting evaluateByGroup(String settingGroup, CompatibilitySettingContext context)
            throws CompatibilitySettingException;

    /**
     * Evaluate the compatibility settings for a specific setting group and setting.
     *
     * @param settingGroup Setting group.
     * @param setting      Specific setting.
     * @param context      Compatibility setting context.
     * @return Compatibility settings DTO.
     * @throws CompatibilitySettingException If an error occurs during evaluation.
     */
    CompatibilitySetting evaluateByGroupAndSetting(String settingGroup, String setting,
                                                   CompatibilitySettingContext context)
            throws CompatibilitySettingException;
}
