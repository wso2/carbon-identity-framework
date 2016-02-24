<!--
  ~
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@page import="org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO"%>
<%@page import="org.wso2.carbon.identity.mgt.ui.AccountCredentialMgtConfigClient"%>
<%@page import="org.wso2.carbon.identity.mgt.ui.EmailConfigDTO"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.owasp.encoder.Encode" %>

<%
	String username = request.getParameter("username");
    String forwardTo = null;
    AccountCredentialMgtConfigClient client = null;
    
    EmailConfigDTO emailConfig = null;
    String emailSubject = null;
    String emailBody = null;
    String emailFooter = null;
    String templateName = null;
    String emailSubject0 = null;
    String emailBody0 = null;
    String emailFooter0 = null;
    String templateName0 = null;
    
    if (username == null) {
        username = (String) request.getSession().getAttribute("logged-user");
    }

    String BUNDLE = "org.wso2.carbon.identity.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session
		.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
		.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
		.getServletContext().getAttribute(
				CarbonConstants.CONFIGURATION_CONTEXT);
        client = new AccountCredentialMgtConfigClient(cookie,
                backendServerURL, configContext);
             	
		try {
			emailConfig = client.loadEmailConfig();

		} catch (Exception e) {
			e.printStackTrace();
		}

		
	} catch (Exception e) {
		String message = resourceBundle
				.getString("error.while.loading.email.tepmplate.data");
		CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
		forwardTo = "../admin/error.jsp";
	}
%>
<script type="text/javascript">
	
	function updateFields(elm){
		var $selectedOption = jQuery(elm).find(":selected");
		jQuery('#emailSubject').val($selectedOption.attr('data-subject'));
		jQuery('#emailBody').val($selectedOption.attr('data-body'));
		jQuery('#emailFooter').val($selectedOption.attr('data-footer'));
		jQuery('#templateName').val($selectedOption.attr('data-templateName'));
		
	}
</script>
<%
    if ( forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    return;
	}
%>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
	<carbon:breadcrumb label="email.template"
		resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<div id="middle">
		<h2>
			<fmt:message key="email.template.heading" />
		</h2>
		<div id="workArea">
			<form action="email-template-config-finish.jsp?userName=<%=Encode.forUriComponent(username)%>" method="post">
				<div class="sectionSeperator">
					<fmt:message key="email.template.set" />
					</div>
				<div class=”sectionSub”>
					<table class="carbonFormTable">
					<tr>
							<td class="leftCol-med labelField"><fmt:message	key="email.types" /></td>
							<td><select id="emailTypes"	name="emailTypes" class="leftCol-big" onchange="updateFields(this);">
									<%
									EmailTemplateDTO[] templates = emailConfig.getTemplates();
									for(int i=0; i < templates.length; i++) {
										
										EmailTemplateDTO template = templates[i];
										if(i==0){
											emailSubject0 = template.getSubject();	
											emailBody0 = template.getBody();
											emailFooter0 = template.getFooter();
											templateName0 = template.getName();
										} 								
										emailSubject = template.getSubject();
										emailBody = template.getBody();
										emailFooter = template.getFooter();
										templateName = template.getName();
	                                %>
										<option 
										value="<%=i%>" 
										data-subject="<%=Encode.forHtmlContent(emailSubject)%>"
										data-body="<%=Encode.forHtmlContent(emailBody)%>"
										data-footer="<%=Encode.forHtmlContent(emailFooter)%>"
										data-templateName="<%=Encode.forHtmlContent(templateName)%>"
										><%=Encode.forHtmlContent(template.getDisplayName())%></option>
									<% 
									}									
                                	%>
							</select></td>
						</tr>
						<tr>
							<td><fmt:message key="emailSubject" /></td>
							<td><input type="text" name="emailSubject" id="emailSubject" style="width : 500px;" value="<%=Encode.forHtmlAttribute(emailSubject0)%>"/></td>
						</tr>
						<tr>
							<td><fmt:message key="emailBody" /></td>
							<td><textarea name="emailBody" id="emailBody"
									class="text-box-big" style="width: 500px; height: 170px;"><%=Encode.forHtmlContent(emailBody0)%></textarea></td>
						</tr>
						<tr>
							<td><fmt:message key="emailFooter" /></td>
							<td><textarea name="emailFooter" id="emailFooter"
									class="text-box-big" style="width: 265px; height: 87px;"><%=Encode.forHtmlContent(emailFooter0)%></textarea></td>
						</tr>
						<tr><td></td>
							<td><input type="hidden" name="templateName" id="templateName" value="<%=Encode.forHtmlAttribute(templateName0)%>"/></td>
						</tr>
					</table>
				</div>
				<div class="buttonRow">
					<input type="submit" class="button" value="Save"/>
				</div>
			</form>
		</div>
	</div>
</fmt:bundle>
