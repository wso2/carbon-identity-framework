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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.AttributeMappingDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<style>
    .sectionHelp {
        padding-left: 17px;
    }
</style>

<%
    String localClaimURI = request.getParameter("localClaimURI");

    LocalClaimDTO[] localClaims = null;
    localClaims = (LocalClaimDTO[])session.getAttribute("localClaims");

    LocalClaimDTO localClaim = null;
    for (int i = 0; i < localClaims.length; i++) {
        if (localClaims[i].getLocalClaimURI().equals(localClaimURI)) {
            localClaim = localClaims[i];
            break;
        }
    }

    String displayName = null;
    String description = null;
    String regex = null;
    String displayOrder = null;
    String supportedByDefault = null;
    String required = null;
    String readonly = null;

    String attributeMapping = "no.attribute";

    if (localClaim != null) {

        AttributeMappingDTO[] attributeMappings = localClaim.getAttributeMappings();
        ClaimPropertyDTO[] claimPropertyDTOs = localClaim.getClaimProperties();

        Properties claimProperties = new Properties();
        for (int j = 0; j < claimPropertyDTOs.length; j++) {
            claimProperties.put(claimPropertyDTOs[j].getPropertyName(),
                    claimPropertyDTOs[j].getPropertyValue());
        }

        displayName = claimProperties.getProperty("display.name");
        description = claimProperties.getProperty("description");
        regex = claimProperties.getProperty("regex");
        displayOrder = claimProperties.getProperty("display.order");
        supportedByDefault = claimProperties.getProperty("supported.by.default");
        required = claimProperties.getProperty("required");
        readonly = claimProperties.getProperty("readonly");

        if (attributeMapping.length() > 0) {
            attributeMapping = attributeMappings[0].getAttributeName();
        }

    } else {
        String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String unformatted = resourceBundle.getString("error.while.loading.local.claim");
        String message = MessageFormat.format(unformatted, new Object[]{Encode.forHtmlContent(localClaimURI)});

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
    <carbon:breadcrumb label="update"
                       resourceBundle="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2><fmt:message key='claim.management'/></h2>

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

                function removeItem(claim, length) {
                    if (length <= 1) {
                        CARBON.showWarningDialog('<fmt:message key="cannot.remove.default.carbon.dialect.all.claims"/>');
                        return false;
                    } else {
                        CARBON.showConfirmationDialog('<fmt:message key="remove.message1"/>' + claim + '<fmt:message key="remove.message2"/>',
                                function () {
                                    location.href = "remove-local-claim.jsp?localClaimURI=" + claim;
                                }, null);
                    }
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


                    document.updateclaim.submit();
                }

            </script>

            <a href="#" class="icon-link deleteLink"
               style="background-image:url(../identity-claim-mgt/images/delete.gif);"
               onclick="removeItem('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(localClaimURI))%>',
                       '<%=Encode.forJavaScriptAttribute(String.valueOf(localClaims.length))%>');return
                       false;"><fmt:message key='delete'/>
            </a>

            <form name="updateclaim" action="update-local-claim-submit.jsp" method="post">
                <table style="width: 100%" class="styledLeft">

                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='update.local.claim.details'/></th>
                    </tr>
                    </thead>
                    <tbody>

                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0" style="width: 100%">

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='dialect.uri'/></td>
                                    <td class="leftCol-big">
                                        <label type="text" name="dialect" id="dialect"><%=Encode.forHtmlContent(UserCoreConstants.DEFAULT_CARBON_DIALECT)%></label>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='claim.uri'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big">
                                        <input type="text" name="localClaimURI" id="localClaimURI"
                                               value="<%=Encode.forHtmlAttribute(localClaimURI)%>" readonly
                                               class="text-box-big"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='display.name'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big">
                                        <input type="text" name="displayName" id="displayName"
                                               value="<%=Encode.forHtmlAttribute(displayName)%>" class="text-box-big"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='description'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big">
                                        <input type="text" name="description" id="description"
                                               value="<%=Encode.forHtmlAttribute(description)%>" class="text-box-big"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.attribute'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big">
                                        <input type="text" name="mappedAttribute"  id="mappedAttribute"
                                               value="<%=Encode.forHtmlAttribute(attributeMapping)%>"
                                               class="text-box-big"/>
                                        <div class="sectionHelp" style="display: block">
                                            <fmt:message key='help.mapped.attribute'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='regular.expression'/></td>
                                    <% if (StringUtils.isNotBlank(regex)) {%>
                                    <td class="leftCol-big">
                                        <input type="text" name="regex" id="regex"
                                               value="<%=Encode.forHtmlAttribute(regex)%>" class="text-box-big"/>
                                    </td>
                                    <%} else { %>
                                    <td class="leftCol-big">
                                        <input type="text" name="regex" id="regex" class="text-box-big"/>
                                    </td>
                                    <%} %>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='display.order'/></td>
                                    <td class="leftCol-big">
                                        <input type="text" name="displayOrder" id="displayOrder"
                                               value="<%=Encode.forHtmlAttribute(displayOrder)%>"
                                               class="text-box-big"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='supported.by.default'/></td>
                                    <%if (Boolean.getBoolean(supportedByDefault)) { %>
                                    <td class="leftCol-big">
                                        <input type='checkbox' name='supported' id='supported' checked='checked'
                                               onclick="setType('supported','supportedhidden')"/>
                                        <input type='hidden' name='supportedhidden' id='supportedhidden' value='true'/>
                                    </td>
                                    <% } else { %>
                                    <td>
                                        <input type='checkbox' name='supported' id='supported'
                                               onclick="setType('supported','supportedhidden')"/>
                                        <input type='hidden' name='supportedhidden' id='supportedhidden' value='false'/>
                                    </td>
                                    <%} %>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='required'/></td>
                                    <%if (Boolean.getBoolean(required)) { %>
                                    <td class="leftCol-big">
                                        <input type='checkbox' name='required' id='required' checked='checked'
                                               onclick="setType('required','requiredhidden')"/>
                                        <input type='hidden' name='requiredhidden' id='requiredhidden' value='true'/>
                                    </td>
                                    <% } else { %>
                                    <td>
                                        <input type='checkbox' name='required' id='required'
                                               onclick="setType('required','requiredhidden')"/>
                                        <input type='hidden' name='requiredhidden' id='requiredhidden' value='false'/>
                                    </td>
                                    <%} %>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='readonly'/></td>
                                    <%if (Boolean.getBoolean(readonly)) { %>
                                    <td>
                                        <input type='checkbox' name='readonly' id='readonly' checked='checked'
                                               onclick="setType('readonly','readonlyhidden')"/>
                                        <input type='hidden' name='requiredhidden' id='requiredhidden' value='true'/>
                                    </td>
                                    <% } else { %>
                                    <td>
                                        <input type='checkbox' name='readonly' id='readonly'
                                               onclick="setType('readonly','readonlyhidden')"/>
                                        <input type='hidden' name='readonlyhidden' id='readonlyhidden' value='false'/>
                                    </td>
                                    <%} %>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" value="<fmt:message key='update'/>" class="button"
                                   onclick="validate();"/>
                            <input class="button" type="reset" value="<fmt:message key='cancel'/>"
                                   onclick="javascript:document.location.href='list-local-claims.jsp?ordinal=1'"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
