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


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil"%>
<%@ page import="org.owasp.encoder.Encode" %>
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
		appBean.updateOutBoundAuthenticationConfig(request);
	%>

	<script>
		location.href = 'configure-service-provider.jsp?authType=flow&display=auth_config&action=update&spName=<%=Encode.forUriComponent(spName)%>';
	</script>


</fmt:bundle>
