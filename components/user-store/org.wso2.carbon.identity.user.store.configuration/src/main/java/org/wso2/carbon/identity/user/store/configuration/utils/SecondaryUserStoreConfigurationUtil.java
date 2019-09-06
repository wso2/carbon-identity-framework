/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPassword;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPasswordContainer;
import org.wso2.carbon.identity.user.store.configuration.cache.RandomPasswordContainerCache;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigComponent;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.FILE_EXTENSION_XML;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORES;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.deploymentDirectory;

/**
 * Util class responsible for processing encryption and decryption over secondary user store
 */

public class SecondaryUserStoreConfigurationUtil {

    public static final Log log = LogFactory.getLog(SecondaryUserStoreConfigurationUtil.class);
    private static final String SERVER_KEYSTORE_FILE = "Security.KeyStore.Location";
    private static final String SERVER_KEYSTORE_TYPE = "Security.KeyStore.Type";
    private static final String SERVER_KEYSTORE_PASSWORD = "Security.KeyStore.Password";
    private static final String SERVER_KEYSTORE_KEY_ALIAS = "Security.KeyStore.KeyAlias";
    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static final String SERVER_INTERNAL_KEYSTORE_FILE = "Security.InternalKeyStore.Location";
    private static final String SERVER_INTERNAL_KEYSTORE_TYPE = "Security.InternalKeyStore.Type";
    private static final String SERVER_INTERNAL_KEYSTORE_PASSWORD = "Security.InternalKeyStore.Password";
    private static final String SERVER_INTERNAL_KEYSTORE_KEY_ALIAS = "Security.InternalKeyStore.KeyAlias";
    private static final String ENCRYPTION_KEYSTORE = "Security.UserStorePasswordEncryption";
    private static final String INTERNAL_KEYSTORE = "InternalKeystore";
    private static Cipher cipher = null;
    private static String cipherTransformation = null;
    private static Certificate certificate = null;

    private SecondaryUserStoreConfigurationUtil() {

    }

    /**
     * Initializes the key store and assign it to Cipher object.
     *
     * @throws IdentityUserStoreMgtException Cipher object creation failed
     */
    private static void initializeKeyStore() throws IdentityUserStoreMgtException {

        if (cipher == null) {
            ServerConfigurationService config =
                    UserStoreConfigComponent.getServerConfigurationService();

            if (config != null) {
                String encryptionKeyStore = config.getFirstProperty(ENCRYPTION_KEYSTORE);

                String filePath = config.getFirstProperty(SERVER_KEYSTORE_FILE);
                String keyStoreType = config.getFirstProperty(SERVER_KEYSTORE_TYPE);
                String password = config.getFirstProperty(SERVER_KEYSTORE_PASSWORD);
                String keyAlias = config.getFirstProperty(SERVER_KEYSTORE_KEY_ALIAS);

                //use internal keystore
                if (INTERNAL_KEYSTORE.equalsIgnoreCase(encryptionKeyStore)) {
                    filePath = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_FILE);
                    keyStoreType = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_TYPE);
                    password = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_PASSWORD);
                    keyAlias = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_KEY_ALIAS);
                }

                KeyStore store;
                InputStream inputStream = null;

                try {
                    inputStream = new FileInputStream(new File(filePath).getAbsolutePath());
                    store = KeyStore.getInstance(keyStoreType);
                    store.load(inputStream, password.toCharArray());
                    Certificate[] certs = store.getCertificateChain(keyAlias);
                    if(System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY) != null) {
                        cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
                        certificate = certs[0];
                        cipher = Cipher.getInstance(cipherTransformation, "BC");
                    } else {
                        cipher = Cipher.getInstance("RSA", "BC");
                    }
                    cipher.init(Cipher.ENCRYPT_MODE, certs[0].getPublicKey());
                } catch (FileNotFoundException e) {
                    String errorMsg = "Keystore File Not Found in configured location";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (IOException e) {
                    String errorMsg = "Keystore File IO operation failed";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (InvalidKeyException e) {
                    String errorMsg = "Invalid key is used to access keystore";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (KeyStoreException e) {
                    String errorMsg = "Faulty keystore";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (GeneralSecurityException e) {
                    String errorMsg = "Some parameters assigned to access the " +
                            "keystore is invalid";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            log.error("Exception occurred while trying to close the keystore " +
                                    "file", e);
                        }
                    }
                }
            } else {
                String errMsg = "ServerConfigurationService is null - this situation can't occur";
                log.error(errMsg);
            }

        }
    }

    /**
     * @param plainText Cipher text to be encrypted
     * @return Returns the encrypted text
     * @throws IdentityUserStoreMgtException Encryption failed
     */
    public static String encryptPlainText(String plainText) throws IdentityUserStoreMgtException {

        if (cipher == null) {
            initializeKeyStore();
        }

        try {
            byte[] encryptedKey = cipher.doFinal((plainText.getBytes()));
            if (cipherTransformation != null) {
                // If cipher transformation is configured via carbon.properties
                encryptedKey = CryptoUtil.getDefaultCryptoUtil()
                        .createSelfContainedCiphertext(encryptedKey, cipherTransformation, certificate);
            }
            return Base64.encode(encryptedKey);
        } catch (GeneralSecurityException e) {
            String errMsg = "Failed to generate the cipher text";
            throw new IdentityUserStoreMgtException(errMsg, e);
        }
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
            userStore = Paths.get(deploymentDirectory);
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
                log.info("folder 'userstores' created to store configurations for tenant = " + tenantId);
            } catch (IOException e) {
                log.error("Error while creating 'userstores' directory to store configurations for tenant = "
                        + tenantId);
            }
        }
        return Paths.get(userStore.toString(), fileName + FILE_EXTENSION_XML);
    }

    /**
     * This method is used to write userStore xml file.
     * @param userStoreConfigFile path of the userStore configuration file
     * @param userStoreDTO instance of {@link UserStoreDTO}
     * @param editSecondaryUserStore true if it is update operation
     * @throws IdentityUserStoreMgtException throws if an error occured while writing to the xml file.
     */
    public static void writeUserMgtXMLFile(Path userStoreConfigFile, UserStoreDTO userStoreDTO,
                                           boolean editSecondaryUserStore, boolean isStateChange)
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
                updateUserStoreProperties(userStoreConfigFile, userStoreDTO, editSecondaryUserStore, documentBuilder);
            }
        } catch (ParserConfigurationException e) {
            String errMsg = " Error occurred due to serious parser configuration exception of " + userStoreConfigFile;
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (TransformerException e) {
            String errMsg = " Error occurred during the transformation process of " + userStoreConfigFile;
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (IOException e) {
            String errMsg = " Error occurred during the creating output stream from " + userStoreConfigFile;
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (SAXException e) {
            throw new IdentityUserStoreMgtException("Error while updating user store state", e);
        }
    }

    /**
     * Get the user store config file.
     * @param userStoreDTO an instance of {@link UserStoreDTO}
     * @return user store properties as a String.
     * @throws IdentityUserStoreMgtException throws if an error occured while getting the user store properties.
     */
    public static String getUserStoreProperties(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        String userStoreProperties;
        DocumentBuilderFactory documentFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        try {
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document doc = getDocument(userStoreDTO, false, documentBuilder);
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
                                                  boolean editSecondaryUserStore, DocumentBuilder documentBuilder)
            throws IdentityUserStoreMgtException, IOException, TransformerException {

        Document doc = getDocument(userStoreDTO, editSecondaryUserStore, documentBuilder);
        StreamResult result = new StreamResult(Files.newOutputStream(userStoreConfigFile));
        DOMSource source = new DOMSource(doc);
        transformProperties().transform(source, result);

    }

    private static Document getDocument(UserStoreDTO userStoreDTO, boolean editSecondaryUserStore,
                                        DocumentBuilder documentBuilder) throws IdentityUserStoreMgtException {

        Document doc = documentBuilder.newDocument();

        //create UserStoreManager element
        Element userStoreElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER);
        doc.appendChild(userStoreElement);

        Attr attrClass = doc.createAttribute("class");
        if (userStoreDTO != null) {
            attrClass.setValue(userStoreDTO.getClassName());
            userStoreElement.setAttributeNode(attrClass);
            if (userStoreDTO.getClassName() != null) {
                addProperties(userStoreDTO.getClassName(), userStoreDTO.getProperties(), doc, userStoreElement,
                        editSecondaryUserStore);
            }
            addProperty(UserStoreConfigConstants.DOMAIN_NAME, userStoreDTO.getDomainId(), doc, userStoreElement, false);
            addProperty(UserStoreConfigurationConstant.DESCRIPTION, userStoreDTO.getDescription(), doc, userStoreElement, false);
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

        if (log.isDebugEnabled()) {
            log.debug("New state :" + isDisable + " of the user store \'" + domain + "\' successfully " +
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
     * Get the RandomPasswordContainer object from the cache for given unique id
     *
     * @param uniqueID Get and Remove the unique id for that particualr cache
     * @return RandomPasswordContainer of particular unique ID
     */
    private static RandomPasswordContainer getAndRemoveRandomPasswordContainer(String uniqueID) {

        return RandomPasswordContainerCache.getInstance().getRandomPasswordContainerCache().getAndRemove(uniqueID);
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
     * Find the RandomPassword object for a given propertyName in the RandomPasswordContainer
     * ( Which is unique per uniqueID )
     *
     * @param randomPasswordContainer RandomPasswordContainer object of an unique id
     * @param propertyName            RandomPassword object to be obtained for that property
     * @return Returns the RandomPassword object from the
     */
    private static RandomPassword getRandomPassword(RandomPasswordContainer randomPasswordContainer,
                                             String propertyName) {

        RandomPassword[] randomPasswords = randomPasswordContainer.getRandomPasswords();

        if (randomPasswords != null) {
            for (RandomPassword randomPassword : randomPasswords) {
                if (randomPassword.getPropertyName().equalsIgnoreCase(propertyName)) {
                    return randomPassword;
                }
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
    private static void addProperties(String userStoreClass, PropertyDTO[] propertyDTOs, Document doc, Element parent,
                               boolean editSecondaryUserStore) throws IdentityUserStoreMgtException {

        RandomPasswordContainer randomPasswordContainer = null;
        if (editSecondaryUserStore) {
            String uniqueID = getUniqueIDFromUserDTO(propertyDTOs);
            if (uniqueID == null) {
                throw new IdentityUserStoreMgtException("UniqueID property is not provided.");
            }
            randomPasswordContainer = getAndRemoveRandomPasswordContainer(uniqueID);
            if (randomPasswordContainer == null) {
                String errorMsg = "randomPasswordContainer is null for uniqueID therefore " +
                        "proceeding without encryption=" + uniqueID;
                log.error(errorMsg); //need this error log to further identify the reason for throwing this exception
                throw new IdentityUserStoreMgtException("Longer delay causes the edit operation be to " +
                        "abandoned");
            }
        }
        //First check for mandatory field with #encrypt
        Property[] mandatoryProperties = getMandatoryProperties(userStoreClass);
        for (PropertyDTO propertyDTO : propertyDTOs) {
            String propertyDTOName = propertyDTO.getName();
            if (UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT.equalsIgnoreCase(propertyDTOName)) {
                continue;
            }

            String propertyDTOValue = propertyDTO.getValue();
            if (propertyDTOValue != null) {
                boolean encrypted = false;
                if (isPropertyToBeEncrypted(mandatoryProperties, propertyDTOName)) {
                    if (randomPasswordContainer != null) {
                        RandomPassword randomPassword = getRandomPassword(randomPasswordContainer, propertyDTOName);
                        if (randomPassword != null) {
                            if (propertyDTOValue.equalsIgnoreCase(randomPassword.getRandomPhrase())) {
                                propertyDTOValue = randomPassword.getPassword();
                            }
                        }
                    }

                    try {
                        propertyDTOValue = SecondaryUserStoreConfigurationUtil.encryptPlainText(propertyDTOValue);
                        encrypted = true;
                    } catch (IdentityUserStoreMgtException e) {
                        log.error("addProperties failed to encrypt", e);
                        //its ok to continue from here
                    }
                }
                addProperty(propertyDTOName, propertyDTOValue, doc, parent, encrypted);
            }
        }
    }

    /**
     * This method is used to validate the domain name. Should not allow to have domain prefixed with 'FEDERATED', to
     * avoid conflicting with federated user domain.
     *
     * @param domain domain name
     * @throws UserStoreException throws if an error occured while validating the domain.
     */
    public static void validateForFederatedDomain(String domain) throws UserStoreException {

        if (IdentityUtil.isNotBlank(domain) && domain.toUpperCase().startsWith(UserStoreConfigurationConstant.FEDERATED)) {
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
     * @param uuid random id.
     * @param randomPhrase random phrase
     * @param className class name
     * @return random password generated.
     */
    public static RandomPassword[] getRandomPasswords(RealmConfiguration secondaryRealmConfiguration,
                                                Map<String, String> userStoreProperties, String uuid,
                                                String randomPhrase, String className) {

        RandomPassword[] randomPasswords = getRandomPasswordProperties(className, randomPhrase,
                secondaryRealmConfiguration);
        if (randomPasswords != null) {
            updatePasswordContainer(randomPasswords, uuid);
        }

        // Replace the property with random password.
        for (RandomPassword randomPassword : randomPasswords) {
            userStoreProperties.put(randomPassword.getPropertyName(), randomPassword.getRandomPhrase());
        }
        return randomPasswords;
    }

    private static RandomPassword[] getRandomPasswordProperties(String userStoreClass,
                                                                String randomPhrase, RealmConfiguration secondaryRealmConfiguration) {
        //First check for mandatory field with #encrypt
        Property[] mandatoryProperties = getMandatoryProperties(userStoreClass);
        ArrayList<RandomPassword> randomPasswordArrayList = new ArrayList<RandomPassword>();
        for (Property property : mandatoryProperties) {
            String propertyName = property.getName();
            if (property.getDescription().contains(UserStoreConfigurationConstant.ENCRYPT_TEXT)) {
                RandomPassword randomPassword = new RandomPassword();
                randomPassword.setPropertyName(propertyName);
                randomPassword.setPassword(secondaryRealmConfiguration.getUserStoreProperty(propertyName));
                randomPassword.setRandomPhrase(randomPhrase);
                randomPasswordArrayList.add(randomPassword);
            }
        }
        return randomPasswordArrayList.toArray(new RandomPassword[randomPasswordArrayList.size()]);
    }


    private static void updatePasswordContainer(RandomPassword[] randomPasswords, String uuid) {

        if (randomPasswords != null) {
            if (log.isDebugEnabled()) {
                log.debug("updatePasswordContainer reached for number of random password properties length = " +
                        randomPasswords.length);
            }
            RandomPasswordContainer randomPasswordContainer = new RandomPasswordContainer();
            randomPasswordContainer.setRandomPasswords(randomPasswords);
            randomPasswordContainer.setUniqueID(uuid);

            RandomPasswordContainerCache.getInstance().getRandomPasswordContainerCache().put(uuid,
                    randomPasswordContainer);
        }
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
    public static void triggerListnersOnUserStorePreUpdate(String previousDomainName, String domainName) throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            userStoreConfigListener.onUserStoreNamePreUpdate(CarbonContext.getThreadLocalCarbonContext().getTenantId
                    (), previousDomainName, domainName);
        }
    }

    /**
     * Trigger the listeners before userstore domain delete
     * @param domainName user store domain name
     * @throws UserStoreException throws when an error occured when triggering listeners.
     */
    public static void triggerListnersOnUserStorePreDelete(String domainName) throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            userStoreConfigListener.onUserStorePreDelete(CarbonContext.getThreadLocalCarbonContext().getTenantId
                    (), domainName);
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
}
