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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementAdminServiceClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle" %>
<%

	String forwardTo = null;
    String finderId = request.getParameter("finderId");
    String type = request.getParameter("type");
    
    String serverURL = CarbonUIUtil.getServerURL(config
			.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        EntitlementAdminServiceClient client =
                new EntitlementAdminServiceClient(cookie, serverURL, configContext);
        if(finderId != null){
            if("attribute".equals(type)) {
                client.refreshAttributeFinder(finderId);
            } else if("resource".equals(type)){
                client.refreshResourceFinder(finderId);
            } else if("policy".equals(type)){
                client.refreshPolicyFinder(finderId);
            }
            forwardTo = "pdp-manage.jsp";            
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("invalid.policy.not.updated");
        CarbonUIMessage.sendCarbonUIMessage(message,	CarbonUIMessage.ERROR, request);
        forwardTo = "pdp-manage.jsp";
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