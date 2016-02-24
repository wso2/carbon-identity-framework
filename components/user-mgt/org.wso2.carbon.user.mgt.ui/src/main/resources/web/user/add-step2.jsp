<%--
  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

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
<%@ page session="true" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
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
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../userstore/display-messages.jsp"/>

<jsp:useBean id="userBean" type="org.wso2.carbon.user.mgt.ui.UserBean"
             class="org.wso2.carbon.user.mgt.ui.UserBean" scope="session"/>
<jsp:setProperty name="userBean" property="*"/>

<%
    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    boolean showFilterMessage = false;
    boolean newFilter = false;
    boolean doUserList = true;
    String currentUser = (String) session.getAttribute("logged-user");
    List<FlaggedName> dataList = null;
    FlaggedName exceededDomains = null;
    FlaggedName[] roles = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;

    if (request.getParameter("pageNumber") == null) {
        session.removeAttribute("checkedRolesMap");
    }
    if (session.getAttribute("checkedRolesMap") == null) {
        session.setAttribute("checkedRolesMap", new HashMap<String, Boolean>());
    }

    // search filter
    String filter = request.getParameter(UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER);
    if (filter == null || filter.trim().length() == 0) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    } else {
        newFilter = true;
    }
    filter = filter.trim();
    session.setAttribute(UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER, filter);

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
            UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE);
    if (flaggedNameMap != null) {
        PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
        if (bean != null) {
            roles = bean.getNames();
            if (roles != null && roles.length > 0) {
                numberOfPages = bean.getNumberOfPages();
                doUserList = false;
            }
        }
    }

    UserRealmInfo userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE_EXCEEDED);
    String userName = userBean.getUsername();

    if (doUserList || newFilter) {
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext()
                                                 .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            if (filter.length() > 0) {
                FlaggedName[] data = client.getRolesOfUser(userName, filter, -1);
                dataList = new ArrayList<FlaggedName>(Arrays.asList(data));
                exceededDomains = dataList.remove(dataList.size() - 1);
                session.setAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE_EXCEEDED, exceededDomains);
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
                    roles = flaggedNameMap.get(pageNumber).getNames();
                    numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                    session.setAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE, flaggedNameMap);
                } else {
                    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER);
                    showFilterMessage = true;
                }
            }

            if (userRealmInfo == null) {
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }
        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("error.while.loading.roles"),
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

    Util.updateCheckboxStateMap((Map<String, Boolean>) session.getAttribute("checkedRolesMap"), flaggedNameMap,
                                request.getParameter("selectedRoles"), request.getParameter("unselectedRoles"), ":");
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="users.in.the.role"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">
        function doValidation() {
            return true;
        }


        function doCancel() {
            location.href = 'user-mgt.jsp?ordinal=1';
        }

        function doPaginate(page, pageNumberParameterName, pageNumber) {
            var form = document.createElement("form");
            form.id = "paginateForm";
            form.setAttribute("method", "POST");
            form.setAttribute("action", page + "?" + pageNumberParameterName + "=" + pageNumber + "&username=" + '<%=Encode.forJavaScriptBlock(Encode.forUriComponent(userName))%>');
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
            document.body.appendChild(form);
            $("#paginateForm").submit();
        }

    </script>


    <div id="middle">
        <h2><fmt:message key="add.user"/></h2>

        <script type="text/javascript">

            <%if(showFilterMessage == true){%>
            CARBON.showInfoDialog('<fmt:message key="no.roles.filtered"/>', null, null);
            <%}%>

        </script>
        <div id="workArea">
            <h3><fmt:message key="step.2.user"/></h3>

            <form name="filterForm" method="post" action="add-step2.jsp?username=<%=Encode.forUriComponent(userName)%>">
                <table class="normal" style="width:100%">
                    <tr>
                        <td style="white-space:nowrap" class="leftCol-med"><fmt:message key="list.roles"/></td>
                        <td class="leftCol-small">
                            <input type="text" name="<%=UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(filter)%>" label="<fmt:message key="list.roles"/>"
                                   black-list-patterns="xml-meta-exists"/>
                        </td>
                        <td style="text-align:left;">
                            <input class="button" type="submit" value="<fmt:message key="user.search"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
            <p>&nbsp;</p>

            <carbon:paginator pageNumber="<%=pageNumber%>"
                              action="post"
                              numberOfPages="<%=numberOfPages%>"
                              noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                              page="add-step2.jsp" pageNumberParameterName="pageNumber"
                              parameters="<%="username=" + Encode.forHtmlAttribute(userName)%>"/>

            <form method="post" action="add-finish.jsp" onsubmit="return doValidation();" name="edit_users"
                  id="edit_users">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="users.in.the.role"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal" style="width:100%">
                                <%
                                    if (roles != null && roles.length > 0) {
                                %>
                                <tr>
                                    <!-- td><fmt:message key="users"/></td -->
                                    <td colspan="4" style="white-space:nowrap">

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

                                        <a href="#" onclick="doSelectAll('userRoles');"/><fmt:message
                                            key="select.all.page"/> </a> |
                                        <a href="#" onclick="doUnSelectAll('userRoles');"/><fmt:message
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
                                        <% }%>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                <tr>
                                    <td colspan="2">
                                        <%
                                            if (roles != null) {
                                                for (FlaggedName name : roles) {
                                                    if (name != null) {
                                                        String doCheck = "";
                                                        String doEdit = "";
                                                        if (name.getItemName()
                                                                .equals(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME) ||
                                                                (!currentUser.equals(userRealmInfo.getAdminUser()) && name
                                                                        .getItemName().equals(userRealmInfo.getAdminRole()))) {
                                                            continue;
                                                        } else if (userRealmInfo.getEveryOneRole()
                                                                                .equals(name.getItemName())) {
                                                            doEdit = "disabled=\"disabled\"";
                                                            doCheck = "checked=\"checked\"";
                                                        } else if (!name.getEditable()) {
                                                            doEdit = "disabled=\"disabled\"";
                                                        } else if (session.getAttribute("checkedRolesMap") != null &&
                                                                   ((Map<String, Boolean>) session
                                                                           .getAttribute("checkedRolesMap"))
                                                                           .get(name.getItemName()) != null &&
                                                                   ((Map<String, Boolean>) session
                                                                           .getAttribute("checkedRolesMap"))
                                                                           .get(name.getItemName()) == true) {
                                                            doCheck = "checked=\"checked\"";
                                                        }
                                        %>
                                        <input type="checkbox" name="userRoles"
                                               value="<%=Encode.forHtmlAttribute(name.getItemName())%>" <%=doCheck%> <%=doEdit%> />
                                        <%=Encode.forHtml(name.getItemName())%>
                                        <input type="hidden" name="shownUsers"
                                               value="<%=Encode.forHtmlAttribute(name.getItemName())%>"/><br/>
                                        <%
                                                    }
                                                }
                                            }

                                        %>
                                    </td>
                                </tr>
                            </table>

                        </td>
                    </tr>
                    <carbon:paginator pageNumber="<%=pageNumber%>"
                                      action="post"
                                      numberOfPages="<%=numberOfPages%>"
                                      noOfPageLinksToDisplay="<%=noOfPageLinksToDisplay%>"
                                      page="add-step2.jsp" pageNumberParameterName="pageNumber"
                                      parameters="<%="username=" + Encode.forHtmlAttribute(userName)%>"/>
                    <%
                        if (roles != null) {
                            if (roles.length > 0) {
                                if (exceededDomains.getItemName() != null ||
                                    exceededDomains.getItemDisplayName() != null) {
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
                                            message = resourceBundle.getString("more.roles.others").replace("{0}", arg);
                                        } else {
                                            message = resourceBundle.getString("more.roles.primary");
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
                        message = resourceBundle.getString("more.roles").replace("{0}", arg);
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
                            <input type="submit" class="button" value="<fmt:message key="finish"/>">
                            <input type="button" class="button" value="<fmt:message key="cancel"/>"
                                   onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

<script type="text/javascript">

    function doCancel() {
        location.href = 'user-mgt.jsp?ordinal=1';
    }

    function doSelectAllRetrieved() {
        var form = document.createElement("form");
        form.id = "selectAllRetrievedForm";
        form.setAttribute("method", "POST");
        form.setAttribute("action", "add-step2.jsp?pageNumber=" + <%=pageNumber%> +"&username=" + '<%=Encode.forJavaScript(Encode.forUriComponent(userName))%>');
        var selectedRolesElem = document.createElement("input");
        selectedRolesElem.setAttribute("type", "hidden");
        selectedRolesElem.setAttribute("name", "selectedRoles");
        selectedRolesElem.setAttribute("value", "ALL");
        form.appendChild(selectedRolesElem);
        document.body.appendChild(form);
        $("#selectAllRetrievedForm").submit();
    }

    function doUnSelectAllRetrieved() {
        var form = document.createElement("form");
        form.id = "unSelectAllRetrievedForm";
        form.setAttribute("method", "POST");
        form.setAttribute("action", "add-step2.jsp?pageNumber=" + <%=pageNumber%> +"&username=" + '<%=Encode.forJavaScript(Encode.forUriComponent(userName))%>');
        var unselectedRolesElem = document.createElement("input");
        unselectedRolesElem.setAttribute("type", "hidden");
        unselectedRolesElem.setAttribute("name", "unselectedRoles");
        unselectedRolesElem.setAttribute("value", "ALL");
        form.appendChild(unselectedRolesElem);
        document.body.appendChild(form);
        $("#unSelectAllRetrievedForm").submit();
    }

        $(document).ready(function () {
            $('form[name=filterForm]').submit(function () {
                return doValidateForm(this, '<fmt:message key="error.input.validation.msg"/>');
            })
        });

    </script>
</fmt:bundle>
