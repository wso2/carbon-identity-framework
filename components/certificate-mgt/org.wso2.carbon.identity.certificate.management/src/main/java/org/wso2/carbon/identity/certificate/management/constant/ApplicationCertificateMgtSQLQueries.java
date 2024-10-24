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
 * This class contains SQL queries for Application Certificate management service.
 * This queries support using auto-incremented IDs for certificate management operations in application-mgt.
 *
 * @deprecated It is recommended to use {@link CertificateMgtSQLConstants.Query}, which supports operations with UUID.
 */
@Deprecated
public class ApplicationCertificateMgtSQLQueries {

    private ApplicationCertificateMgtSQLQueries() {
    }

    @Deprecated
    public static final String GET_CERTIFICATE_BY_ID = "SELECT NAME, CERTIFICATE_IN_PEM FROM IDN_CERTIFICATE " +
            "WHERE ID = ? AND TENANT_ID = ?";
    @Deprecated
    public static final String GET_CERTIFICATE_BY_NAME = "SELECT ID, CERTIFICATE_IN_PEM FROM IDN_CERTIFICATE " +
            "WHERE NAME = ? AND TENANT_ID = ?";
    @Deprecated
    public static final String UPDATE_CERTIFICATE_CONTENT = "UPDATE IDN_CERTIFICATE SET CERTIFICATE_IN_PEM = ? " +
            "WHERE ID = ? AND TENANT_ID = ?";
    @Deprecated
    public static final String DELETE_CERTIFICATE = "DELETE FROM IDN_CERTIFICATE WHERE ID = ? AND TENANT_ID = ?";
}
