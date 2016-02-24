<%--
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.directory.server.manager.ui.DirectoryServerManagerClient"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<%
        String applicationSPName = request.getParameter("spName");
        session.setAttribute("application-sp-name", applicationSPName);
        String spnName = request.getParameter("spnName");
        DirectoryServerManagerClient serverManager = null;
        try{

            serverManager = (DirectoryServerManagerClient)session.getAttribute(DirectoryServerManagerClient.
                    SERVER_MANAGER_CLIENT);

            if(serverManager == null){
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().
                            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

                serverManager = new DirectoryServerManagerClient(cookie, backEndServerURL, configContext);
                session.setAttribute(DirectoryServerManagerClient.SERVER_MANAGER_CLIENT, serverManager);
            }
        }catch(Exception e){
            CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
   %>
            <jsp:include page="../admin/error.jsp"/>
   <%
            return;
        }
   %>



<fmt:bundle basename="org.wso2.carbon.directory.server.manager.ui.i18n.Resources">
<carbon:breadcrumb label="change.password"
               resourceBundle="org.wso2.carbon.directory.server.manager.ui.i18n.Resources"
               topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function doCancel() {
            location.href = '../application/configure-service-provider.jsp?action=cancel&display=kerberos&spName=<%=Encode.forJavaScriptBlock(Encode.forUriComponent(applicationSPName))%>';
        }

        function doValidation() {
            var reason = "";

            reason = validatePasswordOnCreation("newPassword", "checkPassword", "<%=Encode.forJavaScriptBlock(serverManager.getPasswordConformanceRegularExpression())%>");
            if (reason != "") {
                if (reason == "Empty Password") {
                    CARBON.showWarningDialog("<fmt:message key="enter.the.same.password.twice"/>");
                } else if (reason == "Min Length") {
                    CARBON.showWarningDialog("<fmt:message key="password.mimimum.characters"/>");
                } else if (reason == "Invalid Character") {
                    CARBON.showWarningDialog("<fmt:message key="invalid.character.in.password"/>");
                } else if (reason == "Password Mismatch") {
                    CARBON.showWarningDialog("<fmt:message key="password.mismatch"/>");
                } else if (reason == "No conformance") {
                    CARBON.showWarningDialog("<fmt:message key="password.conformance"/>");
                }
                return false;
            }
            return true;
        }

    </script>
    <div id="middle">
        <h2><fmt:message key="change.password.spn"/> <%=Encode.forHml(spnName)%></h2>

        <div id="workArea">
            <form name="chgPassWdForm" method="post"
                  onsubmit="return doValidation();" action="change-passwd-finish.jsp">
                <input type="hidden" name="spnName" value="<%=Encode.forHtmlAttribute(spnName)%>"/>

                <table class="styledLeft" id="changePassword" width="60%">
                    <thead>
                        <tr>
                            <th><fmt:message key="type.new.password"/></th>
                        </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <tr>
                                    <td><fmt:message key="current.password"/><font color="red">*</font></td>
                                    <td><input type="password" name="currentPassword"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="new.password"/><font color="red">*</font></td>
                                    <td><input type="password" name="newPassword"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="new.password.repeat"/><font color="red">*</font></td>
                                    <td><input type="password" name="checkPassword"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input class="button" type="submit" value="<fmt:message key="change"/>"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                   onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>