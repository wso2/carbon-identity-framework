<!--
~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ObligationDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicyRefIdDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.TargetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%

    String policyOrderOrder = entitlementPolicyBean.getPolicyReferenceOrder();
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String policyName = entitlementPolicyBean.getPolicyName();
    String algorithmName = entitlementPolicyBean.getAlgorithmName();
    String policyDescription = entitlementPolicyBean.getPolicyDescription();

    TargetDTO targetDTO = entitlementPolicyBean.getTargetDTO();
    List<ObligationDTO> obligationDTOs = entitlementPolicyBean.getObligationDTOs();
    List<PolicyRefIdDTO>  policyRefIdDTOs = entitlementPolicyBean.getPolicyRefIds();
    
    PolicySetDTO policySetDTO = new PolicySetDTO();
    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policyDTO = null;
    String message = null;
    try {
        if(policyName != null && policyName.trim().length() > 0 && algorithmName != null
                && algorithmName.trim().length() > 0) {            
            policySetDTO.setPolicySetId(policyName);
            policySetDTO.setPolicyCombiningAlgId(algorithmName);
            policySetDTO.setDescription(policyDescription);
            policySetDTO.setPolicyOrder(policyOrderOrder);
            policySetDTO.setTargetDTO(targetDTO);
            policySetDTO.setObligations(obligationDTOs);
            policySetDTO.setPolicyRefIdDTOs(policyRefIdDTOs);
            EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                    serverURL, configContext);
            EntitlementPolicyCreator policyCreator = new EntitlementPolicyCreator();
            
            String[] policyEditorData = PolicyEditorUtil.processPolicySetData(policySetDTO);
            String policyString = policyCreator.createPolicySet(policySetDTO, client);
            
            if(entitlementPolicyBean.isEditPolicy()){
                try{
                    policyDTO = client.getPolicy(policyName, false);
                } catch (Exception e){
                    //ignore
                }

                if(policyDTO == null){
                    policyDTO = new PolicyDTO();
                }

                policyDTO.setPolicy(policyString);
                policyDTO.setPolicyEditor(EntitlementConstants.PolicyEditor.SET);
                if(policyEditorData != null){
                    policyDTO.setPolicyEditorData(policyEditorData);
                }
                client.updatePolicy(policyDTO);
                message = resourceBundle.getString("updated.successfully");
            } else {
                policyDTO = new PolicyDTO();
                policyDTO.setPolicyId(policyName);
                policyDTO.setPolicy(policyString);
                policyDTO.setPolicyEditor(EntitlementConstants.PolicyEditor.SET);
                if(policyEditorData != null){
                    policyDTO.setPolicyEditorData(policyEditorData);
                }
                client.addPolicy(policyDTO);
                message = resourceBundle.getString("ent.policy.added.successfully");
            }
            entitlementPolicyBean.cleanEntitlementPolicyBean();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        }        
        entitlementPolicyBean.cleanEntitlementPolicyBean();
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        forwardTo = "index.jsp?";
    } catch (Exception e) {
        message = resourceBundle.getString("error.while.adding.policy") + " " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    }
%>
<script
        type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>