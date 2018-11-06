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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.DefaultAuthenticationSequence" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig" %>
<%@ page
        import="org.wso2.carbon.identity.application.mgt.defaultsequence.stub.IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.DefaultAuthenticationSeqMgtServiceClient" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page
        import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String receivedSeqDesc = request.getParameter("seqDesc");
    String seqDesc = receivedSeqDesc != null ? receivedSeqDesc.trim() : "";
    String spName = request.getParameter("spName");

    try {
        boolean isSeqExists = Boolean.parseBoolean(request.getParameter("isSeqExists"));
        ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, spName);
        appBean.updateOutBoundAuthenticationConfig(request);
        appBean.conditionalAuthentication(request);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        DefaultAuthenticationSeqMgtServiceClient serviceClient = new DefaultAuthenticationSeqMgtServiceClient(cookie,
                backendServerURL, configContext);
        LocalAndOutboundAuthenticationConfig authenticationConfig = appBean.getServiceProvider()
                .getLocalAndOutBoundAuthenticationConfig();
        DefaultAuthenticationSequence sequence = new DefaultAuthenticationSequence();
        sequence.setContent(authenticationConfig);
        sequence.setName(ApplicationMgtUIConstants.DEFAULT_AUTH_SEQ);
        sequence.setDescription(seqDesc);
        if (isSeqExists) {
            serviceClient.updateDefaultAuthenticationSeq(sequence);
        } else {
            serviceClient.createDefaultAuthenticationSeq(sequence);
        }
    } catch (IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException e) {
        if (e.getFaultMessage() != null && e.getFaultMessage().getDefaultAuthSeqMgtException() != null &&
                ArrayUtils.isNotEmpty(e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages())) {
            session.setAttribute("createError", e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages());
        }
        out.print("createError");
    } catch (Exception e) {
        out.print("createError");
    }
%>


