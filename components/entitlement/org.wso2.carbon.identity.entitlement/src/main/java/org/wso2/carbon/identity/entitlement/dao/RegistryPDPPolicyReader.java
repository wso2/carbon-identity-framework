/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.pap.PAPPolicyReader;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Registry policy reader
 */
public class RegistryPDPPolicyReader implements PDPPolicyReaderModule {

    /**
     * logger
     */
    private static final Log log = LogFactory.getLog(RegistryPDPPolicyReader.class);
    /**
     * Governance registry instance of current tenant
     */
    private final Registry registry;
    /**
     * policy store path of the registry
     */
    private final String policyStorePath;

    /**
     * constructor
     *
     * @param registry        registry instance
     * @param policyStorePath policy store path of the registry
     */
    public RegistryPDPPolicyReader(Registry registry, String policyStorePath) {
        this.registry = registry;
        this.policyStorePath = policyStorePath;
    }

    /**
     * Reads given policy resource as PolicyDTO
     *
     * @param policyId policy id
     * @return PolicyDTO
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO readPolicy(String policyId) throws EntitlementException {

        Resource resource;

        resource = getPolicyResource(policyId);

        if (resource == null) {
            return new PolicyDTO();
        }

        return readPolicy(resource);
    }

    /**
     * Reads All ordered active policies as PolicyDTO
     *
     * @param active only return active policies
     * @param order  return ordered policy
     * @return Array of PolicyDTO
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO[] readAllPolicies(boolean active, boolean order) throws EntitlementException {

        Resource[] resources;
        resources = getAllPolicyResource();

        if (resources == null) {
            return new PolicyDTO[0];
        }
        List<PolicyDTO> policyDTOList = new ArrayList<>();
        for (Resource resource : resources) {
            PolicyDTO policyDTO = readPolicy(resource);
            if (active) {
                if (policyDTO.isActive()) {
                    policyDTOList.add(policyDTO);
                }
            } else {
                policyDTOList.add(policyDTO);
            }
        }

        PolicyDTO[] policyDTOs = policyDTOList.toArray(new PolicyDTO[0]);

        if (order) {
            Arrays.sort(policyDTOs, new PolicyOrderComparator());
        }
        return policyDTOs;
    }


    /**
     * This returns all the policy ids as String list. Here we assume registry resource name as
     * the policy id.
     *
     * @return policy ids as String[]
     * @throws EntitlementException throws if fails
     */
    @Override
    public String[] getAllPolicyIds() throws EntitlementException {

        String path;
        Collection collection;
        String[] children;
        List<String> resources = new ArrayList<>();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all entitlement policies");
        }

        try {
            path = policyStorePath;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();
            for (String child : children) {
                String id = child.substring(child.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                resources.add(id);
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy resources", e);
            throw new EntitlementException("Error while retrieving entitlement policy resources", e);
        }

        return resources.toArray(new String[0]);
    }

    /**
     * Reads PolicyDTO for given registry resource
     *
     * @param resource Registry resource
     * @return PolicyDTO
     * @throws EntitlementException throws, if fails
     */
    private PolicyDTO readPolicy(Resource resource) throws EntitlementException {

        String policy;
        AbstractPolicy absPolicy;
        PolicyDTO dto;

        try {
            if (resource.getContent() == null) {
                throw new EntitlementException("Error while loading entitlement policy. Policy content is null");
            }
            policy = new String((byte[]) resource.getContent(), StandardCharsets.UTF_8);
            absPolicy = PAPPolicyReader.getInstance(null).getPolicy(policy);
            dto = new PolicyDTO();
            dto.setPolicyId(absPolicy.getId().toASCIIString());
            dto.setPolicy(policy);
            String policyOrder = resource.getProperty("order");
            if (policyOrder != null) {
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            String policyActive = resource.getProperty("active");
            if (policyActive != null) {
                dto.setActive(Boolean.parseBoolean(policyActive));
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy", e);
            throw new EntitlementException("Error while loading entitlement policy", e);
        }
    }

    /**
     * This reads the policy combining algorithm from registry resource property
     *
     * @return policy combining algorithm as String
     * @throws EntitlementException throws
     */
    @Override
    public String readPolicyCombiningAlgorithm() throws EntitlementException {
        try {
            Collection policyCollection = null;
            if (registry.resourceExists(policyStorePath)) {
                policyCollection = (Collection) registry.get(policyStorePath);
            }
            if (policyCollection != null) {
                return policyCollection.getProperty("globalPolicyCombiningAlgorithm");
            }
            return null;
        } catch (RegistryException e) {
            log.error("Error while reading policy combining algorithm", e);
            throw new EntitlementException("Error while reading policy combining algorithm", e);
        }
    }

    /**
     * This returns given policy as Registry resource
     *
     * @param policyId policy id
     * @return policy as Registry resource
     * @throws EntitlementException throws, if fails
     */
    private Resource getPolicyResource(String policyId) throws EntitlementException {
        String path;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving entitlement policy");
        }

        try {
            path = policyStorePath + policyId;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            return registry.get(path);
        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy : " + policyId, e);
            throw new EntitlementException("Error while retrieving entitlement policy : " + policyId, e);
        }
    }

    /**
     * This returns all the policies as Registry resources.
     *
     * @return policies as Resource[]
     * @throws EntitlementException throws if fails
     */
    private Resource[] getAllPolicyResource() throws EntitlementException {

        String path;
        Collection collection;
        List<Resource> resources = new ArrayList<>();
        String[] children;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all entitlement policies");
        }

        try {
            path = policyStorePath;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();

            for (String aChildren : children) {
                resources.add(registry.get(aChildren));
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy", e);
            throw new EntitlementException("Error while retrieving entitlement policies", e);
        }

        return resources.toArray(new Resource[0]);
    }

}
