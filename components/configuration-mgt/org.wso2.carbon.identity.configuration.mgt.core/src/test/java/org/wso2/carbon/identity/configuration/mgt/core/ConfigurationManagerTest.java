/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.configuration.mgt.core;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.dao.impl.ConfigurationDAOImpl;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.ComplexCondition;
import org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_NAME1;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_VALUE3_UPDATED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_NAME1;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_TYPE_NAME1;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_TENANT_DOMAIN_ABC;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_TENANT_ID_ABC;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestSQLConstants.REMOVE_CREATED_TIME_COLUMN_H2;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleAttribute1;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleAttribute3;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResource1Add;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResource2Add;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResourceType2Add;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResourceTypeAdd;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleSearchCondition;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.spyConnection;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSamplesPath;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class})
public class ConfigurationManagerTest extends PowerMockTestCase {

    private ConfigurationManager configurationManager;
    private Connection connection;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        mockStatic(IdentityDatabaseUtil.class);
        when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSource);

        connection = TestUtils.getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        prepareConfigs();

        ConfigurationManagerComponentDataHolder.getInstance().setConfigurationManagementEnabled(true);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
    }

    @Test(priority = 1)
    public void testAddResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        ResourceType resourceType = configurationManager.addResourceType(resourceTypeAdd);
        assertNotNull(resourceType.getId(), "Created resource type id cannot be null");
    }

    @Test(priority = 2, expectedExceptions = ConfigurationManagementClientException.class)
    public void testAddDuplicateResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        configurationManager.addResourceType(resourceTypeAdd);
        configurationManager.addResourceType(resourceTypeAdd);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 3)
    public void testReplaceNonExistingResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        ResourceType resourceType = configurationManager.replaceResourceType(resourceTypeAdd);

        assertNotNull(resourceType.getId(), "Created resource id cannot be null");
    }

    @Test(priority = 4)
    public void testReplaceExistingResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        ResourceType resourceTypeCreated = configurationManager.addResourceType(resourceTypeAdd);
        ResourceType resourceTypeReplaced = configurationManager.replaceResourceType(resourceTypeAdd);

        assertEquals("Existing id should be equal to the replaced id", resourceTypeCreated.getId(),
                resourceTypeReplaced.getId());
    }

    @Test(priority = 5, expectedExceptions = ConfigurationManagementClientException.class)
    public void testGetNonExistingResourceType() throws Exception {

        configurationManager.getResourceType(SAMPLE_RESOURCE_TYPE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 6)
    public void testGetExistingResourceType() throws Exception {

        ResourceType resourceTypeCreated = configurationManager.addResourceType(getSampleResourceTypeAdd());
        ResourceType resourceTypeRetrieved = configurationManager.getResourceType(SAMPLE_RESOURCE_TYPE_NAME1);

        assertEquals("Existing id should be equal to the retrieved id", resourceTypeCreated.getId(),
                resourceTypeRetrieved.getId());
    }

    @Test(priority = 7, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteNonExistingResourceType() throws Exception {

        configurationManager.deleteResourceType(SAMPLE_RESOURCE_TYPE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 8, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteExistingResourceType() throws Exception {

        configurationManager.addResourceType(getSampleResourceTypeAdd());
        configurationManager.deleteResourceType(SAMPLE_RESOURCE_TYPE_NAME1);
        configurationManager.getResourceType(SAMPLE_RESOURCE_TYPE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 9)
    public void testAddResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        ResourceAdd resourceTypeAdd = getSampleResource1Add();

        Resource resource = configurationManager.addResource(resourceType.getName(), resourceTypeAdd);
        assertNotNull(resource.getResourceId(), "Created resource type id cannot be null");
    }

    @Test(priority = 10, expectedExceptions = ConfigurationManagementClientException.class)
    public void testAddDuplicateResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        ResourceAdd resourceAdd = getSampleResource1Add();

        configurationManager.addResource(resourceType.getName(), resourceAdd);
        configurationManager.addResource(resourceType.getName(), resourceAdd);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 11)
    public void testReplaceNonExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        ResourceAdd resourceAdd = getSampleResource1Add();

        Resource resource = configurationManager.replaceResource(resourceType.getName(), resourceAdd);

        assertNotNull(resource.getResourceId(), "Created resource id cannot be null");
    }

    @Test(priority = 12)
    public void testReplaceExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        ResourceAdd resourceAdd = getSampleResource1Add();

        Resource resourceCreated = configurationManager.addResource(resourceType.getName(), resourceAdd);
        Resource resourceReplaced = configurationManager.replaceResource(resourceType.getName(), resourceAdd);

        assertNotEquals("Created time should be different from the last updated time",
                resourceReplaced.getCreatedTime(),resourceReplaced.getLastModified());
        assertEquals("Existing id should be equal to the replaced id", resourceCreated.getResourceId(),
                resourceReplaced.getResourceId());
    }

    @Test(priority = 13, expectedExceptions = ConfigurationManagementClientException.class)
    public void testGetNonExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        configurationManager.getResource(resourceType.getName(), SAMPLE_RESOURCE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 14)
    public void testGetExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        Resource resourceCreated = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Resource resourceRetrieved = configurationManager.getResource(resourceType.getName(), SAMPLE_RESOURCE_NAME1);

        assertEquals("Existing id should be equal to the retrieved id", resourceCreated.getResourceId(),
                resourceRetrieved.getResourceId());
    }

    @Test(priority = 15, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteNonExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        configurationManager.deleteResource(resourceType.getName(), SAMPLE_RESOURCE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 16, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        configurationManager.deleteResource(resourceType.getName(), SAMPLE_RESOURCE_TYPE_NAME1);
        configurationManager.getResource(resourceType.getName(), SAMPLE_RESOURCE_TYPE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 17)
    public void testAddAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        Attribute retrievedAttribute = configurationManager.addAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);
        assertNotNull(retrievedAttribute.getAttributeId(), "Created resource type id cannot be null");
    }

    @Test(priority = 18, expectedExceptions = ConfigurationManagementClientException.class)
    public void testAddDuplicateAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        configurationManager.addAttribute(resourceType.getName(), resource.getResourceName(), attribute);
        configurationManager.addAttribute(resourceType.getName(), resource.getResourceName(), attribute);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 19)
    public void testReplaceNonExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        Attribute retrievedAttribute = configurationManager.replaceAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);
        assertNotNull(retrievedAttribute.getAttributeId(), "Created resource id cannot be null");
    }

    @Test(priority = 20)
    public void testReplaceExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        Attribute createdAttribute = configurationManager.addAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);
        Attribute retrievedAttribute = configurationManager.replaceAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);

        assertEquals("Existing id should be equal to the replaced id", createdAttribute.getAttributeId(),
                retrievedAttribute.getAttributeId());
    }

    @Test(priority = 21)
    public void testUpdateExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        Attribute createdAttribute = configurationManager.addAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);
        attribute.setValue(SAMPLE_ATTRIBUTE_VALUE3_UPDATED);
        Attribute retrievedAttribute = configurationManager.updateAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);

        assertEquals("Existing id should be equal to the replaced id",
                createdAttribute.getAttributeId(), retrievedAttribute.getAttributeId());
        assertEquals("Retrieved value should be equal to the updated value",
                retrievedAttribute.getValue(), SAMPLE_ATTRIBUTE_VALUE3_UPDATED);
    }

    @Test(priority = 22, expectedExceptions = ConfigurationManagementClientException.class)
    public void testGetNonExistingAttribute() throws Exception {

        configurationManager.getAttribute(SAMPLE_RESOURCE_TYPE_NAME1, SAMPLE_RESOURCE_NAME1, SAMPLE_ATTRIBUTE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 23)
    public void testGetExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute retrievedAttribute = configurationManager.getAttribute(resourceType.getName(),
                resource.getResourceName(), SAMPLE_ATTRIBUTE_NAME1);

        assertEquals("Existing id should be equal to the retrieved id", SAMPLE_ATTRIBUTE_NAME1,
                retrievedAttribute.getKey());
    }

    @Test(priority = 24, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteNonExistingAttribute() throws Exception {

        configurationManager.deleteAttribute(SAMPLE_RESOURCE_TYPE_NAME1, SAMPLE_RESOURCE_NAME1, SAMPLE_ATTRIBUTE_NAME1);

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 25, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute createdAttribute = configurationManager.addAttribute(resourceType.getName(),
                resource.getResourceName(), getSampleAttribute1());
        configurationManager.deleteAttribute(resourceType.getName(), resource.getResourceName(),
                createdAttribute.getKey());
        configurationManager.getAttribute(resourceType.getName(),
                resource.getResourceName(), createdAttribute.getKey());

        fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 26)
    public void testSearchMultiTenantResources() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        mockCarbonContextForTenant(SAMPLE_TENANT_ID_ABC, SAMPLE_TENANT_DOMAIN_ABC);
        configurationManager.addResource(resourceType.getName(), getSampleResource2Add());

        mockIdentityTenantUtilForTheTest();
        // Mock carbon context back to the super tenant.
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);

        ComplexCondition condition = getSampleSearchCondition();
        Resources resources = configurationManager.getTenantResources(condition);

        assertTrue(isSearchConditionMatch(resources));
    }

    @Test(priority = 27)
    public void testSearchMultiTenantResourcesWithoutCreatedTime() throws Exception {

        ConfigurationManagerComponentDataHolder.setUseCreatedTime(false);
        removeCreatedTimeColumn();
        testSearchMultiTenantResources();
    }

    @Test(priority = 27)
    public void testAddFile() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());

        File sampleResourceFile = new File(getSamplesPath("sample-resource-file.txt"));
        InputStream fileStream = FileUtils.openInputStream(sampleResourceFile);
        // Due to fileStream getting closed in file adding to database keeps a cloned file stream for comparing.
        InputStream cloneFileStream = FileUtils.openInputStream(sampleResourceFile);

        ResourceFile resourceFile = configurationManager.addFile(resourceType.getName(),
                resource.getResourceName(),"sample-resource-file", fileStream);
        InputStream retrievedFile = configurationManager
                .getFileById(resourceType.getName(), resource.getResourceName(), resourceFile.getId());
        Assert.assertEquals(TestUtils.convert(retrievedFile),TestUtils.convert(cloneFileStream));
    }

    @Test(priority = 28)
    public void testGetFileById() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());

        File sampleResourceFile = new File(getSamplesPath("sample-resource-file.txt"));
        InputStream fileStream = FileUtils.openInputStream(sampleResourceFile);
        // Due to fileStream getting closed in file adding to database keeps a cloned file stream for comparing.
        InputStream cloneFileStream = FileUtils.openInputStream(sampleResourceFile);

        ResourceFile resourceFile = configurationManager.addFile(resourceType.getName(),
                resource.getResourceName(), "sample-resource-file", fileStream);
        InputStream retrievedFileStream = configurationManager.getFileById(resourceType.getName(), resource.getResourceName(),
                resourceFile.getId());
        Assert.assertEquals("Stored file and retrieved file should be the same",
                TestUtils.convert(retrievedFileStream),TestUtils.convert(cloneFileStream));
    }

    @Test(priority = 29)
    public void testDeleteFileById() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());

        File sampleResourceFile = new File(getSamplesPath("sample-resource-file.txt"));
        InputStream fileStream = FileUtils.openInputStream(sampleResourceFile);

        ResourceFile resourceFile = configurationManager.addFile(resourceType.getName(),
                resource.getResourceName(), "sample-resource-file", fileStream);

        configurationManager.deleteFileById(resourceType.getName(), resource.getResourceName(), resourceFile.getId());
        Assert.assertFalse(
                "Resource should not contain any files.",
                configurationManager.getResource(resourceType.getName(), resource.getResourceName()).isHasFile()
        );
    }

    @Test(priority = 30)
    public void testGetFiles() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());

        File sampleResourceFile = new File(getSamplesPath("sample-resource-file.txt"));
        File sampleResourceFile1 = new File(getSamplesPath("sample-resource-file-1.txt"));
        InputStream fileStream = FileUtils.openInputStream(sampleResourceFile);
        InputStream fileStream1 = FileUtils.openInputStream(sampleResourceFile1);
        // Due to fileStream getting closed in file adding to database keeps a cloned file stream for comparing.
        InputStream cloneFileStream = FileUtils.openInputStream(sampleResourceFile);
        InputStream cloneFileStream1 = FileUtils.openInputStream(sampleResourceFile1);

        configurationManager.addFile(resourceType.getName(), resource.getResourceName(), "sample-resource-file", fileStream);
        configurationManager.addFile(resourceType.getName(), resource.getResourceName(), "sample-resource-file", fileStream1);

        List<ResourceFile> resourceFiles = configurationManager.getFiles(
                resourceType.getName(), resource.getResourceName());

        Assert.assertTrue("Retrieved file count should be equal to the existing value",
                resourceFiles.size() == 2);
        InputStream retrievedFileStream1 = configurationManager
                .getFileById(resourceType.getName(), resource.getResourceName(), resourceFiles.get(0).getId());
        InputStream retrievedFileStream2 = configurationManager
                .getFileById(resourceType.getName(), resource.getResourceName(), resourceFiles.get(1).getId());
        Assert.assertEquals("Stored file and retrieved file should be the same",
                TestUtils.convert(retrievedFileStream1), TestUtils.convert(cloneFileStream));
        Assert.assertEquals("Stored file and retrieved file should be the same",
                TestUtils.convert(retrievedFileStream2), TestUtils.convert(cloneFileStream1));
    }

    @Test(priority = 30)
    public void testDeleteFiles() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());

        File sampleResourceFile = new File(getSamplesPath("sample-resource-file.txt"));
        File sampleResourceFile1 = new File(getSamplesPath("sample-resource-file-1.txt"));
        InputStream fileStream = FileUtils.openInputStream(sampleResourceFile);
        InputStream fileStream1 = FileUtils.openInputStream(sampleResourceFile1);

        configurationManager.addFile(resourceType.getName(), resource.getResourceName(), "sample-resource-file", fileStream);
        configurationManager.addFile(resourceType.getName(), resource.getResourceName(), "sample-resource-file", fileStream1);

        configurationManager.deleteFiles(
                resourceType.getName(), resource.getResourceName());

        Assert.assertFalse(
                "Resource should not contain any files.",
                configurationManager.getResource(resourceType.getName(), resource.getResourceName()).isHasFile()
        );
    }

    @Test(priority = 31)
    public void testGetResourcesByType() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceType2Add());
        Resource resource1 = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Resource resource2 = configurationManager.addResource(resourceType.getName(), getSampleResource2Add());

        Resources resourcesByType = configurationManager.getResourcesByType(resourceType.getName());
        Assert.assertTrue("Retrieved resource count should be equal to the added value",
                resourcesByType.getResources().size() == 2);
        Assert.assertEquals("Created resource name should be equal to the retrieved resource name",
                resource1.getResourceName(), resourcesByType.getResources().get(0).getResourceName());
        Assert.assertEquals("Created resource name should be equal to the retrieved resource name",
                resource2.getResourceName(), resourcesByType.getResources().get(1).getResourceName());
    }

    @Test(priority = 32)
    public void testSearchTenantSpecificResources() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        mockCarbonContextForTenant(SAMPLE_TENANT_ID_ABC, SAMPLE_TENANT_DOMAIN_ABC);
        configurationManager.addResource(resourceType.getName(), getSampleResource2Add());

        mockIdentityTenantUtilForTheTest();
        // Mock carbon context back to the super tenant.
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);

        ComplexCondition condition = getSampleSearchCondition();
        Resources resources = configurationManager.getTenantResources(SAMPLE_TENANT_DOMAIN_ABC, condition);

        assertTrue(isTenantSearchConditionMatch(resources));
    }

    private void removeCreatedTimeColumn() throws DataAccessException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        jdbcTemplate.executeUpdate(REMOVE_CREATED_TIME_COLUMN_H2);
    }

    private void mockIdentityTenantUtilForTheTest() {

        mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = mock(IdentityTenantUtil.class);

        when(identityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
        when(identityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN_ABC)).thenReturn(SAMPLE_TENANT_ID_ABC);
        when(identityTenantUtil.getTenantDomain(SUPER_TENANT_ID)).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(identityTenantUtil.getTenantDomain(SAMPLE_TENANT_ID_ABC)).thenReturn(SAMPLE_TENANT_DOMAIN_ABC);
    }

    private boolean isSearchConditionMatch(Resources resources) {

        for (Resource resource : resources.getResources()) {
            if ((!resource.getTenantDomain().equals(SUPER_TENANT_DOMAIN_NAME)
                    && !resource.getTenantDomain().equals(SAMPLE_TENANT_DOMAIN_ABC))) {
                return false;
            }
            for (Attribute attribute : resource.getAttributes()) {
                if (!attribute.getKey().equals(SAMPLE_ATTRIBUTE_NAME1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTenantSearchConditionMatch(Resources resources) {

        for (Resource resource : resources.getResources()) {
            if ((!resource.getTenantDomain().equals(SAMPLE_TENANT_DOMAIN_ABC))) {
                return false;
            }
            for (Attribute attribute : resource.getAttributes()) {
                if (!attribute.getKey().equals(SAMPLE_ATTRIBUTE_NAME1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void prepareConfigs() throws Exception {

        // Mock get maximum query length call.
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getProperty(any(String.class))).thenReturn("4194304");
        when(IdentityUtil.getEndpointURIPath(any(String.class), anyBoolean(), anyBoolean())).thenReturn(
                "/t/bob.com/api/identity/config-mgt/v1.0/resource/file/publisher/SMSPublisher/9e038218-8e99-4dae-bf83-a78f5dcd73a8");
        ConfigurationManagerComponentDataHolder.setUseCreatedTime(true);
        ConfigurationManagerConfigurationHolder configurationHolder = new ConfigurationManagerConfigurationHolder();
        ConfigurationDAO configurationDAO = new ConfigurationDAOImpl();
        configurationHolder.setConfigurationDAOS(Collections.singletonList(configurationDAO));
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
        configurationManager = new ConfigurationManagerImpl(configurationHolder);
    }

    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = mock(IdentityTenantUtil.class);
        when(identityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }
}
