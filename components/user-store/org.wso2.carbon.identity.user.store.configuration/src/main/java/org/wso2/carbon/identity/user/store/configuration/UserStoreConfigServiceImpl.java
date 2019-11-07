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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.services.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.getSecondaryUserStorePropertiesFromTenantUserRealm;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ENCRYPTED_PROPERTY_MASK;

/**
 * Implementation class for UserStoreConfigService.
 */
public class UserStoreConfigServiceImpl implements UserStoreConfigService {

    public static final Log log = LogFactory.getLog(UserStoreConfigServiceImpl.class);
    private Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactories = UserStoreConfigListenersHolder.
            getInstance().getUserStoreDAOFactories();
    private static final String FILE_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory";
    private static final String DB_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory";

    @Override
    public void addUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        try {
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled() &&
                    StringUtils.isNotBlank(userStoreDTO.getRepositoryClass())) {
                AbstractUserStoreDAOFactory userStoreDAOFactory = userStoreDAOFactories.
                        get(userStoreDTO.getRepositoryClass());
                userStoreDAOFactory.getInstance().addUserStore(userStoreDTO);
            } else {
                if (StringUtils.isNotBlank(userStoreDTO.getRepositoryClass())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Repository separation of user-stores has been disabled. Adding user-store " +
                                userStoreDTO.getDomainId() + " with file-based configuration.");
                    }
                }
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().addUserStore(userStoreDTO);
            }
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
    }

    @Override
    public void updateUserStore(UserStoreDTO userStoreDTO, boolean isStateChange) throws IdentityUserStoreMgtException {

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
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO,
                        false);
            } else if (StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository separation of user-stores has been disabled. Unable to edit " +
                            "user-store " + userStoreDTO.getDomainId() + " with repository class " +
                            userStoreDTO.getRepositoryClass());
                }
            } else {
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStore(userStoreDTO,
                        false);
            }
        } catch (UserStoreException e) {
            String errorMessage = e.getMessage();
            throw new IdentityUserStoreMgtException(errorMessage, e);
        }
    }

    @Override
    public void updateUserStoreByDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException {

        try {
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
                SecondaryUserStoreConfigurationUtil.getFileBasedUserStoreDAOFactory().updateUserStoreDomainName
                        (previousDomainName, userStoreDTO);
            } else if (StringUtils.isNotEmpty(userStoreDTO.getRepositoryClass())) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository separation of user-stores has been disabled. Unable to update " +
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
            throw new IdentityUserStoreMgtException("No selected user store to delete");
        }

        if (!validateDomainsForDelete(domain)) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to delete user store : No privileges to delete own user store configurations ");
            }
            throw new IdentityUserStoreMgtException("No privileges to delete own user store configurations.");
        }
        try {
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
    public UserStoreDTO getUserStore(String domain) throws IdentityUserStoreMgtException {

        UserStoreDTO[] userStoreDTOS = new UserStoreDTO[0];
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
                    return userStoreDTO;
                }
            }
        }
        return null;
    }

    @Override
    public UserStoreDTO[] getUserStores() throws IdentityUserStoreMgtException {

        List<UserStoreDTO> userStoreDTOList = new ArrayList<>();

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

    @Override
    public Set<String> getAvailableUserStoreClasses() throws IdentityUserStoreMgtException {

        return UserStoreManagerRegistry.getUserStoreManagerClasses();
    }

    @Override
    public boolean testRDBMSConnection(String domainName, String driverName, String connectionURL, String username,
                                       String connectionPassword, String messageID)
            throws IdentityUserStoreMgtException {
        if (StringUtils.isNotEmpty(messageID) && StringUtils.isNotEmpty(domainName)) {
            if (connectionPassword.equalsIgnoreCase(ENCRYPTED_PROPERTY_MASK)) {
                Map<String, String> secondaryUserStoreProperties =
                        getSecondaryUserStorePropertiesFromTenantUserRealm(domainName);
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
            log.error(errorMessage, e);
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

    private boolean validateDomainsForDelete(String domain) {
        String userDomain = IdentityUtil.extractDomainFromName(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getUsername());
        //Trying to delete own domain
        return !domain.equalsIgnoreCase(userDomain);

    }
}
