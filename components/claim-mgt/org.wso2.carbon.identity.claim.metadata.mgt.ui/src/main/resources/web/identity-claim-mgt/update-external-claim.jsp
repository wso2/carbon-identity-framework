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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>

<style>
    .sectionHelp {
        padding-left: 17px;
    }
</style>

<%
    String externalClaimURI = request.getParameter("externalClaimURI");
    String externalClaimDialectURI = request.getParameter("externalClaimDialectURI");

    ExternalClaimDTO[] externalClaims = null;
    externalClaims = (ExternalClaimDTO[])session.getAttribute("externalClaims-"+ externalClaimDialectURI);

    ExternalClaimDTO externalClaim = null;
    for (int i = 0; i < externalClaims.length; i++) {
        if (externalClaims[i].getExternalClaimURI().equals(externalClaimURI)) {
            externalClaim = externalClaims[i];
            break;
        }
    }

    if (externalClaim == null) {
        String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String unformatted = resourceBundle.getString("error.while.loading.external.claim");
        String message = MessageFormat.format(unformatted, new Object[]{Encode.forHtmlContent(externalClaimURI)});

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


                function removeItem(dialect, claim) {

                    CARBON.showConfirmationDialog('<fmt:message key="remove.message1"/>' + claim + '<fmt:message key="remove.message2"/>',
                            function () {
                                location.href = "remove-external-claim.jsp?externalClaimDialectURI=" + dialect + "&externalClaimURI=" + claim;
                            }, null);
                }

                function validate() {

                    var value = document.getElementsByName("mappedLocalClaimURI")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="mapped.local.claim.uri.is.required"/>');
                        return false;
                    } else if (value.length > 100) {
                        CARBON.showWarningDialog('<fmt:message key="mapped.local.claim.uri.is.too.long"/>');
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

                    document.updateclaim.submit();
                }
            </script>

            <div style="height:30px;">
                <a href="#" class="icon-link deleteLink"
                   style="background-image:url(../identity-claim-mgt/images/delete.gif);"
                   onclick="removeItem('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(externalClaimDialectURI))%>',
                           '<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(externalClaimURI))%>');return
                           false;"><fmt:message key='delete'/>
                </a>
            </div>

            <form name="updateclaim" action="update-external-claim-submit.jsp" method="post">
                <table style="width: 100%" class="styledLeft">

                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='update.external.claim.details'/></th>
                    </tr>
                    </thead>

                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0" style="width: 100%">

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='dialect.uri'/></td>
                                    <td
                                            class="leftCol-big"><input type="text" name="externalClaimDialectURI"
                                                                       id="externalClaimDialectURI"
                                                                       value="<%=Encode.forHtmlAttribute(externalClaimDialectURI)%>"
                                                                       readonly class="text-box-big"/></td>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='claim.uri'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="externalClaimURI"
                                               id="externalClaimURI"
                                               value="<%=Encode.forHtmlAttribute(externalClaimURI)%>"
                                               readonly class="text-box-big"/></td>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.local.claim'/><font
                                            class="required">*</font></td>
                                    <td class="leftCol-big"><input type="text" name="mappedLocalClaimURI"
                                                                   id="mappedLocalClaimURI"
                                                                   value="<%=Encode.forHtmlAttribute(externalClaim.getMappedLocalClaimURI())%>"
                                                                   class="text-box-big"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" value="<fmt:message key='update'/>" class="button"
                                   onclick="validate();"/>
                            <input class="button" type="reset" value="<fmt:message key='cancel'/>"
                                   onclick="javascript:document.location.href='list-external-claims.jsp?externalClaimDialectURI=<%=Encode.forUriComponent(externalClaimDialectURI)%>&ordinal=1'"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
