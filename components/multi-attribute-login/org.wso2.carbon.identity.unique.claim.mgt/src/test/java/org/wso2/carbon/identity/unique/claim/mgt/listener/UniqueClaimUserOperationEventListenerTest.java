package org.wso2.carbon.identity.unique.claim.mgt.listener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.unique.claim.mgt.internal.UniqueClaimUserOperationDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniqueClaimUserOperationEventListenerTest {


    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final Logger log = LoggerFactory.getLogger(UniqueClaimUserOperationEventListenerTest.class);

    @InjectMocks
    private UniqueClaimUserOperationEventListener listener;

    @Mock
    private ClaimMetadataManagementService claimMetadataManagementService;

    @Mock
    private UserStoreManager userStoreManager;

    @Mock
    private RealmService realmService;

    @Mock
    private TenantManager tenantManager;

    @Mock
    private IdentityEventListenerConfig mockIdentityEventListenerConfig;

    private MockedStatic<IdentityUtil> identityUtilMock;

    private ClaimManager claimManager;

    @Before
    public void setUp() throws org.wso2.carbon.user.api.UserStoreException, ClaimMetadataException {

        MockitoAnnotations.openMocks(this);
        identityUtilMock = mockStatic(IdentityUtil.class);
        claimManager = mock(ClaimManager.class);

        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getDomain(anyInt())).thenReturn("carbon.super");
        UniqueClaimUserOperationDataHolder.getInstance().setRealmService(realmService);
        UniqueClaimUserOperationDataHolder.getInstance().setRealmService(realmService);
        UniqueClaimUserOperationDataHolder.getInstance().setClaimMetadataManagementService(
                claimMetadataManagementService);
        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim emailClaim = new LocalClaim(EMAIL_CLAIM_URI);
        emailClaim.setClaimProperty("isUnique", "true");
        localClaims.add(emailClaim);
        localClaims.add(new LocalClaim("http://wso2.org/claims/username"));
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenReturn(localClaims);


        identityUtilMock.when(() -> IdentityUtil.readEventListenerProperty(any(), any())).thenReturn(mockIdentityEventListenerConfig);
        Properties properties = new Properties();
        properties.put("ScopeWithinUserstore", "true");
        when(mockIdentityEventListenerConfig.getProperties()).thenReturn(properties);
    }

    @Test
    public void testCheckClaimUniquenessWithPasswordPolicyViolation() throws UserStoreException,
            NoSuchMethodException, IllegalAccessException {

        String username = "testUser";
        Map<String, String> claims = new HashMap<>();
        claims.put(EMAIL_CLAIM_URI, "test@example.com");
        String profile = "default";
        Object credential = "test@example.com";

        // Mock the necessary methods.
        when(userStoreManager.getTenantId()).thenReturn(1);
        when(userStoreManager.getClaimManager()).thenReturn(claimManager);
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        emailClaim.setDisplayTag("Email Address");
        when(claimManager.getClaim(anyString())).thenReturn(emailClaim);

        java.lang.reflect.Method method = UniqueClaimUserOperationEventListener.class.getDeclaredMethod("checkClaimUniqueness", String.class, Map.class, String.class, UserStoreManager.class, Object.class);
        method.setAccessible(true);


        try {
            method.invoke(listener, username, claims, profile, userStoreManager, credential);
        } catch (InvocationTargetException e) {
            assertEquals(e.getTargetException().getClass(), org.wso2.carbon.user.core.UserStoreException.class);
            assertTrue(e.getTargetException().getMessage().contains("Password cannot be equal to the value defined for Email Address!"));
        }
    }

    @Test
    public void testValidatePasswordNotEqualToClaims() throws Exception {

        Map<String, String> claims = new HashMap<>();
        claims.put(EMAIL_CLAIM_URI, "test@example.com");
        Object newCredential = "Wso2@test";

        // Mock the necessary methods.
        when(userStoreManager.getTenantId()).thenReturn(1);
        when(userStoreManager.getClaimManager()).thenReturn(claimManager);
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        emailClaim.setDisplayTag("Email Address");
        when(claimManager.getClaim(anyString())).thenReturn(emailClaim);

        // Use reflection to invoke the private method.
        java.lang.reflect.Method method = UniqueClaimUserOperationEventListener.class.getDeclaredMethod(
                "validatePasswordNotEqualToClaims", Map.class, UserStoreManager.class, Object.class);
        method.setAccessible(true);

        // Should not throw any exception.
        method.invoke(listener, claims, userStoreManager, newCredential);
    }

    @After
    public void tearDown() {

        identityUtilMock.close();
    }
}
