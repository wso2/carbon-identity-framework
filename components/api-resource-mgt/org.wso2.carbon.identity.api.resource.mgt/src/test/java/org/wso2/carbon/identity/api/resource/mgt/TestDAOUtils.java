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

package org.wso2.carbon.identity.api.resource.mgt;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.APIResourceManagementDAOImpl;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test DB Utils.
 */
public class TestDAOUtils {

    public static final String TEST_TYPE_1 = "TEST_TYPE_1";
    public static final String TEST_TYPE_2 = "TEST_TYPE_2";
    public static final String TEST_TYPE_3 = "TEST_TYPE_3";
    public static final int TEST_TENANT_ID = -1234;
    public static final String TEST_TENANT_DOMAIN = "carbon.super";
    public static final String TEST_TYPE_INVALID = "TEST_TYPE_INVALID";

    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String TEST_DB_NAME = "TEST_IAM_RAR_DATABASE";

    /**
     * Generates default authorization details types list for tests.
     *
     * @return a list of {@link AuthorizationDetailsType}
     */
    public static List<AuthorizationDetailsType> getAuthorizationDetailsTypes() {

        final Map<String, Object> properties = new HashMap<>();
        properties.put("type", "string");
        properties.put("enum", Collections.singletonList(TEST_TYPE_1));

        final Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", Collections.singletonList("type"));
        schema.put("properties", properties);

        final AuthorizationDetailsType authorizationDetailsType1 = new AuthorizationDetailsType();
        authorizationDetailsType1.setType(TEST_TYPE_1);
        authorizationDetailsType1.setSchema(schema);
        final AuthorizationDetailsType authorizationDetailsType2 = new AuthorizationDetailsType();
        authorizationDetailsType2.setType(TEST_TYPE_2);

        return Arrays.asList(authorizationDetailsType1, authorizationDetailsType2);
    }

    /**
     * Checks if provided type is available in the provided authorization details types list.
     *
     * @param type                      type to be checked
     * @param authorizationDetailsTypes a list of authorization details types
     * @return true if type is available in the authorization details types list
     */
    public static boolean isTypeExists(String type, List<AuthorizationDetailsType> authorizationDetailsTypes) {

        return authorizationDetailsTypes.stream().anyMatch(detailsType -> type.equals(detailsType.getType()));
    }

    /**
     * Checks and returns if provided type is available in the provided authorization details types list.
     *
     * @param type                      type to be checked
     * @param authorizationDetailsTypes a list of authorization details types
     * @return a {@link AuthorizationDetailsType} if type is available in the authorization details types list,
     * or else null
     */
    public static AuthorizationDetailsType getByType(String type,
                                                     List<AuthorizationDetailsType> authorizationDetailsTypes) {

        return authorizationDetailsTypes.stream()
                .filter(detailsType -> type.equals(detailsType.getType()))
                .findAny()
                .orElse(null);
    }

    /**
     * Close H2 database.
     *
     * @throws Exception Error when closing H2 database.
     */
    public static void closeDataSource() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(TEST_DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Add API resource to the database.
     *
     * @param namePostFix          Postfix to be appended to each API resource and scope information.
     * @param connection           Database connection.
     * @param tenantId             Tenant ID.
     * @param identityDatabaseUtil Mocked IdentityDatabaseUtil.
     * @return API resource.
     * @throws APIResourceMgtException Error when adding API resource.
     */
    public static APIResource addAPIResourceToDB(String namePostFix, Connection connection, int tenantId,
                                                 MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil)
            throws APIResourceMgtException {

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(ArgumentMatchers.any(Connection.class)))
                .thenAnswer((Answer<Void>) invocation -> {
                    connection.commit();
                    return null;
                });
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(ArgumentMatchers.anyBoolean()))
                .thenReturn(connection);

        return new APIResourceManagementDAOImpl().addAPIResource(createAPIResource(namePostFix), tenantId);
    }

    /**
     * Create API resource with the given postfix.
     *
     * @param postFix Postfix to be appended to each API resource and scope information.
     * @return API resource.
     */
    public static APIResource createAPIResource(String postFix) {

        List<Scope> scopes = new ArrayList<>();
        scopes.add(createScope("testScopeOne " + postFix));
        scopes.add(createScope("testScopeTwo " + postFix));

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + postFix)
                .identifier("testAPIResource identifier " + postFix)
                .description("testAPIResource description " + postFix)
                .type("BUSINESS")
                .requiresAuthorization(true)
                .scopes(scopes);

        return apiResourceBuilder.build();
    }

    /**
     * Create scope with the given name.
     *
     * @param name Name of the scope.
     * @return Scope.
     */
    public static Scope createScope(String name) {

        Scope.ScopeBuilder scopeBuilder = new Scope.ScopeBuilder()
                .name(name)
                .displayName("displayName " + name)
                .description("description " + name);
        return scopeBuilder.build();
    }

    public static void closeMockedStatic(MockedStatic<?> mockedStatic) {

        if (mockedStatic != null && !mockedStatic.isClosed()) {
            mockedStatic.close();
        }
    }

    /**
     * Initiate H2 database.
     *
     * @param scriptPath Path to the database script.
     * @throws SQLException Error when initiating H2 database.
     */
    public static void initializeDataSource(String scriptPath) throws SQLException {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:" + TEST_DB_NAME);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(TEST_DB_NAME, dataSource);
    }

    public static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(TEST_DB_NAME) != null) {
            return dataSourceMap.get(TEST_DB_NAME).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + TEST_DB_NAME);
    }

    /**
     * Get the path to the database script.
     *
     * @return Path to the database script.
     */
    public static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        return null;
    }
}
