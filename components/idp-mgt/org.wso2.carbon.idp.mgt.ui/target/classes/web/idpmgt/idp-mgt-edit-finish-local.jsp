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

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityProviderMgtServiceClient client = new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setEnable(true);
        identityProvider.setPrimary(true);
        identityProvider.setIdentityProviderName(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME);
        identityProvider.setHomeRealmId(request.getParameter("homeRealmId"));
        FederatedAuthenticatorConfig samlFedAuthn = new FederatedAuthenticatorConfig();
        samlFedAuthn.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        String[] destinationUrls = request.getParameter("destinationURLs").split(",");
        Property[] properties = new Property[1+destinationUrls.length];
        Property property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue(request.getParameter("idPEntityId"));
        properties[0] = property;
        if (destinationUrls != null && destinationUrls.length > 0) {
            for (int destinationCount = 1; destinationCount <= destinationUrls.length; destinationCount++) {
                property = new Property();
                property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.DESTINATION_URL_PREFIX + IdentityApplicationConstants.MULTIVALUED_PROPERTY_CHARACTER + destinationCount);
                property.setValue(destinationUrls[destinationCount-1]);
                properties[destinationCount] = property;
            }

        }
        samlFedAuthn.setProperties(properties);

        FederatedAuthenticatorConfig passiveStsFedAuthn = new FederatedAuthenticatorConfig();
        passiveStsFedAuthn.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        Property[] stsProperties = new Property[1];
        Property stsProperty = new Property();
        stsProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID);
        stsProperty.setValue(request.getParameter("passiveSTSIdPEntityId"));
        stsProperties[0] = stsProperty;
        passiveStsFedAuthn.setProperties(stsProperties);

        FederatedAuthenticatorConfig[] federatedAuthenticators = new FederatedAuthenticatorConfig[2];
        federatedAuthenticators[0] = samlFedAuthn;
        federatedAuthenticators[1] = passiveStsFedAuthn;
        identityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticators);

        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[2];
        IdentityProviderProperty propertySessionIdelTimeout = new IdentityProviderProperty();
        propertySessionIdelTimeout.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        propertySessionIdelTimeout.setValue(request.getParameter("sessionIdleTimeout"));

        IdentityProviderProperty propertyRememberMeTimeout = new IdentityProviderProperty();
        propertyRememberMeTimeout.setName(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT);
        propertyRememberMeTimeout.setValue(request.getParameter("rememberMeTimeout"));
        idpProperties[0] = propertySessionIdelTimeout;
        idpProperties[1] = propertyRememberMeTimeout;
        identityProvider.setIdpProperties(idpProperties);

        client.updateResidentIdP(identityProvider);
        String message = MessageFormat.format(resourceBundle.getString("success.updating.resident.idp"),null);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("error.updating.resident.idp"),
                new Object[]{e.getMessage()});
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    } finally {
        session.removeAttribute("ResidentIdentityProvider");
    }
%>
<script type="text/javascript">
    location.href = "idp-mgt-list.jsp";
</script>
