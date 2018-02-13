/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.identity.provisioning.rules;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.balana.utils.exception.PolicyBuilderException;
import org.wso2.balana.utils.policy.PolicyBuilder;
import org.wso2.balana.utils.policy.dto.RequestElementDTO;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.common.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.common.dto.RequestDTO;
import org.wso2.carbon.identity.entitlement.common.dto.RowDTO;
import org.wso2.carbon.identity.entitlement.common.util.PolicyCreatorUtil;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.ProvisioningOperation;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class XACMLBasedRuleHandler {

    private static final Log log = LogFactory.getLog(XACMLBasedRuleHandler.class);
    private static volatile XACMLBasedRuleHandler instance;

    public static XACMLBasedRuleHandler getInstance() {

        if (instance == null) {
            synchronized (XACMLBasedRuleHandler.class) {
                if (instance == null) {
                    instance = new XACMLBasedRuleHandler();
                }
            }
        }
        return instance;
    }


    public boolean isAllowedToProvision(String tenantDomainName, ProvisioningEntity provisioningEntity,
                                        ServiceProvider serviceProvider,
                                        String idPName,
                                        String connectorType) {

        if (log.isDebugEnabled()) {
            log.debug("In policy provisioning flow...");
        }

        try {
            RequestDTO requestDTO = createRequestDTO(tenantDomainName, provisioningEntity, serviceProvider,
                                                     idPName, connectorType);
            RequestElementDTO requestElementDTO = PolicyCreatorUtil.createRequestElementDTO(requestDTO);

            String requestString = PolicyBuilder.getInstance().buildRequest(requestElementDTO);
            if (log.isDebugEnabled()) {
                log.debug("XACML request :\n" + requestString);
            }

            String responseString =
                    ProvisioningServiceDataHolder.getInstance().getEntitlementService().getDecision(requestString);
            if (log.isDebugEnabled()) {
                log.debug("XACML response :\n" + responseString);
            }
            Boolean isAuthorized = evaluateXACMLResponse(responseString);
            if (isAuthorized) {
                return true;
            }
        } catch (PolicyBuilderException e) {
            log.error("Policy Builder Exception occurred", e);
        } catch (EntitlementException e) {
            log.error("Entitlement Exception occurred", e);
        } catch (IdentityProvisioningException e) {
            log.error("Error when evaluating the XACML response", e);
        }
        return false;
    }


    private RequestDTO createRequestDTO(String tenantDomainName, ProvisioningEntity provisioningEntity,
                                        ServiceProvider serviceProvider,
                                        String idPName,
                                        String connectorType) {
        List<RowDTO> rowDTOs = new ArrayList<>();
        //Setting up user-info category
        RowDTO tenatDomainDTO =
                createRowDTO(tenantDomainName, EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_TENAT_DOMAIN,
                             ProvisioningRuleConstanats.XACML_CATAGORY_USER);
        RowDTO userDTO =
                createRowDTO(provisioningEntity.getEntityName(), EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_USER, ProvisioningRuleConstanats
                                     .XACML_CATAGORY_USER);

        RowDTO spNameDTO =
                createRowDTO(serviceProvider.getApplicationName(),
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_SP_NAME,
                             ProvisioningRuleConstanats.XACML_CATAGORY_SERVICE_PROVIDER);
        RowDTO spTenantDomainNameDTO =
                createRowDTO(serviceProvider.getOwner().getTenantDomain(),
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_SP_TENANT_DOMAIN,
                             ProvisioningRuleConstanats.XACML_CATAGORY_SERVICE_PROVIDER);
        //Setting up IDP category
        RowDTO idpNameDTO =
                createRowDTO(idPName,
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_IDP_NAME,
                             ProvisioningRuleConstanats.XACML_CATAGORY_IDENTITY_PROVIDER);
        RowDTO connectorTypeDTO =
                createRowDTO(connectorType,
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_CONNECTOR_TYPE,
                             ProvisioningRuleConstanats.XACML_CATAGORY_IDENTITY_PROVIDER);

        //Setting up Identity Action
        RowDTO provisioningFlowDTO =
                createRowDTO(ProvisioningRuleConstanats.IDENTITY_ACTION_PROVISIONING,
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_IDENTITY_ACTION,
                             ProvisioningRuleConstanats.XACML_CATAGORY_IDENTITY_ACTION);

        if (provisioningEntity.getOperation().equals(ProvisioningOperation.POST)) {
            RowDTO provisioningClaimGroupDTO =
                    createRowDTO(StringUtils.substringBetween(provisioningEntity.getAttributes().get(ClaimMapping.build(
                            IdentityProvisioningConstants.GROUP_CLAIM_URI, null, null, false)).toString(), "[", "]"),
                                 EntitlementPolicyConstants.STRING_DATA_TYPE,
                                 ProvisioningRuleConstanats.XACML_ATTRIBUTE_CLAIM_GROUPS,
                                 ProvisioningRuleConstanats.XACML_CATAGORY_PROVISIONING);
            rowDTOs.add(provisioningClaimGroupDTO);
        }
        RowDTO provisioningOperationDTO =
                createRowDTO(provisioningEntity.getOperation().toString(),
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_OPERATION,
                             ProvisioningRuleConstanats.XACML_CATAGORY_PROVISIONING);

        if (provisioningEntity.getInboundAttributes() != null) {
            Iterator<Map.Entry<String, String>> claimIterator = provisioningEntity.getInboundAttributes().entrySet
                    ().iterator();
            while (claimIterator.hasNext()) {
                Map.Entry<String, String> claim = claimIterator.next();
                String claimUri = claim.getKey();
                String claimValue = claim.getValue();
                RowDTO claimRowDTO =
                        createRowDTO(claimValue, EntitlementPolicyConstants.STRING_DATA_TYPE,
                                     claimUri, ProvisioningRuleConstanats.XACML_CATAGORY_USER);
                rowDTOs.add(claimRowDTO);
            }
        }
        RowDTO environmentTypeDTO =
                createRowDTO(tenantDomainName,
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_ENVIRONMENT,
                             ProvisioningRuleConstanats.XACML_CATAGORY_ENVIRONMENT);
        RowDTO dateDTO =
                createRowDTO(getCurrentDateTime(ProvisioningRuleConstanats.DATE_FORMAT),
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_DATE,
                             ProvisioningRuleConstanats.XACML_CATAGORY_ENVIRONMENT);
        RowDTO timeDTO =
                createRowDTO(getCurrentDateTime(ProvisioningRuleConstanats.TIME_FORMAT),
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_TIME,
                             ProvisioningRuleConstanats.XACML_CATAGORY_ENVIRONMENT);
        RowDTO dateTimeDTO =
                createRowDTO(getCurrentDateTime(ProvisioningRuleConstanats.DATE_TIME_FORMAT),
                             EntitlementPolicyConstants.STRING_DATA_TYPE,
                             ProvisioningRuleConstanats.XACML_ATTRIBUTE_DATE_TIME,
                             ProvisioningRuleConstanats.XACML_CATAGORY_ENVIRONMENT);

        rowDTOs.add(tenatDomainDTO);
        rowDTOs.add(userDTO);
        rowDTOs.add(spNameDTO);
        rowDTOs.add(spTenantDomainNameDTO);
        rowDTOs.add(idpNameDTO);
        rowDTOs.add(provisioningFlowDTO);
        rowDTOs.add(connectorTypeDTO);
        rowDTOs.add(environmentTypeDTO);
        rowDTOs.add(dateDTO);
        rowDTOs.add(timeDTO);
        rowDTOs.add(dateTimeDTO);
        rowDTOs.add(provisioningOperationDTO);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setRowDTOs(rowDTOs);
        return requestDTO;
    }

    private RowDTO createRowDTO(String resourceName, String dataType, String attributeId, String categoryValue) {

        RowDTO rowDTOTenant = new RowDTO();
        rowDTOTenant.setAttributeValue(resourceName);
        rowDTOTenant.setAttributeDataType(dataType);
        rowDTOTenant.setAttributeId(attributeId);
        rowDTOTenant.setCategory(categoryValue);
        return rowDTOTenant;
    }

    private boolean evaluateXACMLResponse(String xacmlResponse) throws IdentityProvisioningException {

        try {
            DocumentBuilderFactory documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xacmlResponse));
            Document doc = db.parse(is);

            String decision = "";
            NodeList decisionNode = doc.getDocumentElement().getElementsByTagName(
                            ProvisioningRuleConstanats.XACML_RESPONSE_DECISION_NODE);
            if (decisionNode != null && decisionNode.item(0) != null) {
                decision = decisionNode.item(0).getTextContent();
            }
            if (decision.equalsIgnoreCase(EntitlementPolicyConstants.RULE_EFFECT_PERMIT)
                || decision.equalsIgnoreCase(EntitlementPolicyConstants.RULE_EFFECT_NOT_APPLICABLE)) {
                return true;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IdentityProvisioningException("Exception occurred while xacmlResponse processing", e);
        }
        return false;
    }

    private String getCurrentDateTime(String dateTimeFormat) {
        DateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime()).toString();
    }
}
