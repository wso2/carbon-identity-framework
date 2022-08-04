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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String externalClaimDialectURI = request.getParameter("externalClaimDialectURI");
    ExternalClaimDTO[] externalClaims = null;
    try {
        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        externalClaims = client.getExternalClaims(externalClaimDialectURI);

        if (externalClaims == null) {
            externalClaims = new ExternalClaimDTO[0];
        }

        session.setAttribute("externalClaims-"+ externalClaimDialectURI, externalClaims);
    } catch (Exception e) {
        String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        String message = resourceBundle.getString("error.while.loading.external.claims");
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
    <carbon:breadcrumb label="list.title"
                       resourceBundle="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript">
        jQuery(document).ready(function () {

            jQuery(".toggle_container").hide();
            /*Hide (Collapse) the toggle containers on load use show() insted of hide() 	in the above code if you want to keep the content section expanded. */
            var triggerHandler = function () {
                if (jQuery(this.parentNode).next().is(":visible")) {
                    $('i', this).get(0).className = "claim-arrow-down";
                    jQuery(this).parent().removeClass('trigger-title-container-light');
                } else {
                    $('i', this).get(0).className = "claim-arrow-up";
                    jQuery(this).parent().addClass('trigger-title-container-light');
                }

                jQuery(this.parentNode).next().slideToggle("fast");
                return false; //Prevent the browser jump to the link anchor
            };
            jQuery("a.trigger-title").click(triggerHandler);
        });
        function removeItem(event, externalClaimDialectURI, externalClaimURI, externalClaimURIForMessage) {
            event.preventDefault();

            function doDelete() {
                $.ajax({
                    type: 'POST',
                    url: 'remove-external-claim-finish-ajaxprocessor.jsp',
                    headers: {
                        Accept: "text/html"
                    },
                    data: 'externalClaimDialectURI=' + externalClaimDialectURI + '&externalClaimURI=' +
                    externalClaimURI,
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign("list-external-claims.jsp?externalClaimDialectURI=" +
                                    externalClaimDialectURI + "&ordinal=1");
                        }
                    }
                });
            }

            CARBON.showConfirmationDialog('<fmt:message key="remove.message1"></fmt:message> ' +
                    externalClaimURIForMessage + '<fmt:message key="remove.message2"/>', doDelete, null);
        }
    </script>
    <style type="text/css">
        .editLink {
            background: transparent url(../admin/images/edit.gif) no-repeat 0px 0px !important;
            float: left !important;
            padding: 0px 0px 0px 20px !important;
            color: inherit !important;
            font-size: 12px !important;
            line-height: 20px !important;
            margin-right: 10px !important;
            margin-bottom: 5px !important;
            margin-left: 5px !important;
            height: 30px;
        }

        .deleteLink {
            background: transparent url(../admin/images/delete.gif) no-repeat 0px 0px !important;
            float: left !important;
            padding: 0px 0px 0px 20px !important;
            color: inherit !important;
            font-size: 12px !important;
            line-height: 20px !important;
            margin-right: 10px !important;
            margin-bottom: 5px !important;
            margin-left: 5px !important;
            height: 30px;
        }

        .trigger-title {
            float: left;
            display: block;
            min-width: 36%;
            vertical-align: bottom;
        }

        .claim-arrow-up {
            background: url('../admin/images/up-arrow.png') left bottom;
            width: 16px;
            height: 16px;
            display: inline-block;
            margin-top: 3px;
        }

        .claim-arrow-down {
            background: url('../admin/images/down-arrow.png') left bottom;
            width: 16px;
            height: 16px;
            display: inline-block;
            margin-top: 3px;
        }

        div.trigger-title-container {
            border: solid 1px #c2c4c6;

            -moz-box-shadow: 3px 3px 3px #888;
            -webkit-box-shadow: 3px 3px 3px #888;
            box-shadow: 3px 3px 3px #888;

            padding: 0;
            background-color: #e9e9e9;
            background-repeat: no-repeat;
            background-position: 5px center;
            padding-left: 5px;
            padding-bottom: 0px !important;
            margin-bottom: 0px !important;
            margin: 0;
            z-index: 1;
            cursor: pointer;
            min-height: 30px;

            font-weight: normal;
            outline-color: -moz-use-text-color;
            outline-style: none;
            outline-width: 0;
            font-size: 13px;

        }

        div.trigger-title-container-light {
            background-color: #fefefe;
            -moz-box-shadow: 0 0 0 transparent;
            -webkit-box-shadow: 0 0 0 transparent;
            box-shadow: 0 0 0 transparent;
            font-weight: bold;
        }

        div.trigger-title-container span {
            line-height: 16px;
        }

    </style>
    <div id="middle">
        <h2><fmt:message key='available.claims.for'/> <%=Encode.forHtml(externalClaimDialectURI)%></h2>

        <div id="workArea">

            <%
                for (int i = 0; i < externalClaims.length; i++) {

                    ExternalClaimDTO externalClaim = externalClaims[i];
                    String externalClaimURI = externalClaim.getExternalClaimURI();
                    String mappedLocalClaimURI = externalClaim.getMappedLocalClaimURI();

                    if (StringUtils.isNotBlank(externalClaimURI)) {%>

            <div class="trigger-title-container">
                <a href="#" class="trigger-title">
                    <i class="claim-arrow-down"></i>
                    <span><%=Encode.forHtmlContent(externalClaimURI)%></span>
                </a>
                <a href="update-external-claim.jsp?externalClaimDialectURI=<%=Encode.forUriComponent(externalClaimDialectURI)%>&externalClaimURI=<%=Encode.forUriComponent(externalClaimURI)%>"
                   class="editLink icon-link"><fmt:message key='edit'/>
                </a>
                <a href="#" class="icon-link deleteLink"
                   style="background-image:url(../identity-claim-mgt/images/delete.gif);"
                   onclick="removeItem(event,
                            '<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(externalClaimDialectURI))%>',
                            '<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(externalClaimURI))%>',
                            '<%=Encode.forJavaScriptAttribute(externalClaimURI)%>');return
                            false;"><fmt:message key='delete'/>
                </a>

                <div style="clear:both"></div>
            </div>

            <div class="toggle_container">
                <table style="width: 100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="leftCol-small"><fmt:message key='claim.uri'/></td>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(externalClaimURI)%>
                        </td>
                    </tr>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='mapped.local.claim'/></td>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(mappedLocalClaimURI)%>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <%
                    }
                }
            %>
        </div>
    </div>
</fmt:bundle>
