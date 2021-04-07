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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.beans.MaskedProperty;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStorePersistanceDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreClientException;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.XMLProcessorUtils;
import org.wso2.carbon.user.core.tenant.TenantCache;
import org.wso2.carbon.user.core.tenant.TenantIdKey;
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
import java.util.UUID;

import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.buildIdentityUserStoreClientException;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .convertMapToArray;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .setMaskInUserStoreProperties;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePreAdd;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .triggerListnersOnUserStorePreDelete;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .triggerListnersOnUserStorePreUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .validateForFederatedDomain;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .writeUserMgtXMLFile;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant
        .DEPLOYMENT_DIRECTORY;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant
        .ENCRYPTED_PROPERTY_MASK;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.FILE_EXTENSION_XML;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORES;

/**
 * This class contains the implementation of CRUD operations of the file based user Stores.
 */
public class FileBasedUserStoreDAOImpl extends AbstractUserStoreDAO {

    private static final Log log = LogFactory.getLog(FileBasedUserStoreDAOImpl.class);
    private static final String DISABLED = "Disabled";
    private static final String FILE_BASED = FileBasedUserStoreDAOFactory.class.getName();
    private XMLProcessorUtils xmlProcessorUtils = new XMLProcessorUtils();

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

    private void createUserStoreDirectory(String tenantFilePath, String fileName, boolean isTenant)
            throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Path userStore = Paths.get(DEPLOYMENT_DIRECTORY);
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
            String message = "Provided domain name: '" + domainName + "' is invalid.";
            log.error(message);
            throw new IdentityUserStoreClientException(message);
        }
    }

    private Path getUserStoreConfigurationFile(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        String domainName = userStoreDTO.getDomainId();
        return SecondaryUserStoreConfigurationUtil.getUserStoreConfigurationFile(domainName);
    }

    private void writeToUserStoreConfigurationFile(Path userStoreConfigFile, UserStoreDTO userStoreDTO,
                                                   boolean editSecondaryUserStore, boolean isStateChange,
                                                   String existingDomainName)
            throws IdentityUserStoreMgtException {

        try {
            writeUserMgtXMLFile(userStoreConfigFile, userStoreDTO, editSecondaryUserStore, isStateChange,
                    existingDomainName);
            if (log.isDebugEnabled()) {
                log.debug("New user store successfully written to the file" + userStoreConfigFile.toAbsolutePath());
            }
        } catch (IdentityUserStoreMgtException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    private IdentityUserStoreMgtException buildException(String domainName, boolean editSecondaryUserStore) {

        String msg = "Cannot add user store " + domainName + ". User store already exists.";
        String errorCode = UserStoreConfigurationConstant.ErrorMessage.ERROR_CODE_XML_FILE_ALREADY_EXISTS.getCode();
        if (editSecondaryUserStore) {
            msg = "Cannot edit user store " + domainName + ". User store cannot be edited.";
            errorCode = UserStoreConfigurationConstant.ErrorMessage.ERROR_CODE_XML_FILE_NOT_FOUND.getCode();
        }
        return new IdentityUserStoreClientException(errorCode, msg);
    }

    @Override
    public void deleteUserStore(String domain) throws IdentityUserStoreMgtException {

        if (isDomainNameExists(domain)) {
            deleteUserStores(new String[]{domain});
        }
    }

    @Override
    public void deleteUserStores(String[] domains) throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Path path;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            path = Paths.get(DEPLOYMENT_DIRECTORY);
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
            } catch (UserStoreClientException e) {
                throw buildIdentityUserStoreClientException("Userstore " + domainName + " cannot be deleted.", e);
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
    public UserStoreDTO[] getUserStores() throws IdentityUserStoreMgtException {

        RealmConfiguration secondaryRealmConfiguration;
        List<UserStoreDTO> domains = new ArrayList<>();
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
                String className = secondaryRealmConfiguration.getUserStoreClass();
                UserStoreDTO userStoreDTO = getUserStoreDTO(secondaryRealmConfiguration, userStoreProperties);
                userStoreProperties.put("Class", className);
                userStoreProperties.put(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT, uuid);
                MaskedProperty[] maskedProperties = setMaskInUserStoreProperties(secondaryRealmConfiguration,
                        userStoreProperties, ENCRYPTED_PROPERTY_MASK, className);
                userStoreDTO.setProperties(convertMapToArray(userStoreProperties));
                // Now revert back to original password.
                for (MaskedProperty maskedProperty : maskedProperties) {
                    userStoreProperties.put(maskedProperty.getName(), maskedProperty.getValue());
                }
                domains.add(userStoreDTO);
                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();
            } while (secondaryRealmConfiguration != null);
        }
        return domains.toArray(new UserStoreDTO[domains.size()]);
    }

    @Override
    protected void doAddUserStore(UserStorePersistanceDTO userStorePersistanceDTO) throws
            IdentityUserStoreMgtException {

        String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
        try {
            // Run pre user-store add listeners.
            triggerListenersOnUserStorePreAdd(domainName);
            boolean validDomain = isValidDomainToAdd(domainName);
            validateForFederatedDomain(domainName);
            if (validDomain) {
                Path userStoreConfigFile = getUserStoreConfigurationFile(userStorePersistanceDTO.getUserStoreDTO());
                if (Files.exists(userStoreConfigFile)) {
                    throw buildException(userStorePersistanceDTO.getUserStoreDTO().getDomainId(), false);
                }
                writeToUserStoreConfigurationFile(userStoreConfigFile, userStorePersistanceDTO.getUserStoreDTO(),
                        false, false, domainName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The user store domain: " + domainName + "is not a valid domain name.");
                }
            }
        } catch (UserStoreException e) {
            throw new IdentityUserStoreClientException("Error occurred while adding the user store with the domain: " +
                    domainName, e);
        }
    }

    @Override
    protected void doUpdateUserStore(UserStorePersistanceDTO userStorePersistanceDTO, boolean isStateChange)
            throws IdentityUserStoreMgtException {

        boolean isValidDomain;
        String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
        try {
            validateForFederatedDomain(domainName);
            isValidDomain = isDomainNameExists(domainName);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreClientException("Error while updating the user store.", e);
        }
        if (isValidDomain) {
            Path userStoreConfigFile = getUserStoreConfigurationFile(userStorePersistanceDTO.getUserStoreDTO());
            if (!Files.exists(userStoreConfigFile)) {
                throw buildException(userStorePersistanceDTO.getUserStoreDTO().getDomainId(), true);
            }
            writeToUserStoreConfigurationFile(userStoreConfigFile, userStorePersistanceDTO.getUserStoreDTO(),
                    true, isStateChange, domainName);
        } else {
            String errorMessage = "Trying to edit an invalid domain : " + domainName;
            throw new IdentityUserStoreClientException(errorMessage);
        }
    }

    @Override
    protected void doUpdateUserStoreDomainName(String previousDomainName, UserStorePersistanceDTO
            userStorePersistanceDTO) throws IdentityUserStoreMgtException {

        Path userStoreConfigFile;
        Path previousUserStoreConfigFile;
        String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
        String fileName = domainName.replace(".", "_");
        String previousFileName = previousDomainName.replace(".", "_");
        validateFileName(domainName, fileName);
        validateFileName(previousDomainName, previousFileName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            createUserStoreDirectory(null, fileName, false);
            userStoreConfigFile = Paths.get(DEPLOYMENT_DIRECTORY, fileName + FILE_EXTENSION_XML);
            previousUserStoreConfigFile = Paths.get(DEPLOYMENT_DIRECTORY, previousFileName + FILE_EXTENSION_XML);
        } else {
            String tenantFilePath = CarbonUtils.getCarbonTenantsDirPath();
            createUserStoreDirectory(tenantFilePath, fileName, true);
            userStoreConfigFile = Paths.get(tenantFilePath, String.valueOf(tenantId), USERSTORES,
                    fileName + FILE_EXTENSION_XML);
            previousUserStoreConfigFile = Paths.get(tenantFilePath, String.valueOf(tenantId), USERSTORES,
                    previousFileName + FILE_EXTENSION_XML);
        }
        if (!Files.exists(previousUserStoreConfigFile)) {
            String errorMessage = "Cannot update user store domain name. Previous domain name " + previousDomainName +
                    " does not exists.";
            throw new IdentityUserStoreClientException(UserStoreConfigurationConstant.ErrorMessage
                    .ERROR_CODE_XML_FILE_NOT_FOUND.getCode(),
                    errorMessage);
        }
        if (Files.exists(userStoreConfigFile)) {
            String errorMessage = "Cannot update user store domain name. An user store already exists with new domain" +
                                  " " + domainName + ".";
            throw new IdentityUserStoreClientException(UserStoreConfigurationConstant.ErrorMessage.
                    ERROR_CODE_XML_FILE_ALREADY_EXISTS.getCode(), errorMessage);
        }
        try {
            // Run pre user-store name update listeners
            triggerListnersOnUserStorePreUpdate(previousDomainName, domainName);
            // Update persisted domain name
            updatePersistedDomainName(previousDomainName, domainName, tenantId);

        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + domainName + " cannot be updated.", e);
        } catch (UserStoreException e) {
            String errorMessage = "Error while updating user store domain : " + domainName;
            log.error(errorMessage, e);
            throw new IdentityUserStoreMgtException(errorMessage);
        }
        try {
            Files.delete(previousUserStoreConfigFile);
            writeToUserStoreConfigurationFile(userStoreConfigFile, userStorePersistanceDTO.getUserStoreDTO(),
                    true, false, previousDomainName);
        } catch (IOException e) {
            log.info("Error when deleting previous configuration files " + previousUserStoreConfigFile);
        }
    }

    @Override
    protected UserStorePersistanceDTO doGetUserStore(String domain) throws IdentityUserStoreMgtException {

        return null;
    }

    @Override
    protected UserStorePersistanceDTO[] doGetAllUserStores() throws IdentityUserStoreMgtException {

        RealmConfiguration secondaryRealmConfiguration;
        List<UserStorePersistanceDTO> userStorePersistanceDAOList = new ArrayList<>();
        UserStorePersistanceDTO userStorePersistanceDTO = new UserStorePersistanceDTO();
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
            return new UserStorePersistanceDTO[0];
        } else {
            do {
                Map<String, String> userStoreProperties = secondaryRealmConfiguration.getUserStoreProperties();

                String uuid = userStoreProperties.get(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                }
                String className = secondaryRealmConfiguration.getUserStoreClass();
                UserStoreDTO userStoreDTO = getUserStoreDTO(secondaryRealmConfiguration, userStoreProperties);
                userStoreProperties.put("Class", className);
                userStoreProperties.put(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT, uuid);
                MaskedProperty[] maskedProperties = setMaskInUserStoreProperties(secondaryRealmConfiguration,
                        userStoreProperties, ENCRYPTED_PROPERTY_MASK, className);
                userStoreDTO.setProperties(convertMapToArray(userStoreProperties));

                // Now revert back to original password.
                for (MaskedProperty maskedProperty : maskedProperties) {
                    userStoreProperties.put(maskedProperty.getName(), maskedProperty.getValue());
                }
                userStorePersistanceDTO.setUserStoreDTO(userStoreDTO);
                userStorePersistanceDAOList.add(userStorePersistanceDTO);
                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();

            } while (secondaryRealmConfiguration != null);
        }
        return userStorePersistanceDAOList.toArray(new UserStorePersistanceDTO[userStorePersistanceDAOList.size()]);
    }

    private UserStoreDTO getUserStoreDTO(RealmConfiguration secondaryRealmConfiguration, Map<String, String>
            userStoreProperties) {

        UserStoreDTO userStoreDTO = new UserStoreDTO();
        userStoreDTO.setClassName(secondaryRealmConfiguration.getUserStoreClass());
        userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigurationConstant
                                                                                             .DESCRIPTION));
        userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants
                                                                                          .DOMAIN_NAME));
        userStoreDTO.setRepositoryClass(FILE_BASED);
        if (userStoreProperties.get(DISABLED) != null) {
            userStoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get(DISABLED)));
        }
        return userStoreDTO;
    }

    /**
     * To check whether the provided domain name is valid to add user store.
     *
     * @param domainName user store domain name
     * @return true or false
     * @throws IdentityUserStoreMgtException
     */
    private boolean isValidDomainToAdd(String domainName) throws IdentityUserStoreMgtException {

        if (StringUtils.isEmpty(domainName)) {
            throw new IdentityUserStoreClientException(" User store domain name should not be empty.");
        }

        if (domainName.contains("_")) {
            throw new IdentityUserStoreClientException(" User store domain name should not contain \"_\".");
        }

        if (getDomainNames().contains(domainName)) {
            // if add, user store domain name shouldn't already exists
            throw new IdentityUserStoreClientException(UserStoreConfigurationConstant.ErrorMessage.
                    ERROR_CODE_USER_STORE_DOMAIN_ALREADY_EXISTS.getCode(),
                    " Cannot add user store. Domain name: " + domainName + " already exists.");
        }
        return true;
    }

    /**
     * Get a List of existing domain names.
     *
     * @return : list of domain names
     * @throws IdentityUserStoreMgtException
     */
    private List<String> getDomainNames() throws IdentityUserStoreMgtException {

        List<String> domains = new ArrayList<String>();

        RealmConfiguration realmConfiguration = null;
        try {
            realmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration();
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException(" Error occurred while retrieving the realm configuration ", e);
        }

        // To add PRIMARY domain to the domains list
        String domain = realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (domain == null) {
            domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        domains.add(domain);

        RealmConfiguration secondaryRealmConfiguration = realmConfiguration.getSecondaryRealmConfig();
        while (secondaryRealmConfiguration != null) {
            domains.add(secondaryRealmConfiguration.getUserStoreProperty(UserCoreConstants.
                    RealmConfig.PROPERTY_DOMAIN_NAME));
            secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();
        }
        return domains;
    }

    /**
     * To check whether the provided domain name already exists to update or delete.
     *
     * @param domainName user store domain name
     * @return true or false
     * @throws IdentityUserStoreMgtException
     */
    private boolean isDomainNameExists(String domainName) throws IdentityUserStoreMgtException {

        if (StringUtils.isEmpty(domainName)) {
            throw new IdentityUserStoreClientException(
                    UserStoreConfigurationConstant.ErrorMessage.ERROR_CODE_EMPTY_USERSTORE_DOMAIN_NAME.getCode(),
                    UserStoreConfigurationConstant.ErrorMessage.ERROR_CODE_EMPTY_USERSTORE_DOMAIN_NAME.getMessage());
        }

        if (!getDomainNames().contains(domainName)) {
            // if edit or delete, user store domain name should already exists
            throw new IdentityUserStoreClientException(UserStoreConfigurationConstant.ErrorMessage.
                    ERROR_CODE_USER_STORE_DOMAIN_NOT_FOUND.getCode(), " Cannot find the domain name " +
                    domainName + " to perform this operation");
        }
        return true;
    }
}
