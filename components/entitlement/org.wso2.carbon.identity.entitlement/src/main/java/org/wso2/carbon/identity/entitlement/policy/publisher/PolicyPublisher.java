/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.policy.publisher;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dao.PAPStatusDataHandlerModule;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dao.RegistrySubscriberManager;
import org.wso2.carbon.identity.entitlement.dao.SubscriberManagerModule;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is policy publisher. There can be different modules that have been plugged with this.
 */
public class PolicyPublisher {

    public static final String SUBSCRIBER_ID = "subscriberId";
    public static final String SUBSCRIBER_DISPLAY_NAME = "Subscriber Id";

    /**
     * set of publisher modules
     */
    Set<PolicyPublisherModule> publisherModules = new HashSet<>();

    /**
     * set of post publisher modules
     */
    Set<PAPStatusDataHandlerModule> papStatusDataHandlers = new HashSet<>();

    /**
     * Verification publisher modules
     */
    PublisherVerificationModule verificationModule = null;


    /**
     * Creates PolicyPublisher instance
     */
    public PolicyPublisher() {

        Map<PolicyPublisherModule, Properties> publisherModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyPublisherModules();
        if (publisherModules != null && !publisherModules.isEmpty()) {
            this.publisherModules.addAll(publisherModules.keySet());
        }

        Map<PublisherVerificationModule, Properties> prePublisherModules = EntitlementServiceComponent.
                getEntitlementConfig().getPublisherVerificationModule();
        if (prePublisherModules != null && !prePublisherModules.isEmpty()) {
            this.verificationModule = prePublisherModules.keySet().iterator().next();
        }

        //  creating default subscriber to publish policies to PDP
        CarbonPDPPublisher publisher = new CarbonPDPPublisher();
        this.publisherModules.add(publisher);

        PublisherDataHolder holder = new PublisherDataHolder(publisher.getModuleName());
        PublisherPropertyDTO dto = new PublisherPropertyDTO();
        dto.setId(SUBSCRIBER_ID);
        dto.setDisplayName(SUBSCRIBER_DISPLAY_NAME);
        dto.setValue(EntitlementConstants.PDP_SUBSCRIBER_ID);
        holder.setPropertyDTOs(new PublisherPropertyDTO[] {dto});
        try {
            PublisherDataHolder pdpDataHolder = null;
            SubscriberManagerModule subscriberManager = new RegistrySubscriberManager();
            try {

                pdpDataHolder = subscriberManager.retrieveSubscriber(EntitlementConstants.PDP_SUBSCRIBER_ID, false);
            } catch (Exception e) {
                // ignore
            }
            if (pdpDataHolder == null) {
                subscriberManager.persistSubscriber(holder, false);
            }
        } catch (EntitlementException e) {
            // ignore
        }
    }

    /**
     * publish policy
     *
     * @param policyIds        policy ids to publish,
     * @param version          version
     * @param action           action
     * @param enabled          enabled/disabled
     * @param order            policy order
     * @param subscriberIds    subscriber ids to publish,
     * @param verificationCode verificationCode as String
     */
    public void publishPolicy(String[] policyIds, String version, String action, boolean enabled, int order,
                              String[] subscriberIds, String verificationCode) {

        boolean toPDP = subscriberIds == null;

        PolicyPublishExecutor executor = new PolicyPublishExecutor(policyIds, version, action, enabled, order,
                subscriberIds, this, toPDP, verificationCode);
        executor.setTenantDomain(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        executor.setTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        executor.setUserName(CarbonContext.getThreadLocalCarbonContext().getUsername());

        executor.run();
    }


    public Set<PolicyPublisherModule> getPublisherModules() {
        return publisherModules;
    }

    public Set<PAPStatusDataHandlerModule> getPapStatusDataHandlers() {
        return papStatusDataHandlers;
    }

    public void setPapStatusDataHandlers(Set<PAPStatusDataHandlerModule> papStatusDataHandlers) {
        this.papStatusDataHandlers = papStatusDataHandlers;
    }

    public PublisherVerificationModule getVerificationModule() {
        return verificationModule;
    }
}
