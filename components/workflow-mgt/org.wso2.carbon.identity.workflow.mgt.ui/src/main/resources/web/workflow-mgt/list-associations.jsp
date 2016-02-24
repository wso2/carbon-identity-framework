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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association" %>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%

    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    WorkflowAdminServiceClient client;
    String forwardTo = null;
    Association[] associationsToDisplay = new Association[0];
    String paginationValue = "region=region1&item=association_list_menu";

    String pageNumber = request.getParameter(WorkflowUIConstants.PARAM_PAGE_NUMBER);
    int pageNumberInt = 0;
    int numberOfPages = 0;
    Association[] associations = null;

    if (pageNumber != null) {
        try {
            pageNumberInt = Integer.parseInt(pageNumber);
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

        associations = client.listAllAssociations();
        if (associations != null) {
            numberOfPages = (int) Math.ceil((double) associations.length / WorkflowUIConstants.RESULTS_PER_PAGE);
            int startIndex = pageNumberInt * WorkflowUIConstants.RESULTS_PER_PAGE;
            int endIndex = (pageNumberInt + 1) * WorkflowUIConstants.RESULTS_PER_PAGE;
            associationsToDisplay = new Association[WorkflowUIConstants.RESULTS_PER_PAGE];

            for (int i = startIndex, j = 0; i < endIndex && i < associations.length; i++, j++) {
                associationsToDisplay[j] = associations[i];
            }
        }
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

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript">
        function removeAssociation(id, name) {
            function doDelete() {
                location.href = 'update-association-finish.jsp?<%=WorkflowUIConstants.PARAM_ACTION%>=' +
                '<%=WorkflowUIConstants.ACTION_VALUE_DELETE%>&<%=WorkflowUIConstants.PARAM_ASSOCIATION_ID%>=' + id;
            }
            CARBON.showConfirmationDialog('<fmt:message key="confirmation.association.delete"/> ' + name + '?',
                    doDelete, null);
        }

        function changeState(id, name, action) {
            function onChangeState() {
                location.href = 'update-association-finish.jsp?<%=WorkflowUIConstants.PARAM_ACTION%>=' +
                                action + '&<%=WorkflowUIConstants.PARAM_ASSOCIATION_ID%>=' + id;
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

        <div id="workArea">
            <a title="<fmt:message key='workflow.service.association.add'/>"
               href="#" style="background-image: url(images/add.png);" onclick="addAssociation();return false;"
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
                        for (Association association : associationsToDisplay) {
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
                               '<%=Encode.forJavaScriptAttribute(association.getAssociationName())%>','<%=WorkflowUIConstants.ACTION_VALUE_DISABLE%>');return false;"
                       class="icon-link" href="#" style="background-image: url(images/disable.gif);"><fmt:message
                            key='disable'/></a>

                    <% } else { %>

                    <a title="<fmt:message key='workflow.service.association.state.enable'/>"
                       onclick="changeState('<%=Encode.forJavaScriptAttribute(association.getAssociationId())%>',
                               '<%=Encode.forJavaScriptAttribute(association.getAssociationName())%>','<%=WorkflowUIConstants.ACTION_VALUE_ENABLE%>');return false;"
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
                               '<%=Encode.forJavaScriptAttribute(association.getAssociationName())%>');return false;"
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