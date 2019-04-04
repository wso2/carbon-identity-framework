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
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.DefaultAuthenticationSequence" %>
<%@ page
        import="org.wso2.carbon.identity.application.mgt.defaultsequence.stub.IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.DefaultAuthenticationSeqMgtServiceClient" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<%@ page
        import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String receivedSeqContent = request.getParameter("seqContent");
    String seqContent = receivedSeqContent != null ? receivedSeqContent.trim() : "";
    String receivedSeqDesc = request.getParameter("sequence-desc");
    String seqDesc = receivedSeqDesc != null ? receivedSeqDesc.trim() : "";
    if (StringUtils.isNotEmpty(seqContent)) {
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            DefaultAuthenticationSeqMgtServiceClient serviceClient = new DefaultAuthenticationSeqMgtServiceClient(
                    cookie, backendServerURL, configContext);
            DefaultAuthenticationSequence sequence = new DefaultAuthenticationSequence();
            sequence.setName(IdPManagementUIUtil.DEFAULT_AUTH_SEQ);
            sequence.setDescription(seqDesc);
            sequence.setContentXml(seqContent);
            serviceClient.updateDefaultAuthenticationSeq(sequence);

            String message = resourceBundle.getString("alert.info.update.default.seq");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
%>
<script>
    location.href = 'idp-mgt-edit-local.jsp?selectDefaultSeq=true';
</script>
<%
} catch (IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException e) {
    if (e.getFaultMessage() != null && e.getFaultMessage().getDefaultAuthSeqMgtException() != null &&
            ArrayUtils.isNotEmpty(e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages())) {
        session.setAttribute("updateError", e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages());
%>
<script>
    location.href = 'edit-default-authSeq.jsp';
</script>
<%
} else {
    String message = resourceBundle.getString("alert.error.update.default.seq");
    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);%>

<script>
    location.href = 'edit-default-authSeq.jsp';
</script>

<%
    }
} catch (Exception e) {
    String message = resourceBundle.getString("alert.error.update.default.seq");
    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);%>

<script>
    location.href = 'edit-default-authSeq.jsp';
</script>
<%
    }
} else {
    String message = resourceBundle.getString("alert.error.default.seq.content.should.available");
    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
%>
<script>
    location.href = 'idp-mgt-edit-local.jsp?selectDefaultSeq=true';
</script>
<%
    }
%>

