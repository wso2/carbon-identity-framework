/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt.util;

/**
 * Holds the SQL queries and related constants
 */
public class SQLConstants {

    private SQLConstants() {
    }

    // IDN_CLAIM_DIALECT => ID, DIALECT_URI, TENANT_ID
    // IDN_CLAIM => ID, CLAIM_URI, TENANT_ID
    // IDN_CLAIM_MAPPING => ID, LOCAL_CLAIM_ID, EXT_CLAIM_ID, TENANT_ID
    // IDN_CLAIM_MAPPED_ATTRIBUTE => ID, LOCAL_CLAIM_ID, USER_STORE_DOMAIN_NAME, ATTRIBUTE_NAME, TENANT_ID
    // IDN_CLAIM_PROPERTY => ID, LOCAL_CLAIM_ID, PROPERTY_NAME, PROPERTY_VALUE, TENANT_ID


    public static final String DIALECT_URI_COLUMN = "DIALECT_URI";
    public static final String CLAIM_URI_COLUMN = "CLAIM_URI";
    public static final String ID_COLUMN = "ID";
    public static final String USER_STORE_DOMAIN_NAME_COLUMN = "USER_STORE_DOMAIN_NAME";
    public static final String ATTRIBUTE_NAME_COLUMN = "ATTRIBUTE_NAME";
    public static final String PROPERTY_NAME_COLUMN = "PROPERTY_NAME";
    public static final String PROPERTY_VALUE_COLUMN = "PROPERTY_VALUE";
    public static final String LOCAL_CLAIM_ID_COLUMN = "LOCAL_CLAIM_ID";

    // Claim Dialect SQLs
    public static final String GET_CLAIM_DIALECTS = "SELECT DIALECT_URI FROM IDN_CLAIM_DIALECT WHERE TENANT_ID=?";
    public static final String ADD_CLAIM_DIALECT = "INSERT INTO IDN_CLAIM_DIALECT (DIALECT_URI, TENANT_ID) " +
            "VALUES (?, ?)";
    public static final String UPDATE_CLAIM_DIALECT = "UPDATE IDN_CLAIM_DIALECT SET DIALECT_URI=? WHERE " +
            "DIALECT_URI=? AND TENANT_ID=?";
    public static final String REMOVE_CLAIM_DIALECT = "DELETE FROM IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND " +
            "TENANT_ID=?";

    // Claim SQLs
    public static final String GET_CLAIMS_BY_DIALECT = "SELECT ID, CLAIM_URI FROM IDN_CLAIM WHERE DIALECT_ID=(SELECT " +
            "ID FROM IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?) AND TENANT_ID=?";
    public static final String GET_CLAIM_ID = "SELECT ID FROM IDN_CLAIM WHERE DIALECT_ID=(SELECT ID FROM " +
            "IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?) AND CLAIM_URI=? AND TENANT_ID=?";
    public static final String ADD_CLAIM = "INSERT INTO IDN_CLAIM (DIALECT_ID, CLAIM_URI, TENANT_ID) VALUES ((SELECT " +
            "ID FROM IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?), ?, ?)";
    public static final String REMOVE_CLAIM = "DELETE FROM IDN_CLAIM WHERE DIALECT_ID=(SELECT ID FROM " +
            "IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?) AND CLAIM_URI=? AND TENANT_ID=?";

    // Local Claim Attributes SQLs
    public static final String GET_MAPPED_ATTRIBUTES = "SELECT USER_STORE_DOMAIN_NAME, ATTRIBUTE_NAME FROM " +
            "IDN_CLAIM_MAPPED_ATTRIBUTE WHERE LOCAL_CLAIM_ID=? AND TENANT_ID=?";
    public static final String ADD_CLAIM_MAPPED_ATTRIBUTE = "INSERT INTO IDN_CLAIM_MAPPED_ATTRIBUTE (LOCAL_CLAIM_ID, " +
            "USER_STORE_DOMAIN_NAME, ATTRIBUTE_NAME, TENANT_ID) VALUES (?, ?, ?, ?)";
    public static final String DELETE_CLAIM_MAPPED_ATTRIBUTE = "DELETE FROM IDN_CLAIM_MAPPED_ATTRIBUTE WHERE " +
            "LOCAL_CLAIM_ID=? AND TENANT_ID=?";

    // Claim Properties SQLs
    public static final String GET_CLAIM_PROPERTIES = "SELECT PROPERTY_NAME, PROPERTY_VALUE FROM IDN_CLAIM_PROPERTY " +
            "WHERE LOCAL_CLAIM_ID=? AND TENANT_ID=?";
    public static final String ADD_CLAIM_PROPERTY = "INSERT INTO IDN_CLAIM_PROPERTY (LOCAL_CLAIM_ID, PROPERTY_NAME, " +
            "PROPERTY_VALUE, TENANT_ID) VALUES (?, ?, ?, ?)";
    public static final String DELETE_CLAIM_PROPERTY = "DELETE FROM IDN_CLAIM_PROPERTY WHERE LOCAL_CLAIM_ID=? AND " +
            "TENANT_ID=?";

    // External Claim Mapping SQLs
    public static final String GET_CLAIM_MAPPING = "SELECT CLAIM_URI FROM IDN_CLAIM WHERE ID=(SELECT " +
            "MAPPED_LOCAL_CLAIM_ID FROM IDN_CLAIM_MAPPING WHERE EXT_CLAIM_ID=? AND TENANT_ID=?) AND TENANT_ID=?";
    public static final String ADD_CLAIM_MAPPING = "INSERT INTO IDN_CLAIM_MAPPING (MAPPED_LOCAL_CLAIM_ID, " +
            "EXT_CLAIM_ID, TENANT_ID) VALUES (?, ?, ?)";
    public static final String UPDATE_CLAIM_MAPPING = "UPDATE IDN_CLAIM_MAPPING SET MAPPED_LOCAL_CLAIM_ID=? WHERE " +
            "EXT_CLAIM_ID=? AND TENANT_ID=?";
    public static final String IS_CLAIM_MAPPING = "SELECT MAPPED_LOCAL_CLAIM_ID FROM IDN_CLAIM_MAPPING WHERE " +
            "MAPPED_LOCAL_CLAIM_ID=(SELECT ID FROM IDN_CLAIM WHERE DIALECT_ID=(SELECT ID FROM IDN_CLAIM_DIALECT WHERE" +
            " DIALECT_URI=? AND TENANT_ID=?) AND CLAIM_URI=? AND TENANT_ID=?) AND TENANT_ID=?";
}
