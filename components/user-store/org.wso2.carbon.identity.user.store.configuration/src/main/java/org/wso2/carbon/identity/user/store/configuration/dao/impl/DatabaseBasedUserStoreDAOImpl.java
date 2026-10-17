
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.UserStoreMgtDBQueries;
import org.wso2.carbon.identity.user.store.configuration.beans.MaskedProperty;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStorePersistanceDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.UserStoreConfigXMLProcessor;
import org.wso2.carbon.user.core.config.XMLProcessorUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.isUserStoreServedByRealm;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.setMaskInUserStoreProperties;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePostUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListenersOnUserStorePreAdd;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListnersOnUserStorePreDelete;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListnersOnUserStorePreUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.validateForFederatedDomain;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ENCRYPTED_PROPERTY_MASK;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.FILE_EXTENSION_MIGRATED;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.FILE_EXTENSION_XML;

/**
 * This class contains the implementation of CRUD operations of the database based user Stores.
 */
public class DatabaseBasedUserStoreDAOImpl extends AbstractUserStoreDAO {

    private static final Log log = LogFactory.getLog(DatabaseBasedUserStoreDAOImpl.class);
    private SecretResolver secretResolver;
    private static final String DATABASE_BASED = DatabaseBasedUserStoreDAOFactory.class.getName();
    private XMLProcessorUtils xmlProcessorUtils = new XMLProcessorUtils();
    private static final String OF_TENANT = " of tenant: ";

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
                addRealmToSecondaryUserStoreManager(userStorePersistanceDTO);
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
            addRealmToSecondaryUserStoreManager(userStorePersistanceDTO);
        } catch (UserStoreException | XMLStreamException e) {
            throw new IdentityUserStoreMgtException("Error occured while updating the userstore.", e);
        }
    }

    @Override
    protected void doUpdateUserStoreDomainName(String domainName, UserStorePersistanceDTO userStorePersistanceDTO)
            throws IdentityUserStoreMgtException {

        try {
            String newDomainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
            triggerListnersOnUserStorePreUpdate(domainName, newDomainName);
            updateUserStoreProperties(domainName, userStorePersistanceDTO);
            removeRealmFromSecondaryUserStoreManager(domainName);
            addRealmToSecondaryUserStoreManager(userStorePersistanceDTO);
            triggerListenersOnUserStorePostUpdate(domainName, newDomainName);
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + domainName + " cannot be updated.", e);
        } catch (UserStoreException | XMLStreamException e) {
            throw new IdentityUserStoreMgtException("Error occured while updating the userstore.", e);
        }
    }

    @Override
    protected UserStorePersistanceDTO doGetUserStore(String domainName) throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        InputStream scriptBinaryStream = null;
        InputStream clonedStream = null;
        String userStoreProperties = null;
        UserStorePersistanceDTO userStorePersistanceDTO = new UserStorePersistanceDTO();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement
                     (UserStoreMgtDBQueries.GET_USERSTORE_PROPERTIES)) {
            prepStmt.setString(1, domainName);
            prepStmt.setInt(2, tenantId);
            try (ResultSet rSet = prepStmt.executeQuery()) {
                if (rSet.next()) {
                    RealmConfiguration realmConfiguration = null;
                    scriptBinaryStream = rSet.getBinaryStream(1);
                    clonedStream = rSet.getBinaryStream(1);
                    if (scriptBinaryStream != null) {
                        realmConfiguration = getRealmConfiguration(domainName, scriptBinaryStream);
                    }
                    if (clonedStream != null) {
                        userStoreProperties = IOUtils.toString(clonedStream);
                    }
                    userStorePersistanceDTO.setUserStoreProperties(userStoreProperties);
                    if (realmConfiguration != null) {
                        userStorePersistanceDTO.setUserStoreDTO(getUserStoreDTO(realmConfiguration));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No user store properties found for domain: " + domainName + " in tenant: " +
                                  tenantId);
                    }
                }
            } catch (IOException | UserStoreException | XMLStreamException e) {
                throw new IdentityUserStoreMgtException("Error occured while getting user store properties for " +
                                                        "domain:" + domainName + " in tenant:" + tenantId, e);
            } finally {
                try {
                    if (scriptBinaryStream != null) {
                        scriptBinaryStream.close();
                    }
                    if (clonedStream != null) {
                        clonedStream.close();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new IdentityUserStoreMgtException("Error occured while loading user stores.", e);
                }
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException("Could not read the user store properties for the domain:" +
                    domainName + " in tenant:" + tenantId, e);
        }
        return userStorePersistanceDTO;
    }

    @Override
    protected UserStorePersistanceDTO[] doGetAllUserStores() throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<UserStorePersistanceDTO> userStorePersistanceDTOs = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement
                     (GET_ALL_USERSTORE_PROPERTIES)) {
            prepStmt.setInt(1, tenantId);
            try (ResultSet rSet = prepStmt.executeQuery()) {
                while (rSet.next()) {
                    String identifier = rSet.getString(1);
                    InputStream scriptBinaryStream = null;
                    InputStream clonedStream = null;
                    String userStorePropertyValues = null;
                    try {
                        scriptBinaryStream = rSet.getBinaryStream(2);
                        clonedStream = rSet.getBinaryStream(2);
                        RealmConfiguration realmConfiguration = null;
                        if (scriptBinaryStream != null) {
                            realmConfiguration = getRealmConfiguration(identifier, scriptBinaryStream);
                        }
                        if (clonedStream != null) {
                            userStorePropertyValues = IOUtils.toString(clonedStream);
                        }
                        getUserStorePersistanceDTOs(userStorePersistanceDTOs, realmConfiguration,
                                userStorePropertyValues);

                    } finally {
                        if (scriptBinaryStream != null) {
                            scriptBinaryStream.close();
                        }
                        if (clonedStream != null) {
                            clonedStream.close();
                        }
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
                                             RealmConfiguration realmConfiguration, String userStorePorpertyValues) {

        UserStorePersistanceDTO userStorePersistanceDTO = new UserStorePersistanceDTO();
        userStorePersistanceDTO.setUserStoreDTO(getUserStoreDTO(realmConfiguration));
        userStorePersistanceDTO.setUserStoreProperties(userStorePorpertyValues);
        userStorePersistanceDTOs.add(userStorePersistanceDTO);
    }

    private RealmConfiguration getRealmConfiguration(String identifier, InputStream scriptBinaryStream)
            throws UserStoreException, XMLStreamException {

        /*
         The processor splits this path on the platform's file separator and requires the last segment to
         match the domain, reading underscores back as dots. A hard coded slash leaves it unsplit on Windows.
        */
        UserStoreConfigXMLProcessor userStoreXMLProcessor = new UserStoreConfigXMLProcessor(
                File.separator + identifier.replace(UserStoreConfigurationConstant.PERIOD,
                        UserStoreConfigurationConstant.UNDERSCORE) + FILE_EXTENSION_XML);
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

            try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
                try (PreparedStatement userStorePrepStmt = connection
                        .prepareStatement(UserStoreMgtDBQueries.STORE_USERSTORE_PROPERTIES)) {
                    String uuid = String.valueOf(UUID.randomUUID());
                    userStorePrepStmt.setString(1, uuid);
                    userStorePrepStmt.setInt(2, tenantId);
                    setBlobValue(userStoreProperties, userStorePrepStmt, 3);
                    userStorePrepStmt.setString(4, domainName);
                    userStorePrepStmt.execute();
                    if (log.isDebugEnabled()) {
                        log.debug("The userstore domain:" + domainName + "added for the tenant:" + tenantId);
                    }
                    IdentityDatabaseUtil.commitUserDBTransaction(connection);
                } catch (SQLException e) {
                    IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
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
            removeRealmFromSecondaryUserStoreManager(domain);
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstore " + domain + " cannot be deleted.", e);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error while triggering the userstore pre delete listeners.");
        }
    }

    private void deleteUserStore(String domain, int tenantId) throws IdentityUserStoreMgtException {

        String msg = "Error while removing the user store with the domain name: " + domain + " in the tenant: " +
                     tenantId;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try (PreparedStatement ps = connection.prepareStatement(UserStoreMgtDBQueries
                                                                            .DELETE_USERSTORE_PROPERTIES)) {
                ps.setString(1, domain);
                ps.setInt(2, tenantId);
                ps.executeUpdate();
                if (log.isDebugEnabled()) {
                    log.debug("The userstore domain :" + domain + "removed for the tenant" + tenantId);
                }
                IdentityDatabaseUtil.commitUserDBTransaction(connection);

            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
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
        try {
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.
                    getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            if (userStoreManager == null) {
                throw new IdentityUserStoreMgtException("Unable to find a user store from the " +
                        "ThreadLocalCarbonContext.");
            }
            // Run pre user-store delete listeners, as the single store path does.
            for (String domain : domains) {
                triggerListnersOnUserStorePreDelete(domain);
            }
            deleteUserStoresFromDatabase(domains, tenantId, msg);
            /*
             The realm is only changed once the rows are gone. Doing it while the batch is still open leaves the
             realm without user stores the database still holds if the batch then fails.
            */
            for (String domain : domains) {
                userStoreManager.deletePersistedDomain(domain);
                removeRealmFromSecondaryUserStoreManager(domain);
            }
        } catch (UserStoreClientException e) {
            throw buildIdentityUserStoreClientException("Userstores cannot be deleted.", e);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException(msg, e);
        }
    }

    private void deleteUserStoresFromDatabase(String[] domains, int tenantId, String msg)
            throws IdentityUserStoreMgtException {

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try (PreparedStatement ps = connection.prepareStatement(UserStoreMgtDBQueries
                                                                            .DELETE_USERSTORE_PROPERTIES)) {
                for (String domain : domains) {
                    addToBatchForDeleteUserStores(domain, tenantId, ps);
                    if (log.isDebugEnabled()) {
                        log.debug("The userstore domain :" + domain + "in tenant :" + tenantId + " added to the batch" +
                                  " to remove.");
                    }
                }
                ps.executeBatch();
                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                throw new IdentityUserStoreMgtException(msg, e);
            }
        } catch (SQLException e) {
            throw new IdentityUserStoreMgtException(msg, e);
        }
    }

    private void updateUserStoreProperties(String domainName, UserStorePersistanceDTO userStorePersistanceDTO)
            throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String msg = "Error occured while updating user store properties for the domain:" + domainName + "in the " +
                     "tenant:" + tenantId;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try (PreparedStatement pst = connection.prepareStatement(UserStoreMgtDBQueries
                                                                             .UPDATE_USERSTORE_PROPERTIES)) {
                setBlobValue(userStorePersistanceDTO.getUserStoreProperties(), pst, 1);
                pst.setString(2, userStorePersistanceDTO.getUserStoreDTO().getDomainId());
                pst.setString(3, domainName);
                pst.setInt(4, tenantId);
                pst.executeUpdate();
                IdentityDatabaseUtil.commitUserDBTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("The userstore domain:" + domainName + "updated for the tenant:" + tenantId);
                }
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
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
        preparedStatement.addBatch();
    }

    /**
     * Register every secondary user store persisted for the current tenant with its realm. The file based
     * repository has the deployment engine to do this on tenant load; the database based repository does not,
     * so this is driven by the user store configuration listener instead.
     *
     * @throws IdentityUserStoreMgtException if the persisted user stores cannot be read.
     */
    public void loadSecondaryUserStores() throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        for (UserStorePersistanceDTO userStorePersistanceDTO : doGetAllUserStores()) {
            if (userStorePersistanceDTO.getUserStoreDTO() == null) {
                continue;
            }
            String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
            try {
                if (isUserStoreServedByRealm(domainName)) {
                    /*
                     The realm already serves this domain, which is the case when the same domain is also on
                     the file system and the deployment engine read it first. Registering it again is rejected
                     as a duplicate domain, so leave the realm as it stands.
                    */
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping the user store domain: " + domainName + OF_TENANT + tenantId +
                                ". The realm already serves it.");
                    }
                    continue;
                }
                addRealmToSecondaryUserStoreManager(userStorePersistanceDTO);
                if (log.isDebugEnabled()) {
                    log.debug("Loaded the user store domain: " + domainName + OF_TENANT + tenantId +
                            " from the database.");
                }
            } catch (UserStoreException | XMLStreamException e) {
                // A single broken user store should not stop the remaining ones from loading.
                log.error("Error occurred while loading the user store domain: " + domainName + OF_TENANT +
                        tenantId, e);
            }
        }
    }

    /**
     * Move the secondary user store configurations still held as files by the current tenant into the database.
     * <p>
     * A configuration is written as a row first and only then moved aside, so a run that stops half way leaves
     * nothing behind and can simply be repeated. A domain that already has a row is left untouched, file and all:
     * the row is what the management API serves, and overwriting it would discard everything done since.
     */
    public void migrateFileBasedUserStores() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        File[] configurationFiles = SecondaryUserStoreConfigurationUtil.getUserStoreConfigurationDirectory().toFile()
                .listFiles((directory, name) -> name.endsWith(FILE_EXTENSION_XML));
        if (configurationFiles == null || configurationFiles.length == 0) {
            // The tenant keeps no configurations on the file system, which is the steady state after a migration.
            return;
        }
        for (File configurationFile : configurationFiles) {
            try {
                migrateFileBasedUserStore(configurationFile, tenantId);
            } catch (IdentityUserStoreMgtException | IOException | UserStoreException | XMLStreamException e) {
                // One configuration that cannot be migrated must not hold back the rest of the tenant.
                log.error("Error occurred while migrating the user store configuration file: " +
                        configurationFile.getName() + OF_TENANT + tenantId + ". The file is left in place.", e);
            }
        }
    }

    private void migrateFileBasedUserStore(File configurationFile, int tenantId) throws
            IdentityUserStoreMgtException, IOException, UserStoreException, XMLStreamException {

        Path configurationFilePath = configurationFile.toPath();
        String userStoreProperties = new String(Files.readAllBytes(configurationFilePath), StandardCharsets.UTF_8);
        String fileName = configurationFile.getName();
        RealmConfiguration realmConfiguration = getRealmConfiguration(
                fileName.substring(0, fileName.length() - FILE_EXTENSION_XML.length()),
                new ByteArrayInputStream(userStoreProperties.getBytes(StandardCharsets.UTF_8)));
        if (realmConfiguration == null) {
            log.warn("The user store configuration file: " + fileName + OF_TENANT + tenantId + " could not be " +
                    "read as a user store configuration and is not migrated.");
            return;
        }
        String domainName = realmConfiguration.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
        if (StringUtils.isBlank(domainName)) {
            /*
             Without a domain there is nothing to key the row on. Persisting it would write a row no loader can
             register and move the file aside, taking a working user store out of service.
            */
            log.warn("The user store configuration file: " + fileName + OF_TENANT + tenantId + " carries no " +
                    "domain name and is not migrated.");
            return;
        }
        if (IdentityUtil.getAgentIdentityUserstoreName().equalsIgnoreCase(domainName)) {
            /*
             The agent user store is put in place by the product, not by an administrator, and the component that
             does so treats the presence of this very file as the sign that the tenant already has one. Moving it
             aside would have it attempt to add the store again on every startup.
            */
            if (log.isDebugEnabled()) {
                log.debug("The agent user store of tenant: " + tenantId + " is left on the file system.");
            }
            return;
        }
        if (doGetUserStore(domainName).getUserStoreDTO() != null) {
            log.info("The user store domain: " + domainName + OF_TENANT + tenantId + " is already persisted in " +
                    "the database. The configuration file: " + fileName + " is left in place.");
            return;
        }
        addUserStoreProperties(userStoreProperties, domainName);
        Path migratedFilePath = Paths.get(configurationFilePath.toString() + FILE_EXTENSION_MIGRATED);
        Files.move(configurationFilePath, migratedFilePath);
        log.info("Migrated the user store domain: " + domainName + OF_TENANT + tenantId + " into the database. " +
                "Its configuration file is moved to: " + migratedFilePath.getFileName() + ".");
    }

    private void addRealmToSecondaryUserStoreManager(UserStorePersistanceDTO userStorePersistanceDTO) throws
            UserStoreException, XMLStreamException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRealm userRealm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        AbstractUserStoreManager primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
        InputStream targetStream = new ByteArrayInputStream(userStorePersistanceDTO.getUserStoreProperties()
                                                                                   .getBytes());
        RealmConfiguration realmConfiguration = getRealmConfiguration(userStorePersistanceDTO.getUserStoreDTO().
                getDomainId(), targetStream);
        /*
         Unlike a file based configuration, which the deployment engine reads from the tenant's own deployment
         directory, the configuration read here carries no tenant of its own. Without this the secondary user
         store manager is built against the default tenant and the domain cannot be resolved.
        */
        realmConfiguration.setTenantId(tenantId);
        setSecondaryUserStore(userRealm.getRealmConfiguration(), realmConfiguration);
        primaryUSM.addSecondaryUserStoreManager(realmConfiguration, userRealm);
    }

    /**
     * Append the secondary user store to the end of the realm configuration chain.
     *
     * @param parent Primary realm configuration.
     * @param child  Secondary realm configuration being added.
     */
    private void setSecondaryUserStore(RealmConfiguration parent, RealmConfiguration child) {

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
}
