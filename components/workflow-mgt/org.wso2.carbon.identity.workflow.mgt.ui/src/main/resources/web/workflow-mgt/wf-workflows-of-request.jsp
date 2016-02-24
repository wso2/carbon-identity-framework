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

<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequestAssociation" %>
<%@ page import="org.owasp.encoder.Encode" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String requestId = request.getParameter(WorkflowUIConstants.PARAM_REQUEST_ID);
    WorkflowAdminServiceClient client = null;
    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    String pageNumber = request.getParameter(WorkflowUIConstants.PARAM_PAGE_NUMBER);
    int pageNumberInt = 0;
    int numberOfPages = 0;
    String forwardTo = null;
    if (pageNumber != null) {
        try {
            pageNumberInt = Integer.parseInt(pageNumber);
        } catch (NumberFormatException ignored) {
            //not needed here since it's defaulted to 0
        }
    }

    String paginationValue = "region=region1";
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);
    WorkflowRequestAssociation[] workflowRequestAssociationDTOs = client.getWorkflowsOfRequest(requestId);
    WorkflowRequestAssociation[] workflowsToDisplay;
    numberOfPages = (int) Math.ceil((double) workflowRequestAssociationDTOs.length / WorkflowUIConstants.RESULTS_PER_PAGE);

    int startIndex = pageNumberInt * WorkflowUIConstants.RESULTS_PER_PAGE;
    int endIndex = (pageNumberInt + 1) * WorkflowUIConstants.RESULTS_PER_PAGE;
    workflowsToDisplay = new WorkflowRequestAssociation[WorkflowUIConstants.RESULTS_PER_PAGE];

    for (int i = startIndex, j = 0; i < endIndex && i < workflowRequestAssociationDTOs.length; i++, j++) {
        workflowsToDisplay[j] = workflowRequestAssociationDTOs[i];
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="view"
                       resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <link rel="stylesheet" href="/carbon/styles/css/main.css">

    <div id="middle">
        <h2><fmt:message key='workflow.list'/></h2>

        <div id="workArea">
            <table class="styledLeft" id="servicesTable">
                <thead>
                <tr>
                    <th><fmt:message key="workflow.workflowId"/></th>
                    <th><fmt:message key="workflow.name"/></th>
                    <th><fmt:message key="workflow.updatedAt"/></th>
                    <th><fmt:message key="workflow.status"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (WorkflowRequestAssociation workflow : workflowRequestAssociationDTOs) {
                %>
                <tr>
                    <td><%=Encode.forHtmlContent(workflow.getWorkflowId())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflow.getWorkflowName())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflow.getLastUpdatedTime())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflow.getStatus())%>
                    </td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="wf-workflows-of-request.jsp"
                              pageNumberParameterName="<%=WorkflowUIConstants.PARAM_PAGE_NUMBER%>"
                              resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>
</fmt:bundle>