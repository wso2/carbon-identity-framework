<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.Base64" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.axis2.transport.http.HTTPConstants" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.ui.client.FunctionLibraryManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page
        import="static org.wso2.carbon.identity.functions.library.mgt.ui.util.FunctionLibraryUIConstants.FUNCTION_LIBRARY_NAME" %>
<%@ page
        import="static org.wso2.carbon.identity.functions.library.mgt.ui.util.FunctionLibraryUIConstants.DESCRIPTION" %>
<%@ page
        import="static org.wso2.carbon.identity.functions.library.mgt.ui.util.FunctionLibraryUIConstants.SCRIPT_CONTENT" %>
<%@ page
        import="static org.wso2.carbon.identity.functions.library.mgt.ui.util.FunctionLibraryUIConstants.SCRIPT_SUFFIX" %>


<%
    String httpMethod = request.getMethod();
    if (!HTTPConstants.HTTP_METHOD_POST.equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    
    String functionLibName = request.getParameter(FUNCTION_LIBRARY_NAME);
    String description = request.getParameter(DESCRIPTION);
    String content = request.getParameter(SCRIPT_CONTENT);
    content = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
    
    if (StringUtils.isNotBlank(functionLibName) && StringUtils.isNotBlank(content)) {
        FunctionLibrary functionLibrary = new FunctionLibrary();
        functionLibrary.setFunctionLibraryName(functionLibName + SCRIPT_SUFFIX);
        functionLibrary.setDescription(description);
        functionLibrary.setFunctionLibraryScript(content);
        
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
            FunctionLibraryManagementServiceClient serviceClient = new FunctionLibraryManagementServiceClient(cookie,
                    backendServerURL, configContext);
            serviceClient.createFunctionLibrary(functionLibrary);
%>
<script>
    location.href = 'functions-library-mgt-list.jsp';
</script>
<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script>
    location.href = 'functions-library-mgt-add.jsp';
</script>
<%
    }
} else {
%>
<script>
    location.href = 'functions-library-mgt-add.jsp';
</script>
<%
    }%>
