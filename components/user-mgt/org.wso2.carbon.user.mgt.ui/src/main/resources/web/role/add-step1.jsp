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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>


<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../userstore/display-messages.jsp"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<jsp:useBean id="roleBean" type="org.wso2.carbon.user.mgt.ui.RoleBean"
             class="org.wso2.carbon.user.mgt.ui.RoleBean" scope="session"/>


<%
    UserRealmInfo userRealmInfo = null;
    UserStoreInfo userStoreInfo = null;
    List<String> domainNames = null;
    String selectedDomain = null;
    String roleType = null;
    UserStoreInfo[] allUserStoreInfo = null;
    Boolean sharedRoleEnabled = false;
    boolean internal = false;
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);

        sharedRoleEnabled = (Boolean) session.getAttribute(UserAdminUIConstants.SHARED_ROLE_ENABLED);

        if (sharedRoleEnabled == null) {
            sharedRoleEnabled = client.isSharedRolesEnabled();
        }
        roleType = request.getParameter("roleType");
        internal = UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType);
        sharedRoleEnabled = sharedRoleEnabled && !internal;

        userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
        if (userRealmInfo == null) {
            userRealmInfo = client.getUserRealmInfo();
            session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
        }

        userStoreInfo = userRealmInfo.getPrimaryUserStoreInfo();     // TODO

        // domain name preparations
        String primaryDomainName = userRealmInfo.getPrimaryUserStoreInfo().getDomainName();

        domainNames = new ArrayList<String>();
        allUserStoreInfo = userRealmInfo.getUserStoresInfo();
        if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
            for (int i = 0; i < allUserStoreInfo.length; i++) {
                if (allUserStoreInfo[i] != null) {
                    if (allUserStoreInfo[i].getDomainName() != null && allUserStoreInfo[i].getWriteGroupsEnabled()) {
                        domainNames.add(allUserStoreInfo[i].getDomainName());
                    }
                }
            }
        }
        domainNames.add(UserAdminUIConstants.INTERNAL_DOMAIN.toUpperCase());
        domainNames.add(UserAdminUIConstants.APPLICATION_DOMAIN);

        if (domainNames.size() > 0) {
            if (primaryDomainName == null) {
                primaryDomainName = UserAdminUIConstants.PRIMARY_DOMAIN_NAME_NOT_DEFINED;
                domainNames.add(primaryDomainName);
            }
        }

        selectedDomain = roleBean.getDomain();
        if (selectedDomain == null || selectedDomain.trim().length() == 0) {
            selectedDomain = primaryDomainName;
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
    <carbon:breadcrumb label="add-roles"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function validateTextForIllegal(fld) {

            var illegalChars = /([?#^\|<>\"\'])/;
            var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
            if (illegalChars.test(fld) || illegalCharsInput.test(fld)) {
                return false;
            } else {
                return true;
            }
        }

        function validateString(fld1name, regString) {
            var stringValue = document.getElementsByName(fld1name)[0].value;
            var errorMessage = "";

            if (stringValue == "") {
                errorMessage = "Empty string";
                return errorMessage;
            } else if (regString != "null" && !stringValue.match(new RegExp(regString.trim()))) {
                errorMessage = "No conformance";
                return errorMessage;
            } else if (regString != "null" && stringValue == "") {
                return errorMessage;
            }

            if (stringValue.indexOf("<%=UserAdminUIConstants.DOMAIN_SEPARATOR%>") > -1) {
                errorMessage = "Domain";
                return errorMessage;
            }

            return errorMessage;
        }

        function doValidation() {

            var e = document.getElementById("domain");
            var roleRegEx = "<%=Encode.forJavaScriptBlock(userStoreInfo.getRoleNameRegEx())%>";
            if (e != null) {
                var selectedDomainValue = e.options[e.selectedIndex].text.toUpperCase()
                var rl = "role_";

                var roleRegExElm = document.getElementById(rl + selectedDomainValue);

                if (roleRegExElm != null) {
                    roleRegEx = document.getElementById(rl + selectedDomainValue).value;
                } else {
                    roleRegEx = document.getElementById("role_primary_null").value;
                }
            } else {

                roleRegEx = document.getElementById("role_primary_null").value;

            }

            reason = validateString("roleName", roleRegEx);
            if (reason != "") {
                if (reason == "No conformance") {
                    CARBON.showWarningDialog("<fmt:message key="enter.role.name.not.conforming"/>");
                } else if (reason == "Empty string") {
                    CARBON.showWarningDialog("<fmt:message key="enter.role.name.empty"/>");
                } else if (reason == "Domain") {
                    CARBON.showWarningDialog("<fmt:message key="enter.role.name.domain"/>");
                }
                return false;
            }

            return true;
        }

        function doCancel() {
            location.href = '../userstore/add-user-role.jsp';
        }

        function doNext() {
            if (!validateTextForIllegal(document.getElementsByName("roleName")[0].value)) {
                CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " " + "role name " + " " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
                return false;
            }
            document.addRoleForm.action = "add-step2.jsp";
            if (doValidation() == true) {
                document.addRoleForm.submit();
            }
        }


        function changeBasedOnDomain() {
            var val = $("select[id='domain']").val();
            if (val !== 'PRIMARY') {
                $("#sharedRoleTd").hide();
            } else {
                $("#sharedRoleTd").show();
            }
        }


    </script>

    <div id="middle">
        <%if (UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType)) {%>
        <h2><fmt:message key="add.internal.user.role"/></h2>
        <%} else { %>
        <h2><fmt:message key="add-roles"/></h2>
        <%} %>
        <div id="workArea">
            <h3><fmt:message key="step.1.role"/></h3>

            <form method="post" name="addRoleForm" onsubmit="return doValidation();" action="add-finish-ajaxprocessor.jsp">

                <input type="hidden" id="role_primary_null" name="role_primary_null"
                       value='<%=Encode.forHtmlAttribute(userStoreInfo.getRoleNameRegEx())%>'>


                <%

                    allUserStoreInfo = userRealmInfo.getUserStoresInfo();
                    if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
                        for (int i = 0; i < allUserStoreInfo.length; i++) {
                            if (allUserStoreInfo[i] != null) {
                                String roleRegEx = allUserStoreInfo[i].getRoleNameRegEx();
                                if (allUserStoreInfo[i].getDomainName() != null) {
                %>
                <input type="hidden"
                       id="role_<%=Encode.forHtmlAttribute(allUserStoreInfo[i].getDomainName().toUpperCase())%>"
                       name="role_<%=Encode.forHtmlAttribute(allUserStoreInfo[i].getDomainName().toUpperCase())%>"
                       value='<%=Encode.forHtmlAttribute(roleRegEx)%>'>
                <% }

                }
                }
                }

                %>

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="enter.role.details"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <%
                                    if (!UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType) &&
                                        domainNames != null && domainNames.size() > 0) {
                                %>
                                <tr>
                                    <td><fmt:message key="select.domain"/></td>
                                    <td colspan="2"><select onchange="changeBasedOnDomain()" id="domain" name="domain">
                                        <%
                                            for (String domainName : domainNames) {
                                                if (selectedDomain.equals(domainName)) {
                                        %>
                                        <option selected="selected"
                                                value="<%=Encode.forHtmlAttribute(domainName)%>">
                                            <%=Encode.forHtmlContent(domainName.toUpperCase())%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(domainName)%>">
                                            <%=Encode.forHtmlContent(domainName.toUpperCase())%>
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
                                    <td><fmt:message key="role.name"/><font color="red">*</font>
                                    </td>
                                    <td><input type="text" name="roleName" value=""/></td>
                                    <td id="sharedRoleTd"><c:if test="<%=sharedRoleEnabled%>">
                                        <input type="checkbox" value="true" name="sharedRole"
                                               id="sharedRoleBox"/>
                                        <label for="sharedRoleBox">&nbsp;<fmt:message
                                                key="shared.role"/></label>
                                    </c:if></td>
                                    <td><input type="hidden" name="roleType"
                                               value="<%=Encode.forHtmlAttribute(roleType)%>"/></td>
                                </tr>
                            </table>
                            <!-- normal table -->
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" value="<fmt:message key="next"/> >"
                                   onclick="doNext();"/>
                            <input type="submit" class="button" value="<fmt:message key="finish"/>">
                            <input type="button" class="button" value="<fmt:message key="cancel"/>"
                                   onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>
