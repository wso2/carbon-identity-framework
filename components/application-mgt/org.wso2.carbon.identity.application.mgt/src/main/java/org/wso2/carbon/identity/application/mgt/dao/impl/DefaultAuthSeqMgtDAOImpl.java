/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.mgt.dao.DefaultAuthSeqMgtDAO;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtServerException;
import org.wso2.carbon.identity.application.mgt.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.ADD_DEFAULT_SEQ;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.DELETE_DEFAULT_SEQ;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_DEFAULT_SEQ;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_DEFAULT_SEQ_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_DEFAULT_SEQ_INFO;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_DEFAULT_SEQ;

/**
 * This class access the SP_DEFAULT_AUTH_SEQ to manage tenant default authentication sequences.
 */
public class DefaultAuthSeqMgtDAOImpl implements DefaultAuthSeqMgtDAO {

    public DefaultAuthSeqMgtDAOImpl() {
    }


    @Override
    public void createDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            doCreateDefaultAuthSeq(sequence, tenantDomain, jdbcTemplate);
        } catch (DataAccessException e) {
            throw new DefaultAuthSeqMgtServerException("Error while creating default authentication sequence in tenant: " +
                    tenantDomain, e);
        }
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeq(String tenantDomain)
            throws DefaultAuthSeqMgtServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return doGetDefaultAuthSeq(tenantDomain, jdbcTemplate);
        } catch (DataAccessException e) {
            throw new DefaultAuthSeqMgtServerException("Error while retrieving default authentication sequence in tenant: " +
                    tenantDomain, e);
        }
    }

    @Override
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo(String tenantDomain)
            throws DefaultAuthSeqMgtServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return doGetDefaultAuthenticationSeqInfo(tenantDomain, jdbcTemplate);
        } catch (DataAccessException e) {
            throw new DefaultAuthSeqMgtServerException("Error while retrieving default authentication sequence info " +
                    "in tenant: " + tenantDomain, e);
        }
    }

    @Override
    public boolean isDefaultAuthSeqExists(String tenantDomain) throws DefaultAuthSeqMgtServerException {

        try {
            int sequenceID = doGetSequenceID(tenantDomain, JdbcUtils.getNewTemplate());
            if (sequenceID != 0) {
                return true;
            }
        } catch (DataAccessException e) {
            throw new DefaultAuthSeqMgtServerException("Could not check existence of default authentication sequence in " +
                    "tenant: " + tenantDomain, e);
        }
        return false;
    }

    @Override
    public void deleteDefaultAuthenticationSeq(String tenantDomain) throws DefaultAuthSeqMgtServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            doDeleteDefaultAuthSeq(tenantDomain, jdbcTemplate);
        } catch (DataAccessException e) {
            throw new DefaultAuthSeqMgtServerException("Error while deleting default authentication sequence in tenant: " +
                    tenantDomain, e);
        }
    }

    @Override
    public void updateDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            doUpdateDefaultAuthSeq(sequence, tenantDomain, jdbcTemplate);
        } catch (DataAccessException e) {
            throw new DefaultAuthSeqMgtServerException("Error while deleting default authentication sequence in tenant: " +
                    tenantDomain, e);
        }
    }

    private void doCreateDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain,
                                 JdbcTemplate jdbcTemplate) throws DataAccessException {

        jdbcTemplate.executeInsert(ADD_DEFAULT_SEQ, (preparedStatement -> {
            preparedStatement.setString(1, sequence.getName());
            try {
                setBlobValue(sequence.getContent(), preparedStatement, 2);
            } catch (IOException e) {
                throw new SQLException("Could not set default authentication sequence as a Blob.", e);
            }
            preparedStatement.setInt(3, getTenantID(tenantDomain));
        }), null, false);
    }


    private DefaultAuthenticationSequence doGetDefaultAuthSeq(String tenantDomain, JdbcTemplate jdbcTemplate)
            throws DataAccessException {

        return jdbcTemplate.fetchSingleRecord(GET_DEFAULT_SEQ,
                (resultSet, rowNumber) -> {
                    DefaultAuthenticationSequence sequence = new DefaultAuthenticationSequence();
                    sequence.setName(resultSet.getString(1));
                    try {
                        byte[] requestBytes = resultSet.getBytes(2);
                        ByteArrayInputStream bais = new ByteArrayInputStream(requestBytes);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Object objectRead = ois.readObject();
                        if (objectRead instanceof LocalAndOutboundAuthenticationConfig) {
                            sequence.setContent((LocalAndOutboundAuthenticationConfig) objectRead);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        throw new SQLException("Could not get content of default authentication sequence as a " +
                                "Blob.", e);
                    }
                    return sequence;
                },
                (PreparedStatement preparedStatement) ->
                        preparedStatement.setInt(1, getTenantID(tenantDomain)));
    }

    private DefaultAuthenticationSequence doGetDefaultAuthenticationSeqInfo(String tenantDomain,
                                                                            JdbcTemplate jdbcTemplate)
            throws DataAccessException {

        return jdbcTemplate.fetchSingleRecord(GET_DEFAULT_SEQ_INFO,
                (resultSet, rowNumber) -> {
                    DefaultAuthenticationSequence sequence = new DefaultAuthenticationSequence();
                    sequence.setName(resultSet.getString(1));
                    return sequence;
                },
                (PreparedStatement preparedStatement) ->
                        preparedStatement.setInt(1, getTenantID(tenantDomain)));
    }

    private int doGetSequenceID(String tenantDomain, JdbcTemplate jdbcTemplate) throws DataAccessException {

        int sequenceID = 0;
        String sequenceIDExists;
        sequenceIDExists = jdbcTemplate.fetchSingleRecord(GET_DEFAULT_SEQ_ID,
                (resultSet, rowNumber) -> Integer.toString(resultSet.getInt(1)),
                (PreparedStatement preparedStatement) ->
                        preparedStatement.setInt(1, getTenantID(tenantDomain)));

        if (sequenceIDExists != null) {
            sequenceID = Integer.parseInt(sequenceIDExists);
        }
        return sequenceID;
    }

    private void doUpdateDefaultAuthSeq(DefaultAuthenticationSequence sequence, String tenantDomain,
                                        JdbcTemplate jdbcTemplate) throws DataAccessException {

        jdbcTemplate.executeUpdate(UPDATE_DEFAULT_SEQ,
                preparedStatement -> {
                    preparedStatement.setString(1, sequence.getName());
                    try {
                        setBlobValue(sequence.getContent(), preparedStatement, 2);
                    } catch (IOException e) {
                        throw new SQLException("Could not set default authentication sequence as a Blob.", e);
                    }
                    preparedStatement.setInt(3, getTenantID(tenantDomain));
                });
    }

    private void doDeleteDefaultAuthSeq(String tenantDomain, JdbcTemplate jdbcTemplate) throws DataAccessException {

        jdbcTemplate.executeUpdate(DELETE_DEFAULT_SEQ,
                preparedStatement ->
                    preparedStatement.setInt(1, getTenantID(tenantDomain)));
    }

    /**
     * Set given string as Blob for the given index into the prepared-statement.
     *
     * @param value    string value to be converted to blob
     * @param prepStmt Prepared statement
     * @param index    column index
     * @throws SQLException
     * @throws IOException
     */
    private void setBlobValue(Object value, PreparedStatement prepStmt, int index) throws SQLException,
            IOException {

        if (value != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            prepStmt.setBytes(index, baos.toByteArray());
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }

    private int getTenantID(String tenantDomain) {

        // get logged-in users tenant identifier.
        int tenantID = MultitenantConstants.INVALID_TENANT_ID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }
        return tenantID;
    }
}
