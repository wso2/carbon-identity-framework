/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import org.apache.axiom.om.OMElement;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.ConsentManagerImpl;
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
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONFIG_ELEM_CONSENT;

@PrepareForTest({IdentityConfigParser.class, OMElement.class, IdentityUtil.class, PrivilegedCarbonContext.class,
        FrameworkServiceDataHolder.class, ConsentUtils.class, ConsentManagerConfigurationHolder.class,
        RealmService.class, TenantManager.class, UserRealm.class, DataSource.class,
        ConsentManagerComponentDataHolder.class})
public class SSOConsentServiceImplTest extends PowerMockTestCase {

    private static final String TEMPORARY_CLAIM_URI = "http://wso2.org/claims/nickname";
    private SSOConsentService ssoConsentService;
    ConsentManagerConfigurationHolder configurationHolder;
    private Connection connection;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    @Mock
    private IdentityConfigParser mockConfigParser;

    @Mock
    private OMElement consentElement;

    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolder;

    @Mock
    private ClaimMetadataManagementService claimMetadataManagementService;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        DataSource dataSource = mock(DataSource.class);
        mockStatic(ConsentManagerComponentDataHolder.class);
        ConsentManagerComponentDataHolder componentDataHolder = mock(ConsentManagerComponentDataHolder.class);
        when(ConsentManagerComponentDataHolder.getInstance()).thenReturn(componentDataHolder);
        when(componentDataHolder.getDataSource()).thenReturn(dataSource);

        connection = getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        mockStatic(IdentityConfigParser.class);
        mockStatic(OMElement.class);
        when(IdentityConfigParser.getInstance()).thenReturn(mockConfigParser);
        when(IdentityConfigParser.getInstance().getConfigElement(CONFIG_ELEM_CONSENT)).thenReturn(consentElement);
        ssoConsentService = new SSOConsentServiceImpl();

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH,
                Paths.get(carbonHome, "conf").toString());
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
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
    public void testGetConsentRequiredClaimsWithExistingConsents() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("Travelocity.com");

        User user = new User();
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        serviceProvider.setOwner(user);

        ClaimConfig claimConfig = new ClaimConfig();
        Claim tempClaim1 = new Claim();
        tempClaim1.setClaimUri("http://wso2.org/claims/organization");
        ClaimMapping tempClaimMapping1 = new ClaimMapping();
        tempClaimMapping1.setRequested(true);
        tempClaimMapping1.setMandatory(false);
        tempClaimMapping1.setLocalClaim(tempClaim1);
        tempClaimMapping1.setRemoteClaim(tempClaim1);

        Claim tempClaim2 = new Claim();
        tempClaim2.setClaimUri("http://wso2.org/claims/country");
        ClaimMapping tempClaimMapping2 = new ClaimMapping();
        tempClaimMapping2.setRequested(true);
        tempClaimMapping2.setMandatory(true);
        tempClaimMapping2.setLocalClaim(tempClaim2);
        tempClaimMapping2.setRemoteClaim(tempClaim2);

        claimConfig.setClaimMappings(new ClaimMapping[]{tempClaimMapping1, tempClaimMapping2});
        serviceProvider.setClaimConfig(claimConfig);
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig
                = new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setSubjectClaimUri(null);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

        AuthenticatedUser authenticatedUser = getAuthenticatedUser();

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getProperty("Consent.PromptSubjectClaimRequestedConsent")).thenReturn(null);

        mockCarbonContextForTenant();
        mockStatic(FrameworkServiceDataHolder.class);
        when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);
        setConsentManagerConfigurationHolder();

        RealmService realmService = mock(RealmService.class);
        configurationHolder.setRealmService(realmService);
        ConsentManager consentManager = new ConsentManagerImpl(configurationHolder);
        when(frameworkServiceDataHolder.getConsentManager()).thenReturn(consentManager);

        mockStatic(ConsentUtils.class);
        when(ConsentUtils.getTenantDomainFromCarbonContext()).thenReturn("carbon.super");
        mockRealmService(realmService);
        when(frameworkServiceDataHolder.getClaimMetadataManagementService()).thenReturn(claimMetadataManagementService);
        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/country");
        LocalClaim localClaim2 = new LocalClaim("http://wso2.org/claims/organization");
        localClaims.add(localClaim);
        localClaims.add(localClaim2);
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenReturn(localClaims);
        ConsentClaimsData consentClaimsData = ssoConsentService.
                getConsentRequiredClaimsWithExistingConsents(serviceProvider, authenticatedUser);
        assertEquals(consentClaimsData.getRequestedClaims().get(0).getClaimUri(),
                "http://wso2.org/claims/organization",
                "Incorrect requested claim URI");
        assertEquals(consentClaimsData.getMandatoryClaims().get(0).getClaimUri(),
                "http://wso2.org/claims/country",
                "Incorrect mandatory claim URI");
        assertNotNull(consentClaimsData.getMandatoryClaims().get(0).getClaimUri());
    }

    @Test
    public void testGetClaimsWithConsents() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("Travelocity.com");

        User user = new User();
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        serviceProvider.setOwner(user);

        ClaimConfig claimConfig = new ClaimConfig();
        Claim tempClaim = new Claim();
        tempClaim.setClaimUri(TEMPORARY_CLAIM_URI);
        ClaimMapping tempClaimMapping = new ClaimMapping();
        tempClaimMapping.setRequested(true);
        tempClaimMapping.setLocalClaim(tempClaim);
        tempClaimMapping.setRemoteClaim(tempClaim);

        claimConfig.setClaimMappings(new ClaimMapping[]{tempClaimMapping});
        serviceProvider.setClaimConfig(claimConfig);
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig
                = new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setSubjectClaimUri(null);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

        AuthenticatedUser authenticatedUser = getAuthenticatedUser();

        mockCarbonContextForTenant();
        mockStatic(FrameworkServiceDataHolder.class);
        when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);
        setConsentManagerConfigurationHolder();

        RealmService realmService = mock(RealmService.class);
        configurationHolder.setRealmService(realmService);
        ConsentManager consentManager = new ConsentManagerImpl(configurationHolder);
        when(frameworkServiceDataHolder.getConsentManager()).thenReturn(consentManager);

        mockStatic(ConsentUtils.class);
        when(ConsentUtils.getTenantDomainFromCarbonContext()).thenReturn("carbon.super");
        mockRealmService(realmService);

        assertNotNull(ssoConsentService.getClaimsWithConsents(serviceProvider, authenticatedUser));
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

    private void mockCarbonContextForTenant() {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockRealmService(RealmService realmService) throws Exception {

        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
        when(tenantManager.getDomain(SUPER_TENANT_ID)).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private AuthenticatedUser getAuthenticatedUser() {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier("");
        Map<ClaimMapping, String> userAttributes = new HashMap<>();

        Claim tempClaim1 = new Claim();
        tempClaim1.setClaimUri("http://wso2.org/claims/organization");
        ClaimMapping tempClaimMapping1 = new ClaimMapping();
        tempClaimMapping1.setRequested(false);
        tempClaimMapping1.setMandatory(false);
        tempClaimMapping1.setLocalClaim(tempClaim1);
        tempClaimMapping1.setRemoteClaim(tempClaim1);
        userAttributes.put(tempClaimMapping1, "WSO2");

        Claim tempClaim2 = new Claim();
        tempClaim2.setClaimUri("http://wso2.org/claims/country");
        ClaimMapping tempClaimMapping2 = new ClaimMapping();
        tempClaimMapping1.setRequested(false);
        tempClaimMapping1.setMandatory(false);
        tempClaimMapping2.setLocalClaim(tempClaim2);
        tempClaimMapping2.setRemoteClaim(tempClaim2);
        userAttributes.put(tempClaimMapping2, "Sri Lanka");

        authenticatedUser.setUserAttributes(userAttributes);
        authenticatedUser.setFederatedUser(false);
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserStoreDomain("PRIMARY");
        authenticatedUser.setUserName("alexy");
        return authenticatedUser;
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
    }
}
