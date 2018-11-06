<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.ui.client.FunctionLibraryManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary" %>
<%@ page import="org.owasp.encoder.Encode" %>

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

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String functionLibraryName = request.getParameter("functionLibraryName");
    String oldFunctionLibraryName = request.getParameter("oldFunctionLibraryName");
    String description = request.getParameter("functionLib-description");
    String content = request.getParameter("scriptTextArea");


    if (functionLibraryName != null && !"".equals(functionLibraryName)) {

        String BUNDLE = "org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        FunctionLibrary functionLibrary = new FunctionLibrary();

        functionLibrary.setFunctionLibraryName(functionLibraryName + ".js");
        functionLibrary.setDescription(description);
        functionLibrary.setFunctionLibraryScript(content);

        try {

            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

            FunctionLibraryManagementServiceClient serviceClient = new FunctionLibraryManagementServiceClient(cookie, backendServerURL, configContext);
            serviceClient.updateFunctionLibrary(functionLibrary, oldFunctionLibraryName);


        } catch (Exception e) {
            String message = resourceBundle.getString("alert.error.while.updatinging.function.libraries") + " : " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
%>

<script>
    location.href = "load-function-library.jsp?functionLibraryName=<%=Encode.forUriComponent(oldFunctionLibraryName)%>"
</script>
<%
        }
    }

%>

<script>
    location.href = 'functions-library-mgt-list.jsp';
</script>
