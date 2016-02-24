<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"  prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.owasp.encoder.Encode" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO" %>
<%@page import="org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO" %>
<%@page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.core.UserCoreConstants"%><script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String username =  request.getParameter("username");
    String fromUserMgt =  request.getParameter("fromUserMgt");
    UserFieldDTO[] userFields = null;
    UserProfileDTO profile = null;
    String forwardTo = null;
    String[] profileConfigs = null;
	String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
	
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserProfileCient client = new UserProfileCient(cookie, backendServerURL, configContext);
        profile = client.getProfileFieldsForInternalStore();

        if (profile != null) {
            userFields = client.getOrderedUserFields(profile.getFieldValues());
            profileConfigs = profile.getProfileConfigurations();
        }
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.user.profile.data");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>

<%
    if ( forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }

function validateTextForIllegal(fld,fldName) {

    var illegalChars = /([?#^\|<>\"\'])/;
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
       return false;
    } else {
       return true;
    }
}

</script>

<script type="text/javascript">
    forward();
</script>
<%
    return;
    }
%>



<%@ page import="java.util.ResourceBundle" %>
<fmt:bundle
        basename="org.wso2.carbon.identity.user.profile.ui.i18n.Resources">
    <carbon:breadcrumb label="add.profile"
                       resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <%if ("true".equals(fromUserMgt)) {%>
        <h2><fmt:message key='add.new.profile1'/><%=Encode.forHtml(username)%></h2>
        <%} else {%>
        <h2><fmt:message key='add.new.profile2'/></h2>
        <%}%>

        <div id="workArea">
            <script type="text/javascript">
	            function validateTextForIllegal(fld,fldName) {
	
	                var illegalChars = /([?#^\|<>\"\'])/;
	                var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
	                if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
	                   return false;
	                } else {
	                   return true;
	                }
	            }
                function validate() {
                    // JS injection validation for name fields

                    if(!validateTextForIllegal(document.getElementsByName("profile")[0],"profile")) {
                        CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "profile name content"+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
                        return false;
                    }

                    var value = document.getElementsByName("profile")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="user.profilename.is.required"/>');
                        return false;
                    }

                <%if (userFields!=null) {
                    for (int i=0; i< userFields.length;i++) { %>
                    var value = document.getElementsByName("<%=userFields[i].getClaimUri()%>")[0].value;

                    // JS injection validation for name fields
                    if(!validateTextForIllegal(document.getElementsByName("<%=userFields[i].getClaimUri()%>")[0],"profilefiled")) {
                        CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "profile content"+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
                        return false;
                    }

                <% if (userFields[i].getRequired()&& userFields[i].getDisplayName()!=null) {%>
                    if (validateEmpty("<%=userFields[i].getClaimUri()%>").length > 0) {
                        CARBON.showWarningDialog("<%=Encode.forJavaScript(Encode.forHtml(userFields[i].
                                                           getDisplayName()))%>" + " <fmt:message key='is.required'/>");
                        return false;
                    }
                <%}

                if(userFields[i].getRegEx() != null){
                %>
                    var reg = new RegExp("<%=Encode.forJavaScript(userFields[i].getRegEx())%>");
                    var valid = reg.test(value);
                    if (value != '' && !valid) {
                        CARBON.showWarningDialog("<%=Encode.forJavaScript(Encode.forHtml(userFields[i].
                                                           getDisplayName()))%>" +" <fmt:message key='is.not.valid'/>");
                        return false;
                    }
                <%}
              }
              }%>

                    document.addProfileform.submit();
                }
            </script>

            <form method="post" name="addProfileform"
                  action="set-finish.jsp?fromUserMgt=<%=Encode.forUriComponent(fromUserMgt)%>" target="_self">
                <input type="hidden" name="username" value="<%=Encode.forHtmlAttribute(username)%>"/>
                <table style="width: 100%" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='user.profile'/></th>
                    </tr>
                    </thead>
                    <tbody>
		    <tr>
			<td class="formRow">
				<table class="normal" >
		                    <tr>
		                        <td class="leftCol-small"><fmt:message key='profile.name'/><font class="required">*</font></td>
		                        <td><input class="text-box-big" id="profile" name="profile"
		                                   type="text" /></td>
		                    </tr>
		                    <%if (profileConfigs != null && profileConfigs.length > 0 && profileConfigs[0] != null) { %>
		                    <tr>
		                        <td class="leftCol-small"><fmt:message key='profile.cofiguration'/></td>
		                        <td>
		                            <select name="profileConfiguration">
		                                <%for (int i = 0; i < profileConfigs.length; i++) { %>
		                                <%if (UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION.equals(profileConfigs[i])) { %>
		                                <option value="<%=Encode.forHtmlAttribute(profileConfigs[i])%>" selected="selected">
                                            <%=Encode.forHtmlContent(profileConfigs[i])%>
		                                </option>
		                                <%} else { %>
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
		                    <%} %>
		                    <% if (userFields != null) {
		                        for (int i = 0; i < userFields.length; i++) {
		                    %>
		                    <%if (userFields[i].getDisplayName() != null) {%>
		                    <tr>
		                        <td class="leftCol-small">
		                            <%=Encode.forHtmlContent(userFields[i].getDisplayName())%>
		                               <% if (userFields[i].getRequired()) {%>
		                                        <font class="required">*</font> 
		                               <%}%>
		                        </td>
                                <%
                                    if(!userFields[i].getReadOnly()) {
                                %>
		                        <td><input class="text-box-big"
		                                   id="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
		                                   name="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>" type="text">
                                </td>
                                <%
                                    } else {
                                %>
                                <td><input class="text-box-big"
		                                   id="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>"
		                                   name="<%=Encode.forHtmlAttribute(userFields[i].getClaimUri())%>" type="text"
                                           readonly="true"></td>
                                <%
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
                        <td class="buttonRow" >
                            <input name="addprofile" type="button" class="button" value="<fmt:message key='add'/>" onclick="validate();"/>
                            <input type="button" class="button"
                                  <%if ("true".equals(fromUserMgt)) {%>
                                   onclick="javascript:location.href='index.jsp?username='+
                                           '<%=Encode.forJavaScript(Encode.forUriComponent(username))%>&fromUserMgt=true'"
                                  <%}else{%>
                                   onclick="javascript:location.href='index.jsp?region=region5&item=userprofiles_menu&ordinal=0'"
                                  <%}%>
                                   value="<fmt:message key='cancel'/>"/></td>
                    </tr>
                    </tbody>
                </table>

            </form>
        </div>
    </div>
</fmt:bundle>

