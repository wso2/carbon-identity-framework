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

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.owasp.encoder.Encode" %>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>

<%
    boolean logout = false;
    boolean finish = false;
    boolean viewUsers = false;
    
	String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    
    String pageNumber = request.getParameter("pageNumber");
    String forwardTo = null;

    if(request.getParameter("logout") != null){
        logout = Boolean.parseBoolean(request.getParameter("logout"));
    }
    if(request.getParameter("finish") != null){
        finish = Boolean.parseBoolean(request.getParameter("finish"));
    }
    if(request.getParameter("viewUsers") != null){
        viewUsers = Boolean.parseBoolean(request.getParameter("viewUsers"));
    }
    String roleName = request.getParameter("roleName");
    try {

        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);

        String[] shownUsers = request.getParameterValues("shownUsers");
	    String[] selectedUsers = request.getParameterValues("selectedUsers");
        ArrayList<String> deletedList = new ArrayList<String>();
        if(selectedUsers != null){
            Arrays.sort(selectedUsers);
        }

        if(shownUsers != null){
            for(String name : shownUsers){
                if(selectedUsers != null){
                    if(Arrays.binarySearch(selectedUsers, name) < 0){
                        deletedList.add(name);
                    }
                } else {
                    deletedList.add(name);
                }
            }
        }
        selectedUsers = addSelectedUserLists(selectedUsers, (Map<String,Boolean>)session.getAttribute("checkedUsersMap"));
        addDeletedUserLists(deletedList,(Map<String,Boolean>)session.getAttribute("checkedUsersMap"));

        if(viewUsers){
            client.addRemoveUsersOfRole(roleName, null,deletedList.toArray(new String[deletedList.size()]));
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE_EXCEEDED);
        } else {
            client.addRemoveUsersOfRole(roleName, selectedUsers, null);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED);
        }

        String message = MessageFormat.format(resourceBundle.getString("role.update"), roleName);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);

        if(logout){
            forwardTo = "../admin/logout_action.jsp";
        } else if(finish) {          
        	forwardTo = "role-mgt.jsp";
        } else if (viewUsers){
            forwardTo = "view-users.jsp?roleName=" + Encode.forUriComponent(roleName) + "&pageNumber=" +
                        Encode.forUriComponent(pageNumber);
        } else {
            forwardTo = "edit-users.jsp?roleName=" + Encode.forUriComponent(roleName) + "&pageNumber=" +
                        Encode.forUriComponent(pageNumber);
        }

    } catch(InstantiationException e){
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.",
                CarbonUIMessage.ERROR, request);
        forwardTo = "role-mgt.jsp?ordinal=1";
    } catch (Exception e) {
	    String message = MessageFormat.format(resourceBundle.getString("role.cannot.update"),
                roleName, e.getMessage());
	    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        if (viewUsers){
	        forwardTo = "view-users.jsp?roleName=" + Encode.forUriComponent(roleName);
        } else {
            forwardTo = "edit-users.jsp?roleName=" + Encode.forUriComponent(roleName);
        }
    }
%>

<script type="text/javascript">

    function forward(){
        location.href = "<%=forwardTo%>";
    }

    forward();
 </script>

<%!
    private String[] addSelectedUserLists(String[] selectedUsers, Map<String,Boolean> sessionUsersMap){
        List<String> selectedUsersList = new ArrayList<String>();
        if(selectedUsers != null && selectedUsers.length > 0){
            selectedUsersList = new ArrayList<String>(Arrays.asList(selectedUsers));
        }
        if(sessionUsersMap != null){
            Set<String> keys = sessionUsersMap.keySet();
            for(String key:keys){
                if(sessionUsersMap.get(key) == true && !selectedUsersList.contains(key)){
                    selectedUsersList.add(key);
                }
            }
        }
        selectedUsers = selectedUsersList.toArray(new String[selectedUsersList.size()]);
        return selectedUsers;
    }

    private void addDeletedUserLists(List<String> deletedUsers, Map<String,Boolean> sessionUsersMap){
        if(sessionUsersMap != null){
            Set<String> keys = sessionUsersMap.keySet();
            for(String key:keys){
                if(sessionUsersMap.get(key) == false && !deletedUsers.contains(key)){
                    deletedUsers.add(key);
                }
            }
        }
    }

%>
