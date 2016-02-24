<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<fmt:bundle basename="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources">
<carbon:breadcrumb
        label="new.userstore"
        resourceBundle="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
	<%
		String forwardTo;
		    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
		    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
	        UserStoreDTO[] userStoreDTOs = null;

		    try {

		        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
		        ConfigurationContext configContext =
		                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		        UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);
		        userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
		    }catch(Exception e) {
		   
		        userStoreDTOs = new UserStoreDTO[0];
		        String message = resourceBundle.getString("try.again");
		        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
		        forwardTo = "../admin/index.jsp";
		    }
	%>


	<script type="text/javascript">


    var allUserStoresSelected = false;

    function selectAllInThisPage(isSelected) {
        allUserStoresSelected = false;
        if (document.userStoreForm.userStores != null &&
                document.userStoreForm.userStores[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.userStoreForm.userStores.length; j++) {
                    document.userStoreForm.userStores[j].checked = true;
                }
            } else {
                for (j = 0; j < document.userStoreForm.userStores.length; j++) {
                    document.userStoreForm.userStores[j].checked = false;
                }
            }
        } else if (document.userStoreForm.userStores != null) { // only 1 service
            document.userStoreForm.userStores.checked = isSelected;
        }
        return false;
    }

    function resetVars() {
        allUserStoresSelected = false;

        var isSelected = false;
        if (document.userStoreForm.userStores != null) { // there is more than 1 service
            for (var j = 0; j < document.userStoreForm.userStores.length; j++) {
                if (document.userStoreForm.userStores[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.userStoreForm.userStores != null) { // only 1 service
            if (document.userStoreForm.userStores.checked) {
                isSelected = true;
            }
        }
        return false;
    }


    function deleteUserStores() {
        var selected = false;
        if (document.userStoreForm.userStores[0] != null) { // there is more than 1 user store
            for (var j = 0; j < document.userStoreForm.userStores.length; j++) {
                selected = document.userStoreForm.userStores[j].checked;
                if (selected) break;
            }
        } else if (document.userStoreForm.userStores != null) { // only 1 user store
            selected = document.userStoreForm.userStores.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.user.stores.to.be.deleted"/>');
            return;
        }
        if (allUserStoresSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.user.stores.prompt"/>", function () {

            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.user.stores.on.page.prompt"/>", function () {
                var checkedList = new Array();
                jQuery("input:checked").each(function () {
                    checkedList.push($(this).val());
                });

//                var orderList = new Array();
//                jQuery('.valueCell a').each(function () {
//                    orderList.push($(this).html().trim());
//                });
                document.userStoreForm.action = "remove-userstore.jsp?checkedList=" + checkedList;
                document.userStoreForm.submit();
            });
        }
    }


    function edit(domain, className) {
        document.userStoreForm.action = "userstore-config.jsp?domain=" + domain + "&className=" + className;
        document.userStoreForm.submit();

    }

    function enable(domain) {
        location.href = "enable-disable-userstores.jsp?domain=" + domain + "&action=enable";

    }

    function disable(domain) {
        location.href = "enable-disable-userstores.jsp?domain=" + domain + "&action=disable";

    }

</script>


<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>


<div id="middle">
    <h2><fmt:message key='userstore.config'/></h2>

    <div id="workArea">



        <table style="margin-top:10px;margin-bottom:10px">
            <tbody>
            <tr>
                <td>
                    <a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;" href="#">
                        <fmt:message key="selectAllInPage"/></a>
                    &nbsp; | &nbsp;</td><td><a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;"
                                               href="#"><fmt:message key="selectNone"/></a>
            </td>
            <td width="20%">&nbsp;</td>
                <td>
                    <a onclick="deleteUserStores();return false;" href="#" class="icon-link"
                       style="background-image: url(images/delete.gif);"><fmt:message key="delete"/></a>
                </td>
            </tr>
            </tbody>
        </table>

        <form id="userStoreForm" action="" name="userStoreForm" method="post">
            <table style="width: 100%" id="dataTable" class="styledLeft">
                <thead>
                <tr>
                    <th colspan="5"><fmt:message key='available.secondary.user.stores'/></th>
                </tr>
                </thead>
                <tbody>
                <% if (userStoreDTOs[0] != null) {
                    for (UserStoreDTO userstoreDTO : userStoreDTOs) {
                        String className = userstoreDTO.getClassName();
                        String description = userstoreDTO.getDescription();
                        String domainId = userstoreDTO.getDomainId();
                        Boolean isDisabled = userstoreDTO.getDisabled();
                        if (className == null) {
                            className = "";
                        }
                        if (description == null) {
                            description = "";
                        }
                        if (domainId == null) {
                            domainId = "";
                        }

                %>
                <tr id=<%=Encode.forHtmlAttribute(domainId)%>>
                    <td style="width: 5%;margin-top:10px;">
                        <input type="checkbox" name="userStores"
                               value="<%=Encode.forHtmlAttribute(domainId)%>"
                               onclick="resetVars()"
                               class="chkBox"/>
                    </td>
                    <td style="width: 10%;margin-top:10px;">
                        <a><%=Encode.forHtml(domainId)%>
                        </a>
                    </td>
                    <td style="width: 40%;margin-top:10px;">
                        <a><%=Encode.forHtml(className)%>
                        </a>
                    </td>
                    <td style="width: 45%;margin-top:10px;">
                        <a title="<fmt:message key='edit.userstore'/>"
                           onclick="edit('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(domainId))%>','<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(className))%>');"
                           href="#" style="background-image: url(images/edit.gif);" class="icon-link">
                            <fmt:message key='edit.userstore'/></a>
                        <% if (!isDisabled) { %>
                        <a title="<fmt:message key='disable.userstore'/>"
                           onclick="disable('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(domainId))%>');return false;"
                           href="#" style="background-image: url(images/disable.gif);" class="icon-link">
                            <fmt:message key='disable.userstore'/></a>
                        <% } else { %>
                        <a title="<fmt:message key='enable.userstore'/>"
                           onclick="enable('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(domainId))%>');return false;"
                           href="#" style="background-image: url(images/enable.gif);" class="icon-link">
                            <fmt:message key='enable.userstore'/></a>
                        <%
                            }

                        %>
                    </td>
                </tr>
                <% }
                } else { %>
                <tr>
                    <td colspan="2"><fmt:message key='no.secondary.user.stores.defined'/></td>
                </tr>
                <%}%>
                </tbody>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>

