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
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants"%>

<%@page
	import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<%@page import="org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorElementDTO"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%
	String serverURL = CarbonUIUtil.getServerURL(config
			.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String forwardTo = null;
	String action = request.getParameter("rule");
	String policyid = request.getParameter("policyid");
	String type = request.getParameter("type");
	String value = request.getParameter("value");
	PolicyDTO dto = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

	if ((request.getParameter("policyid") != null)) {

		try {
			EntitlementPolicyAdminServiceClient client =
                    new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
            int i = 0;
			dto = client.getPolicy(policyid, false);
            String[] data = dto.getBasicPolicyEditorMetaData();

            if(data != null){
                i = (data.length -11)/11;
            }
            List<SimplePolicyEditorElementDTO> elementDTOs = new  ArrayList<SimplePolicyEditorElementDTO>();
            SimplePolicyEditorElementDTO elementDTO = new SimplePolicyEditorElementDTO();
            if("permit".equals(action)){
                elementDTO.setOperationType(PolicyEditorConstants.PreFunctions.CAN_DO);
            }
            elementDTO.setResourceValue(PolicyEditorConstants.ANY);
            elementDTO.setActionValue(PolicyEditorConstants.ANY);
            elementDTO.setEnvironmentValue(PolicyEditorConstants.ANY);
            elementDTO.setUserAttributeValue(value);
            if("role".equals(type)){
                elementDTO.setUserAttributeId(PolicyEditorConstants.SUBJECT_ID_ROLE);    
            }
            elementDTOs.add(elementDTO);
            EntitlementPolicyCreator creator = new EntitlementPolicyCreator();
            String policy = creator.addNewRules(dto.getPolicy(),elementDTOs);
            if(PolicyEditorConstants.SOA_POLICY_EDITOR.equals(dto.getPolicyEditor())){
                List<String> metaDataList = new ArrayList<String>();
                metaDataList.add("resourceValue" + i + "|" + "*");
                metaDataList.add("actionValue" + i + "|" + "*");
                metaDataList.add("userAttributeValue" + i + "|" + value);
                if("role".equals(type)){
                    metaDataList.add("userAttributeValue" + i + "|" + value);
                }
                metaDataList.add("environmentValue" + i + "|" + "*");
                metaDataList.add("operationValue" + i + "|" + PolicyEditorConstants.PreFunctions.CAN_DO);
                metaDataList.add("update");
                dto.setBasicPolicyEditorMetaData(metaDataList.toArray(new String[metaDataList.size()]));
            }

            if(policy != null){
                dto.setPolicy(policy);
			    client.updatePolicy(dto);
            }
			//session.setAttribute("entitlementpolicy", dto.getPolicy());
			forwardTo = "index.jsp?region=region1&item=policy_menu";
		} catch (Exception e) {
			String message = resourceBundle.getString("invalid.policy.not.updated");
			//session.setAttribute("entitlementpolicy", dto.getPolicy());
			CarbonUIMessage.sendCarbonUIMessage(message,	CarbonUIMessage.ERROR, request);
			forwardTo = "index.jsp?region=region1&item=policy_menu";
		}
	} else {
		forwardTo = "index.jsp?region=region1&item=policy_menu";
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