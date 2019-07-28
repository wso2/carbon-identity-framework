<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>


<fmt:bundle basename="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.claim.dialect"
                       resourceBundle="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>

    <div id="middle">
        <h2><fmt:message key='add.claim.dialect'/></h2>

        <div id="workArea">

            <script type="text/javascript">
                function setType(chk, hidden) {
                    var val = document.getElementById(chk).checked;
                    var hiddenElement = document.getElementById(hidden);

                    if (val) {
                        hiddenElement.value = "true";
                    } else {
                        hiddenElement.value = "false";
                    }
                }

                function validate() {

                    var dialectURIElement = document.getElementById('dialect');
                    if (dialectURIElement.value == '') {
                        CARBON.showWarningDialog('<fmt:message key="dialect.uri.is.required"/>');
                        return false;
                    } else if (dialectURIElement.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="dialect.is.too.long"/>');
                        return false;
                    } else if (!doValidateInput(dialectURIElement,
                        '<fmt:message key="dialect.has.unsafe.characters"/>')) {
                        return false;
                    }

                    document.adddialect.submit();
                }

            </script>

            <form name="adddialect" action="add-dialect-finish-ajaxprocessor.jsp" method="post">
                <table style="width: 100%" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='add.new.dialect.details'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0">
                                <tr>
                                    <td class="leftCol-small"><fmt:message key='dialect.uri'/><font color="red">*</font>
                                    </td>
                                    <td class="leftCol-big">
                                        <input type="text" name="dialect" id="dialect" class="text-box-big"
                                               black-list-patterns="xml-meta-exists"/>
                                        <div class="sectionHelp"><fmt:message key="dialect.uri.help"/></div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" value="<fmt:message key='add'/>" class="button" onclick="validate();"/>
                            <input class="button" type="button" value="<fmt:message key='cancel'/>"
                                   onclick="javascript:document.location.href='add.jsp'"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
