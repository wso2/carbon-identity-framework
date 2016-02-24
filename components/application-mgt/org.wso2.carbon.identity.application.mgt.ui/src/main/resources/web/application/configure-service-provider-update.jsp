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
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil"%>
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
        ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, spName);
		if (spName != null && !"".equals(spName)) {

			String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
			ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

			try {
				
				if ("wso2carbon-local-sp".equals(spName)){
					appBean.updateLocalSp(request);
				} else {				
					appBean.update(request);
				}
				
				String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
				String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
				ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
				                                                                  .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
				
				ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
				serviceClient.updateApplicationData(appBean.getServiceProvider());

			} catch (Exception e) {
				String message = resourceBundle.getString("alert.error.while.updating.service.provider")+ " : " + e.getMessage();
				CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
			} 
		} 

	%>

</fmt:bundle>
