/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.configuration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPassword;
import org.wso2.carbon.identity.user.store.configuration.beans.RandomPasswordContainer;
import org.wso2.carbon.identity.user.store.configuration.cache.RandomPasswordContainerCache;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceMetaInfo.WSDataSourceDefinition;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.validateForFederatedDomain;

public class UserStoreConfigAdminService extends AbstractAdmin {
    public static final Log log = LogFactory.getLog(UserStoreConfigAdminService.class);

    private static final String FILE_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory";
    private static final String DB_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory";

    /**
     * Get details of current secondary user store configurations
     *
     * @return : Details of all the configured secondary user stores
     * @throws IdentityUserStoreMgtException UserStoreException
     */
    public UserStoreDTO[] getSecondaryRealmConfigurations() throws IdentityUserStoreMgtException {

        List<UserStoreDTO> userStoreDTOList = new ArrayList<>();

        Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactories = UserStoreConfigListenersHolder.
                getInstance().getUserStoreDAOFactories();

        for (Map.Entry<String, AbstractUserStoreDAOFactory> entry : userStoreDAOFactories.entrySet()) {

            if (!SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.equals(entry.getKey(), DB_BASED_REPOSITORY_CLASS)) {
                continue;
            }

            UserStoreDTO[] allUserStores = entry.getValue().getInstance().getUserStores();
            userStoreDTOList.addAll(Arrays.asList(allUserStores));
        }
        return userStoreDTOList.toArray(new UserStoreDTO[0]);
    }

    /**
     * Get user stores from all the repositories.
     * @param repositoryClassName repository class name
     * @return userstore {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException
     */
    public UserStoreDTO[] getSecondaryRealmConfigurationsOnRepository(String repositoryClassName)
            throws IdentityUserStoreMgtException {

        if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
            Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactories = UserStoreConfigListenersHolder.
                    getInstance().getUserStoreDAOFactories();

            AbstractUserStoreDAOFactory userStoreDAOFactory = userStoreDAOFactories.get(repositoryClassName);
            if (userStoreDAOFactory != null) {
                return userStoreDAOFactory.getInstance().getUserStores();
            } else {
                return new UserStoreDTO[0];
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Repository separation of user-stores has been disabled. Returning empty " +
                        "UserStoreDTO array.");
            }
            return new UserStoreDTO[0];
        }
    }

    /**
     * Get available user store manager implementations
     *
     * @return: Available implementations for user store managers
     */
    public String[] getAvailableUserStoreClasses() throws IdentityUserStoreMgtException {
        Set<String> classNames = UserStoreManagerRegistry.getUserStoreManagerClasses();
        return classNames.toArray(new String[classNames.size()]);
    }

    /**
     * Get User Store Manager default properties for a given implementation
     *
     * @param className:Implementation class name for the user store
     * @return : list of default properties(mandatory+optional)
     */
    public Properties getUserStoreManagerProperties(String className) throws IdentityUserStoreMgtException {
        Properties properties = UserStoreManagerRegistry.getUserStoreProperties(className);

        if (properties != null && properties.getOptionalProperties() != null) {

            Property[] optionalProperties =  properties.getOptionalProperties();
            boolean foundUniqueIDProperty = false;
            for (Property property : optionalProperties) {
                if (UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT.equals(property.getName())) {
                    foundUniqueIDProperty = true;
                    break;
                }
            }
            if (!foundUniqueIDProperty) {
                if (log.isDebugEnabled()) {
                    log.debug("Inserting property : " + UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT +
                            " since " + UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT +
                            " property not defined as an optional property in " + className + " class");
                }
                List<Property> optionalPropertyList = new ArrayList<>(Arrays.asList(optionalProperties));
                Property uniqueIDProperty = new Property(
                        UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT, "", "", null);
                optionalPropertyList.add(uniqueIDProperty);

                properties.setOptionalProperties(
                        optionalPropertyList.toArray(new Property[optionalPropertyList.size()]));
            }
        }

        return properties;
    }

    /**
     * Add secondary user store to the system.
     *
     * @param userStoreDTO Represent the configuration of uer store {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException if an error occured while adding a userstore.
     */
    public void addUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotBlank(userStoreDTO.getRepositoryClass())) {
                AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                        getUserStoreDAOFactories().get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().addUserStore(userStoreDTO);
            } else {
                if (StringUtils.isNotBlank(userStoreDTO.getRepositoryClass())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Repository separation of user-stores has been disabled. Adding user-store " +
                                userStoreDTO.getDomainId() + " with file-based configuration.");
                    }
                }
                getFileBasedUserStoreDAOFactory().addUserStore(userStoreDTO);
            }
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
    }

    /**
     * Edit existing user store.
     *
     * @param userStoreDTO: Represent the configuration of user store {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException if an error occured while editing a userstore.
     */
    public void editUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {

                AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                        getUserStoreDAOFactories().get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().updateUserStore(userStoreDTO, false);
            } else if (StringUtils.equals(userStoreDTO.getRepositoryClass(), FILE_BASED_REPOSITORY_CLASS)) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository separation of user-stores has been disabled. Editing user-store " +
                            userStoreDTO.getDomainId() + " with file-based configuration.");
                }
                getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO, false);
            } else if (StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository separation of user-stores has been disabled. Unable to edit " +
                            "user-store " + userStoreDTO.getDomainId() + " with repository class " +
                            userStoreDTO.getRepositoryClass());
                }
            } else {
                getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO, false);
            }
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
    }


    /**
     * Edit currently existing user store with a change of its domain name
     *
     * @param userStoreDTO:      Represent the configuration of new user store
     * @param previousDomainName
     * @throws DataSourceException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public void editUserStoreWithDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException {

        boolean isDebugEnabled = log.isDebugEnabled();
        String domainName = userStoreDTO.getDomainId();
        if (isDebugEnabled) {
            log.debug("Changing user store " + previousDomainName + " to " + domainName);
        }
        try {
            validateForFederatedDomain(domainName);
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                        getUserStoreDAOFactories().get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().updateUserStoreDomainName(previousDomainName, userStoreDTO);
            } else if (StringUtils.equals(userStoreDTO.getRepositoryClass(), FILE_BASED_REPOSITORY_CLASS)) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository separation of user-stores has been disabled. Updating user-store " +
                            "domain name " + userStoreDTO.getDomainId() + " with file-based configuration.");
                }
                getFileBasedUserStoreDAOFactory().updateUserStoreDomainName(previousDomainName, userStoreDTO);
            } else if (StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository separation of user-stores has been disabled. Unable to update " +
                            "user-store domain name " + userStoreDTO.getDomainId() + " with repository class " +
                            userStoreDTO.getRepositoryClass());
                }
            } else {
                getFileBasedUserStoreDAOFactory().updateUserStoreDomainName(previousDomainName, userStoreDTO);
            }
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    /**
     * To get all the registered repository classes.
     *
     * @return repository classes
     */
    public String[] getRepositoryClasses() {

        if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
            Map<String, AbstractUserStoreDAOFactory> userStoreFactories = UserStoreConfigListenersHolder.getInstance().
                    getUserStoreDAOFactories();
            Object[] repositoryArr = userStoreFactories.keySet().toArray(new String[0]);
            String[] repositoryClasses = Arrays.copyOf(repositoryArr, repositoryArr.length, String[].class);
            return repositoryClasses;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Repository separation of user-stores has been disabled. Returning empty " +
                        "repository class array.");
            }
            return new String[0];
        }
    }

    /**
     * Deletes the user store specified from file
     *
     * @param domainName: domain name of the user stores to be deleted
     */
    public void deleteUserStore(String domainName) throws IdentityUserStoreMgtException {
        deleteUserStoresSet(new String[] {domainName});
    }

    /**
     * Deletes the use store specified from any repository.
     *
     * @param userStoreDTO an instance of {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException throws when an error occured while deleting the user store.
     */
    public void deleteUserStoreFromRepository(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        deleteUserStoresSetFromRepository(new UserStoreDTO[]{userStoreDTO});
    }
    /**
     * Delete the given list of user stores from file repository.
     *
     * @param domains: domain names of user stores to be deleted
     */
    public void deleteUserStoresSet(String[] domains) throws IdentityUserStoreMgtException {

        if (domains == null || domains.length <= 0) {
            throw new IdentityUserStoreMgtException("No selected user stores to delete");
        }

        if (!validateDomainsForDelete(domains)) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to delete user store : No privileges to delete own user store configurations ");
            }
            throw new IdentityUserStoreMgtException("No privileges to delete own user store configurations.");
        }
        try {
            getFileBasedUserStoreDAOFactory().deleteUserStores(domains);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error occured while deleting the user store.", e);
        }
    }

    /**
     * Delete the given list of user stores from any repository
     *
     * @param userStoreDTOs an array instance of {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException throws when an error occured while deleting the user store.
     */
    public void deleteUserStoresSetFromRepository(UserStoreDTO[] userStoreDTOs) throws IdentityUserStoreMgtException {

        if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
            for (String repositoryClass :
                    UserStoreConfigListenersHolder.getInstance().getUserStoreDAOFactories().keySet()) {
                List<String> domains = new ArrayList<>();
                for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                    if (repositoryClass.equals(userStoreDTO.getRepositoryClass())) {
                        domains.add(userStoreDTO.getDomainId());
                    }
                }
                if (CollectionUtils.isNotEmpty(domains)) {
                    AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                            getUserStoreDAOFactories().get(repositoryClass);
                    userStoreDAOFactory.getInstance().deleteUserStores(domains.toArray(new String[domains.size()]));
                }
            }
        } else {
            List<String> domainList = new ArrayList<>();
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (StringUtils.equals(userStoreDTO.getRepositoryClass(), FILE_BASED_REPOSITORY_CLASS)) {
                    domainList.add(userStoreDTO.getDomainId());
                }
            }
            String[] domains = domainList.toArray(new String[0]);
            if (log.isDebugEnabled()) {
                log.debug("Repository separation of user-stores has been disabled. Attempting to remove " +
                        "user-stores with file-based configurations. For user-stores " + String.join(",", domainList));
            }
            deleteUserStoresSet(domains);
        }
    }

    private boolean validateDomainsForDelete(String[] domains) {
        String userDomain = IdentityUtil.extractDomainFromName(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getUsername());
        for (String domain : domains) {
            if (domain.equalsIgnoreCase(userDomain)) {
                //Trying to delete own domain
                return false;
            }
        }
        return true;

    }

   /*
     * Update a domain to be disabled/enabled in file repository
     *
     * @param domain:   Name of the domain to be updated
     * @param isDisable : Whether to disable/enable domain(true/false)
     */
   public void changeUserStoreState(String domain, Boolean isDisable) throws IdentityUserStoreMgtException,
           TransformerConfigurationException {

       validateDomain(domain, isDisable);
       UserStoreDTO userStoreDTO = getUserStoreDTO(domain, isDisable, null);
       updateTheStateInFileRepository(userStoreDTO);
   }

    private void updateTheStateInFileRepository(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO, true);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    /**
     * Update the status of domain.
     * @param domain userstore domain
     * @param isDisable true if the userstore domain is disabled.
     * @param repositoryClass repository class
     * @throws IdentityUserStoreMgtException throws an error when changing the status of the user store.
     */
    public void modifyUserStoreState(String domain, Boolean isDisable, String repositoryClass)
            throws IdentityUserStoreMgtException {

        validateDomain(domain, isDisable);
        UserStoreDTO userStoreDTO;
        if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                StringUtils.isNotEmpty(repositoryClass)) {
            AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                    getUserStoreDAOFactories().get(repositoryClass);
            userStoreDTO = getUserStoreDTO(domain, isDisable, repositoryClass);
            userStoreDAOFactory.getInstance().updateUserStore(userStoreDTO, true);
        } else if (StringUtils.equals(repositoryClass, FILE_BASED_REPOSITORY_CLASS)) {
            if (log.isDebugEnabled()) {
                log.debug("Repository separation of user-stores has been disabled. Modifying state for " +
                        "user-store " + domain + " with file-based configuration.");
            }
            userStoreDTO = getUserStoreDTO(domain, isDisable, null);
            updateTheStateInFileRepository(userStoreDTO);
        } else if (StringUtils.isNotEmpty(repositoryClass)) {
            if (log.isDebugEnabled()) {
                log.debug("Repository separation of user-stores has been disabled. Unable to modify state " +
                        "for user-store " + domain + " with repository class " + repositoryClass);
            }
        } else {
            userStoreDTO = getUserStoreDTO(domain, isDisable, null);
            updateTheStateInFileRepository(userStoreDTO);
        }
    }

    private void validateDomain(String domain, Boolean isDisable) throws IdentityUserStoreMgtException {

        String currentAuthorizedUserName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        int index = currentAuthorizedUserName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        String currentUserDomain = null;
        if (index > 0) {
            currentUserDomain = currentAuthorizedUserName.substring(0, index);
        }

        if (currentUserDomain != null && currentUserDomain.equalsIgnoreCase(domain) && isDisable) {
            log.error("Error while disabling user store from a user who is in the same user store.");
            throw new IdentityUserStoreMgtException("Error while updating user store state.");
        }
    }

    private UserStoreDTO getUserStoreDTO(String domain, Boolean isDisable, String repositoryClass) {

        UserStoreDTO userStoreDTO = new UserStoreDTO();
        userStoreDTO.setDomainId(domain);
        userStoreDTO.setDisabled(isDisable);
        userStoreDTO.setRepositoryClass(repositoryClass);
        return userStoreDTO;
    }

    /**
     * Check the connection heath for JDBC userstores
     * @param domainName
     * @param driverName
     * @param connectionURL
     * @param username
     * @param connectionPassword
     * @param messageID
     * @return
     * @throws DataSourceException
     */
    public boolean testRDBMSConnection(String domainName, String driverName, String connectionURL, String username,
                                       String connectionPassword, String messageID) throws IdentityUserStoreMgtException {

        RandomPasswordContainer randomPasswordContainer;
        if (messageID != null) {
            randomPasswordContainer = getRandomPasswordContainer(messageID);
            if (randomPasswordContainer != null) {
                RandomPassword randomPassword = getRandomPassword(randomPasswordContainer, JDBCRealmConstants.PASSWORD);
                if (randomPassword != null) {
                    if (connectionPassword.equalsIgnoreCase(randomPassword.getRandomPhrase())) {
                        connectionPassword = randomPassword.getPassword();
                    }
                }
            }
        }

        WSDataSourceMetaInfo wSDataSourceMetaInfo = new WSDataSourceMetaInfo();

        RDBMSConfiguration rdbmsConfiguration = new RDBMSConfiguration();
        rdbmsConfiguration.setUrl(connectionURL);
        rdbmsConfiguration.setUsername(username);
        rdbmsConfiguration.setPassword(connectionPassword);
        rdbmsConfiguration.setDriverClassName(driverName);

        WSDataSourceDefinition wSDataSourceDefinition = new WSDataSourceDefinition();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(RDBMSConfiguration.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(rdbmsConfiguration, out);

        } catch (JAXBException e) {
            String errorMessage = "Error while checking RDBMS connection health";
            log.error(errorMessage, e);
            throw new IdentityUserStoreMgtException(errorMessage);
        }
        wSDataSourceDefinition.setDsXMLConfiguration(out.toString());
        wSDataSourceDefinition.setType("RDBMS");

        wSDataSourceMetaInfo.setName(domainName);
        wSDataSourceMetaInfo.setDefinition(wSDataSourceDefinition);
        try {
            return DataSourceManager.getInstance().getDataSourceRepository().testDataSourceConnection(wSDataSourceMetaInfo.
                    extractDataSourceMetaInfo());
        } catch (DataSourceException e) {
            String errorMessage = e.getMessage();
            // Does not print the error log since the log is already printed by DataSourceRepository
//            log.error(message, e);
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    /**
     * Get the RandomPasswordContainer object from the cache for given unique id
     *
     * @param uniqueID Get the unique id for that particular cache
     * @return RandomPasswordContainer of particular unique ID
     */
    private RandomPasswordContainer getRandomPasswordContainer(String uniqueID) {
        return RandomPasswordContainerCache.getInstance().getRandomPasswordContainerCache().get(uniqueID);
    }

    /**
     * Finds the RandomPassword object for a given propertyName in the RandomPasswordContainer
     * ( Which is unique per uniqueID )
     *
     * @param randomPasswordContainer RandomPasswordContainer object of an unique id
     * @param propertyName            RandomPassword object to be obtained for that property
     * @return Returns the RandomPassword object from the
     */
    private RandomPassword getRandomPassword(RandomPasswordContainer randomPasswordContainer,
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
}
