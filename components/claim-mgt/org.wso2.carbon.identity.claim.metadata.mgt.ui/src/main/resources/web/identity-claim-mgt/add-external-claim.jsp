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
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.user.api.Claim" %>

<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<style>
    .sectionHelp {
        margin-top: 7px;
        padding-bottom: 10px;
        color: #555;
    }
</style>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String externalClaimURI = request.getParameter("externalClaimURI");
    String externalClaimDialectURI = request.getParameter("externalClaimDialectURI");
    String mappedLocalClaimURI = request.getParameter("mappedLocalClaimURI");

    ClaimDialectDTO[] claimDialects = null;
    List<String> availableLocalClaimsURIs = new ArrayList<String>();
    ExternalClaimDTO[] existingExternalClaims = null;

    try {
        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        claimDialects = client.getClaimDialects();

        LocalClaimDTO[] availableLocalClaims = client.getLocalClaims();
        if (availableLocalClaims != null) {
            for (LocalClaimDTO localClaim : availableLocalClaims) {
                availableLocalClaimsURIs.add(localClaim.getLocalClaimURI());
            }
        }

        if (StringUtils.isBlank(externalClaimDialectURI) && claimDialects != null) {
            for (ClaimDialectDTO claimDialectDTO : claimDialects) {
                if (!UserCoreConstants.DEFAULT_CARBON_DIALECT.equalsIgnoreCase(claimDialectDTO.getClaimDialectURI())) {
                    externalClaimDialectURI = claimDialectDTO.getClaimDialectURI();
                    break;
                }
            }
        }

        existingExternalClaims = client.getExternalClaims(externalClaimDialectURI);
        if (existingExternalClaims != null) {
            for (ExternalClaimDTO externalClaimDTO : existingExternalClaims) {
                availableLocalClaimsURIs.remove(externalClaimDTO.getMappedLocalClaimURI());
            }
        }

    } catch (Exception e) {
        String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        String message = resourceBundle.getString("error.while.loading.claim.details");
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
        <h2><fmt:message key='add.external.claim'/></h2>

        <div id="workArea">

            <script type="text/javascript">
                function validate() {

                    var value = document.getElementsByName("externalClaimDialectURI")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="dialect.uri.is.required"/>');
                        return false;
                    }

                    var value = document.getElementsByName("externalClaimURI")[0].value;
                    if (isEmpty(value)) {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.cannot.be.empty"/>');
                        return false;
                    } else if (value.length > 255) {
                        CARBON.showWarningDialog('<fmt:message key="claim.uri.is.too.long"/>');
                        return false;
                    }

                    var value = document.getElementsByName("mappedLocalClaimURI")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="mapped.local.claim.uri.is.required"/>');
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

                    document.addclaim.submit();
                }

                function getClaimDialect() {
                    document.addclaim.action = "add-external-claim.jsp";
                    document.addclaim.submit();
                }

                function isEmpty(value){
                  return (value == null || value.trim() == '');
                }

            </script>

            <form name="addclaim" action="add-external-claim-finish-ajaxprocessor.jsp" method="post">
                <table style="width: 100%" class="styledLeft">

                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='external.claim.details'/></th>
                    </tr>
                    </thead>

                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" cellspacing="0" style="width: 100%">

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='dialect.uri'/><font
                                            color="red">*</font></td>
                                    <td class="leftCol-big">
                                        <select id="externalClaimDialectURI" name="externalClaimDialectURI"
                                                onchange="getClaimDialect();">
                                        <%
                                            if (claimDialects != null && claimDialects.length > 0) {
                                                for (int i = 0; i < claimDialects.length; i++) {
                                                    String claimDialectURI = claimDialects[i].getClaimDialectURI();
                                                    if (!UserCoreConstants.DEFAULT_CARBON_DIALECT.equalsIgnoreCase(claimDialectURI)) {

                                                        if (StringUtils.isNotBlank(externalClaimDialectURI) &&
                                                                externalClaimDialectURI.equalsIgnoreCase(claimDialectURI)) {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(claimDialectURI)%>" selected><%=Encode.forHtmlContent(claimDialectURI)%></option>
                                        <%
                                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(claimDialectURI)%>"><%=Encode.forHtmlContent(claimDialectURI)%></option>
                                        <%
                                                        }
                                                    }
                                                }
                                            }
                                        %>
                                        </select>
                                        <div class="sectionHelp"><fmt:message key="select.dialect.uri.help"/></div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='external.claim.uri'/><font
                                            color="red">*</font>
                                    </td>
                                    <%
                                        if (StringUtils.isNotBlank(externalClaimURI)) {
                                    %>
                                    <td class="leftCol-big"><input type="text" name="externalClaimURI"
                                                                   id="externalClaimURI"
                                                                   class="text-box-big"
                                                                   value="<%=Encode.forHtmlAttribute(externalClaimURI)%>"/>
                                        <div class="sectionHelp"><fmt:message key="claim.uri.help"/></div>
                                    </td>
                                    <%
                                    } else {
                                    %>
                                    <td class="leftCol-big"><input type="text" name="externalClaimURI"
                                                                   id="externalClaimURI"
                                                                   class="text-box-big"/>
                                        <div class="sectionHelp"><fmt:message key="claim.uri.help"/></div>
                                    </td>
                                    <%
                                        }
                                    %>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.local.claim'/><font
                                            color="red">*</font></td>
                                    <td class="leftCol-big">
                                        <select id="mappedLocalClaimURI" name="mappedLocalClaimURI">
                                            <%
                                                for (String localClaimURI : availableLocalClaimsURIs) {
                                                    if (StringUtils.isNotBlank(mappedLocalClaimURI) &&
                                                            mappedLocalClaimURI.equalsIgnoreCase(localClaimURI)) {
                                            %>
                                            <option value="<%=Encode.forHtmlAttribute(localClaimURI)%>" selected><%=Encode.forHtmlContent(localClaimURI)%></option>
                                            <%
                                                    } else {
                                            %>
                                            <option value="<%=Encode.forHtmlAttribute(localClaimURI)%>"><%=Encode.forHtmlContent(localClaimURI)%></option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                        <div class="sectionHelp"><fmt:message key="select.mapped.claim"/></div>
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
