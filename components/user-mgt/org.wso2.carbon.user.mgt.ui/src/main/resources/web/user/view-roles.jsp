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
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
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

    boolean showFilterMessage = false;
    boolean newFilter = false;
    boolean doUserList = true;
    boolean showUpdate = true;
    FlaggedName exceededDomains = null;
    FlaggedName[] roles = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;
    Set<String> workFlowDeletePendingRoles = null;

    if (request.getParameter("pageNumber") == null) {
        session.removeAttribute("checkedRolesMap");
    }
    if (session.getAttribute("checkedRolesMap") == null) {
        session.setAttribute("checkedRolesMap", new HashMap<String, Boolean>());
    }

    session.removeAttribute("prevString");
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_VIEW_USER_FILTER);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE_EXCEEDED);

    // search filter
    String filter = request.getParameter(UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER);
    if (StringUtils.isBlank(filter)) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER);
        if (StringUtils.isBlank(filter)) {
            filter = "*";
        }
    } else {
        newFilter = true;
    }
    filter = filter.trim();
    session.setAttribute(UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER, filter);

    // check page number
    try {

        pageNumber = Integer.parseInt(request.getParameter("pageNumber"));

    } catch (NumberFormatException ignored) {
        // page number format exception
        pageNumber = 0;
    }

    flaggedNameMap = (Map<Integer, PaginatedNamesBean>) session.
            getAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE);
    if (flaggedNameMap != null) {
        PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
        if (bean != null) {
            roles = bean.getNames();
            if (ArrayUtils.isNotEmpty(roles)) {
                numberOfPages = bean.getNumberOfPages();
                doUserList = false;
            }
        }
    }

    String userName = request.getParameter("username");
    String displayName = request.getParameter("displayName");
    if (StringUtils.isBlank(displayName)) {
        displayName = (String) session.getAttribute(UserAdminUIConstants.USER_DISPLAY_NAME);
        if (StringUtils.isBlank(displayName)) {
            displayName = userName;
        }
    } else {
        session.setAttribute(UserAdminUIConstants.USER_DISPLAY_NAME, displayName);
    }
    UserRealmInfo userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE_EXCEEDED);

    if(doUserList || newFilter){
        try {

            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            UserManagementWorkflowServiceClient UserMgtClient = new
                    UserManagementWorkflowServiceClient(cookie, backendServerURL, configContext);

            if (filter.length() > 0 && userName != null) {
                FlaggedName[] data = client.getRolesOfUser(userName, filter, 0);
                if (CarbonUIUtil.isContextRegistered(config, "/usermgt-workflow/")) {
                    String[] DeletePendingRolesList = UserMgtClient.
                            listAllEntityNames("DELETE_ROLE", "PENDING", "ROLE", filter);
                    workFlowDeletePendingRoles = new LinkedHashSet<String>(Arrays.asList(DeletePendingRolesList));
                    String pendingStatus = "[Pending Role for Delete]";

                    if (data != null) {
                        for (int i = 0; i < data.length; i++) {
                            String updatedStatus = null;
                            if (workFlowDeletePendingRoles.contains(data[i].getItemName())) {
                                updatedStatus = data[i].getItemName() + " " + pendingStatus;
                                data[i].setItemDisplayName(data[i].getItemName());
                                data[i].setItemName(updatedStatus);
                            }
                        }
                    }
                }
                List<FlaggedName> dataList = new ArrayList<FlaggedName>(Arrays.asList(data));
                exceededDomains = dataList.remove(dataList.size() - 1);
                session.setAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE_EXCEEDED, exceededDomains);
                if (CollectionUtils.isNotEmpty(dataList)) {
                    flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                    int max = pageNumber + cachePages;
                    for (int i = (pageNumber - cachePages); i < max; i++) {
                        if (i < 0) {
                            max++;
                            continue;
                        }
                        PaginatedNamesBean bean = Util.
                                retrievePaginatedFlaggedName(i, dataList);
                        flaggedNameMap.put(i, bean);
                        if (bean.getNumberOfPages() == i + 1) {
                            break;
                        }
                    }
                    roles = flaggedNameMap.get(pageNumber).getNames();
                    numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                    session.setAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE, flaggedNameMap);
                } else {
                    session.removeAttribute(UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER);
                    showFilterMessage = true;
                }
            }

            if (userRealmInfo == null) {
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }
        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("error.while.loading.roles.of"),
                    userName, e.getMessage());
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
    }
    Util.updateCheckboxStateMap((Map<String,Boolean>)session.getAttribute("checkedRolesMap"),flaggedNameMap,
            request.getParameter("selectedRoles"),request.getParameter("unselectedRoles"),":");
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="users.in.the.role"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript">

        function doValidation() {
            // no worth of validation here.
            return true;
        }

        function enableCheckBoxes() {
            var formElems = document.getElementsByName('selectedRoles');
            for (var i = 0; i < formElems.length; i++) {
                formElems[i].disabled = false;
            }
            return true;
        }

        function doUpdate() {
            document.edit_users.submit();
        }

        function doCancel() {
            location.href = 'user-mgt.jsp?ordinal=1';
        }

        function doFinish() {

            document.edit_users.finish.value = 'true';
            document.edit_users.submit();

        }

        function doPaginate(page, pageNumberParameterName, pageNumber) {
            var form = document.createElement("form");
            form.id = "paginateForm";
            form.setAttribute("method", "POST");
            form.setAttribute("action", page + "?" + pageNumberParameterName + "=" + pageNumber + "&username=" + '<%=Encode.forJavaScript(Encode.forUriComponent(userName))%>');
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
            selectedRolesElem.setAttribute("name", "selectedRoles");
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
            unselectedRolesElem.setAttribute("name", "unselectedRoles");
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
            form.setAttribute("action", "view-roles.jsp?pageNumber=" + <%=pageNumber%> +"&username=" + '<%=Encode.forJavaScript(Encode.forUriComponent(userName))%>');
            var selectedRolesElem = document.createElement("input");
            selectedRolesElem.setAttribute("type", "hidden");
            selectedRolesElem.setAttribute("name", "selectedRoles");
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
            form.setAttribute("action", "view-roles.jsp?pageNumber=" + <%=pageNumber%> +"&username=" + '<%=Encode.forJavaScript(Encode.forUriComponent(userName))%>');
            var unselectedRolesElem = document.createElement("input");
            unselectedRolesElem.setAttribute("type", "hidden");
            unselectedRolesElem.setAttribute("name", "unselectedRoles");
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
        <h2><fmt:message key="roles.list.in.user"/> <%=Encode.forHtml(displayName)%>
        </h2>

        <script type="text/javascript">

            <%if(showFilterMessage == true){%>
            CARBON.showInfoDialog('<fmt:message key="no.roles.filtered"/>', null, null);
            <%}%>

        </script>
        <div id="workArea">
            <form name="filterForm" method="post"
                  action="view-roles.jsp?username=<%=Encode.forUriComponent(userName)%>">
                <table class="normal">
                    <tr>
                        <td><fmt:message key="list.roles"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(filter)%>" label="<fmt:message key="list.roles"/>"
                                   black-list-patterns="xml-meta-exists"/>
                        </td>
                        <td>
                            <input class="button" type="submit"
                                   value="<fmt:message key="user.search"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
            <p>&nbsp;</p>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              action="post"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="view-roles.jsp" pageNumberParameterName="pageNumber"
                              parameters="<%="username=" + Encode.forHtmlAttribute(userName)%>"/>
            <form method="post" action="edit-user-roles-finish-ajaxprocessor.jsp?viewRoles=true" onsubmit="return doValidation();"
                  name="edit_users" id="edit_users">
                <input type="hidden" id="username" name="username" value="<%=Encode.forHtmlAttribute(userName)%>"/>
                <input type="hidden" id="logout" name="logout" value="false"/>
                <input type="hidden" id="finish" name="finish" value="false"/>

                <div class="sectionHelp"><fmt:message key="role.view.help"/></div>
                <table class="styledLeft" id="table_users_list_of_role">
                    <thead>
                    <tr>
                        <th><fmt:message key="roles.in.the.user"/> <strong><%=Encode.forHtml(userName)%>
                        </strong></th>
                    </tr>
                    </thead>

                    <tbody>
                    <tr>
                        <td colspan="2" style="padding:0 !important;">
                            <%
                                if (ArrayUtils.isNotEmpty(roles)) {
                            %>
                            <div style="padding:5px 5px 10px 10px">
                                <!-- td><fmt:message key="users"/></td -->
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

                                <a style="cursor:pointer;color:#006699;" onclick="doSelectAll('selectedRoles');"/>
                                <fmt:message key="select.all.page"/> </a> |
                                <a style="cursor:pointer;color:#006699;" onclick="doUnSelectAll('selectedRoles');"/>
                                <fmt:message key="unselect.all.page"/> </a>
                                <%if (Integer.parseInt(fromPage) < Integer.parseInt(toPage)) {%>
                                | <a style="cursor:pointer;color:#006699;" onclick="doSelectAllRetrieved();"/>
                                <fmt:message key="select.all.page.from"/> <%=fromPage%> <fmt:message
                                    key="select.all.page.to"/> <%=toPage%></a> |
                                <a style="cursor:pointer;color:#006699;" onclick="doUnSelectAllRetrieved();"/>
                                <fmt:message key="unselect.all.page.from"/> <%=fromPage%> <fmt:message
                                    key="unselect.all.page.to"/> <%=toPage%></a>
                                <%}%>
                            </div>
                            <%
                                }
                            %>
                            <table class="odd-even-data-table normal" style="width:100%">
                                <%
                                    if (roles != null) {
                                        for (FlaggedName name : roles) {
                                            if (name != null) {
                                                String doCheck = "checked=\"checked\"";
                                                String doEdit = "";
                                                if (name.getItemName().equals(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME)) {
                                                    continue;
                                                } else if (userRealmInfo.getEveryOneRole().equals(name.getItemName())) {
                                                    doEdit = "disabled=\"disabled\"";
                                                } else if (!name.getEditable()) {
                                                    doEdit = "disabled=\"disabled\"";
                                                } else if (session.getAttribute("checkedRolesMap") != null &&
                                                        Boolean.FALSE.equals(((Map<String, Boolean>) session.getAttribute("checkedRolesMap")).get(name.getItemName()))) {
                                                    doCheck = "";
                                                }
                                %>
                                <tr>
                                    <td>
                                        <label>
                                            <input type="checkbox" name="selectedRoles"
                                                   value="<%=Encode.forHtmlAttribute(name.getItemName())%>" <%=doCheck%> <%=doEdit%> />
                                            <%
                                                if ((name.getItemName()).contains("[Pending Role for Delete]")) {
                                            %>
                                            <%=Encode.forHtml(name.getItemDisplayName())%>
                                            <img src="images/workflow_pending_remove.gif"
                                                 title="Workflow-pending-user-delete"
                                                 alt="Workflow-pending-user-delete" height="15" width="15">
                                            <%
                                            } else {
                                            %>
                                            <%=Encode.forHtml(name.getItemName())%>
                                            <%if (!name.getEditable()) { %> <%="(Read-Only)"%> <%
                                                }
                                            } %>
                                            <input type="hidden" name="shownRoles"
                                                   value="<%=Encode.forHtmlAttribute(name.getItemName())%>"/>
                                        </label>
                                    </td>
                                    <td>
                                        <% if (!userRealmInfo.getAdminRole().equals(name.getItemName()) &&
                                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/rolemgt/update")) {%>
                                        <a style="background-image:url(images/edit.gif);" class="icon-link"
                                           href="../role/edit-permissions.jsp?roleName=<%=Encode.forUriComponent(name.getItemName())%>&prevPage=view&prevUser=<%=Encode.forUriComponent(userName)%>&prevPageNumber=<%=pageNumber%>"><fmt:message
                                                key="edit.permissions"/></a>
                                        <%} %>
                                        <% if (!userRealmInfo.getEveryOneRole().equals(name.getItemName()) &&
                                                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/usermgt/view")) {%>
                                        <a style="background-image:url(images/view.gif);" class="icon-link"
                                           href="../role/view-users.jsp?roleName=<%=Encode.forUriComponent(name.getItemName())%>&prevPage=view&prevUser=<%=Encode.forUriComponent(userName)%>&prevPageNumber=<%=pageNumber%>&<%=UserAdminUIConstants.ROLE_READ_ONLY%>=<%if (!name.getEditable()) { %>true<% }else{ %>false<% } %>"><fmt:message
                                                key="view.users"/></a>
                                        <% } %>
                                    </td>
                                    <% } %>
                                </tr>
                                <%
                                        }
                                    }
                                %>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
                <carbon:paginator pageNumber="<%=pageNumber%>"
                                  action="post"
                                  numberOfPages="<%=numberOfPages%>"
                                  noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                                  page="view-roles.jsp" pageNumberParameterName="pageNumber"
                                  parameters="<%="username="+Encode.forHtmlAttribute(userName)%>"/>
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
                                    message = resourceBundle.getString("more.roles.others").replace("{0}", arg);
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
                    message = resourceBundle.getString("more.roles").replace("{0}", arg);
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
                        <% if (!showFilterMessage && CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/manage/identity/rolemgt/update")) { %>
                        <input class="button" type="button" value="<fmt:message key="update"/>"
                               onclick="enableCheckBoxes();doUpdate()"/>
                        <input class="button" type="button" value="<fmt:message key="finish"/>"
                               onclick="enableCheckBoxes();doFinish()"/>
                        <%} %>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>"
                               onclick="doCancel()"/>
                    </td>
                </tr>
        </form>
    </div>
</div>
</fmt:bundle>