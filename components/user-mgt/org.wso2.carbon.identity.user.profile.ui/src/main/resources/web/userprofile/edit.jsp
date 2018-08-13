<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO" %>
<%@page import="org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO" %>
<%@page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.net.URLEncoder" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%!
    private static final String ACCOUNT_LOCKED_CLAIM_URI = "http://wso2.org/claims/identity/accountLocked";
    private static final String ACCOUNT_DISABLED_CLAIM_URI = "http://wso2.org/claims/identity/accountDisabled";
%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileUIUtil" %>
<%@ page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileUIException" %>

<%
    boolean readOnlyUserStore = false;
    String profile = request.getParameter("profile");
    String fromUserMgt = request.getParameter("fromUserMgt");
    String noOfProfiles = request.getParameter("noOfProfiles");
    String ACCOUNT_DISABLED = "http://wso2.org/claims/identity/accountDisabled";
    String currentUser = (String) session.getAttribute("logged-user");
    String encryptedUsername = request.getParameter("username");
    String decryptedUsername = null;
    boolean isLockable = true;

    if (noOfProfiles == null) {
        noOfProfiles = "0";
    }

    if (fromUserMgt == null) fromUserMgt = "false";

    UserProfileDTO userProfile = null;
    UserFieldDTO[] userFields = null;
    String forwardTo = null;
    String[] profileConfigs = null;
    String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        if (encryptedUsername != null) {
            decryptedUsername = UserProfileUIUtil.getDecryptedUsername(encryptedUsername);
        }
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserProfileCient client = new UserProfileCient(cookie, backendServerURL,
                configContext);
        userProfile = client.getUserProfile(decryptedUsername, profile);
        
        if ("readonly".equals(userProfile.getProfileConifuration())){
        	readOnlyUserStore = true;
        }
        
        if (userProfile != null) {
            userFields = client.getOrderedUserFields(userProfile.getFieldValues());
            profileConfigs = userProfile.getProfileConfigurations();
        }
        isLockable = UserProfileUIUtil.isAccountLockable(decryptedUsername);
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.loading.user.profile.data");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>

<%
    if (forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    return;
    }
%>

<script type="text/javascript">
</script>

<!-- Set checkbox checked/unchecked value to hidden textbox value -->
<script type="text/javascript">
    function setBooleanValueToTextBox(element) {
        document.getElementById(element.value).value = element.checked;
    }
</script>

<fmt:bundle basename="org.wso2.carbon.identity.user.profile.ui.i18n.Resources">
    <carbon:breadcrumb label="update.profile"
                       resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <%
            if ("true".equals(fromUserMgt)) {
        %>
        <%
            if (!readOnlyUserStore) {
        %>
        <h2><fmt:message key='update.profile1'/><%=Encode.forHtml(decryptedUsername)%>
        </h2>
        <%
        } else {
        %>
        <h2><fmt:message key='view.profile1'/><%=Encode.forHtml(decryptedUsername)%>
        </h2>
        <%
            }
        } else {
        %>
        <%
            if (!readOnlyUserStore) {
        %>
        <h2><fmt:message key='update.profile'/></h2>
        <%
        } else {
        %>
        <h2><fmt:message key='view.profile'/></h2>
        <%
                }
            }
        %>
        <div id="workArea">
            <script type="text/javascript">
                function validate() {

                <% if (userFields != null) {
                    for (int i = 0; i < userFields.length; i++) {
                        if ((ACCOUNT_DISABLED_CLAIM_URI.equals(userFields[i].getClaimUri()) ||
                                ACCOUNT_LOCKED_CLAIM_URI.equals(userFields[i].getClaimUri())) &&
                                userFields[i].getFieldValue() == null ) {
                            userFields[i].setFieldValue("false");
                        }
                        if (!userFields[i].getReadOnly() && !userFields[i].getClaimUri().equals(ACCOUNT_DISABLED)) {%>
                        var value = document.getElementsByName("<%=userFields[i].getClaimUri()%>")[0].value;
                        <%if (userFields[i].getRequired() && userFields[i].getDisplayName()!=null) {%>
                            if (validateEmpty("<%=userFields[i].getClaimUri()%>").length > 0) {
                                CARBON.showWarningDialog("<%=Encode.forJavaScript(Encode.forHtml(userFields[i].
                                                           getDisplayName()))%>" + " <fmt:message key='is.required'/>");
                        return false;
                    }
                    <%}
                      if(userFields[i].getRegEx() != null){ %>
                    var reg = new RegExp("<%=Encode.forJavaScript(userFields[i].getRegEx())%>");
                    var valid = reg.test(value);
                    if (value != '' && !valid) {
                        CARBON.showWarningDialog("<%=Encode.forJavaScript(Encode.forHtml(userFields[i].
                                                          getDisplayName()))%>" + " <fmt:message key='is.not.valid'/>");
                                return false;
                            }
                        <%}
                        }
                    if (!isLockable && userFields[i].getClaimUri().equals(ACCOUNT_LOCKED_CLAIM_URI)) { %>
                        var value = document.getElementsByName("<%=userFields[i].getClaimUri()%>")[0].value;
                        if (value == "true") {
                            CARBON.showWarningDialog(" <fmt:message key='lock.message' />" +
                            "<%=Encode.forJavaScript(Encode.forHtml(decryptedUsername))%>");
                            return false;
                        }
                    <%}
                    }
                }
                %>
                       var unsafeCharPattern = /[<>`\"]/;
                       var elements = document.getElementsByTagName("input");
                       for(i = 0; i < elements.length; i++){
                           if((elements[i].type === 'text' || elements[i].type === 'password') &&
                              elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null){
                               CARBON.showWarningDialog("<fmt:message key="unsafe.char.validation.msg"/>");
                               return false;
                           }
                       }
                       document.updateProfileform.submit();
                }
            </script>

            <form method="post" name="updateProfileform"
                  action="edit-finish-ajaxprocessor.jsp?profile=<%=Encode.forUriComponent(userProfile.getProfileName())%>&fromUserMgt=<%=Encode.forUriComponent(fromUserMgt)%>&noOfProfiles=<%=Encode.forUriComponent(noOfProfiles)%>"
                  target="_self">
                <input type="hidden" name="username" value="<%=Encode.forHtmlAttribute(decryptedUsername)%>"/>
                <table style="width: 100%" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='user.profile'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0">
                                <tr>
                                    <td class="leftCol-small"><fmt:message key='profile.name'/>&nbsp;<span
                                            class="required">*</span></td>
                                    <td><%=Encode.forHtmlContent(userProfile.getProfileName())%>
                                    </td>
                                </tr>
                                <%
                                    if (!readOnlyUserStore && profileConfigs != null && profileConfigs.length > 0
                                            && profileConfigs[0] != null) {
                                %>
                                <tr>
                                    <td class="leftCol-small"><fmt:message key='profile.cofiguration'/></td>
                                    <td>
                                        <select name="profileConfiguration">
                                            <%
                                                for (int i = 0; i < profileConfigs.length; i++) {
                                            %>
                                            <%
                                                if (userProfile.getProfileConifuration().equals(profileConfigs[i])) {
                                            %>
                                            <option value="<%=Encode.forHtmlAttribute(profileConfigs[i])%>"
                                                    selected="selected">
                                                <%=Encode.forHtmlContent(profileConfigs[i])%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option value="<%=Encode.forHtmlAttribute(profileConfigs[i])%>">
                                                <%=Encode.forHtmlContent(profileConfigs[i])%>
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
                                <%
                                    if (userFields != null) {
                                        for (int i = 0; i < userFields.length; i++) {
                                            if (ACCOUNT_DISABLED.equals(userFields[i].getClaimUri()) &&
                                                    decryptedUsername.equals(currentUser)) {
                                                continue;
                                            }
                                %>
                                <%
                                    if (userFields[i].getDisplayName() != null) {
                                %>
                                <tr>
                                    <td class="leftCol-small"><%=Encode.forHtmlContent(userFields[i].getDisplayName())%> <%
                                        if (userFields[i].getRequired()) {
                                    %>
                                        &nbsp;<span class="required">*</span>
                                        <%
                                            }
                                        %>
                                    </td>
                                    <%
                                        String value = userFields[i].getFieldValue();
                                        if (value != null) {
                                            if (!readOnlyUserStore && !userFields[i].getReadOnly()) {
                                                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                                                    // assume as boolean value. But actually this must be sent from backend.
                                                    // will fix for next release.
                                    %>
                                    <td>
                                        <input class="text-box-big" type="checkbox"
                                               onclick="setBooleanValueToTextBox(this)"
                                                <%if (Boolean.parseBoolean(value)) {%> checked="checked" <%}%>
                                               value="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"/>
                                        <input id="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               name="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               type="hidden" value="<%=Encode.forHtmlAttribute(value)%>"/>
                                    </td>
                                    <%
                                    } else {
                                    %>
                                    <td><input id="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               name="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               class="text-box-big" type="text"
                                               value="<%=Encode.forHtmlAttribute(userFields[i].getFieldValue())%>">
                                    </td>
                                    <%
                                        }
                                    } else {
                                    %>
                                    <td><%=Encode.forHtmlContent(userFields[i].getFieldValue())%>
                                    </td>
                                    <%
                                        }
                                    } else {
                                    %>
                                    <%
                                        if (!readOnlyUserStore && !userFields[i].getReadOnly()) {
                                    %>

                                    <td><input id="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               name="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               class="text-box-big" type="text"></td>
                                    <%
                                    } else {
                                    %>
                                    <td><input id="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               name="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
                                               class="text-box-big" type="text" readonly="true"></td>
                                    <%
                                            }
                                        }
                                    %>
                                </tr>
                                <%
                                            }
                                        }
                                    }
                                %>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <%
                                if (!readOnlyUserStore) {
                            %>
                            <input name="updateprofile"
                                   type="button" class="button" value="<fmt:message key='update'/>"
                                   onclick="validate();"/>
                            <%
                                }
                            %>
                            <input type="button" class="button"
                                   onclick="javascript:location.href='index.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>' +
                                           '&fromUserMgt=<%=Encode.forUriComponent(fromUserMgt)%>&editCancel=true'"
                                   value="<fmt:message key='cancel'/>"/></td>
                    </tr>
                    </tbody>
                </table>

            </form>
        </div>
    </div>
</fmt:bundle>

