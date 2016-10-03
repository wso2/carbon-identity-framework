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
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
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


    private static EntityDescriptor generateMetadataObjectFromString(String metadataString) throws IdentityApplicationManagementException {
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
            throw new IdentityApplicationManagementException("Error while converting file content to entity descriptor");
        }
        return entityDescriptor;
    }


//    private void setAssertionConsumerUrl(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        //Assertion Consumer URL
//        //search for the url with the post binding, if there is no post binding select the default url
//        List<AssertionConsumerService> assertionConsumerServices = spssoDescriptor.getAssertionConsumerServices();
//        if (CollectionUtils.isNotEmpty(assertionConsumerServices)) {
//            List<String> acs = new ArrayList<>();
//            boolean foundAssertionConsumerUrl = false;
//            for (AssertionConsumerService assertionConsumerService : assertionConsumerServices) {
//                acs.add(assertionConsumerService.getLocation());
//                if (assertionConsumerService.isDefault()) {
//                    samlssoServiceProviderDO.setDefaultAssertionConsumerUrl(assertionConsumerService.getLocation());//changed
//                    samlssoServiceProviderDO.setAssertionConsumerUrl(assertionConsumerService.getLocation());//changed
//                    foundAssertionConsumerUrl = true;
//                }
//            }
//            samlssoServiceProviderDO.setAssertionConsumerUrls(acs);
//            //select atleast one
//            if (!foundAssertionConsumerUrl) {
//                samlssoServiceProviderDO.setDefaultAssertionConsumerUrl(assertionConsumerServices.get(0).getLocation());
//            }
//        }
//    }
//
//    private void setNameIDFormat(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        List<NameIDFormat> nameIDFormats = spssoDescriptor.getNameIDFormats();
//        samlssoServiceProviderDO.setNameIDFormat(nameIDFormats.get(0).getFormat());
//    }
//
//    private void setClaims(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        List<AttributeConsumingService> services = new ArrayList<>();
//        services = spssoDescriptor.getAttributeConsumingServices();
//        if (CollectionUtils.isNotEmpty(services)) {
//            //assuming that only one AttrbuteComsumingIndex exists
//            AttributeConsumingService service = services.get(0);
//            List<RequestedAttribute> attributes = service.getRequestAttributes();
//            for (RequestedAttribute attribute : attributes) {
//                //set the values to claims
//            }
//        } else {
//        }
//    }
//
//    private void setDoSignAssertions(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        samlssoServiceProviderDO.setDoSignAssertions(spssoDescriptor.getWantAssertionsSigned());
//    }
//
//    private void setDoValidateSignatureInRequests(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        samlssoServiceProviderDO.setDoValidateSignatureInRequests(spssoDescriptor.isAuthnRequestsSigned());
//    }
//
//    private void setSingleLogoutServices(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        List<SingleLogoutService> singleLogoutServices = spssoDescriptor.getSingleLogoutServices();
//        if (CollectionUtils.isNotEmpty(singleLogoutServices)) {
//            boolean foundSingleLogoutServicePostBinding = false;
//            for (SingleLogoutService singleLogoutService : singleLogoutServices) {
//                if (singleLogoutService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI)) {
//                    samlssoServiceProviderDO.setSloRequestURL(singleLogoutService.getLocation());
//                    samlssoServiceProviderDO.setSloResponseURL(singleLogoutService.getResponseLocation());//changed
//                    foundSingleLogoutServicePostBinding = true;
//                    break;
//                }
//            }
//            if (!foundSingleLogoutServicePostBinding) {
//            }
//            samlssoServiceProviderDO.setSloRequestURL(singleLogoutServices.get(0).getLocation());
//            samlssoServiceProviderDO.setSloResponseURL(singleLogoutServices.get(0).getResponseLocation());//chnaged
//            samlssoServiceProviderDO.setDoSingleLogout(true);
//        } else {
//            samlssoServiceProviderDO.setDoSingleLogout(false);
//        }
//    }
//
//    private void setX509Certificate(EntityDescriptor entityDescriptor, SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        List<KeyDescriptor> descriptors = spssoDescriptor.getKeyDescriptors();
//        if (descriptors != null && descriptors.size() > 0) {
//            KeyDescriptor descriptor = descriptors.get(0);
//            if (descriptor != null) {
//                if (descriptor.getUse().toString().equals("SIGNING")) {
//
//                    try {
//                        samlssoServiceProviderDO.setX509Certificate(org.opensaml.xml.security.keyinfo.KeyInfoHelper.getCertificates(descriptor.getKeyInfo()).get(0));
//                        samlssoServiceProviderDO.setCertAlias(entityDescriptor.getEntityID());
//                    } catch (java.security.cert.CertificateException ex) {
//                        log.error("Error While setting Certificate and alias", ex);
//                    } catch (java.lang.Exception ex) {
//                        log.error("Error While setting Certificate and alias", ex);
//                    }
//                }
//            }
//        }
//    }
//
//    private void setSigningAlgorithmUri(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        samlssoServiceProviderDO.setSigningAlgorithmUri("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
//    }
//
//    private void setDigestAlgorithmUri(SPSSODescriptor spssoDescriptor, SAMLSSOServiceProviderDO samlssoServiceProviderDO) {
//        samlssoServiceProviderDO.setDigestAlgorithmUri("http://www.w3.org/2000/09/xmldsig#sha1");
//    }

    /**
     * Convert metadata string to FederatedAuthenticatorConfigobject
     *
     * @param entityDescriptor ,federatedAuthenticatorConfig
     * @return samlssoServiceProviderDO
     */


    private static FederatedAuthenticatorConfig parse(EntityDescriptor entityDescriptor, FederatedAuthenticatorConfig federatedAuthenticatorConfig, StringBuilder builder) throws IdentityApplicationManagementException {

        if (entityDescriptor != null) {
            List<RoleDescriptor> roleDescriptors = entityDescriptor.getRoleDescriptors();
            //TODO: handle when multiple role descriptors are available
            //assuming only one IDPSSO is inside the entitydescripter
            if (roleDescriptors != null && roleDescriptors.size() > 0) {
                RoleDescriptor roleDescriptor = roleDescriptors.get(0);
                if (roleDescriptor != null) {

                    IDPSSODescriptor idpssoDescriptor;
                    try {
                        idpssoDescriptor = (IDPSSODescriptor) roleDescriptor;
                    } catch (ClassCastException ex) {
                        throw new IdentityApplicationManagementException("No IDP Descriptors found, invalid file content");
                    }
                    Property properties[] = new Property[24];
                    Property property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
                    if (entityDescriptor.getEntityID() != null) {
                        property.setValue(entityDescriptor.getEntityID());

                    } else {
                        property.setValue("");
                        throw new IdentityApplicationManagementException("No Entity ID found, invalid file content");
                    }
                    properties[0] = property;


                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
                    property.setValue("");//not available in the metadata specification
                    properties[1] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
                    List<SingleSignOnService> singleSignOnServices = idpssoDescriptor.getSingleSignOnServices();
                    //assuming there is only one ssoservice;
                    if (singleSignOnServices != null && singleSignOnServices.size() > 0) {
                        SingleSignOnService singleSignOnService = singleSignOnServices.get(0);
                        if (singleSignOnService != null) {
                            if (singleSignOnService.getLocation() != null) {
                                property.setValue(singleSignOnService.getLocation());
                            } else {
                                property.setValue("");
                                throw new IdentityApplicationManagementException("No SSO URL, invalid file content");
                            }
                        } else {
                            property.setValue("");
                            throw new IdentityApplicationManagementException("No SSO URL, invalid file content");
                        }
                    } else {
                        property.setValue("");
                        throw new IdentityApplicationManagementException("No SSO URL, invalid file content");
                    }
                    properties[2] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
                    if (idpssoDescriptor.getWantAuthnRequestsSigned() != null && idpssoDescriptor.getWantAuthnRequestsSigned() == true) {
                        property.setValue("true");
                    } else {
                        property.setValue("false");
                    }
                    properties[3] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
                    List<SingleLogoutService> singleLogoutServices = idpssoDescriptor.getSingleLogoutServices();
                    if (singleLogoutServices != null && CollectionUtils.isNotEmpty(singleLogoutServices)) {
                        property.setValue("true");
                    } else {
                        property.setValue("false");
                    }

                    properties[4] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);


                    if (singleLogoutServices != null && CollectionUtils.isNotEmpty(singleLogoutServices)) {
                        boolean foundSingleLogoutServicePostBinding = false;
                        for (SingleLogoutService singleLogoutService : singleLogoutServices) {

                            if (singleLogoutService != null) {
                                if (singleLogoutService.getBinding() != null && singleLogoutService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI) && singleLogoutService.getLocation() != null) {
                                    property.setValue(singleLogoutService.getLocation());
                                    foundSingleLogoutServicePostBinding = true;
                                    break;
                                }
                            }
                        }
                        if (!foundSingleLogoutServicePostBinding) {
                            for (SingleLogoutService singleLogoutService : singleLogoutServices) {

                                if (singleLogoutService != null) {
                                    if (singleLogoutService.getBinding() != null && singleLogoutService.getLocation() != null) {
                                        property.setValue(singleLogoutService.getLocation());
                                        foundSingleLogoutServicePostBinding = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!foundSingleLogoutServicePostBinding) {
                            property.setValue("");
                        }


                    } else {
                        property.setValue("");
                    }
                    properties[5] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
                    property.setValue("");//not found in the metadata spec
                    //TODO while running the code Debug and check for availability

                    properties[6] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[7] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[8] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[9] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[10] = property;


                    List<KeyDescriptor> descriptors = idpssoDescriptor.getKeyDescriptors();
                    if (descriptors != null && descriptors.size() > 0) {
                        for (int i = 0; i < descriptors.size(); i++) {
                            KeyDescriptor descriptor = descriptors.get(i);
                            if (descriptor != null) {
                                String use = "";
                                try {
                                    use = descriptor.getUse().name().toString();
                                } catch (Exception ex) {
                                    log.error("Error !!!!", ex);
                                }


                                if (use != null && use.equals("SIGNING")) {

//                                    try {
                                    properties[10].setValue("true");
                                    //samlssoServiceProviderDO.setX509Certificate(org.opensaml.xml.security.keyinfo.KeyInfoHelper.getCertificates(descriptor.getKeyInfo()).get(0));
                                    //samlssoServiceProviderDO.setCertAlias(entityDescriptor.getEntityID());
//                                    } catch (java.security.cert.CertificateException ex) {
//                                        log.error("Error While setting Certificate and alias", ex);
//                                    } catch (java.lang.Exception ex) {
//                                        log.error("Error While setting Certificate and alias", ex);
//                                    }
                                } else if (use != null && use.equals("ENCRYPTION")) {

//                                    try {
                                    properties[9].setValue("true");
                                    //samlssoServiceProviderDO.setX509Certificate(org.opensaml.xml.security.keyinfo.KeyInfoHelper.getCertificates(descriptor.getKeyInfo()).get(0));
                                    //samlssoServiceProviderDO.setCertAlias(entityDescriptor.getEntityID());
//                                    } catch (java.security.cert.CertificateException ex) {
//                                        log.error("Error While setting Certificate and alias", ex);
//                                    } catch (java.lang.Exception ex) {
//                                        log.error("Error While setting Certificate and alias", ex);
//                                    }
                                }

                            }
                        }
                    }


                    property = new Property();
                    property.setName("commonAuthQueryParams");//SAML querry param in the gui
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[11] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.REQUEST_METHOD);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[12] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[13] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.DIGEST_ALGORITHM);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[14] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_COMPARISON_LEVEL);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[15] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_NAME_ID_POLICY);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[16] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.FORCE_AUTHENTICATION);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[17] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM_POST);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[18] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_CLASS);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[19] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.ATTRIBUTE_CONSUMING_SERVICE_INDEX);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[20] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_CERT);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[21] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_AUTHN_CONTEXT);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[22] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_PROTOCOL_BINDING);
                    property.setValue("");//TODO while running the code Debug and check for availability
                    properties[23] = property;

                    federatedAuthenticatorConfig.setProperties(properties);

                    //set certificates

                    List<KeyDescriptor> descriptorsCert = idpssoDescriptor.getKeyDescriptors();
                    if (descriptors != null && descriptors.size() > 0) {
                        for (int i = 0; i < descriptors.size(); i++) {
                            KeyDescriptor descriptor = descriptors.get(i);
                            if (descriptor != null) {
                                if (descriptor.getUse() != null && descriptor.getUse().toString().equals("SIGNING")) {

                                    try {
                                        String cert = null;
                                        if (descriptor.getKeyInfo() != null) {
                                            if (descriptor.getKeyInfo().getX509Datas() != null && descriptor.getKeyInfo().getX509Datas().size() > 0) {
                                                for (int k = 0; k < descriptor.getKeyInfo().getX509Datas().size(); k++) {
                                                    if (descriptor.getKeyInfo().getX509Datas().get(k) != null) {
                                                        if (descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates() != null && descriptor.getKeyInfo().getX509Datas().get(0).getX509Certificates().size() > 0) {
                                                            for (int y = 0; y < descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().size(); y++) {
                                                                if (descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y) != null) {
                                                                    if (descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y).getValue() != null && descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y).getValue().length() > 0) {
                                                                        cert = descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y).getValue().toString();
                                                                        builder.append(org.apache.axiom.om.util.Base64.encode(cert.getBytes()));
                                                                        return federatedAuthenticatorConfig;
                                                                    }
                                                                }
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
//                                        String cert = descriptor.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue().toString();
//                                        builder.append(org.apache.axiom.om.util.Base64.encode(cert.getBytes()));
//                                        builder.append(org.opensaml.xml.security.keyinfo.KeyInfoHelper.getCertificates(descriptor.getKeyInfo()).get(0)).toString();
//                                        samlssoServiceProviderDO.setCertAlias(entityDescriptor.getEntityID());
                                    } catch (java.lang.Exception ex) {
                                        log.error("Error While setting Certificate", ex);
                                        break;
                                    }
                                }
                            }
                        }


                    }
                }
            } else {
                throw new IdentityApplicationManagementException("No Role Descriptors found, invalid file content");
            }


        }
        return federatedAuthenticatorConfig;
    }


    public static FederatedAuthenticatorConfig build(OMElement saml2FederatedAuthenticatorConfigOM, StringBuilder builder) throws IdentityApplicationManagementException {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        EntityDescriptor entityDescriptor = null;
        entityDescriptor = generateMetadataObjectFromString(saml2FederatedAuthenticatorConfigOM.toString());
        if (entityDescriptor != null) {
            federatedAuthenticatorConfig = parse(entityDescriptor, federatedAuthenticatorConfig, builder);
        } else {
            throw new IdentityApplicationManagementException("Error while trying to convert to metadata, Invalid file content");
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
