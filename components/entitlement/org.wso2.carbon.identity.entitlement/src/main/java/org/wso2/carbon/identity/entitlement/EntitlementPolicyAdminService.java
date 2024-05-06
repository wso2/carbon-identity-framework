/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dao.*;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.EntitlementFinderDataHolder;
import org.wso2.carbon.identity.entitlement.dto.EntitlementTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.dto.PaginatedPolicySetDTO;
import org.wso2.carbon.identity.entitlement.dto.PaginatedStatusHolder;
import org.wso2.carbon.identity.entitlement.dto.PaginatedStringDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.EntitlementAdminEngine;
import org.wso2.carbon.identity.entitlement.pap.EntitlementDataFinder;
import org.wso2.carbon.identity.entitlement.pap.PAPPolicyReader;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisher;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisherModule;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Entitlement Admin Service Class which exposes the PAP
 */
public class EntitlementPolicyAdminService {

    private static final Log log = LogFactory.getLog(EntitlementPolicyAdminService.class);


    /**
     * Adds a new XACML policy in to the system.
     *
     * @param policyDTO policy object
     * @throws EntitlementException throws
     */
    public void addPolicy(PolicyDTO policyDTO) throws EntitlementException {
        addOrUpdatePolicy(policyDTO, true);
    }


    /**
     * Adds XACML policies in bulk to the system.
     *
     * @param policies Array of policies.
     * @throws EntitlementException throws
     */
    public void addPolicies(PolicyDTO[] policies) throws EntitlementException {

        if (policies != null) {
            for (PolicyDTO policyDTO : policies) {
                addOrUpdatePolicy(policyDTO, true);
            }
        } else {
            throw new EntitlementException("No Entitlement policies are provided.");
        }
    }


    /**
     * Finds the policy file from given registry path and adds the policy
     *
     * @param policyRegistryPath given registry path
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws when fails or registry error
     *                                                                   occurs
     */
    public void importPolicyFromRegistry(String policyRegistryPath) throws EntitlementException {

        Registry registry;
        PolicyDTO policyDTO = new PolicyDTO();
        String policy = "";
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;

        // Finding from which registry by comparing prefix of resource path
        String resourceUri = policyRegistryPath.substring(policyRegistryPath.lastIndexOf(':') + 1);
        String registryIdentifier = policyRegistryPath.substring(0, policyRegistryPath.lastIndexOf(':'));
        if ("conf".equals(registryIdentifier)) {
            registry = (Registry) CarbonContext.getThreadLocalCarbonContext().
                    getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        } else {
            registry = (Registry) CarbonContext.getThreadLocalCarbonContext().
                    getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        }

        try {
            Resource resource = registry.get(resourceUri);
            inputStream = resource.getContentStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String stringLine;
            StringBuilder buffer = new StringBuilder(policy);
            while ((stringLine = bufferedReader.readLine()) != null) {
                buffer.append(stringLine);
            }
            policy = buffer.toString();
            policyDTO.setPolicy(policy.replaceAll(">\\s+<", "><"));
            addOrUpdatePolicy(policyDTO, true);
        } catch (RegistryException e) {
            log.error("Registry Error occurs while reading policy from registry", e);
            throw new EntitlementException("Error loading policy from carbon registry");
        } catch (IOException e) {
            log.error("I/O Error occurs while reading policy from registry", e);
            throw new EntitlementException("Error loading policy from carbon registry");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("Error occurs while closing inputStream", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occurs while closing inputStream", e);
                }
            }
        }
    }


    /**
     * Updates given policy
     *
     * @param policyDTO policy object
     * @throws EntitlementException throws if invalid policy
     */
    public void updatePolicy(PolicyDTO policyDTO) throws EntitlementException {
        addOrUpdatePolicy(policyDTO, false);
    }


    /**
     * Paginates policies
     *
     * @param policyTypeFilter   policy type to filter
     * @param policySearchString policy search String
     * @param pageNumber         page number
     * @param isPDPPolicy        whether this is a PDP policy or PAP policy
     * @return paginated and filtered policy set
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws
     */
    public PaginatedPolicySetDTO getAllPolicies(String policyTypeFilter, String policySearchString,
                                                int pageNumber, boolean isPDPPolicy) throws EntitlementException {

        List<PolicyDTO> policyDTOList = new ArrayList<>();
        PolicyDTO[] policyDTOs;

        if (isPDPPolicy) {
            policyDTOs = EntitlementAdminEngine.getInstance().getPolicyStoreManager().getLightPolicies();
        } else {
            policyDTOs = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getAllLightPolicyDTOs();
        }
        policySearchString = policySearchString.replace("*", ".*");
        Pattern pattern = Pattern.compile(policySearchString, Pattern.CASE_INSENSITIVE);
        for (PolicyDTO policyDTO : policyDTOs) {
            boolean useAttributeFiler = false;
            // Filter out policies based on policy type
            if (!policyTypeFilter.equals(EntitlementConstants.PolicyType.POLICY_ALL) &&
                    (!policyTypeFilter.equals(policyDTO.getPolicyType()) &&
                            !(EntitlementConstants.PolicyType.POLICY_ENABLED.equals(policyTypeFilter) &&
                                    policyDTO.isActive()) &&
                            !(EntitlementConstants.PolicyType.POLICY_DISABLED.equals(policyTypeFilter) &&
                                    !policyDTO.isActive()))) {
                continue;
            }

            if (!policySearchString.trim().isEmpty()) {

                if (!isPDPPolicy) {
                    // Filter out policies based on attribute value
                    PolicyDTO metaDataPolicyDTO = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().
                            getMetaDataPolicy(policyDTO.getPolicyId());
                    AttributeDTO[] attributeDTOs = metaDataPolicyDTO.getAttributeDTOs();
                    if (attributeDTOs != null) {
                        for (AttributeDTO attributeDTO : attributeDTOs) {
                            if (policySearchString.equalsIgnoreCase(attributeDTO.getAttributeValue())) {
                                useAttributeFiler = true;
                                break;
                            }
                        }
                    }
                }

                if (!useAttributeFiler) {
                    // Filter out policies based on policy Search String
                    if (!policySearchString.trim().isEmpty()) {
                        Matcher matcher = pattern.matcher(policyDTO.getPolicyId());
                        if (!matcher.matches()) {
                            continue;
                        }
                    }
                }
            }

            policyDTOList.add(policyDTO);
        }

        // Do the pagination and return the set of policies.
        return doPaging(pageNumber, policyDTOList.toArray(new PolicyDTO[0]));
    }


    /**
     * Gets policy for given policy id
     *
     * @param policyId    policy id
     * @param isPDPPolicy whether policy is PDP policy or PAP policy
     * @return returns policy
     * @throws EntitlementException throws
     */
    public PolicyDTO getPolicy(String policyId, boolean isPDPPolicy) throws EntitlementException {

        PolicyDTO policyDTO;

        if (isPDPPolicy) {
            policyDTO = EntitlementAdminEngine.getInstance().getPolicyStoreManager().getPolicy(policyId);
        } else {
            try {
                policyDTO = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getPolicy(policyId);
            } catch (EntitlementException e) {
                policyDTO = new PolicyDTO();
                policyDTO.setPolicy(policyId);
                handleStatus(EntitlementConstants.StatusTypes.GET_POLICY, policyDTO, false, e.getMessage());
                throw e;
            }
            handleStatus(EntitlementConstants.StatusTypes.GET_POLICY, policyDTO, true, null);
        }

        return policyDTO;
    }


    /**
     * Gets policy for given policy id and version
     *
     * @param policyId policy id
     * @param version  version of policy
     * @return returns policy
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws
     */
    public PolicyDTO getPolicyByVersion(String policyId, String version) throws EntitlementException {

        PolicyDTO policyDTO;

        try {
            PolicyDAO policyStore = new RegistryPolicyDAOImpl();
            policyDTO = policyStore.getPolicy(policyId, version);
        } catch (EntitlementException e) {
            policyDTO = new PolicyDTO();
            policyDTO.setPolicy(policyId);
            handleStatus(EntitlementConstants.StatusTypes.GET_POLICY, policyDTO, false, e.getMessage());
            throw e;
        }

        handleStatus(EntitlementConstants.StatusTypes.GET_POLICY, policyDTO, true, null);

        return policyDTO;
    }


    /**
     * Gets lightweight policy DTO for given policy id
     *
     * @param policyId policy id
     * @return returns policy
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws
     */
    public PolicyDTO getLightPolicy(String policyId) throws EntitlementException {
        return EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getLightPolicy(policyId);
    }


    /**
     * Removes policy for given policy object
     *
     * @param policyIds A <code>Array</code> of policy ids
     * @param dePromote whether these policy must be removed from PDP as well
     * @throws EntitlementException throws if fails
     */
    public void removePolicies(String[] policyIds, boolean dePromote) throws EntitlementException {

        if (policyIds == null || policyIds.length == 0) {
            throw new EntitlementException("No Entitlement policyId has been provided.");
        }

        for (String policyId : policyIds) {
            removePolicy(policyId, dePromote);
        }
    }


    /**
     * Removes policy for given policy object
     *
     * @param policyId  policyId
     * @param dePromote whether these policy must be removed from PDP as well
     * @throws EntitlementException throws
     */
    public void removePolicy(String policyId, boolean dePromote) throws EntitlementException {

        if (policyId == null) {
            throw new EntitlementException("Entitlement PolicyId can not be null.");
        }
        PAPPolicyStoreManager policyAdmin = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager();
        PolicyDTO oldPolicy = null;

        try {
            try {
                oldPolicy = getPolicy(policyId, false);
            } catch (Exception e) {
                // exception is ignore. as unwanted details are throws
            }
            if (oldPolicy == null) {
                oldPolicy = new PolicyDTO();
                oldPolicy.setPolicyId(policyId);
            }
            policyAdmin.removePolicy(policyId);
        } catch (EntitlementException e) {
            oldPolicy = new PolicyDTO();
            oldPolicy.setPolicyId(policyId);
            handleStatus(EntitlementConstants.StatusTypes.DELETE_POLICY, oldPolicy, false, e.getMessage());
            throw e;
        }
        handleStatus(EntitlementConstants.StatusTypes.DELETE_POLICY, oldPolicy, true, null);

        // policy remove from PDP.  this is done by separate thread
        if (dePromote) {
            publishToPDP(new String[] {policyId}, EntitlementConstants.PolicyPublish.ACTION_DELETE);
        }
    }


    /**
     * Returns the list of policy id available in PDP
     *
     * @param searchString search String
     * @return array of ids
     * @throws EntitlementException throws
     */
    public String[] getAllPolicyIds(String searchString) throws EntitlementException {

        String[] policyIds = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getPolicyIds();

        if (searchString == null || searchString.isEmpty()) {
            return policyIds;
        }

        String replacedSearchString = searchString.replace("*", ".*");
        Pattern pattern;
        try {
            pattern = Pattern.compile(replacedSearchString, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while compiling pattern with search string: " + replacedSearchString, e);
            }
            throw new EntitlementException("Invalid search string: " + searchString);
        }

        List<String> filteredPolicyIds = new ArrayList<>();
        for (String policyId : policyIds) {
            Matcher matcher = pattern.matcher(policyId);
            if (matcher.matches()) {
                filteredPolicyIds.add(policyId);
            }
        }
        return filteredPolicyIds.toArray(new String[0]);
    }


    /**
     * Gets subscriber details
     *
     * @param subscribeId subscriber id
     * @return subscriber details as SubscriberDTO
     * @throws EntitlementException throws, if any error
     */
    public PublisherDataHolder getSubscriber(String subscribeId) throws EntitlementException {

        SubscriberDAO subscriberManager = new RegistrySubscriberDAOImpl();
        return subscriberManager.getSubscriber(subscribeId, false);

    }


    /**
     * Gets all subscribers ids that is registered,
     *
     * @param searchString search String
     * @return subscriber's ids as String array
     * @throws EntitlementException throws, if fails
     */
    public String[] getSubscriberIds(String searchString) throws EntitlementException {

        SubscriberDAO subscriberManager = new RegistrySubscriberDAOImpl();
        String[] ids = subscriberManager.listSubscriberIds(searchString).toArray(new String[0]);

        if (ids != null) {
            return ids;
        } else {
            return new String[0];
        }
    }


    /**
     * Adds subscriber details
     *
     * @param holder subscriber data as PublisherDataHolder object
     * @throws EntitlementException throws, if fails
     */
    public void addSubscriber(PublisherDataHolder holder) throws EntitlementException {

        SubscriberDAO subscriberManager = new RegistrySubscriberDAOImpl();
        subscriberManager.addSubscriber(holder);

    }


    /**
     * Updates subscriber details
     *
     * @param holder subscriber data as PublisherDataHolder object
     * @throws EntitlementException throws, if fails
     */
    public void updateSubscriber(PublisherDataHolder holder) throws EntitlementException {

        SubscriberDAO subscriberManager = new RegistrySubscriberDAOImpl();
        subscriberManager.updateSubscriber(holder);

    }


    /**
     * Deletes subscriber details
     *
     * @param subscriberId subscriber id
     * @throws EntitlementException throws, if fails
     */
    public void deleteSubscriber(String subscriberId) throws EntitlementException {

        SubscriberDAO subscriberManager = new RegistrySubscriberDAOImpl();
        subscriberManager.removeSubscriber(subscriberId);
    }


    /**
     * Publishes given set of policies to all subscribers
     *
     * @param policyIds     policy ids to publish,  if null or empty, all policies are published
     * @param subscriberIds subscriber ids to publish,  if null or empty, policies are published to all subscribers
     * @param action        publishing action
     * @param version       version
     * @param enabled       whether policy must be enabled or not
     * @param order         order of the policy
     * @throws EntitlementException throws, if fails
     */
    public void publishPolicies(String[] policyIds, String[] subscriberIds, String action, String version,
                                boolean enabled, int order) throws EntitlementException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        if (policyIds == null || policyIds.length < 1) {
            policyIds = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getPolicyIds();
        }
        if (subscriberIds == null || subscriberIds.length < 1) {
            SubscriberDAO subscriberManager = new RegistrySubscriberDAOImpl();
            subscriberIds = subscriberManager.listSubscriberIds("*").toArray(new String[0]);
        }

        if (policyIds == null || policyIds.length < 1) {
            throw new EntitlementException("There are no policies to publish");
        }

        if (subscriberIds == null || subscriberIds.length < 1) {
            throw new EntitlementException("There are no subscribers to publish");
        }

        publisher.publishPolicy(policyIds, version, action, enabled, order, subscriberIds, null);
    }


    /**
     * Publishes given set of policies to all subscribers
     *
     * @param verificationCode verification code that is received by administrator to publish
     * @throws EntitlementException throws, if fails
     */
    public void publish(String verificationCode) throws EntitlementException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        publisher.publishPolicy(null, null, null, false, 0, null,
                verificationCode);

    }


    /**
     * Publishes given set of policies to PDP
     *
     * @param policyIds policyIds array
     */
    private void publishToPDP(String[] policyIds, String action) {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        String[] subscribers = new String[] {EntitlementConstants.PDP_SUBSCRIBER_ID};
        publisher.publishPolicy(policyIds, null, action, false, 0, subscribers, null);
    }


    /**
     * Publishes given set of policies to PDP
     *
     * @param policyIds policyIds
     */
    public void publishToPDP(String[] policyIds, String action, String version, boolean enabled, int order) {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        String[] subscribers = new String[] {EntitlementConstants.PDP_SUBSCRIBER_ID};
        publisher.publishPolicy(policyIds, version, action, enabled, order, subscribers, null);
    }


    /**
     * Rollbacks policy
     *
     * @param policyId policyId
     * @param version  version
     */
    public void rollBackPolicy(String policyId, String version) throws EntitlementException {

        PolicyDAO policyStore = new RegistryPolicyDAOImpl();
        PolicyDTO policyDTO = policyStore.getPolicy(policyId, version);
        addOrUpdatePolicy(policyDTO, false);

    }


    /**
     * Returns status data
     *
     * @param type status type
     * @param key  id
     * @return PaginatedStatusHolder
     */
    public PaginatedStatusHolder getStatusData(String about, String key, String type, String searchString,
                                               int pageNumber) throws EntitlementException {

        StatusDataDAO dataRetrievingHandler = null;
        Set<StatusDataDAO> handlers = EntitlementAdminEngine.getInstance().getPapStatusDataHandlers();
        for (StatusDataDAO handler : handlers) {
            if (handler instanceof RegistryStatusDataDAOImpl) {
                dataRetrievingHandler = handler;
                break;
            }
        }

        if (dataRetrievingHandler == null) {
            throw new EntitlementException("No Status Data Handler is defined for data retrieving");
        }
        StatusHolder[] holders = dataRetrievingHandler.getStatusData(about, key, type, searchString);
        return doPaging(pageNumber, holders);
    }


    /**
     * Gets policy publisher module data to populate in the UI
     *
     * @return array of PublisherDataHolders
     */
    public PublisherDataHolder[] getPublisherModuleData() {

        List<PublisherDataHolder> holders = EntitlementServiceComponent.getEntitlementConfig().
                getModulePropertyHolders(PolicyPublisherModule.class.getName());
        if (holders != null) {
            return holders.toArray(new PublisherDataHolder[0]);
        }

        return null;
    }


    /**
     * Gets entitlement data
     *
     * @param dataModule data module
     * @param category   category
     * @param regexp     regexp
     * @param dataLevel  data level
     * @param limit      limit
     * @return EntitlementTreeNodeDTO
     */
    public EntitlementTreeNodeDTO getEntitlementData(String dataModule, String category, String regexp, int dataLevel,
                                                     int limit) {

        EntitlementDataFinder dataFinder = EntitlementAdminEngine.getInstance().getEntitlementDataFinder();
        return dataFinder.getEntitlementData(dataModule, category, regexp, dataLevel, limit);
    }


    /**
     * Returns entitlement data modules
     *
     * @return array of EntitlementFinderDataHolders
     */
    public EntitlementFinderDataHolder[] getEntitlementDataModules() {

        EntitlementDataFinder dataFinder = EntitlementAdminEngine.getInstance().getEntitlementDataFinder();
        return dataFinder.getEntitlementDataModules();
    }


    /**
     * Returns policy versions
     *
     * @param policyId policyId
     * @return array of policy versions
     * @throws EntitlementException throws, if fails
     */
    public String[] getPolicyVersions(String policyId) throws EntitlementException {

        PolicyDAO policyStore = new RegistryPolicyDAOImpl();
        String[] versions = policyStore.getVersions(policyId);
        if (versions == null) {
            throw new EntitlementException("Error obtaining policy versions");
        }
        Arrays.sort(versions);
        return versions;
    }


    /**
     * Changes policy order
     *
     * @param policyId policyId
     * @param newOrder new order
     */
    public void orderPolicy(String policyId, int newOrder) {

        publishToPDP(new String[] {policyId}, EntitlementConstants.PolicyPublish.ACTION_ORDER, null,
                false, newOrder);
    }


    /**
     * Enables or disables the policy
     *
     * @param policyId policyId
     * @param enable enable or disable
     */
    public void enableDisablePolicy(String policyId, boolean enable) {

        if (enable) {
            publishToPDP(new String[] {policyId}, EntitlementConstants.PolicyPublish.ACTION_ENABLE);
        } else {
            publishToPDP(new String[] {policyId}, EntitlementConstants.PolicyPublish.ACTION_DISABLE);
        }
    }


    /**
     * De-promotes the policy
     *
     * @param policyId policyId
     */
    public void dePromotePolicy(String policyId) {
        publishToPDP(new String[] {policyId}, EntitlementConstants.PolicyPublish.ACTION_DELETE);
    }


    /**
     * Persists a XACML policy
     *
     * @param policyDTO PolicyDTO object
     * @param isAdd     whether this is policy adding or updating
     * @throws EntitlementException throws if invalid policy or if policy
     *                              with the same id exists
     */
    private void addOrUpdatePolicy(PolicyDTO policyDTO, boolean isAdd) throws EntitlementException {


        String regString = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.POLICY_ID_REGEXP_PATTERN);
        if (regString == null || regString.trim().isEmpty()) {
            regString = "[a-zA-Z0-9._:-]{3,100}$";
        }

        PAPPolicyStoreManager policyAdmin = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager();

        AbstractPolicy policyObj;
        String policyId;
        String policy;
        String operation = EntitlementConstants.StatusTypes.UPDATE_POLICY;
        if (isAdd) {
            operation = EntitlementConstants.StatusTypes.ADD_POLICY;
        }
        if (policyDTO == null) {
            throw new EntitlementException("Entitlement Policy can not be null.");
        }

        if (isAdd && policyDTO.getPolicy() == null) {
            throw new EntitlementException("Entitlement Policy can not be null.");
        }

        try {
            policy = policyDTO.getPolicy();
            if (policy != null) {
                policyDTO.setPolicy(policy.replaceAll(">\\s+<", "><"));
                if (!EntitlementUtil.validatePolicy(policyDTO)) {
                    throw new EntitlementException("Invalid Entitlement Policy. " +
                            "Policy is not valid according to XACML schema");
                }
                policyObj = PAPPolicyReader.getInstance(null).getPolicy(policy);
                if (policyObj != null) {
                    policyId = policyObj.getId().toASCIIString();
                    policyDTO.setPolicyId(policyId);
                    // All the policies won't be active at the time been added.
                    policyDTO.setActive(policyDTO.isActive());

                    if (policyId.contains("/")) {
                        throw new EntitlementException(" Policy Id cannot contain / characters. " +
                                "Please correct and upload again");
                    }
                    if (!policyId.matches(regString)) {
                        throw new EntitlementException("An Entitlement Policy Id is not valid. " +
                                "It contains illegal characters");
                    }

                    policyDTO.setPolicyId(policyId);
                    if (isAdd) {
                        if (policyAdmin.isExistPolicy(policyId)) {
                            throw new EntitlementException("An Entitlement Policy with the given Id already exists");
                        }
                    }
                } else {
                    throw new EntitlementException("Unsupported Entitlement Policy. Policy can not be parsed");
                }
            }
            policyAdmin.addOrUpdatePolicy(policyDTO);
        } catch (EntitlementException e) {
            handleStatus(operation, policyDTO, false, e.getMessage());
            throw e;
        }

        handleStatus(operation, policyDTO, true, null);


        // publish policy to PDP directly
        if (policyDTO.isPromote()) {
            if (isAdd) {
                publishToPDP(new String[] {policyDTO.getPolicyId()}, EntitlementConstants.PolicyPublish.ACTION_CREATE,
                        null, policyDTO.isActive(), policyDTO.getPolicyOrder());
            } else {
                publishToPDP(new String[] {policyDTO.getPolicyId()}, EntitlementConstants.PolicyPublish.ACTION_UPDATE,
                        null, policyDTO.isActive(), policyDTO.getPolicyOrder());
            }
        }
    }


    /**
     * Does internal pagination
     *
     * @param pageNumber page Number
     * @param policySet  set of policies
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
     * that reside in the given page.
     */
    private PaginatedPolicySetDTO doPaging(int pageNumber, PolicyDTO[] policySet) {

        PaginatedPolicySetDTO paginatedPolicySet = new PaginatedPolicySetDTO();
        if (policySet.length == 0) {
            paginatedPolicySet.setPolicySet(new PolicyDTO[0]);
            return paginatedPolicySet;
        }
        String itemsPerPage = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.ENTITLEMENT_ITEMS_PER_PAGE);
        if (itemsPerPage != null) {
            itemsPerPage = ServerConfiguration.getInstance().getFirstProperty("ItemsPerPage");
        }
        int itemsPerPageInt = PDPConstants.DEFAULT_ITEMS_PER_PAGE;
        if (itemsPerPage != null) {
            itemsPerPageInt = Integer.parseInt(itemsPerPage);
        }
        int numberOfPages = (int) Math.ceil((double) policySet.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        PolicyDTO[] returnedPolicySet = new PolicyDTO[itemsPerPageInt];

        for (int i = startIndex, j = 0; i < endIndex && i < policySet.length; i++, j++) {
            returnedPolicySet[j] = policySet[i];
        }

        paginatedPolicySet.setPolicySet(returnedPolicySet);
        paginatedPolicySet.setNumberOfPages(numberOfPages);

        return paginatedPolicySet;
    }


    /**
     * Does internal pagination
     *
     * @param pageNumber    page Number
     * @param statusHolders <code>StatusHolder</code>
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
     * that reside in the given page.
     */
    private PaginatedStatusHolder doPaging(int pageNumber, StatusHolder[] statusHolders) {

        PaginatedStatusHolder paginatedStatusHolder = new PaginatedStatusHolder();
        if (statusHolders.length == 0) {
            paginatedStatusHolder.setStatusHolders(new StatusHolder[0]);
            return paginatedStatusHolder;
        }
        String itemsPerPage = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.ENTITLEMENT_ITEMS_PER_PAGE);
        if (itemsPerPage != null) {
            itemsPerPage = ServerConfiguration.getInstance().getFirstProperty("ItemsPerPage");
        }
        int itemsPerPageInt = PDPConstants.DEFAULT_ITEMS_PER_PAGE;
        if (itemsPerPage != null) {
            itemsPerPageInt = Integer.parseInt(itemsPerPage);
        }
        int numberOfPages = (int) Math.ceil((double) statusHolders.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        StatusHolder[] returnedHolders = new StatusHolder[itemsPerPageInt];

        for (int i = startIndex, j = 0; i < endIndex && i < statusHolders.length; i++, j++) {
            returnedHolders[j] = statusHolders[i];
        }

        paginatedStatusHolder.setStatusHolders(returnedHolders);
        paginatedStatusHolder.setNumberOfPages(numberOfPages);

        return paginatedStatusHolder;
    }


    /**
     * Does internal pagination
     *
     * @param pageNumber page Number
     * @param ids        <code>String</code>
     * @return PaginatedStringDTO object containing the number of pages and the set of policies
     * that reside in the given page.
     */
    private PaginatedStringDTO doPagingString(int pageNumber, String[] ids) {

        PaginatedStringDTO paginatedStatusHolder = new PaginatedStringDTO();
        if (ids.length == 0) {
            paginatedStatusHolder.setStatusHolders(new String[0]);
            return paginatedStatusHolder;
        }

        String itemsPerPage = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(PDPConstants.ENTITLEMENT_ITEMS_PER_PAGE);
        if (itemsPerPage != null) {
            itemsPerPage = ServerConfiguration.getInstance().getFirstProperty("ItemsPerPage");
        }
        int itemsPerPageInt = PDPConstants.DEFAULT_ITEMS_PER_PAGE;
        if (itemsPerPage != null) {
            itemsPerPageInt = Integer.parseInt(itemsPerPage);
        }
        int numberOfPages = (int) Math.ceil((double) ids.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        String[] returnedHolders = new String[itemsPerPageInt];

        for (int i = startIndex, j = 0; i < endIndex && i < ids.length; i++, j++) {
            returnedHolders[j] = ids[i];
        }

        paginatedStatusHolder.setStatusHolders(returnedHolders);
        paginatedStatusHolder.setNumberOfPages(numberOfPages);

        return paginatedStatusHolder;
    }


    /**
     * Handles status
     *
     * @param action action
     * @param policyDTO policy
     * @param success success
     * @param message message.
     */
    private void handleStatus(String action, PolicyDTO policyDTO, boolean success, String message) {

        Set<StatusDataDAO> handlers = EntitlementServiceComponent.getEntitlementConfig().
                getPapStatusDataHandlers().keySet();

        String target = "PAP POLICY STORE";
        String targetAction = "";
        if (EntitlementConstants.StatusTypes.ADD_POLICY.equals(action) || EntitlementConstants.StatusTypes.
                UPDATE_POLICY.equals(action)) {
            targetAction = "PERSIST";
        } else if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(action)) {
            targetAction = "REMOVE";
        } else if (EntitlementConstants.StatusTypes.GET_POLICY.equals(action)) {
            targetAction = "LOAD";
        }

        String policyId = policyDTO.getPolicyId();
        if (policyId == null) {
            policyId = "UNKNOWN";
        }

        StatusHolder holder = new StatusHolder(action, policyId, policyDTO.getVersion(), target, targetAction, success,
                message);

        for (StatusDataDAO handler : handlers) {
            try {
                handler.handle(EntitlementConstants.Status.ABOUT_POLICY, holder);
            } catch (EntitlementException e) {
                log.error(e);
            }
        }
    }
}
