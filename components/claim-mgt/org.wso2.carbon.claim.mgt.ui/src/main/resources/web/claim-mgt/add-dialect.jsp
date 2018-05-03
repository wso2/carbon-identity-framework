<%--
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


<%
    String extuser = request.getParameter("extuser");
%>


<fmt:bundle basename="org.wso2.carbon.claim.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.new.claim.dialect"
                       resourceBundle="org.wso2.carbon.claim.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2><fmt:message key='add.new.claim.dialect'/></h2>

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

                    var value = document.getElementsByName("dialect")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="dialect.uri.is.required"/>');
                        return false;
                    } else if (value.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="dialect.is.too.long"/>');
                        return false;
                    }

                    var value = document.getElementsByName("attribute")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="attribute.is.required"/>');
                        return false;
                    } else if (value.length > 30) {
                        CARBON.showWarningDialog('<fmt:message key="attr.id.is.too.long"/>');
                        return false;
                    }


                    var value = document.getElementsByName("displayName")[0].value;
                    if (isEmpty(value)) {
                        CARBON.showWarningDialog('<fmt:message key="displayname.cannot.be.empty"/>');
                        return false;
                    } else if (value.length > 30) {
                        CARBON.showWarningDialog('<fmt:message key="displayname.is.too.long"/>');
                        return false;
                    }


                    var value = document.getElementsByName("description")[0].value;
                    if (isEmpty(value)) {
                        CARBON.showWarningDialog('<fmt:message key="description.cannot.be.empty"/>');
                        return false;
                    } else if (value.length > 150) {
                        CARBON.showWarningDialog('<fmt:message key="description.is.too.long"/>');
                        return false;
                    }

                    var value = document.getElementsByName("claimUri")[0].value;
                    if (isEmpty(value)) {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.cannot.be.empty"/>');
                        return false;
                    } else if (value.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.is.too.long"/>');
                        return false;
                    }

                    var value = document.getElementsByName("displayOrder")[0].value;
                    if (value != '') {
                        var IsFound = /^-?\d+$/.test(value);
                        if (!IsFound) {
                            CARBON.showWarningDialog('<fmt:message key="display.order.has.to.be.integer"/>');
                            return false;
                        }
                    }

                    var value = document.getElementsByName("regex")[0].value;
                    if (value != '' && value.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="regex.is.too.long"/>');
                        return false;
                    }

                    var unsafeCharPattern = /[<>`\"]/;
                    var elements = document.getElementsByTagName("input");
                    for (i = 0; i < elements.length; i++) {
                        if ((elements[i].type === 'text' || elements[i].type === 'password') &&
                                elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null) {
                            CARBON.showWarningDialog("<fmt:message key="unsafe.char.validation.msg"/>");
                            return false;
                        }
                    }

                    document.adddialect.submit();
                }

                function isEmpty(value){
                  return (value == null || value.trim() == '');
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
                                    <td class="leftCol-big"><input type="text" name="dialect" id="dialect"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='display.name'/><font
                                            color="red">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="displayName" id="displayName"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='description'/><font color="red">*</font>
                                    </td>
                                    <td class="leftCol-big"><input type="text" name="description" id="description"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='claim.uri'/><font color="red">*</font>
                                    </td>
                                    <td class="leftCol-big"><input type="text" name="claimUri" id="claimUri"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.attribute'/><font
                                            color="red">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="attribute" id="attribute"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='regular.expression'/></td>
                                    <td class="leftCol-big"><input type="text" name="regex" id="regex"
                                                                   class="text-box-big"/></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-small"><fmt:message key='display.order'/></td>
                                    <td class="leftCol-big"><input type="text" name="displayOrder" id="displayOrder"
                                                                   class="text-box-big"/></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-small"><fmt:message key='supported.by.default'/></td>
                                    <td class="leftCol-big">
                                        <input type='checkbox' name='supported' id='supported'
                                               onclick="setType('supported','supportedhidden')"/>
                                        <input type='hidden' name='supportedhidden' id='supportedhidden'/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='required'/></td>
                                    <td class="leftCol-big">
                                        <input type='checkbox' name='required' id='required'
                                               onclick="setType('required','requiredhidden')"/>
                                        <input type='hidden' name='requiredhidden' id='requiredhidden'/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='readonly'/></td>
                                    <td class="leftCol-big">
                                        <input type='checkbox' name='readonly' id='readonly'
                                               onclick="setType('readonly','readonlyhidden')"/>
                                        <input type='hidden' name='readonlyhidden' id='readonlyhidden'/>
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" value="<fmt:message key='add'/>" class="button" onclick="validate();"/>
                            <input class="button" type="button" value="<fmt:message key='cancel'/>"
                                   onclick="javascript:document.location.href='add-dialect-claim.jsp?region=region1&item=claim_mgt_menu_add'"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
