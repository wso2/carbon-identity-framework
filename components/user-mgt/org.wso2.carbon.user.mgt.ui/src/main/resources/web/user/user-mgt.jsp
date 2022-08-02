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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
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
<%@ page import="org.wso2.carbon.user.mgt.ui.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>
<title>WSO2 Carbon - Security Configuration</title>
<%

    boolean error = false;
    boolean newFilter = false;
    boolean doUserList = true;
    boolean showFilterMessage = false;
    String forwardTo = "user-mgt.jsp";
    
    String disallowedCharacterRegEx = null;
    if (IdentityUtil.getProperty(UserAdminUIConstants.CONFIG_DISALLOWED_CHARACTER_REGEX) != null) {
        disallowedCharacterRegEx =
                IdentityUtil.getProperty(UserAdminUIConstants.CONFIG_DISALLOWED_CHARACTER_REGEX)
                        .replace("\"", "&quot;");
    }
    
    FlaggedName[] datas = null;
    FlaggedName exceededDomains = null;
    String[] claimUris = null;
    FlaggedName[] users = null;
    String[] domainNames = null;
    Map<String, String> userCount = new HashMap<String, String>();
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;
    Set<FlaggedName> workFlowAddPendingUsers = new LinkedHashSet<FlaggedName>();
    Set<String> workFlowAddPendingUsersList = new LinkedHashSet<String>();
    Set<String> workFlowDeletePendingUsers = new LinkedHashSet<String>();
    Set<FlaggedName> activeUserList;
    Set<FlaggedName> showDeletePendingUsers = new LinkedHashSet<FlaggedName>();
    Set<String> showDeletePendingUsersList = new LinkedHashSet<String>();
    Set<FlaggedName> aggregateUserList = new LinkedHashSet<FlaggedName>();
    Set<FlaggedName> removeUserElement = new LinkedHashSet<FlaggedName>();
    Set<String> countableUserStores = new LinkedHashSet<String>();

    String BUNDLE = "org.wso2.carbon.userstore.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    // remove session data
    session.removeAttribute("userBean");
    session.removeAttribute(UserAdminUIConstants.USER_DISPLAY_NAME);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_UNASSIGNED_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_UNASSIGNED_ROLE_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGNED_ROLE_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ADD_USER_ROLE_CACHE_EXCEEDED);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_ASSIGN_ROLE_FILTER);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_UNASSIGNED_ROLE_FILTER);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_VIEW_ROLE_FILTER);
    session.removeAttribute(UserAdminUIConstants.USER_LIST_CACHE);

    // retrieve session attributes
    String currentUser = (String) session.getAttribute("logged-user");
    UserRealmInfo userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
    java.lang.String errorAttribute = (java.lang.String) session.getAttribute(UserAdminUIConstants.DO_USER_LIST);

    String claimUri = request.getParameter("claimUri");
    if (StringUtils.isBlank(claimUri)) {
        claimUri = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_CLAIM_FILTER);
    }

    String countClaimUri = request.getParameter("countClaimUri");
    if (StringUtils.isBlank(countClaimUri)) {
        countClaimUri = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_CLAIM_COUNT_FILTER);
        if (StringUtils.isBlank(countClaimUri)) {
            countClaimUri = UserAdminUIConstants.SELECT;
        }
    }

    session.setAttribute(UserAdminUIConstants.USER_CLAIM_FILTER, claimUri);
    session.setAttribute(UserAdminUIConstants.USER_CLAIM_COUNT_FILTER, countClaimUri);
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.USER_LIST_CACHE_EXCEEDED);

    //  search filter
    String selectedDomain = request.getParameter("domain");
    if (StringUtils.isBlank(selectedDomain)) {
        selectedDomain = (String) session.getAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER);
        if (StringUtils.isBlank(selectedDomain)) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    //  search filter
    String selectedCountDomain = request.getParameter("countDomain");
    if (StringUtils.isBlank(selectedCountDomain)) {
        selectedCountDomain = (String) session.getAttribute(UserAdminUIConstants.USER_LIST_COUNT_DOMAIN_FILTER);
        if (StringUtils.isBlank(selectedCountDomain)) {
            selectedCountDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    session.setAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER, selectedDomain.trim());
    session.setAttribute(UserAdminUIConstants.USER_LIST_COUNT_DOMAIN_FILTER, selectedCountDomain.trim());

    String filter = request.getParameter(UserAdminUIConstants.USER_LIST_FILTER);
    if (StringUtils.isBlank(filter)) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_LIST_FILTER);
        if (StringUtils.isBlank(filter)) {
            filter = "*";
        }
    } else {
        if (filter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
            session.removeAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER);
        }
        newFilter = true;
    }

    String countFilter = request.getParameter(UserAdminUIConstants.USER_COUNT_FILTER);
    if (StringUtils.isBlank(countFilter)) {
        countFilter = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_COUNT_FILTER);
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


    String userDomainSelector;
    String modifiedFilter = filter.trim();
    if (!UserAdminUIConstants.ALL_DOMAINS.equalsIgnoreCase(selectedDomain)) {
        modifiedFilter = selectedDomain + UserAdminUIConstants.DOMAIN_SEPARATOR + filter;
        modifiedFilter = modifiedFilter.trim();
        userDomainSelector = selectedDomain + UserAdminUIConstants.DOMAIN_SEPARATOR + "*";
    } else {
        userDomainSelector = "*";
    }

    session.setAttribute(UserAdminUIConstants.USER_LIST_FILTER, filter.trim());
    session.setAttribute(UserAdminUIConstants.USER_COUNT_FILTER, countFilter.trim());

    // check page number
    String pageNumberStr = request.getParameter("pageNumber");
    if (pageNumberStr == null) {
        pageNumberStr = "0";
    }

    if (userRealmInfo != null) {
        claimUris = userRealmInfo.getRequiredUserClaims();
    }

    try {
        pageNumber = Integer.parseInt(pageNumberStr);
    } catch (NumberFormatException ignored) {
        // page number format exception
    }

    flaggedNameMap = (Map<Integer, PaginatedNamesBean>) session.getAttribute(UserAdminUIConstants.USER_LIST_CACHE);
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

    if (errorAttribute != null) {
        error = true;
        session.removeAttribute(UserAdminUIConstants.DO_USER_LIST);
    }

    if ((doUserList || newFilter) && !error) { // don't call the back end if some kind of message is showing
        try {
            java.lang.String cookie = (java.lang.String) session
                    .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            java.lang.String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
            UserManagementWorkflowServiceClient UserMgtClient = new
                    UserManagementWorkflowServiceClient(cookie, backendServerURL, configContext);

            UserStoreCountClient countClient = new UserStoreCountClient(cookie, backendServerURL, configContext);

            if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/userstore/count/view")) {

                countableUserStores = countClient.getCountableUserStores();

                if (UserAdminUIConstants.SELECT.equalsIgnoreCase(countClaimUri)) {    //this is user name based search
                    if (selectedCountDomain.equalsIgnoreCase(UserAdminUIConstants.ALL_DOMAINS)) {
                        userCount = countClient.countUsers(countFilter);
                    } else {
                        try {
                            userCount.put(selectedCountDomain, String.valueOf(countClient.countUsersInDomain(countFilter,
                                selectedCountDomain)));
                        } catch (Exception e) {
                            // In this scenario an error should be shown in the count results section.
                            userCount.put(selectedCountDomain, "Error while getting user count");
                        }
                    }
                } else {                //this is a claim based search
                    if (selectedCountDomain.equalsIgnoreCase(UserAdminUIConstants.ALL_DOMAINS)) {
                        userCount = countClient.countByClaim(countClaimUri, countFilter);
                    } else {
                        try {
                            userCount.put(selectedCountDomain, String.valueOf(countClient.countByClaimInDomain
                                (countClaimUri, countFilter, selectedCountDomain)));
                        } catch (Exception e) {
                            // In this scenario an error should be shown in the count results section.
                            userCount.put(selectedCountDomain, "Error while getting user count");
                        }
                    }
                }
            }


            if (userRealmInfo == null) {
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }

            if (userRealmInfo != null) {
                claimUris = userRealmInfo.getDefaultUserClaims();
            }

            if (filter.length() > 0) {
                if (claimUri != null && !"select".equalsIgnoreCase(claimUri)) {
                    ClaimValue claimValue = new ClaimValue();
                    claimValue.setClaimURI(claimUri);
                    claimValue.setValue(modifiedFilter);
                    datas = client.listUserByClaim(claimValue, userDomainSelector, -1);
                } else {
                    datas = client.listAllUsers(modifiedFilter, -1);
                }
                if (CarbonUIUtil.isContextRegistered(config, "/usermgt-workflow/")) {
                    List<FlaggedName> preactiveUserList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                    FlaggedName excessiveDomainElement = preactiveUserList.remove(datas.length - 1);
                    removeUserElement.add(excessiveDomainElement);

                    activeUserList = new LinkedHashSet<FlaggedName>(preactiveUserList);

                    String[] AddPendingUsersList = UserMgtClient.
                            listAllEntityNames("ADD_USER", "PENDING", "USER", modifiedFilter);
                    workFlowAddPendingUsersList = new LinkedHashSet<String>(Arrays.asList(AddPendingUsersList));

                    for (String s : AddPendingUsersList) {
                        FlaggedName flaggedName = new FlaggedName();
                        flaggedName.setItemName(s);
                        flaggedName.setEditable(true);
                        workFlowAddPendingUsers.add(flaggedName);
                    }
                    String[] DeletePendingUsersList = UserMgtClient.
                            listAllEntityNames("DELETE_USER", "PENDING", "USER", modifiedFilter);
                    workFlowDeletePendingUsers = new LinkedHashSet<String>(Arrays.asList(DeletePendingUsersList));

                    for (Iterator<FlaggedName> iterator = activeUserList.iterator(); iterator.hasNext(); ) {
                        FlaggedName flaggedName = iterator.next();
                        if (flaggedName == null) {
                            continue;
                        }
                        String userName = flaggedName.getItemName();
                        if (workFlowDeletePendingUsers.contains(userName)) {
                            showDeletePendingUsers.add(flaggedName);
                            showDeletePendingUsersList.add(userName);
                            iterator.remove();
                        }
                    }
                    aggregateUserList.addAll(activeUserList);
                    aggregateUserList.addAll(showDeletePendingUsers);
                    aggregateUserList.addAll(workFlowAddPendingUsers);
                    aggregateUserList.addAll(removeUserElement);
                    datas = aggregateUserList.toArray(new FlaggedName[aggregateUserList.size()]);
                }

                List<FlaggedName> dataList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                exceededDomains = dataList.remove(dataList.size() - 1);
                session.setAttribute(UserAdminUIConstants.USER_LIST_CACHE_EXCEEDED, exceededDomains);
                if (dataList == null || dataList.size() == 0) {
                    session.removeAttribute(UserAdminUIConstants.USER_LIST_FILTER);
                    showFilterMessage = true;
                }

                if (dataList != null) {
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
                    session.setAttribute(UserAdminUIConstants.USER_LIST_CACHE, flaggedNameMap);
                }
            }

        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("error.while.user.filtered"),
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
        domainNames = userRealmInfo.getDomainNames();
        if (domainNames != null) {
            List<String> list = new ArrayList<String>(Arrays.asList(domainNames));
            list.add(UserAdminUIConstants.ALL_DOMAINS);
            domainNames = list.toArray(new String[list.size()]);
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
    <carbon:breadcrumb label="users"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function deleteUser(user) {
            function doDelete() {
                var userName = user;
                $.ajax({
                    type: 'POST',
                    url: 'delete-finish-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: 'username=' + encodeURIComponent(userName),
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("user-mgt.jsp?ordinal=1");
                        }
                    }
                });
            }
            CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.user"/> \'" + user + "\'?", doDelete, null);
        }

        var disallowedCharacterRegEx = "<%=disallowedCharacterRegEx%>";
        $(document).ready(function () {
            $('form[name=filterForm]').submit(function(){
                return doValidateForm(this, '<fmt:message key="error.input.validation.msg"/>');
            })
        });

        <%if (showFilterMessage == true) {%>
        jQuery(document).ready(function () {
            CARBON.showInfoDialog('<fmt:message key="no.users.filtered"/>', null, null);
        });
        <%}%>
    </script>

    <div id="middle">
        <h2><fmt:message key="users"/></h2>

        <div id="workArea">
            <form name="filterForm" method="post" action="user-mgt.jsp">
                <table class="styledLeft noBorders">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="user.search"/></th>
                    </tr>
                    </thead>
                    <tbody>

                    <%
                        if (domainNames != null && domainNames.length > 0) {
                    %>
                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                                key="select.domain.search"/></td>
                        <td><select id="domain" name="domain">
                            <%
                                for (String domainName : domainNames) {
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
                    <%
                        }
                    %>

                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                                key="list.users"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.USER_LIST_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(filter)%>" label="<fmt:message key="list.users"/>"
                                   autocomplete="off" black-list-patterns="invalid-username-search"/>

                            <input class="button" type="submit"
                                   value="<fmt:message key="user.search"/>"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="claim.uri"/></td>
                        <td><select id="claimUri" name="claimUri">
                            <option value="Select" selected="selected"><%=UserAdminUIConstants.SELECT%></option>
                            <%
                                if (claimUris != null) {

                                    for (String claim : claimUris) {
                                        if (claimUri != null && claim.equals(claimUri)) {
                            %>
                            <option selected="selected" value="<%=Encode.forHtmlAttribute(claim)%>">
                                <%=Encode.forHtmlContent(claim)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(claim)%>">
                                <%=Encode.forHtmlContent(claim)%>
                            </option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <p>&nbsp;</p>

            <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/userstore/count/view")) { %>
            <form name="countForm" method="post" action="user-mgt.jsp">
                <table class="styledLeft">
                    <%
                        if (countableUserStores != null && !countableUserStores.isEmpty()) {
                            if (countableUserStores.size() > 1) {
                                countableUserStores.add(UserAdminUIConstants.ALL_DOMAINS);
                            }
                    %>
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="user.count"/></th>
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
                                key="count.users"/></td>
                        <td>
                            <input type="text" name="<%=UserAdminUIConstants.USER_COUNT_FILTER%>"
                                   value="<%=Encode.forHtmlAttribute(countFilter)%>" label="<fmt:message key="count.users"/>"
                                   black-list-patterns="invalid-username-search"/>

                            <input class="button" type="submit"
                                   value="<fmt:message key="user.count"/>"/>
                        </td>
                    </tr>

                    <tr>
                        <td><fmt:message key="claim.uri"/></td>
                        <td><select id="countClaimUri" name="countClaimUri">
                            <option value="Select" selected="selected"><%=UserAdminUIConstants.SELECT%></option>
                            <%
                                if (claimUris != null) {

                                    for (String claim : claimUris) {
                                        if (countClaimUri != null && claim.equals(countClaimUri)) {
                            %>
                            <option selected="selected" value="<%=Encode.forHtmlAttribute(claim)%>">
                                <%=Encode.forHtmlContent(claim)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(claim)%>">
                                <%=Encode.forHtmlContent(claim)%>
                            </option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                        </td>
                    </tr>

                    <%
                        Iterator it = userCount.entrySet().iterator();
                        String key = null;
                        String value = null;
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            key = (String) pair.getKey();
                            value = (String) pair.getValue();
                    %>

                    <tr>
                        <td class="leftCol-big" style="padding-right: 0 !important;"><%=Encode.forHtml(key)%>
                        </td>
                        <td>
                            <%
                            if (StringUtils.isNumeric(value)) {
                            %>
                            <input type="text" readonly=true name="<%=UserAdminUIConstants.USER_COUNT%>"
                                                               value="<%=Encode.forHtmlAttribute(value)%>"/>
                            <%
                            } else {
                            %>
                            <p>Error occurred while getting the count</p>
                            <%
                            }
                            %>
                        </td>
                    </tr>

                    <% it.remove();
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
                              page="user-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <table class="styledLeft" id="userTable">

                <%
                    if (users != null && users.length > 0) {
                %>
                <thead>
                <tr>
                    <th class="leftCol-big"><fmt:message key="name"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    }
                %>
                <tbody>
                <%
                    if (users != null) {
                        for (int i = 0; i < users.length; i++) {
                            if (users[i] != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug Axis!!
                                if (users[i].getItemName().equals(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME)) {
                                    continue;
                                }
                                String userName = users[i].getItemName();
                                String encryptedUsername = null;
                                try {
                                    encryptedUsername = Util.getEncryptedAndBase64encodedUsername(userName);
                                } catch (UserManagementUIException e) {
                                    CarbonUIMessage.sendCarbonUIMessage("Error in viewing user list",
                                            CarbonUIMessage.ERROR, request);
                                }
                                String displayName = users[i].getItemDisplayName();
                                if (displayName == null || displayName.trim().length() == 0) {
                                    displayName = userName;
                                }
                                if (workFlowAddPendingUsersList.contains(userName)) {
                %>
                <tr>
                    <td><%=Encode.forHtml(displayName)%>
                        <%if (!users[i].getEditable()) { %> <%="(Read-Only)"%> <% } %>
                        <img src="images/workflow_pending_add.gif" title="Workflow-pending-user-add"
                             alt="Workflow-pending-user-add" height="15" width="15">
                    </td>
                    <td>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../admin/images/edit.gif);color:#CCC;"><fmt:message
                                key="change.password"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../admin/images/edit.gif);color:#CCC;"><fmt:message
                                key="edit.roles"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/view.gif);color:#CCC;"><fmt:message
                                key="view.roles"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/delete.gif);color:#CCC;"><fmt:message
                                key="delete"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../userprofile/images/my-prof.gif);color:#CCC;">User
                            Profile</a>
                    </td>
                </tr>
                <%
                } else if (showDeletePendingUsersList.contains(userName)) {
                %>
                <tr>
                    <td><%=Encode.forHtml(displayName)%>
                        <%if (!users[i].getEditable()) { %> <%="(Read-Only)"%> <% } %>
                        <img src="images/workflow_pending_remove.gif" title="Workflow-pending-user-delete"
                             alt="Workflow-pending-user-delete" height="15" width="15">
                    </td>
                    <td>
                        <%
                            if (!Util.getUserStoreInfoForUser(userName, userRealmInfo).getPasswordsExternallyManaged() &&
                                    CarbonUIUtil.isUserAuthorized(request,
                                            "/permission/admin/manage/identity/identitymgt/update") &&
                                    users[i].getEditable()) { //if passwords are managed externally do not allow to change passwords.
                                if (Util.isCurrentUser(currentUser, userName, userRealmInfo)) {
                        %>
                        <a href="change-passwd.jsp?isUserChange=true&returnPath=user-mgt.jsp" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="change.password"/></a>

                        <%
                        } else {
                        %>

                        <a href="change-passwd.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="change.password"/></a>
                        <%
                                }
                            }
                        %>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../admin/images/edit.gif);color:#CCC;"><fmt:message
                                key="edit.roles"/></a>

                        <%
                            if (CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/manage/identity/rolemgt/view")) {
                        %>
                        <a href="view-roles.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>"
                           class="icon-link"
                           style="background-image:url(images/view.gif);"><fmt:message
                                key="view.roles"/></a>
                        <%
                            }
                        %>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/delete.gif);color:#CCC;"><fmt:message
                                key="delete"/></a>

                        <%
                            if (CarbonUIUtil.isContextRegistered(config, "/identity-authorization/") &&
                                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/")) {
                        %>
                        <a
                                href="../identity-authorization/permission-root.jsp?userName=<%=Encode.forUriComponent(encryptedUsername)%>&fromUserMgt=true"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="authorization"/></a>
                        <%
                            }
                        %>

                        <%
                            if (CarbonUIUtil.isContextRegistered(config, "/userprofile/")
                                    && CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/manage/identity/usermgt/update")) {
                        %>
                        <a
                                href="../userprofile/index.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>&fromUserMgt=true"
                           class="icon-link"
                           style="background-image:url(../userprofile/images/my-prof.gif);">User
                            Profile</a>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                } else {
                %>
                <tr>
                    <td><%=Encode.forHtml(displayName)%>
                        <%if (!users[i].getEditable()) { %> <%="(Read-Only)"%> <% } %>
                    </td>
                    <td>
                        <%
                            if (userRealmInfo.getAdminUser().equals(userName) &&
                                    !Util.isCurrentUser(currentUser, userName, userRealmInfo)) {
                        %>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../admin/images/edit.gif);color:#CCC;"><fmt:message
                                key="change.password"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../admin/images/edit.gif);color:#CCC;"><fmt:message
                                key="edit.roles"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/view.gif);color:#CCC;"><fmt:message
                                key="view.roles"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/delete.gif);color:#CCC;"><fmt:message
                                key="delete"/></a>

                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(../userprofile/images/my-prof.gif);color:#CCC;">User
                            Profile</a>
                        <%
                                continue;
                            }
                        %>
                        <%
                            if (!Util.getUserStoreInfoForUser(userName, userRealmInfo).getPasswordsExternallyManaged() &&      // TODO
                                    CarbonUIUtil.isUserAuthorized(request,
                                            "/permission/admin/manage/identity/identitymgt/update") &&
                                    users[i].getEditable()) { //if passwords are managed externally do not allow to change passwords.
                                if (Util.isCurrentUser(currentUser, userName, userRealmInfo)) {
                        %>
                        <a href="change-passwd.jsp?isUserChange=true&returnPath=user-mgt.jsp" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="change.password"/></a>

                        <%
                        } else {
                        %>

                        <a href="change-passwd.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="change.password"/></a>
                        <%
                                }
                            }
                        %>

                        <%
                            if (CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/manage/identity/rolemgt/update")) {
                        %>
                        <a href="edit-user-roles.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="edit.roles"/></a>
                        <%
                            }
                        %>

                        <%
                            if (CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/manage/identity/rolemgt/view")) {
                        %>
                        <a href="view-roles.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>"
                           class="icon-link"
                           style="background-image:url(images/view.gif);"><fmt:message
                                key="view.roles"/></a>
                        <%
                            }
                        %>

                        <%
                            if (CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/manage/identity/usermgt/delete")
                                    && !Util.isCurrentUser(currentUser, userName, userRealmInfo)
                                    && !userName.equals(userRealmInfo.getAdminUser()) && users[i].getEditable()) {
                        %>
                        <a href="#" onclick="deleteUser('<%=Encode.forJavaScriptAttribute(userName)%>')"
                           class="icon-link"
                           style="background-image:url(images/delete.gif);"><fmt:message
                                key="delete"/></a>
                        <%
                        } else if (Util.isCurrentUser(currentUser, userName, userRealmInfo) ||
                                userName.equals(userRealmInfo.getAdminUser())) {
                        %>
                        <a href="#" class="icon-link" title="Operation is Disabled"
                           style="background-image:url(images/delete.gif);color:#CCC;"><fmt:message
                                key="delete"/></a>
                        <%
                            }
                        %>
                        <%
                            if (CarbonUIUtil.isContextRegistered(config, "/identity-authorization/") &&
                                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/identity/")) {
                        %>
                        <a
                                href="../identity-authorization/permission-root.jsp?userName=<%=Encode.forUriComponent(encryptedUsername)%>&fromUserMgt=true"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="authorization"/></a>
                        <%
                            }
                        %>

                        <%
                            if (CarbonUIUtil.isContextRegistered(config, "/userprofile/")
                                    && CarbonUIUtil.isUserAuthorized(request,
                                    "/permission/admin/configure/security/usermgt/profiles")) {
                        %>
                        <a href="../userprofile/index.jsp?username=<%=Encode.forUriComponent(encryptedUsername)%>&displayName=<%=Encode.forUriComponent(displayName)%>&fromUserMgt=true"
                           class="icon-link" style="background-image:url(../userprofile/images/my-prof.gif);">User
                            Profile</a>
                        <%
                            }
                        %>

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
                              page="user-mgt.jsp" pageNumberParameterName="pageNumber"/>

            <p>&nbsp;</p>

            <%
                if (users != null && users.length > 0 && exceededDomains != null) {
                    if (exceededDomains.getItemName() != null || exceededDomains.getItemDisplayName() != null) {
                        String message = null;
                        if (exceededDomains.getItemName() != null && exceededDomains.getItemName().equals("true")) {
                            if (exceededDomains.getItemDisplayName() != null && !exceededDomains.getItemDisplayName().equals("")) {
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
            } else if (exceededDomains.getItemDisplayName() != null && !exceededDomains.getItemDisplayName().equals("")) {
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


        </div>
    </div>
    <script language="text/JavaScript">
        alternateTableRows('userTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>