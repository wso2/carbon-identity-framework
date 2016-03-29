<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ may obtain a copy of the License at
  ~
  ~  http://www.apache.org/licenses/LICENSE-2.0
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
<%@ page import="org.wso2.carbon.identity.claim.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.mgt.ui.client.ClaimAdminClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.identity.claim.mgt.stub.dto.ClaimMappingDTO" %>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<style>
    .sectionHelp {
        padding-left: 17px;
    }
</style>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ClaimAdminClient claimAdminClient = new ClaimAdminClient(cookie,serverURL,configContext);
    String LOCAL_DIALECT_URI = "http://wso2.org/claims";
    String USER_STORE_INFO = "org.wso2.carbon.userstore.info";
    ClaimDialectDTO claimDialectDTO = claimAdminClient.getAllClaimMappingsByDialect(LOCAL_DIALECT_URI);
    ClaimMappingDTO[] mapping=claimDialectDTO.getClaimMappings();
    String dialectUri = request.getParameter("dialect");
    String pageAction = request.getParameter("action");
%>


<fmt:bundle basename="org.wso2.carbon.identity.claim.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="claim.add"
                       resourceBundle="org.wso2.carbon.identity.claim.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2><fmt:message key='claim.management'/></h2>

        <div id="workArea">

            <%
                String claimUri = request.getParameter("claimUri");
            %>

            <script type="text/javascript">
                String.prototype.format = function (args) {
                    var str = this;
                    return str.replace(String.prototype.format.regex, function (item) {
                        var intVal = parseInt(item.substring(1, item.length - 1));
                        var replace;
                        if (intVal >= 0) {
                            replace = args[intVal];
                        } else if (intVal === -1) {
                            replace = "{";
                        } else if (intVal === -2) {
                            replace = "}";
                        } else {
                            replace = "";
                        }
                        return replace;
                    });
                };
                String.prototype.format.regex = new RegExp("{-?[0-9]+}", "g");

                function setType(chk, hidden) {
                    var val = document.getElementById(chk).checked;
                    var hiddenElement = document.getElementById(hidden);

                    if (val) {
                        hiddenElement.value = "true";
                    } else {
                        hiddenElement.value = "false";
                    }
                }

                function remove(dialect, claim) {
                    CARBON.showConfirmationDialog('<fmt:message key="remove.message1"/>' + claim + '<fmt:message key="remove.message2"/>',
                            function () {
                                location.href = "remove-claim.jsp?dialect=" + dialect + "&claimUri=" + claim;
                            }, null);
                }

                function validate() {

                    var value = document.getElementsByName("displayName")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="displayname.is.required"/>');
                        return false;
                    } else if (value.length > 30) {
                        CARBON.showWarningDialog('<fmt:message key="displayname.is.too.long"/>');
                        return false;
                    }

                    var value = document.getElementsByName("description")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="description.is.required"/>');
                        return false;
                    } else if (value.length > 150) {
                        CARBON.showWarningDialog('<fmt:message key="description.is.too.long"/>');
                        return false;
                    }

                    var value = document.getElementsByName("claimUri")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.is.required"/>');
                        return false;
                    } else if (value.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.is.too.long"/>');
                        return false;
                    }

                    <%--var value = document.getElementsByName("attribute")[0].value;--%>
                    <%--if (value == '') {--%>
                    <%--CARBON.showWarningDialog('<fmt:message key="attribute.is.required"/>');--%>
                    <%--return false;--%>
                    <%--} else if (value.length > 300) {--%>
                    <%--CARBON.showWarningDialog('<fmt:message key="attr.id.is.too.long"/>');--%>
                    <%--return false;--%>
                    <%--}--%>

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

                    //Mapped Attributes Validation
                    <%--var value = document.getElementsByName("attribute")[0].value;--%>
                    <%--var mappedAttributes = value.split(";");--%>
                    <%--var domainSeparator = "/";--%>
                    <%--for (var i = 0; i < mappedAttributes.length; i++) {--%>
                    <%--var index = mappedAttributes[i].indexOf(domainSeparator);--%>
                    <%--if (index >= 0) { //has domain--%>
                    <%--var lastIndex = mappedAttributes[i].lastIndexOf(domainSeparator);--%>
                    <%--if (index == 0) {--%>
                    <%--//domain separator cannot be the first letter of the mapped attribute--%>
                    <%--var message = '<fmt:message key="attribute.domain.required"/>';--%>
                    <%--message = message.format([mappedAttributes[i]]);--%>
                    <%--CARBON.showWarningDialog(message);--%>
                    <%--return false;--%>
                    <%--}--%>
                    <%--else if (index != lastIndex) {--%>
                    <%--//mapped attribute cannot have duplicated domainSeparator--%>
                    <%--var message = '<fmt:message key="attribute.domain.separator.duplicate"/>';--%>
                    <%--message = message.format([mappedAttributes[i]]);--%>
                    <%--CARBON.showWarningDialog(message);--%>
                    <%--return false;--%>
                    <%--} else if (index == (mappedAttributes[i].length - 1)) {--%>
                    <%--//domain separator cannot be the last character of the mapped attribute--%>
                    <%--var message = '<fmt:message key="attribute.domain.mapped.attribute.required"/>';--%>
                    <%--message = message.format([mappedAttributes[i]]);--%>
                    <%--CARBON.showWarningDialog(message);--%>
                    <%--return false;--%>
                    <%--}--%>
                    <%--}--%>
                    <%--}--%>

                    document.addclaim.submit();
                }

            </script>

            <form name="addclaim" action="add-claim-submit.jsp?dialect=<%=dialectUri%>" method="post">
                <table style="width: 100%" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='new.claim.details'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0">
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
                                    <%
                                        if (claimUri != null && claimUri.trim().length() > 0) {
                                    %>
                                    <td class="leftCol-big"><input type="text" name="claimUri" id="claimUri"
                                                                   class="text-box-big" value="<%=claimUri%>"/></td>
                                    <%
                                    } else {
                                    %>
                                    <td class="leftCol-big"><input type="text" name="claimUri" id="claimUri"
                                                                   class="text-box-big"/></td>
                                    <%
                                        }
                                    %>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.attribute'/><font
                                            color="red">*</font></td>
                                    <%if(LOCAL_DIALECT_URI.equals(dialectUri)){%>
                                    <td class="leftCol-big">
                                        <input type="text" name="attribute" id="attribute" class="text-box-big"/>
                                    </td>
                                    <%}else{%>
                                    <td class="leftCol-big">
                                        <select id="claimDropDown" style="float:left; width: 100%">
                                            <%for(ClaimMappingDTO map:mapping){%>
                                            <option value="<%=map.getClaim().getClaimUri()%>" selected> <%=map.getClaim().getClaimUri()%></option>
                                            <%}%>
                                        </select>
                                    </td>
                                    <%}%>

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
                                    <td>
                                        <input type='checkbox' name='supported' id='supported'
                                               onclick="setType('supported','supportedhidden')"/>
                                        <input type='hidden' name='supportedhidden' id='supportedhidden'/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='required'/></td>
                                    <td>
                                        <input type='checkbox' name='required' id='required'
                                               onclick="setType('required','requiredhidden')"/>
                                        <input type='hidden' name='requiredhidden' id='requiredhidden'/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='readonly'/></td>
                                    <td>
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
                                   onclick="javascript:document.location.href='claim-view.jsp?dialect=<%=dialectUri%>&ordinal=1'"/
                            >
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
