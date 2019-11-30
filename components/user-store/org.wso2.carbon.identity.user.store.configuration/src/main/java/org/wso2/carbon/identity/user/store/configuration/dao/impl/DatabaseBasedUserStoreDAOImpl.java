
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

import static org.wso2.carbon.identity.user.store.configuration.UserStoreMgtDBQueries.GET_All_USERSTORE_PROPERTIES;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.convertMapToArray;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.setMaskInUserStoreProperties;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListnersOnUserStorePreDelete;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.triggerListnersOnUserStorePreUpdate;
import static org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil.validateForFederatedDomain;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.ENCRYPTED_PROPERTY_MASK;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.USERSTORE;
import static org.wso2.carbon.identity.user.store.configuration.utils.UserStoreConfigurationConstant.XML;

public class DatabaseBasedUserStoreDAOImpl extends AbstractUserStoreDAO {

    private static final Log log = LogFactory.getLog(DatabaseBasedUserStoreDAOImpl.class);
    private SecretResolver secretResolver;
    private static final String DATABASE_BASED = DatabaseBasedUserStoreDAOFactory.class.getName();
    private XMLProcessorUtils xmlProcessorUtils = new XMLProcessorUtils();

    @Override
    protected void doAddUserStore(UserStorePersistanceDTO userStorePersistanceDTO) throws IdentityUserStoreMgtException {

        String domainName = userStorePersistanceDTO.getUserStoreDTO().getDomainId();
        try {
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
            triggerListnersOnUserStorePreUpdate(domainName, userStorePersistanceDTO.getUserStoreDTO().getDomainId());
            updateUserStoreProperties(domainName, userStorePersistanceDTO);
            removeRealmFromSecondaryUserStoreManager(domainName);
            addRealmToSecondaryUserStoreManager(userStorePersistanceDTO);
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
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement
                     (UserStoreMgtDBQueries.GET_USERSTORE_PROPERTIES)) {
            prepStmt.setString(1, domainName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, USERSTORE);
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
                        log.debug("No user store properties found for domain: " + domainName + " in tenant: " + tenantId);
                    }
                }
            } catch (IOException | UserStoreException | XMLStreamException e) {
                throw new IdentityUserStoreMgtException("Error occured while getting user store properties for domain:" +
                        domainName + " in tenant:" + tenantId, e);
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
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement
                     (GET_All_USERSTORE_PROPERTIES)) {
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, USERSTORE);
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
            throw new IdentityUserStoreMgtException("Could not read the user store properties in tenant:" + tenantId, e);
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
        userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigurationConstant.DESCRIPTION));
        userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME));
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
            deleteUserStore(domain, tenantId);
            removeRealmFromSecondaryUserStoreManager(domain);
        } catch (UserStoreException e) {
            throw new IdentityUserStoreMgtException("Error while triggering the userstore pre delete listeners.");
        }
    }

    private void deleteUserStore(String domain, int tenantId) throws IdentityUserStoreMgtException {

        String msg = "Error while removing the user store with the domain name: " + domain + " in the tenant: " + tenantId;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(UserStoreMgtDBQueries.DELETE_USERSTORE_PROPERTIES)) {
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
            try (PreparedStatement ps = connection.prepareStatement(UserStoreMgtDBQueries.DELETE_USERSTORE_PROPERTIES)) {
                for (String domain : domains) {
                    addToBatchForDeleteUserStores(domain, tenantId, ps);
                    if (log.isDebugEnabled()) {
                        log.debug("The userstore domain :" + domain + "in tenant :" + tenantId + " added to the batch to " +
                                "remove.");
                    }
                    removeRealmFromSecondaryUserStoreManager(domain);
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

    private void updateUserStoreProperties(String domainName, UserStorePersistanceDTO userStorePersistanceDTO)
            throws IdentityUserStoreMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String msg = "Error occured while updating user store properties for the domain:" + domainName + "in the tenant:"
                + tenantId;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement pst = connection.prepareStatement(UserStoreMgtDBQueries.UPDATE_USERSTORE_PROPERTIES)) {
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

        private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException, IOException {

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

    private void addRealmToSecondaryUserStoreManager(UserStorePersistanceDTO userStorePersistanceDTO) throws
            UserStoreException, XMLStreamException {

        UserRealm userRealm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        AbstractUserStoreManager primaryUSM = (AbstractUserStoreManager) userRealm.getUserStoreManager();
        InputStream targetStream = new ByteArrayInputStream(userStorePersistanceDTO.getUserStoreProperties().getBytes());
        RealmConfiguration realmConfiguration = getRealmConfiguration(userStorePersistanceDTO.getUserStoreDTO().
                getDomainId(), targetStream);
        primaryUSM.addSecondaryUserStoreManager(realmConfiguration, userRealm);
    }
}
