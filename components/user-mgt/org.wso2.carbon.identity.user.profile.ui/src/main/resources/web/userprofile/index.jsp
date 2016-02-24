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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO"%>
<%@page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo"%>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants"%>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    boolean readOnlyUserStore = false;
    String username = request.getParameter("username");
    String forwardTo = null;
    String fromUserMgt = null;
    UserProfileCient client = null;
    String editCancel = request.getParameter("editCancel");
    UserRealmInfo userRealmInfo = null;
    boolean multipleProfilesEnabled = false;

    if (username == null) {
        username = (String) request.getSession().getAttribute("logged-user");
    }

    fromUserMgt = request.getParameter("fromUserMgt");
    
    if (fromUserMgt==null) fromUserMgt = "false";

    String addAction = "add.jsp?username=" + Encode.forUriComponent(username) + "&fromUserMgt=" +
            Encode.forUriComponent(fromUserMgt);

    UserProfileDTO[] profiles = new UserProfileDTO[0];
    String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session
				.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
				.getServletContext().getAttribute(
						CarbonConstants.CONFIGURATION_CONTEXT);
        client = new UserProfileCient(cookie,
                backendServerURL, configContext);
        readOnlyUserStore = client.isReadOnlyUserStore();
     	profiles = client.getUserProfiles(username);


        //read the domain of the user
        String userDomain = UserProfileCient.extractDomainFromName(username);
        if (StringUtils.isNotBlank(userDomain)) {
            multipleProfilesEnabled = client.isAddProfileEnabledForDomain(userDomain);
        } else {
            multipleProfilesEnabled = client.isAddProfileEnabledForDomain("");

        }

        //get user store manager config
        userRealmInfo = (UserRealmInfo)session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
        if (userRealmInfo == null) {
            UserAdminClient userAdminClient = new UserAdminClient(cookie, backendServerURL, configContext);
            userRealmInfo = userAdminClient.getUserRealmInfo();
            session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
        }

        //select the user storemanager of user and check readonly.
        UserStoreInfo[] allUserStoreInfo = userRealmInfo.getUserStoresInfo();
        if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
            for (int i = 0; i < allUserStoreInfo.length; i++) {
                if (allUserStoreInfo[i] != null) {
                    if (allUserStoreInfo[i].getDomainName() != null &&
                        allUserStoreInfo[i].getDomainName().equalsIgnoreCase(userDomain)) {
                        readOnlyUserStore = allUserStoreInfo[i].getReadOnly();
                    }
                }
            }
        }

        //if only one profile exist, take directly to edit.jsp
        if ((!multipleProfilesEnabled && profiles != null && profiles.length == 1) || readOnlyUserStore ) {

            if("true".equals(editCancel) && "true".equals(fromUserMgt)){
                forwardTo = "../user/user-mgt.jsp?ordinal=1";
            } else {
                forwardTo = "edit.jsp?username=" + Encode.forUriComponent(username) + "&profile=" +
                            Encode.forUriComponent(profiles[0].getProfileName()) + "&fromUserMgt=" +
                            Encode.forUriComponent(fromUserMgt) + "&noOfProfiles=1";
            }
        } else {
            
        	 %>
        	 <jsp:include page="../dialog/display_messages.jsp" />
        	 <% 
        }
	} catch (Exception e) {
		String message = resourceBundle.getString("error.while.loading.user.profile.data");
		CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.ERROR, request);
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
</script>

<script type="text/javascript">
    forward();
</script>
<%
    return;
	}
%>

<fmt:bundle basename="org.wso2.carbon.identity.user.profile.ui.i18n.Resources">
    <carbon:breadcrumb
            label="user.profiles"
            resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
            
            <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
			<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
			<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
			
    <div id="middle">
        <%
        	if ("true".equals(fromUserMgt)) {
        %>
       		<h2><fmt:message key='user.profiles1'/><%=Encode.forHtml(username)%></h2>
        <%
        	} else {
        %>
        <h2><fmt:message key='my.profiles'/></h2>
        <%
        	}
        %>
        <div id="workArea">   
            <script type="text/javascript">
              function removeProfile(username,profile) {
                 if(profile == "default"){
                	 CARBON.showWarningDialog("<fmt:message key='cannot.remove.default.profile'/>", null, null);
                	 return;
                 }
        	     CARBON.showConfirmationDialog("<fmt:message key='remove.message1'/>"+ profile +"<fmt:message key='remove.message2'/>",
                    function() {
              	       location.href = "remove-profile.jsp?username=" + username + "&profile=" + profile +
                                     "&fromUserMgt=<%=Encode.forUriComponent(fromUserMgt)%>";
                     }, null);
                 }
            </script>
            <% if(!readOnlyUserStore) {%>
            <div style="height:30px;">
                <%if (multipleProfilesEnabled) {%>
                <a href="javascript:document.location.href='<%=Encode.forJavaScript(addAction)%>'" class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"><fmt:message
                        key='add.new.profiles'/></a>
                <%}%>
            </div>
             <% } %>      
           	<table style="width: 100%" class="styledLeft">
			<thead>
				<tr>
					<th colspan="2"><fmt:message key='available.profiles'/></th>
				</tr>
			</thead>
				<tbody>
           <%
           	if (profiles != null && profiles.length > 0) {
           			for (int i = 0; i < profiles.length; i++) { 
           				String profileName = profiles[i].getProfileName();
           %>		
			<tr>
                <td width="50%">
                    <a href="edit.jsp?username=<%=Encode.forUriComponent(username)%>&profile=<%=Encode.
                    forUriComponent(profileName)%>&fromUserMgt=<%=Encode.forUriComponent(fromUserMgt)%>">
                        <%=Encode.forHtmlContent(profileName)%>
                    </a>
                </td>
				<td width="50%">
				<%
                    if (readOnlyUserStore == false && !"default".equals(profileName)) {
                %>
				<a title="<fmt:message key='remove.profile'/>"
                                   onclick="removeProfile('<%=Encode.forJavaScriptAttribute(username)%>',
                                           '<%=Encode.forJavaScriptAttribute(profileName)%>');return false;"
                                   href="#" style="background-image: url(../userprofile/images/delete.gif);" class="icon-link">
                                    <fmt:message key='delete'/></a>
                <%
                    }
                %>                    
               </td>			
			</tr>
		  <%
		  	}
		  		} else {
		  %>
		    <tr>
				<td width="100%" colspan="2"><i><fmt:message key='no.profiles'/></i></td>
			 </tr>
		  <%
		  	}
		  %>
		  </tbody>
		  </table>		
          </div>
    </div>
</fmt:bundle>
