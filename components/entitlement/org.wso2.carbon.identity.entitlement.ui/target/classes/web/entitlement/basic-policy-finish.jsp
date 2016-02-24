<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.balana.utils.policy.dto.BasicPolicyDTO"%>
<%@ page import="org.wso2.balana.utils.policy.dto.BasicRuleDTO"%>
<%@ page import="org.wso2.balana.utils.policy.dto.BasicTargetDTO"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorException"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
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
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String forwardTo = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String policy = "";
    PolicyDTO policyDTO = null;
    BasicPolicyDTO basicPolicyDTO = new BasicPolicyDTO();
    EntitlementPolicyCreator policyCreator = new EntitlementPolicyCreator();

    String ruleElementOrder = request.getParameter("ruleElementOrder");
    if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
        entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
    } else {
        ruleElementOrder = entitlementPolicyBean.getRuleElementOrder();
    }

    List<BasicRuleDTO> basicRuleDTOs = entitlementPolicyBean.getBasicRuleDTOs();
    BasicTargetDTO basicTargetDTO = entitlementPolicyBean.getBasicTargetDTO();

    String policyName = entitlementPolicyBean.getPolicyName();
    String algorithmName = entitlementPolicyBean.getAlgorithmName();
    String policyDescription = entitlementPolicyBean.getPolicyDescription();

    String[] policyEditorData = null;

    try {

        if(policyName != null && policyName.trim().length() > 0) {

            basicPolicyDTO.setPolicyId(policyName);
            basicPolicyDTO.setRuleAlgorithm(algorithmName);
            basicPolicyDTO.setDescription(policyDescription);
            basicPolicyDTO.setBasicRuleDTOs(basicRuleDTOs);
            basicPolicyDTO.setTargetDTO(basicTargetDTO);

            if(basicRuleDTOs != null && basicTargetDTO != null){
                policyEditorData = PolicyEditorUtil.generateBasicPolicyEditorData(basicPolicyDTO, ruleElementOrder);
                policy = policyCreator.createBasicPolicy(basicPolicyDTO);
            }

            EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                    serverURL, configContext);

            String message = null;
            if(entitlementPolicyBean.isEditPolicy()){
                try{
                    policyDTO = client.getPolicy(policyName, false);
                } catch (Exception e){
                    //ignore
                }

                if(policyDTO == null){
                    policyDTO = new PolicyDTO();
                }

                policyDTO.setPolicy(policy);
                policyDTO.setPolicyEditor(EntitlementConstants.PolicyEditor.BASIC);
                if(policyEditorData != null){
                    policyDTO.setPolicyEditorData(policyEditorData);
                }
                client.updatePolicy(policyDTO);
                message = resourceBundle.getString("updated.successfully");
            } else {
                policyDTO = new PolicyDTO();
                policyDTO.setPolicyId(policyName);
                policyDTO.setPolicy(policy);
                policyDTO.setPolicyEditor(EntitlementConstants.PolicyEditor.BASIC);
                if(policyEditorData != null){
                    policyDTO.setPolicyEditorData(policyEditorData);
                }
                client.addPolicy(policyDTO);
                message = resourceBundle.getString("ent.policy.added.successfully");
            }
            entitlementPolicyBean.cleanEntitlementPolicyBean();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
            forwardTo = "index.jsp?";
        }
    } catch (PolicyEditorException e) {
        String message = resourceBundle.getString("error.while.creating.policy");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.adding.policy") + " " + e.getMessage();
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