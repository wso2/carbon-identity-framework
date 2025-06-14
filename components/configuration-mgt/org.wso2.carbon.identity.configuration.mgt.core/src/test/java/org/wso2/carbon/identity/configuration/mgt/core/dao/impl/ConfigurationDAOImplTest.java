/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.configuration.mgt.core.dao.impl;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.ExecuteCallable;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.QueryFilter;
import org.wso2.carbon.database.utils.jdbc.RowMapper;
import org.wso2.carbon.database.utils.jdbc.Template;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementServerException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * Test class for ConfigurationDAOImpl.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/config/h2.sql"})
public class ConfigurationDAOImplTest {

    private final ConfigurationDAOImpl configurationDAO = new ConfigurationDAOImpl();

    private static final String RESOURCE_TYPE_ID = "test-resource-type-id";
    private static final String RESOURCE_TYPE_NAME = "test-resource-type-name";
    private static final String RESOURCE_ID = "test-resource-id";
    private static final String RESOURCE_NAME = "test-resource-name";
    private static final String FILE_ID = "test-file-id";
    private static final String FILE_CONTENT = "This is a test file content.";
    private static final String TENANT_DOMAIN = "test-tenant-domain";
    private static final int TENANT_ID = 1;

    private MockedStatic<IdentityTenantUtil> tenantUtilMockedStatic;

    @BeforeClass
    public void init() {

        tenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        tenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                .thenReturn(TENANT_ID);
        tenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID))
                .thenReturn(TENANT_DOMAIN);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TENANT_DOMAIN);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(TENANT_ID);
    }

    @AfterClass
    public void cleanup() {

        tenantUtilMockedStatic.close();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
    }

    @Test(description = "Test for addResourceType method")
    public void testAddResource() throws Exception {

        // add a test resource type.
        ResourceType resourceType =
                new ResourceType(RESOURCE_TYPE_NAME, RESOURCE_TYPE_ID, "test-resource-type-description");
        configurationDAO.addResourceType(resourceType);

        // Add a new resource.
        Resource resource = new Resource(RESOURCE_NAME, RESOURCE_TYPE_NAME);
        resource.setResourceId(RESOURCE_ID);
        resource.setTenantDomain("test-tenant-domain");
        resource.setHasFile(true);
        ResourceFile resourceFile = new ResourceFile(FILE_ID, "test-file-name");
        resourceFile.setInputStream(new ByteArrayInputStream(FILE_CONTENT.getBytes(StandardCharsets.UTF_8)));
        resource.setFiles(new ArrayList<ResourceFile>() {{
            add(resourceFile);
        }});
        configurationDAO.addResource(resource);

        Resource dbResource = configurationDAO.getResourceById(RESOURCE_ID);
        assertEquals(FILE_ID, dbResource.getFiles().get(0).getId());
    }

    @Test(description = "Test for getFileById method", dependsOnMethods = "testAddResource")
    public void testGetFileById() throws Exception {

        // Test the successful retrieval of a file by its ID.
        InputStream dbResourceFile = configurationDAO.getFileById(RESOURCE_TYPE_NAME, RESOURCE_NAME, FILE_ID);
        String result = new BufferedReader(new InputStreamReader(dbResourceFile, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        assertEquals(FILE_CONTENT, result);
        try (MockedStatic<JdbcUtils> mockedStatic = mockStatic(JdbcUtils.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(JdbcUtils::isPostgreSQLDB).thenReturn(true);
            dbResourceFile = configurationDAO.getFileById(RESOURCE_TYPE_NAME, RESOURCE_NAME, FILE_ID);
            result = new BufferedReader(new InputStreamReader(dbResourceFile, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(FILE_CONTENT, result);
        }

        // Test the retrieval of a file with an invalid ID.
        dbResourceFile = configurationDAO.getFileById(RESOURCE_TYPE_NAME, RESOURCE_NAME, "wrong-id");
        assertNull(dbResourceFile);

        // Test the exception scenarios.
        try (MockedConstruction<Template> mockedConstruction = mockConstruction(Template.class, (mock, context) -> {
            when(mock.fetchSingleRecord(anyString(), any(RowMapper.class), any(QueryFilter.class))).thenThrow(
                    DataAccessException.class);
        })) {
            assertThrows(ConfigurationManagementServerException.class,
                    () -> configurationDAO.getFileById(RESOURCE_TYPE_NAME, RESOURCE_NAME, FILE_ID));
        }
        try (MockedStatic<JdbcUtils> mockedStatic = mockStatic(JdbcUtils.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(JdbcUtils::isPostgreSQLDB).thenThrow(DataAccessException.class);
            assertThrows(ConfigurationManagementServerException.class,
                    () -> configurationDAO.getFileById(RESOURCE_TYPE_NAME, RESOURCE_NAME, FILE_ID));
        }
        try (MockedStatic<JdbcUtils> mockedStatic = mockStatic(JdbcUtils.class, CALLS_REAL_METHODS)) {
            Blob blob = mock(Blob.class);
            when(blob.getBinaryStream()).thenThrow(SQLException.class);
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            mockedStatic.when(JdbcUtils::getNewTemplate).thenReturn(jdbcTemplate);
            when(jdbcTemplate.withTransaction(any(ExecuteCallable.class))).thenReturn(blob);
            assertThrows(ConfigurationManagementServerException.class,
                    () -> configurationDAO.getFileById(RESOURCE_TYPE_NAME, RESOURCE_NAME, FILE_ID));
        }
    }

    @Test(description = "Test for getFiles method", dependsOnMethods = "testAddResource")
    public void testGetFiles() throws Exception {

        // Test the successful retrieval of files for a resource.
        List<ResourceFile> resourceFiles = configurationDAO.getFiles(RESOURCE_ID, RESOURCE_TYPE_NAME, RESOURCE_NAME);
        assertEquals(1, resourceFiles.size());
        assertEquals(FILE_ID, resourceFiles.get(0).getId());

        // Test the exception scenarios.
        try (MockedConstruction<Template> mockedConstruction = mockConstruction(Template.class, (mock, context) -> {
            when(mock.executeQuery(anyString(), any(RowMapper.class), any(QueryFilter.class))).thenThrow(
                    DataAccessException.class);
        })) {
            assertThrows(ConfigurationManagementServerException.class,
                    () -> configurationDAO.getFiles(RESOURCE_ID, RESOURCE_TYPE_NAME, RESOURCE_NAME));
        }
    }

    @Test(description = "Test deleteResourceByName method", dependsOnMethods = {"testAddResource", "testGetFileById",
            "testGetFiles"})
    public void testDeleteResourceByName() throws Exception {

        // Successfully delete a resource by its name.
        configurationDAO.deleteResourceByName(TENANT_ID, RESOURCE_TYPE_ID, RESOURCE_NAME);
        assertNull(configurationDAO.getResourceById(RESOURCE_ID));

        // Test the exception scenarios.
        try (MockedConstruction<Template> mockedConstruction = mockConstruction(Template.class, (mock, context) -> {
            doThrow(DataAccessException.class).when(mock).executeUpdate(anyString(), any(QueryFilter.class));
        })) {
            assertThrows(ConfigurationManagementServerException.class,
                    () -> configurationDAO.deleteResourceByName(TENANT_ID, RESOURCE_TYPE_ID, RESOURCE_NAME));
        }
    }
}
