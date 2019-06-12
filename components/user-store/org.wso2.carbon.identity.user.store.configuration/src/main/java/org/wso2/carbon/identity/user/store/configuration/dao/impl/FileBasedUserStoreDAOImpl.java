/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.dao.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPassword;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPasswordContainer;
import org.wso2.carbon.identity.user.store.configuration.cache.RandomPasswordContainerCache;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.tenant.TenantCache;
import org.wso2.carbon.user.core.tenant.TenantIdKey;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.writeUserMgtXMLFile;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORES;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.deploymentDirectory;

public class FileBasedUserStoreDAOImpl implements UserStoreDAO {
    private static final Log log = LogFactory.getLog(FileBasedUserStoreDAOImpl.class);
    public static final String DISABLED = "Disabled";
    public static final String DESCRIPTION = "Description";
    private static final String FILE_BASED = FileBasedUserStoreDAOImpl.class.getName();

    @Override
    public void addUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        Path userStoreConfigFile = getUserStoreConfigurationFile(userStoreDTO);
        if (Files.exists(userStoreConfigFile)) {
            throwException(userStoreDTO.getDomainId(), false);
        }
        writeToUserStoreConfigurationFile(userStoreConfigFile, userStoreDTO, false, false);
    }

    @Override
    public void updateUserStore(UserStoreDTO userStoreDTO, boolean isStateChange) throws IdentityUserStoreMgtException {

        Path userStoreConfigFile = getUserStoreConfigurationFile(userStoreDTO);
        if (!Files.exists(userStoreConfigFile)) {
            throwException(userStoreDTO.getDomainId(), true);
        }
        writeToUserStoreConfigurationFile(userStoreConfigFile, userStoreDTO, true, isStateChange);
    }

    @Override
    public void updateUserStoreDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException {

        Path userStoreConfigFile;
        Path previousUserStoreConfigFile;
        String domainName = userStoreDTO.getDomainId();
        String fileName = domainName.replace(".", "_");
        String previousFileName = previousDomainName.replace(".", "_");
        validateFileName(domainName, fileName);
        validateFileName(previousDomainName, previousFileName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            createUserStoreDirectory(null, fileName, false);
            userStoreConfigFile = Paths.get(deploymentDirectory, fileName + ".xml");
            previousUserStoreConfigFile = Paths.get(deploymentDirectory, previousFileName + ".xml");
        } else {
            String tenantFilePath = CarbonUtils.getCarbonTenantsDirPath();
            createUserStoreDirectory(tenantFilePath, fileName, true);
            userStoreConfigFile = Paths.get(tenantFilePath, String.valueOf(tenantId), fileName + ".xml");
            previousUserStoreConfigFile = Paths.get(tenantFilePath, String.valueOf(tenantId), previousFileName + ".xml");
        }

        if (!Files.exists(previousUserStoreConfigFile)) {
            String errorMessage = "Cannot update user store domain name. Previous domain name " + previousDomainName +
                    " does not exists.";
            throw new IdentityUserStoreMgtException(errorMessage);
        }

        if (Files.exists(userStoreConfigFile)) {
            String errorMessage = "Cannot update user store domain name. An user store already exists with new domain " +
                    domainName + ".";
            throw new IdentityUserStoreMgtException(errorMessage);
        }

        try {
            // Run pre user-store name update listeners
            triggerListnersOnUserStorePreUpdate(previousDomainName, domainName);

            // Update persisted domain name
            updatePersistedDomainName(previousDomainName, domainName, tenantId);

        } catch (UserStoreException e) {
            String errorMessage = "Error while updating user store domain : " + domainName;
            log.error(errorMessage, e);
            throw new IdentityUserStoreMgtException(errorMessage);
        }

        try {
            Files.delete(previousUserStoreConfigFile);
            writeToUserStoreConfigurationFile(userStoreConfigFile, userStoreDTO, true, false);
        } catch (IOException e) {
            log.info("Error when deleting previous configuration files " + previousUserStoreConfigFile);
        }
    }

    private void updatePersistedDomainName(String previousDomainName, String domainName, int tenantId)
            throws UserStoreException {

        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.
                getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
        userStoreManager.updatePersistedDomain(previousDomainName, domainName);
        if (log.isDebugEnabled()) {
            log.debug("Renamed persisted domain name from" + previousDomainName + " to " + domainName +
                    " of tenant:" + tenantId + " from UM_DOMAIN.");
        }
    }

    private void triggerListnersOnUserStorePreUpdate(String previousDomainName, String domainName) throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            userStoreConfigListener.onUserStoreNamePreUpdate(CarbonContext.getThreadLocalCarbonContext().getTenantId
                    (), previousDomainName, domainName);
        }
    }

    private void triggerListnersOnUserStorePreDelete(String domainName) throws UserStoreException {

        List<UserStoreConfigListener> userStoreConfigListeners = UserStoreConfigListenersHolder.getInstance()
                .getUserStoreConfigListeners();
        for (UserStoreConfigListener userStoreConfigListener : userStoreConfigListeners) {
            userStoreConfigListener.onUserStorePreDelete(CarbonContext.getThreadLocalCarbonContext().getTenantId
                    (), domainName);
        }
    }

    private void createUserStoreDirectory(String tenantFilePath, String fileName, boolean isTenant)
            throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Path userStore = Paths.get(deploymentDirectory);
        if (isTenant) {
            userStore = Paths.get(tenantFilePath, String.valueOf(tenantId), USERSTORES);
        }
        if (!Files.exists(userStore)) {
            try {
                Files.createDirectory(userStore);
                if (isTenant) {
                    log.info("folder 'userstores' created for tenant: " + tenantId + "for the file: " + fileName);
                } else {
                    log.info("folder 'userstores' created for super tenant for the file: " + fileName);
                }
            } catch (IOException e) {
                log.error("Error at creating 'userstores' directory to store configurations for super tenant");
                throw new IdentityUserStoreMgtException("Error while updating the userStore.");
            }
        }
    }

    private void validateFileName(String domainName, String fileName) throws IdentityUserStoreMgtException {

        if (!IdentityUtil.isValidFileName(fileName)) {
            String message = "Provided domain name : '" + domainName + "' is invalid.";
            log.error(message);
            throw new IdentityUserStoreMgtException(message);
        }
    }

    private Path getUserStoreConfigurationFile(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        String domainName = userStoreDTO.getDomainId();
        return SecondaryUserStoreConfigurationUtil.getUserStoreConfigurationFile(domainName);
    }

    private void writeToUserStoreConfigurationFile(Path userStoreConfigFile, UserStoreDTO userStoreDTO,
                                                   boolean editSecondaryUserStore , boolean isStateChange) throws IdentityUserStoreMgtException {

        try {
            writeUserMgtXMLFile(userStoreConfigFile, userStoreDTO, editSecondaryUserStore, isStateChange);
            if (log.isDebugEnabled()) {
                log.debug("New user store successfully written to the file" + userStoreConfigFile.toAbsolutePath());
            }
        } catch (IdentityUserStoreMgtException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    private void throwException(String domainName, boolean editSecondaryUserStore) throws IdentityUserStoreMgtException {

        String msg = "Cannot add user store " + domainName + ". User store already exists.";
        if (editSecondaryUserStore) {
            msg = "Cannot edit user store " + domainName + ". User store cannot be edited.";
        }
        log.error(msg);
        throw new IdentityUserStoreMgtException(msg);
    }

    @Override
    public void deleteUserStore(String domain) {

    }

    @Override
    public void deleteUserStores(String[] domains) throws IdentityUserStoreMgtException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Path path;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            path = Paths.get(deploymentDirectory);
        } else {
            path = Paths.get(CarbonUtils.getCarbonTenantsDirPath(), String.valueOf(tenantId), USERSTORES);

        }
        File file = path.toFile();
        for (String domainName : domains) {
            if (log.isDebugEnabled()) {
                log.debug("Deleting, .... " + domainName + " domain.");
            }

            try {
                // Run pre user-store name update listeners
                triggerListnersOnUserStorePreDelete(domainName);
                // Delete persisted domain name
                deletePersitedDomain(tenantId, domainName);

            } catch (UserStoreException e) {
                String errorMessage = "Error while deleting user store : " + domainName;
                log.error(errorMessage, e);
                throw new IdentityUserStoreMgtException(errorMessage);
            }

            // Delete file
            deleteFile(file, domainName.replace(".", "_").concat(".xml"));
        }

    }

    private void deletePersitedDomain(int tenantId, String domainName) throws UserStoreException {

        // Delete persisted domain name
        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.
                getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();

        userStoreManager.deletePersistedDomain(domainName);
        if (log.isDebugEnabled()) {
            log.debug("Removed persisted domain name: " + domainName + " of tenant:" + tenantId + " from " +
                    "UM_DOMAIN.");
        }
        //clear cache to make the modification effective
        UserCoreUtil.getRealmService().clearCachedUserRealm(tenantId);
        TenantCache.getInstance().clearCacheEntry(new TenantIdKey(tenantId));
    }

    private void deleteFile(File file, final String userStoreName) throws IdentityUserStoreMgtException {

        validateFileName(userStoreName, userStoreName);

        File[] deleteCandidates = file.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(userStoreName);
            }
        });

        if (ArrayUtils.isNotEmpty(deleteCandidates)) {
            for (File file1 : deleteCandidates) {
                if (file1.delete()) {
                    log.info("File " + file.getName() + " deleted successfully");
                } else {
                    log.error("error at deleting file:" + file.getName());
                }
            }
        }

    }

    @Override
    public UserStoreDTO getUserStore(String domain) {

        return null;
    }

    @Override
    public UserStoreDTO[] getUserStores(String[] domain) {

        return new UserStoreDTO[0];
    }

    @Override
    public UserStoreDTO[] getAllUserStores() throws IdentityUserStoreMgtException {

        RealmConfiguration secondaryRealmConfiguration;
        ArrayList<UserStoreDTO> domains = new ArrayList<>();
        try {
            secondaryRealmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getRealmConfiguration().getSecondaryRealmConfig();
        } catch (UserStoreException e) {
            String errorMessage = "Error while retrieving user store configurations";
            throw new IdentityUserStoreMgtException(errorMessage);
        }
        if (secondaryRealmConfiguration == null) {
            if (log.isDebugEnabled()) {
                log.debug("SecondaryRealmConfiguration is null. Can not find any userStore.");
            }
            return new UserStoreDTO[0];
        } else {

            do {
                Map<String, String> userStoreProperties = secondaryRealmConfiguration.getUserStoreProperties();


                String uuid = userStoreProperties.get(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                }

                String randomPhrase = UserStoreConfigurationConstant.RANDOM_PHRASE_PREFIX + uuid;
                String className = secondaryRealmConfiguration.getUserStoreClass();
                UserStoreDTO userStoreDTO = getUserStoreDTO(secondaryRealmConfiguration, userStoreProperties);
                userStoreProperties.put("Class", className);
                userStoreProperties.put(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT, uuid);
                RandomPassword[] randomPasswords = getRandomPasswords(secondaryRealmConfiguration, userStoreProperties,
                        uuid, randomPhrase, className);

                userStoreDTO.setProperties(convertMapToArray(userStoreProperties));

                // Now revert back to original password.
                for (RandomPassword randomPassword : randomPasswords) {
                    userStoreProperties.put(randomPassword.getPropertyName(), randomPassword.getPassword());
                }

                domains.add(userStoreDTO);
                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();

            } while (secondaryRealmConfiguration != null);
        }
        return domains.toArray(new UserStoreDTO[domains.size()]);
    }

    private RandomPassword[] getRandomPasswords(RealmConfiguration secondaryRealmConfiguration,
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

    private UserStoreDTO getUserStoreDTO(RealmConfiguration secondaryRealmConfiguration, Map<String, String> userStoreProperties) {

        UserStoreDTO userStoreDTO = new UserStoreDTO();
        userStoreDTO.setClassName(secondaryRealmConfiguration.getUserStoreClass());
        userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(DESCRIPTION));
        userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME));
        userStoreDTO.setRepository(FILE_BASED);
        if (userStoreProperties.get(DISABLED) != null) {
            userStoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get(DISABLED)));
        }
        return userStoreDTO;
    }

    /**
     * Generate the RandomPassword[] from secondaryRealmConfiguration for given userStoreClass
     *
     * @param userStoreClass              Extract the mandatory properties of this class
     * @param randomPhrase                The randomly generated keyword which will be stored in
     *                                    RandomPassword object
     * @param secondaryRealmConfiguration RealmConfiguration object consists the properties
     * @return RandomPassword[] array for each property
     */
    private RandomPassword[] getRandomPasswordProperties(String userStoreClass,
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

    /**
     * Obtains the mandatory properties for a given userStoreClass
     *
     * @param userStoreClass userStoreClass name
     * @return Property[] of Mandatory Properties
     */
    private Property[] getMandatoryProperties(String userStoreClass) {
        return UserStoreManagerRegistry.getUserStoreProperties(userStoreClass).getMandatoryProperties();
    }

    /**
     * Create and update the RandomPasswordContainer with given unique ID and randomPasswords array
     *
     * @param randomPasswords array contains the elements to be encrypted with thier random
     *                        password phrase, password and unique id
     * @param uuid            Unique id of the RandomPasswordContainer
     */
    private void updatePasswordContainer(RandomPassword[] randomPasswords, String uuid) {

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
     * Get user store properties of a given active user store manager as an array
     *
     * @param properties: properties of the user store
     * @return key#value
     */
    private PropertyDTO[] convertMapToArray(Map<String, String> properties) {
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
}
