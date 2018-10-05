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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.DefaultAuthenticationSequence" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.DefaultAuthenticationSeqMgtServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page
        import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page
        import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%! private static final String BYTES = "bytes";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream;";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String XML = ".xml";
    private static final String ATTACHMENT_FILENAME = "attachment;filename=\"";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
%>
<%
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        DefaultAuthenticationSeqMgtServiceClient serviceClient = new
                DefaultAuthenticationSeqMgtServiceClient(cookie, backendServerURL, configContext);
        DefaultAuthenticationSequence sequence = serviceClient.getDefaultAuthenticationSeq();
        if (sequence == null || StringUtils.isBlank(sequence.getContentXml())) {
            String message = resourceBundle.getString("alert.error.read.default.seq");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            return;
        }
        String seqData = sequence.getContentXml();
        out.clearBuffer();
        response.setHeader(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + "default_seq" + XML + "\"");
        response.setHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
        response.setHeader(ACCEPT_RANGES, BYTES);
        response.setHeader(CONTENT_LENGTH, String.valueOf(seqData.length()));
        out.write(seqData);
    } catch (Exception e) {
        String message = resourceBundle.getString("alert.error.export.default.seq");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
    }
%>
<script>
    location.href = 'idp-mgt-edit-local.jsp?selectDefaultSeq=true';
</script>
