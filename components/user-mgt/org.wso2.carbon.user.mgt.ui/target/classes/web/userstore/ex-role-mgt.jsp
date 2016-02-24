<%--
  Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

   WSO2 Inc. licenses this file to you under the Apache License,
   Version 2.0 (the "License"); you may not use this file except
   in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@ page import="org.wso2.carbon.user.mgt.common.IUserAdmin" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>


<%!String[] datas = null;%>
<%!String[] internalDatas = null;%>
<%
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IUserAdmin proxy =
                (IUserAdmin) CarbonUIUtil.getServerProxy(new UserAdminClient(cookie, backendServerURL, configContext),
                                                         IUserAdmin.class, session);
        datas = proxy.getExternalGroups(); //TODO: DimuthuL Get the stuff with one service call!
        internalDatas = proxy.getSpecialInternalRoles(); //TODO: DimuthuL Get the stuff with one service call!
    } catch (Exception e) {
        String message = "Unable to retrieve roles from the external user store. " +
                         "Please click on the main menu to continue.";
        CarbonUIMessage uiMsg = new CarbonUIMessage(message, CarbonUIMessage.ERROR, e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="roles.of.external.users"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function deleteUserGroup(role) {
            function doDelete() {
                var roleName = role;
                location.href = '../role/delete-role.jsp?roleName=' + roleName + '&userType=special';
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.role"/> ' + role + '?', doDelete, null);
        }


    </script>

    <div id="middle">
        <h2><fmt:message key="roles.of.external.users"/></h2>

        <div id="workArea">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="name"/></th>
                    <th><fmt:message key="type"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (datas != null) {
                        for (String data : datas) {
                            if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!
                                String roleName = data;
                %>
                <tr>
                    <td><%=Encode.forHtml(roleName)%>
                    </td>
                    <td><fmt:message key="external"/></td>
                    <td>
                        <a href="../role/view-role.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&userType=external"
                           class="icon-link" style="background-image:url(../admin/images/view.gif);"><fmt:message
                                key="view"/></a>
                        <a href="../role/edit-permissions.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&userType=external"
                           class="icon-link"
                           style="background-image:url(../admin/images/application_edit.gif);"><fmt:message
                                key="edit.permissions"/></a>
                </tr>
                <%
                            }
                        }
                    }
                %>

                <%
                    if (internalDatas != null) {
                        for (String data : internalDatas) {
                            if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!
                                String roleName = data;
                %>
                <tr>
                    <td><%=Encode.forHtml(roleName)%>
                    </td>
                    <td><fmt:message key="internal"/></td>
                    <td>
                        <a href="../role/view-role.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&userType=special"
                           class="icon-link" style="background-image:url(../admin/images/view.gif);"><fmt:message
                                key="view"/></a>
                        <a href="../role/edit-permissions.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&userType=special"
                           class="icon-link"
                           style="background-image:url(../admin/images/application_edit.gif);"><fmt:message
                                key="edit.permissions"/></a>
                        <a href="../role/edit-users.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&userType=special"
                           class="icon-link"
                           style="background-image:url(../admin/images/application_edit.gif);"><fmt:message
                                key="edit.users"/></a>
                        <a href="#" onclick="deleteUserGroup('<%=Encode.forJavaScriptAttribute(roleName)%>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/cancel.gif);"><fmt:message key="delete"/></a>
                    </td>
                </tr>
                <%
                            }
                        }
                    }
                %>
                </tbody>
            </table>
            <p>&nbsp;</p>
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td class="action-link add-link"><a href="add-step1.jsp?userType=external"><img
                            src="../admin/images/add.gif" border="0"/></a></td>
                    <td class="action-link add-link" width="100%"><a
                            href="../role/add-step1.jsp?userType=external"><fmt:message
                            key="add.new.internal.role"/></a></td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>
