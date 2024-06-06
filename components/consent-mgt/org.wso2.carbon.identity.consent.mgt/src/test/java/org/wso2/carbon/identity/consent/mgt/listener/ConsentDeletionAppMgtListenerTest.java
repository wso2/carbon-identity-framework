/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.consent.mgt.listener;

import org.apache.axiom.om.OMElement;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.ConsentManagerImpl;
import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.connector.impl.DefaultPIIController;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.dao.impl.PIICategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeCategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.ReceiptDAOImpl;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONFIG_ELEM_CONSENT;
import static org.wso2.carbon.identity.consent.mgt.listener.ConsentDeletionAppMgtListener.CONSENT_SEARCH_PII_PRINCIPAL_ID;

/*
  Unit tests for ConsentDeletionAppMgtListener.
 */
public class ConsentDeletionAppMgtListenerTest {

    @Mock
    private IdentityConfigParser mockConfigParser;

    @Mock
    private OMElement consentElement;

    @Mock
    private FrameworkServiceDataHolder mockFrameworkServiceDataHolder;

    @Mock
    private IdentityConsentDataHolder mockIdentityConsentDataHolder;

    @Mock
    private ConsentConfigParser configParser;

    private MockedStatic<ConsentManagerComponentDataHolder> consentManagerComponentDataHolder;
    private MockedStatic<IdentityConfigParser> identityConfigParser;

    ConsentManagerConfigurationHolder configurationHolder;
    ConsentDeletionAppMgtListener consentDeletionAppMgtListener;
    private Connection connection;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    private static final int TENANT_ID = 1;
    private static final String TENANT_DOMAIN = "testorg.com";
    private static final String APPLICATION_NAME = "testServiceProvider";
    private static final String USER_NAME = "DEFAULT/testuser@wso2.com";

    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);
        initiateH2Base();
        DataSource dataSource = mock(DataSource.class);

        consentManagerComponentDataHolder = mockStatic(ConsentManagerComponentDataHolder.class);
        ConsentManagerComponentDataHolder mockComponentDataHolder = mock(ConsentManagerComponentDataHolder.class);
        consentManagerComponentDataHolder.when(
                ConsentManagerComponentDataHolder::getInstance).thenReturn(mockComponentDataHolder);
        when(mockComponentDataHolder.getDataSource()).thenReturn(dataSource);

        connection = getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockConfigParser);
        when(mockConfigParser.getConfigElement(CONFIG_ELEM_CONSENT)).thenReturn(consentElement);
        consentDeletionAppMgtListener = new ConsentDeletionAppMgtListener();

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH,
                Paths.get(carbonHome, "conf").toString());
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
        consentManagerComponentDataHolder.close();
        identityConfigParser.close();
    }

    public static void closeH2Base() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get("ConsentDB");
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static Connection getConnection() throws SQLException {

        if (dataSourceMap.get("ConsentDB") != null) {
            return dataSourceMap.get("ConsentDB").getConnection();
        }
        throw new RuntimeException("No data source initiated for database: ConsentDB");
    }

    public static Connection spyConnection(Connection connection) throws SQLException {

        Connection spy = spy(connection);
        doNothing().when(spy).close();
        return spy;
    }

    @Test
    public void testPostUpdateApplication() throws Exception {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<ConsentUtils> consentUtils = mockStatic(ConsentUtils.class);
             MockedStatic<IdentityConsentDataHolder> identityConsentDataHolder =
                     mockStatic(IdentityConsentDataHolder.class)) {

            frameworkServiceDataHolder.when(
                    FrameworkServiceDataHolder::getInstance).thenReturn(this.mockFrameworkServiceDataHolder);
            setConsentManagerConfigurationHolder();

            RealmService realmService = mock(RealmService.class);
            configurationHolder.setRealmService(realmService);
            ConsentManager consentManager = new ConsentManagerImpl(configurationHolder);
            when(this.mockFrameworkServiceDataHolder.getConsentManager()).thenReturn(consentManager);

            consentUtils.when(ConsentUtils::getTenantDomainFromCarbonContext).thenReturn(TENANT_DOMAIN);
            mockRealmService(realmService);

            when(configParser.getConfiguration()).thenReturn(getConfiguration());

            // Assert to ensure the data is available in the database to proceed with the test.
            Assert.assertEquals("Consent receipt data not available in the database to proceed with the test.",
                    consentManager.searchReceipts(100, 0, CONSENT_SEARCH_PII_PRINCIPAL_ID,
                            TENANT_DOMAIN, APPLICATION_NAME, null).size(), 1);

            identityConsentDataHolder.when(IdentityConsentDataHolder::getInstance).
                    thenReturn(mockIdentityConsentDataHolder);
            when(mockIdentityConsentDataHolder.getConsentManager()).thenReturn(consentManager);

            consentDeletionAppMgtListener.doPostUpdateApplication(getServiceProvider(), TENANT_DOMAIN,
                    getAuthenticatedUser().getUserName());

            Assert.assertEquals("Consent receipt is not removed when application is disabled",
                    consentManager.searchReceipts(100, 0, CONSENT_SEARCH_PII_PRINCIPAL_ID,
                            TENANT_DOMAIN, APPLICATION_NAME, null).size(), 0);
        }
    }

    public static void initiateH2Base() throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + "ConsentDB");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + getFilePath("h2.sql") + "'");
        }
        dataSourceMap.put("ConsentDB", dataSource);
    }

    public static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbScripts", "consent",
                    fileName).toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private void mockRealmService(RealmService realmService) throws Exception {

        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(tenantManager.getDomain(TENANT_ID)).thenReturn(TENANT_DOMAIN);
    }

    private AuthenticatedUser getAuthenticatedUser() {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier("");
        Map<ClaimMapping, String> userAttributes = new HashMap<>();

        Claim tempClaim1 = new Claim();
        tempClaim1.setClaimUri("http://wso2.org/claims/lastname");
        ClaimMapping tempClaimMapping1 = new ClaimMapping();
        tempClaimMapping1.setRequested(false);
        tempClaimMapping1.setMandatory(false);
        tempClaimMapping1.setLocalClaim(tempClaim1);
        tempClaimMapping1.setRemoteClaim(tempClaim1);
        userAttributes.put(tempClaimMapping1, "Test Lastname");
        authenticatedUser.setUserAttributes(userAttributes);
        authenticatedUser.setFederatedUser(false);
        authenticatedUser.setTenantDomain(TENANT_DOMAIN);
        authenticatedUser.setUserStoreDomain("DEFAULT");
        authenticatedUser.setUserName(USER_NAME);
        return authenticatedUser;
    }

    private ServiceProvider getServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setApplicationID(1);
        serviceProvider.setApplicationEnabled(false);
        return serviceProvider;
    }

    private void setConsentManagerConfigurationHolder() {

        configurationHolder = new ConsentManagerConfigurationHolder();

        PurposeDAO purposeDAO = new PurposeDAOImpl();
        configurationHolder.setPurposeDAOs(Collections.singletonList(purposeDAO));

        PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
        configurationHolder.setPiiCategoryDAOs(Collections.singletonList(piiCategoryDAO));

        PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
        configurationHolder.setPurposeCategoryDAOs(Collections.singletonList(purposeCategoryDAO));

        ReceiptDAO receiptDAO = new ReceiptDAOImpl();
        configurationHolder.setReceiptDAOs(Collections.singletonList(receiptDAO));

        List<PIIController> piiControllers = new ArrayList<>();
        DefaultPIIController defaultPIIController = new DefaultPIIController(configParser);
        piiControllers.add(defaultPIIController);
        configurationHolder.setPiiControllers(piiControllers);
    }


    private Map<String, Object> getConfiguration() {
        Map<String, Object> configuration = new HashMap<>();

        // Mocked configuration values
        configuration.put("COUNTRY_ELEMENT", "US");
        configuration.put("LOCALITY_ELEMENT", "New York");
        configuration.put("REGION_ELEMENT", "NY");
        configuration.put("POST_OFFICE_BOX_NUMBER_ELEMENT", "12345");
        configuration.put("POST_CODE_ELEMENT", "10001");
        configuration.put("STREET_ADDRESS_ELEMENT", "123 Main St");

        configuration.put("PII_CONTROLLER_NAME_ELEMENT", "Example Controller");
        configuration.put("PII_CONTROLLER_CONTACT_ELEMENT", "John Doe");
        configuration.put("PII_CONTROLLER_PHONE_ELEMENT", "+1-555-555-5555");
        configuration.put("PII_CONTROLLER_EMAIL_ELEMENT", "controller@example.com");
        configuration.put("PII_CONTROLLER_ON_BEHALF_ELEMENT", "false");
        configuration.put("PII_CONTROLLER_URL_ELEMENT", "http://example.com");

        return configuration;
    }
}
