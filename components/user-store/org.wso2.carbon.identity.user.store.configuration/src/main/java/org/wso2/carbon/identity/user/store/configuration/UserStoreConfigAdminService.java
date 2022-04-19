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
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreClientException;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;

import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .getFileBasedUserStoreDAOFactory;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePreStateChange;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil
        .validateForFederatedDomain;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.H2_INIT_REGEX;

/**
 * User store config admin service.
 */
public class UserStoreConfigAdminService extends AbstractAdmin {

    public static final Log LOG = LogFactory.getLog(UserStoreConfigAdminService.class);

    private static final String FILE_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory";

    private static Pattern h2InitPattern = Pattern.compile(H2_INIT_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Get details of current secondary user store configurations
     *
     * @return : Details of all the configured secondary user stores
     * @throws IdentityUserStoreMgtException UserStoreException
     */
    public UserStoreDTO[] getSecondaryRealmConfigurations() throws IdentityUserStoreMgtException {

        return UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().getUserStores();
    }

    /**
     * Get user stores from all the repositories.
     *
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Repository separation of user-stores has been disabled. Returning empty " +
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

        Set<String> classNames = UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().
                getAvailableUserStoreClasses();
        return classNames.toArray(new String[0]);
    }

    /**
     * Get User Store Manager default properties for a given implementation
     *
     * @param className Implementation class name for the user store
     * @return list of default properties(mandatory+optional)
     */
    public Properties getUserStoreManagerProperties(String className) throws IdentityUserStoreMgtException {
        Properties properties = UserStoreManagerRegistry.getUserStoreProperties(className);

        if (properties != null && properties.getOptionalProperties() != null) {

            Property[] optionalProperties = properties.getOptionalProperties();
            boolean foundUniqueIDProperty = false;
            for (Property property : optionalProperties) {
                if (UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT.equals(property.getName())) {
                    foundUniqueIDProperty = true;
                    break;
                }
            }
            if (!foundUniqueIDProperty) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Inserting property : " + UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT +
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

            UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().addUserStore(userStoreDTO);
        } catch (IdentityUserStoreClientException e) {
            throw buildIdentityUserStoreMgtException(e, "Error while adding the userstore.");
        }
    }

    /**
     * Edit existing user store.
     *
     * @param userStoreDTO Represent the configuration of user store {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException if an error occurred while editing a userstore.
     */
    public void editUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().updateUserStore(userStoreDTO,
                    false);
        } catch (IdentityUserStoreClientException e) {
            throw buildIdentityUserStoreMgtException(e, "Error while updating the userstore.");
        }
    }


    /**
     * Edit currently existing user store with a change of its domain name
     *
     * @param userStoreDTO Represent the configuration of new user store
     * @param previousDomainName previous name of the user store
     * @throws IdentityUserStoreMgtException if an error occurred while editing a userstore.
     */
    public void editUserStoreWithDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException {

        boolean isDebugEnabled = LOG.isDebugEnabled();
        String domainName = userStoreDTO.getDomainId();
        if (isDebugEnabled) {
            LOG.debug("Changing user store " + previousDomainName + " to " + domainName);
        }
        try {
            validateForFederatedDomain(domainName);
            UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().
                    updateUserStoreByDomainName(previousDomainName, userStoreDTO);
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        } catch (IdentityUserStoreClientException e) {
            throw buildIdentityUserStoreMgtException(e, "Error while updating the userstore.");
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
            return Arrays.copyOf(repositoryArr, repositoryArr.length, String[].class);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Repository separation of user-stores has been disabled. Returning empty " +
                          "repository class array.");
            }
            return new String[0];
        }
    }

    /**
     * Deletes the user store specified from file
     *
     * @param domainName domain name of the user stores to be deleted
     */
    public void deleteUserStore(String domainName) throws IdentityUserStoreMgtException {

        try {
            UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().deleteUserStore(domainName);
        } catch (IdentityUserStoreClientException e) {
            throw buildIdentityUserStoreMgtException(e, "Error while deleting the userstore.");
        }
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
     * @param domains domain names of user stores to be deleted
     */
    public void deleteUserStoresSet(String[] domains) throws IdentityUserStoreMgtException {

        try {
            UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().deleteUserStoreSet(domains);
        } catch (IdentityUserStoreClientException e) {
            throw buildIdentityUserStoreMgtException(e, "Error while deleting the userstore list.");
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
                    try {
                        userStoreDAOFactory.getInstance().deleteUserStores(domains.toArray(new String[0]));
                    } catch (IdentityUserStoreClientException e) {
                        throw buildIdentityUserStoreMgtException(e, "Error while deleting the userstore list.");
                    }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Repository separation of user-stores has been disabled. Attempting to remove " +
                          "user-stores with file-based configurations. For user-stores " + String.join(",",
                                                                                                       domainList));
            }
            deleteUserStoresSet(domains);
        }
    }

    /**
     * Update a domain to be disabled/enabled in file repository.
     *
     * @param domain Name of the domain to be updated
     * @param isDisable Whether to disable/enable domain(true/false)
     * @throws IdentityUserStoreMgtException If error occurs during domain validation
     * @throws TransformerConfigurationException If error occurs during configuration transformation
     */
    public void changeUserStoreState(String domain, Boolean isDisable) throws IdentityUserStoreMgtException,
            TransformerConfigurationException {

        validateDomain(domain, isDisable);

        try {
            triggerListenersOnUserStorePreStateChange(domain, isDisable);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error occurred while triggering the user store pre state change" +
                    " listeners.");
        }

        UserStoreDTO userStoreDTO = getUserStoreDTO(domain, isDisable, null);
        updateStateInFileRepository(userStoreDTO);
    }

    private void updateStateInFileRepository(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO, true);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    /**
     * Update the status of domain.
     *
     * @param domain          userstore domain
     * @param isDisable       true if the userstore domain is disabled.
     * @param repositoryClass repository class
     * @throws IdentityUserStoreMgtException throws an error when changing the status of the user store.
     */
    public void modifyUserStoreState(String domain, Boolean isDisable, String repositoryClass)
            throws IdentityUserStoreMgtException {

        validateDomain(domain, isDisable);
        try {
            triggerListenersOnUserStorePreStateChange(domain, isDisable);
            UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService()
                    .modifyUserStoreState(domain, isDisable,
                            repositoryClass);
        } catch (IdentityUserStoreClientException e) {
            throw buildIdentityUserStoreMgtException(e, "Error while modifying the userstore state.");
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error occurred while triggering the user store pre state change" +
                    " listeners.");
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
            LOG.error("Error while disabling user store from a user who is in the same user store.");
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
     *
     * @param domainName
     * @param driverName
     * @param connectionURL
     * @param username
     * @param connectionPassword
     * @param messageID
     * @return
     * @throws IdentityUserStoreMgtException
     */
    public boolean testRDBMSConnection(String domainName, String driverName, String connectionURL, String username,
                                       String connectionPassword, String messageID) throws
            IdentityUserStoreMgtException {
        if (StringUtils.isNotEmpty(connectionURL)) {
            String validationConnectionString = connectionURL.toLowerCase().replace("\\", "");
            Matcher matcher = h2InitPattern.matcher(validationConnectionString);
            if (matcher.find()) {
                String errorMessage = "INIT expressions are not allowed in the connection URL due to security reasons.";
                LOG.error(errorMessage);
                throw new IdentityUserStoreMgtException(errorMessage);
            }
        }
        return UserStoreConfigListenersHolder.getInstance().getUserStoreConfigService().testRDBMSConnection(domainName,
                driverName, connectionURL, username, connectionPassword, messageID);
    }

    private IdentityUserStoreMgtException buildIdentityUserStoreMgtException(IdentityUserStoreClientException e,
                                                                             String defaultMessage) {

        String errorMessage = defaultMessage;
        if (e.getMessage() != null) {
            errorMessage = e.getMessage();
        }
        return new IdentityUserStoreMgtException(errorMessage, e);
    }
}
