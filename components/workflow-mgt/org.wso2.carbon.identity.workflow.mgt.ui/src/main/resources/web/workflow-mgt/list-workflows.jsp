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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.base.ServerConfiguration" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<%

    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    WorkflowAdminServiceClient client;
    String forwardTo = null;
    String resultsPerPage = ServerConfiguration.getInstance().getFirstProperty(WorkflowUIConstants.RESULTS_PER_PAGE_PROPERTY);
    String filterString = request.getParameter(WorkflowUIConstants.WF_NAME_FILTER);
    if (!StringUtils.isNotBlank(filterString)) {
        filterString =  WorkflowUIConstants.DEFAULT_FILTER;
    } else {
        filterString = filterString.trim();
    }
    String paginationValue;
    if (StringUtils.isNotBlank(filterString)) {
        paginationValue = String.format(WorkflowUIConstants.PAGINATION_VALUE_WITH_FILTER, WorkflowUIConstants.DEFAULT_REGION_VALUE, WorkflowUIConstants.DEFAULT_WF_ITEM_VALUE, filterString);
    } else {
        paginationValue = String.format(WorkflowUIConstants.PAGINATION_VALUE,  WorkflowUIConstants.DEFAULT_REGION_VALUE, WorkflowUIConstants.DEFAULT_WF_ITEM_VALUE);
    }
    String pageNumber = request.getParameter(WorkflowUIConstants.PARAM_PAGE_NUMBER);
    int pageNumberInt = 0;
    int numberOfPages = 0;
    int offset = 0;
    int numberOfWorkflows = 0;
    int resultsPerPageInt = WorkflowUIConstants.DEFAULT_RESULTS_PER_PAGE;
    WorkflowWizard[] workflows = null;

    //clear any unnecessary session data
    if (session.getAttribute(WorkflowUIConstants.ATTRIB_WORKFLOW_WIZARD) != null) {
        session.removeAttribute(WorkflowUIConstants.ATTRIB_WORKFLOW_WIZARD);
    }
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
        numberOfWorkflows = client.getWorkflowsCount(filterString);
        workflows = client.listPaginatedWorkflows(resultsPerPageInt, offset, filterString);
        numberOfPages = (int) Math.ceil((double) numberOfWorkflows / resultsPerPageInt);
    } catch (Exception e) {
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
        function doCancel() {
            location.href = 'list-workflows.jsp';
        }
        function removeWorkflow(id, name) {
            function doDelete() {

                $.ajax({
                    type: 'POST',
                    url: 'finish-wf-wizard-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: '<%=WorkflowUIConstants.PARAM_ACTION%>=' + '<%=WorkflowUIConstants.ACTION_VALUE_DELETE%>' +
                    '&' + '<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>=' + id,
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("list-workflows.jsp");
                        }
                    }
                });
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirmation.workflow.delete"/> ' + name + '?',
                    doDelete, null);
        }

        function editWorkflow(id) {
            location.href = 'add-wf-wizard.jsp?<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>=' + id;
        }
    </script>

    <div id="middle">
        <h2><fmt:message key='workflow.list'/></h2>

        <table style="border:none; !important margin-top:10px;margin-left:5px;">
            <tr>
                <td>
                    <form action="list-workflows.jsp" name="searchForm" method="post">
                        <table style="border:0;!important margin-top:10px;margin-bottom:10px;">
                            <tr>
                                <td>
                                    <table style="border:0; !important">
                                        <tbody>
                                            <tr style="border:0; !important">
                                                <td style="border:0; !important">
                                                    <fmt:message key="workflow.service.workflow.add.name.pattern"/>
                                                    <input style="margin-left:30px; !important"
                                                           type="text" name="<%=WorkflowUIConstants.WF_NAME_FILTER%>"
                                                           value="<%=filterString != null ?
                                                       Encode.forHtmlAttribute(filterString) : Encode.forHtmlAttribute(WorkflowUIConstants.DEFAULT_FILTER) %>"/>&nbsp;
                                                    <input class="button" type="submit"
                                                           value="<fmt:message key="workflow.service.workflow.search"/>"/>
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

            <table class="styledLeft" id="servicesTable">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="workflow.name"/></th>
                    <th width="30%"><fmt:message key="workflow.description"/></th>
                    <th width="20%"><fmt:message key="workflow.template"/></th>
                    <th width="20%"><fmt:message key="workflow.template.impl"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (workflows != null && workflows.length > 0) {
                        for (WorkflowWizard workflow : workflows) {
                            if (workflow != null) {

                %>
                <tr>
                    <td>
                        <a href="view-workflow.jsp?<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>=<%=Encode.forHtmlAttribute(workflow.getWorkflowId())%>">
                        <%=Encode.forHtmlContent(workflow.getWorkflowName())%>
                        </a>
                    </td>
                    <td><%=workflow.getWorkflowDescription() == null ? "" : Encode.forHtmlContent(workflow.getWorkflowDescription())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflow.getTemplate().getName())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflow.getWorkflowImpl().getWorkflowImplName())%>
                    </td>
                    <td>
                        <%if(CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/manage/identity/workflow/definition/update")) {%>
                        <a title="<fmt:message key='workflow.service.workflow.edit.title'/>"
                           onclick="editWorkflow('<%=Encode.forJavaScriptAttribute(workflow.getWorkflowId())%>');
                                   return false;"
                           href="#" style="background-image: url(images/edit.gif);"
                           class="icon-link"><fmt:message key='edit'/></a>
                        <%}
                        if (CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/manage/identity/workflow/definition/delete")) {%>
                        <a title="<fmt:message key='workflow.service.workflow.delete.title'/>"
                           onclick="removeWorkflow('<%=Encode.forJavaScriptAttribute(workflow.getWorkflowId())%>','<%=Encode.forJavaScriptAttribute(workflow.getWorkflowName())%>');
                                   return false;"
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
                    <td colspan="5"><i>No workflows found.</i></td>
                </tr>
                <% }
                %>
                </tbody>
            </table>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="list-workflows.jsp"
                              pageNumberParameterName="<%=WorkflowUIConstants.PARAM_PAGE_NUMBER%>"
                              resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>

</fmt:bundle>