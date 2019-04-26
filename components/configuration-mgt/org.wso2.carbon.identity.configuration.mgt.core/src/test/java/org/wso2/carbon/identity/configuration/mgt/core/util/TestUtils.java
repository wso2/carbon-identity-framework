package org.wso2.carbon.identity.configuration.mgt.core.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.search.ComplexCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.configuration.mgt.core.search.PrimitiveCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_KEY;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_NAME1;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_NAME2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_NAME3;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_VALUE1;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_VALUE2;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_ATTRIBUTE_VALUE3;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_TYPE_DESCRIPTION;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.TestConstants.SAMPLE_TENANT_DOMAIN_ABC;

public class TestUtils {

    public static final String H2_SCRIPT_NAME = "h2.sql";
    private static final String DB_NAME = "Config";
    public static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    public static void initiateH2Base() throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + getFilePath(H2_SCRIPT_NAME) + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    public static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "config",
                    fileName).toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    public static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("No data source initiated for database: " + DB_NAME);
    }

    public static Connection spyConnection(Connection connection) throws SQLException {

        Connection spy = spy(connection);
        doNothing().when(spy).close();
        return spy;
    }

    public static void closeH2Base() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static ResourceTypeAdd getSampleResourceTypeAdd() {

        ResourceTypeAdd resourceTypeAdd = new ResourceTypeAdd();
        resourceTypeAdd.setName(SAMPLE_RESOURCE_TYPE_NAME);
        resourceTypeAdd.setDescription(SAMPLE_RESOURCE_TYPE_DESCRIPTION);
        return resourceTypeAdd;
    }

    public static ResourceAdd getSampleResource1Add() {

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(getSampleAttribute1());
        attributes.add(getSampleAttribute2());

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(SAMPLE_RESOURCE_NAME);
        resourceAdd.setAttributes(attributes);
        return resourceAdd;
    }

    public static ResourceAdd getSampleResource2Add() {

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(getSampleAttribute1());
        attributes.add(getSampleAttribute3());

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(SAMPLE_RESOURCE_NAME);
        resourceAdd.setAttributes(attributes);
        return resourceAdd;
    }

    public static Attribute getSampleAttribute1() {

        Attribute attribute = new Attribute();
        attribute.setKey(SAMPLE_ATTRIBUTE_NAME1);
        attribute.setValue(SAMPLE_ATTRIBUTE_VALUE1);
        return attribute;
    }

    public static Attribute getSampleAttribute2() {

        Attribute attribute = new Attribute();
        attribute.setKey(SAMPLE_ATTRIBUTE_NAME2);
        attribute.setValue(SAMPLE_ATTRIBUTE_VALUE2);
        return attribute;
    }

    public static Attribute getSampleAttribute3() {

        Attribute attribute = new Attribute();
        attribute.setKey(SAMPLE_ATTRIBUTE_NAME3);
        attribute.setValue(SAMPLE_ATTRIBUTE_VALUE3);
        return attribute;
    }

    public static ComplexCondition getSampleSearchCondition() {

        PrimitiveCondition tenantDomainEqSuper = new PrimitiveCondition(RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN,
                ConditionType.PrimitiveOperator.EQUALS, SUPER_TENANT_DOMAIN_NAME);
        PrimitiveCondition tenantDomainEqABC = new PrimitiveCondition(RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN,
                ConditionType.PrimitiveOperator.EQUALS, SAMPLE_TENANT_DOMAIN_ABC);
        PrimitiveCondition attributeKeyEqFrom = new PrimitiveCondition(RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_KEY,
                ConditionType.PrimitiveOperator.EQUALS, SAMPLE_ATTRIBUTE_NAME1);

        List<Condition> tenantDomainConditions = new ArrayList<>();
        tenantDomainConditions.add(tenantDomainEqSuper);
        tenantDomainConditions.add(tenantDomainEqABC);
        ComplexCondition eitherTenantDomain = new ComplexCondition(
                ConditionType.ComplexOperator.OR, tenantDomainConditions);

        List<Condition> conditions = new ArrayList<>();
        conditions.add(eitherTenantDomain);
        conditions.add(attributeKeyEqFrom);
        return new ComplexCondition(ConditionType.ComplexOperator.AND, conditions);
    }
}

