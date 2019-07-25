<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page import="org.owasp.encoder.Encode"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.UUID" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>

<%
    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String idPName = request.getParameter("idPName");

    try {

        if (idPName != null && !idPName.equals("")) {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IdentityProviderMgtServiceClient client = new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);
            IdentityProvider identityProvider = client.getIdPByName(idPName);

            if (identityProvider != null) {
                Map<String, UUID> idpUniqueIdMap = (Map<String, UUID>) session.getAttribute(IdPManagementUIUtil.IDP_LIST_UNIQUE_ID);

                if (idpUniqueIdMap == null || idpUniqueIdMap.get(idPName) == null) {
                    response.sendRedirect("idp-mgt-list-load.jsp");
                    return;
                }
                session.setAttribute(idpUniqueIdMap.get(idPName).toString(), identityProvider);
            }
        } else if (session.getAttribute(IdPManagementUIUtil.IDP_LIST) == null) {
            response.sendRedirect("idp-mgt-list-load.jsp");
            return;
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.loading.idp"),
                new Object[]{e.getMessage()});
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        response.sendRedirect("idp-mgt-list.jsp");
        return;
    }
%>
<script type="text/javascript">
    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
</script>


