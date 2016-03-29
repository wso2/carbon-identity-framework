/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.mgt.model.Claim;
import org.wso2.carbon.identity.claim.mgt.bean.MetaDataBean;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ClaimDAOImpl implements ClaimDAO {
    private static Log log = LogFactory.getLog(ClaimDAOImpl.class);


    /**
     * initializing data source and the tenant
     *
     */
    public ClaimDAOImpl() {

    }

    /**
     * Initiate adding claim to database
     *
     * @param claim claim
     * @throws UserStoreException
     */
    @Override
    public void addClaimMapping(ClaimMapping claim, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            this.addClaimMapping(dbConnection, claim, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when initiating add claim mappings - ", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Initiate updating claim to database
     *
     * @param claim
     * @throws UserStoreException
     */
    @Override
    public void updateClaim(ClaimMapping claim, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            this.updateClaimMapping(dbConnection, claim, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error initiate claim update - " + claim.getClaim().getClaimUri(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Initiate deleting claim mapping from database
     *
     * @param claimMapping
     * @throws UserStoreException
     */
    @Override
    public void deleteClaimMapping(ClaimMapping claimMapping, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            deleteClaimMapping(dbConnection, claimMapping.getClaim().getClaimUri(), claimMapping.getClaim()
                    .getDialectURI(), tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when deleting claim  - " + claimMapping.getClaim()
                    .getClaimUri(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Delete dialect from the database
     *
     * @param dialectUri dialect uri
     * @throws UserStoreException
     */
    @Override
    public void deleteDialect(String dialectUri, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants
                    .GET_CLAIMS_FOR_DIALECTT_SQL);
            prepStmt.setString(1, dialectUri);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            ResultSet rs = prepStmt.executeQuery();
            List<String> lst = new ArrayList<String>();
            while (rs.next()) {
                lst.add(rs.getString(1));
            }
            prepStmt.close();
            for (Iterator<String> ite = lst.iterator(); ite.hasNext(); ) {
                String claimUri = ite.next();
                this.deleteClaimMapping(dbConnection, claimUri, dialectUri, tenantId);
            }

            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.DELETE_DIALECT);
            prepStmt.setString(1, dialectUri);
            prepStmt.executeUpdate();
            prepStmt.close();

            dbConnection.commit();
        } catch (SQLException e) {
            log.error("Database Error when deleting dialect - " + dialectUri + " in tenant: " + tenantId, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }


    /**
     * Add array of claims to the database
     *
     * @param claims
     * @return
     * @throws UserStoreException
     */
    @Override
    public boolean addClaimMappings(ClaimMapping[] claims, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            for (ClaimMapping claim : claims) {
                this.addClaimMapping(dbConnection, claim, tenantId);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when adding claim mappings.", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
        return true;
    }


    /**
     * Get dialect count from the database.
     *
     * @return dialect count
     * @throws UserStoreException
     */
    @Override
    public int getDialectCount(int tenantId) throws UserStoreException {
        int count = 0;
        PreparedStatement prepStmt = null;
        Connection dbConnection = null;
        ResultSet rs = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.COUNT_DIALECTS);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when retrieving dialect count. ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return count;
    }

    /**
     * Add each claim to the database. This will add dialect, claim,
     * claim relation(if it is not a local claim) or meta data for the claim respectively.
     *
     * @param claimMapping claim mapping
     * @throws UserStoreException
     */
    protected void addClaimMapping(Connection dbConnection, ClaimMapping claimMapping, int tenantId)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        try {
            Claim claim = claimMapping.getClaim();
            int dialectId = getDialectId(dbConnection, claim.getDialectURI(), tenantId);
            if (dialectId == -1) {
                dialectId = addDialect(dbConnection, claim.getDialectURI(), tenantId);
            }

            int claimId = getClaimId(dbConnection, claim.getClaimUri(), tenantId);
            if (claimId == -1) {
                claimId = addClaim(dbConnection, dialectId, claim.getClaimUri(), tenantId);
            }
            if (claim.getIsLocalClaim() == null && claim.getDialectURI().toLowerCase().contains(LOCAL_CLAIM_URI)) {
                claim.setIsLocalClaim(true);
            } else if (claim.getIsLocalClaim() == null) {
                claim.setIsLocalClaim(false);
            }

            Map<String, String> attributes = claimMapping.getMappedAttributes();
            if (claim.getIsLocalClaim()) {
                if (claimMapping.getMappedAttribute() != null) {
                    addMappedAttribute(dbConnection, claimId, null, claimMapping.getMappedAttribute(), tenantId);
                }

                if (attributes.size() > 0) {
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        addMappedAttribute(dbConnection, claimId, entry.getValue(), entry.getKey(), tenantId);
                    }
                }
            } else {
                if (claimMapping.getMappedAttribute() != null) {
                    int localClaimId = getClaimId(dbConnection, claimMapping.getMappedAttribute(), tenantId);
                    log.info("dialect=" + claim.getDialectURI() + " Attribute=" + claimMapping.getMappedAttribute() +
                            " Url=" + claim.getClaimUri());
                    addClaimRelation(dbConnection, localClaimId, claimId, tenantId);
                }
            }

            Map<String, String> metaData = new HashMap<>();

            short isSupported = 0;
            if (claim.isSupportedByDefault()) {
                isSupported = 1;
            }

            short isRequired = 0;
            if (claim.isRequired()) {
                isRequired = 1;
            }

            short isCheckedAttribute = 0;
            if (claim.isCheckedAttribute()) {
                isCheckedAttribute = 1;
            }

            short isReadOnly = 0;
            if (claim.isReadOnly()) {
                isReadOnly = 1;
            }

            if (claim.getCustomMetaData() != null) {
                for (Map.Entry<String, String> entry : claim.getCustomMetaData().entrySet()) {
                    metaData.put(entry.getKey(), entry.getValue());
                }
            }

            metaData.put(LOCAL_NAME_CHECKED_ATTR, String.valueOf(isCheckedAttribute));
            metaData.put(LOCAL_NAME_REQUIRED, String.valueOf(isRequired));
            metaData.put(LOCAL_NAME_SUPPORTED_BY_DEFAULT, String.valueOf(isSupported));
            metaData.put(LOCAL_NAME_READ_ONLY, String.valueOf(isReadOnly));
            metaData.put(LOCAL_NAME_DISPLAY_NAME, claim.getDisplayTag());
            metaData.put(LOCAL_NAME_DESCRIPTION, claim.getDescription());
            metaData.put(LOCAL_NAME_REG_EX, claim.getRegEx());
            metaData.put(LOCAL_NAME_DISPLAY_ORDER, String.valueOf(claim.getDisplayOrder()));

            if (metaData.size() != 0) {
                for (Map.Entry<String, String> entry : metaData.entrySet()) {                //addtobatch
                    addMetaData(dbConnection, claimId, entry.getKey(), entry.getValue(), tenantId);
                }
            }

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }

    }


    /**
     * Load all the claim mappings from the database, this will get claims and meta data
     * for the claims separately and bind them.
     *
     * @return
     * @throws UserStoreException
     */
    @Override
    public List<ClaimMapping> loadClaimMappings(int tenantId) throws UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();
        PreparedStatement prepStmt = null;
        PreparedStatement prepStmtAdditional = null;
        PreparedStatement prepStmtLocal = null;
        Connection dbConnection = null;
        ResultSet claimResultSetLocal = null;
        ResultSet claimResultSetAdditional = null;
        ResultSet metaDataResultSet = null;
        Map<String, ClaimMapping> dialectMap = new HashMap<>();

        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            prepStmtLocal = dbConnection.prepareStatement(ClaimDBConstants.GET_LOCAL_CLAIMS);
            prepStmtLocal.setInt(1, tenantId);
            prepStmtLocal.setInt(2, tenantId);
            claimResultSetLocal = prepStmtLocal.executeQuery();

            prepStmtAdditional = dbConnection.prepareStatement(ClaimDBConstants.GET_ADDITIONAL_CLAIMS);
            prepStmtAdditional.setInt(1, tenantId);
            prepStmtAdditional.setInt(2, tenantId);
            claimResultSetAdditional = prepStmtAdditional.executeQuery();

            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_ALL_CLAIM_META_DATA_SQL);
            prepStmt.setInt(1, tenantId);
            metaDataResultSet = prepStmt.executeQuery();

            List<MetaDataBean> beans = new ArrayList<>();
            while (metaDataResultSet.next()) {
                MetaDataBean bean = new MetaDataBean();
                bean.setClaimId(metaDataResultSet.getInt("IDN_CLAIM_ID"));
                bean.setMetaDataKey(metaDataResultSet.getString("IDN_KEY"));
                bean.setMetaDataValue(metaDataResultSet.getString("IDN_VALUE"));
                beans.add(bean);
            }

            while (claimResultSetLocal.next()) {
                ClaimMapping cm = new ClaimMapping();
                Claim claim = new Claim();

                String dialectUri = claimResultSetLocal.getString("IDN_DIALECT_URI");
                claim.setIsLocalClaim(true);
                claim.setDialectURI(dialectUri);
                claim.setClaimUri(claimResultSetLocal.getString("IDN_CLAIM_URI"));
                String attributeName = claimResultSetLocal.getString("IDN_MAPPED_ATTRIBUTE");
                String domainName = claimResultSetLocal.getString("IDN_MAPPED_ATTRIBUTE_DOMAIN");

                int claimId = claimResultSetLocal.getInt("IDN_CLAIM_ID");
                populateClaimObject(beans, claim, claimId);

                cm.setClaim(claim);
                cm.setMappedAttribute(domainName, attributeName);

                if (dialectMap.get(claim.getDialectURI() + claim.getClaimUri()) == null) {
                    dialectMap.put(claim.getDialectURI() + claim.getClaimUri(), cm);
                } else {
                    ClaimMapping mapping;
                    mapping = dialectMap.get(claim.getDialectURI() + claim.getClaimUri());
                    mapping.setMappedAttribute(domainName, attributeName);
                    dialectMap.put(claim.getDialectURI() + claim.getClaimUri(), mapping);
                }
            }

            while (claimResultSetAdditional.next()) {
                ClaimMapping cm = new ClaimMapping();
                Claim claim = new Claim();

                String dialectUri = claimResultSetLocal.getString("IDN_DIALECT_URI");
                claim.setIsLocalClaim(false);
                claim.setDialectURI(dialectUri);
                claim.setClaimUri(claimResultSetLocal.getString("IDN_CLAIM_URI"));
                String attributeName = claimResultSetLocal.getString("IDN_MAPPED_ATTRIBUTE");

                int claimId = claimResultSetLocal.getInt("IDN_CLAIM_ID");
                populateClaimObject(beans, claim, claimId);

                cm.setClaim(claim);
                cm.setMappedAttribute(attributeName);

                //cannot have mapped attributes in different domains
                dialectMap.put(claim.getDialectURI() + claim.getClaimUri(), cm);
            }

            for (Map.Entry<String, ClaimMapping> entry : dialectMap.entrySet()) {
                claimList.add(entry.getValue());
            }

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when loading claim mappings. ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, claimResultSetLocal, prepStmt);
            IdentityDatabaseUtil.closeResultSet(claimResultSetAdditional);
            IdentityDatabaseUtil.closeResultSet(metaDataResultSet);
            IdentityDatabaseUtil.closeStatement(prepStmtAdditional);
            IdentityDatabaseUtil.closeStatement(prepStmtLocal);
        }
        return claimList;
    }

    public List<ClaimMapping> loadClaimMappings(int tenantId, String dialectURI) throws UserStoreException {
        List<ClaimMapping> claimList = new ArrayList<>();
        PreparedStatement prepStmt = null;
        PreparedStatement prepStmtAdditional = null;
        PreparedStatement prepStmtLocal = null;
        Connection dbConnection = null;
        ResultSet claimResultSetLocal = null;
        ResultSet claimResultSetAdditional = null;
        ResultSet metaDataResultSet = null;
        Map<String, ClaimMapping> dialectMap = new HashMap<>();

        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_ALL_CLAIM_META_DATA_SQL);
            prepStmt.setInt(1, tenantId);
            metaDataResultSet = prepStmt.executeQuery();

            List<MetaDataBean> beans = new ArrayList<>();
            while (metaDataResultSet.next()) {
                MetaDataBean bean = new MetaDataBean();
                bean.setClaimId(metaDataResultSet.getInt("IDN_CLAIM_ID"));
                bean.setMetaDataKey(metaDataResultSet.getString("IDN_KEY"));
                bean.setMetaDataValue(metaDataResultSet.getString("IDN_VALUE"));
                beans.add(bean);
            }

            if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equals(dialectURI)) {
                dbConnection = IdentityDatabaseUtil.getDBConnection();
                prepStmtLocal = dbConnection.prepareStatement(ClaimDBConstants.GET_LOCAL_CLAIMS);
                prepStmtLocal.setInt(1, tenantId);
                prepStmtLocal.setInt(2, tenantId);
                claimResultSetLocal = prepStmtLocal.executeQuery();

                while (claimResultSetLocal.next()) {
                    ClaimMapping cm = new ClaimMapping();
                    Claim claim = new Claim();

                    String dialectUri = claimResultSetLocal.getString("IDN_DIALECT_URI");
                    claim.setIsLocalClaim(false);
                    claim.setDialectURI(dialectUri);
                    claim.setClaimUri(claimResultSetLocal.getString("IDN_CLAIM_URI"));
                    String attributeName = claimResultSetLocal.getString("IDN_MAPPED_ATTRIBUTE");
                    String domainName = claimResultSetLocal.getString("IDN_MAPPED_ATTRIBUTE_DOMAIN");

                    int claimId = claimResultSetLocal.getInt("IDN_CLAIM_ID");
                    populateClaimObject(beans, claim, claimId);

                    cm.setClaim(claim);
                    cm.setMappedAttribute(domainName, attributeName);

                    if (dialectMap.get(claim.getDialectURI() + claim.getClaimUri()) == null) {
                        dialectMap.put(claim.getDialectURI() + claim.getClaimUri(), cm);
                    } else {
                        ClaimMapping mapping;
                        mapping = dialectMap.get(claim.getDialectURI() + claim.getClaimUri());
                        mapping.setMappedAttribute(domainName, attributeName);
                        dialectMap.put(claim.getDialectURI() + claim.getClaimUri(), mapping);
                    }
                }
            }else {
                prepStmtAdditional = dbConnection.prepareStatement(ClaimDBConstants.GET_ADDITIONAL_CLAIMS);
                prepStmtAdditional.setInt(1, tenantId);
                prepStmtAdditional.setInt(2, tenantId);
                claimResultSetAdditional = prepStmtAdditional.executeQuery();
                while (claimResultSetAdditional.next()) {
                    ClaimMapping cm = new ClaimMapping();
                    Claim claim = new Claim();

                    String dialectUri = claimResultSetLocal.getString("IDN_DIALECT_URI");
                    claim.setIsLocalClaim(false);
                    claim.setDialectURI(dialectUri);
                    claim.setClaimUri(claimResultSetLocal.getString("IDN_CLAIM_URI"));
                    String attributeName = claimResultSetLocal.getString("IDN_MAPPED_ATTRIBUTE");

                    int claimId = claimResultSetLocal.getInt("IDN_CLAIM_ID");
                    populateClaimObject(beans, claim, claimId);

                    cm.setClaim(claim);
                    cm.setMappedAttribute(attributeName);

                    //cannot have mapped attributes in different domains
                    dialectMap.put(claim.getDialectURI() + claim.getClaimUri(), cm);
                }
            }

            for (Map.Entry<String, ClaimMapping> entry : dialectMap.entrySet()) {
                claimList.add(entry.getValue());
            }

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when loading claim mappings. ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, claimResultSetLocal, prepStmt);
            IdentityDatabaseUtil.closeResultSet(claimResultSetAdditional);
            IdentityDatabaseUtil.closeResultSet(metaDataResultSet);
            IdentityDatabaseUtil.closeStatement(prepStmtAdditional);
            IdentityDatabaseUtil.closeStatement(prepStmtLocal);
        }
        return claimList;
    }

    private void populateClaimObject(List<MetaDataBean> beans, Claim claim, int claimId) {
        Map<String, String> customMetaData = new HashMap<>();
        for (MetaDataBean bean : beans) {
            if (bean.getClaimId() == claimId) {
                if (LOCAL_NAME_DISPLAY_NAME.equals(bean.getMetaDataKey())) {
                    claim.setDisplayTag(bean.getMetaDataValue());
                } else if (LOCAL_NAME_DESCRIPTION.equals(bean.getMetaDataKey())) {
                    claim.setDescription(bean.getMetaDataValue());
                } else if (LOCAL_NAME_REG_EX.equals(bean.getMetaDataKey())) {
                    claim.setRegEx(bean.getMetaDataValue());
                } else if (LOCAL_NAME_DISPLAY_ORDER.equals(bean.getMetaDataKey())) {
                    claim.setDisplayOrder(Integer.parseInt(bean.getMetaDataValue()));
                } else if (LOCAL_NAME_CHECKED_ATTR.equals(bean.getMetaDataKey())) {
                    if (Integer.parseInt(bean.getMetaDataValue()) == 1) {
                        claim.setCheckedAttribute(true);
                    }
                } else if (LOCAL_NAME_REQUIRED.equals(bean.getMetaDataKey())) {
                    if (Integer.parseInt(bean.getMetaDataValue()) == 1) {
                        claim.setRequired(true);
                    }
                } else if (LOCAL_NAME_SUPPORTED_BY_DEFAULT.equals(bean.getMetaDataKey())) {
                    if (Integer.parseInt(bean.getMetaDataValue()) == 1) {
                        claim.setSupportedByDefault(true);
                    }
                } else if (LOCAL_NAME_READ_ONLY.equals(bean.getMetaDataKey())) {
                    if (Integer.parseInt(bean.getMetaDataValue()) == 1) {
                        claim.setReadOnly(true);
                    }
                } else {
                    customMetaData.put(bean.getMetaDataKey(), bean.getMetaDataValue());
                }
            }
        }

        claim.setCustomMetaData(customMetaData);
    }


    /**
     * This will initiate the claim update. Updating meta data will take much time because
     * of the string matching and meta data count may vary from claim to claim, it's better
     * to delete and do insert.
     *
     * @param dbConnection database connection
     * @param claimMapping claim mapping
     * @throws UserStoreException
     */
    protected void updateClaimMapping(Connection dbConnection, ClaimMapping claimMapping, int tenantId)
            throws UserStoreException {
        //application mgt
        Claim claim = claimMapping.getClaim();
        this.deleteClaimMapping(dbConnection, claim.getClaimUri(), claim.getDialectURI(), tenantId);
        this.addClaimMapping(dbConnection, claimMapping, tenantId);
    }


    /**
     * Deleting claim mappings.
     *
     * @param claimUri   uri of the claim to be deleted
     * @param dialectUri dialect uri
     * @throws UserStoreException
     */
    protected void deleteClaimMapping(Connection dbConnection, String claimUri, String dialectUri, int tenantId)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean isFinalRow = true;
        try {
            if (dialectUri.equals(UserCoreConstants.DEFAULT_CARBON_DIALECT)) {
                prepStmt = dbConnection
                        .prepareStatement(ClaimDBConstants.GET_CLAIMS_FOR_DIALECTT_SQL,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
                prepStmt.setString(1, dialectUri);
                prepStmt.setInt(2, tenantId);
                prepStmt.setInt(3, tenantId);
                ResultSet rs = prepStmt.executeQuery();
                while (rs.next()) {
                    if (rs.getRow() > 1) {
                        isFinalRow = false;
                        break;
                    }
                }
                if (isFinalRow) {
                    throw new UserStoreException("Cannot delete all claim mappings");
                }
            }

            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.DELETE_CLAIM_SQL);
            prepStmt.setString(1, claimUri);
            prepStmt.setString(2, dialectUri);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();//do not need

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when deleting claim mapping " + claimUri + "Dialect- " +
                    dialectUri, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Get dialect identifier from the database by using the dialect uri.
     *
     * @param dbConnection database connection
     * @param uri          dialect uri
     * @return dialect identifier
     */
    protected int getDialectId(Connection dbConnection, String uri, int tenantId) throws UserStoreException {
        int dialectId = -1;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_DIALECT_ID_SQL);
            prepStmt.setString(1, uri);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                dialectId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when getting dialect Id from dialect " + uri, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
        return dialectId;
    }


    /**
     * Insert dialect to the database.
     *
     * @param dbConnection database connection
     * @param uri          dialect uri
     * @return inserted dialect identifier
     * @throws UserStoreException
     */
    protected int addDialect(Connection dbConnection, String uri, int tenantId) throws UserStoreException {
        int dialectId = -1;
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.ADD_DIALECT_SQL);
            prepStmt.setString(1, uri);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();
            dialectId = getDialectId(dbConnection, uri, tenantId);
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when adding dialect - " + uri, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return dialectId;
    }


    /**
     * Get claim identifier from the database by using the claim uri.
     *
     * @param dbConnection database connection
     * @param uri          claim uri
     * @return claim identifier
     */
    protected int getClaimId(Connection dbConnection, String uri, int tenantId) throws UserStoreException {
        int claimId = -1;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_CLAIM_ID_SQL);
            prepStmt.setString(1, uri);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                claimId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when getting claim Id from claim- " + uri, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
        return claimId;
    }

    /**
     * Wrapper method for the getClaimId which will check the claim exist or not.
     *
     * @param dbConnection database connection
     * @param uri          uri of the claim
     * @return boolean value for claim exist or no
     * @throws UserStoreException
     */
    protected boolean isClaimExists(Connection dbConnection, String uri, int tenantId) throws UserStoreException {
        int claimId = getClaimId(dbConnection, uri, tenantId);
        return claimId != -1;
    }

    /**
     * Wrapper method for the getDialectId which will check the dialect exist or not.
     *
     * @param dbConnection database connection
     * @param uri          uri of the dialect
     * @return boolean value for claim exist or no
     * @throws UserStoreException
     */
    private boolean isDialectExists(Connection dbConnection, String uri, int tenantId) throws UserStoreException {
        int dialectId = getDialectId(dbConnection, uri, tenantId);
        return dialectId != -1;
    }

    /**
     * Add claim to the data base.
     *
     * @param dbConnection database connection
     * @param dialectId    dialect identifier for the particular claim
     * @param claimUri     claim uri which need to be added
     * @return claim identifier
     * @throws UserStoreException
     */
    protected int addClaim(Connection dbConnection, int dialectId, String claimUri, int tenantId) throws UserStoreException {
        int claimId = -1;
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.ADD_CLAIM_SQL);
            prepStmt.setInt(1, dialectId);
            prepStmt.setString(2, claimUri);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();
            claimId = getClaimId(dbConnection, claimUri, tenantId);
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when adding claim - " + claimUri + "tenant: " + tenantId, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return claimId;
    }

    /**
     * Add mapped attributes of the local claim.
     *
     * @param dbConnection          database connection
     * @param claimId               claim identifier of the mapped attributes
     * @param mappedAttributeDomain domain of the mapped attribute
     * @param mappedAttribute       mapped attribute
     * @throws UserStoreException
     */
    protected int addMappedAttribute(Connection dbConnection, int claimId, String mappedAttributeDomain, String
            mappedAttribute, int tenantId) throws UserStoreException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.ADD_CLAIM_MAPPED_ATTRIBUTE_SQL);
            prepStmt.setInt(1, claimId);
            prepStmt.setString(2, mappedAttributeDomain);
            prepStmt.setString(3, mappedAttribute);
            prepStmt.setInt(4, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when adding mapped attribute - " + mappedAttribute +
                    "Domain: " + mappedAttributeDomain, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return claimId;
    }

    /**
     * Add claim relation between local claims and the additional claims.
     *
     * @param dbConnection      database connection
     * @param localClaimId      local claim(wso2 claim) identifier
     * @param additionalClaimId additional claim identifier
     * @throws UserStoreException
     */
    protected void addClaimRelation(Connection dbConnection, int localClaimId, int additionalClaimId, int tenantId)
            throws UserStoreException {
        log.info("localClaimId=" + localClaimId + " additionalClaimId=" + additionalClaimId);
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.ADD_CLAIM_RELATION_SQL);
            prepStmt.setInt(1, localClaimId);
            prepStmt.setInt(2, additionalClaimId);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when adding claim relation between local claim and " +
                    "additional claim", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }


    /**
     * Add meta data of the claim.
     *
     * @param dbConnection database connection
     * @param claimId      claim identifier for each meta data
     * @param key          meta data key
     * @param value        meta data value
     * @throws UserStoreException
     */
    protected void addMetaData(Connection dbConnection, int claimId, String key, String value, int tenantId) throws
            UserStoreException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.ADD_CLAIM_META_DATA_SQL);
            prepStmt.setInt(1, claimId);
            prepStmt.setString(2, key);
            prepStmt.setString(3, value);
            prepStmt.setInt(4, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when adding meta data, key: "+ key + ", value: " + value , e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, null, prepStmt);
        }
    }

    /**
     * Remove the relationship between local claim and the additional claim
     *
     * @param claimId claim identifier for the local claim or additional claim
     * @throws UserStoreException
     */
    protected void deleteClaimRelation(int claimId, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.DELETE_CLAIM_RELATION_SQL);
            prepStmt.setInt(1, claimId);
            prepStmt.setInt(2, claimId);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when deleting claim relation- ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * Delete meta data for a specific claim
     *
     * @param claimId claim identifier for the meta data
     * @throws UserStoreException
     */

    protected void deleteMetaData(int claimId, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.DELETE_CLAIM_META_DATA_SQL);
            prepStmt.setInt(1, claimId);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when deleting claim meta data.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * Delete mapped attributes for a specific local claim.
     *
     * @param claimId claim identifier for the mapped attributes
     * @throws UserStoreException
     */
    protected void deleteMappedAttribute(int claimId, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.DELETE_MAPPED_ATTRIBUTE_SQL);
            prepStmt.setInt(1, claimId);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

            dbConnection.commit();
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when deleting mapped attribute. ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * Return mapped attribute for the claims in wso2 dialect.
     * Otherwise returns the mapped attribute of the mapped wso2 dialect claim
     *
     * @param claimURI
     * @param tenantId
     * @return
     * @throws UserStoreException
     */
    public String getMappedAttribute(String claimURI, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_MAPPED_ATTRIBUTE);
            prepStmt.setString(1, claimURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, claimURI);
            prepStmt.setInt(4, tenantId);
            resultSet = prepStmt.executeQuery();

            dbConnection.commit();
            if (resultSet.next()){
                return resultSet.getString("IDN_MAPPED_ATTRIBUTE");
            }
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when retrieving mapped attribute for " + claimURI, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
        return null;
    }

    /**
     * Return mapped attribute for the claims in wso2 dialect.
     * Otherwise returns the mapped attribute of the mapped wso2 dialect claim
     *
     * @param claimURI
     * @param tenantId
     * @param domain
     * @return
     * @throws UserStoreException
     */
    public String getMappedAttribute(String claimURI, int tenantId, String domain) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_MAPPED_ATTRIBUTE_OF_DOMAIN);
            prepStmt.setString(1, claimURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, domain);
            prepStmt.setString(4, claimURI);
            prepStmt.setInt(5, tenantId);
            prepStmt.setString(6, domain);
            resultSet = prepStmt.executeQuery();

            dbConnection.commit();
            if (resultSet.next()){
                return resultSet.getString("IDN_MAPPED_ATTRIBUTE");
            }
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when retrieving mapped attribute for " + claimURI + " of " +
                    domain + " domain", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
        return null;
    }

    public Claim getClaim(String claimURI, int tenantId) throws UserStoreException {
        //TODO: need to check what to do when the dialect is not wso2
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_CLAIM);
            prepStmt.setString(1, claimURI);
            prepStmt.setInt(2, tenantId);
            resultSet = prepStmt.executeQuery();

            dbConnection.commit();
            Claim claim = new Claim();
            Map<String, String> customMetadata = new HashMap<>();
            while (resultSet.next()){
                String key = resultSet.getString("IDN_KEY");
                String value = resultSet.getString("IDN_VALUE");
                if (LOCAL_NAME_DISPLAY_NAME.equals(key)) {
                    claim.setDisplayTag(value);
                } else if (LOCAL_NAME_DESCRIPTION.equals(key)) {
                    claim.setDescription(value);
                } else if (LOCAL_NAME_REG_EX.equals(key)) {
                    claim.setRegEx(value);
                } else if (LOCAL_NAME_DISPLAY_ORDER.equals(key)) {
                    claim.setDisplayOrder(Integer.parseInt(value));
                } else if (LOCAL_NAME_CHECKED_ATTR.equals(key)) {
                    if (Integer.parseInt(key) == 1) {
                        claim.setCheckedAttribute(true);
                    }
                } else if (LOCAL_NAME_REQUIRED.equals(key)) {
                    if (Integer.parseInt(value) == 1) {
                        claim.setRequired(true);
                    }
                } else if (LOCAL_NAME_SUPPORTED_BY_DEFAULT.equals(key)) {
                    if (Integer.parseInt(value) == 1) {
                        claim.setSupportedByDefault(true);
                    }
                } else if (LOCAL_NAME_READ_ONLY.equals(key)) {
                    if (Integer.parseInt(value) == 1) {
                        claim.setReadOnly(true);
                    }
                } else {
                    customMetadata.put(key, value);
                }
            }
            claim.setCustomMetaData(customMetadata);
            return claim;
        } catch (SQLException e) {
            throw new UserStoreException("Database Error when retrieving mapped attribute for " + claimURI, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

    public ClaimMapping getClaimMapping(String claimURI, int tenantId) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        ClaimMapping claimMapping = new ClaimMapping();
        try {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
            dbConnection.setAutoCommit(false);
            prepStmt = dbConnection.prepareStatement(ClaimDBConstants.GET_MAPPED_ATTRIBUTES);
            prepStmt.setString(1, claimURI);
            prepStmt.setInt(2, tenantId);
            resultSet = prepStmt.executeQuery();

            dbConnection.commit();
            String dialectURI = null;
            while (resultSet.next()) {
                dialectURI = resultSet.getString("IDN_DIALECT_URI");
                String mappedAttribute = resultSet.getString("IDN_MAPPED_ATTRIBUTE");
                String mappedAttributeDomain = resultSet.getString("IDN_MAPPED_ATTRIBUTE_DOMAIN");

                claimMapping.setMappedAttribute(mappedAttributeDomain, mappedAttribute);
            }

            claimMapping.setClaim(getClaim(claimURI, tenantId));
            claimMapping.getClaim().setDialectURI(dialectURI);

        } catch (SQLException e) {
            throw new UserStoreException("Database Error when retrieving claim mapping. ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
        return null;
    }
}
