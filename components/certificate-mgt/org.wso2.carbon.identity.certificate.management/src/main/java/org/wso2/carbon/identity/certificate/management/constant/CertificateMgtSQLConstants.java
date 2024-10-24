/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.certificate.management.constant;

/**
 * This class contains SQL queries for Certificate management service.
 */
public class CertificateMgtSQLConstants {

    private CertificateMgtSQLConstants() {
    }

    /**
     * Column Names.
     */
    public static class Column {

        public static final String ID = "ID";
        public static final String NAME = "NAME";
        public static final String CERTIFICATE_IN_PEM = "CERTIFICATE_IN_PEM";

        private Column() {
        }
    }

    /**
     * Queries.
     */
    public static class Query {

        public static final String ADD_CERTIFICATE = "INSERT INTO IDN_CERTIFICATE(UUID, NAME, CERTIFICATE_IN_PEM, " +
                "TENANT_ID) VALUES(?, ?, ?, ?)";
        public static final String GET_CERTIFICATE_BY_ID = "SELECT NAME, CERTIFICATE_IN_PEM FROM IDN_CERTIFICATE " +
                "WHERE UUID = ? AND TENANT_ID = ?";
        public static final String UPDATE_CERTIFICATE_CONTENT = "UPDATE IDN_CERTIFICATE SET CERTIFICATE_IN_PEM = ? " +
                "WHERE UUID = ? AND TENANT_ID = ?";
        public static final String DELETE_CERTIFICATE = "DELETE FROM IDN_CERTIFICATE WHERE UUID = ? AND TENANT_ID = ?";

        private Query() {
        }
    }
}
