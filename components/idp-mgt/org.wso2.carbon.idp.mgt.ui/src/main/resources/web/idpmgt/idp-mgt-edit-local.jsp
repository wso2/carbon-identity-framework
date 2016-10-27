<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.apache.tools.ant.util.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>

<%
    IdentityProvider residentIdentityProvider =
            (IdentityProvider)session.getAttribute("ResidentIdentityProvider");
    String homeRealmId = residentIdentityProvider.getHomeRealmId();
    String openidUrl = null;
    String idPEntityId = null;
    String samlSSOUrl = null;
    String samlSLOUrl = null;
    String oauth1RequestTokenUrl = null;
    String oauth1AuthorizeUrl = null;
    String oauth1AccessTokenUrl = null;
    String authzUrl = null;
    String tokenUrl = null;
    String revokeUrl = null;
    String userInfoUrl = null;
    String oidcCheckSessionEndpoint = null;
    String oidcLogoutEndpoint = null;
    String passiveSTSUrl = null;
    String passivestsIdPEntityId = null;
    String oidcIdpEntityId = null;
    String stsUrl = null;
    String sessionIdleTimeout = null;
    String rememberMeTimeout = null;
    List<Property> destinationURLList = new ArrayList<Property>();
    FederatedAuthenticatorConfig[] federatedAuthenticators = residentIdentityProvider.getFederatedAuthenticatorConfigs();
    for(FederatedAuthenticatorConfig federatedAuthenticator : federatedAuthenticators){
        Property[] properties = federatedAuthenticator.getProperties();
        if(IdentityApplicationConstants.Authenticator.OpenID.NAME.equals(federatedAuthenticator.getName())){
            openidUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL).getValue();
        } else if(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(federatedAuthenticator.getName())){
            idPEntityId = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID).getValue();
            samlSSOUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL).getValue();
            samlSLOUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL).getValue();
            destinationURLList = IdPManagementUIUtil.getPropertySetStartsWith(properties,
                    IdentityApplicationConstants.Authenticator.SAML2SSO.DESTINATION_URL_PREFIX);
            if (destinationURLList.size() == 0) {
                destinationURLList.add(IdPManagementUIUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL));
            }
        } else if(IdentityApplicationConstants.OAuth10A.NAME.equals(federatedAuthenticator.getName())) {
            oauth1RequestTokenUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL).getValue();
            oauth1AuthorizeUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL).getValue();
            oauth1AccessTokenUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL).getValue();
        } else if(IdentityApplicationConstants.Authenticator.OIDC.NAME.equals(federatedAuthenticator.getName())){
            oidcIdpEntityId = IdPManagementUIUtil.getProperty(properties, "IdPEntityId").getValue();
            authzUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL).getValue();
            tokenUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL).getValue();
            revokeUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_REVOKE_URL).getValue();
            userInfoUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_USER_INFO_EP_URL).getValue();
            oidcCheckSessionEndpoint = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OIDC.OIDC_CHECK_SESSION_URL).getValue();
            oidcLogoutEndpoint = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.OIDC.OIDC_LOGOUT_URL).getValue();
        } else if(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME.equals(federatedAuthenticator.getName())){
            passiveSTSUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL).getValue();
            passivestsIdPEntityId = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID).getValue();
        } else if(IdentityApplicationConstants.Authenticator.WSTrust.NAME.equals(federatedAuthenticator.getName())){
            stsUrl = IdPManagementUIUtil.getProperty(properties,
                    IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL).getValue();
        }
    }
    String scimUserEp = null;
    String scimGroupEp = null;
    ProvisioningConnectorConfig[] provisioningConnectors = residentIdentityProvider.getProvisioningConnectorConfigs();
    for(ProvisioningConnectorConfig provisioningConnector : provisioningConnectors){
        if(provisioningConnector.getName().equals("scim")){
            Property[] provisioningProperties = provisioningConnector.getProvisioningProperties();
            if(provisioningProperties == null){
                provisioningProperties = new Property[0];
            }
            for(Property property : provisioningProperties){
                if(property.getName().equals("scimUserEndpoint")){
                    scimUserEp = property.getValue();
                } else if(property.getName().equals("scimGroupEndpoint")){
                    scimGroupEp = property.getValue();
                }
            }
        }
    }

    IdentityProviderProperty[] idpProperties = residentIdentityProvider.getIdpProperties();
    if (idpProperties != null) {
        for (IdentityProviderProperty property : idpProperties) {
            if (property.getName().equals(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT)) {
                sessionIdleTimeout = property.getValue();
            } else if (property.getName().equals(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT)) {
                rememberMeTimeout = property.getValue();
            }
        }
    }

    session.setAttribute("returnToPath", "../idpmgt/idp-mgt-edit-local.jsp");
    session.setAttribute("cancelLink", "../idpmgt/idp-mgt-edit-local.jsp");
    session.setAttribute("backLink", "../idpmgt/idp-mgt-edit-local.jsp");
%>
<script>


jQuery(document).ready(function(){

    jQuery('h2.trigger').click(function(){
        if (jQuery(this).next().is(":visible")) {
            this.className = "active trigger";
        } else {
            this.className = "trigger";
        }
        jQuery(this).next().slideToggle("fast");
        return false; //Prevent the browser jump to the link anchor
    })

})

    initSections("");
    function idpMgtUpdate(){
        if(doValidation()){
            jQuery('#idp-mgt-edit-local-form').submit();
        }
    }
    function idpMgtCancel(){
        location.href = "idp-mgt-list.jsp"
    }
    function doValidation() {
        var reason = "";
        reason = validateEmpty("homeRealmId");
        if (reason != "") {
            CARBON.showWarningDialog("Resident Home Realm ID cannot be empty");
            return false;
        }
        reason = validateEmpty("idPEntityId");
        if (reason != "") {
            CARBON.showWarningDialog("Resident IdP Entity ID cannot be empty");
            return false;
        }
        var isSessionTimeoutValidated = doValidateInput(document.getElementById('sessionIdleTimeout'), "Resident IdP Idle Session Timeout must be numeric value greater than 0");
        if (!isSessionTimeoutValidated) {
            return false;
        }
        var isRememberTimeValidated = doValidateInput(document.getElementById('rememberMeTimeout'), "Resident IdP Remember Me Period must be numeric value greater than 0");
        if (!isRememberTimeValidated) {
            return false;
        }
        return true;
    }
    function onClickAddDestinationUrl() {
        var isValidated = doValidateInput(document.getElementById('destinationURLTxt'), "Please enter a valid destination");
        if (isValidated) {
            addDestinationURL();
        }
    }
    function addDestinationURL() {

        var destinationURL = $("#destinationURLTxt").val();
        if (destinationURL == null || destinationURL.trim().length == 0) {
            CARBON.showWarningDialog("Please enter a valid destination");
            return false;
        }

        destinationURL = destinationURL.trim();

        if (!$("#destinationURLTblRow").length) {
            var row = '<tr id="destinationURLTblRow">' +
                    '    <td></td>' +
                    '    <td>' +
                    '        <table id="destinationURLsTable" style="width: 40%; margin-bottom: 3px;" class="styledInner">' +
                    '            <tbody id="destinationURLsTableBody">' +
                    '            </tbody>' +
                    '        </table>' +
                    '        <input type="hidden" id="destinationURLs" name="destinationURLs" value="">' +
                    '        <input type="hidden" id="currentColumnId" value="0">' +
                    '    </td>' +
                    '</tr>';
            $('#destinationURLInputRow').after(row);
        }

        var destinationURLs = $("#destinationURLs").val();
        var currentColumnId = $("#currentColumnId").val();
        if (destinationURLs == null || destinationURLs.trim().length == 0) {
            $("#destinationURLs").val(destinationURL);
            var row =
                    '<tr id="destinationUrl_' + parseInt(currentColumnId) + '">' +
                    '</td><td style="padding-left: 15px !important; color: rgb(119, 119, 119);font-style: italic;">' + destinationURL +
                    '</td><td><a onclick="removeDestinationURL (\'' + destinationURL + '\', \'destinationUrl_' + parseInt(currentColumnId) + '\');return false;"' +
                    'href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif)"> Delete </a></td></tr>';

            $('#destinationURLsTable tbody').append(row);
        } else {
            var isExist = false;
            $.each(destinationURLs.split(","), function (index, value) {
                if (value === destinationURL) {
                    isExist = true;
                    CARBON.showWarningDialog("Destination URL already exist");
                    return false;
                }
            });
            if (isExist) {
                return false;
            }

            $("#destinationURLs").val(destinationURLs + "," + destinationURL);
            var row =
                    '<tr id="destinationUrl_' + parseInt(currentColumnId) + '">' +
                    '</td><td style="padding-left: 15px !important; color: rgb(119, 119, 119);font-style: italic;">' + destinationURL +
                    '</td><td><a onclick="removeDestinationURL(\'' + destinationURL + '\', \'destinationUrl_' + parseInt(currentColumnId) + '\');return false;"' +
                    'href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif)"> Delete </a></td></tr>';

            $('#destinationURLsTable tr:last').after(row);

        }
        $("#destinationURLTxt").val("");
        $("#currentColumnId").val(parseInt(currentColumnId) + 1);
    }

    function removeDestinationURL(destinationURL, columnId) {

        var destinationURLs = $("#destinationURLs").val();
        var newDestinationURLs = "";
        var isDeletingSelected = false;

        if (destinationURLs.split(',').length <= 1) {
            CARBON.showWarningDialog("You should have atleast one destination URL. Add another URL to remove the last URL", null, null);
            return false;
        }
        if (destinationURLs != null && destinationURLs.trim().length > 0) {
            $.each(destinationURLs.split(","), function (index, value) {
                if (value === destinationURL) {
                    return true;
                }

                if (newDestinationURLs.length > 0) {
                    newDestinationURLs = newDestinationURLs + "," + value;
                } else {
                    newDestinationURLs = value;
                }
            });
        }


        $('#' + columnId).remove();
        $("#destinationURLs").val(newDestinationURLs);

        if (newDestinationURLs.length == 0) {
            $('#destinationURLTblRow').remove();
        }
    }
</script>
<script>
        function downloadRIDPMetadata() {
                jQuery('#idp-mgt-get-RIDP-form').submit();
        }
</script>
<form id="idp-mgt-get-RIDP-form" name="idp-mgt-get-RIDP-form" method="post"
action="idp-mgt-get-ridp-metadata-finish-ajaxprocessor.jsp"></form>
<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='resident.idp'/>
        </h2>
        <div id="workArea">
            <form id="idp-mgt-edit-local-form" name="idp-mgt-edit-local-form" method="post"
                  action="idp-mgt-edit-finish-local-ajaxprocessor.jsp">
                <div class="sectionSeperator "><fmt:message key='resident.realm.config'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='home.realm.id'/>:</td>
                            <td>
                                <input id="homeRealmId" name="homeRealmId" type="text" value="<%=Encode.forHtmlAttribute(homeRealmId)%>" autofocus/>
                                <div class="sectionHelp">
                                    <fmt:message key='home.realm.id.resident.help'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='idle.session.timeout'/><font color="red">*</font>:</td>
                            <td>
                                <input id="sessionIdleTimeout" name="sessionIdleTimeout" type="text" white-list-patterns="^0*[1-9][0-9]*$" value="<%=Encode.forHtmlAttribute(sessionIdleTimeout)%>" autofocus/>
                                <div class="sectionHelp">
                                    <fmt:message key='idle.session.timeout.help'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='remember.me.timeout'/><font color="red">*</font>:</td>
                            <td>
                                <input id="rememberMeTimeout" name="rememberMeTimeout" type="text" white-list-patterns="^0*[1-9][0-9]*$" value="<%=Encode.forHtmlAttribute(rememberMeTimeout)%>" autofocus/>
                                <div class="sectionHelp">
                                    <fmt:message key='remember.me.timeout.help'/>
                                </div>
                            </td>
                        </tr>
                    </table>

                    <h2 id="authenticationconfighead"  class="sectionSeperator trigger active" >
                		<a href="#">Inbound Authentication Configuration</a>
            		</h2>
            		<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="authenticationconfig">

                    <h2 id="openidconfighead"  class="sectionSeperator trigger active" style="background-color: beige;">
                		<a href="#"><fmt:message key='openid.config'/></a>
            		</h2>
            		<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="openidconfig">
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='openid.url'/>:</td>
                            <td><%=Encode.forHtml(openidUrl)%></td>
                        </tr>
                    </table>
                    </div>

                    <h2 id="saml2confighead"  class="sectionSeperator trigger active" style="background-color: beige;">
                		<a href="#"><fmt:message key='saml2.web.sso.config'/></a>
            		</h2>
            		<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="saml2config">

                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='idp.entity.id'/>:</td>
                            <td>
                                <input id="idPEntityId" name="idPEntityId" type="text" value="<%=Encode.forHtmlAttribute(idPEntityId)%>"/>
                                <div class="sectionHelp">
                                    <fmt:message key='idp.entity.id.help'/>
                                </div>
                            </td>
                        </tr>
                        <tr id="destinationURLInputRow">
                            <td class="leftCol-med labelField">
                                <fmt:message key="idp.entity.destinations"/>
                                <font color="red">*</font>
                            </td>
                            <td>
                                <input type="text" id="destinationURLTxt" class="text-box-big" value="" white-list-patterns="http-url https-url"/>
                                <input id="addDestinationURLBtn" type="button" value="<fmt:message key="idp.destination.add"/>"
                                       onclick="onClickAddDestinationUrl()"/>
                            </td>
                        </tr>
                        <!--Start destination url table conditrion check from here.-->
                        <tr id="destinationURLTblRow">
                            <td></td>
                            <td>
                                <table id="destinationURLsTable" style="width: 40%; margin-bottom: 3px;" class="styledInner">
                                    <tbody id="destinationURLsTableBody">
                                    <%
                                        StringBuilder destinationURLsBuilder = new StringBuilder();
                                        int destinationColumnId = 0;
                                        if (destinationURLList != null) {
                                            for (Property destinationURL : destinationURLList) {
                                                if (destinationURLsBuilder.length() > 0) {
                                                    destinationURLsBuilder.append(",").append(destinationURL.getValue());
                                                } else {
                                                    destinationURLsBuilder.append(destinationURL.getValue());
                                                }
                                                String id = StringUtils.replace(destinationURL.getName(), IdentityApplicationConstants.MULTIVALUED_PROPERTY_CHARACTER, "_");
                                    %>
                                    <tr id="<%=Encode.forHtmlAttribute(id)%>">
                                        <td style="padding-left: 15px !important; color: rgb(119, 119, 119);font-style: italic;">
                                            <%=Encode.forHtml(destinationURL.getValue())%>
                                        </td>
                                        <td>
                                            <a onclick="removeDestinationURL('<%=Encode.forJavaScriptAttribute(destinationURL.getValue())%>',
                                                    '<%=Encode.forJavaScriptAttribute(id)%>');return false;"
                                               href="#" class="icon-link"
                                               style="background-image: url(../admin/images/delete.gif)">
                                                Delete
                                            </a>
                                        </td>
                                    </tr>
                                    <%
                                                destinationColumnId++;
                                            }
                                        }
                                    %>
                                    </tbody>
                                </table>
                                <input type="hidden" id="destinationURLs" name="destinationURLs" value="<%=destinationURLsBuilder.length() > 0 ?
         Encode.forHtmlAttribute(destinationURLsBuilder.toString()) : ""%>">
                                <input type="hidden" id="currentColumnId" value="<%=destinationColumnId%>">
                            </td>
                        </tr>
                        <!--End the if conditions from here. For the destination url table-->
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='sso.url'/>:</td>
                            <td><%=Encode.forHtmlContent(samlSSOUrl)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='logout.url'/>:</td>
                            <td><%=Encode.forHtmlContent(samlSLOUrl)%></td>
                        </tr>
                    </table>
                        <br>
                        <button onclick="downloadRIDPMetadata()" type="button" id="downloadResidentIdpMetadataData"
                                                                  name="downloadResidentIdpMetadataData"><fmt:message
                                key='download.metadata.saml'/>
                        </button>
                    </div>

                    <h2 id="oauth1confighead"  class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#"><fmt:message key='oauth1.config'/></a>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="oauth1config">

                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='oauth1.request.endpoint'/>:</td>
                                <td><%=Encode.forHtmlContent(oauth1RequestTokenUrl)%></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='oauth1.authz.endpoint'/>:</td>
                                <td><%=Encode.forHtmlContent(oauth1AuthorizeUrl)%></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='oauth1.access.endpoint'/>:</td>
                                <td><%=Encode.forHtmlContent(oauth1AccessTokenUrl)%></td>
                            </tr>
                        </table>
                    </div>

                    <h2 id="oidcconfighead"  class="sectionSeperator trigger active" style="background-color: beige;">
                		<a href="#"><fmt:message key='oidc.config'/></a>
            		</h2>
            		<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="oidcconfig">

                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='idp.entity.id'/>:</td>
                            <td>
                                <input id="oidcIdPEntityId" name="oidcIdPEntityId" type="text"
                                       value="<%=Encode.forHtmlAttribute(oidcIdpEntityId)%>"/>

                                <div class="sectionHelp">
                                    <fmt:message key='idp.entity.id.help'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='authz.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(authzUrl)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='token.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(tokenUrl)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='revoke.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(revokeUrl)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='user.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(userInfoUrl)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='checksession.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(oidcCheckSessionEndpoint)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='logout.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(oidcLogoutEndpoint)%></td>
                        </tr>
                    </table>
                    </div>

                    <h2 id="passivestsconfighead"  class="sectionSeperator trigger active" style="background-color: beige;">
                		<a href="#"><fmt:message key='passive.sts.local.config'/></a>
            		</h2>
            		<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="passivestsconfig">
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='idp.entity.id'/>:</td>
                            <td>
                                <input id="passiveSTSIdPEntityId" name="passiveSTSIdPEntityId" type="text" value="<%=Encode.forHtmlAttribute(passivestsIdPEntityId)%>"/>
                                <div class="sectionHelp">
                                    <fmt:message key='idp.entity.id.help'/>
                                </div>
                            </td>
                        </tr>

                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='passive.sts.url'/>:</td>
                            <td><%=Encode.forHtmlContent(passiveSTSUrl)%></td>
                        </tr>
                    </table>
                    </div>

                        <h2 id="stsconfighead"  class="sectionSeperator trigger active" style="background-color: beige;">
                            <a href="#"><fmt:message key='sts.local.config'/></a>
                        </h2>
                        <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="stsconfig">
                            <table class="carbonFormTable">
                                <tr>
                                    <td class="leftCol-med labelField" style="padding-top: 5px"><fmt:message key='sts.url'/>:</td>
                                    <td>
                                        <a href="javascript:document.location.href='<%=
                                        Encode.forUriComponent(stsUrl)+"?wsdl"%>'"
                                           class="icon-link"
                                           style="background-image:url(images/sts.gif);margin-left: 0"><%=Encode.forHtmlContent(stsUrl)%>
                                        </a>
                                    </td>
                                    <td>
                                        <a href="javascript:document.location.href='../securityconfig/index.jsp?serviceName=wso2carbon-sts'"
                                           class="icon-link"
                                           style="background-image:url(images/configure.gif);margin-right: 300px">
                                            <fmt:message key='apply.security.policy'/>
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                </div>

                    <h2 id="inboundprovisioningconfighead"  class="sectionSeperator trigger active">
                		<a href="#">Inbound Provisioning Configuration</a>
            		</h2>
            		<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none" id="inboundprovisioningconfig">
            		  <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='scim.user.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(scimUserEp)%></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='scim.group.endpoint'/>:</td>
                            <td><%=Encode.forHtmlContent(scimGroupEp)%></td>
                        </tr>
                    </table>

            		</div>
                </div>
                <div class="buttonRow">
                    <input type="button" value="<fmt:message key='update'/>" onclick="idpMgtUpdate();"/>
                    <input type="button" value="<fmt:message key='cancel'/>" onclick="idpMgtCancel();"/>
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>
