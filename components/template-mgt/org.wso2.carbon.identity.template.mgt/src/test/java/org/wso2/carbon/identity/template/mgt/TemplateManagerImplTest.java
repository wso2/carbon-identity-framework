/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt;

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.template.mgt.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.template.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.identity.template.mgt.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.template.mgt.util.TestUtils.mockDataSource;
import static org.wso2.carbon.identity.template.mgt.util.TestUtils.spyConnection;

@PrepareForTest({IdentityDatabaseUtil.class, PrivilegedCarbonContext.class, CarbonContextDataHolder.class})
public class TemplateManagerImplTest extends PowerMockTestCase {

    private static String sampleScript = "<!-- You can customize the user prompt template here... -->\n" +
            "\t\n" +
            "<div class=\"uppercase\">\n" +
            "    <h3>Welcome {{name}}</h3>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"boarder-all \">\n" +
            "    <div class=\"clearfix\"></div>\n" +
            "    <div class=\"padding-double login-form\">\n" +
            "\n" +
            "        <form id=\"template-form\" method=\"POST\"> <!-- *DO NOT CHANGE THIS* -->\n" +
            "            <div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required\">\n" +
            "\n" +
            "                <!-- Add the required input field/s here...\n" +
            "                It should follow the below mentioned format-->\n" +
            "\n" +
            "                <label for=\"sampleInput\" class=\"control-label\">sample input</label>\n" +
            "                <input type=\"text\" id=\"sampleInput\" name=\"sample_input\" class=\"form-control\" placeholder=\"sample input placeholder\" />\n" +
            "\n" +
            "            </div>\n" +
            "\n" +
            "            <input type=\"hidden\" id=\"promptResp\" name=\"promptResp\" value=\"true\"> <!-- *DO NOT CHANGE THIS* -->\n" +
            "            <input type=\"hidden\" id=\"promptId\" name=\"promptId\"> <!-- *DO NOT CHANGE THIS* -->\n" +
            "\n" +
            "            <div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required\">\n" +
            "                <input type=\"submit\" class=\"wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large\" value=\"Submit\">\n" +
            "            </div>\n" +
            "        </form>\n" +
            "        <div class=\"clearfix\"></div>\n" +
            "    </div>\n" +
            "</div>";

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);

        Mockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        Mockito.when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
    }

    @DataProvider(name = "TemplateDataProvider")
    public Object[][] addTemplateData() throws Exception {

        Template testTemplate1 = new Template(SUPER_TENANT_ID, "T1", "Description 1", sampleScript);
        Template testTemplate2 = new Template(SUPER_TENANT_ID, "T2", "Description 2", sampleScript);
        Template testTemplate3 = new Template(SUPER_TENANT_ID, "T3", "Description 3", sampleScript);

        return new Object[][]{
                {
                        testTemplate1
                },
                {
                        testTemplate2
                },
                {
                        testTemplate3,
                },
        };
    }

    @DataProvider(name = "getTemplateByNameDataProvider")
    public Object[][] getTemplateByNameData() throws Exception {

        Template testTemplate1 = new Template(SUPER_TENANT_ID, "T1", "Description 1", sampleScript);
        Template testTemplate2 = new Template(SUPER_TENANT_ID, "T2", "Description 2", sampleScript);
        Template testTemplate3 = new Template(SUPER_TENANT_ID, "T3", "Description 3", sampleScript);

        return new Object[][]{
                {
                        testTemplate1,
                        "T1"
                },
                {
                        testTemplate2,
                        "T2"
                },
                {
                        testTemplate3,
                        "T3"
                },
        };
    }

    @DataProvider(name = "UpdateTemplateDataProvider")
    public Object[][] updateTemplateData() throws Exception {

        Template testTemplate1 = new Template(SUPER_TENANT_ID, "T1", "Description 1", sampleScript);
        Template testTemplate2 = new Template(SUPER_TENANT_ID, "T2", "Description 2", sampleScript);
        Template newTestTemplate1 = new Template(SUPER_TENANT_ID, "T1 Updated", "Updated Description 1", sampleScript);
        Template newTestTemplate2 = new Template(SUPER_TENANT_ID, "T2 Updated", "Updated Description 2", sampleScript);

        return new Object[][]{
                {
                        "T1",
                        testTemplate1,
                        newTestTemplate1
                },
                {
                        "T2",
                        testTemplate2,
                        newTestTemplate2
                },

        };
    }

    @DataProvider(name = "templateListProvider")
    public Object[][] provideListData() throws Exception {

        return new Object[][]{
                // limit, offset, resultSize
                {0, 0, 3},
                {1, 1, 1},
                {10, 0, 3}
        };
    }

    @DataProvider(name = "validateInputsDataProvider")
    public Object[][] provideInputData() throws Exception {

        Template testTemplate1 = new Template(SUPER_TENANT_ID, null, "sample description", sampleScript);
        Template testTemplate2 = new Template(null, "sample Template", "sample description", null);
        Template testTemplate3 = new Template(null, null, "sample description", null);

        return new Object[][]{
                {
                        testTemplate1
                },
                {
                        testTemplate2
                },
                {
                        testTemplate3
                }
        };
    }

    @Test(dataProvider = "TemplateDataProvider")
    public void testAddTemplate(Object template) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            TemplateManager templateManager = new TemplateManagerImpl();
            Template templateResult = templateManager.addTemplate(((Template) template));

            Assert.assertEquals(templateResult.getTemplateName(), ((Template) template).getTemplateName());
            Assert.assertEquals(templateResult.getTenantId(), ((Template) template).getTenantId());
        }
    }

    @Test(dataProvider = "UpdateTemplateDataProvider")
    public void testUpdateTemplate(String oldTemplateName, Object oldtemplate, Object newTemplate) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            TemplateManager templateManager = new TemplateManagerImpl();
            addTemplates(templateManager, Collections.singletonList(oldtemplate), dataSource);

            try (Connection connection1 = getConnection()) {
                Connection spyConnection1 = spyConnection(connection1);
                when(dataSource.getConnection()).thenReturn(spyConnection1);
                Template updatedTemplate = templateManager.updateTemplate(oldTemplateName, ((Template) newTemplate));
                Assert.assertEquals(((Template) newTemplate).getTenantId(), updatedTemplate.getTenantId());
                Assert.assertEquals(((Template) newTemplate).getTemplateName(), updatedTemplate.getTemplateName());
            }
        }
    }

    @Test(dataProvider = "getTemplateByNameDataProvider")
    public void testGetTemplateByName(Object templateObject, String templateName) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            TemplateManager templateManager = new TemplateManagerImpl();
            addTemplates(templateManager, Collections.singletonList(templateObject), dataSource);

            try (Connection connection1 = getConnection()) {

                Connection spyConnection1 = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection1);
                Template templateByName = templateManager.getTemplateByName(templateName);
                Assert.assertEquals(((Template) templateObject).getTemplateName(), templateByName.getTemplateName());
                Assert.assertEquals(((Template) templateObject).getDescription(), templateByName.getDescription());
                Assert.assertEquals(((Template) templateObject).getTemplateScript(), templateByName.getTemplateScript());
            }
        }
    }

    @Test(dataProvider = "templateListProvider")
    public void testGetTemplateList(Integer limit, Integer offset, int resultSize) throws Exception {

        Template testTemplate1 = new Template(SUPER_TENANT_ID, "T1", "Description 1", sampleScript);
        Template testTemplate2 = new Template(SUPER_TENANT_ID, "T2", "Description 2", sampleScript);
        Template testTemplate3 = new Template(SUPER_TENANT_ID, "Template3", "Description 3", "Script 3");

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            TemplateManager templateManager = new TemplateManagerImpl();

            Template templateResult1 = templateManager.addTemplate(testTemplate1);
            Assert.assertEquals(templateResult1.getTemplateName(), testTemplate1.getTemplateName());

            Template templateResult2 = templateManager.addTemplate(testTemplate2);
            Assert.assertEquals(templateResult2.getTemplateName(), testTemplate2.getTemplateName());

            Template templateResult3 = templateManager.addTemplate(testTemplate3);
            Assert.assertEquals(templateResult3.getTemplateName(), testTemplate3.getTemplateName());

            List<TemplateInfo> templateList = templateManager.listTemplates(limit, offset);

            Assert.assertEquals(templateList.size(), resultSize);
        }
    }

    @Test(dataProvider = "TemplateDataProvider")
    public void testDeleteTemplate(Object template) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            TemplateManager templateManager = new TemplateManagerImpl();
            Template templateResult = templateManager.addTemplate(((Template) template));
            Assert.assertEquals(templateResult.getTemplateName(), ((Template) template).getTemplateName());

            templateManager.deleteTemplate(templateResult.getTemplateName());

        }
    }

    @Test(dataProvider = "validateInputsDataProvider", expectedExceptions = TemplateManagementClientException.class)
    public void testValidatingInputs(Object template) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            TemplateManager templateManager = new TemplateManagerImpl();
            templateManager.addTemplate(((Template) template));
            Assert.fail("Expected: " + TemplateManagementClientException.class.getName());
        }
    }

    @Test
    public void testSetTenantIdIfNull() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);
        Template template = new Template(null, "T1", "Description 1", sampleScript);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            TemplateManager templateManager = new TemplateManagerImpl();
            Template templateInfo = templateManager.addTemplate(template);
            Assert.assertEquals(templateInfo.getTenantId(), new Integer(SUPER_TENANT_ID));
        }

    }

    @Test()
    public void testErrorCodes() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);
        Template template = new Template(SUPER_TENANT_ID, null, "sample description", sampleScript);
        String errorCode = TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NAME_REQUIRED.getCode();

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            TemplateManager templateManager = new TemplateManagerImpl();
            try {
                templateManager.addTemplate(template);
            } catch (TemplateManagementClientException e) {
                String errorCode1 = e.getErrorCode();
                Assert.assertEquals(errorCode, errorCode1);
            }

        }
    }

    @Test(expectedExceptions = TemplateManagementClientException.class)
    public void testvalidatingPaginationParameters() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            TemplateManager templateManager = new TemplateManagerImpl();
            templateManager.listTemplates(-10, -5);
            Assert.fail("Expected: " + TemplateManagementClientException.class.getName());

            templateManager.listTemplates(0, -5);
            Assert.fail("Expected: " + TemplateManagementClientException.class.getName());

            templateManager.listTemplates(-10, 0);
            Assert.fail("Expected: " + TemplateManagementClientException.class.getName());
        }

    }

    private void addTemplates(TemplateManager templateManager, List<Object> templates, DataSource dataSource) throws SQLException, TemplateManagementException {

        for (Object template : templates) {
            templateManager.addTemplate((Template) template);
        }
    }

}