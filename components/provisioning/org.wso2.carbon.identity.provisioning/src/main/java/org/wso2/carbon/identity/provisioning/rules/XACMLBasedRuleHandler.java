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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.balana.utils.exception.PolicyBuilderException;
import org.wso2.balana.utils.policy.PolicyBuilder;
import org.wso2.balana.utils.policy.dto.RequestElementDTO;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementService;
import org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.ui.dto.RequestDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.RowDTO;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyCreatorUtil;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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


    public boolean isAuthorized(String tenantDomainName, ProvisioningEntity provisioningEntity, String idPName,
                                String connectorType) {

        if (log.isDebugEnabled()) {
            log.debug("In policy provisioning flow...");
        }

        try {
            RequestDTO requestDTO = createRequestDTO(tenantDomainName, provisioningEntity, idPName, connectorType);
            RequestElementDTO requestElementDTO = PolicyCreatorUtil.createRequestElementDTO(requestDTO);

            String requestString = PolicyBuilder.getInstance().buildRequest(requestElementDTO);
            if (log.isDebugEnabled()) {
                log.debug("XACML request :\n" + requestString);
            }

            EntitlementService entitlementService = new EntitlementService();

            try {
                entitlementService.getDecisionByAttributes("support", "federal", "support", new String[]{"test"});
            } catch (EntitlementException e) {
                e.printStackTrace();
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


    private RequestDTO createRequestDTO(String tenantDomainName, ProvisioningEntity provisioningEntity, String idPName,
                                        String connectorType) {

        List<RowDTO> rowDTOs = new ArrayList<>();
        RowDTO tenatDomainDTO =
                createRowDTO(tenantDomainName, "urn:oasis:names:tc:xacml:1" +
                                               ".0:resource:tenantDomain", "userInfo");
        RowDTO userDTO =
                createRowDTO(provisioningEntity.getEntityName(), "urn:oasis:names:tc:xacml:1" +
                                                                 ".0:resource:user", "userInfo");
        RowDTO idpNameDTO =
                createRowDTO(idPName, "urn:oasis:names:tc:xacml:1" +
                                      ".0:resource:idpName", "idp");
        RowDTO connectorTypeDTO =
                createRowDTO(connectorType, "urn:oasis:names:tc:xacml:1" +
                                            ".0:resource:connectorType", "idp");

        Iterator<Map.Entry<String, String>> claimIterator = provisioningEntity.getInboundAttributes().entrySet
                ().iterator();
        while (claimIterator.hasNext()) {
            Map.Entry<String, String> claim = claimIterator.next();
            String claimUri = claim.getKey();
            String claimValue = claim.getValue();
            RowDTO claimRowDTO =
                    createRowDTO(claimValue, "urn:oasis:names:tc:xacml:1" +
                                             ".0:resource:" + claimUri, "claims");
            rowDTOs.add(claimRowDTO);
        }

        rowDTOs.add(tenatDomainDTO);
        rowDTOs.add(userDTO);
        rowDTOs.add(idpNameDTO);
        rowDTOs.add(connectorTypeDTO);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setRowDTOs(rowDTOs);
        return requestDTO;
    }

    private RowDTO createRowDTO(String resourceName, String attributeId, String categoryValue) {

        RowDTO rowDTOTenant = new RowDTO();
        rowDTOTenant.setAttributeValue(resourceName);
        rowDTOTenant.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTOTenant.setAttributeId(attributeId);
        rowDTOTenant.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:".concat(categoryValue));
        return rowDTOTenant;
    }

    private boolean evaluateXACMLResponse(String xacmlResponse) throws IdentityProvisioningException {

        try {
            DocumentBuilder db = IdentityUtil.getSecuredDocumentBuilderFactory().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xacmlResponse));
            Document doc = db.parse(is);

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/Response/Result/Decision/text()");
            String decision = (String) expr.evaluate(doc, XPathConstants.STRING);
            if (decision.equalsIgnoreCase(EntitlementPolicyConstants.RULE_EFFECT_PERMIT)
                || decision.equalsIgnoreCase(EntitlementPolicyConstants.RULE_EFFECT_NOT_APPLICABLE)) {
                return true;
            }

        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            throw new IdentityProvisioningException("Exception occurred while xacmlResponse processing", e);
        }
        return false;
    }
}
