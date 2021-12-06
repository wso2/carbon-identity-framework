
/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.dao.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.store.configuration.UserStoreMgtDBQueries;
import org.wso2.carbon.identity.user.store.configuration.beans.MaskedProperty;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStorePersistanceDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.UserStoreConfigXMLProcessor;
import org.wso2.carbon.user.core.config.XMLProcessorUtils;
import org.wso2.carbon.user.core.tenant.TenantCache;
import org.wso2.carbon.user.core.tenant.TenantIdKey;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.identity.user.store.configuration.UserStoreMgtDBQueries.GET_ALL_USERSTORE_PROPERTIES;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.buildIdentityUserStoreClientException;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.convertMapToArray;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.setMaskInUserStoreProperties;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePostUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePreAdd;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListnersOnUserStorePreDelete;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListnersOnUserStorePreUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.validateForFederatedDomain;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ENCRYPTED_PROPERTY_MASK;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORE;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.XML;

/**
 * This class contains the implementation of CRUD operations of the database based user Stores.
 */
public class DatabaseBasedUserStoreDAOImpl extends AbstractUserStoreDAO {

    private static final Log log = LogFactory.getLog(DatabaseBasedUserStoreDAOImpl.class);
    private static SecretResolver secretResolver;
    private static final String DATABASE_BASED = DatabaseBasedUserStoreDAOFactory.class.getName();
    private final XMLProcessorUtils xmlProcessorUtils = new XMLProcessorUtils();

    @Override
    protected void doAddUserStore(UserStorePersistanceDTO userStorePersistanceDTO) throws
            IdentityUserStoreMgtException {

        String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
        try {
            // Run pre user-store add listeners.
            triggerListenersOnUserStorePreAdd(domainName);
            boolean isValidDomain = xmlProcessorUtils.isValidDomain(domainName, true);
            validateForFederatedDomain(domainName);
            if (isValidDomain) {
                addUserStoreProperties(userStorePersistanceDTO.getUserStoreProperties(), domainName);
                addSecondaryUserStoreManagerFromRealm(userStorePersistanceDTO);
                invalidateTenantCache();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The user store domain: " + domainName + "is not a valid domain name.");
                }
            }
        } catch (UserStoreException | XMLStreamException e) {
            throw new IdentityUserStoreMgtException("Error occured while adding the user store with the domain: " +
                    domainName, e);
        }
    }

    @Override
    protected void doUpdateUserStore(UserStorePersistanceDTO userStorePersistanceDTO, boolean isStateChange)
            throws IdentityUserStoreMgtException {

        String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
        updateUserStoreProperties(domainName, userStorePersistanceDTO);
        try {
            removeRealmFromSecondaryUserStoreManager(domainName);
            removeUserStoreDomainFromRealmChain(domainName);
            addSecondaryUserStoreManagerFromRealm(userStorePersistanceDTO);
            invalidateTenantCache();
        } catch (UserStoreException | XMLStreamException e) {
            throw new IdentityUserStoreMgtException("Error occurred while updating the user store.", e);
        }
    }

    @Override
    protected void doUpdateUserStoreDomainName(String domainName, UserStorePersistanceDTO userStorePersistanceDTO)
            throws IdentityUserStoreMgtException {

        try {
            String newDomainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
            triggerListnersOnUserStorePreUpdate(domainName, newDomainName);
            updateUserStoreProperties(domainName, userStorePersistanceDTO);
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            removeUserStoreDomainFromRealmChain(domainName);
            removeRealmFromSecondaryUserStoreManager(domainName);
            updatePersistedDomainName(domainName, newDomainName, tenantId);
            addSecondaryUserStoreManagerFromRealm(userStorePersistanceDTO);
            triggerListenersOnUserStorePostUpdate(domainName, newDomainName);
            invalidateTenantCache();
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + domainName + " cannot be updated.", e);
        } catch (UserStoreException | XMLStreamException e) {
            throw new IdentityUserStoreMgtException("Error occured while updating the userstore.", e);
        }
    }

    @Override
    protected UserStorePersistanceDTO doGetUserStore(String domainName) throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserStorePersistanceDTO userStorePersistanceDTO = new UserStorePersistanceDTO();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement
                     (UserStoreMgtDBQueries.GET_USERSTORE_PROPERTIES)) {
            prepStmt.setString(1, domainName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, USERSTORE);
            try (ResultSet rSet = prepStmt.executeQuery()) {
                if (rSet.next()) {
                    try (InputStream scriptBinaryStream = rSet.getBinaryStream(1)) {
                        RealmConfiguration realmConfiguration = null;
                        if (scriptBinaryStream != null) {
                            realmConfiguration = getRealmConfiguration(domainName, scriptBinaryStream);
                        }
                        if (realmConfiguration != null) {
                            userStorePersistanceDTO.setUserStoreRealmConfiguration(realmConfiguration);
                            userStorePersistanceDTO.setUserStoreDTO(getUserStoreDTO(realmConfiguration));
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No user store found for domain: " + domainName + " in tenant: " +
                                  tenantId);
                    }
                }
            } catch (IOException | UserStoreException | XMLStreamException e) {
                throw new IdentityUserStoreMgtException("Error occurred while getting user store" +
                        " for domain:" + domainName + " in tenant:" + tenantId, e);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException("Could not read the user store properties for the domain: " +
                    domainName + " in tenant: " + tenantId, e);
        }
        return userStorePersistanceDTO;
    }

    @Override
    protected UserStorePersistanceDTO[] doGetAllUserStores() throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStorePersistanceDTO> userStorePersistanceDTOs = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(GET_ALL_USERSTORE_PROPERTIES)) {
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, USERSTORE);
            try (ResultSet rSet = prepStmt.executeQuery()) {
                while (rSet.next()) {
                    String identifier = rSet.getString(1);
                    try (InputStream scriptBinaryStream = rSet.getBinaryStream(2)) {
                        RealmConfiguration realmConfiguration = null;
                        if (scriptBinaryStream != null) {
                            realmConfiguration = getRealmConfiguration(identifier, scriptBinaryStream);
                            realmConfiguration.setRepositoryClassName(DatabaseBasedUserStoreDAOFactory.class.getName());
                        }
                        getUserStorePersistanceDTOs(userStorePersistanceDTOs, realmConfiguration);
                    }
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new IdentityUserStoreMgtException("Error occured while listing user stores in tenant: "
                        + tenantId, e);
            } catch (XMLStreamException | IOException e) {
                throw new IdentityUserStoreMgtException("Could not read the user store properties in tenant:" +
                        tenantId, e);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException("Could not read the user store properties in tenant:" + tenantId,
                                                    e);
        }
        return userStorePersistanceDTOs.toArray(new UserStorePersistanceDTO[userStorePersistanceDTOs.size()]);
    }

    @Override
    protected UserStorePersistanceDTO[] doGetUserStoresForTenant(int tenantId) throws IdentityUserStoreMgtException {

        List<UserStorePersistanceDTO> userStorePersistanceDTOs = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(GET_ALL_USERSTORE_PROPERTIES)) {
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, USERSTORE);
            try (ResultSet rSet = prepStmt.executeQuery()) {
                while (rSet.next()) {
                    String identifier = rSet.getString(1);
                    try (InputStream scriptBinaryStream = rSet.getBinaryStream(2)) {
                        RealmConfiguration realmConfiguration = null;
                        if (scriptBinaryStream != null) {
                            realmConfiguration = getRealmConfiguration(identifier, scriptBinaryStream);
                            realmConfiguration.setRepositoryClassName(DatabaseBasedUserStoreDAOFactory.class.getName());
                        }
                        getUserStorePersistanceDTOs(userStorePersistanceDTOs, realmConfiguration);
                    }
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new IdentityUserStoreMgtException("Error occured while listing user stores in tenant: "
                        + tenantId, e);
            } catch (XMLStreamException | IOException e) {
                throw new IdentityUserStoreMgtException("Could not read the user store properties in tenant:" +
                        tenantId, e);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException("Could not read the user store properties in tenant:" + tenantId,
                    e);
        }
        return userStorePersistanceDTOs.toArray(new UserStorePersistanceDTO[userStorePersistanceDTOs.size()]);
    }

    private void getUserStorePersistanceDTOs(List<UserStorePersistanceDTO> userStorePersistanceDTOs,
                                             RealmConfiguration realmConfiguration) {

        UserStorePersistanceDTO userStorePersistanceDTO = new UserStorePersistanceDTO();
        userStorePersistanceDTO.setUserStoreDTO(getUserStoreDTO(realmConfiguration));
        userStorePersistanceDTO.setUserStoreRealmConfiguration(realmConfiguration);
        userStorePersistanceDTOs.add(userStorePersistanceDTO);
    }

    private RealmConfiguration getRealmConfiguration(String identifier, InputStream scriptBinaryStream)
            throws UserStoreException, XMLStreamException {

        UserStoreConfigXMLProcessor userStoreXMLProcessor =
                new UserStoreConfigXMLProcessor("/" + identifier + ".xml");
        return userStoreXMLProcessor.
                buildUserStoreConfiguration(getRealmElement(scriptBinaryStream));
    }

    private UserStoreDTO getUserStoreDTO(RealmConfiguration realmConfiguration) {

        Map<String, String> userStoreProperties = realmConfiguration.getUserStoreProperties();

        String uuid = userStoreProperties.get(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }

        String className = realmConfiguration.getUserStoreClass();
        UserStoreDTO userStoreDTO = getUserStoreDTO(realmConfiguration, userStoreProperties);
        userStoreProperties.put("Class", className);
        userStoreProperties.put(UserStoreConfigurationConstant.UNIQUE_ID_CONSTANT, uuid);
        MaskedProperty[] maskedProperties = setMaskInUserStoreProperties(realmConfiguration,
                userStoreProperties, ENCRYPTED_PROPERTY_MASK, className);

        userStoreDTO.setProperties(convertMapToArray(userStoreProperties));

        // Now revert back to original password.
        for (MaskedProperty maskedProperty : maskedProperties) {
            userStoreProperties.put(maskedProperty.getName(), maskedProperty.getValue());
        }

        return userStoreDTO;
    }

    private OMElement getRealmElement(InputStream inputStream) throws XMLStreamException,
            org.wso2.carbon.user.core.UserStoreException {

        try {
            inputStream = CarbonUtils.replaceSystemVariablesInXml(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            OMElement documentElement = builder.getDocumentElement();
            setSecretResolver(documentElement);
            return documentElement;
        } catch (CarbonException e) {
            throw new org.wso2.carbon.user.core.UserStoreException(e.getMessage(), e);
        }
    }

    private void setSecretResolver(OMElement rootElement) {

        secretResolver = SecretResolverFactory.create(rootElement, true);
    }

    private UserStoreDTO getUserStoreDTO(RealmConfiguration secondaryRealmConfiguration,
                                         Map<String, String> userStoreProperties) {

        UserStoreDTO userStoreDTO = new UserStoreDTO();
        userStoreDTO.setClassName(secondaryRealmConfiguration.getUserStoreClass());
        userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigurationConstant
                                                                                             .DESCRIPTION));
        userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants
                                                                                          .DOMAIN_NAME));
        userStoreDTO.setRepositoryClass(DATABASE_BASED);
        if (userStoreProperties.get(DISABLED) != null) {
            userStoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get(DISABLED)));
        }
        return userStoreDTO;
    }

    private void addUserStoreProperties(String userStoreProperties, String domainName)
            throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String errorMessage = "Error occurred while updating the user store properties for " +
                "the userstore domain:" + domainName + "in the tenant:" + tenantId;
        try {

            try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
                try (PreparedStatement userStorePrepStmt = connection
                        .prepareStatement(UserStoreMgtDBQueries.STORE_USERSTORE_PROPERTIES)) {
                    String uuid = String.valueOf(UUID.randomUUID());
                    userStorePrepStmt.setString(1, uuid);
                    userStorePrepStmt.setInt(2, tenantId);
                    setBlobValue(userStoreProperties, userStorePrepStmt, 3);
                    userStorePrepStmt.setString(4, domainName);
                    userStorePrepStmt.setString(5, XML);
                    userStorePrepStmt.setString(6, USERSTORE);
                    userStorePrepStmt.execute();
                    if (log.isDebugEnabled()) {
                        log.debug("The userstore domain:" + domainName + "added for the tenant:" + tenantId);
                    }
                    IdentityDatabaseUtil.commitTransaction(connection);
                } catch (SQLException e) {
                    IdentityDatabaseUtil.rollbackTransaction(connection);
                    throw new IdentityUserStoreMgtException(errorMessage, e);
                }
            } catch (IOException | SQLException ex) {
                throw new IdentityUserStoreMgtException(errorMessage, ex);
            }
        } catch (IdentityRuntimeException e) {
            throw new IdentityUserStoreMgtException("Couldn't get a database connection.", e);
        }
    }

    public void deleteUserStore(String domain) throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            // Run pre user-store name update listeners
            triggerListnersOnUserStorePreDelete(domain);
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.
                    getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            if (userStoreManager == null) {
                throw new IdentityUserStoreMgtException("Unable to find a user store from the " +
                        "ThreadLocalCarbonContext.");
            }
            userStoreManager.deletePersistedDomain(domain);
            deleteUserStore(domain, tenantId);
            removeUserStoreDomainFromRealmChain(domain);
            removeRealmFromSecondaryUserStoreManager(domain);
            deletePersistedDomain(tenantId, domain);
            invalidateTenantCache();
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + domain + " cannot be deleted.", e);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error while triggering the userstore pre delete listeners.");
        }
    }

    private void deleteUserStore(String domain, int tenantId) throws IdentityUserStoreMgtException {

        String msg = "Error while removing the user store with the domain name: " + domain + " in the tenant: " +
                     tenantId;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(UserStoreMgtDBQueries
                                                                            .DELETE_USERSTORE_PROPERTIES)) {
                ps.setString(1, domain);
                ps.setInt(2, tenantId);
                ps.setString(3, USERSTORE);
                ps.executeUpdate();
                if (log.isDebugEnabled()) {
                    log.debug("The userstore domain :" + domain + "removed for the tenant" + tenantId);
                }
                IdentityDatabaseUtil.commitTransaction(connection);

            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new IdentityUserStoreMgtException(msg, e);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException(msg, e);
        }
    }

    private void removeRealmFromSecondaryUserStoreManager(String domain) throws org.wso2.carbon.user.core.
            UserStoreException {

        AbstractUserStoreManager primaryUSM;
        UserRealm userRealm = (UserRealm) CarbonContext.
                getThreadLocalCarbonContext().getUserRealm();
        primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
        primaryUSM.removeSecondaryUserStoreManager(domain);
    }

    public void deleteUserStores(String[] domains) throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String msg = "Error while removing the user store in the tenant: " + tenantId;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(UserStoreMgtDBQueries
                                                                            .DELETE_USERSTORE_PROPERTIES)) {
                for (String domain : domains) {
                    addToBatchForDeleteUserStores(domain, tenantId, ps);
                    if (log.isDebugEnabled()) {
                        log.debug("The userstore domain :" + domain + "in tenant :" + tenantId + " added to the batch" +
                                  " to remove.");
                    }
                    removeUserStoreDomainFromRealmChain(domain);
                    removeRealmFromSecondaryUserStoreManager(domain);
                    deletePersistedDomain(tenantId, domain);
                    invalidateTenantCache();
                }
                ps.executeBatch();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new IdentityUserStoreMgtException(msg, e);
            } catch (UserStoreException e) {
                throw new IdentityUserStoreMgtException(msg, e);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException(msg, e);
        }
    }

    private void deletePersistedDomain(int tenantId, String domainName) throws UserStoreException {

        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.
                getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
        userStoreManager.deletePersistedDomain(domainName);
        if (log.isDebugEnabled()) {
            log.debug("Removed persisted domain name: " + domainName + " of tenant:" + tenantId + " from " +
                    "UM_DOMAIN.");
        }
    }

    private void updateUserStoreProperties(String domainName, UserStorePersistanceDTO userStorePersistanceDTO)
            throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String msg = "Error occured while updating user store properties for the domain:" + domainName + "in the " +
                     "tenant:" + tenantId;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement pst = connection.prepareStatement(UserStoreMgtDBQueries
                                                                             .UPDATE_USERSTORE_PROPERTIES)) {
                setBlobValue(userStorePersistanceDTO.getUserStoreProperties(), pst, 1);
                pst.setString(2, userStorePersistanceDTO.getUserStoreDTO().getDomainId());
                pst.setString(3, domainName);
                pst.setInt(4, tenantId);
                pst.setString(5, USERSTORE);
                pst.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("The userstore domain:" + domainName + "updated for the tenant:" + tenantId);
                }
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new IdentityUserStoreMgtException(msg, e);
            } catch (IOException ex) {
                throw new IdentityUserStoreMgtException(msg, ex);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException(msg);
        }
    }

        private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException,
                IOException {

        if (value != null) {
            InputStream inputStream = new ByteArrayInputStream(value.getBytes());
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }

    private void addToBatchForDeleteUserStores(String domain,
                                               int tenantId,
                                               PreparedStatement preparedStatement
    ) throws SQLException {

        preparedStatement.setString(1, domain);
        preparedStatement.setInt(2, tenantId);
        preparedStatement.setString(3, USERSTORE);
        preparedStatement.addBatch();
    }

    private void addSecondaryUserStoreManagerFromRealm(UserStorePersistanceDTO userStorePersistanceDTO) throws
            UserStoreException, XMLStreamException {

        UserRealm userRealm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        AbstractUserStoreManager primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
        InputStream targetStream = new ByteArrayInputStream(userStorePersistanceDTO.getUserStoreProperties()
                .getBytes());
        RealmConfiguration realmConfiguration = getRealmConfiguration(userStorePersistanceDTO.getUserStoreDTO().
                getDomainId(), targetStream);
        realmConfiguration.setRepositoryClassName(DatabaseBasedUserStoreDAOFactory.class.getName());
        primaryUSM.addSecondaryUserStoreManager(realmConfiguration, userRealm);
        setSecondaryUserStoreToChain(userRealm.getRealmConfiguration(), realmConfiguration);
    }

    /**
     * Set secondary user store at the very end of chain.
     *
     * @param parent : primary user store
     * @param child  : secondary user store
     */
    private void setSecondaryUserStoreToChain(RealmConfiguration parent, RealmConfiguration child) {

        String parentDomain = parent.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        String addingDomain = child.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);

        if (parentDomain == null) {
            return;
        }

        while (parent.getSecondaryRealmConfig() != null) {
            if (parentDomain.equals(addingDomain)) {
                return;
            }
            parent = parent.getSecondaryRealmConfig();
            parentDomain = parent.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        }

        if (parentDomain.equals(addingDomain)) {
            return;
        }
        parent.setSecondaryRealmConfig(child);
    }

    private void invalidateTenantCache() throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            UserCoreUtil.getRealmService().clearCachedUserRealm(tenantId);
            TenantCache.getInstance().clearCacheEntry(new TenantIdKey(tenantId));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new UserStoreException("Error occurred while invalidating the tenant cache for tenant: " +
                    tenantId, e);
        }
    }

    private void removeUserStoreDomainFromRealmChain(String domainName) throws UserStoreException {

        RealmConfiguration secondaryRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRealm tenantUserRealm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        try {
            RealmConfiguration realmConfig = tenantUserRealm.getRealmConfiguration();
            while (realmConfig.getSecondaryRealmConfig() != null) {
                secondaryRealm = realmConfig.getSecondaryRealmConfig();
                if (secondaryRealm.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME).
                        equalsIgnoreCase(domainName)) {
                    realmConfig.setSecondaryRealmConfig(secondaryRealm.getSecondaryRealmConfig());
                    log.info("User store: " + domainName + " of tenant:" + tenantId +
                            " is removed from realm chain.");
                    break;
                } else {
                    realmConfig = realmConfig.getSecondaryRealmConfig();
                }
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new UserStoreException("Error occurred while removing the user store : " + domainName +
                    " from realm chain.", e);
        }
    }

    private void updatePersistedDomainName(String previousDomainName, String domainName, int tenantId)
            throws UserStoreException {

        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.
                getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
        userStoreManager.updatePersistedDomain(previousDomainName, domainName);
        if (log.isDebugEnabled()) {
            log.debug("Renamed persisted domain name from" + previousDomainName + " to " + domainName +
                    " of tenant :" + tenantId + " from UM_DOMAIN.");
        }
    }
}
