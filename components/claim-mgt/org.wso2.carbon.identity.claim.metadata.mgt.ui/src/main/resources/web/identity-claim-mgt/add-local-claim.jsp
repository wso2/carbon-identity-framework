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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<style>
    .sectionHelp {
        padding-left: 17px;
    }
</style>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String localClaimURI = request.getParameter("localClaimURI");
    ClaimDialectDTO[] claimDialects = null;
    try {
        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        claimDialects = client.getClaimDialects();
    } catch (Exception e) {
        String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        String message = resourceBundle.getString("error.while.loading.claim.dialects");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        String forwardTo = "../admin/error.jsp";
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }

    forward();
</script>
<%
    }
%>


<fmt:bundle basename="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.new.claim.mapping"
                       resourceBundle="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2><fmt:message key='add.local.claim'/></h2>

        <div id="workArea">

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

                function validate() {

                    var value = document.getElementsByName("localClaimURI")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.is.required"/>');
                        return false;
                    } else if (value.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.is.too.long"/>');
                        return false;
                    }

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

                    var value = document.getElementsByName("mappedAttribute")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="attribute.is.required"/>');
                        return false;
                    } else if (value.length > 300) {
                        CARBON.showWarningDialog('<fmt:message key="attr.id.is.too.long"/>');
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

                    //Mapped Attributes Validation
                    var value = document.getElementsByName("mappedAttribute")[0].value;
                    var mappedAttributes = value.split(";");
                    var domainSeparator = "/";
                    for (var i = 0; i < mappedAttributes.length; i++) {
                        var index = mappedAttributes[i].indexOf(domainSeparator);
                        if (index >= 0) { //has domain
                            var lastIndex = mappedAttributes[i].lastIndexOf(domainSeparator);
                            if (index == 0) {
                                //domain separator cannot be the first letter of the mapped attribute
                                var message = '<fmt:message key="attribute.domain.required"/>';
                                message = message.format([mappedAttributes[i]]);
                                CARBON.showWarningDialog(message);
                                return false;
                            }
                            else if (index != lastIndex) {
                                //mapped attribute cannot have duplicated domainSeparator
                                var message = '<fmt:message key="attribute.domain.separator.duplicate"/>';
                                message = message.format([mappedAttributes[i]]);
                                CARBON.showWarningDialog(message);
                                return false;
                            } else if (index == (mappedAttributes[i].length - 1)) {
                                //domain separator cannot be the last character of the mapped attribute
                                var message = '<fmt:message key="attribute.domain.mapped.attribute.required"/>';
                                message = message.format([mappedAttributes[i]]);
                                CARBON.showWarningDialog(message);
                                return false;
                            }
                        }
                    }

                    document.addclaim.submit();
                }

            </script>

            <form name="addclaim" action="add-local-claim-finish-ajaxprocessor.jsp" method="post">
                <table style="width: 100%" class="styledLeft">

                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='local.claim.details'/></th>
                    </tr>
                    </thead>

                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0" style="width: 100%">

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='dialect.uri'/></td>
                                    <td
                                            class="leftCol-big"><label type="text" name="dialect"
                                                                       id="dialect"><%=Encode.forHtmlContent(UserCoreConstants.DEFAULT_CARBON_DIALECT)%></label>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='claim.uri'/><font
                                            class="required">*</font></td>
                                    <%
                                        if (StringUtils.isNotBlank(localClaimURI)) {
                                    %>
                                    <td class="leftCol-big"><input type="text" name="localClaimURI" id="localClaimURI"
                                                                   class="text-box-big"
                                                                   value="<%=Encode.forHtmlAttribute(localClaimURI)%>"/></td>
                                    <%
                                    } else {
                                    %>
                                    <td class="leftCol-big"><input type="text" name="localClaimURI" id="localClaimURI"
                                                                   class="text-box-big"/></td>
                                    <%
                                        }
                                    %>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='display.name'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="displayName" id="displayName"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='description'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="description" id="description"
                                                                   class="text-box-big"/></td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.attribute'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="mappedAttribute"
                                                                   id="mappedAttribute" class="text-box-big"/>

                                        <div class="sectionHelp" style="display: block">
                                            <fmt:message key='help.mapped.attribute'/>
                                        </div>
                                    </td>
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
                                   onclick="javascript:document.location.href='add.jsp'"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
