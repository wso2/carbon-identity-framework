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
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
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

import java.io.ByteArrayInputStream;
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
public class DefaultAuthSeqMgtServiceImpl extends DefaultAuthSeqMgtService {

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

        validateDefaultAuthSeqExists(tenantDomain);
        unmarshalDefaultAuthSeq(sequence, tenantDomain);
        validateAuthSeqConfiguration(sequence, tenantDomain, "Validation error when creating default " +
                "authentication sequence in : ");
        doCreateDefaultAuthSeq(sequence, tenantDomain);
        clearServiceProviderCache(tenantDomain);
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeq(String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving default authentication sequence of tenant: " + tenantDomain);
        }
        return doGetDefaultAuthSeq(tenantDomain);
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInXML(String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving default authentication sequence of tenant: " + tenantDomain);
        }

        return doGetDefaultAuthSeqInXml(tenantDomain);
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo(String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving basic info of default authentication sequence of tenant: " + tenantDomain);
        }

        return doGetDefaultAuthenticationSeqInfo(tenantDomain);
    }

    @Override
    public boolean isExistingDefaultAuthenticationSequence(String tenantDomain) throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Checking existence of default authentication sequence in tenant: " + tenantDomain);
        }
        return doCheckDefaultAuthSeq(tenantDomain);
    }

    @Override
    public void deleteDefaultAuthenticationSeq(String tenantDomain) throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting default authentication sequence of tenant: " + tenantDomain);
        }

        doDeleteDefaultAuthSeq(tenantDomain);
        clearServiceProviderCache(tenantDomain);
    }

    @Override
    public void updateDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        if (log.isDebugEnabled()) {
            log.debug("Updating default authentication sequence of tenant: " + tenantDomain);
        }

        validateDefaultAuthSeqNotExists(tenantDomain);
        unmarshalDefaultAuthSeq(sequence, tenantDomain);
        validateAuthSeqConfiguration(sequence, tenantDomain, "Validation error when updating default " +
                "authentication sequence in : ");
        doUpdateDefaultAuthSeq(sequence, tenantDomain);
        clearServiceProviderCache(tenantDomain);
    }

    private void doCreateDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        if (seqMgtDAO.isDefaultAuthSeqExists(tenantDomain)) {
            throw new DefaultAuthSeqMgtClientException(new String[]{"Default auth sequence is already exists in " +
                    "tenant: " + tenantDomain});
        }

        seqMgtDAO.createDefaultAuthenticationSeq(sequence, tenantDomain);
        addDefaultAuthSeqToCache(sequence, tenantDomain);
    }

    private DefaultAuthenticationSequence doGetDefaultAuthSeq(String tenantDomain) throws DefaultAuthSeqMgtException {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCacheEntry entry = DefaultAuthSeqMgtCache.getInstance().getValueFromCache(tenantDomain);
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Default authentication sequence of tenant: " + tenantDomain +
                            " is retrieved from cache.");
                }
                return entry.getSequence();
            }
        }

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        DefaultAuthenticationSequence sequence = seqMgtDAO.getDefaultAuthenticationSeq(tenantDomain);

        if (sequence != null) {
            addDefaultAuthSeqToCache(sequence, tenantDomain);
        }
        return sequence;
    }

    private DefaultAuthenticationSequence doGetDefaultAuthSeqInXml(String tenantDomain)
            throws DefaultAuthSeqMgtException {

        DefaultAuthenticationSequence sequence = getDefaultAuthSeqFromCache(tenantDomain);
        if (sequence == null) {
            DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
            sequence = seqMgtDAO.getDefaultAuthenticationSeq(tenantDomain);
            addDefaultAuthSeqToCache(sequence, tenantDomain);
        }

        if (sequence != null) {
            if (sequence.getContentXml() == null) {
                String sequenceInXML = marshalDefaultAuthSeq(sequence.getContent(), tenantDomain);
                sequence.setContentXml(sequenceInXML);
            }
        }
        return sequence;
    }

    private DefaultAuthenticationSequence doGetDefaultAuthenticationSeqInfo(String tenantDomain)
            throws DefaultAuthSeqMgtException {

        DefaultAuthenticationSequence sequence = getDefaultAuthSeqFromCache(tenantDomain);
        if (sequence == null) {
            DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
            sequence = seqMgtDAO.getDefaultAuthenticationSeqInfo(tenantDomain);
        }
        return sequence;
    }

    private boolean doCheckDefaultAuthSeq(String tenantDomain) throws DefaultAuthSeqMgtException {

        // Check existence in cache
        DefaultAuthenticationSequence sequence = getDefaultAuthSeqFromCache(tenantDomain);

        if (sequence == null) {
            // Check existence in database
            DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
            return seqMgtDAO.isDefaultAuthSeqExists(tenantDomain);
        }
        return true;
    }

    private void doDeleteDefaultAuthSeq(String tenantDomain) throws DefaultAuthSeqMgtServerException {

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        seqMgtDAO.deleteDefaultAuthenticationSeq(tenantDomain);

        removeDefaultAuthSeqFromCache(tenantDomain);
    }

    private void doUpdateDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtServerException {

        DefaultAuthSeqMgtDAO seqMgtDAO = new DefaultAuthSeqMgtDAOImpl();
        seqMgtDAO.updateDefaultAuthenticationSeq(sequence, tenantDomain);

        addDefaultAuthSeqToCache(sequence, tenantDomain);
    }

    private void unmarshalDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtClientException {

        if (sequence.getContent() == null && sequence.getContentXml() != null) {
            sequence.setContent(unmarshalDefaultAuthSeq(sequence.getContentXml(), tenantDomain));
        }
    }

    private void validateDefaultAuthSeqExists(String tenantDomain) throws DefaultAuthSeqMgtException {

        List<String> validationMsg = new ArrayList<>();
        if (doCheckDefaultAuthSeq(tenantDomain)) {
            validationMsg.add(String.format("Default authentication sequence is already configured for tenant: %s.",
                    tenantDomain));
            throw new DefaultAuthSeqMgtClientException(validationMsg.toArray(new String[0]));
        }
    }

    private void validateDefaultAuthSeqNotExists(String tenantDomain) throws DefaultAuthSeqMgtException {

        List<String> validationMsg = new ArrayList<>();
        if (!doCheckDefaultAuthSeq(tenantDomain)) {
            validationMsg.add(String.format("Default authentication sequence is not configured for tenant: %s.",
                    tenantDomain));
            throw new DefaultAuthSeqMgtClientException(validationMsg.toArray(new String[0]));
        }
    }

    private void addDefaultAuthSeqToCache(DefaultAuthenticationSequence sequence, String tenantDomain) {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCacheEntry entry = new DefaultAuthSeqMgtCacheEntry(sequence);
            DefaultAuthSeqMgtCache.getInstance().addToCache(tenantDomain, entry);
            if (log.isDebugEnabled()) {
                log.debug("Default authentication sequence for tenant: " + tenantDomain + " is added to cache.");
            }
        }
    }

    private void removeDefaultAuthSeqFromCache(String tenantDomain) {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCache.getInstance().clearCacheEntry(tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Default authentication sequence for tenant: " + tenantDomain + " is removed from cache.");
            }
        }
    }

    private DefaultAuthenticationSequence getDefaultAuthSeqFromCache(String tenantDomain) {

        if (DefaultAuthSeqMgtCache.getInstance().isEnabled()) {
            DefaultAuthSeqMgtCacheEntry entry = DefaultAuthSeqMgtCache.getInstance().getValueFromCache(tenantDomain);
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

        try {
            validateLocalAndOutBoundAuthenticationConfig(sequence.getContent(), tenantDomain);
        } catch (IdentityApplicationManagementValidationException e) {
            log.error(errorMsg + tenantDomain);
            for (String msg : e.getValidationMsg()) {
                log.error(msg);
            }
            throw new DefaultAuthSeqMgtClientException(e.getValidationMsg());
        } catch (IdentityApplicationManagementException e) {
            throw new DefaultAuthSeqMgtServerException(errorMsg, e);
        }
    }

    private void validateLocalAndOutBoundAuthenticationConfig(
            LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig, String tenantDomain)
            throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();
        if (localAndOutBoundAuthenticationConfig == null) {
            return;
        }

        AuthenticationStep[] authenticationSteps = localAndOutBoundAuthenticationConfig.getAuthenticationSteps();
        if (authenticationSteps == null || authenticationSteps.length == 0) {
            return;
        }
        Map<String, Property[]> allLocalAuthenticators = getAllLocalAuthenticators(tenantDomain);
        AtomicBoolean isAuthenticatorIncluded = new AtomicBoolean(false);

        for (AuthenticationStep authenticationStep : authenticationSteps) {
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
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
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

    private Map<String, Property[]> getAllLocalAuthenticators(String tenantDomain) throws IdentityApplicationManagementException {
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

    private String marshalDefaultAuthSeq(LocalAndOutboundAuthenticationConfig sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LocalAndOutboundAuthenticationConfig.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            DocumentBuilderFactory docBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
            Document document = docBuilderFactory.newDocumentBuilder().newDocument();
            marshaller.marshal(sequence, document);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
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
     * @throws DefaultAuthSeqMgtClientException Auth Sequence Management Client Exception
     */
    private LocalAndOutboundAuthenticationConfig unmarshalDefaultAuthSeq(String defaultAuthSeq, String tenantDomain)
            throws DefaultAuthSeqMgtClientException {

        if (StringUtils.isEmpty(defaultAuthSeq)) {
            throw new DefaultAuthSeqMgtClientException(new String[]{"Empty default authentication sequence " +
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
            throw new DefaultAuthSeqMgtClientException(new String[]{"Syntax error in the provided default " +
                    "authentication sequence"});
        } catch (JAXBException e) {
            String msg = "Error in reading default authentication sequence configuration in tenant: " + tenantDomain;
            log.error(msg, e);
            throw new DefaultAuthSeqMgtClientException(new String[]{msg});
        }
    }

    private void clearServiceProviderCache(String tenantDomain) {

        IdentityServiceProviderCache.getInstance().clear();
        if (log.isDebugEnabled()) {
            log.debug("Clearing ServiceProviderCache of tenant: " + tenantDomain);
        }
    }
}
