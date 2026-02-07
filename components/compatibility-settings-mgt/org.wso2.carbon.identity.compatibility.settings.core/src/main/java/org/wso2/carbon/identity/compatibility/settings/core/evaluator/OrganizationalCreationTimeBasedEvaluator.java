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
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingContext;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;
import org.wso2.carbon.identity.compatibility.settings.core.util.IdentityCompatibilitySettingsUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;

import java.time.Instant;
import java.util.Map;

/**
 * Evaluator that determines compatibility settings based on organization creation time.
 * If the organization's creation time is less than the metadata's timestamp reference,
 * it returns the target value; otherwise, it returns the default value.
 */
public class OrganizationalCreationTimeBasedEvaluator extends AbstractCompatibilitySettingEvaluator {

    private static final Log LOG = LogFactory.getLog(OrganizationalCreationTimeBasedEvaluator.class);
    private static final int EXECUTION_ORDER = 10;

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

        CompatibilitySettingMetaData metaData = context.getCompatibilitySettingMetaData();
        return metaData != null && !metaData.getSettingsMetaData().isEmpty();
    }

    @Override
    public CompatibilitySetting evaluate(CompatibilitySettingContext context) throws CompatibilitySettingException {

        String tenantDomain = context.getTenantDomain();
        CompatibilitySettingMetaData metaData = context.getCompatibilitySettingMetaData();
        Instant orgCreationTime;

        try {
            orgCreationTime = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(tenantDomain, true);
        } catch (OrganizationManagementException e) {
            throw IdentityCompatibilitySettingsUtil.handleServerException(
                    IdentityCompatibilitySettingsConstants.ErrorMessages
                            .ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME, e, tenantDomain);
        }
        CompatibilitySetting result = new CompatibilitySetting();

        for (Map.Entry<String, CompatibilitySettingMetaDataGroup> groupEntry :
                metaData.getSettingsMetaData().entrySet()) {

            String settingGroupKey = groupEntry.getKey();
            CompatibilitySettingMetaDataGroup metaDataGroup = groupEntry.getValue();

            CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
            settingGroup.setSettingGroup(settingGroupKey);

            for (Map.Entry<String, CompatibilitySettingMetaDataEntry> settingEntry :
                    metaDataGroup.getSettingsMetaData().entrySet()) {

                String settingKey = settingEntry.getKey();
                CompatibilitySettingMetaDataEntry metaDataEntry = settingEntry.getValue();
                String evaluatedValue = evaluateSetting(orgCreationTime, metaDataEntry);
                settingGroup.addSetting(settingKey, evaluatedValue);
            }

            result.addCompatibilitySetting(settingGroup);
        }
        return result;
    }

    @Override
    public CompatibilitySetting evaluateByGroup(String settingGroup, CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        String tenantDomain = context.getTenantDomain();
        CompatibilitySettingMetaData metaData = context.getCompatibilitySettingMetaData();
        CompatibilitySettingMetaDataGroup metaDataGroup = metaData.getSettingMetaDataGroup(settingGroup);

        if (metaDataGroup == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No metadata found for setting group: " + settingGroup);
            }
            return new CompatibilitySetting();
        }

        Instant orgCreationTime;

        try {
            orgCreationTime = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(tenantDomain, true);
        } catch (OrganizationManagementException e) {
            throw IdentityCompatibilitySettingsUtil.handleServerException(
                    IdentityCompatibilitySettingsConstants.ErrorMessages
                            .ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME, e, tenantDomain);
        }
        CompatibilitySetting result = new CompatibilitySetting();
        CompatibilitySettingGroup resultGroup = new CompatibilitySettingGroup();
        resultGroup.setSettingGroup(settingGroup);

        for (Map.Entry<String, CompatibilitySettingMetaDataEntry> settingEntry :
                metaDataGroup.getSettingsMetaData().entrySet()) {

            String settingKey = settingEntry.getKey();
            CompatibilitySettingMetaDataEntry metaDataEntry = settingEntry.getValue();
            String evaluatedValue = evaluateSetting(orgCreationTime, metaDataEntry);
            resultGroup.addSetting(settingKey, evaluatedValue);
        }

        result.addCompatibilitySetting(resultGroup);
        return result;
    }

    @Override
    public CompatibilitySetting evaluateByGroupAndSetting(String settingGroup, String setting,
                                                          CompatibilitySettingContext context)
            throws CompatibilitySettingException {

        String tenantDomain = context.getTenantDomain();
        CompatibilitySettingMetaData metaData = context.getCompatibilitySettingMetaData();
        CompatibilitySettingMetaDataEntry metaDataEntry =
                metaData.getSettingMetaDataEntry(settingGroup, setting);

        if (metaDataEntry == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No metadata found for setting: " + settingGroup + "." + setting);
            }
            return new CompatibilitySetting();
        }

        Instant orgCreationTime;

        try {
            orgCreationTime = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(tenantDomain, true);
        } catch (OrganizationManagementException e) {
            throw IdentityCompatibilitySettingsUtil.handleServerException(
                    IdentityCompatibilitySettingsConstants.ErrorMessages
                            .ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME, e, tenantDomain);
        }
        String evaluatedValue = evaluateSetting(orgCreationTime, metaDataEntry);

        CompatibilitySetting result = new CompatibilitySetting();
        CompatibilitySettingGroup resultGroup = new CompatibilitySettingGroup();
        resultGroup.setSettingGroup(settingGroup);
        resultGroup.addSetting(setting, evaluatedValue);
        result.addCompatibilitySetting(resultGroup);
        return result;
    }

    /**
     * Evaluate a single setting based on organization creation time and metadata.
     *
     * @param orgCreationTime    Organization creation time.
     * @param metaDataEntry      Metadata entry for the setting.
     * @return Evaluated value (target or default).
     */
    private String evaluateSetting(Instant orgCreationTime, CompatibilitySettingMetaDataEntry metaDataEntry) {

        Instant timestampReference = metaDataEntry.getTimestampReference();
        if (timestampReference != null  && orgCreationTime != null && orgCreationTime.isBefore(timestampReference)) {
            return metaDataEntry.getTargetValue();
        }
        return metaDataEntry.getDefaultValue();
    }
}
