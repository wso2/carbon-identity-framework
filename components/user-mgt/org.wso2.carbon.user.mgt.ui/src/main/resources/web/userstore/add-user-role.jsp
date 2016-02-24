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
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>

<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<script type="text/javascript" src="extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">

    <carbon:breadcrumb label="system.user.store"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>


    <script type="text/javascript">
        function deleteUserStore() {
            CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.userstore"/> ' + '?', doDelete, null);
        }

        function doDelete() {
            location.href = 'delete-finish.jsp';
        }
        function displayToken() {
            //alert(cookie + backendURL);
            sessionAwareFunction(function () {
                new Ajax.Request('../resources/get_accesstoken_ajaxprocessor.jsp',
                                 {
                                     method:'post',
                                     parameters:{},

                                     onSuccess:function (transport) {
                                         var response = transport.responseText;
                                         var startIndex = response.indexOf("$$$");
                                         var endIndex = response.lastIndexOf("$$$")
                                         var token = response.substring(startIndex + 3, endIndex);
                                         CARBON.showInfoDialog("Your access token is: " + token);


                                     },

                                     onFailure:function (transport) {
                                         CARBON.showErrorDialog("Unbale to get the access token");

                                     }
                                 });
            }, "session timed out");
        }

    </script>
    <%
        boolean multipleUserStores = false;
        UserRealmInfo userRealmInfo = null;

            try {
    			String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    			String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    			ConfigurationContext configContext = (ConfigurationContext) config
    					.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    			UserAdminClient client = new UserAdminClient(cookie,backendServerURL, configContext);
    			userRealmInfo = client.getUserRealmInfo();
                       if (userRealmInfo != null) {
                           multipleUserStores = userRealmInfo.getMultipleUserStore();
                       }
    			session.setAttribute(UserAdminUIConstants.USER_STORE_INFO,userRealmInfo);
    		} catch (Exception e) {
    			CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(),
    					CarbonUIMessage.ERROR, e);
    			session.setAttribute(CarbonUIMessage.ID, uiMsg);
    			return;
    		}
    %>


    <div id="middle">
        <h2><fmt:message key="add.user.management"/></h2>

        <div id="workArea">
            <table width="100%">

                <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security") ||
                       CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security/usermgt") ||
                       CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security/usermgt/users") ||
                       CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security/usermgt/passwords") ||
                       CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security/usermgt/profiles")) {%>
                <tr>
                    <td>
                        <table class="styledLeft" id="internal" width="100%">
                            <tr>
                                <td>
                                    <a class="icon-link"
                                       style="background-image:url(images/users.gif);"
                                       href="../user/add-step1.jsp"><fmt:message key="add-users"/></a>
                                </td>
                            </tr>


                            <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security")) { %>
                            <tr>
                                <td>
                                    <a class="icon-link"
                                       style="background-image:url(images/user-roles.gif);"
                                       href="../role/add-step1.jsp"><fmt:message key="add-roles"/></a>
                                </td>
                            </tr>
                            <% } %>
                        </table>
                        <%
                            boolean show = false;
                            UserStoreInfo[] userStoreInfos = userRealmInfo.getUserStoresInfo();
                            for (UserStoreInfo info : userStoreInfos) {
                                if (info.getBulkImportSupported()) {
                                    show = true;
                                    break;
                                }
                            }
                            if (show && ((multipleUserStores || !userRealmInfo.getPrimaryUserStoreInfo().getReadOnly())
                                    && userRealmInfo.getPrimaryUserStoreInfo().getExternalIdP() == null
                                    && CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/configure/security/usermgt/users"))) {
                        %>
                        <table width="100%" border="0" cellpadding="0" cellspacing="0" style="margin-top:2px;">
                            <tr>
                                <td class="addNewSecurity">
                                    <a href="../user/bulk-import.jsp" class="icon-link"
                                       style="background-image:url(../user/images/bulk-import.gif);"><fmt:message
                                            key="bulk.import.user"/></a>
                                </td>
                            </tr>
                        </table>

                        <%
                            }
                        %>
                    </td>
                </tr>
            </table>
            <% } %>

        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('internal', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('external', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
