/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.*;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.BasicParserPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SAML2SSOFederatedAuthenticatorConfig extends FederatedAuthenticatorConfig {
    private static Log log = LogFactory.getLog(SAML2SSOFederatedAuthenticatorConfig.class);

    //TODO override build
    //parse


    private static EntityDescriptor generateMetadataObjectFromString(String metadataString) {
        EntityDescriptor entityDescriptor = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(metadataString.getBytes()));
            Element node = document.getDocumentElement();
            DOMMetadataProvider idpMetaDataProvider = new DOMMetadataProvider(node);
            idpMetaDataProvider.setRequireValidMetadata(true);
            idpMetaDataProvider.setParserPool(new BasicParserPool());
            idpMetaDataProvider.initialize();
            XMLObject xmlObject = idpMetaDataProvider.getMetadata();
            entityDescriptor = (EntityDescriptor) xmlObject;
        } catch (MetadataProviderException | SAXException | ParserConfigurationException | IOException e) {
            log.error("Error While reading Service Provider metadata xml", e);
        }
        return entityDescriptor;
    }


    private void setAssertionConsumerUrl(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        //Assertion Consumer URL
        //search for the url with the post binding, if there is no post binding select the default url
        List<AssertionConsumerService> assertionConsumerServices = spssoDescriptor.getAssertionConsumerServices();
        if (CollectionUtils.isNotEmpty(assertionConsumerServices)) {
            List<String> acs = new ArrayList<>();
            boolean foundAssertionConsumerUrl = false;
            for (AssertionConsumerService assertionConsumerService : assertionConsumerServices) {
                acs.add(assertionConsumerService.getLocation());
                if (assertionConsumerService.isDefault()) {
                    samlssoServiceProviderDO.setDefaultAssertionConsumerUrl(assertionConsumerService.getLocation());//changed
                    samlssoServiceProviderDO.setAssertionConsumerUrl(assertionConsumerService.getLocation());//changed
                    foundAssertionConsumerUrl = true;
                }
            }
            samlssoServiceProviderDO.setAssertionConsumerUrls(acs);
            //select atleast one
            if (!foundAssertionConsumerUrl) {
                samlssoServiceProviderDO.setDefaultAssertionConsumerUrl(assertionConsumerServices.get(0).getLocation());
            }
        }
    }

    private void setNameIDFormat(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        List<NameIDFormat> nameIDFormats = spssoDescriptor.getNameIDFormats();
        samlssoServiceProviderDO.setNameIDFormat(nameIDFormats.get(0).getFormat());
    }
    private void  setClaims(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        List<AttributeConsumingService> services = new ArrayList<>();
        services = spssoDescriptor.getAttributeConsumingServices();
        if (CollectionUtils.isNotEmpty(services)) {
            //assuming that only one AttrbuteComsumingIndex exists
            AttributeConsumingService service = services.get(0);
            List<RequestedAttribute> attributes = service.getRequestAttributes();
            for (RequestedAttribute attribute : attributes){
                //set the values to claims
            }
        } else {
        }
    }
    private void setDoSignAssertions (SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        samlssoServiceProviderDO.setDoSignAssertions(spssoDescriptor.getWantAssertionsSigned());
    }
    private void  setDoValidateSignatureInRequests(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        samlssoServiceProviderDO.setDoValidateSignatureInRequests(spssoDescriptor.isAuthnRequestsSigned());
    }
    private void  setSingleLogoutServices(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        List<SingleLogoutService> singleLogoutServices = spssoDescriptor.getSingleLogoutServices();
        if (CollectionUtils.isNotEmpty(singleLogoutServices)) {
            boolean foundSingleLogoutServicePostBinding = false;
            for (SingleLogoutService singleLogoutService : singleLogoutServices) {
                if (singleLogoutService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI)) {
                    samlssoServiceProviderDO.setSloRequestURL(singleLogoutService.getLocation());
                    samlssoServiceProviderDO.setSloResponseURL(singleLogoutService.getResponseLocation());//changed
                    foundSingleLogoutServicePostBinding = true;
                    break;
                }
            }
            if (!foundSingleLogoutServicePostBinding) {
            }
            samlssoServiceProviderDO.setSloRequestURL(singleLogoutServices.get(0).getLocation());
            samlssoServiceProviderDO.setSloResponseURL(singleLogoutServices.get(0).getResponseLocation());//chnaged
            samlssoServiceProviderDO.setDoSingleLogout(true);
        } else {
            samlssoServiceProviderDO.setDoSingleLogout(false);
        }
    }
    private void  setX509Certificate(EntityDescriptor entityDescriptor,SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        List<KeyDescriptor> descriptors = spssoDescriptor.getKeyDescriptors();
        if (descriptors != null && descriptors.size() > 0) {
            KeyDescriptor descriptor = descriptors.get(0);
            if (descriptor != null) {
                if (descriptor.getUse().toString().equals("SIGNING")) {

                    try {
                        samlssoServiceProviderDO.setX509Certificate(org.opensaml.xml.security.keyinfo.KeyInfoHelper.getCertificates(descriptor.getKeyInfo()).get(0));
                        samlssoServiceProviderDO.setCertAlias(entityDescriptor.getEntityID());
                    } catch (java.security.cert.CertificateException ex) {
                        log.error("Error While setting Certificate and alias", ex);
                    }catch(java.lang.Exception ex){
                        log.error("Error While setting Certificate and alias", ex);
                    }
                }
            }
        }
    }
    private void  setSigningAlgorithmUri(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        samlssoServiceProviderDO.setSigningAlgorithmUri("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
    }
    private void  setDigestAlgorithmUri(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO){
        samlssoServiceProviderDO.setDigestAlgorithmUri("http://www.w3.org/2000/09/xmldsig#sha1");
    }

    /**
     * Convert metadata string to samlssoServiceProviderDO object
     *
     * @param metadata ,samlssoServiceProviderDO
     * @return samlssoServiceProviderDO

     */


    private static FederatedAuthenticatorConfig parse(EntityDescriptor entityDescriptor,FederatedAuthenticatorConfig federatedAuthenticatorConfig) {

        if (entityDescriptor != null) {
            List<RoleDescriptor> roleDescriptors = entityDescriptor.getRoleDescriptors();
            //TODO: handle when multiple role descriptors are available
            //assuming only one IDPSSO is inside the entitydescripter
            RoleDescriptor roleDescriptor = roleDescriptors.get(0);
            IDPSSODescriptor spssoDescriptor = (IDPSSODescriptor) roleDescriptor;
            this.setAssertionConsumerUrl(spssoDescriptor,samlssoServiceProviderDO);
            //Response Signing Algorithm - not found
            //Response Digest Algorithm - not found
            //NameID format
            this.setNameIDFormat(spssoDescriptor,samlssoServiceProviderDO);
            //Enable Assertion Signing
            this.setDoSignAssertions(spssoDescriptor,samlssoServiceProviderDO);
            //Enable Signature Validation in Authentication Requests and Logout Requests
            this.setDoValidateSignatureInRequests(spssoDescriptor,samlssoServiceProviderDO);
            //Enable Assertion Encryption - not found
            //Enable Single Logout
            this.setSingleLogoutServices(spssoDescriptor,samlssoServiceProviderDO);
            //Enable Attribute Profile - no method found
            //TODO: currently this is stored as a property in registry. need to add it to the metadata file
            // Enable Audience Restriction - not found
            // Enable Recipient Validation - not found
            //Enable IdP Initiated SSO - not found
            // Enable IdP Initiated SLO - not found
            this.setClaims(spssoDescriptor,samlssoServiceProviderDO);
            //setting response signing algorythm - Hardcoded
            //not found in the the spec, no in the SPSSODescriptor
            this.setSigningAlgorithmUri(spssoDescriptor,samlssoServiceProviderDO);
            //setting response digest algorythm - Hardcoded
            //not found in the the spec, no in the SPSSODescriptor
            this.setDigestAlgorithmUri(spssoDescriptor,samlssoServiceProviderDO);
            //set alias and certificate
            this.setX509Certificate(entityDescriptor,spssoDescriptor,samlssoServiceProviderDO);
        }
        Property properties [] = new Property[25];
        Property property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue(entityDescriptor.getEntityID());
        properties[0] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue("");//not available in the metadata specification
        properties[1] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue(paramMap.get("ssoUrl"));
        properties[2] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
        if ("on".equals(paramMap.get("authnRequestSigned"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[3] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
        if ("on".equals(paramMap.get("sloEnabled"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[4] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        property.setValue(paramMap.get("logoutUrl"));
        properties[5] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
        if ("on".equals(paramMap.get("logoutRequestSigned"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[6] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
        if ("on".equals(paramMap.get("authnResponseSigned"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[7] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
        if ("1".equals(paramMap.get("saml2_sso_user_id_location"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[8] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
        if ("on".equals(paramMap.get("IsEnableAssetionEncription"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[9] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
        if ("on".equals(paramMap.get("isEnableAssertionSigning"))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[10] = property;

        property = new Property();
        property.setName("commonAuthQueryParams");

        if (paramMap.get("samlQueryParam") != null
                && paramMap.get("samlQueryParam").trim().length() > 0) {
            property.setValue(paramMap.get("samlQueryParam"));
        }

        properties[11] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.REQUEST_METHOD);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.REQUEST_METHOD));
        properties[12] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM));
        properties[13] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.DIGEST_ALGORITHM);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.DIGEST_ALGORITHM));
        properties[14] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_COMPARISON_LEVEL);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_COMPARISON_LEVEL));
        properties[15] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_NAME_ID_POLICY);
        if ("on".equals(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_NAME_ID_POLICY))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[16] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.FORCE_AUTHENTICATION);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.FORCE_AUTHENTICATION));
        properties[17] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM_POST);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM_POST));
        properties[18] = property;

        String authenticationContextClass = paramMap.get(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_CLASS);
        if (IdentityApplicationConstants.Authenticator.SAML2SSO.CUSTOM_AUTHENTICATION_CONTEXT_CLASS_OPTION.equals(authenticationContextClass)) {
            authenticationContextClass = paramMap.get(IdentityApplicationConstants.Authenticator.SAML2SSO.ATTRIBUTE_CUSTOM_AUTHENTICATION_CONTEXT_CLASS);
        }
        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_CLASS);
        property.setValue(authenticationContextClass);
        properties[19] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.ATTRIBUTE_CONSUMING_SERVICE_INDEX);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.ATTRIBUTE_CONSUMING_SERVICE_INDEX));
        properties[20] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_CERT);
        if ("on".equals(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_CERT))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[21] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_AUTHN_CONTEXT);
        property.setValue(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_AUTHN_CONTEXT));
        properties[22] = property;

        property = new Property();
        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_PROTOCOL_BINDING);
        if ("on".equals(paramMap
                .get(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_PROTOCOL_BINDING))) {
            property.setValue("true");
        } else {
            property.setValue("false");
        }
        properties[23] = property;

        if (paramMap.containsKey("metadataFromFileSystem")) {
            property = new Property();
            property.setName("metadataFromFileSystem");
            property.setValue(paramMap.get("metadataFromFileSystem"));
        }
        properties[24] = property;

        federatedAuthenticatorConfig.setProperties(properties);
















        return federatedAuthenticatorConfig;
    }





    public static FederatedAuthenticatorConfig build(OMElement saml2FederatedAuthenticatorConfigOM) {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        EntityDescriptor entityDescriptor = null;
        entityDescriptor = generateMetadataObjectFromString(saml2FederatedAuthenticatorConfigOM.toString());
        if(entityDescriptor!=null){
            federatedAuthenticatorConfig = parse(entityDescriptor,federatedAuthenticatorConfig);
        }








        return federatedAuthenticatorConfig;
    }



    /**
     *
     */
    private static final long serialVersionUID = -171672098979315832L;

    /**
     * The IdP's Entity Issuer value
     */
    private String idpEntityId;

    /**
     * If Single Logout is enabled
     */
    private boolean isLogoutEnabled;

    /**
     * The SAML2 Web SSO URL of the IdP
     */
    private String saml2SSOUrl;

    /**
     * If LogoutRequest should be signed
     */
    private boolean isLogoutRequestSigned;

    /**
     * If the LogoutRequestUrl is different from ACS URL
     */
    private String logoutRequestUrl;

    /*
     * The service provider's Entity Id
     */
    private String spEntityId;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnRequestSigned;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnResponseSigned;

    /**
     * If the AuthnResponse has to be encrypted
     */
    private boolean isAuthnResponseEncrypted;

    /**
     * If User ID is found among claims
     */
    private boolean isUserIdInClaims;

    public SAML2SSOFederatedAuthenticatorConfig(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        for (Property property : federatedAuthenticatorConfig.getProperties()) {
            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                idpEntityId = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID.equals(property.getName())) {
                spEntityId = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(property.getName())) {
                saml2SSOUrl = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED.equals(
                    property.getName())) {
                isAuthnRequestSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED.equals(
                    property.getName())) {
                isLogoutEnabled = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED.equals(
                    property.getName())) {
                isLogoutRequestSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(property.getName())) {
                logoutRequestUrl = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED.equals(
                    property.getName())) {
                isAuthnResponseSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION.equals(
                    property.getName())) {
                isAuthnResponseEncrypted = Boolean.parseBoolean(property.getValue());
            }
        }
    }

    @Override
    public boolean isValid() {

        if (IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID))
                && IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID))
                && IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL))) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return IdentityApplicationConstants.Authenticator.SAML2SSO.NAME;
    }

    ////////////////////////////// Getters ///////////////////////////

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public boolean isLogoutEnabled() {
        return isLogoutEnabled;
    }

    public boolean isLogoutRequestSigned() {
        return isLogoutRequestSigned;
    }

    public String getLogoutRequestUrl() {
        return logoutRequestUrl;
    }

    public String getSpEntityId() {
        return spEntityId;
    }

    public boolean isAuthnRequestSigned() {
        return isAuthnRequestSigned;
    }

    public boolean isAuthnResponseSigned() {
        return isAuthnResponseSigned;
    }

    public boolean isUserIdInClaims() {
        return isUserIdInClaims;
    }

    public String getSaml2SSOUrl() {
        return saml2SSOUrl;
    }

    public boolean isAuthnResponseEncrypted() {
        return isAuthnResponseEncrypted;
    }
}
