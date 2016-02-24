<!--
  ~
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  ~
  -->

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@page import="org.wso2.carbon.identity.mgt.ui.AccountCredentialMgtConfigClient" %>
<%@page import="org.wso2.carbon.identity.mgt.ui.EmailConfigDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String emailSubject = request.getParameter("emailSubject");
	String emailBody = request.getParameter("emailBody");
	String emailFooter = request.getParameter("emailFooter");
	String templateName = request.getParameter("templateName");

	EmailConfigDTO emailConfig = null;
	EmailTemplateDTO templateChanged = new EmailTemplateDTO();
	
	
	if(emailSubject != null && emailSubject.trim().length() > 0){
		templateChanged.setSubject(emailSubject);
	}
	if(emailBody != null && emailBody.trim().length() > 0){
		templateChanged.setBody(emailBody);
	}
	if(emailFooter != null && emailFooter.trim().length() > 0){
		templateChanged.setFooter(emailFooter);
	}
	if(templateName != null && templateName.trim().length() > 0){
		templateChanged.setName(templateName);
	}
	
    try {
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        AccountCredentialMgtConfigClient configClient =
                            new AccountCredentialMgtConfigClient(cookie, backendServerURL, configContext);
        
        // Load the configuration with all the templates.
        emailConfig = configClient.loadEmailConfig();
        
        // Set the changed template.
    	emailConfig.setTemplate(templateChanged);
        
        // Now save the configuration with changed tempalate.
        configClient.saveEmailConfig(emailConfig);


%>
    <script type="text/javascript">
        location.href = "email-template-config.jsp";
    </script>
<%
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR,
                request);
%>
    <script type="text/javascript">
        location.href = "email-template-config.jsp";
    </script>
<%
    return;
    }
%>
        

