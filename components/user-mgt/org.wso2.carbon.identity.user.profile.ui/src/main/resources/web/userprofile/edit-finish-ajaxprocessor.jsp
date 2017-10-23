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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="org.owasp.encoder.Encode"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO"%>
<%@ page import="org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO"%>
<%@ page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%><script type="text/javascript" src="extensions/js/vui.js"></script>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileUIUtil" %>
<%@ page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileUIException" %>
<jsp:include page="../dialog/display_messages.jsp" />
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String username = request.getParameter("username");
    String profile = request.getParameter("profile");
    String profileConfiguration = request.getParameter("profileConfiguration");
    String fromUserMgt = request.getParameter("fromUserMgt");
    String noOfProfiles = request.getParameter("noOfProfiles");
    String encryptedUsername;

    try {
        encryptedUsername = UserProfileUIUtil.getEncryptedAndBase64encodedUsername(username);
    } catch (UserProfileUIException e) {
        String message = MessageFormat.format(resourceBundle.getString("error.while.updating.profile"), null);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
%>
        <script type="text/javascript">
            location.href = "../user/user-mgt.jsp?ordinal=1";
        </script>
<%
        return;
    }
%>

<%
    if (StringUtils.isBlank(username) || StringUtils.isBlank(profile)) {
        String message = MessageFormat.format(resourceBundle.getString("error.while.updating.profile"), null);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
%>
        <script type="text/javascript">
            location.href = "../user/user-mgt.jsp?ordinal=1";
        </script>
<%
        return;
    }
	UserFieldDTO[] fieldDTOs = null;
	String forwardTo = null;
    if (StringUtils.isBlank(noOfProfiles)) {
        noOfProfiles = "0";
    }
    if (StringUtils.isBlank(fromUserMgt)) {
        fromUserMgt = "false";
    }

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserProfileCient client = new UserProfileCient(cookie, backendServerURL, configContext);        

    	fieldDTOs = client.getProfileFieldsForInternalStore().getFieldValues();
    	
        if (fieldDTOs!=null)
        {
          for (UserFieldDTO field : fieldDTOs) {
            String value = request.getParameter(field.getClaimUri());
            if(value == null){
                value = "";
            }
            field.setFieldValue(value);   
          }
        }
        
        UserProfileDTO userprofile= new UserProfileDTO();
        userprofile.setProfileName(profile);
        userprofile.setFieldValues(fieldDTOs);      
        userprofile.setProfileConifuration(profileConfiguration);
        client.setUserProfile(username, userprofile);
        String message = resourceBundle.getString("user.profile.updated.successfully");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.INFO, request);
        if ("true".equals(fromUserMgt)) {
            //if there is only one profile, send directly to user-mgt.jsp
            if ((!client.isAddProfileEnabled()) && ((Integer.parseInt(noOfProfiles)) == 1)) {
                forwardTo = "../user/user-mgt.jsp?ordinal=1";
            } else {
                forwardTo = "index.jsp?username=" + Encode.forUriComponent(encryptedUsername) + "&fromUserMgt=" +
                        Encode.forUriComponent(fromUserMgt);
            }
        } else {
        	forwardTo ="index.jsp?region=region5&item=userprofiles_menu&ordinal=0";        	
        }

    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.while.updating.profile.user"),
                username, e.getMessage());
    	CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "edit.jsp?username=" + Encode.forUriComponent(encryptedUsername) + "&profile=" +
                Encode.forUriComponent(profile) + "&fromUserMgt=true&noOfProfiles=" + Encode.forUriComponent(noOfProfiles);
    }
%>

<script type="text/javascript">
    function forward() {
        location.href ="<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>