/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.sample.outbound.authenticator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.exception.SAML2SSOAuthenticatorException;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.request.SAML2ACSRequest;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.response.SAML2SSOPostRequestResponse;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.response.SAML2SSORedirectRequestResponse;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.util.SAML2SSOConstants;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.util.Utils;
import org.wso2.carbon.identity.authenticator.outbound.saml2sso.util.X509CredentialImpl;
import org.wso2.carbon.identity.gateway.api.response.IdentityResponse;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.IDPCertificate;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.model.FederatedUser;
import org.wso2.carbon.identity.gateway.processor.authenticator.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.service.GatewayClaimResolverService;
import org.wso2.carbon.identity.mgt.claim.Claim;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * SAML2 SSO Outbound Authenticator.
 */
public class SampleFederatedAuthenticator extends AbstractApplicationAuthenticator implements
        FederatedApplicationAuthenticator {

    private static Logger log = LoggerFactory.getLogger(SampleFederatedAuthenticator.class);

    @Override
    public String getName() {
        return "SampleFederatedAuthenticator";
    }

    @Override
    public String getFriendlyName() {
        return "SampleFederatedAuthenticator";
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public List<Properties> getConfigurationProperties() {
        return null;
    }

    @Override
    public boolean canHandle(AuthenticationContext authenticationContext) {
        return true;
    }

    @Override
    public String getContextIdentifier(AuthenticationContext authenticationContext) {
        return null;
    }

    @Override
    protected boolean isInitialRequest(AuthenticationContext authenticationContext) {

        if (authenticationContext.getIdentityRequest() instanceof SAML2ACSRequest) {
            return false;
        }
        return true;
    }


    @Override
    protected AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException {

        boolean isPost = isPost(getIdentityProviderConfig(context), context);
        boolean isForce = isForce(getIdentityProviderConfig(context), context);
        boolean isPassive = isPassive(getIdentityProviderConfig(context), context);

        AuthenticationResponse authenticationResponse = AuthenticationResponse.INCOMPLETE;
        IdentityResponse.IdentityResponseBuilder builder;
        if (isPost) {
            try {
                builder = buildSAML2SSOPostRequest(isForce, isPassive, context);
            } catch (SAML2SSOAuthenticatorException e) {
                throw new AuthenticationHandlerException("Error occurred while building SAML2SSOPostRequest", e);
            }
        } else {
            try {
                builder = buildSAML2SSORedirectRequest(isForce, isPassive, context);
            } catch (SAML2SSOAuthenticatorException e) {
                throw new AuthenticationHandlerException("Error occurred while building SAML2SSORedirectRequest", e);
            }
        }
        authenticationResponse.setIdentityResponseBuilder(builder);
        return authenticationResponse;
    }

    protected SAML2SSOPostRequestResponse.SAML2SSOPostRequestResponseBuilder buildSAML2SSOPostRequest(
            boolean isForce, boolean isPassive, AuthenticationContext context)
            throws SAML2SSOAuthenticatorException, AuthenticationHandlerException {

        SAML2SSOPostRequestResponse.SAML2SSOPostRequestResponseBuilder builder = new SAML2SSOPostRequestResponse
                .SAML2SSOPostRequestResponseBuilder();

        String saml2SSOUrl = getSAML2SSOUrl(getIdentityProviderConfig(context));
        builder.setSaml2SSOUrl(saml2SSOUrl);
        builder.setSamlRequest(buildAuthnRequest(saml2SSOUrl, isForce, isPassive, context));
        builder.setRelayState(context.getInitialAuthenticationRequest().getRequestKey());
        builder.setAuthnRequestSigned(isAuthnRequestSigned(getIdentityProviderConfig(context)));
        builder.setIdPCredential(Utils.getServerCredentials());
        builder.setSigAlg(getSignatureAlgorithm(getIdentityProviderConfig(context)));
        builder.setDigestAlg(getDigestAlgorithm(getIdentityProviderConfig(context)));
        return builder;
    }

    protected SAML2SSORedirectRequestResponse.SAML2SSORedirectRequestResponseBuilder buildSAML2SSORedirectRequest(
            boolean isForce, boolean isPassive, AuthenticationContext context)
            throws SAML2SSOAuthenticatorException, AuthenticationHandlerException {

        SAML2SSORedirectRequestResponse.SAML2SSORedirectRequestResponseBuilder builder = new
                SAML2SSORedirectRequestResponse.SAML2SSORedirectRequestResponseBuilder();

        String saml2SSOUrl = getSAML2SSOUrl(getIdentityProviderConfig(context));
        builder.setSaml2SSOUrl(saml2SSOUrl);
        builder.setSamlRequest(buildAuthnRequest(saml2SSOUrl, isForce, isPassive, context));
        builder.setRelayState(context.getInitialAuthenticationRequest().getRequestKey());
        builder.setAuthnRequestSigned(isAuthnRequestSigned(getIdentityProviderConfig(context)));
        builder.setIdPCredential(Utils.getServerCredentials());
        builder.setSigAlg(getSignatureAlgorithm(getIdentityProviderConfig(context)));
        return builder;
    }

    protected AuthnRequest buildAuthnRequest(String idpUrl, boolean isForce, boolean isPassive, AuthenticationContext
            context) throws SAML2SSOAuthenticatorException {

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");

        // Get SP_ENTITY_ID from Harsha's model
        String spEntityId = "carbonServer";
        issuer.setValue(spEntityId);

        DateTime issueInstant = new DateTime();

        AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol",
                "AuthnRequest", "samlp");
        authRequest.setForceAuthn(isForce);
        authRequest.setIsPassive(isPassive);
        authRequest.setIssueInstant(issueInstant);

        // how about redirect binding URI?
        authRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);

        String acsUrl = null;
//        try {
//            acsUrl = SAML2SSOAuthenticatorDataHolder.getInstance().getIdentityUtilService().getURLUtils()
//                    .getServerURL("", true, true);
            acsUrl = "https://localhost:9292/gateway";
//        } catch (IdentityException e) {
//            // fix IdentityUtilService.getURLUtils() to throw runtime exception or not throw exceptions are all
//            throw new SAML2SSOAuthenticatorRuntimeException("Error while getting URLUtils.", e);
//        }

        authRequest.setAssertionConsumerServiceURL(acsUrl);
        authRequest.setIssuer(issuer);
        authRequest.setID(Utils.createID());
        authRequest.setVersion(SAMLVersion.VERSION_20);
        authRequest.setDestination(idpUrl);

        // Get AttributeConsumingServiceIndex from Harsha's model
        String attributeConsumingServiceIndex = null;
        if (StringUtils.isNotBlank(attributeConsumingServiceIndex)) {
            try {
                authRequest.setAttributeConsumingServiceIndex(Integer.valueOf(attributeConsumingServiceIndex));
            } catch (NumberFormatException e) {
                log.error("Error while setting AttributeConsumingServiceIndex to SAMLRequest.", e);
            }
        }

        // Get IncludeNameIDPolicy from Harsha's model
        String includeNameIDPolicyProp = null;
        if (StringUtils.isNotBlank(includeNameIDPolicyProp) || Boolean.parseBoolean(includeNameIDPolicyProp)) {
            NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
            NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
            nameIdPolicy.setFormat(NameIDType.UNSPECIFIED);
            //nameIdPolicy.setSPNameQualifier("Issuer");
            nameIdPolicy.setAllowCreate(true);
            authRequest.setNameIDPolicy(nameIdPolicy);
        }

        RequestedAuthnContext requestedAuthnContext = buildRequestedAuthnContext(context);
        if (requestedAuthnContext != null) {
            authRequest.setRequestedAuthnContext(requestedAuthnContext);
        }

        // handle SAML2 extensions

        return authRequest;
    }

    protected RequestedAuthnContext buildRequestedAuthnContext(AuthenticationContext context) {

        RequestedAuthnContextBuilder requestedAuthnContextBuilder = null;
        RequestedAuthnContext requestedAuthnContext = null;

        // Get IncludeAuthnContext from Harsha's model
        String includeAuthnContext = null;

        if (StringUtils.isNotBlank(includeAuthnContext) && SAML2SSOConstants.AS_REQUEST.equalsIgnoreCase
                (includeAuthnContext)) {
            AuthnRequest inboundAuthnRequest = (AuthnRequest) context.getParameter(SAML2SSOConstants
                    .INBOUND_AUTHN_REQUEST);
            if (inboundAuthnRequest != null) {
                RequestedAuthnContext incomingRequestedAuthnContext = inboundAuthnRequest.getRequestedAuthnContext();
                if (incomingRequestedAuthnContext != null) {
                    requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
                    requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
                    requestedAuthnContext.setDOM(incomingRequestedAuthnContext.getDOM());
                }
            }
        } else if (StringUtils.isEmpty(includeAuthnContext) || Boolean.parseBoolean(includeAuthnContext)) {

            requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
            requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
            AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
            AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder
                    .buildObject(SAMLConstants.SAML20_NS,
                            AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME,
                            SAMLConstants.SAML20_PREFIX);

            // Get AuthnContextClass from Harsha's model
            String authnContext = null;
            if (StringUtils.isNotBlank(authnContext)) {
                authnContextClassRef.setAuthnContextClassRef(authnContext);
            } else {
                authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
            }

            // Get AuthnContextComparisonLevel from Harsha's model
            String authnContextComparison = null;
            if (StringUtils.isNotEmpty(authnContextComparison)) {
                if (AuthnContextComparisonTypeEnumeration.EXACT.toString().equalsIgnoreCase(
                        authnContextComparison)) {
                    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
                } else if (AuthnContextComparisonTypeEnumeration.MINIMUM.toString().equalsIgnoreCase(
                        authnContextComparison)) {
                    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
                } else if (AuthnContextComparisonTypeEnumeration.MAXIMUM.toString().equalsIgnoreCase(
                        authnContextComparison)) {
                    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM);
                } else if (AuthnContextComparisonTypeEnumeration.BETTER.toString().equalsIgnoreCase(
                        authnContextComparison)) {
                    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.BETTER);
                }
            } else {
                requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
            }
            requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        }
        return requestedAuthnContext;
    }

    @Override
    protected AuthenticationResponse processResponse(AuthenticationContext context)
            throws AuthenticationHandlerException {

        SAML2ACSRequest saml2ACSRequest = (SAML2ACSRequest) context.getIdentityRequest();
        String samlResponse = saml2ACSRequest.getSamlResponse();
        String decodedResponse = new String(Base64.decode(samlResponse));
        XMLObject xmlObject = null;
        try {
            xmlObject = Utils.unmarshall(decodedResponse);
        } catch (SAML2SSOAuthenticatorException e) {
            log.error("Error while unmarshalling SAMLResponse message");
            return AuthenticationResponse.AUTHENTICATED;

        }
        Response response = (Response) xmlObject;
        try {
            Assertion assertion = decryptAssertion(response, context);
            if (assertion != null) {

                validateAudienceRestriction(assertion, context);

                validateSignature(assertion, response, getIdentityProviderConfig(context));

                processSubject(assertion, context);

                processAttributeStatements(assertion, getIdentityProviderConfig(context), context);

            }
        } catch (SAML2SSOAuthenticatorException e) {
            log.error("Error while processing the SAMLResponse.", e);
            return AuthenticationResponse.AUTHENTICATED;
        }

        return AuthenticationResponse.AUTHENTICATED;
    }

    protected Assertion decryptAssertion(Response response, AuthenticationContext context) throws
            SAML2SSOAuthenticatorException,
            AuthenticationHandlerException {

        Assertion assertion = null;
        if (isAssertionEncryptionEnabled(getIdentityProviderConfig(context))) {
            List<EncryptedAssertion> encryptedAssertions = response.getEncryptedAssertions();
            EncryptedAssertion encryptedAssertion = null;
            if (CollectionUtils.isNotEmpty(encryptedAssertions)) {
                encryptedAssertion = encryptedAssertions.get(0);
                try {
                    // get IDP cert
                    X509Certificate idPCredential = null;
                    X509Credential credential = new X509CredentialImpl(idPCredential);
                    KeyInfoCredentialResolver keyResolver = new StaticKeyInfoCredentialResolver(credential);
                    EncryptedKey key = encryptedAssertion.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0);
                    Decrypter decrypter = new Decrypter(null, keyResolver, null);
                    SecretKey dkey = (SecretKey) decrypter.decryptKey(key, encryptedAssertion.getEncryptedData().
                            getEncryptionMethod().getAlgorithm());
                    Credential shared = SecurityHelper.getSimpleCredential(dkey);
                    decrypter = new Decrypter(new StaticKeyInfoCredentialResolver(shared), null, null);
                    decrypter.setRootInNewDocument(true);
                    assertion = decrypter.decrypt(encryptedAssertion);
                } catch (DecryptionException e) {
                    throw new SAML2SSOAuthenticatorException("Error while decrypting SAML Assertion.", e);
                }
            }
        } else {
            List<Assertion> assertions = response.getAssertions();
            if (CollectionUtils.isNotEmpty(assertions)) {
                assertion = assertions.get(0);
            }
        }
        if (assertion == null) {
            if (response.getStatus() != null &&
                    response.getStatus().getStatusCode() != null &&
                    response.getStatus().getStatusCode().getValue().equals(StatusCode.RESPONDER_URI) &&
                    response.getStatus().getStatusCode().getStatusCode() != null &&
                    response.getStatus().getStatusCode().getStatusCode().getValue().equals(StatusCode.NO_PASSIVE_URI)) {
                return null;
            }
            throw new SAML2SSOAuthenticatorException("Cannot find Assertion in SAMLResponse");
        }
        return assertion;
    }

    protected void validateAudienceRestriction(Assertion assertion, AuthenticationContext context)
            throws SAML2SSOAuthenticatorException {

        if (assertion != null) {
            Conditions conditions = assertion.getConditions();
            if (conditions != null) {
                List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
                if (audienceRestrictions != null && !audienceRestrictions.isEmpty()) {
                    for (AudienceRestriction audienceRestriction : audienceRestrictions) {
                        if (CollectionUtils.isNotEmpty(audienceRestriction.getAudiences())) {
                            boolean audienceFound = false;
                            for (Audience audience : audienceRestriction.getAudiences()) {
                                // Get spEntityId from config
                                String spEntityId = "carbonServer";
                                if (spEntityId.equals(audience.getAudienceURI())) {
                                    audienceFound = true;
                                    break;
                                }
                            }
                            if (!audienceFound) {
                                throw new SAML2SSOAuthenticatorException("Assertion Audience Restriction validation failed");
                            }
                        } else {
                            throw new SAML2SSOAuthenticatorException("SAML Response's AudienceRestriction doesn't contain Audiences");
                        }
                    }
                } else {
                    throw new SAML2SSOAuthenticatorException("SAML Response doesn't contain AudienceRestrictions");
                }
            } else {
                throw new SAML2SSOAuthenticatorException("SAML Response doesn't contain Conditions");
            }
        }
    }

    protected void validateSignature(Assertion assertion, Response response,
                                     IdentityProviderConfig identityProviderConfig) throws
            SAML2SSOAuthenticatorException,
            AuthenticationHandlerException {

        if (assertion.getSignature() == null) {
            throw new SAML2SSOAuthenticatorException("Signature element not found in Assertion.");
        } else {
            try {
                Credential credential = getIdPCredential(identityProviderConfig);
                if (credential == null) {
                    throw new SAML2SSOAuthenticatorException("Identity Provider doesn't have a certificate.");
                }
                SignatureValidator validator = new SignatureValidator(credential);
                validator.validate(assertion.getSignature());
                if (isAuthnResponseSigned(identityProviderConfig)) {
                    if (response.getSignature() == null) {
                        throw new SAML2SSOAuthenticatorException("SAMLResponse signing is enabled, but signature element " +
                                "not found in Response element.");
                    } else {
                        try {
                            validator = new SignatureValidator(credential);
                            validator.validate(response.getSignature());
                        } catch (ValidationException e) {
                            throw new SAML2SSOAuthenticatorException("Signature validation failed for Response", e);
                        }
                    }
                }
            } catch (ValidationException e) {
                throw new SAML2SSOAuthenticatorException("Signature validation failed for Assertion.", e);
            }
        }

    }

    protected void processSubject(Assertion assertion, AuthenticationContext context) throws
            SAML2SSOAuthenticatorException {

        String subject = null;
        if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
            subject = assertion.getSubject().getNameID().getValue();
        }

        if (StringUtils.isBlank(subject)) {
            throw new SAML2SSOAuthenticatorException("Assertion does not contain the name of the subject");
        }

        FederatedUser federatedUser = new FederatedUser(subject);
        context.addParameter("Subject", federatedUser);
    }

    protected void processAttributeStatements(Assertion assertion, IdentityProviderConfig identityProviderConfig,
                                              AuthenticationContext context) throws SAML2SSOAuthenticatorException,
            AuthenticationHandlerException {

        Set<Claim> claims = new HashSet();
        if (assertion != null) {
            List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();
            if (attributeStatementList != null) {
                for (AttributeStatement statement : attributeStatementList) {
                    List<Attribute> attributesList = statement.getAttributes();
                    for (Attribute attribute : attributesList) {
                        List<XMLObject> values = attribute.getAttributeValues();
                        if (values != null) {
                            for (int i = 0; i < values.size(); i++) {
                                Element value = attribute.getAttributeValues().get(i).getDOM();
                                String attributeValue = value.getTextContent();
                                if (StringUtils.isNotBlank(attributeValue)) {
                                    claims.add(new Claim(getClaimDialectURI(identityProviderConfig).get(), attribute
                                            .getName(),attributeValue));
                                }
                            }
                        }

                    }
                }
            }
        }

        Optional<String> attributeProfile = getAttributeProfile(identityProviderConfig);
        String claimDialectURI = getClaimDialectURI(identityProviderConfig).get();
        GatewayClaimResolverService gatewayClaimResolverService = GatewayClaimResolverService.getInstance();
        Set<Claim> mappedRootClaims = gatewayClaimResolverService.transformToNativeDialect(claims, claimDialectURI,
                                                                                           attributeProfile);

        if (StringUtils.isNotBlank(getUsernameClaimURI(getIdentityProviderConfig(context)))) {
            boolean isUsernameExists = false;
            Iterator<Claim> it = mappedRootClaims.iterator();
            while (it.hasNext()) {
                Claim claim = it.next();
                if (claim.getClaimUri().equals(getUsernameClaimURI(getIdentityProviderConfig(context)))) {
                    isUsernameExists = true;
                    mappedRootClaims.remove(claim);
                    String value = claim.getValue();
                    FederatedUser tmpFederatedUser = new FederatedUser(value, mappedRootClaims);
                    context.getSequenceContext().getCurrentStepContext().setUser(tmpFederatedUser);
                }
            }
            if (!isUsernameExists) {
                throw new SAML2SSOAuthenticatorException("Cannot find username claim.");
            }
        }else{
            FederatedUser federatedUser = (FederatedUser) context.getParameter("Subject");
            federatedUser.setUserClaims(mappedRootClaims);
            context.getSequenceContext().getCurrentStepContext().setUser(federatedUser);
        }
    }

    public IdentityProviderConfig getIdentityProviderConfig(AuthenticationContext context)
            throws AuthenticationHandlerException {

        return context.getSequence().getIdentityProvider(context.getSequenceContext().getCurrentStep(),
                context.getSequenceContext().getCurrentStepContext()
                        .getIdentityProviderName())
                .getIdentityProviderConfig();
    }

    public boolean isPost(IdentityProviderConfig identityProviderConfig, AuthenticationContext context) {


        String requestBinding = (String) getAuthenticatorConfigProperties(identityProviderConfig).get(SAML2SSOConstants
                .REQUEST_BINDING);
        boolean isPost = true;
        if (SAML2SSOConstants.GET.equals(requestBinding)) {
            isPost = false;
        } else if (SAML2SSOConstants.AS_REQUEST.equals(requestBinding)) {
            String method = context.getInitialAuthenticationRequest().getHttpMethod();
            if (SAML2SSOConstants.GET.equalsIgnoreCase(method)) {
                isPost = false;
            }
        }
        return isPost;
    }

    public boolean isForce(IdentityProviderConfig identityProviderConfig, AuthenticationContext context) {

        String force = (String) getAuthenticatorConfigProperties(identityProviderConfig).get(SAML2SSOConstants
                .FORCE);
        boolean isForce = true;
        if (!Boolean.parseBoolean(force)) {
            isForce = false;
        } else if (SAML2SSOConstants.AS_REQUEST.equals(force)) {
            // get the client authentication request and cast to saml2sso request and get isForce attribute
            // context.getInitialAuthenticationRequest();
            boolean isClientAuthRequestForce = false;
            if (isClientAuthRequestForce) {
                isForce = true;
            }
        }
        return isForce;
    }

    public boolean isPassive(IdentityProviderConfig identityProviderConfig, AuthenticationContext context) {

        String passive = (String) getAuthenticatorConfigProperties(identityProviderConfig).get(SAML2SSOConstants
                .PASSIVE);
        boolean isPassive = true;
        if (!Boolean.parseBoolean(passive)) {
            isPassive = false;
        } else if (SAML2SSOConstants.AS_REQUEST.equals(passive)) {
            // get the client authentication request and cast to saml2sso request and get isPassive attribute
            // context.getInitialAuthenticationRequest();
            boolean isClientAuthRequestPassive = false;
            if (isClientAuthRequestPassive) {
                isPassive = true;
            }
        }
        return isPassive;
    }

    public String getSAML2SSOUrl(IdentityProviderConfig identityProviderConfig) {

        String saml2SSOUrl = (String) getAuthenticatorConfigProperties(identityProviderConfig).get
                (SAML2SSOConstants.SAML2_SSO_URL);
        return saml2SSOUrl;
    }

    public boolean isAuthnRequestSigned(IdentityProviderConfig identityProviderConfig) {

        String authnReqSigned = (String) getAuthenticatorConfigProperties(identityProviderConfig).get
                (SAML2SSOConstants.AUTHN_REQUEST_SIGNED);
        boolean isAuthnReqSigned = true;
        if (!Boolean.parseBoolean(authnReqSigned)) {
            isAuthnReqSigned = false;
        }
        return isAuthnReqSigned;
    }

    public boolean isAuthnResponseSigned(IdentityProviderConfig identityProviderConfig) {

        String authnRespSigned = (String) getAuthenticatorConfigProperties(identityProviderConfig).get
                (SAML2SSOConstants.AUTHN_RESPONSE_SIGNED);
        boolean isAuthnRespSigned = true;
        if (!Boolean.parseBoolean(authnRespSigned)) {
            isAuthnRespSigned = false;
        }
        return isAuthnRespSigned;
    }

    public boolean isAssertionEncryptionEnabled(IdentityProviderConfig identityProviderConfig) {

        String authnRespEncrypted = (String) getAuthenticatorConfigProperties(identityProviderConfig).get
                (SAML2SSOConstants.AUTHN_RESPONSE_ENCRYPTED);
        boolean isAuthnRespEncrypted = true;
        if (!Boolean.parseBoolean(authnRespEncrypted)) {
            isAuthnRespEncrypted = false;
        }
        return isAuthnRespEncrypted;
    }

    public X509Credential getIdPCredential(IdentityProviderConfig identityProviderConfig) {

        X509Credential credential = null;
        List<IDPCertificate> certs = identityProviderConfig.getIdpMetaData().getCertificates();
        for (IDPCertificate cert : certs) {
            String key = cert.getKey();
            String value = cert.getValue();
            if ("primary".equals(key)) {
                Certificate certificate = null;
                try {
                    certificate = decodeCertificate(value);
                    credential = new X509CredentialImpl((X509Certificate) certificate);
                    return credential;
                } catch (CertificateException e) {
                    log.error("Error while decoding certificate: " + value + ".", e);
                }
            }
        }
        return credential;
    }

    /**
     * Decode X509 certificate.
     *
     * @param encodedCert Base64 encoded certificate
     * @return Decoded <code>Certificate</code>
     * @throws CertificateException Error when decoding certificate
     */
    public Certificate decodeCertificate(String encodedCert) throws CertificateException {

        if (encodedCert != null) {
            byte[] bytes = Base64.decode(encodedCert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(bytes));
            return cert;
        } else {
            String errorMsg = "Invalid encoded certificate: \'NULL\'";
            log.debug(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public String getSignatureAlgorithm(IdentityProviderConfig identityProviderConfig) {

        String sigAlg = (String) getAuthenticatorConfigProperties(identityProviderConfig).get
                (SAML2SSOConstants.SIGNATURE_ALGO);
        return sigAlg;
    }

    public String getDigestAlgorithm(IdentityProviderConfig identityProviderConfig) {

        String digAlg = (String) getAuthenticatorConfigProperties(identityProviderConfig).get
                (SAML2SSOConstants.DIGEST_ALGO);
        return digAlg;
    }

    public String getUsernameClaimURI(IdentityProviderConfig identityProviderConfig) {

        return null;
    }

    public Properties getAuthenticatorConfigProperties(IdentityProviderConfig identityProviderConfig) {

        Properties properties = new Properties();
        List<AuthenticatorConfig> configs = identityProviderConfig.getAuthenticationConfig()
                .getAuthenticatorConfigs();
        for (AuthenticatorConfig config : configs) {
            if (getName().equals(config.getName())) {
                properties = config.getProperties();
            }
        }
        return properties;
    }

    protected Optional<String> getClaimDialectURI(IdentityProviderConfig identityProviderConfig) {
        return Optional.of(identityProviderConfig.getIdpMetaData().getClaimConfig().getDialectUri());
    }

    protected Optional<String> getAttributeProfile(IdentityProviderConfig identityProviderConfig) {
        String profile = identityProviderConfig.getIdpMetaData().getClaimConfig().getProfile();
        if(profile != null){
            return Optional.of(profile);
        }
        return Optional.empty();
    }

}
