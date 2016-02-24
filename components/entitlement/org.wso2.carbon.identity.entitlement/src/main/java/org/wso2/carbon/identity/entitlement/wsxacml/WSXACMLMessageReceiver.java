/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.wsxacml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.util.CarbonEntityResolver;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WSXACMLMessageReceiver extends RPCMessageReceiver {

    private static final String SECURITY_MANAGER_PROPERTY = Constants.XERCES_PROPERTY_PREFIX +
            Constants.SECURITY_MANAGER_PROPERTY;
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private static Log log = LogFactory.getLog(WSXACMLMessageReceiver.class);
    private static boolean isBootStrapped = false;
    private static OMNamespace xacmlContextNS = OMAbstractFactory.getOMFactory()
            .createOMNamespace("urn:oasis:names:tc:xacml:2.0:context:schema:os", "xacml-context");

    /**
     * Bootstrap the OpenSAML2 library only if it is not bootstrapped.
     */
    public static void doBootstrap() {

        if (!isBootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                isBootStrapped = true;
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    /**
     * Create the issuer object to be added
     *
     * @return : the issuer of the statements
     */
    private static Issuer createIssuer() {

        IssuerBuilder issuer = (IssuerBuilder) org.opensaml.xml.Configuration.getBuilderFactory().
                getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuerObject = issuer.buildObject();
        issuerObject.setValue("https://identity.carbon.wso2.org");
        issuerObject.setSPProvidedID("SPPProvierId");
        return issuerObject;
    }

    /**
     * Overloaded method to sign a SAML response
     *
     * @param response           : SAML response to be signed
     * @param signatureAlgorithm : algorithm to be used in signing
     * @param cred               : signing credentials
     * @return signed SAML response
     * @throws EntitlementException
     */
    private static Response setSignature(Response response, String signatureAlgorithm,
                                         X509Credential cred) throws EntitlementException {
        doBootstrap();
        try {
            Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(cred);
            signature.setSignatureAlgorithm(signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            try {
                KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
                X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
                X509Certificate cert = (X509Certificate) buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME);
                String value = org.apache.xml.security.utils.Base64.encode(cred.getEntityCertificate().getEncoded());
                cert.setValue(value);
                data.getX509Certificates().add(cert);
                keyInfo.getX509Datas().add(data);
                signature.setKeyInfo(keyInfo);
            } catch (CertificateEncodingException e) {
                throw new EntitlementException("errorGettingCert");
            }
            response.setSignature(signature);
            List<Signature> signatureList = new ArrayList<Signature>();
            signatureList.add(signature);
            //Marshall and Sign
            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(response);
            marshaller.marshall(response);
            org.apache.xml.security.Init.init();
            Signer.signObjects(signatureList);
            return response;
        } catch (Exception e) {
            throw new EntitlementException("Error When signing the assertion.", e);
        }
    }

    /**
     * Create XMLObject from a given QName
     *
     * @param objectQName: QName of the object to be built into a XMLObject
     * @return built xmlObject
     * @throws EntitlementException
     */
    private static XMLObject buildXMLObject(QName objectQName) throws EntitlementException {

        XMLObjectBuilder builder = org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null) {
            throw new EntitlementException("Unable to retrieve builder for object QName "
                    + objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(),
                objectQName.getPrefix());
    }

    /**
     * Create basic credentials needed to generate signature using EntitlementServiceComponent
     *
     * @return basicX509Credential
     */
    private static BasicX509Credential createBasicCredentials() {

        Certificate certificate = null;
        PrivateKey issuerPK = null;

        KeyStoreManager keyMan = KeyStoreManager.getInstance(-1234);

        try {
            certificate = keyMan.getDefaultPrimaryCertificate();
            issuerPK = keyMan.getDefaultPrivateKey();
        } catch (Exception e) {
            log.error("Error occurred while getting the KeyStore from KeyManger.", e);
        }

        BasicX509Credential basicCredential = new BasicX509Credential();
        basicCredential.setEntityCertificate((java.security.cert.X509Certificate) certificate);
        basicCredential.setPrivateKey(issuerPK);

        return basicCredential;
    }

    /**
     * Set relevant xacml namespace to all the children in the given iterator.     *
     *
     * @param iterator: Iterator for all children inside OMElement
     */
    private static void setXACMLNamespace(Iterator iterator) {

        while (iterator.hasNext()) {
            OMElement omElement2 = (OMElement) iterator.next();
            omElement2.setNamespace(xacmlContextNS);
            if (omElement2.getChildElements().hasNext()) {
                setXACMLNamespace(omElement2.getChildElements());
            }
        }
    }

    @Override
    public void invokeBusinessLogic(MessageContext inMessageContext, MessageContext outMessageContext)
            throws AxisFault {

        try {
            OMElement xacmlAuthzDecisionQueryElement = inMessageContext.getEnvelope().getBody().getFirstElement();
            String xacmlAuthzDecisionQuery = xacmlAuthzDecisionQueryElement.toString();
            String xacmlRequest = extractXACMLRequest(xacmlAuthzDecisionQuery);
            String serviceClass;
            try {
                serviceClass = inMessageContext.getAxisService().getParameterValue("XACMLHandlerImplClass").
                        toString().trim();
            } catch (NullPointerException e) {
                log.error("WS-XACML ServiceClass not specified in service context");
                throw new AxisFault("WS-XACML ServiceClass not specified in service context");
            }
            if (serviceClass == null || serviceClass.length() == 0) {
                log.error("WS-XACML ServiceClass not specified in service context");
                throw new AxisFault("WS-XACML ServiceClass not specified in service context");
            }
            XACMLHandler xacmlHandler = (XACMLHandler) Class.forName(serviceClass).newInstance();
            xacmlRequest = xacmlRequest.replaceAll("xacml-context:", "");
            String xacmlResponse = xacmlHandler.XACMLAuthzDecisionQuery(xacmlRequest);
            String samlResponse = secureXACMLResponse(xacmlResponse);
            OMElement samlResponseElement = AXIOMUtil.stringToOM(samlResponse);
            SOAPEnvelope outSOAPEnvelope = createDefaultSOAPEnvelope(inMessageContext);
            if (outSOAPEnvelope != null) {
                outSOAPEnvelope.getBody().addChild(samlResponseElement);
                outMessageContext.setEnvelope(outSOAPEnvelope);
            } else {
                throw new Exception("SOAP envelope can not be null");
            }
        } catch (Exception e) {
            log.error("Error occurred while evaluating XACML request.", e);
            throw new AxisFault("Error occurred while evaluating XACML request.", e);
        }
    }

    /* Creating a soap response according the the soap namespce uri */
    private SOAPEnvelope createDefaultSOAPEnvelope(MessageContext inMsgCtx) {

        String soapNamespace = inMsgCtx.getEnvelope().getNamespace()
                .getNamespaceURI();
        SOAPFactory soapFactory = null;
        if (soapNamespace.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespace
                .equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Unknown SOAP Envelope");
        }
        if (soapFactory != null) {
            return soapFactory.getDefaultEnvelope();
        }

        return null;
    }

    /**
     * Extract XACML request from passed in SAML-XACMLAuthzDecisionQuery
     *
     * @param decisionQuery : XACMLAuthxDecisionQuery passed in from PEP as a String
     * @return xacml Request
     * @throws Exception
     */
    private String extractXACMLRequest(String decisionQuery) throws Exception {

        RequestType xacmlRequest = null;
        doBootstrap();
        String queryString = null;
        XACMLAuthzDecisionQueryType xacmlAuthzDecisionQuery;
        try {
            xacmlAuthzDecisionQuery = (XACMLAuthzDecisionQueryType) unmarshall(decisionQuery);
            //Access the XACML request only if Issuer and the Signature are valid.
            if (validateIssuer(xacmlAuthzDecisionQuery.getIssuer())) {
                if (validateSignature(xacmlAuthzDecisionQuery.getSignature())) {
                    xacmlRequest = xacmlAuthzDecisionQuery.getRequest();
                } else {
                    log.debug("The submitted signature is not valid!");
                }
            } else {
                log.debug("The submitted issuer is not valid!");
            }

            if (xacmlRequest != null) {
                queryString = marshall(xacmlRequest);
                queryString = queryString.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").replace("\n", "");
            }
            return queryString;
        } catch (Exception e) {
            log.error("Error unmarshalling the XACMLAuthzDecisionQuery.", e);
            throw new Exception("Error unmarshalling the XACMLAuthzDecisionQuery.", e);
        }

    }

    /**
     * Constructing the SAML or XACML Objects from a String
     *
     * @param xmlString Decoded SAML or XACML String
     * @return SAML or XACML Object
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException
     */
    public XMLObject unmarshall(String xmlString) throws EntitlementException {

        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SecurityManager securityManager = new SecurityManager();
            securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
            documentBuilderFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);

            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new CarbonEntityResolver());
            Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.trim().getBytes()));
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            log.error("Error in constructing XML(SAML or XACML) Object from the encoded String", e);
            throw new EntitlementException("Error in constructing XML(SAML or XACML) from the encoded String ", e);
        }
    }

    /**
     * Check for the validity of the issuer
     *
     * @param issuer :who makes the claims inside the Query
     * @return whether the issuer is valid
     */
    private boolean validateIssuer(Issuer issuer) {

        boolean isValidated = false;

        if (issuer.getValue().equals("https://identity.carbon.wso2.org")
                && issuer.getSPProvidedID().equals("SPPProvierId")) {
            isValidated = true;
        }
        return isValidated;
    }

    /**
     * `
     * Serialize XML objects
     *
     * @param xmlObject : XACML or SAML objects to be serialized
     * @return serialized XACML or SAML objects
     * @throws EntitlementException
     */
    private String marshall(XMLObject xmlObject) throws EntitlementException {

        try {
            doBootstrap();
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl =
                    (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(byteArrayOutputStream);
            writer.write(element, output);
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
            log.error("Error Serializing the SAML Response");
            throw new EntitlementException("Error Serializing the SAML Response", e);
        }
    }

    /**
     * Check the validity of the Signature
     *
     * @param signature : XML Signature that authenticates the assertion
     * @return whether the signature is valid
     * @throws Exception
     */
    private boolean validateSignature(Signature signature) throws Exception {

        boolean isSignatureValid = false;

        try {
            SignatureValidator validator = new SignatureValidator(getPublicX509CredentialImpl());
            validator.validate(signature);
            isSignatureValid = true;
        } catch (ValidationException e) {
            log.warn("Signature validation failed.");
        } catch (Exception e) {
            throw new Exception("Error in getting public X509Credentials to validate signature. ");
        }
        return isSignatureValid;
    }

    /**
     * get a org.wso2.carbon.identity.entitlement.wsxacml.X509CredentialImpl using RegistryService
     *
     * @return created X509Credential
     */
    private X509CredentialImpl getPublicX509CredentialImpl() throws Exception {

        X509CredentialImpl credentialImpl;
        KeyStoreManager keyStoreManager;
        try {
            keyStoreManager = KeyStoreManager.getInstance(-1234);
            // load the default pub. cert using the configuration in carbon.xml
            java.security.cert.X509Certificate cert = keyStoreManager.getDefaultPrimaryCertificate();
            credentialImpl = new X509CredentialImpl(cert);
            return credentialImpl;
        } catch (Exception e) {
            log.error("Error instantiating an org.wso2.carbon.identity.entitlement.wsxacml.X509CredentialImpl " +
                    "object for the public cert.", e);
            throw new Exception("Error instantiating an org.wso2.carbon.identity.entitlement.wsxacml.X509CredentialImpl " +
                    "object for the public cert.", e);
        }
    }

    /**
     * Encapsulates the passed in xacml response into a saml response
     *
     * @param xacmlResponse : xacml response returned from PDP
     * @return saml response
     * @throws Exception
     */
    public String secureXACMLResponse(String xacmlResponse) throws Exception {

        ResponseType responseType;
        String responseString;
        doBootstrap();

        try {
            responseType = (ResponseType) unmarshall(formatResponse(xacmlResponse));
        } catch (Exception e) {
            log.error("Error while unmarshalling the formatted XACML response.", e);
            throw new EntitlementException("Error while unmarshalling the formatted XACML response.", e);
        }
        XACMLAuthzDecisionStatementTypeImplBuilder xacmlauthz = (XACMLAuthzDecisionStatementTypeImplBuilder)
                org.opensaml.xml.Configuration.getBuilderFactory().
                        getBuilder(XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);
        XACMLAuthzDecisionStatementType xacmlAuthzDecisionStatement = xacmlauthz
                .buildObject(Statement.DEFAULT_ELEMENT_NAME, XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);
        xacmlAuthzDecisionStatement.setResponse(responseType);
        AssertionBuilder assertionBuilder = (AssertionBuilder) org.opensaml.xml.Configuration.getBuilderFactory()
                .getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        DateTime currentTime = new DateTime();
        Assertion assertion = assertionBuilder.buildObject();
        assertion.setVersion(org.opensaml.common.SAMLVersion.VERSION_20);
        assertion.setIssuer(createIssuer());
        assertion.setIssueInstant(currentTime);
        assertion.getStatements().add(xacmlAuthzDecisionStatement);
        ResponseBuilder builder = (ResponseBuilder) org.opensaml.xml.Configuration.getBuilderFactory()
                .getBuilder(Response.DEFAULT_ELEMENT_NAME);
        Response response = builder.buildObject();
        response.getAssertions().add(assertion);
        response.setIssuer(createIssuer());
        DateTime issueInstant = new DateTime();
        response.setIssueInstant(issueInstant);
        response = setSignature(response, XMLSignature.ALGO_ID_SIGNATURE_RSA, createBasicCredentials());
        try {
            responseString = marshall(response);
            responseString = responseString.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
            return responseString;
        } catch (EntitlementException e) {
            log.error("Error occurred while marshalling the SAML Response.", e);
            throw new Exception("Error occurred while marshalling the SAML Response.", e);
        }
    }

    /**
     * Format the sent in response as required by OpenSAML
     *
     * @param xacmlResponse : received XACML response
     * @return formatted response
     */
    private String formatResponse(String xacmlResponse) throws Exception {

        xacmlResponse = xacmlResponse.replace("\n", "");
        OMElement omElemnt;

        try {
            omElemnt = org.apache.axiom.om.util.AXIOMUtil.stringToOM(xacmlResponse);
            omElemnt.setNamespace(xacmlContextNS);
            if (omElemnt.getChildren() != null) {
                Iterator childIterator = omElemnt.getChildElements();
                setXACMLNamespace(childIterator);
            }
        } catch (Exception e) {
            log.error("Error while generating the OMElement from the XACML request.", e);
            throw new Exception("Error while generating the OMElement from the XACML request.", e);
        }

        return omElemnt.toString();
    }
}
