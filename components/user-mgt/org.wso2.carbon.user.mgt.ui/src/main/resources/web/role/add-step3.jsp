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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.PaginatedNamesBean" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.Util" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:useBean id="roleBean" type="org.wso2.carbon.user.mgt.ui.RoleBean" scope="session"/>
<jsp:setProperty name="roleBean" property="*"/>


<%
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    boolean showFilterMessage = false;
    boolean newFilter = false;
    boolean doUserList = true;
    List<FlaggedName> dataList = null;
    FlaggedName exceededDomains = null;
    FlaggedName[] users = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    String roleType = null;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;

    if (request.getParameter("pageNumber") == null) {
        session.removeAttribute("checkedUsersMap");
    }
    if (session.getAttribute("checkedUsersMap") == null) {
        session.setAttribute("checkedUsersMap", new HashMap<String, Boolean>());
    }

    // search filter
    String filter = request.getParameter(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER);
    if (filter == null || filter.trim().length() == 0) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    } else {
        newFilter = true;
    }
    filter = filter.trim();
    session.setAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER, filter);

    // check page number
    String pageNumberStr = request.getParameter("pageNumber");
    if (pageNumberStr == null) {
        pageNumberStr = "0";
    }

    try {
        pageNumber = Integer.parseInt(pageNumberStr);
    } catch (NumberFormatException ignored) {
        // page number format exception
    }

    flaggedNameMap = (Map<Integer, PaginatedNamesBean>) session.getAttribute(
            UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE);
    if (flaggedNameMap != null) {
        PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
        if (bean != null) {
            users = bean.getNames();
            if (users != null && users.length > 0) {
                numberOfPages = bean.getNumberOfPages();
                doUserList = false;
            }
        }
    }

    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE_EXCEEDED);
    String roleName = roleBean.getRoleName();

    if (doUserList || newFilter) {
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext()
                                                 .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            roleType = roleBean.getRoleType();

            if (filter.length() > 0) {
                FlaggedName[] datas;
                if (UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType)) {
                    datas = client.listAllUsers(filter, -1);
                } else {
                    datas = client.getUsersOfRole(roleName, filter, -1);
                }
                dataList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                exceededDomains = dataList.remove(dataList.size() - 1);
                session.setAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE_EXCEEDED, exceededDomains);
                if (dataList != null && dataList.size() > 0) {
                    flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                    int max = pageNumber + cachePages;
                    for (int i = (pageNumber - cachePages); i < max; i++) {
                        if (i < 0) {
                            max++;
                            continue;
                        }
                        PaginatedNamesBean bean = Util.retrievePaginatedFlaggedName(i, dataList);
                        flaggedNameMap.put(i, bean);
                        if (bean.getNumberOfPages() == i + 1) {
                            break;
                        }
                    }
                    users = flaggedNameMap.get(pageNumber).getNames();
                    numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                    session.setAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE, flaggedNameMap);
                } else {
                    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER);
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
            location.href = "add-step1.jsp";
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
    <carbon:breadcrumb label="add.users"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function doCancel() {
            location.href = 'role-mgt.jsp?ordinal=1';
        }

        function doPaginate(page, pageNumberParameterName, pageNumber) {
            var form = document.createElement("form");
            form.id = "paginateForm";
            form.setAttribute("method", "POST");
            form.setAttribute("action", page + "?" + pageNumberParameterName + "=" + pageNumber + "&roleName=" + '<%=Encode.forJavaScript(Encode.forUriComponent(roleName))%>');
            var selectedUsersStr = "";
            $("input[type='checkbox']:checked").each(function (index) {
                if (!$(this).is(":disabled")) {
                    selectedUsersStr += $(this).val();
                    if (index != $("input[type='checkbox']:checked").length - 1) {
                        selectedUsersStr += ":";
                    }
                }
            });
            var selectedUsersElem = document.createElement("input");
            selectedUsersElem.setAttribute("type", "hidden");
            selectedUsersElem.setAttribute("name", "selectedUsers");
            selectedUsersElem.setAttribute("value", selectedUsersStr);
            form.appendChild(selectedUsersElem);
            var unselectedUsersStr = "";
            $("input[type='checkbox']:not(:checked)").each(function (index) {
                if (!$(this).is(":disabled")) {
                    unselectedUsersStr += $(this).val();
                    if (index != $("input[type='checkbox']:not(:checked)").length - 1) {
                        unselectedUsersStr += ":";
                    }
                }
            });
            var unselectedUsersElem = document.createElement("input");
            unselectedUsersElem.setAttribute("type", "hidden");
            unselectedUsersElem.setAttribute("name", "unselectedUsers");
            unselectedUsersElem.setAttribute("value", unselectedUsersStr);
            form.appendChild(unselectedUsersElem);
            document.body.appendChild(form);
            $("#paginateForm").submit();
        }
    </script>


    <div id="middle">
        <%if (UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType)) {%>
        <h2><fmt:message key="add.internal.user.role"/></h2>
        <%} else { %>
        <h2><fmt:message key="add.user.role"/></h2>
        <%} %>

        <script type="text/javascript">

            <% if(showFilterMessage == true){ %>
            CARBON.showInfoDialog('<fmt:message key="no.users.filtered"/>', null, null);
            <% } %>

        </script>
        <div id="workArea">
            <h3><fmt:message key="step.3.role"/></h3>

            <form name="filterForm" method="post" action="add-step3.jsp?roleName=<%=Encode.forUriComponent(roleName)%>">
                <table class="normal">
                    <tr>
                        <td><fmt:message key="list.users"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(filter)%>"
                                   label="<fmt:message key="list.users"/>" black-list-patterns="xml-meta-exists"/>
                        </td>
                        <td>
                            <input class="button" type="submit" value="<fmt:message key="user.search"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
            <form method="get" name="dataForm" action="add-finish.jsp">

                <carbon:paginator pageNumber="<%=pageNumber%>"
                                  action="post"
                                  numberOfPages="<%=numberOfPages%>"
                                  noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                                  page="add-step3.jsp" pageNumberParameterName="pageNumber"
                                  parameters="<%="roleName=" + Encode.forHtmlAttribute(roleName)%>"/>


                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="add.users"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <% if (users != null && users.length > 1) { %>
                                <tr>
                                    <td>

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

                                        <a href="#" onclick="doSelectAll('roleUsers');"/><fmt:message
                                            key="select.all.page"/> </a> |
                                        <a href="#" onclick="doUnSelectAll('roleUsers');"/><fmt:message
                                            key="unselect.all.page"/> </a>
                                        <%if (Integer.parseInt(fromPage) < Integer.parseInt(toPage)) {%>
                                        | <a href="#" onclick="doSelectAllRetrieved();"/><fmt:message
                                            key="select.all.page.from"/> <%=fromPage%> <%
                                        if (Integer.parseInt(fromPage) < Integer.parseInt(toPage)) {%><fmt:message
                                            key="select.all.page.to"/> <%=toPage%>
                                                <%}%></a> |
                                        <a href="#" onclick="doUnSelectAllRetrieved();"/><fmt:message
                                            key="unselect.all.page.from"/> <%=fromPage%> <%
                                        if (Integer.parseInt(fromPage) < Integer.parseInt(toPage)) {%><fmt:message
                                            key="unselect.all.page.to"/> <%=toPage%>
                                                <%}%></a>
                                        <% } %>
                                    </td>
                                </tr>
                                <% } %>
                                <%
                                    if (users != null) {
                                        for (FlaggedName user : users) {
                                            if (user != null) {
                                                String userName = user.getItemName();
                                                String displayName = user.getItemDisplayName();
                                                if (displayName == null || displayName.trim().length() == 0) {
                                                    displayName = userName;
                                                }

                                                String doCheck = "";
                                                String doEdit = "";
                                                if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                                                    continue;
                                                } else if (!UserAdminUIConstants.INTERNAL_ROLE.equalsIgnoreCase(roleType) &&
                                                           !user.getEditable()) {
                                                    doEdit = "disabled=\"disabled\"";
                                                } else if (session.getAttribute("checkedUsersMap") != null &&
                                                           ((Map<String, Boolean>) session
                                                                   .getAttribute("checkedUsersMap")).get(userName) !=
                                                           null &&
                                                           ((Map<String, Boolean>) session
                                                                   .getAttribute("checkedUsersMap")).get(userName) ==
                                                           true) {
                                                    doCheck = "checked=\"checked\"";
                                                }

                                %>
                                <tr>
                                    <td>
                                        <input type="checkbox" name="roleUsers"
                                               value="<%=Encode.forHtmlAttribute(userName)%>" <%=doEdit%>
                                                <%=doCheck%>/><%=Encode.forHtml(displayName)%>
                                        <%if (!user.getEditable()) { %> <%="(Read-Only)"%> <% } %>
                                    </td>

                                </tr>
                                <%
                                            }
                                        }
                                    }
                                %>
                            </table>
                        </td>
                    </tr>
                </table>
                <carbon:paginator pageNumber="<%=pageNumber%>"
                                  action="post"
                                  numberOfPages="<%=numberOfPages%>"
                                  noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                                  page="add-step3.jsp" pageNumberParameterName="pageNumber"
                                  parameters="<%="roleName=" + Encode.forHtmlAttribute(roleName)%>"/>

                <%
                    if (users != null) {
                        if (users.length > 0) {
                            if (exceededDomains.getItemName() != null || exceededDomains.getItemDisplayName() != null) {
                                String message = null;
                                if (exceededDomains.getItemName() != null &&
                                    exceededDomains.getItemName().equals("true")) {
                                    if (exceededDomains.getItemDisplayName() != null &&
                                        !exceededDomains.getItemDisplayName().equals("")) {
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
                                        message = resourceBundle.getString("more.users.others").replace("{0}", arg);
                                    } else {
                                        message = resourceBundle.getString("more.users.primary");
                                    }
                %>
                <strong><%=Encode.forHtml(message)%>
                </strong>
                <%
                } else if (exceededDomains.getItemDisplayName() != null &&
                           !exceededDomains.getItemDisplayName().equals("")) {
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
                    }
                %>

                <tr>
                    <td class="buttonRow">
                        <input type="submit" class="button" value="<fmt:message key="finish"/>"/>
                        <input type="button" class="button" value="<fmt:message key="cancel"/>" onclick="doCancel();"/>
                    </td>
                </tr>
            </form>
        </div>
    </div>

<script type="text/javascript">

    function doSelectAllRetrieved() {
        var form = document.createElement("form");
        form.id = "selectAllRetrievedForm";
        form.setAttribute("method", "POST");
        form.setAttribute("action", "add-step3.jsp?pageNumber=" + <%=pageNumber%> +"&roleName=" + '<%=Encode.forJavaScript(Encode.forUriComponent(roleName))%>');
        var selectedRolesElem = document.createElement("input");
        selectedRolesElem.setAttribute("type", "hidden");
        selectedRolesElem.setAttribute("name", "selectedUsers");
        selectedRolesElem.setAttribute("value", "ALL");
        form.appendChild(selectedRolesElem);
        document.body.appendChild(form);
        $("#selectAllRetrievedForm").submit();

    }

    function doUnSelectAllRetrieved() {
        var form = document.createElement("form");
        form.id = "unSelectAllRetrievedForm";
        form.setAttribute("method", "POST");
        form.setAttribute("action", "add-step3.jsp?pageNumber=" + <%=pageNumber%> +"&roleName=" + '<%=Encode.forJavaScript(Encode.forUriComponent(roleName))%>');
        var unselectedRolesElem = document.createElement("input");
        unselectedRolesElem.setAttribute("type", "hidden");
        unselectedRolesElem.setAttribute("name", "unselectedUsers");
        unselectedRolesElem.setAttribute("value", "ALL");
        form.appendChild(unselectedRolesElem);
        document.body.appendChild(form);
        $("#unSelectAllRetrievedForm").submit();
    }

    $(document).ready(function () {
        $('form[name=filterForm]').submit(function(){
            return doValidateForm(this, '<fmt:message key="error.input.validation.msg"/>');
        })
    });

</script>

</fmt:bundle>
