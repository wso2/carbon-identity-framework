<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.model.Template" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.ui.client.TemplateManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

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

<fmt:bundle
        basename="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="template.mgt"
                       resourceBundle="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    
    <%
        String templateName = request.getParameter("templateName");
        String BUNDLE = "org.wso2.carbon.identity.template.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        
        if (StringUtils.isNotBlank(templateName)) {
            
            try {
                String currentUser = (String) session.getAttribute("logged-user");
                TemplateManagementServiceClient serviceClient = new TemplateManagementServiceClient(currentUser);
                Template template = serviceClient.getTemplateByName(templateName);
                
            } catch (Exception e) {
                String message = resourceBundle.getString("alert.error.while.reading.template") +
                        " : " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
            }
        }
    %>
    <script>
        location.href = 'edit-template.jsp?templateName=<%=Encode.forUriComponent(templateName)%>';
    </script>
</fmt:bundle>
