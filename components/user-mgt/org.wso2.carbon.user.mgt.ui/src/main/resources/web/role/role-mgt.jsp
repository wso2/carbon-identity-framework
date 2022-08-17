<%--
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.PaginatedNamesBean" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserManagementWorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserStoreCountClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    boolean error = false;
    boolean newFilter = false;
    boolean doRoleList = true;
    boolean showFilterMessage = false;
    boolean multipleUserStores = false;
    List<FlaggedName> datasList= null;
    FlaggedName[] roles = null;
    FlaggedName exceededDomains = null;
    String[] domainNames = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;
    UserRealmInfo userRealmInfo = null;
    Set<FlaggedName> workFlowAddPendingRoles = new LinkedHashSet<FlaggedName>();
    Set<String> workFlowAddPendingRolesList = new LinkedHashSet<String>();
    Set<String> workFlowDeletePendingRoles = null;
    Set<FlaggedName> activeRoleList;
    Set<FlaggedName> showDeletePendingRoles = new LinkedHashSet<FlaggedName>();
    Set<String> showDeletePendingRolesList = new LinkedHashSet<String>();
    Set<FlaggedName> aggregateRoleList = new LinkedHashSet<FlaggedName>();
    Set<FlaggedName> removeRoleElement = new LinkedHashSet<FlaggedName>();
    Set<String> countableUserStores = new LinkedHashSet<String>();
    Map<String, String> roleCount = new HashMap<String, String>();

    // clear session data
    session.removeAttribute("roleBean");
    session.removeAttribute(UserAdminUIConstants.ROLE_READ_ONLY);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_VIEW_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
    session.removeAttribute("previousRole");
    // search filter
    String selectedDomain = request.getParameter("domain");
    if (StringUtils.isBlank(selectedDomain)) {
        selectedDomain = (String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER);
        if (StringUtils.isBlank(selectedDomain)) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }


    //  search filter for count
    String selectedCountDomain = request.getParameter("countDomain");
    if (StringUtils.isBlank(selectedCountDomain)) {
        selectedCountDomain = (String) session.getAttribute(UserAdminUIConstants.USER_LIST_COUNT_DOMAIN_FILTER);
        if (StringUtils.isBlank(selectedCountDomain)) {
            selectedCountDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    session.setAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER, selectedDomain.trim());
    session.setAttribute(UserAdminUIConstants.ROLE_LIST_COUNT_DOMAIN_FILTER, selectedCountDomain.trim());

    String filter = request.getParameter(UserAdminUIConstants.ROLE_LIST_FILTER);
    if (StringUtils.isBlank(filter)) {
        filter = (String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_FILTER);
        if (StringUtils.isBlank(filter)) {
            filter = "*";
        }
    } else {
        if (filter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER);
        }
        newFilter = true;
    }


    String countFilter = request.getParameter(UserAdminUIConstants.ROLE_COUNT_FILTER);
    if (StringUtils.isBlank(countFilter)) {
        countFilter = (java.lang.String) session.getAttribute(UserAdminUIConstants.ROLE_COUNT_FILTER);
        if (StringUtils.isBlank(countFilter)) {
            countFilter = "%";
        }
    } else {
        if (countFilter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
            session.removeAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER);
        }
        newFilter = true;
    }

    String modifiedFilter = filter.trim();
    if (!UserAdminUIConstants.ALL_DOMAINS.equalsIgnoreCase(selectedDomain)) {
        modifiedFilter = selectedDomain + UserAdminUIConstants.DOMAIN_SEPARATOR + filter;
        modifiedFilter = modifiedFilter.trim();
    }

    session.setAttribute(UserAdminUIConstants.ROLE_LIST_FILTER, filter.trim());
    session.setAttribute(UserAdminUIConstants.ROLE_COUNT_FILTER, countFilter.trim());

    String currentUser = (String) session.getAttribute("logged-user");
    userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
    if (userRealmInfo != null) {
        multipleUserStores = userRealmInfo.getMultipleUserStore();
    }
    String errorAttribute = (String) session.getAttribute(UserAdminUIConstants.DO_ROLE_LIST);
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED);

    // check page number
    try {

        pageNumber = Integer.parseInt(request.getParameter("pageNumber"));

    } catch (NumberFormatException ignored) {
        // page number format exception
        pageNumber = 0;
    }

    flaggedNameMap = (Map<Integer, PaginatedNamesBean>) session.getAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
    if (flaggedNameMap != null) {
        PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
        if (bean != null) {
            roles = bean.getNames();
            if (!ArrayUtils.isEmpty(roles)) {
                numberOfPages = bean.getNumberOfPages();
                doRoleList = false;
            }
        }
    }

    if (errorAttribute != null) {
        error = true;
        session.removeAttribute(UserAdminUIConstants.DO_ROLE_LIST);
    }

    if ((doRoleList || newFilter) && !error) {

        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            UserStoreCountClient countClient = new UserStoreCountClient(cookie, backendServerURL, configContext);
            UserManagementWorkflowServiceClient UserMgtClient = new
                    UserManagementWorkflowServiceClient(cookie, backendServerURL, configContext);

            if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/userstore/count/view")) {

                countableUserStores = countClient.getCountableUserStores();

                if (countableUserStores != null && countableUserStores.size() > 0) {
                    countableUserStores.add(UserAdminUIConstants.ALL_DOMAINS);
                    countableUserStores.add(UserAdminUIConstants.INTERNAL_DOMAIN);
                    countableUserStores.add(UserAdminUIConstants.APPLICATION_DOMAIN);
                }

                if (selectedCountDomain.equalsIgnoreCase(UserAdminUIConstants.ALL_DOMAINS)) {
                    roleCount = countClient.countRoles(countFilter);
                } else {
                    try {
                        roleCount.put(selectedCountDomain, String.valueOf(countClient.countRolesInDomain(countFilter,
                            selectedCountDomain)));
                    } catch (Exception e) {
                        // In this scenario an error should be shown in the count results section.
                        roleCount.put(selectedCountDomain, "Error while getting the role count");
                    }
                }
            }

            boolean sharedRoleEnabled = client.isSharedRolesEnabled();
            session.setAttribute(UserAdminUIConstants.SHARED_ROLE_ENABLED, sharedRoleEnabled);
            if (filter.length() > 0) {
                FlaggedName[] datas = client.getAllRolesNames(modifiedFilter, -1);
                if (CarbonUIUtil.isContextRegistered(config, "/usermgt-workflow/")) {
                    List<FlaggedName> preactiveRoleList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                    FlaggedName excessiveDomainElement = preactiveRoleList.remove(datas.length - 1);
                    removeRoleElement.add(excessiveDomainElement);

                    activeRoleList = new LinkedHashSet<FlaggedName>(preactiveRoleList);

                    String[] AddPendingRolesList = UserMgtClient.
                            listAllEntityNames("ADD_ROLE", "PENDING", "ROLE", modifiedFilter);

                    workFlowAddPendingRolesList = new LinkedHashSet<String>(Arrays.asList(AddPendingRolesList));

                    for (String s : AddPendingRolesList) {
                        FlaggedName flaggedName = new FlaggedName();
                        flaggedName.setItemName(s);
                        flaggedName.setEditable(true);
                        workFlowAddPendingRoles.add(flaggedName);
                    }

                    String[] DeletePendingUsersList = UserMgtClient.
                            listAllEntityNames("DELETE_ROLE", "PENDING", "ROLE", modifiedFilter);
                    workFlowDeletePendingRoles = new LinkedHashSet<String>(Arrays.asList(DeletePendingUsersList));

                    for (Iterator<FlaggedName> iterator = activeRoleList.iterator(); iterator.hasNext(); ) {
                        FlaggedName flaggedName = iterator.next();
                        if (flaggedName == null) {
                            continue;
                        }
                        String userName = flaggedName.getItemName();
                        if (workFlowDeletePendingRoles.contains(userName)) {
                            showDeletePendingRoles.add(flaggedName);
                            showDeletePendingRolesList.add(userName);
                            iterator.remove();
                        }
                    }
                    aggregateRoleList.addAll(activeRoleList);
                    aggregateRoleList.addAll(showDeletePendingRoles);
                    aggregateRoleList.addAll(workFlowAddPendingRoles);
                    aggregateRoleList.addAll(removeRoleElement);
                    datas = aggregateRoleList.toArray(new FlaggedName[aggregateRoleList.size()]);
                }
                datasList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                exceededDomains = datasList.remove(datasList.size() - 1);
                session.setAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED, exceededDomains);
                datas = datasList.toArray(new FlaggedName[datasList.size()]);
                if (datas == null || datas.length == 0) {
                    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_FILTER);
                    showFilterMessage = true;
                }
            }
            if (userRealmInfo == null) {
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }

            if (datasList != null) {
                flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                int max = pageNumber + cachePages;
                for (int i = (pageNumber - cachePages); i < max; i++) {
                    if (i < 0) {
                        max++;
                        continue;
                    }
                    PaginatedNamesBean bean = Util.retrievePaginatedFlaggedName(i, datasList);
                    flaggedNameMap.put(i, bean);
                    if (bean.getNumberOfPages() == i + 1) {
                        break;
                    }
                }
                roles = flaggedNameMap.get(pageNumber).getNames();
                numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                session.setAttribute(UserAdminUIConstants.ROLE_LIST_CACHE, flaggedNameMap);
            }
        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("error.while.role.filtered"),
                    e.getMessage());
%>
<script type="text/javascript">

    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>', null);
    });
</script>
<%
        }
    }

    if (userRealmInfo != null) {
        List<String> list = new ArrayList<String>();

        UserStoreInfo[]  allUserStoreInfo = userRealmInfo.getUserStoresInfo();
        if (allUserStoreInfo != null && allUserStoreInfo.length > 0) {
            for (int i = 0; i < allUserStoreInfo.length; i++) {
                if (allUserStoreInfo[i] != null) {
                    if (allUserStoreInfo[i].getDomainName() != null && allUserStoreInfo[i].getReadGroupsEnabled()) {
                        list.add(allUserStoreInfo[i].getDomainName());
                    }
                }
            }
        }

        list.add(UserAdminUIConstants.ALL_DOMAINS);
        list.add(UserAdminUIConstants.INTERNAL_DOMAIN);
        list.add(UserAdminUIConstants.APPLICATION_DOMAIN);
        //list.add(UserAdminUIConstants.WORKFLOW_DOMAIN);
        domainNames = list.toArray(new String[list.size()]);
    }
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
<carbon:breadcrumb label="roles"
		resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <script type="text/javascript">

        function deleteUserGroup(role) {
            function doDelete() {
                var roleName = role;
                $.ajax({
                    type: 'POST',
                    url: 'delete-finish-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: 'roleName=' + roleName + '&userType=internal',
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("role-mgt.jsp");
                        }
                    }
                });
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.role"/> ' + role + '?', doDelete, null);
        }

        <%if (showFilterMessage) {%>
        jQuery(document).ready(function () {
            CARBON.showInfoDialog('<fmt:message key="no.roles.filtered"/>', null, null);
        });
        <%}%>
        /*function doDelete(){
            location.href = 'delete-role.jsp?roleName=' + this.role+'&userType=internal';
        }*/
    </script>
    <script type="text/javascript">

        function updateUserGroup(role) {
                var roleName = role;
                location.href = 'rename-role.jsp?roleName=' + roleName;
        }

    </script>
    <div id="middle">
        <h2><fmt:message key="roles"/></h2>

        <div id="workArea">

            <form name="filterForm" method="post" action="role-mgt.jsp">
                <table class="styledLeft noBorders">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="role.search"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        if (ArrayUtils.isNotEmpty(domainNames)) {
                    %>
                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                                key="select.domain.search"/></td>
                        <td><select id="domain" name="domain">
                            <%
                                for (String domainName : domainNames) {
                                    if (selectedDomain.equals(domainName)) {
                            %>
                            <option selected="selected"
                                    value="<%=Encode.forHtmlAttribute(domainName)%>"><%=Encode.forHtmlContent(domainName)%>
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
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                                key="list.roles"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.ROLE_LIST_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(filter)%>" label="<fmt:message key="list.roles"/>"
                                   black-list-patterns="xml-meta-exists"/>

                            <input class="button" type="submit"
                                   value="<fmt:message key="role.search"/>"/>
                        </td>

                    </tr>
                    </tbody>
                </table>
            </form>

            <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/userstore/count/view")) { %>

            <form name="countForm" method="post" action="role-mgt.jsp">
                <table class="styledLeft">
                    <%
                        if (countableUserStores != null && !countableUserStores.isEmpty()) {
                    %>
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="role.count"/></th>
                    </tr>
                    </thead>
                    <tbody>


                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                                key="select.domain.search"/></td>
                        <td><select id="countDomain" name="countDomain">
                            <%
                                for (String domainName : countableUserStores) {
                                    if (selectedDomain.equals(domainName)) {
                            %>
                            <option selected="selected" value="<%=Encode.forHtmlAttribute(domainName)%>">
                                <%=Encode.forHtml(domainName)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(domainName)%>">
                                <%=Encode.forHtml(domainName)%>
                            </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                        </td>
                    </tr>


                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                                key="role.count"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.ROLE_COUNT_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(countFilter)%>" label="<fmt:message key="count.roles"/>"
                                   black-list-patterns="xml-meta-exists"/>

                            <input class="button" type="submit"
                                   value="<fmt:message key="role.count"/>"/>
                        </td>
                    </tr>

                    <%
                        Iterator it = roleCount.entrySet().iterator();
                        String key = null;
                        String value = null;
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            key = (String) pair.getKey();
                            value = (String) pair.getValue();
                    %>

                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><%=Encode.forHtml(key)%></td>
                        <td>
                            <%
                            if (StringUtils.isNumeric(value)) {
                            %>
                            <input type="text" readonly=true name="<%=UserAdminUIConstants.ROLE_COUNT%>"
                                   value="<%=Encode.forHtmlAttribute(value)%>" />
                            <%
                            } else {
                            %>
                            <p>Error occurred while getting the count</p>
                            <%
                            }
                            %>
                        </td>
                    </tr>

                    <%        it.remove();
                        }
                    %>


                    </tbody>
                    <%
                        }
                    %>
                </table>
            </form>

            <%}%>
            <p>&nbsp;</p>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="role-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <table class="styledLeft" id="roleTable">
                <%
                    if (ArrayUtils.isNotEmpty(roles)) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="name"/></th>
                        <%--<%if(hasMultipleUserStores){%>
                        <th><fmt:message key="domainName"/></th>
                        <%}
                        %>--%>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    }
                %>
                <tbody>
                <%
                    if (ArrayUtils.isNotEmpty(roles)) {
                        for (FlaggedName data : roles) {
                            if (data != null) {
                                if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(data.getItemName())) {
                                    continue;
                                }
                                if (userRealmInfo.getAdminRole().equals(data.getItemName()) &&
                                        !userRealmInfo.getAdminUser().equals(currentUser)) {
                                    continue;
                                }
                                String roleName = data.getItemName();
                                String displayName = data.getItemDisplayName();
                                if (displayName == null) {
                                    displayName = roleName;
                                }
                                if (workFlowAddPendingRolesList.contains(roleName)) {
                %>
                <tr>
                    <td><%=Encode.forHtmlContent(displayName)%>
                        <%if (!data.getEditable()) { %> <%="(Read-Only)"%> <% } %>
                        <img src="images/workflow_pending_add.gif" title="Workflow-pending-user-add"
                             alt="Workflow-pending-user-add" height="15" width="15">
                    </td>
                    <td>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/edit.gif);color:#CCC;"><fmt:message key="rename"/></a>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/edit.gif);color:#CCC;"><fmt:message
                                key="edit.permissions"/></a>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/edit.gif);color:#CCC;"><fmt:message key="edit.users"/></a>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/view.gif);color:#CCC;"><fmt:message key="view.users"/></a>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/delete.gif);color:#CCC;"><fmt:message key="delete"/></a>
                    </td>
                </tr>
                <%
                } else if (showDeletePendingRolesList.contains(roleName)) {
                %>
                   <%-- <%if(hasMultipleUserStores){%>
                    	<td>
                            <%if(data.getDomainName() != null){%>
                            <%data.getDomainName();%>
                            <%} %>
                        </td>
                    <%}%>--%>
                <tr>
                    <td><%=Encode.forHtmlContent(displayName)%>
                        <%if (!data.getEditable()) { %> <%="(Read-Only)"%> <% } %>
                        <img src="images/workflow_pending_remove.gif" title="Workflow-pending-user-delete"
                             alt="Workflow-pending-user-delete" height="15" width="15">
                    </td>
                    <td>
                        <%if (!data.getShared()) { %>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/edit.gif);color:#CCC;"><fmt:message key="rename"/></a>
                        <% if (!data.getItemName().equals(userRealmInfo.getAdminRole())) {%>
                        <a href="edit-permissions.jsp?roleName=<%=Encode.forUriComponent(roleName)%>" class="icon-link"
                           style="background-image:url(images/edit.gif);"><fmt:message key="edit.permissions"/></a>
                        <% }
                        }%>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/edit.gif);color:#CCC;"><fmt:message key="edit.users"/></a>

                        <% if (!userRealmInfo.getEveryOneRole().equals(data.getItemName())) { %>
                        <a href="view-users.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&<%=UserAdminUIConstants.ROLE_READ_ONLY%>=<%=!data.getEditable()%>"
                           class="icon-link" style="background-image:url(images/view.gif);"><fmt:message
                                key="view.users"/></a>
                        <% } %>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/delete.gif);color:#CCC;"><fmt:message key="delete"/></a>
                    </td>
                </tr>
                <%
                } else {
                %>
                <tr>
                    <td><%=Encode.forHtmlContent(displayName)%>
                        <%if (!data.getEditable()) { %> <%="(Read-Only)"%> <% } %>
                    </td>
                        <%-- <%if(hasMultipleUserStores){%>
                             <td>
                                 <%if(data.getDomainName() != null){%>
                                 <%data.getDomainName();%>
                                 <%} %>
                             </td>
                         <%}%>--%>
                    <td>
                        <%if (!data.getShared()) { %>
                        <% if (!data.getItemName().equals(userRealmInfo.getAdminRole()) &&
                                !data.getItemName().equals(userRealmInfo.getEveryOneRole()) && data.getEditable() &&
                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/rolemgt/update")) {%>

                        <a href="#" onclick="updateUserGroup('<%=Encode.forUriComponent(roleName)%>')"
                           class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                                key="rename"/></a>
                        <% } %>
                        <% if (!data.getItemName().equals(userRealmInfo.getAdminRole()) &&
                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/rolemgt/update")) {%>
                        <a href="edit-permissions.jsp?roleName=<%=Encode.forUriComponent(roleName)%>" class="icon-link"
                           style="background-image:url(images/edit.gif);"><fmt:message key="edit.permissions"/></a>
                        <% }
                        }%>

                        <% if (!userRealmInfo.getEveryOneRole().equals(data.getItemName()) && data.getEditable() &&
                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/usermgt/update"))
                        { %>
                        <a href="edit-users.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&<%=UserAdminUIConstants.ROLE_READ_ONLY%>=<%=!data.getEditable()%>"
                           class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                                key="edit.users"/></a>
                        <% } %>
                        <% if (!userRealmInfo.getEveryOneRole().equals(data.getItemName()) &&
                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/usermgt/view"))
                        { %>
                        <a href="view-users.jsp?roleName=<%=Encode.forUriComponent(roleName)%>&<%=UserAdminUIConstants.ROLE_READ_ONLY%>=<%=!data.getEditable()%>"
                           class="icon-link" style="background-image:url(images/view.gif);"><fmt:message
                                key="view.users"/></a>
                        <% } %>
                        <%if (!data.getShared()) { %>

                        <% if (!data.getItemName().equals(userRealmInfo.getAdminRole()) &&
                                !data.getItemName().equals(userRealmInfo.getEveryOneRole()) && data.getEditable() &&
                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/rolemgt/delete"))
                        {%>
                        <a href="#" onclick="deleteUserGroup('<%=Encode.forUriComponent(roleName)%>')"
                           class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                                key="delete"/></a>
                        <% }
                        } %>

                    </td>
                </tr>
                <%
                    }
                %>
                <%
                            }
                        }
                    }
                %>
                </tbody>
            </table>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="role-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <%
                if (ArrayUtils.isNotEmpty(roles) && exceededDomains != null) {
                    if (exceededDomains.getItemName() != null || exceededDomains.getItemDisplayName() != null) {
                        String message = null;
                        if (Boolean.parseBoolean(exceededDomains.getItemName())) {
                            if (StringUtils.isNotBlank(exceededDomains.getItemDisplayName())) {
                                String arg = "";
                                String[] domains = exceededDomains.getItemDisplayName().split(":");
                                for (int i = 0; i < domains.length; i++) {
                                    arg += "\'" + domains[i] + "\'";
                                    if (i < domains.length - 2) {
                                        arg += ", ";
                                    } else if (i == domains.length - 2) {
                                        arg += " and ";
                                    }
                                }
                                message = MessageFormat.format(resourceBundle.getString("more.roles.others"), arg);
                            } else {
                                message = resourceBundle.getString("more.roles.primary");
                            }
            %>
            <strong><%=Encode.forHtml(message)%>
            </strong>
            <%
            } else if (StringUtils.isNotBlank(exceededDomains.getItemDisplayName())) {
                String[] domains = exceededDomains.getItemDisplayName().split(":");
                String arg = "";
                for (int i = 0; i < domains.length; i++) {
                    arg += "\'" + domains[i] + "\'";
                    if (i < domains.length - 2) {
                        arg += ", ";
                    } else if (i == domains.length - 2) {
                        arg += " and ";
                    }
                }
                message = MessageFormat.format(resourceBundle.getString("more.roles"), arg);
            %>
            <strong><%=Encode.forHtml(message)%>
            </strong>
            <%
                        }
                    }
                }
            %>

        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('roleTable', 'tableEvenRow', 'tableOddRow');

        $(document).ready(function () {
            $('form[name=filterForm]').submit(function(){
                return doValidateForm(this, '<fmt:message key="error.input.validation.msg"/>');
            })
        });
    </script>
</fmt:bundle>