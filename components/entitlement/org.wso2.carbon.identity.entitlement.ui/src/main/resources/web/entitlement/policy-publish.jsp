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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder" %>
<%@page import="org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.util.ClientUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%

    String[] subscriberIds = null;
    session.removeAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_MODULE);

    EntitlementPolicyAdminServiceClient client = null;

    int numberOfPages = 0;
    boolean isPaginated = Boolean.parseBoolean(request.getParameter("isPaginated"));
    String subscriberSearchString = request.getParameter("subscriberSearchString");
    if (subscriberSearchString == null) {
        subscriberSearchString = "*";
    } else {
        subscriberSearchString = subscriberSearchString.trim();
    }
    String paginationValue = "isPaginated=true&subscriberSearchString=" + subscriberSearchString;

    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
        // ignore
    }

    String selectedModule = request.getParameter("selectedModule");
    boolean update = Boolean.parseBoolean(request.getParameter("update"));
    PublisherPropertyDTO[] propertyDTOs = (PublisherPropertyDTO[]) session.
            getAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_PROPERTY);

    session.removeAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_PROPERTY);

    if (propertyDTOs != null) {
        for (PublisherPropertyDTO dto : propertyDTOs) {
            String value = request.getParameter(dto.getId());
            if (value != null && value.trim().length() > 0) {
                dto.setValue(value);
            }
        }
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                                                                                   CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    try {

        if (client == null) {

            client = new EntitlementPolicyAdminServiceClient(cookie,
                                                             serverURL, configContext);
            session.setAttribute(EntitlementPolicyConstants.ENTITLEMENT_ADMIN_CLIENT, client);
        }


        if (selectedModule != null && selectedModule.trim().length() > 0 && propertyDTOs != null
            && propertyDTOs.length > 0) {
            PublisherDataHolder holder = new PublisherDataHolder();
            holder.setModuleName(selectedModule);
            holder.setPropertyDTOs(propertyDTOs);
            client.updateSubscriber(holder, update);

        }
        int itemsPerPageInt = EntitlementPolicyConstants.DEFAULT_ITEMS_PER_PAGE;
        // as these are just strings, get all values in to UI and the do the pagination
        String[] allSubscriberIds = (String[])session.getAttribute("subscriberIds");
        if(allSubscriberIds == null || !isPaginated){
            allSubscriberIds = client.getSubscriberIds(subscriberSearchString);
            session.setAttribute("subscriberIds", allSubscriberIds);
        }
        if (allSubscriberIds != null) {
            numberOfPages = (int) Math.ceil((double) allSubscriberIds.length / itemsPerPageInt);
            subscriberIds = ClientUtil.doPagingForStrings(pageNumberInt, itemsPerPageInt, allSubscriberIds);
        }
    } catch (Exception e) {
%>

<script type="text/javascript">
    CARBON.showErrorDialog('<%=Encode.forJavaScript(e.getMessage())%>', function () {
        location.href = "policy-publish.jsp";
    });
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
        label="policy.publisher"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    var allSubscribersSelected = false;

    function doCancel() {
        location.href = 'index.jsp';
    }

    function editSubscriber(subscriber) {
        location.href = "add-subscriber.jsp?view=false&subscriberId=" + subscriber;
    }

    function viewSubscriber(subscriber) {
        location.href = "add-subscriber.jsp?view=true&subscriberId=" + subscriber;
    }

    function viewSubscriberStatus(subscriber) {
        location.href = "show-subscriber-status.jsp?subscriberId=" + subscriber;
    }

    function publishToSubscriber() {
        var selected = false;

        if (document.policyForm.subscribers == null) {
            CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
            return;
        }

        if (document.policyForm.subscribers[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                selected = document.policyForm.subscribers[j].checked;
                if (selected) break;
            }
        } else if (document.policyForm.subscribers != null) { // only 1 policy
            selected = document.policyForm.subscribers.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.subscriber.to.be.published"/>');
            return;
        }
        if (allSubscribersSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribersList.prompt"/>", function () {
                document.policyForm.action = "publish-finish-ajaxprocessor.jsp";
                document.policyForm.submit();
            }, null);
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.selected.subscriber.prompt"/>", function () {
                document.policyForm.action = "publish-finish-ajaxprocessor.jsp";
                document.policyForm.submit();
            }, null);
        }

    }

    function resetVars() {
        allSubscribersSelected = false;

        var isSelected = false;
        if (document.policyForm.subscribers[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                if (document.policyForm.subscribers[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.policyForm.subscribers != null) { // only 1 service
            if (document.policyForm.subscribers.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function deleteServices() {
        var selected = false;
        if (document.policyForm.subscribers[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                selected = document.policyForm.subscribers[j].checked;
                if (selected) break;
            }
        } else if (document.policyForm.subscribers != null) { // only 1 policy
            selected = document.policyForm.subscribers.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.subscribers.to.be.deleted"/>');
            return;
        }
        if (allSubscribersSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.subscribers.prompt"/>", function () {
                document.policyForm.action = "remove-subscriber-ajaxprocessor.jsp";
                document.policyForm.submit();
            }, null);
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.subscribers.on.page.prompt"/>", function () {
                document.policyForm.action = "remove-subscriber-ajaxprocessor.jsp";
                document.policyForm.submit();
            }, null);
        }
    }

    function selectAllInThisPage(isSelected) {

        allSubscribersSelected = false;
        if (document.policyForm.subscribers != null &&
            document.policyForm.subscribers[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                    document.policyForm.subscribers[j].checked = true;
                }
            } else {
                for (j = 0; j < document.policyForm.subscribers.length; j++) {
                    document.policyForm.subscribers[j].checked = false;
                }
            }
        } else if (document.policyForm.subscribers != null) { // only 1 service
            document.policyForm.subscribers.checked = isSelected;
        }
        return false;
    }

    function searchService() {
        document.searchForm.submit();
    }

</script>

<div id="middle">

    <h2><fmt:message key="policy.publisher"/></h2>

    <div id="workArea">
        <table style="border:none; margin-bottom:10px">
            <tr>
                <td>
                    <div style="height:30px;">
                        <a href="javascript:document.location.href='add-subscriber.jsp'"
                           class="icon-link"
                           style="background-image:url(images/add.gif);"><fmt:message
                                key='add.subscriber'/></a>
                    </div>
                </td>
            </tr>

            <tr>
                <td>
                    <form action="policy-publish.jsp" name="searchForm" method="post">
                        <table style="border:0;
                                                !important margin-top:10px;margin-bottom:10px;">
                            <tr>
                                <td>
                                    <table style="border:0; !important">
                                        <tbody>
                                        <tr style="border:0; !important">
                                            <td style="border:0; !important">
                                                <nobr>
                                                    <fmt:message key="enter.subscriber.search"/>
                                                    <input type="text" name="subscriberSearchString"
                                                           value="<%=subscriberSearchString != null ?
                                                           Encode.forHtmlAttribute(subscriberSearchString) : "" %>"/>&nbsp;
                                                </nobr>
                                            </td>
                                            <td style="border:0; !important">
                                                <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                                                   onclick="searchService(); return false;"
                                                   alt="<fmt:message key="search"/>"></a>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </form>
                </td>
            </tr>
            <tr>
                <td>
                    <a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;"
                       href="#"><fmt:message key="selectAllInPage"/></a>
                    &nbsp;<b>|</b>&nbsp;
                 <a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;"
                       href="#"><fmt:message key="selectNone"/></a>
                </td>
                <td>
                    <a onclick="deleteServices();return false;" class="icon-link"
                       style="background-image:url(images/delete.gif);" href="#"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>

        </table>

        <form action="" name="policyForm" method="post">
        <table style="width: 100%" id="dataTable" class="styledLeft">
            <thead>
            <tr>
                <th colspan='2'><fmt:message key='subscriber.name'/></th>
                <th><fmt:message key='action'/></th>
            </tr>
            </thead>
            <%
                if (subscriberIds != null && subscriberIds.length > 0) {
                    for (String subscriber : subscriberIds) {
                        if (subscriber != null && subscriber.trim().length() > 0 ) {
            %>
            <tr>
                <td width="10px" style="text-align:center; !important">
                    <input type="checkbox" name="subscribers"
                           value="<%=Encode.forHtmlAttribute(subscriber)%>"
                           onclick="resetVars()" class="chkBox"/>
                </td>
                <td><%=Encode.forHtmlContent(subscriber)%></td>
                <td>
                    <a onclick="editSubscriber('<%=Encode.forJavaScriptAttribute(subscriber)%>');return false;"
                       href="#" style="background-image: url(images/edit.gif);"
                       class="icon-link">
                        <fmt:message key='edit'/></a>
                    <a onclick="viewSubscriber('<%=Encode.forJavaScriptAttribute(subscriber)%>');return false;"
                       href="#" style="background-image: url(images/view.gif);"
                       class="icon-link">
                        <fmt:message key='view'/></a>
                    <a onclick="viewSubscriberStatus('<%=Encode.forJavaScriptAttribute(subscriber)%>');return false;"
                       href="#" style="background-image: url(images/view.gif);"
                       class="icon-link">
                        <fmt:message key='view.status'/></a>

                </td>
            </tr>
            <%
                        }
                    }
            %>
            <%
                }
            %>
        </table>
        <carbon:paginator pageNumber="<%=pageNumberInt%>"
                          numberOfPages="<%=numberOfPages%>"
                          page="policy-publish.jsp"
                          pageNumberParameterName="pageNumber"
                          parameters="<%=Encode.forHtmlAttribute(paginationValue)%>"
                          resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"/>
        </form>
    </div>
</div>
</fmt:bundle>