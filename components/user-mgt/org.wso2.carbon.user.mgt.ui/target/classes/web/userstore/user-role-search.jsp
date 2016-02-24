<%--
  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue" %>
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

<%
    String isAJAXRequest = request.getParameter("ajax");
    if(!StringUtils.isNotBlank(isAJAXRequest) || isAJAXRequest.equals("false")){
%>


<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>

<%
    }
%>


<%


    String navigatorHolder = request.getParameter("navigator-holder");
    if (navigatorHolder == null || navigatorHolder == "") {
        navigatorHolder = "navigator";
    }
    String resultHolder = request.getParameter("result-holder");
    if (resultHolder == null || resultHolder == "") {
        resultHolder = "result";
    }

    String functionForGetAllItems = request.getParameter("function-get-all-items");

    boolean error = false;
    boolean newFilter = false;
    boolean doUserList = true;
    boolean showFilterMessage = false;
    boolean multipleUserStores = false;
    String forwardTo = "user-mgt.jsp";

    FlaggedName[] datas = null;
    FlaggedName exceededDomains = null;
    String[] claimUris = null;
    FlaggedName[] users = null;
    String[] domainNames = null;
    int pageNumber = 0;
    int cachePages = 3;
    int noOfPageLinksToDisplay = 5;
    int numberOfPages = 0;
    Map<Integer, PaginatedNamesBean> flaggedNameMap = null;

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
    if (userRealmInfo != null) {
        multipleUserStores = userRealmInfo.getMultipleUserStore();
    }
    java.lang.String errorAttribute = (java.lang.String) session.getAttribute(UserAdminUIConstants.DO_USER_LIST);

    String claimUri = request.getParameter("claimUri");
    if (claimUri == null || claimUri.length() == 0) {
        claimUri = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_CLAIM_FILTER);
    }
    session.setAttribute(UserAdminUIConstants.USER_CLAIM_FILTER, claimUri);
    exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.USER_LIST_CACHE_EXCEEDED);

    //  search filter
    String selectedDomain = request.getParameter("domain");
    if (selectedDomain == null || selectedDomain.trim().length() == 0) {
        selectedDomain = (String) session.getAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER);
        if (selectedDomain == null || selectedDomain.trim().length() == 0) {
            selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    session.setAttribute(UserAdminUIConstants.USER_LIST_DOMAIN_FILTER, selectedDomain.trim());

    String filter = request.getParameter(UserAdminUIConstants.USER_LIST_FILTER);
    if (filter == null || filter.trim().length() == 0) {
        filter = (java.lang.String) session.getAttribute(UserAdminUIConstants.USER_LIST_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    } else {
        if (filter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)) {
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
            if (userRealmInfo == null) {
                userRealmInfo = client.getUserRealmInfo();
                session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
            }

            if (userRealmInfo != null) {
                claimUris = userRealmInfo.getRequiredUserClaims();
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
            if (!StringUtils.isNotBlank(isAJAXRequest) || isAJAXRequest.equals("false")) {
                %>
                    <script type="text/javascript">
                        jQuery(document).ready(function () {
                            CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>', null);
                        });
                    </script>
                <%
            }
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
<%
    if(!StringUtils.isNotBlank(isAJAXRequest) || isAJAXRequest.equals("false")){
%>
<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">
<script>

    var navigatorHolder = '<%=Encode.forJavaScript(navigatorHolder)%>';
    var resultHolder = '<%=Encode.forJavaScript(resultHolder)%>';

    function search(pageNumber){
        if(!pageNumber){
            pageNumber = "0";
        }

        if (doValidateForm($("#id_search")[0], '<fmt:message key="error.input.validation.msg"/>')) {
            var category = $("input[name=radio_user_role]:checked").val();
            $.ajax({
                url: "/userandrolemgtservice?category=" + category + "&pageNumber=" + pageNumber,
                type: "POST",
                data: $("#id_search").serialize(),
                success: function (data, textStatus, jqXHR) {
                    doSearch("success", data);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    doSearch("fail", errorThrown);
                }
            });
        }
    }
    var registerSearchResult = null ;
    function registerSearchResultEvent(registerSearchResultParam){
        registerSearchResult = registerSearchResultParam ;
    }

    var registerNavigateEvent = null ;
    function registerNavigateEvent(registerNavigateParam){
        registerNavigateEvent = registerNavigateParam ;
    }

    var registerGetSelectedItem = <%= functionForGetAllItems %> ;



    function doSearch(status, data){
        if(registerSearchResult!=null){
            registerSearchResult(status, data);
            return;
        }
        $('#'+navigatorHolder).empty();
        $('#'+resultHolder).empty();

        var navigatorHtml = loadNavigator(data.numberOfPages, data.pageNumber , data.noOfPageLinksToDisplay);
        $('#'+navigatorHolder).append(navigatorHtml);

        var category = $("input[name=radio_user_role]:checked").val();

        var resultTable = "" ;
        if(category == "users"){

            resultTable = '<table class="styledLeft noBorders" id="userTable"><thead>';
            resultTable += '<tr>';
            resultTable += '<th width="50px">Select</th>';
            resultTable += '<th>UserName</th>';
            resultTable += '</tr>';
            resultTable += '</thead>';
            resultTable += '<tbody>';
            for(var i=0 ;i<data.userBeans.length;i++){
                var userName = data.userBeans[i].username ;

                resultTable += '<tr>';
                resultTable += '<td><input type="checkbox" name="item" value="'+userName+'"/></td>';
                resultTable += '<td>'+userName+'</td>';
                resultTable += '</tr>';

            }
            resultTable += '</tbody>';
            resultTable += '</table>';
            resultTable += '<table>';
            resultTable += '<tr>';
            resultTable += '<td colspan="2" align="left"><a class="icon-link"  style="background-image:url(images/add.png);margin-left:0" onclick="addSelectedItems();"><fmt:message key='workflow.template.button.add.users'/></a></td>';
            resultTable += '</tr>';
            resultTable += '</table>';

        }else if(category == "roles"){

            resultTable = '<table class="styledLeft noBorders" id="userTable"><thead>';
            resultTable += '<tr>';
            resultTable += '<th width="50px">Select</th>';
            resultTable += '<th>RoleName</th>';
            resultTable += '</tr>';
            resultTable += '</thead>';
            resultTable += '<tbody>';
            for(var i=0 ;i<data.roleBeans.length;i++){
                var roleName = data.roleBeans[i].roleName ;

                resultTable += '<tr>';
                resultTable += '<td><input type="checkbox" name="item" value="'+roleName+'"/></td>';
                resultTable += '<td>'+roleName+'</td>';
                resultTable += '</tr>';

            }
            resultTable += '</tbody>';
            resultTable += '</table>';
            resultTable += '<table>';
            resultTable += '<tr>';
            resultTable += '<td colspan="2" align="left"><a class="icon-link"  style="background-image:url(images/add.png);margin-left:0" onclick="addSelectedItems();"><fmt:message key='workflow.template.button.add.roles'/></a></td>';
            resultTable += '</tr>';
            resultTable += '</table>';

        }

        $('#'+resultHolder).append(resultTable);
    }




    function updateEmptyResultView(){

        $('#'+navigatorHolder).empty();
        $('#'+resultHolder).empty();


        var category = $("input[name=radio_user_role]:checked").val();

        var resultTable = "" ;
        if(category == "users"){

            resultTable = '<table class="styledLeft noBorders" id="userTable"><thead>';
            resultTable += '<tr>';
            resultTable += '<th width="50px">Select</th>';
            resultTable += '<th>UserName</th>';
            resultTable += '</tr>';
            resultTable += '</thead>';
            resultTable += '</table>';
            resultTable += '<table>';
            resultTable += '<tr>';
            resultTable += '<td colspan="2" align="left"><a class="icon-link"  style="background-image:url(images/add.png);margin-left:0" onclick="addSelectedItems();"><fmt:message key='workflow.template.button.add.users'/></a></td>';
            resultTable += '</tr>';
            resultTable += '</table>';

        }else if(category == "roles"){

            resultTable = '<table class="styledLeft noBorders" id="userTable"><thead>';
            resultTable += '<tr>';
            resultTable += '<th width="50px">Select</th>';
            resultTable += '<th>RoleName</th>';
            resultTable += '</tr>';
            resultTable += '</thead>';
            resultTable += '</table>';
            resultTable += '<table>';
            resultTable += '<tr>';
            resultTable += '<td colspan="2" align="left"><a class="icon-link"  style="background-image:url(images/add.png);margin-left:0" onclick="addSelectedItems();"><fmt:message key='workflow.template.button.add.roles'/></a></td>';
            resultTable += '</tr>';
            resultTable += '</table>';

        }

        $('#'+resultHolder).append(resultTable);
    }

    function doPaginate(pageName, pageNumberParamaeterName, pageNumber){
        if(registerNavigateEvent!=null){
            registerNavigateEvent(pageName, pageNumberParamaeterName, pageNumber);
            return;
        }
        search(pageNumber);

    }

    function changeCategory(category,init){

        updateEmptyResultView();

        if(category == "users"){
            $("#id_claim_attribute").show();
            $("#id_search_button").val('<fmt:message key="user.search"/>');
            $("#id_pattern_category").html('<fmt:message key="list.users"/>');
            $("#id_filter").attr('name','<%=UserAdminUIConstants.USER_LIST_FILTER%>');


        }else{
            $("#id_claim_attribute").hide();
            $("#id_search_button").val('<fmt:message key="role.search"/>');
            $("#id_pattern_category").html('<fmt:message key="list.roles"/>');
            $("#id_filter").attr('name','<%=UserAdminUIConstants.ROLE_LIST_FILTER%>');
        }


    }

    function loadCategory(category,init){

        if(init){
            $('input:radio[name=radio_user_role][value=roles]').prop('checked', true);
        }else{
            $('input:radio[name=radio_user_role][value='+category+']').prop('checked', true);
        }
        changeCategory(category,init);
    }

    function addSelectedItems(){

        var allVals = [];
        $('[name=item]:checked').each(function () {
            allVals.push($(this).val());
        });

        if(registerGetSelectedItem!=null){
            var category = $("input[name=radio_user_role]:checked").val();
            registerGetSelectedItem(allVals, category);
        }
    }


    function loadNavigator(numberOfPages, pageNumber , noOfPageLinksToDisplay){

            var next = "next" ;
            var prev = "prev" ;
            var page = "test.jsp" ;
            var pageNumberParameterName = "pageName" ;
            var showPageNumbers = true ;
            var action = "post" ;

            var var11 = "<table><tr>";
            if(numberOfPages > 1) {
                if(pageNumber > 0) {
                    if( "post" != action) {
                        var11 = var11 + "<td><strong><a href=\"" + page + "?" + pageNumberParameterName + "=0" + "&" + parameters + "\">&lt;&lt;first" + "&nbsp;&nbsp;</a></strong></td>" + "<td><strong><a href=\"" + page + "?" + pageNumberParameterName + "=" + (pageNumber - 1) + "&" + parameters + "\">" + "&lt;&nbsp;" + prev + "&nbsp;&nbsp;</a></strong></td>";
                    } else {
                        var11 = var11 + "<td><strong><a href=# onclick=\"doPaginate(\'" + page + "\',\'" + pageNumberParameterName + "\',\'" + 0 + "\')\">&lt;&lt;first" + "&nbsp;&nbsp;</a></strong></td>" + "<td><strong><a href=# onclick=\"doPaginate(\'" + page + "\',\'" + pageNumberParameterName + "\',\'" + (pageNumber - 1) + "\')\">" + "&lt;&nbsp;" + prev + "&nbsp;&nbsp;</a></strong></td>";
                    }
                } else {
                    var11 = var11 + "<td ><strong ><span style=\"color:gray\">&lt;&lt; first &nbsp;&nbsp;&lt;" + prev + "&nbsp;&nbsp;</span></strong></td>";
                }

                if(!showPageNumbers) {
                    var11 = var11 + "<td><strong> Page &nbsp;&nbsp;" + (pageNumber + 1) + " of  " + numberOfPages + " &nbsp;&nbsp;</strong></td>";
                } else {
                    var e;
                    var msg;
                    if(noOfPageLinksToDisplay % 2 == 0) {
                        if(pageNumber - (noOfPageLinksToDisplay / 2 - 1) < 0) {
                            e = 0;
                        } else {
                            e = pageNumber - (noOfPageLinksToDisplay / 2 - 1);
                        }

                        if(pageNumber + noOfPageLinksToDisplay / 2 > numberOfPages - 1) {
                            msg = numberOfPages - 1;
                        } else {
                            msg = pageNumber + noOfPageLinksToDisplay / 2;
                        }
                    } else {
                        if(pageNumber - Math.floor((noOfPageLinksToDisplay / 2)) < 0) {
                            e = 0;
                        } else {
                            e = pageNumber - Math.floor((noOfPageLinksToDisplay / 2));
                        }

                        if(pageNumber +  Math.floor((noOfPageLinksToDisplay / 2)) > numberOfPages - 1) {
                            msg = numberOfPages - 1;
                        } else {
                            msg = pageNumber +  Math.floor((noOfPageLinksToDisplay / 2));
                        }
                    }

                    if(e != 0) {
                        var11 = var11 + "<td><strong> ... &nbsp;&nbsp;</strong></td> ";
                    }

                    for(var i = e; i <= msg; ++i) {
                        if(i == pageNumber) {
                            var11 = var11 + "<td><strong>" + (i + 1) + "&nbsp;&nbsp;</strong></td>";
                        } else if("post" != action) {
                            var11 = var11 + "<td><strong><a href=\"" + page + "?" + pageNumberParameterName + "=" + i + "&" + parameters + "\">" + (i + 1) + " &nbsp;&nbsp;</a></strong></td>";
                        } else {
                            var11 = var11 + "<td><strong><a href=# onclick=\"doPaginate(\'" + page + "\',\'" + pageNumberParameterName + "\',\'" + i + "\')\">" + (i + 1) + " &nbsp;&nbsp;</a></strong></td>";
                        }
                    }

                    if(msg != numberOfPages - 1) {
                        var11 = var11 + "<td><strong> ... &nbsp;&nbsp;</strong></td> ";
                    }
                }

                if(pageNumber < numberOfPages - 1) {
                    if( "post" != action) {
                        var11 = var11 + "<td ><strong ><a href =\"" + page + "?" + pageNumberParameterName + "=" + (pageNumber + 1) + "&" + parameters + "\">" + next + "&nbsp;&gt;</a></strong></td>" + "<td ><strong ><a href =\"" + page + "?" + pageNumberParameterName + "=" + (numberOfPages - 1) + "&" + parameters + "\">" + "&nbsp;&nbsp;last" + "&nbsp;&gt;&gt;</a></strong></td>";
                    } else {
                        var11 = var11 + "<td ><strong><a href=# onclick=\"doPaginate(\'" + page + "\',\'" + pageNumberParameterName + "\',\'" + (pageNumber + 1) + "\')\">" + next + "&nbsp;&gt;</a></strong></td>" + "<td ><strong ><a href=# onclick=\"doPaginate(\'" + page + "\',\'" + pageNumberParameterName + "\',\'" + (numberOfPages - 1) + "\')\">" + "&nbsp;&nbsp;last" + "&nbsp;&gt;&gt;</a></strong></td>";
                    }
                } else {
                    var11 = var11 + "<td ><strong ><span style=\"color:gray\">" + next + " &gt;&nbsp;&nbsp;" + "last" + "&gt;&gt; " + "</span></strong></td>";
                }
            }

            var11 = var11 + "</tr ></table > ";
            return var11 ;

    }



</script>

<style>
    .LargeHeader{
        font-size: small;
    }

    h2.triggerIn {
        border: solid 1px #c2c4c6;
        -moz-box-shadow: 3px 3px 3px #888;
        -webkit-box-shadow: 3px 3px 3px #888;
        box-shadow: 3px 3px 3px #888;
        padding: 0;
        background-color: #e9e9e9;
        background-repeat: no-repeat;
        background-position: 5px center;
        padding-left: 0px;
        padding-bottom: 0px !important;
        margin-bottom: 0px !important;
        margin: 0;
        height: 25px;
    }


</style>

    <carbon:breadcrumb label="users"
                       resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">



        function deleteUser(user) {
            function doDelete() {
                var userName = user;
                location.href = 'delete-finish.jsp?username=' + userName;
            }

            CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.user"/> \'" + user + "\'?", doDelete, null);
        }

        <%if (showFilterMessage == true) {%>
        jQuery(document).ready(function () {
            CARBON.showInfoDialog('<fmt:message key="no.users.filtered"/>', null, null);
        });
        <%}%>
    </script>

    <div id="middle">



        <div id="workArea" style="margin-left: 10px">
            <form id="id_search" name="filterForm" method="post" onsubmit="search();return false">

                <h2  class="triggerIn" style="background-color: floralwhite;">
                    <div class="LargeHeader">
                        <input onclick="changeCategory('roles',false);" type="radio" id="id_radio_role" name="radio_user_role" value="roles" checked="checked"><label for="id_radio_role"><img  style="margin-top:  5px; margin-right:3px" src="images/user-roles.gif"><fmt:message key="role.search"/></label>
                        <input onclick="changeCategory('users',false);" type="radio" id="id_radio_user" name="radio_user_role" value="users"><label for="id_radio_user"><img style="margin-top:  5px; margin-right:3px" src="images/users.gif"><fmt:message key="user.search"/></label>
                    </div>
                </h2>

                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="local_auth_head_dev_1">
                    <table>
                        <tbody>
                        <%
                            if(domainNames != null && domainNames.length > 0){
                        %>
                        <tr>
                            <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message key="select.domain.search"/></td>
                            <td><select id="domain" name="domain">
                                <%
                                    for(String domainName : domainNames) {
                                        if(selectedDomain.equals(domainName)) {
                                %>
                                <option selected="selected" value="<%=Encode.forHtmlAttribute(domainName)%>">
                                    <%=Encode.forHtmlContent(domainName)%>
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
                            <td id="id_pattern_category" class="leftCol-big" style="padding-right: 0 !important;"><fmt:message key="list.users"/></td>
                            <td>
                                <input id="id_filter" type="text" name="<%=UserAdminUIConstants.USER_LIST_FILTER%>"
                                       value="<%=Encode.forHtmlAttribute(filter)%>" label="<fmt:message
                                       key="list.users"/>" black-list-patterns="xml-meta-exists"/>

                                <input id="id_search_button" onclick="search();" class="button" type="button"
                                       value="<fmt:message key="user.search"/>"/>
                            </td>
                        </tr>
                        <tr id="id_claim_attribute">
                            <td><fmt:message key="claim.uri"/></td>
                            <td><select id="claimUri" name="claimUri">
                                <option value="Select" selected="selected">Select</option>
                                <%
                                    if(claimUris != null){

                                        for(String claim : claimUris) {
                                            if(claimUri != null && claim.equals(claimUri)) {
                                %>
                                <option selected="selected" value="<%=Encode.forHtmlAttribute(claim)%>">
                                    <%=Encode.forHtmlContent(claim)%>
                                </option>
                                <%
                                } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(claim)%>"><%=Encode.forHtmlContent(claim)%>
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
                    <div id="result">

                    </div>
                    <div id="navigator">

                    </div>
                </div>


            </form>
            <p>&nbsp;</p>


    </div>


</fmt:bundle>

<%
    }
%>