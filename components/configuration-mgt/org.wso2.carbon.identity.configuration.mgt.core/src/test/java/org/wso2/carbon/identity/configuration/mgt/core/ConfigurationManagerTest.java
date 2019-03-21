package org.wso2.carbon.identity.configuration.mgt.core;

import org.junit.Assert;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
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
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.ComplexCondition;
import org.wso2.carbon.identity.configuration.mgt.core.util.JdbcUtils;
import org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import javax.sql.DataSource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestSQLConstants.REMOVE_CREATED_TIME_COLUMN_H2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_NAME1;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_VALUE3_UPDATED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_TENANT_DOMAIN_ABC;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_TENANT_ID_ABC;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleAttribute1;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleAttribute3;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResource1Add;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResource2Add;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleResourceTypeAdd;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.getSampleSearchCondition;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.configuration.mgt.core.util.TestUtils.spyConnection;

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
        Assert.assertNotNull(resourceType.getId(), "Created resource type id cannot be null");
    }

    @Test(priority = 2, expectedExceptions = ConfigurationManagementClientException.class)
    public void testAddDuplicateResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        configurationManager.addResourceType(resourceTypeAdd);
        configurationManager.addResourceType(resourceTypeAdd);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 3)
    public void testReplaceNonExistingResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        ResourceType resourceType = configurationManager.replaceResourceType(resourceTypeAdd);

        Assert.assertNotNull(resourceType.getId(), "Created resource id cannot be null");
    }

    @Test(priority = 4)
    public void testReplaceExistingResourceType() throws Exception {

        ResourceTypeAdd resourceTypeAdd = getSampleResourceTypeAdd();

        ResourceType resourceTypeCreated = configurationManager.addResourceType(resourceTypeAdd);
        ResourceType resourceTypeReplaced = configurationManager.replaceResourceType(resourceTypeAdd);

        Assert.assertEquals("Existing id should be equal to the replaced id", resourceTypeCreated.getId(),
                resourceTypeReplaced.getId());
    }

    @Test(priority = 5, expectedExceptions = ConfigurationManagementClientException.class)
    public void testGetNonExistingResourceType() throws Exception {

        configurationManager.getResourceType(SAMPLE_RESOURCE_TYPE_NAME);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 6)
    public void testGetExistingResourceType() throws Exception {

        ResourceType resourceTypeCreated = configurationManager.addResourceType(getSampleResourceTypeAdd());
        ResourceType resourceTypeRetrieved = configurationManager.getResourceType(SAMPLE_RESOURCE_TYPE_NAME);

        Assert.assertEquals("Existing id should be equal to the retrieved id", resourceTypeCreated.getId(),
                resourceTypeRetrieved.getId());
    }

    @Test(priority = 7, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteNonExistingResourceType() throws Exception {

        configurationManager.deleteResourceType(SAMPLE_RESOURCE_TYPE_NAME);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 8, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteExistingResourceType() throws Exception {

        configurationManager.addResourceType(getSampleResourceTypeAdd());
        configurationManager.deleteResourceType(SAMPLE_RESOURCE_TYPE_NAME);
        configurationManager.getResourceType(SAMPLE_RESOURCE_TYPE_NAME);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 9)
    public void testAddResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        ResourceAdd resourceTypeAdd = getSampleResource1Add();

        Resource resource = configurationManager.addResource(resourceType.getName(), resourceTypeAdd);
        Assert.assertNotNull(resource.getResourceId(), "Created resource type id cannot be null");
    }

    @Test(priority = 10, expectedExceptions = ConfigurationManagementClientException.class)
    public void testAddDuplicateResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        ResourceAdd resourceAdd = getSampleResource1Add();

        configurationManager.addResource(resourceType.getName(), resourceAdd);
        configurationManager.addResource(resourceType.getName(), resourceAdd);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 11)
    public void testReplaceNonExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        ResourceAdd resourceAdd = getSampleResource1Add();

        Resource resource = configurationManager.replaceResource(resourceType.getName(), resourceAdd);

        Assert.assertNotNull(resource.getResourceId(), "Created resource id cannot be null");
    }

    @Test(priority = 12)
    public void testReplaceExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        ResourceAdd resourceAdd = getSampleResource1Add();

        Resource resourceCreated = configurationManager.addResource(resourceType.getName(), resourceAdd);
        Resource resourceReplaced = configurationManager.replaceResource(resourceType.getName(), resourceAdd);

        Assert.assertNotEquals("Created time should be different from the last updated time",
                resourceReplaced.getCreatedTime(),resourceReplaced.getLastModified());
        Assert.assertEquals("Existing id should be equal to the replaced id", resourceCreated.getResourceId(),
                resourceReplaced.getResourceId());
    }

    @Test(priority = 13, expectedExceptions = ConfigurationManagementClientException.class)
    public void testGetNonExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        configurationManager.getResource(resourceType.getName(), SAMPLE_RESOURCE_NAME);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 14)
    public void testGetExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        Resource resourceCreated = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Resource resourceRetrieved = configurationManager.getResource(resourceType.getName(), SAMPLE_RESOURCE_NAME);

        Assert.assertEquals("Existing id should be equal to the retrieved id", resourceCreated.getResourceId(),
                resourceRetrieved.getResourceId());
    }

    @Test(priority = 15, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteNonExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        configurationManager.deleteResource(resourceType.getName(), SAMPLE_RESOURCE_NAME);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 16, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteExistingResource() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());

        configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        configurationManager.deleteResource(resourceType.getName(), SAMPLE_RESOURCE_TYPE_NAME);
        configurationManager.getResource(resourceType.getName(), SAMPLE_RESOURCE_TYPE_NAME);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 17)
    public void testAddAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        Attribute retrievedAttribute = configurationManager.addAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);
        Assert.assertNotNull(retrievedAttribute.getAttributeId(), "Created resource type id cannot be null");
    }

    @Test(priority = 18, expectedExceptions = ConfigurationManagementClientException.class)
    public void testAddDuplicateAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        configurationManager.addAttribute(resourceType.getName(), resource.getResourceName(), attribute);
        configurationManager.addAttribute(resourceType.getName(), resource.getResourceName(), attribute);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 19)
    public void testReplaceNonExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute attribute = getSampleAttribute3();

        Attribute retrievedAttribute = configurationManager.replaceAttribute(resourceType.getName(),
                resource.getResourceName(), attribute);
        Assert.assertNotNull(retrievedAttribute.getAttributeId(), "Created resource id cannot be null");
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

        Assert.assertEquals("Existing id should be equal to the replaced id", createdAttribute.getAttributeId(),
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

        Assert.assertEquals("Existing id should be equal to the replaced id",
                createdAttribute.getAttributeId(), retrievedAttribute.getAttributeId());
        Assert.assertEquals("Retrieved value should be equal to the updated value",
                retrievedAttribute.getValue(), SAMPLE_ATTRIBUTE_VALUE3_UPDATED);
    }

    @Test(priority = 22, expectedExceptions = ConfigurationManagementClientException.class)
    public void testGetNonExistingAttribute() throws Exception {

        configurationManager.getAttribute(SAMPLE_RESOURCE_TYPE_NAME, SAMPLE_RESOURCE_NAME, SAMPLE_ATTRIBUTE_NAME1);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
    }

    @Test(priority = 23)
    public void testGetExistingAttribute() throws Exception {

        ResourceType resourceType = configurationManager.addResourceType(getSampleResourceTypeAdd());
        Resource resource = configurationManager.addResource(resourceType.getName(), getSampleResource1Add());
        Attribute retrievedAttribute = configurationManager.getAttribute(resourceType.getName(),
                resource.getResourceName(), SAMPLE_ATTRIBUTE_NAME1);

        Assert.assertEquals("Existing id should be equal to the retrieved id", SAMPLE_ATTRIBUTE_NAME1,
                retrievedAttribute.getKey());
    }

    @Test(priority = 24, expectedExceptions = ConfigurationManagementClientException.class)
    public void testDeleteNonExistingAttribute() throws Exception {

        configurationManager.deleteAttribute(SAMPLE_RESOURCE_TYPE_NAME, SAMPLE_RESOURCE_NAME, SAMPLE_ATTRIBUTE_NAME1);

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
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

        Assert.fail("Expected: " + ConfigurationManagementClientException.class.getName());
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

        Assert.assertTrue(isSearchConditionMatch(resources));
    }

    @Test(priority = 27)
    public void testSearchMultiTenantResourcesWithoutCreatedTime() throws Exception {

        ConfigurationManagerComponentDataHolder.setUseCreatedTime(false);
        removeCreatedTimeColumn();
        testSearchMultiTenantResources();
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

    private void prepareConfigs() throws ConfigurationManagementException {

        // Mock get maximum query length call.
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getProperty(any(String.class))).thenReturn("4194304");
        ConfigurationManagerComponentDataHolder.setUseCreatedTime(true);
        ConfigurationManagerConfigurationHolder configurationHolder =
                new ConfigurationManagerConfigurationHolder();
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
