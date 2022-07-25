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
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ page session="true" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.PaginatedNamesBean" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserManagementWorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../userstore/display-messages.jsp"/>

<%
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    boolean newFilter = false;
    boolean doUserList = true;
    boolean readOnlyRole = false;
    boolean showFilterMessage = false;
    boolean showAllAssignMessage = false;
    FlaggedName exceededDomains = null;
    FlaggedName[] users = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;
    Set<String> workFlowDeletePendingUsers = null;

    if (request.getParameter("pageNumber") == null) {
        session.removeAttribute("checkedUsersMap");
    }
    if (session.getAttribute("checkedUsersMap") == null) {
        session.setAttribute("checkedUsersMap", new HashMap<String, Boolean>());
    }

    // search filter
    String filter = request.getParameter(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER);
    if (StringUtils.isEmpty(filter)) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER);
        if (StringUtils.isEmpty(filter)) {
            filter = "*";
        }
    } else {
        newFilter = true;
    }
    filter = filter.trim();
    session.setAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER, filter);

    String roleName = request.getParameter("roleName");

    String readOnlyRoleString = request.getParameter(UserAdminUIConstants.ROLE_READ_ONLY);
    if (readOnlyRoleString == null) {
        readOnlyRoleString = (String) session.getAttribute(UserAdminUIConstants.ROLE_READ_ONLY);
    }
    if (Boolean.parseBoolean(readOnlyRoleString)) {
        readOnlyRole = true;
    }

    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED);

    // check page number
    try {

        pageNumber = Integer.parseInt(request.getParameter("pageNumber"));

    } catch (NumberFormatException ignored) {
        // page number format exception
        pageNumber = 0;
    }

    String prevRole = (String) session.getAttribute("previousRole");

    boolean useCache = false;

    if (StringUtils.equals(roleName, prevRole)) {
        useCache = true;
    } else if (prevRole != null) {
        session.setAttribute("previousRole", roleName);
    }
    if (useCache) {
        flaggedNameMap = (Map<Integer, PaginatedNamesBean>) session.
                getAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE);
        if (flaggedNameMap != null) {
            PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
            if (bean != null) {
                users = bean.getNames();
                if (ArrayUtils.isNotEmpty(users)) {
                    numberOfPages = bean.getNumberOfPages();
                    doUserList = false;
                }
            }
        }
    }
    if (doUserList || newFilter) {
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            UserManagementWorkflowServiceClient UserMgtClient = new
                    UserManagementWorkflowServiceClient(cookie, backendServerURL, configContext);
            if (filter.length() > 0) {
                FlaggedName[] data = client.getUsersOfRole(roleName, filter, -1);
                if (CarbonUIUtil.isContextRegistered(config, "/usermgt-workflow/")) {
                    String[] DeletePendingRolesList = UserMgtClient.
                            listAllEntityNames("DELETE_USER", "PENDING", "USER", filter);
                    workFlowDeletePendingUsers = new LinkedHashSet<String>(Arrays.asList(DeletePendingRolesList));
                    String pendingStatus = "[Pending User for Delete]";
                    if (data != null) {
                        for (int i = 0; i < data.length; i++) {
                            String updatedStatus = null;
                            if (workFlowDeletePendingUsers.contains(data[i].getItemName())) {
                                updatedStatus = data[i].getItemName() + " " + pendingStatus;
                                data[i].setItemDisplayName(data[i].getItemName());
                                data[i].setItemName(updatedStatus);
                            }
                        }
                    }
                }
                List<FlaggedName> datasList = new ArrayList<FlaggedName>(Arrays.asList(data));
                exceededDomains = datasList.remove(datasList.size() - 1);
                session.setAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED, exceededDomains);
                if (datasList != null) {
                    List<FlaggedName> nameList = new ArrayList<FlaggedName>();
                    for (FlaggedName value : datasList) {
                        if (!value.getSelected()) {
                            nameList.add(value);
                        }
                    }
                    datasList = nameList;
                }

                if (CollectionUtils.isNotEmpty(datasList)) {
                    flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                    int max = pageNumber + cachePages;
                    for (int i = (pageNumber - cachePages); i < max; i++) {
                        if (i < 0) {
                            max++;
                            continue;
                        }
                        PaginatedNamesBean bean = Util.
                                retrievePaginatedFlaggedName(i, datasList);
                        flaggedNameMap.put(i, bean);
                        if (bean.getNumberOfPages() == i + 1) {
                            break;
                        }
                    }
                    users = flaggedNameMap.get(pageNumber).getNames();
                    numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                    session.setAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE, flaggedNameMap);
                } else {
                    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER);
                    showFilterMessage = true;
                }
            }
        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("error.while.loading.users"),
                    e.getMessage());
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>', function () {
            location.href = "role-mgt.jsp";
        });
    });
</script>
<%
        }
    }
    Util.updateCheckboxStateMap((Map<String, Boolean>) session.getAttribute("checkedUsersMap"), flaggedNameMap,
                                request.getParameter("selectedUsers"), request.getParameter("unselectedUsers"), ":");
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="users.in.the.role"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript">

        function doValidation() {
            return true;
        }

        function doUpdate() {
            document.edit_users.submit();
        }

        function doCancel() {
            location.href = 'role-mgt.jsp?ordinal=1';
        }

        function doFinish() {

            document.edit_users.finish.value = 'true';
            document.edit_users.submit();
        }

        function doPaginate(page, pageNumberParameterName, pageNumber) {
            var form = document.createElement("form");
            form.id = "paginateForm";
            form.setAttribute("method", "POST");
            form.setAttribute("action", page + "?" + pageNumberParameterName + "=" + pageNumber + "&roleName=" + '<%=Encode.forJavaScript(Encode.forUriComponent(roleName))%>');
            var selectedRolesStr = "";
            $("input[type='checkbox']:checked").each(function (index) {
                if (!$(this).is(":disabled")) {
                    selectedRolesStr += $(this).val();
                    if (index != $("input[type='checkbox']:checked").length - 1) {
                        selectedRolesStr += ":";
                    }
                }
            });
            var selectedRolesElem = document.createElement("input");
            selectedRolesElem.setAttribute("type", "hidden");
            selectedRolesElem.setAttribute("name", "selectedUsers");
            selectedRolesElem.setAttribute("value", selectedRolesStr);
            form.appendChild(selectedRolesElem);
            var unselectedRolesStr = "";
            $("input[type='checkbox']:not(:checked)").each(function (index) {
                if (!$(this).is(":disabled")) {
                    unselectedRolesStr += $(this).val();
                    if (index != $("input[type='checkbox']:not(:checked)").length - 1) {
                        unselectedRolesStr += ":";
                    }
                }
            });
            var unselectedRolesElem = document.createElement("input");
            unselectedRolesElem.setAttribute("type", "hidden");
            unselectedRolesElem.setAttribute("name", "unselectedUsers");
            unselectedRolesElem.setAttribute("value", unselectedRolesStr);
            form.appendChild(unselectedRolesElem);
            var CSRFTokenElem = document.createElement("input");
            CSRFTokenElem.setAttribute("type", "hidden");
            CSRFTokenElem.setAttribute("name", "<csrf:tokenname/>");
            CSRFTokenElem.setAttribute("value", "<csrf:tokenvalue/>");
            form.appendChild(CSRFTokenElem);
            document.body.appendChild(form);
            $("#paginateForm").submit();
        }

        function doSelectAllRetrieved() {
            var form = document.createElement("form");
            form.id = "selectAllRetrievedForm";
            form.setAttribute("method", "POST");
            form.setAttribute("action", "edit-users.jsp?pageNumber=" + <%=pageNumber%> +"&roleName=" + '<%=Encode.forJavaScript(Encode.forUriComponent(roleName))%>');
            var selectedRolesElem = document.createElement("input");
            selectedRolesElem.setAttribute("type", "hidden");
            selectedRolesElem.setAttribute("name", "selectedUsers");
            selectedRolesElem.setAttribute("value", "ALL");
            form.appendChild(selectedRolesElem);
            var CSRFTokenElem = document.createElement("input");
            CSRFTokenElem.setAttribute("type", "hidden");
            CSRFTokenElem.setAttribute("name", "<csrf:tokenname/>");
            CSRFTokenElem.setAttribute("value", "<csrf:tokenvalue/>");
            form.appendChild(CSRFTokenElem);
            document.body.appendChild(form);
            $("#selectAllRetrievedForm").submit();
        }

        function doUnSelectAllRetrieved() {
            var form = document.createElement("form");
            form.id = "unSelectAllRetrievedForm";
            form.setAttribute("method", "POST");
            form.setAttribute("action", "edit-users.jsp?pageNumber=" + <%=pageNumber%> +"&roleName=" + '<%=Encode.forJavaScript(Encode.forUriComponent(roleName))%>');
            var unselectedRolesElem = document.createElement("input");
            unselectedRolesElem.setAttribute("type", "hidden");
            unselectedRolesElem.setAttribute("name", "unselectedUsers");
            unselectedRolesElem.setAttribute("value", "ALL");
            form.appendChild(unselectedRolesElem);
            var CSRFTokenElem = document.createElement("input");
            CSRFTokenElem.setAttribute("type", "hidden");
            CSRFTokenElem.setAttribute("name", "<csrf:tokenname/>");
            CSRFTokenElem.setAttribute("value", "<csrf:tokenvalue/>");
            form.appendChild(CSRFTokenElem);
            document.body.appendChild(form);
            $("#unSelectAllRetrievedForm").submit();
        }

        $(document).ready(function () {
            $('form[name=filterForm]').submit(function(){
                return doValidateForm(this, '<fmt:message key="error.input.validation.msg"/>');
            })
        });

    </script>


    <div id="middle">
        <h2><fmt:message key="users.list.in.role"/> <%=Encode.forHtmlContent(roleName)%>
        </h2>

        <script type="text/javascript">

            <%if(showFilterMessage == true){%>
            CARBON.showInfoDialog('<fmt:message key="no.users.filtered"/>', null, null);
            <%}%>

            <%if(showAllAssignMessage == true){%>
            CARBON.showInfoDialog('<fmt:message key="all.users.assigned"/>', null, null);
            <%}%>

        </script>
        <div id="workArea">
            <form name="filterForm" method="post" action="edit-users.jsp">
                <input type="hidden" name="roleName" value="<%=Encode.forHtmlAttribute(roleName)%>">
                <table class="normal">
                    <tr>
                        <td><fmt:message key="list.users"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(filter)%>" label="<fmt:message key="list.users"/>"
                                   autocomplete="off" black-list-patterns="xml-meta-exists"/>
                        </td>
                        <td>
                            <input class="button" type="submit"
                                   value="<fmt:message key="user.search"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
            <p>&nbsp;</p>

            <form method="post" action="edit-users-finish-ajaxprocessor.jsp" onsubmit="return doValidation();"
                  name="edit_users" id="edit_users">
                <input type="hidden" id="roleName" name="roleName" value="<%=Encode.forHtmlAttribute(roleName)%>"/>
                <input type="hidden" id="logout" name="logout" value="false"/>
                <input type="hidden" id="finish" name="finish" value="false"/>
                <input type="hidden" name="pageNumber" value="<%=pageNumber%>"/>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="unassigned.users"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td style="padding:0 !important">
                            <carbon:paginator pageNumber="<%=pageNumber%>"
                                              action="post"
                                              numberOfPages="<%=numberOfPages%>"
                                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                                              page="edit-users.jsp" pageNumberParameterName="pageNumber"
                                              parameters="<%="roleName=" + Encode.forHtmlAttribute(roleName)%>"/>
                            <table class="normal">
                                <%
                                    if (!ArrayUtils.isEmpty(users)) {
                                %>
                                <tr>
                                    <!-- td><fmt:message key="users"/></td -->
                                    <td colspan="4">

                                        <%
                                            String fromPage = "1";
                                            String toPage = String.valueOf(numberOfPages);
                                            if (pageNumber - cachePages >= 0) {
                                                fromPage = String.valueOf(pageNumber + 1 - cachePages);
                                            }
                                            if (pageNumber + cachePages <= numberOfPages - 1) {
                                                toPage = String.valueOf(pageNumber + 1 + cachePages);
                                            }
                                        %>

                                        <a href="#" onclick="doSelectAll('selectedUsers');"/>
                                        <fmt:message key="select.all.page"/> </a> |
                                        <a href="#" onclick="doUnSelectAll('selectedUsers');"/>
                                        <fmt:message key="unselect.all.page"/> </a>
                                        <%if (Integer.parseInt(fromPage) < Integer.parseInt(toPage)) {%>
                                        | <a href="#" onclick="doSelectAllRetrieved();"/>
                                        <fmt:message key="select.all.page.from"/> <%=fromPage%> <fmt:message
                                            key="select.all.page.to"/> <%=toPage%></a> |
                                        <a href="#" onclick="doUnSelectAllRetrieved();"/>
                                        <fmt:message key="unselect.all.page.from"/> <%=fromPage%> <fmt:message
                                            key="unselect.all.page.to"/> <%=toPage%></a>
                                        <%}%>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                <tr>
                                    <td colspan="2">
                                        <%
                                            if (users != null) {
                                                for (int i = 0; i < users.length; i++) {
                                                    if (users[i] != null) {
                                                        String doEdit = "";
                                                        String doCheck = "";
                                                        String userName = users[i].getItemName();
                                                        String displayName = users[i].getItemDisplayName();
                                                        if (displayName == null || displayName.trim().length() == 0) {
                                                            displayName = userName;
                                                        }
                                                        if (users[i].getItemName()
                                                                .equals(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME)) {
                                                            continue;
                                                        } else if (readOnlyRole && !users[i].getEditable()) {
                                                            doEdit = "disabled=\"disabled\"";
                                                        } else if (session.getAttribute("checkedUsersMap") != null &&
                                                                Boolean.TRUE.equals(((Map<String, Boolean>) session.getAttribute("checkedUsersMap")).get(users[i].getItemName()))) {
                                                            doCheck = "checked=\"checked\"";
                                                        }
                                        %>
                                        <input type="checkbox" name="selectedUsers"
                                               value="<%=Encode.forHtmlAttribute(userName)%>" <%=doEdit%> <%=doCheck%>/>
                                        <%
                                            if (userName.contains("[Pending User for Delete]")) {
                                        %>
                                        <%=Encode.forHtml(users[i].getItemDisplayName())%>
                                        <img src="images/workflow_pending_remove.gif"
                                             title="Workflow-pending-user-delete"
                                             alt="Workflow-pending-user-delete" height="15" width="15">
                                        <%
                                        } else {
                                        %>
                                        <%=Encode.forHtml(displayName)%>
                                        <%
                                            }
                                        %>
                                        <input type="hidden" name="shownUsers"
                                               value="<%=Encode.forHtmlAttribute(userName)%>"/><br/>
                                        <%
                                                    }
                                                }
                                            } else {
                                        %>
                                            <div> <fmt:message key="all.users.assigned"/> </div>
                                        <%
                                            }
                                        %>
                                    </td>
                                </tr>
                            </table>

                        </td>
                    </tr>
                </table>

                <carbon:paginator pageNumber="<%=pageNumber%>"
                                  action="post"
                                  numberOfPages="<%=numberOfPages%>"
                                  noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                                  page="edit-users.jsp" pageNumberParameterName="pageNumber"
                                  parameters="<%="roleName=" + Encode.forHtmlAttribute(roleName)%>"/>
                <%
                    if (ArrayUtils.isNotEmpty(users) && exceededDomains != null) {
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
                    message = resourceBundle.getString("more.users").replace("{0}", arg);
                %>
                <strong><%=Encode.forHtml(message)%>
                </strong>
                <%
                            }
                        }
                    }
                %>

                <tr>
                    <td class="buttonRow">
                        <% if (!showFilterMessage) {%>
                        <input class="button" type="button" value="<fmt:message key="update"/>"
                               onclick="doUpdate()"/>
                        <input class="button" type="button" value="<fmt:message key="finish"/>"
                               onclick="doFinish()"/>
                        <% } %>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>"
                               onclick="doCancel()"/>
                    </td>
                </tr>
            </form>
        </div>
    </div>
</fmt:bundle>
