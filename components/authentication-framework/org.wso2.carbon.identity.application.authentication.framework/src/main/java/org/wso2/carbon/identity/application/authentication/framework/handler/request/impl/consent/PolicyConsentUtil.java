/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import com.google.gson.Gson;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.RESIDENT_IDP;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.PURPOSE_GROUP_TYPE_POLICY;

/**
 * Utility class for retrieving and classifying unconsented policy purposes for a user.
 * Intended to be called from both the {@link PolicyConsentPostAuthnHandler} and
 * the policy consent JSP page.
 */
public final class PolicyConsentUtil {

    private static final String POLICY_URL_PROPERTY_KEY = "policyUrl";
    private static final String PROMPT_ON_LOGIN_PROPERTY_KEY = "promptOnLogin";

    private PolicyConsentUtil() {
    }

    /**
     * Classified result of unconsented policy purposes for a user.
     */
    public static final class ClassifiedPolicies {

        private final List<String> mandatoryUnconsentedIds;
        private final List<String> mandatoryNewVersionIds;
        private final List<String> optionalUnconsentedIds;
        private final List<String> optionalNewVersionIds;
        private final String purposeMetadataJson;

        public ClassifiedPolicies(List<String> mandatoryUnconsentedIds, List<String> mandatoryNewVersionIds,
                                  List<String> optionalUnconsentedIds, List<String> optionalNewVersionIds,
                                  String purposeMetadataJson) {

            this.mandatoryUnconsentedIds = mandatoryUnconsentedIds;
            this.mandatoryNewVersionIds = mandatoryNewVersionIds;
            this.optionalUnconsentedIds = optionalUnconsentedIds;
            this.optionalNewVersionIds = optionalNewVersionIds;
            this.purposeMetadataJson = purposeMetadataJson;
        }

        public List<String> getMandatoryUnconsentedIds() {

            return mandatoryUnconsentedIds;
        }

        public List<String> getMandatoryNewVersionIds() {

            return mandatoryNewVersionIds;
        }

        public List<String> getOptionalUnconsentedIds() {

            return optionalUnconsentedIds;
        }

        public List<String> getOptionalNewVersionIds() {

            return optionalNewVersionIds;
        }

        public String getPurposeMetadataJson() {

            return purposeMetadataJson;
        }

        public boolean isEmpty() {
            return mandatoryUnconsentedIds.isEmpty() && mandatoryNewVersionIds.isEmpty()
                    && optionalUnconsentedIds.isEmpty() && optionalNewVersionIds.isEmpty();
        }
    }

    /**
     * Classifies unconsented policy purposes for the user identified by the given session data key.
     * Intended for use from JSP pages that only have access to the session data key.
     *
     * @param sessionDataKey the session data key from the URL parameter
     * @return classified policy purposes, or an empty result if the context cannot be resolved
     * @throws ConsentManagementException if an error occurs during consent lookup
     */
    public static ClassifiedPolicies classifyUnconsentedPolicies(String sessionDataKey)
            throws ConsentManagementException {

        AuthenticationContext context = FrameworkUtils.getAuthenticationContextFromCache(sessionDataKey);
        if (context == null || context.getSequenceConfig() == null) {
            return emptyResult();
        }
        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();
        if (user == null) {
            return emptyResult();
        }
        Set<String> mappedPolicyIds = getMappedPolicyIds(context);
        String subjectId = UserCoreUtil.addDomainToName(user.getUserName(), user.getUserStoreDomain());
        String tenantDomain = user.getTenantDomain();
        return classifyUnconsentedPolicies(subjectId, tenantDomain, mappedPolicyIds);
    }

    /**
     * Classifies unconsented policy purposes for the given user, restricted to the given set of policy IDs.
     * Only purposes whose UUID is in {`@code` policyIds} are evaluated.
     *
     * @param subjectId    the user's subject identifier (domain-qualified)
     * @param tenantDomain the user's tenant domain
     * @param policyIds    the set of policy purpose UUIDs to restrict the check to
     * @return classified policy purposes
     * @throws ConsentManagementException if an error occurs during consent lookup
     */
    public static ClassifiedPolicies classifyUnconsentedPolicies(String subjectId, String tenantDomain,
                                                                 Set<String> policyIds)
            throws ConsentManagementException {

        if (policyIds == null || policyIds.isEmpty()) {
            return emptyResult();
        }

        ConsentManager consentManager = FrameworkServiceDataHolder.getInstance().getConsentManager();
        List<Purpose> policyPurposes;
        List<String> mandatoryUnconsentedIds = new ArrayList<>();
        List<String> mandatoryNewVersionIds = new ArrayList<>();
        List<String> optionalUnconsentedIds = new ArrayList<>();
        List<String> optionalNewVersionIds = new ArrayList<>();

        try {
            startTenantFlow(subjectId, tenantDomain);
            policyPurposes = getPolicyPurposes(consentManager);
            for (Purpose purpose : policyPurposes) {
                if (!policyIds.contains(purpose.getUuid())) {
                    continue;
                }
                if (!isPolicyConsentMissing(subjectId, purpose, consentManager)) {
                    continue;
                }
                boolean hasPreviousConsent = hasConsentForAnyVersion(subjectId, purpose, consentManager);
                if (isMandatoryPurpose(purpose)) {
                    (hasPreviousConsent ? mandatoryNewVersionIds : mandatoryUnconsentedIds).add(purpose.getUuid());
                } else {
                    (hasPreviousConsent ? optionalNewVersionIds : optionalUnconsentedIds).add(purpose.getUuid());
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Set<String> mandatoryIdSet = new HashSet<>(mandatoryUnconsentedIds);
        mandatoryIdSet.addAll(mandatoryNewVersionIds);
        Set<String> newVersionIdSet = new HashSet<>(mandatoryNewVersionIds);
        newVersionIdSet.addAll(optionalNewVersionIds);

        List<Purpose> relevantPurposes = new ArrayList<>();
        for (Purpose purpose : policyPurposes) {
            if (mandatoryUnconsentedIds.contains(purpose.getUuid())
                    || mandatoryNewVersionIds.contains(purpose.getUuid())
                    || optionalUnconsentedIds.contains(purpose.getUuid())
                    || optionalNewVersionIds.contains(purpose.getUuid())) {
                relevantPurposes.add(purpose);
            }
        }

        String metadataJson = buildPurposeMetadataJson(relevantPurposes, mandatoryIdSet, newVersionIdSet);
        return new ClassifiedPolicies(mandatoryUnconsentedIds, mandatoryNewVersionIds,
                optionalUnconsentedIds, optionalNewVersionIds, metadataJson);
    }

    /**
     * Checks whether the user has any unconsented policy purpose among the given set of policy IDs.
     * When {@code policyIds} is non-empty only purposes whose UUID is in that set are evaluated.
     *
     * @param subjectId    the user's subject identifier (domain-qualified)
     * @param tenantDomain the user's tenant domain
     * @param policyIds    the set of policy purpose UUIDs to restrict the check to
     * @return {@code true} if at least one matching policy purpose requires consent
     * @throws ConsentManagementException if an error occurs during consent lookup
     */
    public static boolean hasUnconsentedPolicies(String subjectId, String tenantDomain, Set<String> policyIds)
            throws ConsentManagementException {

        if (policyIds == null || policyIds.isEmpty()) {
            return false;
        }
        ConsentManager consentManager = FrameworkServiceDataHolder.getInstance().getConsentManager();
        try {
            startTenantFlow(subjectId, tenantDomain);
            List<Purpose> policyPurposes = getPolicyPurposes(consentManager);
            for (Purpose purpose : policyPurposes) {
                if (!policyIds.isEmpty() && !policyIds.contains(purpose.getUuid())) {
                    continue;
                }
                if (isPolicyConsentMissing(subjectId, purpose, consentManager)) {
                    return true;
                }
            }
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private static boolean isPolicyConsentMissing(String subjectId, Purpose purpose, ConsentManager consentManager)
            throws ConsentManagementException {

        List<PurposeVersion> allVersions = consentManager.listPurposeVersions(purpose.getUuid());
        if (allVersions == null || allVersions.isEmpty()) {
            return false;
        }
        PurposeVersion promptOnLoginVersion = getLatestVersionWithPromptOnLogin(purpose, allVersions, consentManager);
        if (promptOnLoginVersion == null) {
            return false;
        }
        boolean mandatory = isMandatoryPurpose(purpose);
        return missingConsentForVersion(subjectId, purpose, promptOnLoginVersion, allVersions, consentManager,
                mandatory);
    }

    private static void startTenantFlow(String subjectId, String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subjectId);
    }

    private static List<Purpose> getPolicyPurposes(ConsentManager consentManager) throws ConsentManagementException {

        ExpressionNode expressionNode = new ExpressionNode();
        expressionNode.setAttributeValue("type");
        expressionNode.setOperation("eq");
        expressionNode.setValue(PURPOSE_GROUP_TYPE_POLICY);
        List<Purpose> purposes = consentManager.listPurposes(
                Collections.singletonList(expressionNode), 0);
        return purposes != null ? purposes : Collections.emptyList();
    }

    private static boolean isMandatoryPurpose(Purpose purpose) {

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

    private static PurposeVersion getLatestVersionWithPromptOnLogin(Purpose purpose, List<PurposeVersion> allVersions,
            ConsentManager consentManager) throws ConsentManagementException {

        for (int i = allVersions.size() - 1; i >= 0; i--) {
            PurposeVersion version = consentManager.getPurposeVersion(
                    purpose.getUuid(), allVersions.get(i).getUuid());
            if (shouldPromptOnLogin(version)) {
                return version;
            }
        }
        return null;
    }

    private static boolean shouldPromptOnLogin(PurposeVersion version) {

        if (version == null || version.getProperties() == null) {
            return false;
        }
        return Boolean.parseBoolean(version.getProperties().get(PROMPT_ON_LOGIN_PROPERTY_KEY));
    }

    private static boolean missingConsentForVersion(String subjectId, Purpose purpose,
                                                    PurposeVersion promptOnLoginVersion,
                                                    List<PurposeVersion> allVersions,
                                                    ConsentManager consentManager, boolean mandatory)
            throws ConsentManagementException {

        int promptOnLoginVersionIndex = -1;
        for (int i = 0; i < allVersions.size(); i++) {
            if (promptOnLoginVersion.getUuid().equals(allVersions.get(i).getUuid())) {
                promptOnLoginVersionIndex = i;
                break;
            }
        }
        if (promptOnLoginVersionIndex == -1) {
            return true;
        }
        String state = mandatory ? ConsentConstants.ACTIVE_STATE : null;
        for (int i = promptOnLoginVersionIndex; i < allVersions.size(); i++) {
            List<Receipt> receipts = consentManager.listReceipts(subjectId, RESIDENT_IDP,
                    state, purpose.getUuid(), allVersions.get(i).getUuid(), null, null, 1);
            if (receipts != null && !receipts.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasConsentForAnyVersion(String subjectId, Purpose purpose, ConsentManager consentManager)
            throws ConsentManagementException {

        List<Receipt> receipts = consentManager.listReceipts(subjectId, RESIDENT_IDP,
                null, purpose.getUuid(), null, null, null, 1);
        return receipts != null && !receipts.isEmpty();
    }

    private static String buildPurposeMetadataJson(List<Purpose> purposes, Set<String> mandatoryIdSet,
            Set<String> newVersionIdSet) {

        List<Map<String, Object>> metadataList = new ArrayList<>();
        for (Purpose purpose : purposes) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("purposeId", purpose.getUuid() != null ? purpose.getUuid() : "");
            metadata.put("name", purpose.getName() != null ? purpose.getName() : purpose.getUuid());
            metadata.put("mandatory", mandatoryIdSet.contains(purpose.getUuid()));
            metadata.put("newVersion", newVersionIdSet.contains(purpose.getUuid()));

            PurposeVersion latestVersion = purpose.getLatestVersion();
            String description = "";
            if (latestVersion != null && latestVersion.getDescription() != null) {
                description = latestVersion.getDescription();
            } else if (purpose.getDescription() != null) {
                description = purpose.getDescription();
            }
            metadata.put("description", description);

            String policyUrl = "";
            if (latestVersion != null && latestVersion.getProperties() != null) {
                String url = latestVersion.getProperties().get(POLICY_URL_PROPERTY_KEY);
                if (url != null) {
                    policyUrl = url;
                }
            }
            metadata.put("policyUrl", policyUrl);
            metadataList.add(metadata);
        }
        return new Gson().toJson(metadataList);
    }

    /**
     * Resolves the set of policy purpose IDs mapped to the application of the given authentication context.
     *
     * @param context the authentication context
     * @return the set of mapped policy purpose UUIDs (empty if none or if the application cannot be resolved)
     * @throws ConsentManagementException if an error occurs while retrieving the application policy mappings
     */
    private static Set<String> getMappedPolicyIds(AuthenticationContext context) throws ConsentManagementException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        ApplicationConfig applicationConfig = sequenceConfig != null ? sequenceConfig.getApplicationConfig() : null;
        ServiceProvider serviceProvider = applicationConfig != null ? applicationConfig.getServiceProvider() : null;
        if (serviceProvider == null || serviceProvider.getApplicationResourceId() == null) {
            return Collections.emptySet();
        }
        String appResourceId = serviceProvider.getApplicationResourceId();
        try {
            List<String> purposeIds = FrameworkServiceDataHolder.getInstance()
                    .getConsentAppMappingService()
                    .getPurposesForApplication(appResourceId);
            return new HashSet<>(purposeIds);
        } catch (ConsentAppMappingException e) {
            throw new ConsentManagementException(
                    "Error retrieving policy mappings for application: " + appResourceId, e.getErrorCode(), e);
        }
    }

    private static ClassifiedPolicies emptyResult() {

        return new ClassifiedPolicies(Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), "[]");
    }
}
