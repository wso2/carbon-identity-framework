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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorException" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String type = request.getParameter("type");
    if(request.getParameter("editorConfig") != null){
        try {
            PolicyEditorEngine.getInstance().persistConfig(type, request.getParameter("editorConfig"));
            String message = resourceBundle.getString("policy.editor.config.update");
            %>
            <script type="text/javascript">
                CARBON.showInfoDialog('<%=message%>', null, null);
            </script>
            <%
        } catch (PolicyEditorException e) {
            String message = resourceBundle.
                    getString("policy.editor.config.can.not.update") + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            %>
            <script type="text/javascript">
                function forward() {
                    location.href = "policy-editor-config-view.jsp?type=" + <%=Encode.forUriComponent(type)%>;
                }
            </script>
            <script type="text/javascript">
                forward();
            </script>
            <%
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
        label="add.new.policy"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>" />
<div id="middle">
    <h2><fmt:message key="add.new.policy"/></h2>
<div id="workArea">
    <%--<p> <fmt:message key="add.new.policy.description"/> </p>--%>
    <table class="styledLeft" style="width:100%">
        <thead>
        <tr>
            <th><fmt:message key="add.new.policy.method"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="formRow">
                <table class="normal" style="width:100%">
                    <tbody>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="simple-policy-editor.jsp"><fmt:message key="add.new.policy.simple"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.simple.description"/>
                                <a href="policy-editor-config-view.jsp?type=<%=EntitlementConstants.PolicyEditor.RBAC%>">
                                    <fmt:message key="here"/></a></td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="basic-policy-editor.jsp"><fmt:message key="add.new.policy.basic"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.basic.description"/>
                            <a href="policy-editor-config-view.jsp?type=<%=EntitlementConstants.PolicyEditor.BASIC%>">
                                <fmt:message key="here"/></a></td>
                        </tr>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="policy-editor.jsp"><fmt:message key="add.new.policy.editor"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.editor.description"/>
                            <a href="policy-editor-config-view.jsp?type=<%=EntitlementConstants.PolicyEditor.STANDARD%>">
                                <fmt:message key="here"/></a></td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="create-policy-set.jsp"><fmt:message key="add.new.policy.set.editor"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.set.editor.description"/>
                                <a href="policy-editor-config-view.jsp?type=<%=EntitlementConstants.PolicyEditor.SET%>">
                                    <fmt:message key="here"/></a>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="import-policy.jsp"><fmt:message key="add.new.policy.import"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.import.description"/></td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="policy-view.jsp"><fmt:message key="add.new.policy.write"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.write.description"/></td>
                        </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div>
</fmt:bundle>