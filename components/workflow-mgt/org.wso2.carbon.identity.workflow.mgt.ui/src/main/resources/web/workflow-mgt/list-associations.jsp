<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.base.ServerConfiguration" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<%

    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    WorkflowAdminServiceClient client;
    String forwardTo = null;
    String resultsPerPage = ServerConfiguration.getInstance().getFirstProperty(WorkflowUIConstants.RESULTS_PER_PAGE_PROPERTY);
    String filterString = request.getParameter(WorkflowUIConstants.ASSOC_NAME_FILTER);

    if (!StringUtils.isNotBlank(filterString)) {
        filterString =  WorkflowUIConstants.DEFAULT_FILTER;
    } else {
        filterString = filterString.trim();
    }

    String paginationValue;
    if (StringUtils.isNotBlank(filterString)) {
        paginationValue = String.format(WorkflowUIConstants.PAGINATION_VALUE_WITH_FILTER,
        WorkflowUIConstants.DEFAULT_REGION_VALUE, WorkflowUIConstants.DEFAULT_ASSOC_ITEM_VALUE, filterString);
    } else {
        paginationValue = String.format(WorkflowUIConstants.PAGINATION_VALUE,
        WorkflowUIConstants.DEFAULT_REGION_VALUE, WorkflowUIConstants.DEFAULT_ASSOC_ITEM_VALUE);
    }

    String pageNumber = request.getParameter(WorkflowUIConstants.PARAM_PAGE_NUMBER);
    int pageNumberInt = 0;
    int numberOfPages = 0;
    int offset = 0;
    int numberOfAssociations;
    int resultsPerPageInt = WorkflowUIConstants.DEFAULT_RESULTS_PER_PAGE;
    Association[] associations = null;

    if (StringUtils.isNotBlank(resultsPerPage)) {
        try {
            resultsPerPageInt = Integer.parseInt(resultsPerPage);
        } catch (NumberFormatException ignored) {
            //not needed to handle here, since the default value is already set.
        }
    }

    if (StringUtils.isNotBlank(pageNumber)) {
        try {
            pageNumberInt = Integer.parseInt(pageNumber);
            offset = ((pageNumberInt) * resultsPerPageInt);
        } catch (NumberFormatException ignored) {
            //not needed here since it's defaulted to 0
        }
    }

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);
        numberOfAssociations = client.getAssociationsCount(filterString);
        associations = client.listPaginatedAssociations(resultsPerPageInt, offset, filterString);
        numberOfPages = (int) Math.ceil((double) numberOfAssociations / resultsPerPageInt);
    } catch (AxisFault e) {
        String message = resourceBundle.getString("workflow.error.when.initiating.service.client");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>

<%
    if (forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="workflow.mgt"
                       resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript">
        function removeAssociation(id, name, pageNumberInt, filterString) {
            function doDelete() {

                $.ajax({
                    type: 'POST',
                    url: 'update-association-finish-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: '<%=WorkflowUIConstants.PARAM_ACTION%>=' + '<%=WorkflowUIConstants.ACTION_VALUE_DELETE%>&' +
                    '<%=WorkflowUIConstants.PARAM_ASSOCIATION_ID%>=' + id,
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("list-associations.jsp?pageNumber=" +
                                pageNumberInt.toString() + "&region=region1&item=associations_list&filterString=" + filterString);
                        }
                    }
                });
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirmation.association.delete"/> ' + name + '?',
                    doDelete, null);
        }

        function changeState(id, name, action, pageNumberInt, filterString) {
            function onChangeState() {

                $.ajax({
                    type: 'POST',
                    url: 'update-association-finish-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: '<%=WorkflowUIConstants.PARAM_ACTION%>=' + action + '&' +
                    '<%=WorkflowUIConstants.PARAM_ASSOCIATION_ID%>=' + id,
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("list-associations.jsp?pageNumber=" +
                                pageNumberInt.toString() + "&region=region1&item=associations_list&filterString=" + filterString);
                        }
                    }
                });
            }

            if(action == '<%=WorkflowUIConstants.ACTION_VALUE_ENABLE%>'){
                CARBON.showConfirmationDialog('<fmt:message key="confirmation.association.enable"/> ',
                                              onChangeState, null);
            }else{
                CARBON.showConfirmationDialog('<fmt:message key="confirmation.association.disable"/> ',
                                              onChangeState, null);
            }

        }


        function addAssociation() {
            window.location = "add-association.jsp";
        }
    </script>

    <div id="middle">
        <h2><fmt:message key='workflow.association.list'/></h2>

        <table style="border:none; !important margin-top:10px;margin-left:5px;">
            <tr>
                <td>
                    <form action="list-associations.jsp" name="searchForm" method="post">
                        <table style="border:0;!important margin-top:10px;margin-bottom:10px;">
                            <tr>
                                <td>
                                    <table style="border:0; !important">
                                        <tbody>
                                            <tr style="border:0; !important">
                                                <td style="border:0; !important">
                                                    <fmt:message key="workflow.service.association.add.name.pattern"/>
                                                    <input style="margin-left:30px; !important"
                                                           type="text" name="<%=WorkflowUIConstants.ASSOC_NAME_FILTER%>"
                                                           value="<%=filterString != null ?
                                                        Encode.forHtmlAttribute(filterString) : Encode
                                                        .forHtmlAttribute(WorkflowUIConstants.DEFAULT_FILTER) %>"/>&nbsp;
                                                    <input class="button" type="submit"
                                                           value="<fmt:message key="workflow.service.association.search"/>"/>
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
        </table>

        <div id="workArea">
            <a title="<fmt:message key='workflow.service.association.add'/>"
               href="#" style="background-image: url(images/add.png); margin-top: -10px;" onclick="addAssociation();return false;"
               class="icon-link"><fmt:message key='workflow.service.association.add'/></a>
            <table class="styledLeft" id="servicesTable">
                <thead>
                <tr>
                    <th width="30%"><fmt:message key="workflow.service.association.name"/></th>
                    <th width="30%"><fmt:message key="workflow.service.associate.event"/></th>
                    <th width="15%"><fmt:message key="workflow.name"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (associations != null && associations.length > 0) {
                        for (Association association : associations) {
                            if (association != null) {
                %>
                <td>
                    <%=Encode.forHtmlContent(association.getAssociationName())%>
                </td>
                <td><%=Encode.forHtmlContent(association.getEventName())%>
                </td>
                <td><%=Encode.forHtmlContent(association.getWorkflowName())%>
                </td>
                <td>
                    <% if(CarbonUIUtil.isUserAuthorized(request,
                            "/permission/admin/manage/identity/workflow/association/update")) {
                        if (association.getEnabled()) { %>

                    <a title="<fmt:message key='workflow.service.association.state.disable'/>"
                       onclick="changeState('<%=Encode.forJavaScriptAttribute(association.getAssociationId())%>',
                               '<%=Encode.forJavaScriptAttribute(association.getAssociationName())%>','<%=WorkflowUIConstants.ACTION_VALUE_DISABLE%>',
                               '<%=pageNumberInt%>', '<%=Encode.forJavaScriptAttribute(filterString)%>');return false;"
                       class="icon-link" href="#" style="background-image: url(images/disable.gif);"><fmt:message
                            key='disable'/></a>

                    <% } else { %>

                    <a title="<fmt:message key='workflow.service.association.state.enable'/>"
                       onclick="changeState('<%=Encode.forJavaScriptAttribute(association.getAssociationId())%>',
                               '<%=Encode.forJavaScriptAttribute(association.getAssociationName())%>','<%=WorkflowUIConstants.ACTION_VALUE_ENABLE%>',
                               '<%=pageNumberInt%>', '<%=Encode.forJavaScriptAttribute(filterString)%>');return false;"
                       class="icon-link" href="#" style="background-image: url(images/enable.gif);"><fmt:message
                            key='enable'/></a>

                    <%
                        }
                        }
                        if (CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/manage/identity/workflow/association/delete")) {
                    %>
                    <a title="<fmt:message key='workflow.service.association.delete.title'/>"
                       onclick="removeAssociation('<%=Encode.forJavaScriptAttribute(association.getAssociationId())%>',
                               '<%=Encode.forJavaScriptAttribute(association.getAssociationName())%>',
                               '<%=pageNumberInt%>', '<%=Encode.forJavaScriptAttribute(filterString)%>');return false;"
                       href="#" style="background-image: url(images/delete.gif);"
                       class="icon-link"><fmt:message key='delete'/></a>
                    <%}%>
                </td>
                </tr>
                <%
                            }
                        }
                    } else {%>
                <tr>
                    <td colspan="4"><i>No associations found.</i></td>
                </tr>
                <% }
                %>
                </tbody>
            </table>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="list-associations.jsp"
                              pageNumberParameterName="<%=WorkflowUIConstants.PARAM_PAGE_NUMBER%>"
                              resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>
</fmt:bundle>