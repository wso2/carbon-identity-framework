<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil"%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />

<fmt:bundle
	basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
	<carbon:breadcrumb label="application.mgt"
		resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

	<%
		String spName = request.getParameter("spName");
	    String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
	    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
	    ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, spName);
	    appBean.reset();
		if (spName != null && !"".equals(spName)) {
		
			try {

				String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
				String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
				ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		
				ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
				ServiceProvider serviceProvider = serviceClient.getApplication(spName);
		
				IdentityProvider[] federatedIdPs = serviceClient.getAllFederatedIdentityProvider();
				String[] claimUris = serviceClient.getAllClaimUris();
				LocalAuthenticatorConfig[] localAuthenticatorConfigs = serviceClient.getAllLocalAuthenticators();
				RequestPathAuthenticatorConfig[] requestPathAuthenticators = serviceClient.getAllRequestPathAuthenticators();
				appBean.setServiceProvider(serviceProvider);
				appBean.setLocalAuthenticatorConfigs(localAuthenticatorConfigs);
				appBean.setFederatedIdentityProviders(federatedIdPs);
				appBean.setRequestPathAuthenticators(requestPathAuthenticators);
				appBean.setClaimUris(claimUris);
				
			} catch (Exception e) {
				String message = resourceBundle.getString("alert.error.while.reading.service.provider") + " : " + e.getMessage();
				CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
			}
		}
	%>

	<script>
	    <% if ("wso2carbon-local-sp".equals(spName)) {%>
		location.href = 'configure-local-service-provider.jsp?spName=<%=Encode.forUriComponent(spName)%>';
		<%} else {%>
		location.href = 'configure-service-provider.jsp?spName=<%=Encode.forUriComponent(spName)%>';
		<%}%>
	</script>


</fmt:bundle>
