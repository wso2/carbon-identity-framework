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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletRequestContext" %>
<%@ page import="org.apache.commons.fileupload.FileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItem" %>
<%@ page import="org.apache.axiom.om.util.Base64" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityProviderMgtServiceClient client = new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);

        IdentityProvider identityProvider = IdPManagementUIUtil
                .buildFederatedIdentityProvider(request, new StringBuilder());
        client.addIdP(identityProvider);
        String message = MessageFormat.format(resourceBundle.getString("success.adding.idp"), null);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.adding.idp"),
                new Object[]{e.getMessage()});
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    } finally {
        session.removeAttribute("idpUniqueIdMap");
        session.removeAttribute("identityProviderList");
    }
%>
<script type="text/javascript">
    function editIdPName(idpName) {
        location.href = "idp-mgt-edit-load.jsp?idPName=" + encodeURIComponent(idpName);
    }
    <%

            ServletRequestContext servletContext = new ServletRequestContext(request);
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items = upload.parseRequest(servletContext);
            String meta = "";
            String idpName = "";
             for (Object item : items) {
                DiskFileItem diskFileItem = (DiskFileItem) item;

                if (diskFileItem != null) {
                    byte[] value = diskFileItem.get();
                    String key = diskFileItem.getFieldName();

                    if ("meta_data_saml".equals(key)) {
                        if(Base64.encode(value).length()>0){
                            meta = Base64.encode(value);
                        }
                        break;
                    }
                    if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(key)) {

                            idpName = value.toString();

                        break;
                    }

                }

             }
             System.out.println("meta"+meta+" idp"+ idpName);
             if(meta.length()>0 && idpName.length()>0){
                %>
    editIdPName('<%=Encode.forJavaScriptAttribute(idpName)%>');

    <%
             }else{
             %>

    location.href = "idp-mgt-list-load.jsp";
    <%

             }

%>

</script>
