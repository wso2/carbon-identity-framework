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
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.client.ClaimDataAdminClient" %>
<%@ page import="org.slf4j.Logger"%>
<%@ page import="org.slf4j.LoggerFactory"%>
<%@ page import="org.wso2.carbon.user.mgt.ui.ClaimDataAdminException" %>

<jsp:useBean id="userBean"
             type="org.wso2.carbon.user.mgt.ui.UserBean"
             class="org.wso2.carbon.user.mgt.ui.UserBean" scope="session"/>
<jsp:setProperty name="userBean" property="*"/>
<jsp:include page="../userstore/display-messages.jsp"/>

<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<%!
    private static final String EMAIL_CLAIM = "http://wso2.org/claims/emailaddress";
%>
<%
    UserStoreInfo userStoreInfo = null;
    UserRealmInfo userRealmInfo = null;
    UserStoreInfo[] allUserStoreInfo = null;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    List<String> domainNames = null;
    String selectedDomain = null;
    boolean isAskPasswordEnabled = false;
    boolean isUserOnBoardingEnabled = false;

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        if (userRealmInfo == null) {
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext()
                                                 .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            userRealmInfo = client.getUserRealmInfo();
            session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
        }

        userStoreInfo = userRealmInfo.getPrimaryUserStoreInfo(); // TODO

        // domain name preparations
        String primaryDomainName = userRealmInfo.getPrimaryUserStoreInfo().getDomainName();

        domainNames = new ArrayList<String>();
        allUserStoreInfo = userRealmInfo.getUserStoresInfo();
        if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
            for (int i = 0; i < allUserStoreInfo.length; i++) {
                if (allUserStoreInfo[i] != null) {
                    if (allUserStoreInfo[i].getDomainName() != null && !allUserStoreInfo[i].getReadOnly()) {
                        domainNames.add(allUserStoreInfo[i].getDomainName());
                    }
                }
            }
        }

        if (domainNames.size() > 0) {
            if (primaryDomainName == null) {
                primaryDomainName = UserAdminUIConstants.PRIMARY_DOMAIN_NAME_NOT_DEFINED;
                domainNames.add(primaryDomainName);
            }
        }

        selectedDomain = userBean.getDomain();
        if (selectedDomain == null || selectedDomain.trim().length() == 0) {
            selectedDomain = primaryDomainName;
        }

        isAskPasswordEnabled = Util.isAskPasswordEnabled();
        isUserOnBoardingEnabled = Util.isUserOnBoardingEnabled(config.getServletContext(), session);
        if (!isAskPasswordEnabled) {
            isAskPasswordEnabled = isUserOnBoardingEnabled;
        }

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
%>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="add.user"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        var skipPasswordValidation = false;

        function validateString(fld1name, regString) {
            var stringValue = document.getElementsByName(fld1name)[0].value;
            var errorMessage = "";
            if (regString != "null" && !stringValue.match(new RegExp(regString))) {
                errorMessage = "No conformance";
                return errorMessage;
            } else if (regString != "null" && stringValue == '') {
                return errorMessage;
            }

            if (stringValue == '') {
                errorMessage = "Empty string";
                return errorMessage;
            }

            if (stringValue.indexOf("<%=UserAdminUIConstants.DOMAIN_SEPARATOR%>") > -1) {
                errorMessage = "Domain";
                return errorMessage;
            }

            return errorMessage;
        }

        function doValidation() {
            var reason = "";

            var e = document.getElementById("domain");

            var usrRegEx = "<%=Encode.forJavaScriptBlock(userStoreInfo.getUserNameRegEx())%>";

            if (e != null) {

                var selectedDomainValue = e.options[e.selectedIndex].text.toUpperCase()
                var pwd = "pwd_";
                var usr = "usr_";

                var passwordRegExElm = document.getElementById(pwd + selectedDomainValue);
                var usrRegExElm = document.getElementById(pwd + selectedDomainValue);

                if (passwordRegExElm != null) {
                    passwordRegEx = document.getElementById(pwd + selectedDomainValue).value;
                } else {
                    passwordRegEx = document.getElementById("pwd_primary_null").value;
                }

                if (usrRegExElm != null) {
                    usrRegEx = document.getElementById(usr + selectedDomainValue).value;
                } else {
                    usrRegEx = document.getElementById("usr_primary_null").value;
                }
            } else {

                passwordRegEx = document.getElementById("pwd_primary_null").value;

                usrRegEx = document.getElementById("usr_primary_null").value;

            }

            reason = validateString("username", usrRegEx);

            if (reason != "") {
                if (reason == "No conformance") {
                    <%
                           String usernameRegEx =   userStoreInfo.getUserNameRegEx();
                           String usernameErrorMessage = userStoreInfo.getUsernameRegExViolationErrorMsg();
                           if (StringUtils.isBlank(usernameErrorMessage)){
                               usernameErrorMessage = resourceBundle.getString("enter.user.name.not.conforming");
                           }
                       %>

                    CARBON.showWarningDialog("<%=Encode.forJavaScriptBlock(Encode.forHtml(usernameErrorMessage))%>");

                } else if (reason == "Empty string") {
                    CARBON.showWarningDialog("<fmt:message key="enter.user.name.empty"/>");
                } else if (reason == "Domain") {
                    CARBON.showWarningDialog("<fmt:message key="enter.user.name.domain"/>");
                }
                return false;
            }

            if (!skipPasswordValidation) {

                reason = validatePasswordOnCreation("password", "retype", passwordRegEx);
                if (reason != "") {
                    if (reason == "Empty Password") {
                        CARBON.showWarningDialog("<fmt:message key="password.fields.empty"/>");
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
            } else {
                <%
                String pattern = "";

                if (isAskPasswordEnabled) {
                    ClaimDataAdminClient claimDataAdminClient = new ClaimDataAdminClient((String)
                session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE),
                CarbonUIUtil.getServerURL(config.getServletContext(), session),
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT));
                try {
                    pattern = claimDataAdminClient.getRegex(EMAIL_CLAIM);
                }
                catch (ClaimDataAdminException e) {
                    logger.error("Error while getting local claims", e);
                }
                }
                %>
                var emailPattern;
                <%
                if (pattern.isEmpty() || pattern.length() == 2) {
                %>
                emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
                <%
                } else {
                %>
                emailPattern = <%=pattern%>;
                <%
                }
                %>
                reason = validateString("email", emailPattern);
                if (reason != "") {
                    if (reason == "Empty string") {
                        CARBON.showWarningDialog("<fmt:message key="enter.email.empty"/>");
                    } else if (reason == "No conformance") {
                        CARBON.showWarningDialog("<fmt:message key="enter.email.not.conforming"/>");
                    }
                    return false;
                }
            }
            return true;
        }

        function showHideUsers(element) {
            element.style.display = (element.style.display != 'block') ? 'block' : 'none';
        }

        function doCancel() {
            location.href = '../userstore/add-user-role.jsp';
        }

        function doNext() {
            document.dataForm.action = "add-step2.jsp";
            if (doValidation() == true) {
                document.dataForm.submit();
            }
        }

        function doFinish() {
            document.dataForm.action = "add-finish-ajaxprocessor.jsp";
            if (doValidation() == true) {
                document.dataForm.submit();
            }
        }

        function definePasswordHere() {
            var passwordMethod = document.getElementById('defineHere');
            if (passwordMethod.checked) {
                skipPasswordValidation = false;
                jQuery('#emailRow').remove();
                jQuery('#passwordRow').show();
                jQuery('#retypeRow').show();
            }
        }

        function askPasswordFromUser() {
            var emailInput = document.getElementsByName('email')[0];
            var passwordMethod = document.getElementById('askFromUser');
            if (passwordMethod.checked) {
                skipPasswordValidation = true;
                jQuery('#passwordRow').hide();
                jQuery('#retypeRow').hide();
                if (emailInput == null) {
                    var mainTable = document.getElementById('mainTable');
                    var newTr = mainTable.insertRow(mainTable.rows.length);
                    newTr.id = "emailRow";
                    newTr.innerHTML = '<td><fmt:message key="enter.email"/><font color="red">*</font></td><td>' +
                    '<input type="text" name="email" style="width:150px"/></td>';
                } else {
                    jQuery('#emailRow').show();
                }
            }
        }


    </script>
    <div id="middle">
        <h2><fmt:message key="add.new.user"/></h2>

        <div id="workArea">
            <h3><fmt:message key="step.1.user"/></h3>

            <form method="post" action="add-finish-ajaxprocessor.jsp" name="dataForm" onsubmit="return doValidation();">

                <input type="hidden" id="pwd_primary_null" name="pwd_primary_null"
                       value=<%=Encode.forHtml(userRealmInfo.getPrimaryUserStoreInfo().getPasswordRegEx())%>>
                <input type="hidden" id="usr_primary_null" name="usr_primary_null"
                       value=<%=Encode.forHtml(userRealmInfo.getPrimaryUserStoreInfo().getUserNameRegEx())%>>

                <%

                    allUserStoreInfo = userRealmInfo.getUserStoresInfo();
                    if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
                        for (int i = 0; i < allUserStoreInfo.length; i++) {
                            if (allUserStoreInfo[i] != null) {
                                String pwdRegEx = allUserStoreInfo[i].getPasswordRegEx();
                                String usrRegEx = allUserStoreInfo[i].getUserNameRegEx();
                                if (allUserStoreInfo[i].getDomainName() != null) {
                %>
                <input type="hidden"
                       id="pwd_<%=Encode.forHtmlAttribute(allUserStoreInfo[i].getDomainName().toUpperCase())%>"
                       name="pwd_<%=Encode.forHtmlAttribute(allUserStoreInfo[i].getDomainName().toUpperCase())%>"
                       value=<%=Encode.forHtml(pwdRegEx)%>>
                <input type="hidden"
                       id="usr_<%=Encode.forHtmlAttribute(allUserStoreInfo[i].getDomainName().toUpperCase())%>"
                       name="usr_<%=Encode.forHtmlAttribute(allUserStoreInfo[i].getDomainName().toUpperCase())%>"
                       value=<%=Encode.forHtml(usrRegEx)%>>

                <% }

                }
                }
                }

                %>

                <table class="styledLeft" id="userAdd" width="60%">
                    <thead>
                    <tr>
                        <th><fmt:message key="enter.user.name"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal" id="mainTable">
                                <%
                                    if (domainNames != null && domainNames.size() > 0) {
                                %>
                                <tr>
                                    <td><fmt:message key="select.domain"/></td>
                                    <td><select id="domain" name="domain">
                                        <%
                                            for (String domainName : domainNames) {
                                                if (selectedDomain.equals(domainName)) {
                                        %>
                                        <option selected="selected" value="<%=Encode.forHtmlAttribute(domainName)%>">
                                            <%=Encode.forHtmlContent(domainName)%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(domainName)%>">
                                            <%=Encode.forHtmlContent(domainName)%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                <tr>
                                    <td><fmt:message key="user.name"/><font color="red">*</font>
                                    </td>
                                    <td><input type="text" name="username"
                                               value=""
                                               style="width:150px"/></td>
                                </tr>
                                <%
                                    if (isAskPasswordEnabled) {
                                %>

                                <tr>
                                    <td>
                                        <input type="radio" name="passwordMethod" id="defineHere"
                                               value="defineHere" checked="checked" onclick="definePasswordHere();"/>
                                    </td>
                                    <td><fmt:message key="define.password.here"/></td>
                                </tr>
                                <tr>
                                    <td>
                                        <input type="radio" name="passwordMethod" id="askFromUser"
                                               value="askFromUser" onclick="askPasswordFromUser();"/>
                                    </td>
                                    <td><fmt:message key="ask.password.user"/></td>
                                </tr>

                                <%
                                    }
                                %>
                                <tr id="passwordRow">
                                    <td><fmt:message key="password"/><font color="red">*</font></td>
                                    <td><input type="password" name="password" id="password" style="width:150px"
                                               autocomplete="new-password"/></td>
                                </tr>
                                <tr id="retypeRow">
                                    <td><fmt:message key="password.repeat"/><font color="red">*</font></td>
                                    <td><input type="password" id="password-repeat" autocomplete="new-password" name="retype" style="width:150px"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <%
                                if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/rolemgt")) {
                            %>
                            <input type="button" class="button" value="<fmt:message key="next"/> >"
                                   onclick="doNext();"/>
                            <%
                                }
                            %>
                            <input type="button" class="button" value="<fmt:message key="finish"/>"
                                   onclick="doFinish();"/>
                            <input type="button" class="button" value="<fmt:message key="cancel"/>"
                                   onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
        <p>&nbsp;</p>
    </div>
</fmt:bundle>
