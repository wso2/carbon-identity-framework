/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt.defaultsequence;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.cache.DefaultAuthSeqMgtCache;
import org.wso2.carbon.identity.application.mgt.cache.DefaultAuthSeqMgtCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.dao.DefaultAuthSeqMgtDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.DefaultAuthSeqMgtDAOImpl;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This service provides the services needed to manage tenant wise default authentication sequences.
 */
public class DefaultAuthSeqMgtServiceImpl implements DefaultAuthSeqMgtService {

    private static final Log log = LogFactory.getLog(DefaultAuthSeqMgtServiceImpl.class);
    private static final String AUTHENTICATOR_NOT_AVAILABLE = "Authenticator %s is not available in the server.";
    private static final String AUTHENTICATOR_NOT_CONFIGURED = "Authenticator %s is not configured for %s " +
            "identity Provider.";
    private static final String FEDERATED_IDP_NOT_AVAILABLE = "Federated Identity Provider %s is not available in " +
            "the server.";
    public static final String IS_HANDLER = "IS_HANDLER";

    private static volatile DefaultAuthSeqMgtServiceImpl defaultAuthSeqMgtService;

    private DefaultAuthSeqMgtServiceImpl() {

    }

    public static DefaultAuthSeqMgtServiceImpl getInstance() {

        if (defaultAuthSeqMgtService == null) {
            synchronized (DefaultAuthSeqMgtServiceImpl.class) {
                if (defaultAuthSeqMgtService == null) {
                    defaultAuthSeqMgtService = new DefaultAuthSeqMgtServiceImpl();
                }
            }
        }
        return defaultAuthSeqMgtService;
    }

    @Override
    public void createDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Creating default authentication sequence in tenant: %s", tenantDomain));
        }

        String errorMsg = "Validation error when creating default authentication sequence in : ";
        validateDefaultAuthSeqExists(sequence.getName(), tenantDomain);
        unmarshalDefaultAuthSeq(sequence, tenantDomain, errorMsg);
        validateAuthSeqConfiguration(sequence, tenantDomain, errorMsg);
        doCreateDefaultAuthSeq(sequence, tenantDomain);
        clearServiceProviderCache(tenantDomain);
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeq(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving default authentication sequence of tenant: " + tenantDomain);
        }
        return doGetDefaultAuthSeq(sequenceName, tenantDomain);
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInXML(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving default authentication sequence of tenant: " + tenantDomain);
        }

        return doGetDefaultAuthSeqInXml(sequenceName, tenantDomain);
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving basic info of default authentication sequence of tenant: " + tenantDomain);
        }

        return doGetDefaultAuthenticationSeqInfo(sequenceName, tenantDomain);
    }

    @Override
    public boolean isExistingDefaultAuthenticationSequence(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Checking existence of default authentication sequence in tenant: " + tenantDomain);
        }
        return doCheckDefaultAuthSeq(sequenceName, tenantDomain);
    }

    @Override
    public void deleteDefaultAuthenticationSeq(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting default authentication sequence of tenant: " + tenantDomain);
        }

        doDeleteDefaultAuthSeq(sequenceName, tenantDomain);
        clearServiceProviderCache(tenantDomain);
    }

    @Override
    public void updateDefaultAuthenticationSeq(String sequenceName, DefaultAuthenticationSequence sequence,
                                               String tenantDomain) throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Updating default authentication sequence of tenant: " + tenantDomain);
        }

        String errorMsg = "Validation error when updating default authentication sequence in : ";
        validateDefaultAuthSeqNotExists(sequenceName, tenantDomain);
        unmarshalDefaultAuthSeq(sequence, tenantDomain, errorMsg);
        validateAuthSeqConfiguration(sequence, tenantDomain, errorMsg);
        doUpdateDefaultAuthSeq(sequenceName, sequence, tenantDomain);
        clearServiceProviderCache(tenantDomain);
    }

    private void doCreateDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        seqMgtDAO.createDefaultAuthenticationSeq(sequence, tenantDomain);
        addDefaultAuthSeqToCache(sequence, tenantDomain);
    }

    private DefaultAuthenticationSequence doGetDefaultAuthSeq(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCacheEntry entry = DefaultAuthSeqMgtCache.getInstance().getValueFromCache(sequenceName);
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Default authentication sequence of tenant: " + tenantDomain +
                            " is retrieved from cache.");
                }
                return entry.getSequence();
            }
        }

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        DefaultAuthenticationSequence sequence = seqMgtDAO.getDefaultAuthenticationSeq(sequenceName, tenantDomain);

        if (sequence != null) {
            addDefaultAuthSeqToCache(sequence, tenantDomain);
        }
        return sequence;
    }

    private DefaultAuthenticationSequence doGetDefaultAuthSeqInXml(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        DefaultAuthenticationSequence sequence = getDefaultAuthSeqFromCache(sequenceName, tenantDomain);
        if (sequence == null) {
            DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
            sequence = seqMgtDAO.getDefaultAuthenticationSeq(sequenceName, tenantDomain);
        }

        if (sequence != null) {
            if (sequence.getContentXml() == null) {
                String sequenceInXML = marshalDefaultAuthSeq(sequence.getContent(), tenantDomain);
                String updatedSequenceInXML = removeUnsupportedXMLElements(sequenceInXML);
                sequence.setContentXml(updatedSequenceInXML);
                addDefaultAuthSeqToCache(sequence, tenantDomain);
            }
        }
        return sequence;
    }

    private DefaultAuthenticationSequence doGetDefaultAuthenticationSeqInfo(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        DefaultAuthenticationSequence sequence = getDefaultAuthSeqFromCache(sequenceName, tenantDomain);
        if (sequence == null) {
            DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
            sequence = seqMgtDAO.getDefaultAuthenticationSeqInfo(sequenceName, tenantDomain);
        }
        return sequence;
    }

    private boolean doCheckDefaultAuthSeq(String sequenceName, String tenantDomain) throws DefaultAuthSeqMgtException {

        // Check existence in cache
        DefaultAuthenticationSequence sequence = getDefaultAuthSeqFromCache(sequenceName, tenantDomain);

        if (sequence == null) {
            // Check existence in database
            DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
            return seqMgtDAO.isDefaultAuthSeqExists(sequenceName, tenantDomain);
        }
        return true;
    }

    private void doDeleteDefaultAuthSeq(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtServerException {

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        seqMgtDAO.deleteDefaultAuthenticationSeq(sequenceName, tenantDomain);

        removeDefaultAuthSeqFromCache(sequenceName, tenantDomain);
    }

    private void doUpdateDefaultAuthSeq(String sequenceName, DefaultAuthenticationSequence sequence,
                                        String tenantDomain) throws DefaultAuthSeqMgtServerException {

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        seqMgtDAO.updateDefaultAuthenticationSeq(sequenceName, sequence, tenantDomain);

        addDefaultAuthSeqToCache(sequence, tenantDomain);
    }

    private void unmarshalDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain, String errorMsg)
            throws DefaultAuthSeqMgtException {

        if (sequence.getContent() == null && sequence.getContentXml() != null) {
            checkUnsupportedXMLElements(sequence.getContentXml(), tenantDomain, errorMsg);
            sequence.setContent(unmarshalDefaultAuthSeq(sequence.getContentXml(), tenantDomain));
        }
    }

    private void validateDefaultAuthSeqExists(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        List<String> validationMsg = new ArrayList<>();
        if (doCheckDefaultAuthSeq(sequenceName, tenantDomain)) {
            validationMsg.add(String.format("Default authentication sequence is already configured for tenant: %s.",
                    tenantDomain));
            throw new DefaultAuthSeqMgtException(validationMsg.toArray(new String[0]));
        }
    }

    private void validateDefaultAuthSeqNotExists(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        List<String> validationMsg = new ArrayList<>();
        if (!doCheckDefaultAuthSeq(sequenceName, tenantDomain)) {
            validationMsg.add(String.format("Default authentication sequence is not configured for tenant: %s.",
                    tenantDomain));
            throw new DefaultAuthSeqMgtException(validationMsg.toArray(new String[0]));
        }
    }

    private void addDefaultAuthSeqToCache(DefaultAuthenticationSequence sequence, String tenantDomain) {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCacheEntry entry = new DefaultAuthSeqMgtCacheEntry(sequence);
            DefaultAuthSeqMgtCache.getInstance().addToCache(sequence.getName(), entry);
            if (log.isDebugEnabled()) {
                log.debug("Default authentication sequence for tenant: " + tenantDomain + " is added to cache.");
            }
        }
    }

    private void removeDefaultAuthSeqFromCache(String sequenceName, String tenantDomain) {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCache.getInstance().clearCacheEntry(sequenceName);
            if (log.isDebugEnabled()) {
                log.debug("Default authentication sequence for tenant: " + tenantDomain + " is removed from cache.");
            }
        }
    }

    private DefaultAuthenticationSequence getDefaultAuthSeqFromCache(String sequenceName, String tenantDomain) {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCacheEntry entry = DefaultAuthSeqMgtCache.getInstance().getValueFromCache(sequenceName);
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Default authentication sequence of tenant: " + tenantDomain +
                            " is retrieved from cache.");
                }
                return entry.getSequence();
            }
        }
        return null;
    }

    private void validateAuthSeqConfiguration(DefaultAuthenticationSequence sequence, String tenantDomain,
                                              String errorMsg) throws DefaultAuthSeqMgtException {

        List<String> validationMsg = new ArrayList<>();
        LocalAndOutboundAuthenticationConfig authenticationConfig = sequence.getContent();
        if (authenticationConfig == null) {
            return;
        }

        AuthenticationStep[] authenticationSteps = authenticationConfig.getAuthenticationSteps();
        if (authenticationSteps == null || authenticationSteps.length == 0) {
            return;
        }
        Map<String, Property[]> allLocalAuthenticators;
        try {
            allLocalAuthenticators = getAllLocalAuthenticators(tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new DefaultAuthSeqMgtServerException(errorMsg, e);
        }

        AtomicBoolean isAuthenticatorIncluded = new AtomicBoolean(false);

        for (AuthenticationStep authenticationStep : authenticationSteps) {
            if (authenticationStep == null || (authenticationStep.getFederatedIdentityProviders() == null &&
                    authenticationStep.getLocalAuthenticatorConfigs() == null)) {
                validationMsg.add("Some authentication steps do not have authenticators.");
                break;
            }
            for (IdentityProvider idp : authenticationStep.getFederatedIdentityProviders()) {
                validateFederatedIdp(idp, isAuthenticatorIncluded, validationMsg, tenantDomain);
            }
            validateLocalAuthenticatorConfig(validationMsg, allLocalAuthenticators, isAuthenticatorIncluded,
                    authenticationStep);
        }

        if (!isAuthenticatorIncluded.get()) {
            validationMsg.add("No authenticator have been registered in the authentication flow.");
        }

        if (!validationMsg.isEmpty()) {
            log.error(errorMsg + tenantDomain);
            for (String msg : validationMsg) {
                log.error(msg);
            }
            throw new DefaultAuthSeqMgtException(validationMsg.toArray(new String[0]));
        }

        removeUnsupportedConfigurations(authenticationConfig);
    }

    private void removeUnsupportedConfigurations(LocalAndOutboundAuthenticationConfig authenticationConfig) {

        authenticationConfig.setAuthenticationType(null);
        authenticationConfig.setSubjectClaimUri(null);
        authenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs(false);
        authenticationConfig.setUseTenantDomainInLocalSubjectIdentifier(false);
        authenticationConfig.setUseUserstoreDomainInLocalSubjectIdentifier(false);
        authenticationConfig.setUseUserstoreDomainInRoles(false);
        authenticationConfig.setEnableAuthorization(false);
    }

    private void validateLocalAuthenticatorConfig(List<String> validationMsg,
                                                  Map<String, Property[]> allLocalAuthenticators,
                                                  AtomicBoolean isAuthenticatorIncluded,
                                                  AuthenticationStep authenticationStep) {

        for (LocalAuthenticatorConfig localAuth : authenticationStep.getLocalAuthenticatorConfigs()) {
            if (!allLocalAuthenticators.keySet().contains(localAuth.getName())) {
                validationMsg.add(String.format(AUTHENTICATOR_NOT_AVAILABLE, localAuth.getName()));
            } else if (!isAuthenticatorIncluded.get()) {
                Property[] properties = allLocalAuthenticators.get(localAuth.getName());
                if (properties.length == 0) {
                    isAuthenticatorIncluded.set(true);
                } else {
                    for (Property property : properties) {
                        if (!(IS_HANDLER.equals(property.getName()) && Boolean.valueOf(property.getValue()))) {
                            isAuthenticatorIncluded.set(true);
                        }
                    }
                }
            }
        }
    }

    private Map<String, Property[]> getAllLocalAuthenticators(String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationManagementService applicationMgtService = ApplicationManagementService.getInstance();
        return Arrays.stream(applicationMgtService
                .getAllLocalAuthenticators(tenantDomain))
                .collect(Collectors.toMap(LocalAuthenticatorConfig::getName, LocalAuthenticatorConfig::getProperties));
    }

    private void validateFederatedIdp(IdentityProvider idp, AtomicBoolean isAuthenticatorIncluded, List<String>
            validationMsg, String tenantDomain) {

        try {
            IdentityProvider savedIdp = IdentityProviderManager.getInstance().getIdPByName(idp
                    .getIdentityProviderName(), tenantDomain, false);
            if (savedIdp.getId() == null) {
                validationMsg.add(String.format(FEDERATED_IDP_NOT_AVAILABLE,
                        idp.getIdentityProviderName()));
            } else if (savedIdp.getFederatedAuthenticatorConfigs() != null) {
                isAuthenticatorIncluded.set(true);
                List<String> savedIdpAuthenticators = Arrays.stream(savedIdp
                        .getFederatedAuthenticatorConfigs()).map(FederatedAuthenticatorConfig::getName)
                        .collect(Collectors.toList());
                for (FederatedAuthenticatorConfig federatedAuth : idp.getFederatedAuthenticatorConfigs()) {
                    if (!savedIdpAuthenticators.contains(federatedAuth.getName())) {
                        validationMsg.add(String.format(AUTHENTICATOR_NOT_CONFIGURED,
                                federatedAuth.getName(), idp.getIdentityProviderName()));
                    }
                }
            } else {
                for (FederatedAuthenticatorConfig federatedAuth : idp.getFederatedAuthenticatorConfigs()) {
                    validationMsg.add(String.format(AUTHENTICATOR_NOT_CONFIGURED,
                            federatedAuth.getName(), idp.getIdentityProviderName()));
                }
            }
        } catch (IdentityProviderManagementException e) {
            String errorMsg = String.format(FEDERATED_IDP_NOT_AVAILABLE, idp.getIdentityProviderName());
            log.error(errorMsg, e);
            validationMsg.add(errorMsg);
        }
    }

    private void checkUnsupportedXMLElements(String seqConfigXml, String tenantDomain,
                                             String errorMsg) throws DefaultAuthSeqMgtException {

        List<String> validationMsg = new ArrayList<>();

        if (seqConfigXml != null) {
            try {
                DocumentBuilder builder = IdentityUtil.getSecuredDocumentBuilderFactory().newDocumentBuilder();
                InputSource src = new InputSource();
                src.setCharacterStream(new StringReader(seqConfigXml));
                Document doc = builder.parse(src);
                if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase(
                        LocalAndOutboundAuthenticationConfig.class.getSimpleName())) {
                    validationMsg.add("Invalid XML element: " + doc.getDocumentElement().getNodeName() + " in the " +
                            "sequence configuration.");
                } else {
                    NodeList nodeList = doc.getDocumentElement().getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node currentNode = nodeList.item(i);
                        if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
                                !currentNode.getNodeName().equals("AuthenticationSteps") &&
                                !currentNode.getNodeName().equals("AuthenticationScript")) {
                            validationMsg.add("Invalid XML element: " + currentNode.getNodeName() + " in the " +
                                    "sequence configuration.");
                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new DefaultAuthSeqMgtServerException(errorMsg, e);
            }
        }

        if (!validationMsg.isEmpty()) {
            log.error(errorMsg + tenantDomain);
            for (String msg : validationMsg) {
                log.error(msg);
            }
            throw new DefaultAuthSeqMgtException(validationMsg.toArray(new String[0]));
        }
    }

    private String removeUnsupportedXMLElements(String seqConfigXml) throws DefaultAuthSeqMgtException {

        String updatedSeqConfigXml = null;
        if (seqConfigXml != null) {
            try {
                DocumentBuilder builder = IdentityUtil.getSecuredDocumentBuilderFactory().newDocumentBuilder();
                InputSource src = new InputSource();
                src.setCharacterStream(new StringReader(seqConfigXml));
                Document doc = builder.parse(src);
                NodeList nodeList = doc.getDocumentElement().getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node currentNode = nodeList.item(i);
                    if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
                            !currentNode.getNodeName().equals("AuthenticationSteps") &&
                            !currentNode.getNodeName().equals("AuthenticationScript")) {
                        doc.getDocumentElement().removeChild(currentNode);
                    }
                }

                StringWriter stringWriter = new StringWriter();
                Transformer transformer = IdentityUtil.getSecuredTransformerFactory().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "true");
                transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
                updatedSeqConfigXml = stringWriter.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                throw new DefaultAuthSeqMgtServerException("Error when retrieving default authentication sequence", e);
            }
        }
        return updatedSeqConfigXml;
    }

    private String marshalDefaultAuthSeq(LocalAndOutboundAuthenticationConfig sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LocalAndOutboundAuthenticationConfig.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            DocumentBuilderFactory docBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
            Document document = docBuilderFactory.newDocumentBuilder().newDocument();
            marshaller.marshal(sequence, document);
            TransformerFactory transformerFactory = IdentityUtil.getSecuredTransformerFactory();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    "AuthenticationScript inboundConfiguration");
            StringWriter stringBuilder = new StringWriter();
            StreamResult result = new StreamResult(stringBuilder);
            transformer.transform(new DOMSource(document), result);
            return stringBuilder.getBuffer().toString();
        } catch (JAXBException | ParserConfigurationException | TransformerException e) {
            throw new DefaultAuthSeqMgtException("Error in marshalling default authentication sequence in: " +
                    tenantDomain, e);
        }
    }

    /**
     * Convert xml file of default authentication sequence to object.
     *
     * @param defaultAuthSeq xml string of the default authentication sequence
     * @param tenantDomain   tenant domain name
     * @return LocalAndOutboundAuthenticationConfig instance
     * @throws DefaultAuthSeqMgtException Auth Sequence Management Client Exception
     */
    private LocalAndOutboundAuthenticationConfig unmarshalDefaultAuthSeq(String defaultAuthSeq, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (StringUtils.isEmpty(defaultAuthSeq)) {
            throw new DefaultAuthSeqMgtException(new String[]{"Empty default authentication sequence " +
                    "configuration is provided"});
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LocalAndOutboundAuthenticationConfig.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<LocalAndOutboundAuthenticationConfig> root = unmarshaller.unmarshal(
                    new StreamSource(new ByteArrayInputStream(defaultAuthSeq.getBytes(StandardCharsets.UTF_8))),
                    LocalAndOutboundAuthenticationConfig.class);
            if (root.getName().getLocalPart().equalsIgnoreCase(LocalAndOutboundAuthenticationConfig.class
                    .getSimpleName())) {
                return root.getValue();
            }
            throw new DefaultAuthSeqMgtException(new String[]{"Syntax error in the provided default " +
                    "authentication sequence"});
        } catch (JAXBException e) {
            String msg = "Error in reading default authentication sequence configuration in tenant: " + tenantDomain;
            log.error(msg, e);
            throw new DefaultAuthSeqMgtException(new String[]{msg});
        }
    }

    private void clearServiceProviderCache(String tenantDomain) {

        IdentityServiceProviderCache.getInstance().clear();
        if (log.isDebugEnabled()) {
            log.debug("Clearing ServiceProviderCache of tenant: " + tenantDomain);
        }
    }
}
