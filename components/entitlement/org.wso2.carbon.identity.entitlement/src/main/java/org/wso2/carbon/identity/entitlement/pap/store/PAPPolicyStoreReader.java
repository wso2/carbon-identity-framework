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
package org.wso2.carbon.identity.entitlement.pap.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.pap.PAPPolicyReader;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PAPPolicyStoreReader {

    // the optional logger used for error reporting
    private static Log log = LogFactory.getLog(PAPPolicyStoreReader.class);

    private PAPPolicyStore store;

    /**
     * @param store
     */
    public PAPPolicyStoreReader(PAPPolicyStore store) {
        this.store = store;
    }


    /**
     * @param policyId
     * @param finder
     * @return
     * @throws EntitlementException
     */
    public synchronized AbstractPolicy readPolicy(String policyId, PolicyFinder finder)
            throws EntitlementException {
        Resource resource = store.getPolicy(policyId, PDPConstants.ENTITLEMENT_POLICY_PAP);
        if (resource != null) {
            try {
                String policy = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
                return PAPPolicyReader.getInstance(null).getPolicy(policy);
            } catch (RegistryException e) {
                log.error("Error while parsing entitlement policy", e);
                throw new EntitlementException("Error while loading entitlement policy");
            }
        }
        return null;
    }

    /**
     * Reads All policies as Light Weight PolicyDTO
     *
     * @return Array of PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO[] readAllLightPolicyDTOs() throws EntitlementException {

        String[] resources = null;
        resources = store.getAllPolicyIds();

        if (resources == null) {
            return new PolicyDTO[0];
        }

        List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();

        for (String resource : resources) {
            PolicyDTO policyDTO = readLightPolicyDTO(resource);
            policyDTOList.add(policyDTO);
        }

        return policyDTOList.toArray(new PolicyDTO[policyDTOList.size()]);
    }

    /**
     * Reads PolicyDTO for given policy id
     *
     * @param policyId policy id
     * @return PolicyDTO
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readPolicyDTO(String policyId) throws EntitlementException {
        Resource resource = null;
        PolicyDTO dto = null;
        try {
            resource = store.getPolicy(policyId, PDPConstants.ENTITLEMENT_POLICY_PAP);
            if (resource == null) {
                log.error("Policy does not exist in the system with id " + policyId);
                throw new EntitlementException("Policy does not exist in the system with id " + policyId);
            }

            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(new String((byte[]) resource.getContent(), Charset.forName("UTF-8")));
            dto.setActive(Boolean.parseBoolean(resource.getProperty(PDPConstants.ACTIVE_POLICY)));
            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if (policyOrder != null) {
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));
            String version = resource.getProperty(PDPConstants.POLICY_VERSION);
            if (version != null) {
                dto.setVersion(version);
            }
            String lastModifiedTime = resource.getProperty(PDPConstants.LAST_MODIFIED_TIME);
            if (lastModifiedTime != null) {
                dto.setLastModifiedTime(lastModifiedTime);
            }
            String lastModifiedUser = resource.getProperty(PDPConstants.LAST_MODIFIED_USER);
            if (lastModifiedUser != null) {
                dto.setLastModifiedUser(lastModifiedUser);
            }
            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if (policyReferences != null && policyReferences.trim().length() > 0) {
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if (policySetReferences != null && policySetReferences.trim().length() > 0) {
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }
            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(PDPConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if (basicPolicyEditorMetaDataAmount != null) {
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for (int i = 0; i < amount; i++) {
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }

    /**
     * Checks whether policy is exist for given policy id
     *
     * @param policyId policy id
     * @return true of false
     */
    public boolean isExistPolicy(String policyId) {
        Resource resource = null;
        try {
            resource = store.getPolicy(policyId, PDPConstants.ENTITLEMENT_POLICY_PAP);
            if (resource != null) {
                return true;
            }
        } catch (EntitlementException e) {
            //ignore
        }
        return false;
    }


    /**
     * Reads Light Weight PolicyDTO for given policy id
     *
     * @param policyId policy id
     * @return PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readLightPolicyDTO(String policyId) throws EntitlementException {

        Resource resource = null;
        PolicyDTO dto = null;
        resource = store.getPolicy(policyId, PDPConstants.ENTITLEMENT_POLICY_PAP);
        if (resource == null) {
            return null;
        }
        dto = new PolicyDTO();
        dto.setPolicyId(policyId);
        String version = resource.getProperty(PDPConstants.POLICY_VERSION);
        if (version != null) {
            dto.setVersion(version);
        }
        String lastModifiedTime = resource.getProperty(PDPConstants.LAST_MODIFIED_TIME);
        if (lastModifiedTime != null) {
            dto.setLastModifiedTime(lastModifiedTime);
        }
        String lastModifiedUser = resource.getProperty(PDPConstants.LAST_MODIFIED_USER);
        if (lastModifiedUser != null) {
            dto.setLastModifiedUser(lastModifiedUser);
        }
        dto.setActive(Boolean.parseBoolean(resource.getProperty(PDPConstants.ACTIVE_POLICY)));
        String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
        if (policyOrder != null) {
            dto.setPolicyOrder(Integer.parseInt(policyOrder));
        } else {
            dto.setPolicyOrder(0);
        }
        dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

        String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
        if (policyReferences != null && policyReferences.trim().length() > 0) {
            dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
        }

        String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
        if (policySetReferences != null && policySetReferences.trim().length() > 0) {
            dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
        }

        dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));

        return dto;
    }


    /**
     * Reads Light Weight PolicyDTO with Attribute meta data for given policy id
     *
     * @param policyId policy id
     * @return PolicyDTO but don not contains XACML policy
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readMetaDataPolicyDTO(String policyId) throws EntitlementException {
        Resource resource = null;
        PolicyDTO dto = null;

        resource = store.getPolicy(policyId, PDPConstants.ENTITLEMENT_POLICY_PAP);
        if (resource == null) {
            return null;
        }
        dto = new PolicyDTO();
        dto.setPolicyId(policyId);
        dto.setActive(Boolean.parseBoolean(resource.getProperty(PDPConstants.ACTIVE_POLICY)));
        String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
        if (policyOrder != null) {
            dto.setPolicyOrder(Integer.parseInt(policyOrder));
        } else {
            dto.setPolicyOrder(0);
        }

        String version = resource.getProperty(PDPConstants.POLICY_VERSION);
        if (version != null) {
            dto.setVersion(version);
        }
        String lastModifiedTime = resource.getProperty(PDPConstants.LAST_MODIFIED_TIME);
        if (lastModifiedTime != null) {
            dto.setLastModifiedTime(lastModifiedTime);
        }
        String lastModifiedUser = resource.getProperty(PDPConstants.LAST_MODIFIED_USER);
        if (lastModifiedUser != null) {
            dto.setLastModifiedUser(lastModifiedUser);
        }
        dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

        String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
        if (policyReferences != null && policyReferences.trim().length() > 0) {
            dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
        }

        String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
        if (policySetReferences != null && policySetReferences.trim().length() > 0) {
            dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
        }

        dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
        String basicPolicyEditorMetaDataAmount = resource.getProperty(PDPConstants.
                BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
        if (basicPolicyEditorMetaDataAmount != null) {
            int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
            String[] basicPolicyEditorMetaData = new String[amount];
            for (int i = 0; i < amount; i++) {
                basicPolicyEditorMetaData[i] = resource.
                        getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
            }
            dto.setPolicyEditorData(basicPolicyEditorMetaData);
        }
        PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
        dto.setAttributeDTOs(policyAttributeBuilder.
                getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
        return dto;

    }

    /**
     * Reads PolicyDTO for given registry resource
     *
     * @param resource Registry resource
     * @return PolicyDTO
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readPolicyDTO(Resource resource) throws EntitlementException {
        String policy = null;
        String policyId = null;
        AbstractPolicy absPolicy = null;
        PolicyDTO dto = null;
        try {
            policy = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
            absPolicy = PAPPolicyReader.getInstance(null).getPolicy(policy);
            policyId = absPolicy.getId().toASCIIString();
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(policy);
            dto.setActive(Boolean.parseBoolean(resource.getProperty(PDPConstants.ACTIVE_POLICY)));
            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if (policyOrder != null) {
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            String version = resource.getProperty(PDPConstants.POLICY_VERSION);
            if (version != null) {
                dto.setVersion(version);
            }
            String lastModifiedTime = resource.getProperty(PDPConstants.LAST_MODIFIED_TIME);
            if (lastModifiedTime != null) {
                dto.setLastModifiedTime(lastModifiedTime);
            }
            String lastModifiedUser = resource.getProperty(PDPConstants.LAST_MODIFIED_USER);
            if (lastModifiedUser != null) {
                dto.setLastModifiedUser(lastModifiedUser);
            }
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));
            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if (policyReferences != null && policyReferences.trim().length() > 0) {
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if (policySetReferences != null && policySetReferences.trim().length() > 0) {
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(PDPConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if (basicPolicyEditorMetaDataAmount != null) {
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for (int i = 0; i < amount; i++) {
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }
}
