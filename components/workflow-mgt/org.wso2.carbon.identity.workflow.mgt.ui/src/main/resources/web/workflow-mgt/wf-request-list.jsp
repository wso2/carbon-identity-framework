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
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>

<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%! public static final String ADMIN_ERROR_PAGE = "../admin/error.jsp";
    public static final String ALL_TASKS = "allTasks";
    public static final String CREATED_AT = "createdAt";
    private static final String FAILED_STATUS = "FAILED";
    private static final String REJECTED_STATUS = "REJECTED";
    private static final String APPROVED_STATUS = "APPROVED";
    private static final String PENDING_STATUS = "PENDING";
    private static final String REGION1_PAGINATION = "region=region1";
    private static final String I18N_RESOURCDE_FILE = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    private static final String LOGGED_USER = "logged-user";
    private static final String TIME_CATEGORY_TO_FILTER = "timeCategoryToFilter";
    private static final String CREATED_AT_TO = "createdAtTo";
    private static final String CREATED_AT_FROM = "createdAtFrom";
    private static final String REQUEST_STATUS_FILTER = "requestStatusFilter";
    private static final String REQUEST_TYPE_FILTER = "requestTypeFilter";
%>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String taskTypeFilter = request.getParameter(REQUEST_TYPE_FILTER);
    String statusToFilter = request.getParameter(REQUEST_STATUS_FILTER);
    String lowerBound = request.getParameter(CREATED_AT_FROM);
    String upperBound = request.getParameter(CREATED_AT_TO);
    String timeFilterCategory = request.getParameter(TIME_CATEGORY_TO_FILTER);
    String loggedUser = (String) session.getAttribute(LOGGED_USER);
    String bundle = I18N_RESOURCDE_FILE;
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    WorkflowAdminServiceClient client;
    String forwardTo = null;
    WorkflowRequest[] associationToDisplay = new WorkflowRequest[0];
    WorkflowRequest[] requestList = null;
    String paginationValue = REGION1_PAGINATION;

    String pageNumber = request.getParameter(WorkflowUIConstants.PARAM_PAGE_NUMBER);
    int pageNumberInt = 0;
    int numberOfPages = 0;
    WorkflowEvent[] workflowEvents;
    Map<String, List<WorkflowEvent>> events = new HashMap<String, List<WorkflowEvent>>();

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

        if (taskTypeFilter == null) {
            if ((String) session.getAttribute(REQUEST_TYPE_FILTER) != null) {
                taskTypeFilter = (String) session.getAttribute(REQUEST_TYPE_FILTER);
            } else {
                 taskTypeFilter = StringUtils.EMPTY;
            }
        }
        if (statusToFilter == null) {
             if ((String) session.getAttribute(REQUEST_STATUS_FILTER) != null) {
                statusToFilter = (String) session.getAttribute(REQUEST_STATUS_FILTER);
            } else {
                statusToFilter = StringUtils.EMPTY;
            }
        }
        if (lowerBound == null) {
            if ((String) session.getAttribute(CREATED_AT_FROM) != null) {
                lowerBound = (String) session.getAttribute(CREATED_AT_FROM);
            } else {
                lowerBound = StringUtils.EMPTY;
            }
        }
        if (upperBound == null) {
            if ((String) session.getAttribute(CREATED_AT_TO) != null) {
                upperBound = (String) session.getAttribute(CREATED_AT_TO);
            } else {
                upperBound = StringUtils.EMPTY;
            }
        }
        if (timeFilterCategory == null) {
            if ((String) session.getAttribute(TIME_CATEGORY_TO_FILTER) != null) {
                timeFilterCategory = (String) session.getAttribute(TIME_CATEGORY_TO_FILTER);
            } else {
                timeFilterCategory = CREATED_AT;
            }
        }

        session.setAttribute(REQUEST_TYPE_FILTER, taskTypeFilter);
        session.setAttribute(REQUEST_STATUS_FILTER, statusToFilter);
        session.setAttribute(CREATED_AT_FROM, lowerBound);
        session.setAttribute(CREATED_AT_TO, upperBound);
        session.setAttribute(TIME_CATEGORY_TO_FILTER, timeFilterCategory);


        if (ALL_TASKS.equals(taskTypeFilter)) {
            requestList = client.getAllRequests(lowerBound, upperBound, timeFilterCategory, statusToFilter);
        } else {
            requestList = client.getRequestsCreatedByUser(loggedUser, lowerBound, upperBound, timeFilterCategory,
                    statusToFilter);
        }

        if (requestList == null) {
            requestList = new WorkflowRequest[0];
        }

        numberOfPages = (int) Math.ceil((double) requestList.length / WorkflowUIConstants.RESULTS_PER_PAGE);

        int startIndex = pageNumberInt * WorkflowUIConstants.RESULTS_PER_PAGE;
        int endIndex = (pageNumberInt + 1) * WorkflowUIConstants.RESULTS_PER_PAGE;
        associationToDisplay = new WorkflowRequest[WorkflowUIConstants.RESULTS_PER_PAGE];

        for (int i = startIndex, j = 0; i < endIndex && i < requestList.length; i++, j++) {
            associationToDisplay[j] = requestList[i];
        }

        workflowEvents = client.listWorkflowEvents();
        for (WorkflowEvent event : workflowEvents) {
            String category = event.getEventCategory();
            if (!events.containsKey(category)) {
                events.put(category, new ArrayList<WorkflowEvent>());
            }
            events.get(category).add(event);
        }
    } catch (WorkflowAdminServiceWorkflowException e) {
        // Removing the filter attributed from the session if an exception occurred.
        session.removeAttribute(REQUEST_TYPE_FILTER);
        session.removeAttribute(REQUEST_STATUS_FILTER);
        session.removeAttribute(CREATED_AT_FROM);
        session.removeAttribute(CREATED_AT_TO);
        session.removeAttribute(TIME_CATEGORY_TO_FILTER);
        String message = resourceBundle.getString("workflow.error.when.listing.services");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = ADMIN_ERROR_PAGE;
    } catch (AxisFault e) {
        // Removing the filter attributed from the session if an exception occurred.
        session.removeAttribute(REQUEST_TYPE_FILTER);
        session.removeAttribute(REQUEST_STATUS_FILTER);
        session.removeAttribute(CREATED_AT_FROM);
        session.removeAttribute(CREATED_AT_TO);
        session.removeAttribute(TIME_CATEGORY_TO_FILTER);
        String message = resourceBundle.getString("workflow.error.when.initiating.service.client");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = ADMIN_ERROR_PAGE;
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
    <carbon:breadcrumb label="view"
                       resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <link rel="stylesheet" href="/carbon/styles/css/main.css">


    <script type="text/javascript">

        function removeRequest(requestId) {
            function doDelete() {

                $.ajax({
                    type: 'POST',
                    url: 'wf-request-delete-finish-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: '<%=WorkflowUIConstants.PARAM_REQUEST_ID%>=' + requestId,
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("wf-request-list.jsp");
                        }
                    }
                });
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirmation.request.delete"/> ?',
                    doDelete, null);
        }
        function listWorkflows(requestId) {
            location.href = 'wf-workflows-of-request.jsp?<%=WorkflowUIConstants.PARAM_REQUEST_ID%>=' +
                    requestId;
        }
    </script>
    <script type="text/javascript">
        var eventsObj = {};
        var lastSelectedCategory = '';
        <%
            for (Map.Entry<String,List<WorkflowEvent>> eventCategory : events.entrySet()) {
            %>
        eventsObj["<%=eventCategory.getKey()%>"] = [];
        <%
            for (WorkflowEvent event : eventCategory.getValue()) {
                %>
        var eventObj = {};
        eventObj.displayName = "<%=Encode.forJavaScriptBlock(event.getEventFriendlyName())%>";
        eventObj.value = "<%=Encode.forJavaScriptBlock(event.getEventId())%>";
        eventObj.title = "<%=event.getEventDescription()!=null?Encode.forJavaScriptBlock(event.getEventDescription()):""%>";
        eventsObj["<%=Encode.forJavaScriptBlock(eventCategory.getKey())%>"].push(eventObj);
        <%
                    }
            }
        %>

        function updateActions() {
            var categoryDropdown = document.getElementById("categoryDropdown");
            var actionDropdown = document.getElementById("actionDropdown");
            var selectedCategory = categoryDropdown.options[categoryDropdown.selectedIndex].value;
            if (selectedCategory != lastSelectedCategory) {
                var eventsOfCategory = eventsObj[selectedCategory];
                for (var i = 0; i < eventsOfCategory.length; i++) {
                    var opt = document.createElement("option");
                    opt.text = eventsOfCategory[i].displayName;
                    opt.value = eventsOfCategory[i].value;
                    opt.title = eventsOfCategory[i].title;
                    actionDropdown.options.add(opt);
                }
                lastSelectedCategory = selectedCategory;
            }
        }

        function getSelectedRequestType() {
        }
        function getSelectedStatusType() {
        }
        function searchRequests() {
            document.searchForm.submit();
        }

    </script>

    <script>
        $(function () {
            $("#createdAtFrom").datepicker({
                defaultDate: "+1w",
                changeMonth: true,
                numberOfMonths: 1,
                onClose: function (selectedDate) {
                    $("#createdAtTo").datepicker({minDate:
                            new Date($('#createdAtFrom').datepicker("getDate"))});
                }
            });
            $("#createdAtTo").datepicker({
                defaultDate: "+1w",
                changeMonth: true,
                numberOfMonths: 1,
                onClose: function (selectedDate) {
                    $("#createdAtFrom").datepicker({maxDate: new
                            Date($('#createdAtTo').datepicker("getDate"))});
                }
            });
        });
    </script>

    <div id="middle">
        <h2><fmt:message key='request.list'/></h2>

        <form action="wf-request-list.jsp" name="searchForm" method="post">
            <table id="searchTable" name="searchTable" class="styledLeft" style="border:0;
                                                !important margin-top:10px;margin-bottom:10px;">
                <tr>
                    <td>
                        <table style="border:0; !important">
                            <tbody>
                            <tr style="border:0; !important">
                                <td style="border:0; !important">
                                    <nobr>
                                        <fmt:message key="workflow.request.type"/>
                                        <select name="requestTypeFilter" id="requestTypeFilter"
                                                onchange="getSelectedRequestType();">
                                            <% if (ALL_TASKS.equals(taskTypeFilter)) { %>
                                            <option value="myTasks"><fmt:message key="myTasks"/></option>
                                            <option value="allTasks"
                                                    selected="selected"><fmt:message key="allTasks"/></option>
                                            <%} else {%>
                                            <option value="myTasks"
                                                    selected="selected"><fmt:message key="myTasks"/></option>
                                            <option value="allTasks"><fmt:message key="allTasks"/></option>
                                            <% } %>
                                        </select>
                                    </nobr>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>

                    <td>
                        <table style="border:0; !important">
                            <tbody>
                            <tr style="border:0; !important">
                                <td style="border:0; !important">
                                    <nobr>
                                        <fmt:message key="workflow.request.status"/>
                                        <% if (PENDING_STATUS.equals(statusToFilter)) { %>

                                        <select name="requestStatusFilter" id="requestStatusFilter"
                                                onchange="getSelectedStatusType();">
                                            <option value="allTasks"><fmt:message key="allTasks"/></option>
                                            <option value="PENDING"
                                                    selected="selected"><fmt:message key="pending"/></option>
                                            <option value="APPROVED"><fmt:message key="approved"/></option>
                                            <option value="REJECTED"><fmt:message key="rejected"/></option>
                                            <option value="FAILED"><fmt:message key="failed"/></option>
                                        </select>

                                        <%} else if (APPROVED_STATUS.equals(statusToFilter)) { %>

                                        <select name="requestStatusFilter" id="requestStatusFilter"
                                                onchange="getSelectedStatusType();">
                                            <option value="allTasks"><fmt:message key="allTasks"/></option>
                                            <option value="PENDING"><fmt:message key="pending"/></option>
                                            <option value="APPROVED"
                                                    selected="selected"><fmt:message key="approved"/></option>
                                            <option value="REJECTED"><fmt:message key="rejected"/></option>
                                            <option value="FAILED"><fmt:message key="failed"/></option>
                                        </select>

                                        <%} else if (REJECTED_STATUS.equals(statusToFilter)) { %>

                                        <select name="requestStatusFilter" id="requestStatusFilter"
                                                onchange="getSelectedStatusType();">
                                            <option value="allTasks"><fmt:message key="allTasks"/></option>
                                            <option value="PENDING"><fmt:message key="pending"/></option>
                                            <option value="APPROVED"><fmt:message key="approved"/></option>
                                            <option value="REJECTED"
                                                    selected="selected"><fmt:message key="rejected"/></option>
                                            <option value="FAILED"><fmt:message key="failed"/></option>
                                        </select>

                                        <%} else if (FAILED_STATUS.equals(statusToFilter)) { %>

                                        <select name="requestStatusFilter" id="requestStatusFilter"
                                                onchange="getSelectedStatusType();">
                                            <option value="allTasks"><fmt:message key="allTasks"/></option>
                                            <option value="PENDING"><fmt:message key="pending"/></option>
                                            <option value="APPROVED"><fmt:message key="approved"/></option>
                                            <option value="REJECTED"><fmt:message key="rejected"/></option>
                                            <option value="FAILED"
                                                    selected="selected"><fmt:message key="failed"/></option>
                                        </select>

                                        <%} else { %>

                                        <select name="requestStatusFilter" id="requestStatusFilter"
                                                onchange="getSelectedStatusType();">
                                            <option value="allTasks"
                                                    selected="selected"><fmt:message key="allTasks"/></option>
                                            <option value="PENDING"><fmt:message key="pending"/></option>
                                            <option value="APPROVED"><fmt:message key="approved"/></option>
                                            <option value="REJECTED"><fmt:message key="rejected"/></option>
                                            <option value="FAILED"><fmt:message key="failed"/></option>
                                        </select>
                                        <%}%>
                                    </nobr>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                    <td>
                        <table style="border:0; !important">
                            <tbody>
                            <tr style="border:0; !important">
                                <td style="border:0; !important">
                                    <nobr>
                                        <% if ("updatedAt".equals(timeFilterCategory)) { %>
                                        <select name="timeCategoryToFilter" id="timeCategoryToFilter"
                                                onchange="getSelectedRequestType();">
                                            <option value="createdAt"><fmt:message key="createdAt"/></option>
                                            <option value="updatedAt"
                                                    selected="selected"><fmt:message key="updatedAt"/></option>
                                        </select>
                                        <%} else { %>
                                        <select name="timeCategoryToFilter" id="timeCategoryToFilter"
                                                onchange="getSelectedRequestType();">
                                            <option value="createdAt"
                                                    selected="selected"><fmt:message key="createdAt"/></option>
                                            <option value="updatedAt"><fmt:message key="updatedAt"/></option>
                                        </select>
                                        <%}%>

                                    </nobr>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                    <td>
                        <table style="border:0; !important">
                            <tbody>
                            <tr style="border:0; !important">
                                <td style="border:0; !important">
                                    <nobr>
                                        <label for="createdAtFrom">From</label>
                                        <input type="text" id="createdAtFrom" value="<%=lowerBound%>" name="createdAtFrom">
                                        <label for="createdAtTo">to</label>
                                        <input type="text" id="createdAtTo" value="<%=upperBound%>" name="createdAtTo">
                                    </nobr>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                    <td style="border:0; !important">
                        <a class="icon-link" href="#" style="background-image: url(images/search-top.png);"
                           onclick="searchRequests(); return false;"
                           alt="<fmt:message key="search"/>"></a>
                    </td>
                </tr>
            </table>
        </form>

        <div id="workArea">
            <table class="styledLeft" id="servicesTable">
                <thead>
                <tr>
                    <th><fmt:message key="workflow.eventType"/></th>
                    <th><fmt:message key="workflow.createdAt"/></th>
                    <th><fmt:message key="workflow.updatedAt"/></th>
                    <th><fmt:message key="workflow.status"/></th>
                    <th><fmt:message key="workflow.requestParams"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (requestList != null && requestList.length > 0) {
                        for (WorkflowRequest workflowReq : associationToDisplay) {
                            if (workflowReq != null && (statusToFilter == null || statusToFilter == ""
                                    || ALL_TASKS.equals(statusToFilter) || workflowReq.getStatus().equals(statusToFilter))) {
                %>
                <tr>
                    <td><%=Encode.forHtmlContent(workflowReq.getEventType())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflowReq.getCreatedAt())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflowReq.getUpdatedAt())%>
                    </td>

                    <td><%=Encode.forHtmlContent(workflowReq.getStatus())%>
                    </td>
                    <td><%=Encode.forHtmlContent(workflowReq.getRequestParams())%>
                    </td>
                    <td>
                        <a title="<fmt:message key='workflow.request.list.title'/>"
                           onclick="listWorkflows('<%=workflowReq.getRequestId()%>');return false;"
                           href="#" style="background-image: url(images/list.png);"
                           class="icon-link"><fmt:message key='workflows'/></a>
                        <%
                            Boolean authorizedToDelete = false;
                            if (PENDING_STATUS.equals(workflowReq.getStatus())) {
                                if (CarbonUIUtil.isUserAuthorized(request,
                                        "/permission/admin/manage/identity/workflow/monitor/anydelete")) {
                                    authorizedToDelete = true;
                                }
                                else if (workflowReq.getCreatedBy() != null && CarbonUIUtil.isUserAuthorized(request,
                                        "/permission/admin/manage/identity/workflow/monitor/delete") &&
                                        workflowReq.getCreatedBy().equals(loggedUser)) {
                                    authorizedToDelete = true;
                                }
                            }
                            if (authorizedToDelete) { %>
                        <a title="<fmt:message key='workflow.request.delete.title'/>"
                           onclick="removeRequest('<%=workflowReq.getRequestId()%>');return false;"
                           href="#" style="background-image: url(images/delete.gif);"
                           class="icon-link"><fmt:message key='delete'/></a>
                        <% } else { %>

                        <% } %>
                    </td>
                </tr>
                <%
                            }
                        }
                    } else { %>
                <tr>
                    <td colspan="6"><i>No requests found.</i></td>
                </tr>
                <% }
                %>
                </tbody>
            </table>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="wf-request-list.jsp"
                              pageNumberParameterName="<%=WorkflowUIConstants.PARAM_PAGE_NUMBER%>"
                              resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>
</fmt:bundle>