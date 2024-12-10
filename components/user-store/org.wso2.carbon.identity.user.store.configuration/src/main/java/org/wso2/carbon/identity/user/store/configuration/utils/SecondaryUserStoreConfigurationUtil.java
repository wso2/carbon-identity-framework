/*
 * Copyright (c) 2014-2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants.UserStoreState;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.beans.MaskedProperty;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigComponent;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.DEPLOYMENT_DIRECTORY;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ENCRYPTED_PROPERTY_MASK;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.FILE_EXTENSION_XML;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORES;

/**
 * Util class responsible for processing encryption and decryption over secondary user store
 */
public class SecondaryUserStoreConfigurationUtil {

    private static final Log LOG = LogFactory.getLog(SecondaryUserStoreConfigurationUtil.class);

    private SecondaryUserStoreConfigurationUtil() {

    }

    /**
     * @param plainText Cipher text to be encrypted
     * @return Returns the encrypted text
     * @throws IdentityUserStoreMgtException Encryption failed
     */
    public static String encryptPlainText(String plainText) throws IdentityUserStoreMgtException {

        SecondaryUserStoreConfigurator configurator = new SecondaryUserStoreConfigurator();
        return configurator.encryptPlainText(plainText);
    }

    /**
     * To get the path of the userStore XML file
     * @param domainName userStore domain name
     * @return the path of the userstore xml
     * @throws IdentityUserStoreMgtException if an error occurs when getting the file path.
     */
    public static Path getUserStoreConfigurationFile(String domainName) throws IdentityUserStoreMgtException {

        String fileName = domainName.replace(UserStoreConfigurationConstant.PERIOD,
                UserStoreConfigurationConstant.UNDERSCORE);
        Path userStore;

        if (!IdentityUtil.isValidFileName(fileName)) {
            String message = "Provided domain name : '" + domainName + "' is invalid.";
            throw new IdentityUserStoreMgtException(message);
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            userStore = Paths.get(DEPLOYMENT_DIRECTORY);
        } else {
            String tenantFilePath = CarbonUtils.getCarbonTenantsDirPath();
            userStore = Paths.get(tenantFilePath, String.valueOf(tenantId), USERSTORES);
        }
        return getUserStoreConfigFile(userStore, fileName);
    }

    private static Path getUserStoreConfigFile(Path userStore, String fileName) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!Files.exists(userStore)) {
            try {
                Files.createDirectory(userStore);
                LOG.info("folder 'userstores' created to store configurations for tenant = " + tenantId);
            } catch (IOException e) {
                LOG.error("Error while creating 'userstores' directory to store configurations for tenant = "
                          + tenantId);
            }
        }
        return Paths.get(userStore.toString(), fileName + FILE_EXTENSION_XML);
    }

    /**
     * This method is used to write userStore xml file.
     *
     * @deprecated use {@link #writeUserMgtXMLFile(Path, UserStoreDTO, boolean, boolean, String)} instead.
     */
    @Deprecated
    public static void writeUserMgtXMLFile(Path userStoreConfigFile, UserStoreDTO userStoreDTO,
                                           boolean editSecondaryUserStore, boolean isStateChange)
            throws IdentityUserStoreMgtException {

        writeUserMgtXMLFile(userStoreConfigFile, userStoreDTO, editSecondaryUserStore, isStateChange,
                userStoreDTO.getDomainId());
    }

    /**
     * This method is used to write userStore xml file.
     * @param userStoreConfigFile path of the userStore configuration file
     * @param userStoreDTO instance of {@link UserStoreDTO}
     * @param editSecondaryUserStore true if it is update operation
     * @param  existingDomainName domain name of existing userstore
     * @throws IdentityUserStoreMgtException throws if an error occurred while writing to the xml file.
     */
    public static void writeUserMgtXMLFile(Path userStoreConfigFile, UserStoreDTO userStoreDTO,
                                           boolean editSecondaryUserStore, boolean isStateChange,
                                           String existingDomainName)
            throws IdentityUserStoreMgtException {

        boolean isDisable = false;
        if (userStoreDTO.getDisabled() != null) {
            isDisable = userStoreDTO.getDisabled();
        }
        String domain = userStoreDTO.getDomainId();
        DocumentBuilderFactory documentFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder documentBuilder;

        try {
            documentBuilder = documentFactory.newDocumentBuilder();

            if (isStateChange) {
                updateStateOfUserStore(userStoreConfigFile, isDisable, domain, documentBuilder);
            } else {
                updateUserStoreProperties(userStoreConfigFile, userStoreDTO, editSecondaryUserStore, documentBuilder,
                        existingDomainName);
            }
        } catch (ParserConfigurationException e) {
            String errMsg = " Error occurred due to serious parser configuration exception of " + userStoreConfigFile;
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (TransformerException e) {
            String errMsg = " Error occurred during the transformation process of " + userStoreConfigFile;
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (IOException e) {
            String errMsg = " Error occurred while creating or closing the output stream from " + userStoreConfigFile;
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (SAXException e) {
            throw new IdentityUserStoreMgtException("Error while updating user store state", e);
        }
    }


    /**
     * This method is used to Get the user store config file.
     *
     * @deprecated use {@link #getUserStoreProperties(UserStoreDTO, String)} instead.
     */
    @Deprecated
    public static String getUserStoreProperties(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {
        return getUserStoreProperties(userStoreDTO, userStoreDTO.getDomainId());
    }

    /**
     * Get the user store config file.
     * @param userStoreDTO an instance of {@link UserStoreDTO}
     * @param existingDomainName existing userstore domain name
     * @return user store properties as a String.
     * @throws IdentityUserStoreMgtException throws if an error occurred while getting the user store properties.
     */
    public static String getUserStoreProperties(UserStoreDTO userStoreDTO, String existingDomainName)
            throws IdentityUserStoreMgtException {

        String userStoreProperties;
        DocumentBuilderFactory documentFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        try {
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document doc = getDocument(userStoreDTO, false, documentBuilder, existingDomainName);
            StringWriter writer = new StringWriter();
            transformProperties().transform(new DOMSource(doc), new StreamResult(writer));
            //To replace the line breaks
            userStoreProperties = writer.getBuffer().toString().replaceAll("\n|\r", "");

        } catch (ParserConfigurationException | TransformerException e) {
            throw new IdentityUserStoreMgtException("Error occured while parsing the user store file.", e);
        } catch (IdentityUserStoreMgtException e) {
            throw new IdentityUserStoreMgtException("Error occured while getting the user store properties.", e);
        }
        return userStoreProperties;
    }

    public static Map<String, String> getSecondaryUserStorePropertiesFromTenantUserRealm(String userStoreDomain)
            throws IdentityUserStoreMgtException {

        Map<String, String> secondaryUserStoreProperties = null;
        try {
            RealmConfiguration realmConfiguration = UserStoreConfigComponent.getRealmService().getTenantUserRealm(
                    getTenantIdInTheCurrentContext()).getRealmConfiguration();
            while (realmConfiguration != null) {
                String domainName = realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig
                        .PROPERTY_DOMAIN_NAME);
                if (StringUtils.equalsIgnoreCase(domainName, userStoreDomain)) {
                    secondaryUserStoreProperties = realmConfiguration.getUserStoreProperties();
                    break;
                } else {
                    realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
                }
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while retrieving user store configurations for user store domain: "
                    + userStoreDomain;
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
        return secondaryUserStoreProperties;
    }

    private static Transformer transformProperties() throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "6");
        return transformer;
    }

    private static void updateUserStoreProperties(Path userStoreConfigFile, UserStoreDTO userStoreDTO,
                                                  boolean editSecondaryUserStore, DocumentBuilder documentBuilder,
                                                  String existingDomainName)
            throws IdentityUserStoreMgtException, IOException, TransformerException {

        Document doc = getDocument(userStoreDTO, editSecondaryUserStore, documentBuilder, existingDomainName);
        StreamResult result = new StreamResult(Files.newOutputStream(userStoreConfigFile));
        DOMSource source = new DOMSource(doc);
        transformProperties().transform(source, result);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing the output stream from " + userStoreConfigFile);
        }
        result.getOutputStream().close();

    }

    private static Document getDocument(UserStoreDTO userStoreDTO, boolean editSecondaryUserStore,
                                        DocumentBuilder documentBuilder, String existingDomainName)
            throws IdentityUserStoreMgtException {

        Document doc = documentBuilder.newDocument();

        //create UserStoreManager element
        Element userStoreElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER);
        doc.appendChild(userStoreElement);

        Attr attrClass = doc.createAttribute("class");
        if (userStoreDTO != null) {
            attrClass.setValue(userStoreDTO.getClassName());
            userStoreElement.setAttributeNode(attrClass);
            if (userStoreDTO.getClassName() != null) {
                addProperties(existingDomainName, userStoreDTO.getClassName(), userStoreDTO.getProperties(),
                        doc, userStoreElement, editSecondaryUserStore);
            }
            addProperty(UserStoreConfigConstants.DOMAIN_NAME, userStoreDTO.getDomainId(), doc, userStoreElement, false);
            addProperty(UserStoreConfigurationConstant.DESCRIPTION, userStoreDTO.getDescription(), doc,
                        userStoreElement, false);
        }
        return doc;
    }

    private static void updateStateOfUserStore(Path userStoreConfigFile, boolean isDisable, String domain,
                                               DocumentBuilder documentBuilder) throws SAXException, IOException,
            TransformerException {

        Document doc = documentBuilder.parse(Files.newInputStream(userStoreConfigFile));

        NodeList elements = doc.getElementsByTagName("Property");
        for (int i = 0; i < elements.getLength(); i++) {
            //Assumes a property element only have attribute 'name'
            if ("Disabled".compareToIgnoreCase(elements.item(i).getAttributes().item(0).getNodeValue()) == 0) {
                elements.item(i).setTextContent(String.valueOf(isDisable));
                break;
            }
        }
        StreamResult result = new StreamResult(Files.newOutputStream(userStoreConfigFile));
        DOMSource source = new DOMSource(doc);
        transformProperties().transform(source, result);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing the output stream from " + userStoreConfigFile);
        }
        result.getOutputStream().close();

        if (LOG.isDebugEnabled()) {
            LOG.debug("New state :" + isDisable + " of the user store \'" + domain + "\' successfully " +
                      "written to the file system");
        }
    }

    /**
     * Obtains the mandatory properties for a given userStoreClass
     *
     * @param userStoreClass userStoreClass name
     * @return Property[] of Mandatory Properties
     */
    private static Property[] getMandatoryProperties(String userStoreClass) {

        return UserStoreManagerRegistry.getUserStoreProperties(userStoreClass).getMandatoryProperties();
    }

    /**
     * Check whether the given property should be encrypted or not.
     *
     * @param mandatoryProperties mandatory property array
     * @param propertyName        property name
     * @return returns true if the property should be encrypted
     */
    private static boolean isPropertyToBeEncrypted(Property[] mandatoryProperties,
                                                   String propertyName) {

        for (Property property : mandatoryProperties) {
            if (propertyName.equalsIgnoreCase(property.getName())) {
                return property.getDescription().contains(UserStoreConfigurationConstant.ENCRYPT_TEXT);
            }
        }
        return false;
    }

    /**
     * Obtain the UniqueID ID constant value from the propertyDTO object which was set well
     * before sending the edit request.
     *
     * @param propertyDTOs PropertyDTO[] object passed from JSP page
     * @return unique id string value
     */
    private static String getUniqueIDFromUserDTO(PropertyDTO[] propertyDTOs) {

        int length = propertyDTOs.length;
        for (int i = length - 1; i >= 0; i--) {
            PropertyDTO propertyDTO = propertyDTOs[i];
            if (propertyDTO != null && propertyDTO.getName() != null && propertyDTO.getName()
                    .equalsIgnoreCase(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT)) {
                return propertyDTO.getValue();
            }
        }

        return null;
    }

    /**
     * Adds a property
     *
     * @param name:   Name of property
     * @param value:  Value
     * @param doc:    Document
     * @param parent: Parent element of the property to be added as a child
     */
    private static void addProperty(String name, String value, Document doc, Element parent, boolean encrypted) {
        Element property = doc.createElement("Property");
        Attr attr;
        if (encrypted) {
            attr = doc.createAttribute("encrypted");
            attr.setValue("true");
            property.setAttributeNode(attr);
        }

        attr = doc.createAttribute("name");
        attr.setValue(name);
        property.setAttributeNode(attr);

        property.setTextContent(value);
        parent.appendChild(property);
    }

    /**
     * Adds an array of properties
     *
     * @param propertyDTOs List of user store properties
     * @param doc          Document
     * @param parent       Parent element of the properties to be added
     */
    private static void addProperties(String userStoreDomain, String userStoreClass, PropertyDTO[] propertyDTOs,
                                      Document doc, Element parent, boolean editSecondaryUserStore)
            throws IdentityUserStoreMgtException {

        if (editSecondaryUserStore) {
            String uniqueID = getUniqueIDFromUserDTO(propertyDTOs);
            if (uniqueID == null) {
                throw new IdentityUserStoreMgtException("UniqueID property is not provided.");
            }
        }

        //First check for mandatory field with #encrypt
        Property[] mandatoryProperties = getMandatoryProperties(userStoreClass);

        Map<String, String> secondaryUserStoreProperties =
                getSecondaryUserStorePropertiesFromTenantUserRealm(userStoreDomain);

        for (PropertyDTO propertyDTO : propertyDTOs) {
            String propertyDTOName = propertyDTO.getName();
            if (UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT.equalsIgnoreCase(propertyDTOName)) {
                continue;
            }

            String propertyDTOValue = propertyDTO.getValue();
            if (propertyDTOValue != null) {
                boolean encrypted = false;
                if (isPropertyToBeEncrypted(mandatoryProperties, propertyDTOName)) {
                    propertyDTOValue = getPropertyValueIfMasked(secondaryUserStoreProperties, propertyDTOName,
                            propertyDTOValue);
                    try {
                        propertyDTOValue = SecondaryUserStoreConfigurationUtil.encryptPlainText(propertyDTOValue);
                        encrypted = true;
                    } catch (IdentityUserStoreMgtException e) {
                        LOG.error("addProperties failed to encrypt", e);
                        //its ok to continue from here
                    }
                }
                addProperty(propertyDTOName, propertyDTOValue, doc, parent, encrypted);
            }
        }
    }

    /**
     * If the property value is a mask value, this method will return the corresponding property value.
     *
     * @param secondaryUserStoreProperties Value {@link Map} of a the secondary user store, where actual value is
     *                                     pulled instead of the mask.
     * @param propertyDTOName   Name of the property.
     * @param propertyDTOValue Value of the property.
     * @return If property value is masked, returns the actual value to be stored instead of the mask. Otherwise the
     * same propertyDTOValue passed as the argument.
     */
    private static String getPropertyValueIfMasked(Map<String, String> secondaryUserStoreProperties,
                                                   String propertyDTOName, String propertyDTOValue) {

        if (ENCRYPTED_PROPERTY_MASK.equalsIgnoreCase(propertyDTOValue)) {
            propertyDTOValue = getExistingPropertyValue(secondaryUserStoreProperties, propertyDTOName);
        }
        return propertyDTOValue;
    }

    private static String getExistingPropertyValue(Map<String, String> secondaryUserStoreProperties,
                                                   String propertyDTOName) {

        String existingPropertyValue = null;
        if (secondaryUserStoreProperties != null) {
            existingPropertyValue = secondaryUserStoreProperties.get(propertyDTOName);
        }
        return existingPropertyValue;
    }

    private static UserStoreManager getSecondaryUserStoreManager(String userStoreDomain) throws UserStoreException {

        UserStoreManager secondaryUserStoreManager = null;
        UserRealm userRealm = (UserRealm) UserStoreConfigComponent.getRealmService().getTenantUserRealm(
                getTenantIdInTheCurrentContext());
        UserStoreManager userStoreManager = userRealm.getUserStoreManager();
        if (userStoreManager != null) {
            secondaryUserStoreManager = userStoreManager.getSecondaryUserStoreManager(userStoreDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not locate user store manager for user store domain: " + userStoreDomain);
            }
        }
        return secondaryUserStoreManager;
    }

    private static int getTenantIdInTheCurrentContext() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * This method is used to validate the domain name. Should not allow to have domain prefixed with 'FEDERATED', to
     * avoid conflicting with federated user domain.
     *
     * @param domain domain name
     * @throws UserStoreException throws if an error occured while validating the domain.
     */
    public static void validateForFederatedDomain(String domain) throws UserStoreException {

        if (IdentityUtil.isNotBlank(domain) && domain.toUpperCase().startsWith(UserStoreConfigurationConstant
                                                                                       .FEDERATED)) {
            throw new UserStoreException("'FEDERATED' is a reserved user domain prefix. "
                    + "Please start the domain name in a different manner.");
        }
    }

    /**
     * To get instance of {@link FileBasedUserStoreDAOFactory}
     *
     * @return an instance of {@link FileBasedUserStoreDAOFactory}
     * @throws UserStoreException throws when instantiating {@link FileBasedUserStoreDAOFactory}
     */
    public static UserStoreDAO getFileBasedUserStoreDAOFactory() throws UserStoreException {

        UserStoreDAO userStoreDAO = UserStoreConfigListenersHolder.getInstance().getUserStoreDAOFactories()
                .get(FileBasedUserStoreDAOFactory.class.getName()).getInstance();
        if (userStoreDAO == null) {
            throw new UserStoreException("Error occured while creating an instance of FileBasedUserStoreDAOFactory.");
        }
        return userStoreDAO;
    }

    /**
     * get random passwords generated.
     * @param secondaryRealmConfiguration realm configuration.
     * @param userStoreProperties user store properties.
     * @param encryptPropertyMaskValue The value used as the mask.
     * @param className class name
     * @return The array of {@link MaskedProperty} for the user store.
     */
    public static MaskedProperty[] setMaskInUserStoreProperties(RealmConfiguration secondaryRealmConfiguration,
                                                                Map<String, String> userStoreProperties,
                                                                String encryptPropertyMaskValue, String className) {

        MaskedProperty[] maskedProperties = getMaskedProperties(className, encryptPropertyMaskValue,
                secondaryRealmConfiguration);

        for (MaskedProperty maskedProperty : maskedProperties) {
            userStoreProperties.put(maskedProperty.getName(), maskedProperty.getMask());
        }

        return maskedProperties;
    }

    private static MaskedProperty[] getMaskedProperties(String userStoreClass, String maskValue,
                                                        RealmConfiguration secondaryRealmConfiguration) {
        //First check for mandatory field with #encrypt
        Property[] mandatoryProperties = getMandatoryProperties(userStoreClass);
        ArrayList<MaskedProperty> maskedProperties = new ArrayList<>();
        for (Property property : mandatoryProperties) {
            String propertyName = property.getName();
            if (property.getDescription().contains(UserStoreConfigurationConstant.ENCRYPT_TEXT)) {
                MaskedProperty maskedProperty = new MaskedProperty();
                maskedProperty.setName(propertyName);
                maskedProperty.setValue(secondaryRealmConfiguration.getUserStoreProperty(propertyName));
                maskedProperty.setMask(maskValue);
                maskedProperties.add(maskedProperty);
            }
        }
        return maskedProperties.toArray(new MaskedProperty[0]);
    }

    /**
     * To convert user store properties map to an array.
     * @param properties user store properties map
     * @return userstore properties array
     */
    public static PropertyDTO[] convertMapToArray(Map<String, String> properties) {

        Set<Map.Entry<String, String>> propertyEntries = properties.entrySet();
        ArrayList<PropertyDTO> propertiesList = new ArrayList<PropertyDTO>();
        String key;
        String value;
        for (Map.Entry<String, String> entry : propertyEntries) {
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            PropertyDTO propertyDTO = new PropertyDTO(key, value);
            propertiesList.add(propertyDTO);
        }
        return propertiesList.toArray(new PropertyDTO[propertiesList.size()]);
    }

    /**
     * Trigger the listeners before userstore name update
     * @param previousDomainName previous user store domain name
     * @param domainName current user store domain name
     * @throws UserStoreException throws when an error occured when triggering listeners.
     */
    public static void triggerListnersOnUserStorePreUpdate(String previousDomainName, String domainName)
            throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore pre update listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStoreNamePreUpdate(CarbonContext.getThreadLocalCarbonContext().getTenantId
                    (), previousDomainName, domainName);
        }
    }

    /**
     * Trigger the listeners after a userstore name update.
     *
     * @param previousDomainName Previous userstore domain name.
     * @param newDomainName      Current(new) userstore domain name.
     * @throws UserStoreException If an error occurred while invoking post listeners.
     */
    public static void triggerListenersOnUserStorePostUpdate(String previousDomainName, String newDomainName)
            throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering post userstore domain update listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStoreNamePostUpdate(CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                    previousDomainName, newDomainName);
        }
    }

    /**
     * Trigger the listeners before userstore domain delete
     *
     * @param domainName user store domain name
     * @throws UserStoreException throws when an error occured when triggering listeners.
     */
    public static void triggerListnersOnUserStorePreDelete(String domainName) throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore pre delete listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStorePreDelete(CarbonContext.getThreadLocalCarbonContext().getTenantId
                    (), domainName);
        }
    }

    /**
     * Trigger the listeners before a user store state is changed.
     *
     * @param domainName User store domain name.
     * @param isDisable True if disabled, else false.
     * @throws UserStoreException Thrown when an error occurred while triggering listeners.
     */
    public static void triggerListenersOnUserStorePreStateChange(String domainName, boolean isDisable)
            throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();

        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore pre state change listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStorePreStateChange(isDisable ? UserStoreState.DISABLED
                    : UserStoreState.ENABLED, tenantId, domainName);
        }
    }

    /**
     * Trigger the listeners before userstore is added.
     *
     * @param domainName user store domain name.
     * @throws UserStoreException throws when an error occurred when triggering listeners.
     */
    public static void triggerListenersOnUserStorePreAdd(String domainName) throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();

        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore pre add listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStorePreAdd(tenantId, domainName);
        }
    }

    /**
     * Trigger the listeners after a userstore is retrieved.
     *
     * @param userStoreDTO Retrieved userstore configuration.
     * @throws UserStoreException throws when an error occurred when triggering listeners.
     */
    public static void triggerListenersOnUserStorePostGet(UserStoreDTO userStoreDTO) throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();

        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore post get listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStorePostGet(tenantId, userStoreDTO);
        }
    }

    /**
     * Trigger the listeners after all userstores are retrieved.
     *
     * @param userStoreDTOS Array of userstore configurations.
     * @throws UserStoreException throws when an error occurred when triggering listeners.
     */
    public static void triggerListenersOnUserStoresPostGet(UserStoreDTO[] userStoreDTOS) throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();

        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstores post get listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStoresPostGet(tenantId, userStoreDTOS);
        }
    }

    /**
     * Trigger the listeners before a userstore is added.
     *
     * @param userStoreDTO Userstore configuration to be added.
     * @throws UserStoreException throws when an error occurred when triggering listeners.
     */
    public static void triggerListenersOnUserStorePreAdd(UserStoreDTO userStoreDTO) throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();

        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore pre add listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStorePreAdd(tenantId, userStoreDTO);
        }
    }

    /**
     * Trigger the listeners before a userstore is updated.
     *
     * @param userStoreDTO Userstore configuration to be updated.
     * @param isStateChange Boolean flag denoting whether the
     *                      update is a userstore state change.
     * @throws UserStoreException throws when an error occurred when triggering listeners.
     */
    public static void triggerListenersOnUserStorePreUpdate(UserStoreDTO userStoreDTO, boolean isStateChange)
            throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();

        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Triggering userstore pre update listener: %s for tenant: %s",
                        userStoreConfigListener.getClass().getName(),
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain()));
            }
            userStoreConfigListener.onUserStorePreUpdate(tenantId, userStoreDTO, isStateChange);
        }
    }

    /**
     * Checks whether having user stores based on separate repositories are supported.
     *
     * @return True if repository separation is enabled.
     */
    public static boolean isUserStoreRepositorySeparationEnabled() {

        // Support for user stores based on different repositories is
        // disabled as the feature has on going improvements.
        // https://github.com/wso2/product-is/issues/5673
        return false;
    }

    /**
     * Wrap UserStoreClientException in a IdentityUserStoreClientException.
     *
     * @param defaultMessage Default error message.
     * @param e UserStoreClientException.
     * @return new IdentityUserStoreClientException.
     */
    public static IdentityUserStoreClientException buildIdentityUserStoreClientException(String defaultMessage,
                                                                                         UserStoreClientException e) {

        String errorMessage = defaultMessage;
        if (e.getMessage() != null) {
            errorMessage = e.getMessage();
        }
        if (e.getErrorCode() != null) {
            return new IdentityUserStoreClientException(e.getErrorCode(), errorMessage, e);
        }
        return new IdentityUserStoreClientException(errorMessage, e);
    }
}
