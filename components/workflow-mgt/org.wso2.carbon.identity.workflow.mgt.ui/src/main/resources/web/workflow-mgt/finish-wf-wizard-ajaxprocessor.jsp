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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.base.IdentityValidationUtil" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>

<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.util.WorkflowUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>


<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());

    String action = request.getParameter(WorkflowUIConstants.PARAM_ACTION);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    WorkflowAdminServiceClient client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);

    String reqPath = request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH);


    String forwardTo = "list-workflows.jsp";

    if (WorkflowUIConstants.ACTION_VALUE_ADD.equals(action)) {
        String requestToken = request.getParameter(WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN);
        if(StringUtils.isBlank(requestToken) || session.getAttribute(requestToken)==null){
            throw new WorkflowAdminServiceWorkflowException("This page is expired or can not access from this URL");
        }

        WorkflowWizard workflowWizard = (WorkflowWizard)session.getAttribute(requestToken);
        WorkflowUIUtil.loadWorkflowImplParameters(request.getParameterMap(),workflowWizard);
        if (StringUtils.isNotBlank(reqPath) && IdentityValidationUtil
                .isValidOverBlackListPatterns(reqPath,
                                              IdentityValidationUtil.ValidatorPattern.URI_RESERVED_EXISTS.name())) {
            forwardTo = reqPath +".jsp?wizard=finish&" + WorkflowUIConstants.PARAM_WORKFLOW_NAME + "=" +workflowWizard.getWorkflowName() ;
        }
        try {
            client.addWorkflow(workflowWizard);
        } catch (Exception e) {
            String message = resourceBundle.getString("workflow.error.when.adding.workflow");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            forwardTo = "../admin/error.jsp";
        }

    } else if (WorkflowUIConstants.ACTION_VALUE_DELETE.equals(action)) {
        String workflowId = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_ID);
        if (StringUtils.isNotBlank(workflowId)) {
            try {
                client.deleteWorkflow(workflowId);
            } catch (Exception e) {
                String message = resourceBundle.getString("workflow.error.when.deleting.workflow");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                forwardTo = "../admin/error.jsp";
            }
        }
    } else if (WorkflowUIConstants.ACTION_VALUE_DELETE_ASSOCIATION.equals(action)) {
        String associationId = request.getParameter(WorkflowUIConstants.PARAM_ASSOCIATION_ID);
        String workflowId = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_ID);
        try {
            client.deleteAssociation(associationId);
            forwardTo = "view-workflow.jsp?" + WorkflowUIConstants.PARAM_WORKFLOW_ID + "=" + workflowId;
        } catch (Exception e) {
            String message = resourceBundle.getString("workflow.error.when.deleting.association");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            forwardTo = "../admin/error.jsp";
        }
    }
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
