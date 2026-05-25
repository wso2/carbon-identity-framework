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

package org.wso2.carbon.identity.consent.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.FORM;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.MARKETING;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.POLICY;

/**
 * Listener to enrich POLICY and MARKETING components with purpose and attribute details from the consent manager.
 */
public class ConsentFlowExecutionListener extends AbstractFlowExecutionListener {

    private static final String PURPOSES_CONFIG = "purposes";
    private static final String PURPOSE_ID_KEY = "purposeId";
    private static final String PURPOSE_NAME_KEY = "name";
    private static final String PURPOSE_DESCRIPTION_KEY = "description";
    private static final String POLICY_MANDATORY_KEY = "mandatory";
    private static final String POLICY_URL_KEY = "policyUrl";
    private static final String ATTRIBUTES_KEY = "attributes";
    private static final String ATTRIBUTE_ID_KEY = "id";
    private static final String ATTRIBUTE_NAME_KEY = "name";
    private static final String ATTRIBUTE_DISPLAY_NAME_KEY = "displayName";

    @Override
    public int getDefaultOrderId() {

        return 4;
    }

    @Override
    public int getExecutionOrderId() {

        return 4;
    }

    @Override
    public boolean isEnabled() {

        if (!FrameworkUtils.isConsentV2APIEnabled()) {
            return false;
        }
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId() == null;
    }

    @Override
    public boolean doPostExecute(FlowExecutionStep step, FlowExecutionContext context) throws FlowEngineException {

        if (step.getData() == null || step.getData().getComponents() == null) {
            return true;
        }
        for (ComponentDTO component : step.getData().getComponents()) {
            if (FORM.equals(component.getType()) && component.getComponents() != null) {
                for (ComponentDTO child : component.getComponents()) {
                    if (POLICY.equals(child.getType())) {
                        enrichPolicyComponent(child, context);
                    } else if (MARKETING.equals(child.getType())) {
                        enrichMarketingComponent(child, context);
                    }
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void enrichPolicyComponent(ComponentDTO component, FlowExecutionContext context)
            throws FlowEngineException {

        if (component.getConfigs() == null || !component.getConfigs().containsKey(PURPOSES_CONFIG)) {
            return;
        }
        Object policiesObj = component.getConfigs().get(PURPOSES_CONFIG);
        if (!(policiesObj instanceof List)) {
            return;
        }
        List<Map<String, Object>> policies = (List<Map<String, Object>>) policiesObj;

        for (Map<String, Object> policy : policies) {
            String purposeId = (String) policy.get(PURPOSE_ID_KEY);
            if (StringUtils.isBlank(purposeId)) {
                continue;
            }
            String purposeName = (String) policy.get(PURPOSE_NAME_KEY);
            if (!StringUtils.isBlank(purposeName)) {
                continue;
            }
            Purpose purpose;
            try {
                purpose = IdentityConsentDataHolder.getInstance().getConsentManager().getPurposeByUuid(purposeId);
            } catch (ConsentManagementException e) {
                throw new FlowEngineServerException(
                        Constants.ErrorMessages.ERROR_CODE_POLICY_CONSENT_FAILURE.getCode(),
                        Constants.ErrorMessages.ERROR_CODE_POLICY_CONSENT_FAILURE.getMessage(),
                        String.format(Constants.ErrorMessages.ERROR_CODE_POLICY_CONSENT_FAILURE.getDescription(),
                                context.getFlowType(), context.getContextIdentifier()));
            }
            if (purpose == null) {
                continue;
            }
            policy.put(PURPOSE_NAME_KEY, purpose.getName());
            policy.put(POLICY_MANDATORY_KEY, isMandatoryPurpose(purpose));
            policy.put(PURPOSE_DESCRIPTION_KEY, resolveDescription(purpose));
            policy.put(POLICY_URL_KEY, resolvePolicyUrl(purpose));
        }
    }

    @SuppressWarnings("unchecked")
    private void enrichMarketingComponent(ComponentDTO component, FlowExecutionContext context)
            throws FlowEngineException {

        if (component.getConfigs() == null || !component.getConfigs().containsKey(PURPOSES_CONFIG)) {
            return;
        }
        Object purposesObj = component.getConfigs().get(PURPOSES_CONFIG);
        if (!(purposesObj instanceof List)) {
            return;
        }
        List<Map<String, Object>> purposes = (List<Map<String, Object>>) purposesObj;

        for (Map<String, Object> purposeEntry : purposes) {
            String purposeId = (String) purposeEntry.get(PURPOSE_ID_KEY);
            if (StringUtils.isBlank(purposeId)) {
                continue;
            }
            String existingName = (String) purposeEntry.get(PURPOSE_NAME_KEY);
            if (!StringUtils.isBlank(existingName)) {
                continue;
            }
            Purpose purpose;
            try {
                purpose = IdentityConsentDataHolder.getInstance().getConsentManager().getPurposeByUuid(purposeId);
            } catch (ConsentManagementException e) {
                throw new FlowEngineServerException(
                        Constants.ErrorMessages.ERROR_CODE_POLICY_CONSENT_FAILURE.getCode(),
                        Constants.ErrorMessages.ERROR_CODE_POLICY_CONSENT_FAILURE.getMessage(),
                        String.format(Constants.ErrorMessages.ERROR_CODE_POLICY_CONSENT_FAILURE.getDescription(),
                                context.getFlowType(), context.getContextIdentifier()));
            }
            if (purpose == null) {
                continue;
            }
            purposeEntry.put(PURPOSE_NAME_KEY, purpose.getName());
            purposeEntry.put(PURPOSE_DESCRIPTION_KEY, resolveDescription(purpose));
            purposeEntry.put(ATTRIBUTES_KEY, resolveAttributes(purpose));
        }
    }

    private boolean isMandatoryPurpose(Purpose purpose) {

        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion == null) {
            return false;
        }
        List<PurposePIICategory> categories = latestVersion.getPurposePIICategories();
        if (categories == null) {
            return false;
        }
        return categories.stream().anyMatch(cat -> Boolean.TRUE.equals(cat.getMandatory()));
    }

    private String resolveDescription(Purpose purpose) {

        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion != null && latestVersion.getDescription() != null) {
            return latestVersion.getDescription();
        }
        return purpose.getDescription() != null ? purpose.getDescription() : "";
    }

    private String resolvePolicyUrl(Purpose purpose) {

        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion != null && latestVersion.getProperties() != null) {
            String url = latestVersion.getProperties().get(POLICY_URL_KEY);
            if (url != null) {
                return url;
            }
        }
        return "";
    }

    private List<Map<String, Object>> resolveAttributes(Purpose purpose) {

        List<Map<String, Object>> attributes = new ArrayList<>();
        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion == null) {
            return attributes;
        }
        List<PurposePIICategory> piiCategories = latestVersion.getPurposePIICategories();
        if (piiCategories == null) {
            return attributes;
        }
        for (PurposePIICategory category : piiCategories) {
            Map<String, Object> attribute = new HashMap<>();
            attribute.put(ATTRIBUTE_ID_KEY, category.getUuid());
            attribute.put(ATTRIBUTE_NAME_KEY, category.getName());
            attribute.put(ATTRIBUTE_DISPLAY_NAME_KEY,
                    category.getDisplayName() != null ? category.getDisplayName() : category.getName());
            attributes.add(attribute);
        }
        return attributes;
    }
}
