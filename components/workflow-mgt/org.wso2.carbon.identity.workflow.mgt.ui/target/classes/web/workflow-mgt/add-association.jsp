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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
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
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard" %>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>


<%
    String wizard = request.getParameter("wizard");
    String forwardTo = null;
    Association association = new Association();
    String workflowName = "" ;

    String workflowId = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_ID);

    if("start".equals(wizard)){


        String name = request.getParameter(WorkflowUIConstants.PARAM_ASSOCIATION_NAME);
        String operation = request.getParameter(WorkflowUIConstants.PARAM_OPERATION);
        String operationCategory =
                request.getParameter(WorkflowUIConstants.PARAM_OPERATION_CATEGORY);
        String condition = request.getParameter(WorkflowUIConstants.PARAM_ASSOCIATION_CONDITION);
        association = new Association();
        association.setAssociationName(name);
        association.setEventName(operation);
        association.setCondition(condition);
        association.setEventCategory(operationCategory);

        session.setAttribute("add-association", association);
        forwardTo = "add-wf-wizard.jsp?"+WorkflowUIConstants.PARAM_REQUEST_PATH+"=add-association" ;

    }else if("finish".equals(wizard)){
        association = (Association)session.getAttribute("add-association");
        workflowName = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_NAME);
    }

    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    WorkflowAdminServiceClient client = null;


    WorkflowEvent[] workflowEvents = null;
    Map<String, List<WorkflowEvent>> events = new HashMap<String, List<WorkflowEvent>>();

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);

        workflowEvents = client.listWorkflowEvents();
        for (WorkflowEvent event : workflowEvents) {
            String category = event.getEventCategory();
            if (!events.containsKey(category)) {
                events.put(category, new ArrayList<WorkflowEvent>());
            }
            events.get(category).add(event);
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
            location.href = "<%=forwardTo%>";
        }
        forward();

    </script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="workflow.mgt" resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources" topPage="true" request="<%=request%>"/>
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
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
            $("#actionDropdown").empty();
            var headOption = document.createElement("option");
            headOption.text = '<%=resourceBundle.getString("select")%>';
            headOption.value = "";
            headOption.selected = true;
            headOption.disabled = "disabled";
            actionDropdown.options.add(headOption);
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
            $(".enableOnCategorySel").prop('disabled', false);
        }

        var paramDefs = {};
        var operations = {
            "INTEGER": ["equals", "less than", "greater than"],
            "DOUBLE": ["equals", "less than", "greater than"],
            "STRING": ["equals", "contains"],
            "STRING_LIST": ["has value"],
            "STRING_STRING_MAP": ["contains key"]
        };

        var selectionType = "applyToAll";
        <%
        if (workflowEvents != null) {
        for (WorkflowEvent event : workflowEvents) {
        %>
        paramDefs["<%=event.getEventId()%>"] = {};
        <%
                for (Parameter parameter : event.getParameters()) {
                    if(parameter!=null){

            %>
        paramDefs["<%=event.getEventId()%>"]["<%=parameter.getParamName()%>"] = "<%=parameter.getParamValue()%>";
        <%
                    }else {

                    }
                }
            }
        }
        %>

        function updateParams() {
            var actionDropdown = document.getElementById("actionDropdown");
            var selectedCategory = actionDropdown.options[actionDropdown.selectedIndex].value;
            var paramDropDown = document.getElementById("paramSelect");
            $("#paramSelect").empty();
            $("#operationSelect").empty();
            var headOption = document.createElement("option");
            headOption.text = '<%=resourceBundle.getString("select")%>';
            headOption.value = "";
            headOption.selected = true;
            headOption.disabled = "disabled";
            paramDropDown.options.add(headOption);
            for (var key in paramDefs[selectedCategory]) {
                if (paramDefs[selectedCategory].hasOwnProperty(key)) {
                    var opt = document.createElement("option");
                    opt.text = key;
                    opt.value = key;
                    paramDropDown.options.add(opt);
                }
            }
            $(".enableOnOperationSel").prop('disabled', false);
        }

        function updateOperator() {
            var actionDropdown = document.getElementById("actionDropdown");
            var selectedCategory = actionDropdown.options[actionDropdown.selectedIndex].value;
            var paramDropDown = document.getElementById("paramSelect");
            var operationDropdown = document.getElementById("operationSelect");
            $("#operationSelect").empty();
            var selectedParam = paramDropDown.options[paramDropDown.selectedIndex].value;
            var operationsForParam = operations[paramDefs[selectedCategory][selectedParam]];
            var headOption = document.createElement("option");
            headOption.text = '<%=resourceBundle.getString("select")%>';
            headOption.value = "";
            headOption.selected = true;
            headOption.disabled = "disabled";
            operationDropdown.options.add(headOption);
            for (var i = 0; i < operationsForParam.length; i++) {
                var opt = document.createElement("option");
                opt.text = operationsForParam[i];
                opt.value = operationsForParam[i];
                operationDropdown.options.add(opt);
            }
            operationDropdown.disabled = false;
            var val1 = document.getElementById("val1");
            val1.disabled = false;
        }

        function generateXpath() {
            var paramDropDown = document.getElementById("paramSelect");
            var operationDropdown = document.getElementById("operationSelect");
            var condition = "boolean(1)";

            if (selectionType == "applyIf") {
                var selectedParam = paramDropDown.options[paramDropDown.selectedIndex].value;
                var selectedOperation = operationDropdown.options[operationDropdown.selectedIndex].value;
                var val1 = document.getElementById("val1").value;

                switch (selectedOperation) {
                    case "contains":
                        var template =
                                "boolean(//*[local-name()='parameter'][@name='{{paramName}}']/*[local-name()='value']/*[local-name()='itemValue'][contains(text(),'{{value}}')])";
                        condition = template.replace("{{paramName}}", selectedParam).replace("{{value}}", val1);
                        break;
                    case "less than":
                        template =
                                "boolean(//*[local-name()='parameter'][@name='{{paramName}}']/*[local-name()='value']/*[local-name()='itemValue']/text()<{{value}})";
                        condition = template.replace("{{paramName}}", selectedParam).replace("{{value}}", val1);
                        break;
                    case "greater than":
                        template =
                                "boolean(//*[local-name()='parameter'][@name='{{paramName}}']/*[local-name()='value']/*[local-name()='itemValue']/text()>{{value}})";
                        condition = template.replace("{{paramName}}", selectedParam).replace("{{value}}", val1);
                        break;
                    case "equals"://both equals and has value has same condition, but works differently because of string and list
                    case "has value":
                        template =
                                "//*[local-name()='parameter'][@name='{{paramName}}']/*[local-name()='value']/*[local-name()='itemValue']/text()='{{value}}'";
                        condition = template.replace("{{paramName}}", selectedParam).replace("{{value}}", val1);
                        break;
                    case "contains key":
                        template =
                                "boolean(//*[local-name()='parameter'][@name='{{paramName}}']/*[local-name()='value'][@itemName='{{value}}']/*[local-name()='itemValue'])";
                        condition = template.replace("{{paramName}}", selectedParam).replace("{{value}}", val1);
                        break;
                    default :
                        CARBON.showErrorDialog('<fmt:message key="condition.error"/>', null, null);
                        return false;
                }
            }

            if (selectionType != "advanced") {
                document.getElementsByName("<%=WorkflowUIConstants.PARAM_ASSOCIATION_CONDITION%>")[0].value =
                        condition;
            }
            return true;
        }

        function doValidation() {

//            todo:validate other params
        }

        function doCancel() {
            function cancel() {
                location.href = 'list-associations.jsp';
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirmation.association.add.abort"/> ' + name + '?',
                    cancel, null);
        }

        function handleRadioInput(radio) {
            if (radio.value != selectionType) {
                switch (radio.value) {
                    case "applyToAll":
                        document.getElementById("conditionSelectRow").style.display = 'none';
                        document.getElementById("conditionXpath").style.display = 'none';
                        break;
                    case "applyIf":
                        document.getElementById("conditionSelectRow").style.display = 'block';
                        document.getElementById("conditionXpath").style.display = 'none';
                        break;
                    case "advanced":
                        document.getElementById("conditionSelectRow").style.display = 'none';
                        document.getElementById("conditionXpath").style.display = 'block';
                        break;
                }
                selectionType = radio.value;
            }
        }

        function changeWorkFlow(){

            var workflowList = document.getElementById("id_<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>");
            var selectedValue = workflowList[workflowList.selectedIndex].value;
            if(selectedValue == "create_new_workflow"){
                var add_association_form = document.getElementById("id_form_add_association");
                add_association_form.action = "add-association.jsp?wizard=start";
                add_association_form.submit();
            }
        }

        function addAssociation(){
            var form_add_association = document.getElementById("id_form_add_association");
            var input_association_name = document.getElementById("id_<%=WorkflowUIConstants.PARAM_ASSOCIATION_NAME%>");
            if(input_association_name.value.length == 0){
                CARBON.showErrorDialog('<fmt:message key="workflow.error.empty.association.name"/>', null, null);
                return;
            }

            var select_category_dropdown = document.getElementById("categoryDropdown");
            if(select_category_dropdown.selectedIndex == 0){
                CARBON.showErrorDialog('<fmt:message key="workflow.error.not.select.operation.category"/>', null, null);
                return;
            }

            var select_operation_name = document.getElementById("actionDropdown");
            if(select_operation_name.selectedIndex == 0){
                CARBON.showErrorDialog('<fmt:message key="workflow.error.not.select.operation.name"/>', null, null);
                return;
            }

            var select_workflow = document.getElementById("id_<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>");
            if(select_workflow.selectedIndex == 0){
                CARBON.showErrorDialog('<fmt:message key="workflow.error.not.select.workflow"/>', null, null);
                return;
            }

            var ret = generateXpath();
            if(!ret){
                return false;
            }



            form_add_association.submit();
        }

        window.onload = function () {
            enableOnOperationSel();
        }

        function enableOnOperationSel() {
            <%
                if(association != null && StringUtils.isNotBlank(association.getEventName())){
            %>
            $(".enableOnOperationSel").prop('disabled', false);
            <%
                }
            %>
        }
    </script>

    <div id="middle">
        <h2><fmt:message key='workflow.service.association.add'/></h2>

        <div id="workArea">
            <div id="addNew">
                <form id="id_form_add_association" action="update-association-finish.jsp" method="post">
                    <input type="hidden" name="<%=WorkflowUIConstants.PARAM_ACTION%>"
                           value="<%=WorkflowUIConstants.ACTION_VALUE_ADD%>">



                    <table class="styledLeft noBorders" style="margin-top: 10px">
                        <thead>
                        <tr>
                            <th colspan="2">
                                <fmt:message key="workflow.service.association.details">
                                </fmt:message>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td width="30%"><fmt:message key="workflow.service.association.name"/></td>
                            <td>
                                <%
                                    if(association != null && association.getAssociationName() != null && !association.getAssociationName().isEmpty()){
                                %>
                                        <input type="text" name="<%=WorkflowUIConstants.PARAM_ASSOCIATION_NAME%>" id="id_<%=WorkflowUIConstants.PARAM_ASSOCIATION_NAME%>" style="min-width: 30%;" value="<%=Encode.forHtmlAttribute(association.getAssociationName())%>">
                                <%
                                    }else{
                                %>
                                        <input type="text" name="<%=WorkflowUIConstants.PARAM_ASSOCIATION_NAME%>" id="id_<%=WorkflowUIConstants.PARAM_ASSOCIATION_NAME%>" style="min-width: 30%;" value="">
                                <%
                                    }
                                %>
                            </td>
                        </tr>

                        <tr>
                            <td width="30%"><fmt:message key='workflow.operation.category'/></td>
                            <td>
                                <select id="categoryDropdown" onchange="updateActions();" style="min-width: 30%;" name="<%=WorkflowUIConstants.PARAM_OPERATION_CATEGORY%>">
                                    <option  selected value=""><fmt:message key="select"/></option>
                                    <%
                                        for (String key : events.keySet()) {
                                            if(key.equals(association.getEventCategory())){
                                    %>
                                                <option selected value="<%=Encode.forHtmlAttribute(key)%>"><%=Encode.forHtmlContent(key)%>
                                                </option>
                                    <%
                                            }else{
                                    %>
                                                <option  value="<%=Encode.forHtmlAttribute(key)%>"><%=Encode.forHtmlContent(key)%>
                                                </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td width="30%">
                                <fmt:message key='workflow.operation.name'/>
                            </td>
                            <td>
                                <select id="actionDropdown" onchange="updateParams();" style="min-width: 30%;" name="<%=WorkflowUIConstants.PARAM_OPERATION%>" class="enableOnCategorySel">
                                    <option  selected value=""><fmt:message key="select"/></option>
                                    <%
                                        if(association != null && association.getEventCategory() != null && !association.getEventCategory().isEmpty()){
                                            for (Map.Entry<String,List<WorkflowEvent>> eventCategory : events.entrySet()) {
                                                if(eventCategory.getKey().equals(association.getEventCategory())){
                                                    for (WorkflowEvent event : eventCategory.getValue()) {
                                                        if(event.getEventId().equals(association.getEventName())){
                                    %>
                                                            <option selected value="<%=Encode.forHtmlAttribute(event.getEventId())%>"><%=Encode.forHtmlContent(event.getEventId())%>
                                    <%
                                                        }else{
                                    %>
                                                            <option value="<%=Encode.forHtmlAttribute(event.getEventId())%>"><%=Encode.forHtmlContent(event.getEventId())%>
                                    <%
                                                        }
                                                    }
                                                }
                                            }
                                        }else{
                                    %>

                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                        </tbody>
                    </table>





                    <table class="styledLeft noBorders" style="margin-top: 10px">
                        <thead>
                        <tr>
                            <th colspan="2">
                                <fmt:message key="workflow.details">
                                </fmt:message>
                            </th>
                        </tr>
                        </thead>
                        <tr>
                            <td width="30%">
                                <fmt:message key="workflow.select"/>
                            </td>
                            <td>
                                <select id="id_<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>" onchange="changeWorkFlow();" name="<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>" style="min-width: 30%;">
                                    <option value=""><fmt:message key="select"/></option>
                                    <option value="create_new_workflow">Create New Workflow...</option>
                                    <%
                                        for (WorkflowWizard workflowBean : client.listWorkflows()) {
                                            if (workflowBean != null) {
                                                boolean select = false;
                                                if (StringUtils.equals(workflowId, workflowBean.getWorkflowId()) || StringUtils.equals(workflowName, workflowBean.getWorkflowName())) {
                                                    select = true;
                                                }
                                    %>
                                    <option value="<%=Encode.forHtmlAttribute(workflowBean.getWorkflowId())%>" <%=select ? "selected" : ""%>
                                            title="<%=Encode.forHtmlAttribute(workflowBean.getWorkflowDescription())%>">
                                        <%=Encode.forHtmlContent(workflowBean.getWorkflowName())%>
                                    </option>
                                    <%
                                            }
                                        }

                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <input type="radio" name="conditionType" value="applyToAll" disabled  checked="checked"
                                       onclick="handleRadioInput(this);" class="enableOnOperationSel">
                                Apply to all requests
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <input type="radio" name="conditionType" value="applyIf" disabled
                                       class="enableOnOperationSel" onclick="handleRadioInput(this);">
                                Apply if,
                            </td>
                        </tr>
                        <tr id="conditionSelectRow" style="display: none">
                            <td class="formRow" colspan="2">
                                <table class="normal noBorders">
                                    <tr>
                                        <td>
                                            <select id="paramSelect" onchange="updateOperator()"></select>
                                        </td>
                                        <td>
                                            <select id="operationSelect" disabled="disabled"></select>
                                        </td>
                                        <td>
                                            <input id="val1" type="text" disabled="disabled"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><input type="radio" name="conditionType" value="advanced" disabled
                                                   onclick="handleRadioInput(this);" class="enableOnOperationSel">
                                Advanced
                            </td>
                        </tr>
                        <tr id="conditionXpath" style="display: none">
                            <td class="formRow" colspan="2">
                                <table class="normal noBorders">
                                    <tr>
                                        <td><fmt:message key='workflow.service.associate.condition'/></td>
                                        <td>
                                            <input type="text"
                                                   name="<%=WorkflowUIConstants.PARAM_ASSOCIATION_CONDITION%>"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>





                    <table class="styledLeft noBorders" style="margin-top: 10px">
                        <tr>
                            <td class="buttonRow">
                                <input id="id_add_association" onclick="addAssociation();" class="button" value="<fmt:message key="add"/>" type="button"/>
                                <input class="button" value="<fmt:message key="cancel"/>" type="button"
                                       onclick="doCancel();"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
            <br/>
        </div>
    </div>
</fmt:bundle>