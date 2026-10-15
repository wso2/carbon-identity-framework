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

package org.wso2.carbon.identity.rule.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Rule Management Config util class.
 * Holds system configurations related to rule management that can be overridden per flow type in identity.xml.
 */
public class RuleManagementConfig {

    private static final RuleManagementConfig INSTANCE = new RuleManagementConfig();

    private static final Log LOG = LogFactory.getLog(RuleManagementConfig.class);

    private static final int DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND = 5;
    private static final int DEFAULT_MAX_RULES_COMBINED_WITH_OR = 10;

    /**
     * The identity.xml section holding the limits of each flow type.
     */
    private static final Map<FlowType, String> CONFIG_SECTIONS = new EnumMap<>(FlowType.class);

    static {
        CONFIG_SECTIONS.put(FlowType.PRE_ISSUE_ACCESS_TOKEN, "Rules.PreIssueAccessToken");
        CONFIG_SECTIONS.put(FlowType.PRE_UPDATE_PASSWORD, "Rules.PreUpdatePassword");
        CONFIG_SECTIONS.put(FlowType.PRE_UPDATE_PROFILE, "Rules.PreUpdateProfile");
        CONFIG_SECTIONS.put(FlowType.PRE_ISSUE_ID_TOKEN, "Rules.PreIssueIdToken");
        CONFIG_SECTIONS.put(FlowType.APPROVAL_WORKFLOW, "Rules.ApprovalWorkflow");
        CONFIG_SECTIONS.put(FlowType.DEVICE_POLICY, "Rules.DevicePolicy");
        CONFIG_SECTIONS.put(FlowType.REGISTRATION, "Rules.Registration");
        CONFIG_SECTIONS.put(FlowType.PASSWORD_RECOVERY, "Rules.PasswordRecovery");
        CONFIG_SECTIONS.put(FlowType.INVITED_USER_REGISTRATION, "Rules.InvitedUserRegistration");
    }

    private RuleManagementConfig() {

    }

    /**
     * Returns the singleton instance of RuleManagementConfig.
     *
     * @return RuleManagementConfig instance.
     */
    public static RuleManagementConfig getInstance() {

        return INSTANCE;
    }

    /**
     * Returns the maximum number of expressions that can be combined with AND for the given flow type.
     * The value can be overridden per flow type in identity.xml. If the property is not configured or invalid,
     * the default value is returned.
     *
     * @param flowType Flow type.
     * @return The maximum number of expressions that can be combined with AND for the given flow type.
     */
    public int getMaxAndExpressionCount(FlowType flowType) {

        return resolveLimit(flowType, "MaxExpressionsCombinedWithAnd", DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND);
    }

    /**
     * Returns the maximum number of rules that can be combined with OR for the given flow type.
     * The value can be overridden per flow type in identity.xml. If the property is not configured or invalid,
     * the default value is returned.
     *
     * @param flowType Flow type.
     * @return The maximum number of rules that can be combined with OR for the given flow type.
     */
    public int getMaxOrRuleCount(FlowType flowType) {

        return resolveLimit(flowType, "MaxRulesCombinedWithOr", DEFAULT_MAX_RULES_COMBINED_WITH_OR);
    }

    /**
     * Resolves a limit configured under the flow type's section in identity.xml.
     *
     * @param flowType     Flow type.
     * @param property     Name of the limit within the flow type's section.
     * @param defaultValue Value used when the limit is not configured, is invalid, or the flow type has no section.
     * @return The limit.
     */
    private static int resolveLimit(FlowType flowType, String property, int defaultValue) {

        String section = CONFIG_SECTIONS.get(flowType);
        return section == null ? defaultValue : resolvePositive(section + "." + property, defaultValue);
    }

    private static int resolvePositive(String propertyKey, int defaultValue) {

        try {
            String propertyValue = (String) IdentityConfigParser.getInstance().getConfiguration().get(propertyKey);
            if (StringUtils.isNotBlank(propertyValue)) {
                int parsedValue = Integer.parseInt(propertyValue);
                if (parsedValue > 0) {
                    return parsedValue;
                }
            }
        } catch (NullPointerException | NumberFormatException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to resolve " + propertyKey + " from identity.xml. Using the default value: " +
                        defaultValue, e);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Positive value not configured for " + propertyKey +
                    ". Using the default value: " + defaultValue);
        }
        return defaultValue;
    }
}
