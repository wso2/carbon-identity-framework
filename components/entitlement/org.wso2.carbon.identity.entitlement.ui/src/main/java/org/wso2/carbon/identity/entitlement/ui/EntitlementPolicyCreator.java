/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.utils.exception.PolicyBuilderException;
import org.wso2.balana.utils.policy.PolicyBuilder;
import org.wso2.balana.utils.policy.dto.BasicPolicyDTO;
import org.wso2.balana.utils.policy.dto.ObligationElementDTO;
import org.wso2.balana.utils.policy.dto.PolicyElementDTO;
import org.wso2.balana.utils.policy.dto.PolicySetElementDTO;
import org.wso2.balana.utils.policy.dto.RequestElementDTO;
import org.wso2.balana.utils.policy.dto.RuleElementDTO;
import org.wso2.balana.utils.policy.dto.TargetElementDTO;
import org.wso2.carbon.identity.entitlement.common.PolicyEditorException;
import org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient;
import org.wso2.carbon.identity.entitlement.ui.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.PolicyRefIdDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.PolicySetDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.RequestDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.RuleDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorDTO;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyCreatorUtil;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil;

import java.util.List;

/**
 * create XACML policy and convert it to a String Object
 */
public class EntitlementPolicyCreator {

    private static Log log = LogFactory.getLog(EntitlementPolicyCreator.class);

    /**
     * Create XACML policy using the data received from basic policy wizard
     *
     * @param basicPolicyDTO BasicPolicyDTO
     * @return String object of the XACML policy
     * @throws PolicyEditorException throws
     */
    public String createBasicPolicy(BasicPolicyDTO basicPolicyDTO) throws PolicyEditorException {

        if (basicPolicyDTO == null) {
            throw new PolicyEditorException("Policy object can not be null");
        }

        try {
            return PolicyBuilder.getInstance().build(basicPolicyDTO);
        } catch (PolicyBuilderException e) {
            log.error(e);
            throw new PolicyEditorException("Error while building policy");
        }
    }


    /**
     * Create XACML policy using the data received from basic policy wizard
     *
     * @param policyDTO PolicyDTO
     * @return String object of the XACML policy
     * @throws PolicyEditorException throws
     */
    public String createPolicy(PolicyDTO policyDTO) throws PolicyEditorException {

        if (policyDTO == null) {
            throw new PolicyEditorException("Policy object can not be null");
        }

        PolicyElementDTO policyElementDTO = new PolicyElementDTO();
        policyElementDTO.setPolicyName(policyDTO.getPolicyId());
        policyElementDTO.setRuleCombiningAlgorithms(policyDTO.getRuleAlgorithm());
        policyElementDTO.setPolicyDescription(policyDTO.getDescription());
        policyElementDTO.setVersion(policyDTO.getVersion());

        if (policyDTO.getTargetDTO() != null) {
            TargetElementDTO targetElementDTO = PolicyEditorUtil.
                    createTargetElementDTO(policyDTO.getTargetDTO());
            policyElementDTO.setTargetElementDTO(targetElementDTO);
        }

        if (policyDTO.getRuleDTOs() != null) {
            for (RuleDTO ruleDTO : policyDTO.getRuleDTOs()) {
                RuleElementDTO ruleElementDTO = PolicyEditorUtil.createRuleElementDTO(ruleDTO);
                policyElementDTO.addRuleElementDTO(ruleElementDTO);
            }
        }

        if (policyDTO.getObligationDTOs() != null) {
            List<ObligationElementDTO> obligationElementDTOs = PolicyEditorUtil.
                    createObligation(policyDTO.getObligationDTOs());
            policyElementDTO.setObligationElementDTOs(obligationElementDTOs);
        }

        try {
            return PolicyBuilder.getInstance().build(policyElementDTO);
        } catch (PolicyBuilderException e) {
            throw new PolicyEditorException("Error while building XACML Policy");
        }
    }


    /**
     * Create XACML policy using the data received from basic policy wizard
     *
     * @param policyEditorDTO complete policy editor object
     * @return String object of the XACML policy
     * @throws PolicyEditorException throws
     */
    public String createSOAPolicy(SimplePolicyEditorDTO policyEditorDTO) throws PolicyEditorException {

        return PolicyEditorUtil.createSOAPolicy(policyEditorDTO);
    }


    /**
     * Create policy set using the added policy ot policy sets
     *
     * @param policySetDTO policy set element
     * @param client
     * @return String object of the XACML policy Set
     * @throws PolicyEditorException throws
     */
    public String createPolicySet(PolicySetDTO policySetDTO,
                                  EntitlementPolicyAdminServiceClient client) throws PolicyEditorException {

        if (policySetDTO == null) {
            throw new PolicyEditorException("Policy Set object can not be null");
        }

        PolicySetElementDTO policyElementDTO = new PolicySetElementDTO();
        policyElementDTO.setPolicySetId(policySetDTO.getPolicySetId());
        policyElementDTO.setPolicyCombiningAlgId(policySetDTO.getPolicyCombiningAlgId());
        policyElementDTO.setDescription(policySetDTO.getDescription());
        policyElementDTO.setVersion(policySetDTO.getVersion());

        if (policySetDTO.getTargetDTO() != null) {
            TargetElementDTO targetElementDTO = PolicyEditorUtil.
                    createTargetElementDTO(policySetDTO.getTargetDTO());
            policyElementDTO.setTargetElementDTO(targetElementDTO);
        }

        if (policySetDTO.getPolicyIdReferences() != null) {

            for (PolicyRefIdDTO dto : policySetDTO.getPolicyRefIdDTOs()) {
                if (dto.isReferenceOnly()) {
                    if (dto.isPolicySet()) {
                        policyElementDTO.getPolicySetIdReferences().add(dto.getId());
                    } else {
                        policyElementDTO.getPolicyIdReferences().add(dto.getId());
                    }
                } else {
                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policyDTO = null;
                    try {
                        policyDTO = client.getPolicy(dto.getId(), false);
                    } catch (Exception e) {
                        //ignore
                    }
                    if (policyDTO != null && policyDTO.getPolicy() != null) {
                        if (dto.isPolicySet()) {
                            policyElementDTO.getPolicySets().add(policyDTO.getPolicy());
                        } else {
                            policyElementDTO.getPolicies().add(policyDTO.getPolicy());
                        }
                    }
                }
            }
        }

        if (policySetDTO.getObligations() != null) {
            List<ObligationElementDTO> obligationElementDTOs = PolicyEditorUtil.
                    createObligation(policySetDTO.getObligations());
            policyElementDTO.setObligationElementDTOs(obligationElementDTOs);
        }

        try {
            return PolicyBuilder.getInstance().build(policyElementDTO);
        } catch (PolicyBuilderException e) {
            throw new PolicyEditorException("Error while building XACML Policy");
        }
    }


    /**
     * Create basic XACML request
     *
     * @param requestDTO request element
     * @return String object of the XACML request
     * @throws EntitlementPolicyCreationException throws
     */
    public String createBasicRequest(RequestDTO requestDTO)
            throws EntitlementPolicyCreationException, PolicyEditorException {
        try {

            RequestElementDTO requestElementDTO = PolicyCreatorUtil.createRequestElementDTO(requestDTO);
            return PolicyBuilder.getInstance().buildRequest(requestElementDTO);
        } catch (PolicyBuilderException e) {
            throw new PolicyEditorException("Error while building XACML Request");
        }

    }
}
