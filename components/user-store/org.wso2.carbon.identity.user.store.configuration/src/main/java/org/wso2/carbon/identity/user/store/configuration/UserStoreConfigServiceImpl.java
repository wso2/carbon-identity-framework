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
package org.wso2.carbon.identity.user.store.configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.model.UserStoreAttributeMappings;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreClientException;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.user.api.UserStoreClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.buildIdentityUserStoreClientException;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePostGet;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePreAdd;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePreUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStoresPostGet;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.H2_INIT_REGEX;

/**
 * Implementation class for UserStoreConfigService.
 */
public class UserStoreConfigServiceImpl implements UserStoreConfigService {

    private static final Log LOG = LogFactory.getLog(UserStoreConfigServiceImpl.class);
    private static final String FILE_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory";
    private static final String DB_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory";
    private static Pattern h2InitPattern = Pattern.compile(H2_INIT_REGEX, Pattern.CASE_INSENSITIVE);

    @Override
    public void addUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        loadTenant();
        try {
            triggerListenersOnUserStorePreAdd(userStoreDTO);
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotBlank(userStoreDTO.getRepositoryClass())) {
                AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.
                        getInstance().getUserStoreDAOFactories().get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().addUserStore(userStoreDTO);
            } else {
                if (StringUtils.isNotBlank(userStoreDTO.getRepositoryClass())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Repository separation of user-stores has been disabled. Adding user-store " +
                                  userStoreDTO.getDomainId() + " with file-based configuration.");
                    }
                }
                validateConnectionUrl(userStoreDTO);
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().addUserStore(userStoreDTO);
            }
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + userStoreDTO.getDomainId()
                    + " cannot be added.", e);
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
    }

    @Override
    public void updateUserStore(UserStoreDTO userStoreDTO, boolean isStateChange) throws IdentityUserStoreMgtException {

        loadTenant();
        try {
            triggerListenersOnUserStorePreUpdate(userStoreDTO, isStateChange);
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {

                AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                        getUserStoreDAOFactories().get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().updateUserStore(userStoreDTO, false);
            } else if (StringUtils.equals(userStoreDTO.getRepositoryClass(), FILE_BASED_REPOSITORY_CLASS)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Repository separation of user-stores has been disabled. Editing user-store " +
                              userStoreDTO.getDomainId() + " with file-based configuration.");
                }
                validateConnectionUrl(userStoreDTO);
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO,
                        false);
            } else if (StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Repository separation of user-stores has been disabled. Unable to edit " +
                              "user-store " + userStoreDTO.getDomainId() + " with repository class " +
                              userStoreDTO.getRepositoryClass());
                }
            } else {
                validateConnectionUrl(userStoreDTO);
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO,
                        false);
            }
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + userStoreDTO.getDomainId()
                    + " cannot be updated.", e);
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
    }

    @Override
    public void updateUserStoreByDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException {

        loadTenant();
        try {
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                AbstractUserStoreDAOFactory userStoreDAOFactory = UserStoreConfigListenersHolder.getInstance().
                        getUserStoreDAOFactories().get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().updateUserStoreDomainName(previousDomainName, userStoreDTO);
            } else if (StringUtils.equals(userStoreDTO.getRepositoryClass(), FILE_BASED_REPOSITORY_CLASS)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Repository separation of user-stores has been disabled. Updating user-store " +
                              "domain name " + userStoreDTO.getDomainId() + " with file-based configuration.");
                }
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStoreDomainName
                        (previousDomainName, userStoreDTO);
            } else if (StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Repository separation of user-stores has been disabled. Unable to update " +
                              "user-store domain name " + userStoreDTO.getDomainId() + " with repository class " +
                              userStoreDTO.getRepositoryClass());
                }
            } else {
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().
                        updateUserStoreDomainName(previousDomainName, userStoreDTO);
            }
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    @Override
    public void deleteUserStore(String domain) throws IdentityUserStoreMgtException {

        if (StringUtils.isEmpty(domain)) {
            throw new IdentityUserStoreClientException("No selected user store to delete.");
        }

        if (!validateDomainsForDelete(new String[]{domain})) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to delete user store " + domain + " " +
                          ": No privileges to delete own user store configurations ");
            }
            throw new IdentityUserStoreClientException("No privileges to delete own user store configurations.");
        }
        try {
            Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactories = UserStoreConfigListenersHolder.
                    getInstance().getUserStoreDAOFactories();
            for (Map.Entry<String, AbstractUserStoreDAOFactory> entry : userStoreDAOFactories.entrySet()) {

                if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                        StringUtils.equals(entry.getKey(), DB_BASED_REPOSITORY_CLASS)) {
                    entry.getValue().getInstance().deleteUserStore(domain);
                } else {
                    SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().deleteUserStore(domain);
                }
            }
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error occurred while deleting the user store.", e);
        }
    }

    @Override
    public void deleteUserStoreSet(String[] domains) throws IdentityUserStoreMgtException {

        if (domains == null || domains.length <= 0) {
            throw new IdentityUserStoreMgtException("No selected user stores to delete");
        }

        if (!validateDomainsForDelete(domains)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to delete user store : No privileges to delete own user store configurations ");
            }
            throw new IdentityUserStoreClientException("No privileges to delete own user store configurations.");
        }
        try {
            SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().deleteUserStores(domains);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error occurred while deleting the user store.", e);
        }
    }

    @Override
    public UserStoreDTO getUserStore(String domain) throws IdentityUserStoreMgtException {

        UserStoreDTO[] userStoreDTOS = new UserStoreDTO[0];
        Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactories = UserStoreConfigListenersHolder.
                getInstance().getUserStoreDAOFactories();
        for (Map.Entry<String, AbstractUserStoreDAOFactory> entry : userStoreDAOFactories.entrySet()) {

            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.equals(entry.getKey(), DB_BASED_REPOSITORY_CLASS)) {
                return entry.getValue().getInstance().getUserStore(domain);
            }
            try {
                userStoreDTOS = SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().getUserStores();
            } catch (UserStoreException e) {
                throw new IdentityUserStoreMgtException("Error occurred while retrieving the user stores from file" +
                        " based system.", e);
            }
        }
        if (userStoreDTOS != null) {
            for (UserStoreDTO userStoreDTO : userStoreDTOS) {
                if (userStoreDTO.getDomainId().equals(domain)) {
                    // Trigger post get listeners.
                    try {
                        triggerListenersOnUserStorePostGet(userStoreDTO);
                    } catch (UserStoreClientException e) {
                        throw buildIdentityUserStoreClientException("Userstore " + domain + " cannot be retrieved.", e);
                    } catch (UserStoreException e) {
                        throw new IdentityUserStoreMgtException("Error occurred while triggering userstore post get " +
                                "listener. " + e.getMessage(), e);
                    }
                    return userStoreDTO;
                }
            }
        }
        return null;
    }

    @Override
    public UserStoreDTO[] getUserStores() throws IdentityUserStoreMgtException {

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
        UserStoreDTO[] userStoreDTOS = userStoreDTOList.toArray(new UserStoreDTO[0]);

        // Trigger post get listeners.
        try {
            triggerListenersOnUserStoresPostGet(userStoreDTOS);
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstores cannot be retrieved.", e);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error occurred while triggering userstores post get listener.", e);
        }

        return userStoreDTOS;
    }

    @Override
    public Set<String> getAvailableUserStoreClasses() throws IdentityUserStoreMgtException {

        return getAllowedUserstoreClasses(UserStoreManagerRegistry.getUserStoreManagerClasses());
    }

    private Set<String> getAllowedUserstoreClasses(Set<String> userstores) {

        Set<String> allowedUserstores = UserStoreConfigListenersHolder.getInstance().getAllowedUserstores();
        // Preserving the old behavior, if the 'AllowedUserstores' config is not set.
        if (allowedUserstores == null) {
            return userstores;
        }
        userstores.retainAll(allowedUserstores);
        return userstores;
    }

    @Override
    public boolean testRDBMSConnection(String domainName, String driverName, String connectionURL, String username,
                                       String connectionPassword, String messageID)
            throws IdentityUserStoreMgtException {

        if (StringUtils.isNotEmpty(messageID) && StringUtils.isNotEmpty(domainName)) {
            if (connectionPassword.equalsIgnoreCase(UserStoreConfigurationConstant.ENCRYPTED_PROPERTY_MASK)) {
                Map<String, String> secondaryUserStoreProperties = SecondaryUserStoreConfigurationUtil
                        .getSecondaryUserStorePropertiesFromTenantUserRealm(domainName);
                if (secondaryUserStoreProperties != null) {
                    connectionPassword = secondaryUserStoreProperties.get(JDBCRealmConstants.PASSWORD);
                }
            }
        }

        WSDataSourceMetaInfo wSDataSourceMetaInfo = new WSDataSourceMetaInfo();

        RDBMSConfiguration rdbmsConfiguration = new RDBMSConfiguration();
        rdbmsConfiguration.setUrl(connectionURL);
        rdbmsConfiguration.setUsername(username);
        rdbmsConfiguration.setPassword(connectionPassword);
        rdbmsConfiguration.setDriverClassName(driverName);

        WSDataSourceMetaInfo.WSDataSourceDefinition wSDataSourceDefinition = new
                WSDataSourceMetaInfo.WSDataSourceDefinition();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(RDBMSConfiguration.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(rdbmsConfiguration, out);

        } catch (JAXBException e) {
            String errorMessage = "Error while checking RDBMS connection health";
            LOG.error(errorMessage, e);
            throw new IdentityUserStoreMgtException(errorMessage);
        }
        wSDataSourceDefinition.setDsXMLConfiguration(out.toString());
        wSDataSourceDefinition.setType("RDBMS");
        if (StringUtils.isNotEmpty(domainName)) {
            wSDataSourceMetaInfo.setName(domainName);
        }
        wSDataSourceMetaInfo.setDefinition(wSDataSourceDefinition);
        try {
            return DataSourceManager.getInstance().getDataSourceRepository().testDataSourceConnection
                    (wSDataSourceMetaInfo.extractDataSourceMetaInfo());
        } catch (DataSourceException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    @Override
    public void modifyUserStoreState(String domain, Boolean isDisable, String repositoryClass)
            throws IdentityUserStoreMgtException {

        loadTenant();
        UserStoreDTO userStoreDTO;
        if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                StringUtils.isNotEmpty(repositoryClass)) {
            Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactories = UserStoreConfigListenersHolder.
                    getInstance().getUserStoreDAOFactories();
            AbstractUserStoreDAOFactory userStoreDAOFactory = userStoreDAOFactories.get(repositoryClass);
            userStoreDTO = getUserStoreDTO(domain, isDisable, repositoryClass);
            userStoreDAOFactory.getInstance().updateUserStore(userStoreDTO, true);
        } else if (StringUtils.equals(repositoryClass, FILE_BASED_REPOSITORY_CLASS)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Repository separation of user-stores has been disabled. Modifying state for " +
                          "user-store " + domain + " with file-based configuration.");
            }
            userStoreDTO = getUserStoreDTO(domain, isDisable, null);
            updateStateInFileRepository(userStoreDTO);
        } else if (StringUtils.isNotEmpty(repositoryClass)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Repository separation of user-stores has been disabled. Unable to modify state " +
                          "for user-store " + domain + " with repository class " + repositoryClass);
            }
        } else {
            userStoreDTO = getUserStoreDTO(domain, isDisable, null);
            updateStateInFileRepository(userStoreDTO);
        }
    }

    /**
     * To check the provided domain set are exists to delete.
     *
     * @param domains domain name array.
     * @return true or false.
     */
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

    /**
     * To update the state in file repository.
     *
     * @param userStoreDTO {@link UserStoreDTO}
     * @throws IdentityUserStoreMgtException
     */
    private void updateStateInFileRepository(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO, true);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage);
        }
    }

    /**
     * To construct UserStoreDTO object to get user store details.
     *
     * @param domain          the domain name
     * @param isDisable       the boolean value to specify whether user store should enable or disable
     * @param repositoryClass the repository class
     * @return UserStoreDTO
     */
    private UserStoreDTO getUserStoreDTO(String domain, Boolean isDisable, String repositoryClass) {

        UserStoreDTO userStoreDTO = new UserStoreDTO();
        userStoreDTO.setDomainId(domain);
        userStoreDTO.setDisabled(isDisable);
        userStoreDTO.setRepositoryClass(repositoryClass);
        return userStoreDTO;
    }

    /**
     * Checks whether the tenant is loaded if not loaded the tenant.
     *
     * Note: This is required only if tenant configurations
     * need to be redeployed.
     */
    private void loadTenant() throws IdentityUserStoreMgtException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return;
        }

        Set<String> loadedTenants = TenantAxisUtils.getTenantConfigurationContexts(UserStoreConfigListenersHolder
                .getInstance().getConfigurationContextService().getServerConfigContext()).keySet();
        if (!loadedTenants.contains(tenantDomain)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Tenant: " + tenantDomain + " is not loaded. Therefore attempting to load the tenant.");
            }
            try {
                TenantAxisUtils.getTenantConfigurationContext(tenantDomain, UserStoreConfigListenersHolder
                        .getInstance().getConfigurationContextService().getServerConfigContext());
            } catch (Exception e) {
                throw new IdentityUserStoreMgtException(e.getMessage(), e);
            }
        }
    }

    @Override
    public UserStoreAttributeMappings getUserStoreAttributeMappings() throws IdentityUserStoreMgtException {

        return UserStoreConfigListenersHolder.getInstance().getUserStoreAttributeMappings();
    }

    /**
     * Validate the userstore connection URL. Currently the init param is checked.
     *
     * @param userStoreDTO contains the userstore details.
     * @throws IdentityUserStoreMgtException throws when the URL is invalid.
     */
    private void validateConnectionUrl(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        PropertyDTO[] propertyDTO = userStoreDTO.getProperties();
        for (PropertyDTO propertyDTOValue : propertyDTO) {
            if (propertyDTOValue != null && "url".equals(propertyDTOValue.getName())) {
                String connectionURL = propertyDTOValue.getValue();
                if (StringUtils.isNotEmpty(connectionURL)) {
                    String validationConnectionString = connectionURL.toLowerCase().replace("\\", "");
                    Matcher matcher = h2InitPattern.matcher(validationConnectionString);
                    if (matcher.find()) {
                        throw new IdentityUserStoreMgtException("INIT expressions are not allowed in the connection " +
                                "URL due to security reasons.");
                    }
                }
            }
        }
    }
}
