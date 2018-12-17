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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserManagementUIException" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String isUserChange = request.getParameter("isUserChange");
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String returnPath = request.getParameter("returnPath");
    String cancelPath = null;
    String encryptedUsername = null;
    String decryptedUsername = null;

    String trustedReturnPath = "../userstore/index.jsp";
    if ("user-mgt.jsp".equals(returnPath)) {
        trustedReturnPath = "user-mgt.jsp";
    }

    if (isUserChange != null) {
        cancelPath = trustedReturnPath;
    } else {
        encryptedUsername = request.getParameter("username");
        cancelPath = "user-mgt.jsp?ordinal=1";
    }

    UserStoreInfo userStoreInfo = null;
    UserRealmInfo userRealmInfo = null;
    UserStoreInfo[] allUserStoreInfo = null;
    try {
        if (encryptedUsername != null) {
            decryptedUsername = Util.getDecryptedUsername(encryptedUsername);
        }
        if (decryptedUsername == null) {
            decryptedUsername = (String) session.getAttribute("logged-user");
        }
        userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
        if (userRealmInfo == null) {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext()
                                                 .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            userRealmInfo = client.getUserRealmInfo();
            session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
        }

        userStoreInfo = userRealmInfo.getPrimaryUserStoreInfo(); // TODO

    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.while.loading.user.store.info"),
                                              e.getMessage());
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>', function () {
            location.href = "user-mgt.jsp";
        });
    });
</script>
<%
    }

    String displayName = request.getParameter("displayName");
    if (StringUtils.isBlank(displayName)) {
        displayName = (String) session.getAttribute(UserAdminUIConstants.USER_DISPLAY_NAME);
        if (StringUtils.isBlank(displayName)) {
            displayName = decryptedUsername;
        }
    }

    String regEx = userStoreInfo.getPasswordRegEx();

    if (decryptedUsername.indexOf(UserAdminUIConstants.DOMAIN_SEPARATOR) > 0) {
        String domain = decryptedUsername.substring(0, decryptedUsername.indexOf(UserAdminUIConstants.DOMAIN_SEPARATOR));
        allUserStoreInfo = userRealmInfo.getUserStoresInfo();
        if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
            for (int i = 0; i < allUserStoreInfo.length; i++) {
                if (allUserStoreInfo[i] != null && allUserStoreInfo[i].getDomainName() != null &&
                    allUserStoreInfo[i].getDomainName().equalsIgnoreCase(domain)) {
                    regEx = allUserStoreInfo[i].getPasswordRegEx();
                    break;
                }
            }
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="change.password"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        var skipPasswordValidation = false;

        function doCancel() {
            location.href = '<%=Encode.forJavaScriptBlock(cancelPath)%>';
        }
        jQuery(document).ready(function () {
            jQuery('#defineHere').attr('checked', 'checked');
        });
        function definePasswordHere() {
            var passwordMethod = document.getElementById('defineHere');
            if (passwordMethod.checked) {
                skipPasswordValidation = false;
                jQuery('#emailRow').hide();
                jQuery('#passwordRow').show();
                jQuery('#checkPasswordRow').show();
            }
        }

        function askPasswordFromUser() {
            var emailRow = document.getElementById('emailRow');
            var passwordMethod = document.getElementById('askFromUser');
            if (passwordMethod.checked) {
                skipPasswordValidation = true;
                jQuery('#passwordRow').hide();
                jQuery('#checkPasswordRow').hide();
                jQuery('#emailRow').show();

            }
        }

        function doValidation() {
            var reason = "";
            if (!skipPasswordValidation) {
                if (!(typeof document.getElementsByName("currentPassword")[0] === 'undefined')) {
                    if (isEmpty("currentPassword")) {
                        CARBON.showWarningDialog("<fmt:message key="empty.current.password"/>");
                        return false;
                    }
                }

                var pwdRegEX = document.getElementById("pwd_regex").value;
                reason = validatePasswordOnCreation("newPassword", "checkPassword", pwdRegEX);

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
                        <%
                            String passwordRegEx =   userStoreInfo.getPasswordRegEx();
                            String passwordErrorMessage = userStoreInfo.getPasswordRegExViolationErrorMsg();
                            if (StringUtils.isBlank(passwordErrorMessage)){
                                passwordErrorMessage = MessageFormat.format(resourceBundle.getString("password.conformance"), passwordRegEx);
                            }
                        %>
                        CARBON.showWarningDialog("<%=Encode.forJavaScriptBlock(Encode.forHtml(passwordErrorMessage))%>");
                    }
                    return false;
                }
            }
            return true;
        }

    </script>
    <jsp:include page="../userstore/display-messages.jsp"/>
    <div id="middle">
        <h2><fmt:message key="change.password.for.user"/> <%=Encode.forHtml(displayName)%></h2>

        <div id="workArea">
            <form name="chgPassWdForm" method="post"
                  onsubmit="return doValidation();" action="change-passwd-finish-ajaxprocessor.jsp">
                <input type="hidden" id="pwd_regex" name="pwd_regex" value=<%=Encode.forHtmlAttribute(regEx)%>>

                <input type="hidden" name="username" value="<%=Encode.forHtmlAttribute(decryptedUsername)%>"/>
                <% if (isUserChange != null) { %>
                <input type="hidden" name="isUserChange" value="<%=Encode.forHtmlAttribute(isUserChange)%>"/>
                <input type="hidden" name="returnPath" value="<%=Encode.forHtmlAttribute(trustedReturnPath)%>"/>
                <% } %>
                <table class="styledLeft" id="changePassword" width="60%">
                    <thead>
                    <tr>
                        <th><fmt:message key="type.new.password"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal" id="secondaryTable">
                                <% if (isUserChange != null) { %>
                                <tr>
                                    <td><fmt:message key="current.password"/><font color="red">*</font></td>
                                    <td><input type="password" name="currentPassword" autocomplete="off"/></td>
                                </tr>
                                <% } %>

                                <tr id="passwordRow">
                                    <td><fmt:message key="new.password"/><font color="red">*</font></td>
                                    <td><input type="password" name="newPassword" autocomplete="off"/></td>
                                </tr>
                                <tr id="checkPasswordRow">
                                    <td><fmt:message key="new.password.repeat"/><font color="red">*</font></td>
                                    <td><input type="password" name="checkPassword" autocomplete="off"/></td>
                                </tr>

                            </table>
                            <div id="emailRow" class="sectionHelp" style="display: none;">
                                <fmt:message key="email.user.profile"/><span class="required">*</span>
                            </div>
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