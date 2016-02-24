<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<%
    String importFrom = (String)request.getParameter("importFrom");
    String[] importingMethods = new String[]{EntitlementPolicyConstants.IMPORT_POLICY_REGISTRY,
            EntitlementPolicyConstants.IMPORT_POLICY_FILE_SYSTEM};
    if(importFrom == null || importFrom.trim().length() == 0){
        importFrom = EntitlementPolicyConstants.IMPORT_POLICY_FILE_SYSTEM;
    }
%>

<fmt:bundle
        basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb label="import.policy"
                       resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript" src="extensions/js/vui.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <script type="text/javascript" src="../resources/js/registry-browser.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <script type="text/javascript" src="../yui/build/event/event-min.js"></script>

    <div id="middle">
        <h2><fmt:message key='import.new.ent.policy'/></h2>

        <div id="workArea">
            <script type="text/javascript">

                function doSubmit(){
                    var policy;
                    var importFrom = "<%=Encode.forJavaScript(importFrom)%>";

                    if(importFrom == 'FileSystem') {
                        policy = document.importPolicy.policyFromFileSystem.value;
                    } else {
                        policy = document.importPolicy.policyFromRegistry.value;
                        document.importPolicy.action = "import-policy-submit.jsp";
                    }

                    if (policy == '') {
                        CARBON.showWarningDialog("<fmt:message key='select.policy.to.upload'/>");
                        return;
                    }

                    document.importPolicy.submit();
                }

                function doCancel(){
                    location.href = 'index.jsp';
                }

                function selectPolicyImportMethod(){

                    var comboBox = document.getElementById("importingMethod");
                    var importingMethod = comboBox[comboBox.selectedIndex].value;
                    location.href = 'import-policy.jsp?importFrom=' + importingMethod;
                }


            </script>
            <table class="styledLeft noBorders" style="width: 100%">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key='import.ent.policy'/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td width="30%">
                        <fmt:message key="import.entitlement.policy.from"/>
                        <select onchange="selectPolicyImportMethod();" id="importingMethod" name="importingMethod">
                            <%
                                for (String importingMethod : importingMethods) {
                                    if(importFrom.equals(importingMethod)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(importingMethod)%>" selected="selected"><%=Encode.forHtmlContent(importingMethod)%></option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(importingMethod)%>"><%=Encode.forHtmlContent(importingMethod)%></option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>
                        <form name="importPolicy" id="importPolicy" target="_self" action="../../fileupload/entitlement-policy" method="post"
                                <%
                                    if(importFrom.equals(EntitlementPolicyConstants.IMPORT_POLICY_FILE_SYSTEM)){
                                %>
                              enctype="multipart/form-data"
                                <%
                                    }
                                %>
                                >
                            <%
                                if(importFrom.equals(EntitlementPolicyConstants.IMPORT_POLICY_FILE_SYSTEM)){
                            %>
                            <tr>
                                <td><input type="file" id="policyFromFileSystem" name="policyFromFileSystem" size="50" />
                                </td>
                            </tr>
                            <%
                            } else {
                            %>
                            <tr>
                                <td>
                                    <table>
                                        <tbody>
                                        <tr>
                                            <td width="5%">
                                                <input type="text" name="policyFromRegistry" id="policyFromRegistry" readonly="readonly"/>
                                            </td>
                                            <td>
                                                <label for="policyFromRegistry">
                                                    <a class="registry-picker-icon-link"
                                                       style="padding-left:30px;cursor:pointer;color:#386698"
                                                       onclick="showRegistryBrowser('policyFromRegistry','/_system/config');"
                                                            ><fmt:message key="conf.registry"/>
                                                    </a>
                                                </label>
                                                <label for='policyFromRegistry'>
                                                    <a class="registry-picker-icon-link"
                                                       style="padding-left:30px;cursor:pointer;color:#386698"
                                                       onclick="showRegistryBrowser('policyFromRegistry','/_system/governance');">
                                                        <fmt:message key="gov.registry"/>
                                                    </a>
                                                </label>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <%
                                    }
                                %>
                                <td colspan="2" class="buttonRow">
                                    <input type="button" value="<fmt:message key='upload'/>" class="button" onclick="doSubmit();"/>
                                    <input class="button" type="reset" value="<fmt:message key='cancel'/>" onclick="doCancel();"/>
                                </td>
                            </tr>
                        </form>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
