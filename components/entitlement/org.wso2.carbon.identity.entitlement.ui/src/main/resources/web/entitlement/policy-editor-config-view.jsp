<!--
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>

<%
    String type = request.getParameter("type");
    if(type == null || type.trim().length() == 0){
        type = EntitlementConstants.PolicyEditor.STANDARD;
    }
    String editorConfig = PolicyEditorEngine.getInstance().getConfig(type);
    if(editorConfig == null){
        editorConfig = "";
    }
    editorConfig = editorConfig.trim();
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="policy.editor.config"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<div id="middle">
<h2><fmt:message key='policy.editor.config'/></h2>
<div id="workArea">
    <form method="post" name="configForm" id="configForm" action="add-policy.jsp?type=<%=Encode.forUriComponent(type)%>">
        <table class="styledLeft" style="width:100%">
            <thead>
            <tr>
                <th>
                    <fmt:message key='policy.editor.config'/>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="formRow">
                    <table class="normal" style="width:100%">
                        <tbody>
                        <tr>
                            <td>
                                <input type="hidden" name="editorConfig" id="editorConfig">
                                <textarea name="editorConfigText"  id="editorConfigText"  rows="50" cols="50"
                                          style="border: 1px solid rgb(204, 204, 204); width: 90%;">
                                          <%--height: 1000px; margin-top: 5px; display: none;"--%>
                                          <%=Encode.forHtmlContent(editorConfig)%>
                                </textarea>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <button class="button" onclick="submitForm(); return false;"><fmt:message key="update"/></button>
                    <button class="button" onclick="cancel(); return false;"><fmt:message key="cancel"/></button>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
</div>
</fmt:bundle>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

    <script type="text/javascript">

    jQuery(document).ready(function(){
        editAreaLoader.init({
            id : "editorConfigText"		// text area id
            ,syntax: "xml"			// syntax to be uses for highlighting
            ,start_highlight: true  // to display with highlight mode on start-up
        });
    })

    function submitForm() {
        document.getElementById("editorConfig").value = editAreaLoader.getValue("editorConfigText");
        document.configForm.submit();
    }

    function cancel() {
        location.href = "add-policy.jsp";
    }

</script>
