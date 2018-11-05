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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>

<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<jsp:include page="../userstore/display-messages.jsp"/>

<%
    UserRealmInfo userRealmInfo;
    UserStoreInfo userStoreInfo;
    String roleName = request.getParameter("roleName");
    String modifiedRole = roleName;
    UserStoreInfo[] allUserStoreInfo = null;

    if (modifiedRole.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)) {
        modifiedRole = modifiedRole.substring(modifiedRole.indexOf(UserAdminUIConstants.DOMAIN_SEPARATOR) + 1);
    }
    if (session.getAttribute(UserAdminUIConstants.USER_STORE_INFO) != null) {
        userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
        userStoreInfo = Util.getUserStoreInfoForUser(roleName, userRealmInfo);
    } else {
%>
        <script>window.location.href='../admin/logout_action.jsp'</script>
<%      return;
    }

    String regEx = userStoreInfo.getRoleNameRegEx();

    if (roleName.indexOf(UserAdminUIConstants.DOMAIN_SEPARATOR) > 0) {
        String domain = roleName.substring(0, roleName.indexOf(UserAdminUIConstants.DOMAIN_SEPARATOR));
        allUserStoreInfo = userRealmInfo.getUserStoresInfo();
        if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
            for (int i = 0; i < allUserStoreInfo.length; i++) {
                if (allUserStoreInfo[i] != null && allUserStoreInfo[i].getDomainName() != null &&
                    allUserStoreInfo[i].getDomainName().equalsIgnoreCase(domain)) {
                    regEx = allUserStoreInfo[i].getRoleNameRegEx();
                    break;
                }
            }
        }
    }


%>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="rename.user.role"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function validateElement(fld1, regString) {
            var errorMessage = "";
            if (regString != "null" && !fld1.value.match(new RegExp(regString))) {
                errorMessage = "No conformance";
                return errorMessage;
            } else if (regString != "null" && fld1.value == "") {
                return errorMessage;
            } else if (fld1.value == fld1.defaultValue) {
                errorMessage = "Role name not changed";
                return errorMessage;
            }

            if (fld1.value == '') {
                errorMessage = "Empty string";
                return errorMessage;
            }

            return errorMessage;
        }

        function doValidation() {
            var fld = document.getElementById("roleName");
            var roleRegEx = document.getElementById("role_regex").value;

            var reason = validateElement(fld, roleRegEx);
            if (reason != "") {
                if (reason == "No conformance") {
                    CARBON.showWarningDialog("<fmt:message key="enter.role.name.not.conforming"/>");
                } else if (reason == "Empty string") {
                    CARBON.showWarningDialog("<fmt:message key="enter.role.name.empty"/>");
                } else if (reason == "Domain") {
                    CARBON.showWarningDialog("<fmt:message key="rename.role.name.domain"/>");
                } else if (reason == "Role name not changed") {
                    CARBON.showWarningDialog("<fmt:message key="enter.role.name.not.changed"/>");
                }
                return false;
            }
            return true;
        }

        function doCancel() {
            location.href = 'role-mgt.jsp?ordinal=1';
        }

        function doRename() {
            if (doValidation()) {
                var oldRoleName = "<%=Encode.forJavaScriptBlock(roleName)%>";
                var newRoleName = document.getElementById("roleName").value;

                if (newRoleName.indexOf("<%=UserAdminUIConstants.DOMAIN_SEPARATOR%>") != -1) {
                    if (oldRoleName.indexOf("<%=UserAdminUIConstants.DOMAIN_SEPARATOR%>") == -1) {
                        CARBON.showWarningDialog("<fmt:message key="oldrole.no.domain"/>");
                        return false;
                    } else {
                        var oldDomain = oldRoleName.substring(0, oldRoleName.indexOf(
                        "<%=UserAdminUIConstants.DOMAIN_SEPARATOR%>"));
                        var newDomain = newRoleName.substring(0, newRoleName.indexOf(
                        "<%=UserAdminUIConstants.DOMAIN_SEPARATOR%>"));

                        if (oldDomain.toUpperCase() != newDomain.toUpperCase()) {
                            CARBON.showWarningDialog("<fmt:message key="invalid.domain"/>");
                            return false;
                        }

                    }
                }

                $("#renameRoleForm").submit();
            }
        }

    </script>


    <div id="middle">
        <h2><fmt:message key="rename.user.role"/></h2>

        <div id="workArea">
            <form action="rename-role-finish-ajaxprocessor.jsp" method="post" id="renameRoleForm">

                <input type="hidden" id="role_regex" name="role_regex" value=<%=Encode.forHtmlAttribute(regEx)%>>
                <input type="hidden" id="oldRoleName" name="oldRoleName" value=<%=Encode.forHtmlAttribute(roleName)%>>

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="enter.role.name"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <tr>
                                    <td><fmt:message key="role.rename"/><font color="red">*</font>
                                    </td>
                                    <td><input type="text" name="newRoleName"
                                               value="<%=Encode.forHtmlAttribute(modifiedRole)%>" id="roleName"/></td>
                                </tr>
                            </table>
                            <!-- normal table -->
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" onclick="doRename();"
                                   value="<fmt:message key="finish"/>"/>
                            <input type="button" class="button" onclick="doCancel();"
                                   value="<fmt:message key="cancel"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>