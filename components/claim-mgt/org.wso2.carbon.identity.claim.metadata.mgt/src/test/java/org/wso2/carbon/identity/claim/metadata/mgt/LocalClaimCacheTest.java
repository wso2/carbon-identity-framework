package org.wso2.carbon.identity.claim.metadata.mgt;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.LocalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test class for LocalClaimCache.
 */
@WithCarbonHome
public class LocalClaimCacheTest {

    private static final String TENANT_DOMAIN = "carbon.super";

    @BeforeMethod
    public void setUp() {
        // Any common setup logic can go here
    }

    /**
     * Test to verify that the LocalClaimCache instance is not null.
     */
    @Test
    public void testGetInstance() {

        try (MockedStatic<CarbonContext> carbonContextMock = mockStatic(CarbonContext.class)) {
            initializeCarbonContextMock(carbonContextMock);
            assertNotNull(LocalClaimCache.getInstance(), "LocalClaimCache instance should not be null");
        }
    }

    /**
     * Test to verify adding claims to the cache and retrieving them.
     */
    @Test
    public void testAddClaimsToCache() {

        try (MockedStatic<CarbonContext> carbonContextMock = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            initializeCarbonContextMocks(carbonContextMock, identityTenantUtilMock);
            LocalClaimCache claimCache = LocalClaimCache.getInstance();

            ArrayList<LocalClaim> initialClaims = createLocalClaims("http://wso2.org/claims/firstName");
            claimCache.addToCache(1, initialClaims, 1);
            assertEquals(claimCache.getValueFromCache(1, 1), initialClaims, "Cache should store the initial claims");

            ArrayList<LocalClaim> updatedClaims = createLocalClaims("http://wso2.org/claims/lastName");
            claimCache.addToCache(1, updatedClaims, 1);
            assertEquals(claimCache.getValueFromCache(1, 1), updatedClaims, "Cache should update with new claims");
        }
    }

    /**
     * Test to verify adding claims to the cache with a tenant domain and retrieving them.
     */
    @Test
    public void testAddClaimsToCacheWithTenantDomain() {

        try (MockedStatic<CarbonContext> carbonContextMock = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            initializeCarbonContextMocks(carbonContextMock, identityTenantUtilMock);
            LocalClaimCache claimCache = LocalClaimCache.getInstance();

            ArrayList<LocalClaim> initialClaims = createLocalClaims("http://wso2.org/claims/firstName");
            claimCache.addToCache(1, initialClaims, TENANT_DOMAIN);
            assertEquals(claimCache.getValueFromCache(1, TENANT_DOMAIN), initialClaims, "Cache should store the initial claims");

            ArrayList<LocalClaim> updatedClaims = createLocalClaims("http://wso2.org/claims/lastName");
            claimCache.addToCache(1, updatedClaims, TENANT_DOMAIN);
            assertEquals(claimCache.getValueFromCache(1, TENANT_DOMAIN), updatedClaims, "Cache should update with new claims");
        }
    }

    /**
     * Test to verify that adding claims to the cache with a null key does not overwrite existing values.
     */
    @Test
    public void testAddToCacheWithNullKey() {

        try (MockedStatic<CarbonContext> carbonContextMock = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            initializeCarbonContextMocks(carbonContextMock, identityTenantUtilMock);
            LocalClaimCache localClaimCache = LocalClaimCache.getInstance();

            ArrayList<LocalClaim> localClaims = createLocalClaims("http://wso2.org/claims/firstName");
            localClaimCache.addToCache(1, localClaims, 1);
            assertNotNull(localClaimCache.getValueFromCache(1, 1), "Cache should store the initial claims");

            ArrayList<LocalClaim> newLocalClaims = createLocalClaims("http://wso2.org/claims/lastName");
            localClaimCache.addToCache(null, newLocalClaims, 1);

            List<LocalClaim> cachedValue = localClaimCache.getValueFromCache(1, 1);
            assertEquals(cachedValue, localClaims, "Cache should not overwrite with null key");
        }
    }

    private void initializeCarbonContextMocks(MockedStatic<CarbonContext> carbonContextMock,
                                              MockedStatic<IdentityTenantUtil> identityTenantUtilMock) {

        initializeCarbonContextMock(carbonContextMock);
        identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
    }

    private void initializeCarbonContextMock(MockedStatic<CarbonContext> carbonContextMock) {

        CarbonContext carbonContext = mock(CarbonContext.class);
        carbonContextMock.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
    }

    private ArrayList<LocalClaim> createLocalClaims(String claimURI) {

        ArrayList<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim(claimURI);
        localClaim.setMappedAttribute(new AttributeMapping("primary", claimURI));
        localClaims.add(localClaim);
        return localClaims;
    }
}