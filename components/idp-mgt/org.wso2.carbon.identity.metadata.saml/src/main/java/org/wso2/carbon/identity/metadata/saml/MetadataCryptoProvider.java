/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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
package org.wso2.carbon.identity.metadata.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.w3c.dom.Document;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.MetadataException;
import org.wso2.carbon.identity.metadata.saml.util.BuilderUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

/*
* This class adds key descriptors to Roledescriptors
* */
public class MetadataCryptoProvider implements CryptoProvider {

    private X509Credential credential;

    private static Log log = LogFactory.getLog(MetadataCryptoProvider.class);

    public MetadataCryptoProvider() throws MetadataException {
        if (log.isDebugEnabled()) {
            log.debug("Creating the credential object");
        }
        credential = new SignKeyDataHolder();
    }

    public void signMetadata(EntityDescriptor baseDescriptor) throws MetadataException {

        // Add key descriptors for each element in base descriptor.
        List<RoleDescriptor> roleDescriptors = baseDescriptor.getRoleDescriptors();
        if (roleDescriptors.size() > 0) {
            for (RoleDescriptor roleDesc : roleDescriptors) {
                roleDesc.getKeyDescriptors().add(createKeyDescriptor());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Key Descriptors set for all the role descriptor types");
        }

        // Remove namespace of Signature element
        try {
            org.apache.xml.security.utils.ElementProxy.setDefaultPrefix(ConfigElements.XMLSIGNATURE_NS, "");
        } catch (XMLSecurityException e) {
            throw new MetadataException("Unable to set default prefix for signature element", e);
        }
        org.apache.xml.security.Init.init();
    }



    /**
     * Creates the KeyInfo from the provided Credential.
     *
     * @throws MetadataException
     */
    private KeyInfo createKeyInfo() throws MetadataException {

        if (log.isDebugEnabled()) {
            log.debug("Creating the KeyInfo element");
        }
        KeyInfo keyInfo = BuilderUtil.createSAMLObject(ConfigElements.XMLSIGNATURE_NS, "KeyInfo", "");
        X509Data data = BuilderUtil.createSAMLObject(ConfigElements.XMLSIGNATURE_NS, "X509Data", "");
        X509Certificate cert = BuilderUtil.createSAMLObject(ConfigElements.XMLSIGNATURE_NS, "X509Certificate", "");

        String value;
        try {
            value = org.apache.xml.security.utils.Base64.encode(credential.getEntityCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            throw new MetadataException("Error while encoding the certificate.", e);
        }
        cert.setValue(value);
        data.getX509Certificates().add(cert);
        keyInfo.getX509Datas().add(data);

        if (log.isDebugEnabled()) {
            log.debug("Completed KeyInfo element creation");
        }

        return keyInfo;
    }

    /**
     * Creates the key descriptor element with new key info each time called.
     *
     * @return KeyDescriptor with a new KeyInfo element.
     * @throws MetadataException
     */
    private KeyDescriptor createKeyDescriptor() throws MetadataException {

        if (log.isDebugEnabled()) {
            log.debug("Creating the KeyDescriptor element");
        }
        KeyDescriptor keyDescriptor = BuilderUtil.createSAMLObject(ConfigElements.FED_METADATA_NS, "KeyDescriptor", "");
        keyDescriptor.setUse(UsageType.SIGNING);
        keyDescriptor.setKeyInfo(createKeyInfo());

        return keyDescriptor;
    }

    /**
     * Marshall the provided descriptor element contents to DOM.
     *
     * @param desc
     * @return Document after marshalling the SAML object to XML
     * @throws MetadataException
     */
    private Document marshallDescriptor(EntityDescriptor desc) throws MetadataException {

        DocumentBuilderFactory factory = IdentityUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new MetadataException("Error while creating the document.", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Marshalling the metadata element contents");
        }
        Document document = builder.newDocument();
        Marshaller out = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(desc);

        try {
            out.marshall(desc, document);
        } catch (MarshallingException e) {
            throw new MetadataException("Error while marshalling the descriptor.", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Marshalling completed");
        }
        return document;

    }

}
