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
<body>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.io.*" %>

<%@ page import="org.owasp.encoder.Encode" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String metadata = "";
    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityProviderMgtServiceClient client = new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);
        //create a .xml type
        metadata = client.getResidentIDPMetadata();
//        MetadataDownloadHandler metadataDownloadHandler = new MetadataDownloadHandler();
//        metadataDownloadHandler.createFile("Somthing", request);
//
//        metadata = metadataDownloadHandler.downloadFile();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage("Error downloading metadata file", CarbonUIMessage.INFO, request);
    } finally {
    }
%>
<script>
    var metadata = "<%=metadata.trim()%>";
    //    function  makeTextFile (text){
    //        var textFile = "";
    //        var data = new Blob([text], {type: 'text/plain'});
    //
    //                // If we are replacing a previously generated file we need to
    //                // manually revoke the object URL to avoid memory leaks.
    //        if (textFile !== null) {
    //                window.URL.revokeObjectURL(textFile);
    //        }
    //
    //        textFile = window.URL.createObjectURL(data);
    //
    //                // returns a URL you can use as a href
    //        return textFile;
    //    }
    //    location.href=makeTextFile(metadata);
    var link = document.createElement('a');
    link.download = 'metadata.xml';
    var blob = new Blob([metadata], {type: 'text/plain'});
    link.href = window.URL.createObjectURL(blob);
    link.click();
</script>




<script type="text/javascript">
    location.href = "idp-mgt-edit-local.jsp";
</script>


</body>