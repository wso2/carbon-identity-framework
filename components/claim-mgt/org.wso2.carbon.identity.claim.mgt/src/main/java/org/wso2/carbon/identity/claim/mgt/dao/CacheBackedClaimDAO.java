package org.wso2.carbon.identity.claim.mgt.dao;

import org.wso2.carbon.identity.claim.mgt.cache.MappedAttributeCache;
import org.wso2.carbon.identity.claim.mgt.cache.MappedAttributeCacheEntry;
import org.wso2.carbon.identity.claim.mgt.cache.MappedAttributeCacheKey;
import org.wso2.carbon.identity.claim.mgt.model.Claim;
import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.List;

public class CacheBackedClaimDAO implements ClaimDAO {
    MappedAttributeCache mappedAttributeCache = MappedAttributeCache.getInstance();
    ClaimDAO claimDAO = new ClaimDAOImpl();

    @Override
    public void addClaimMapping(org.wso2.carbon.user.api.ClaimMapping claim, int tenantId) throws UserStoreException {
        claimDAO.addClaimMapping(claim, tenantId);
    }

    @Override
    public void updateClaim(org.wso2.carbon.user.api.ClaimMapping claim, int tenantId) throws UserStoreException {
        claimDAO.updateClaim(claim, tenantId);
    }

    @Override
    public void deleteClaimMapping(org.wso2.carbon.user.api.ClaimMapping claimMapping, int tenantId) throws
            UserStoreException {
        claimDAO.deleteClaimMapping(claimMapping, tenantId);
    }

    @Override
    public void deleteDialect(String dialectUri, int tenantId) throws UserStoreException {
        claimDAO.deleteDialect(dialectUri, tenantId);
    }

    @Override
    public boolean addClaimMappings(ClaimMapping[] claims, int tenantId) throws UserStoreException {
        return claimDAO.addClaimMappings(claims, tenantId);
    }

    @Override
    public int getDialectCount(int tenantId) throws UserStoreException {
        return claimDAO.getDialectCount(tenantId);
    }

    @Override
    public List<ClaimMapping> loadClaimMappings(int tenantId) throws UserStoreException {
        //TODO: need to add cache implementation
        return claimDAO.loadClaimMappings(tenantId);
    }

    @Override
    public String getMappedAttribute(String claimURI, int tenantId) throws UserStoreException {
        MappedAttributeCacheEntry mappedAttributeCacheEntry = mappedAttributeCache.getValueFromCache(new
                MappedAttributeCacheKey(claimURI, tenantId));
        if (mappedAttributeCacheEntry != null) {
            return mappedAttributeCacheEntry.getMappedAttribute();
        } else {
            return claimDAO.getMappedAttribute(claimURI, tenantId);
        }
    }

    @Override
    public String getMappedAttribute(String claimURI, int tenantId, String domain) throws UserStoreException {
        MappedAttributeCacheEntry mappedAttributeCacheEntry = mappedAttributeCache.getValueFromCache(new
                MappedAttributeCacheKey(claimURI, tenantId, domain));
        if (mappedAttributeCacheEntry != null) {
            return mappedAttributeCacheEntry.getMappedAttribute();
        } else {
            return claimDAO.getMappedAttribute(claimURI, tenantId);
        }
    }

    @Override
    public Claim getClaim(String claimURI, int tenantId) throws UserStoreException {
        //TODO: need to add cache implementation
        return claimDAO.getClaim(claimURI, tenantId);
    }

    @Override
    public ClaimMapping getClaimMapping(String claimURI, int tenantId) throws UserStoreException {
        //TODO: need to add cache implementation
        return claimDAO.getClaimMapping(claimURI, tenantId);
    }

    public List<ClaimMapping> loadClaimMappings(int tenantId, String dialectURI) throws UserStoreException {
        //TODO: need to add cache implementation
        return claimDAO.loadClaimMappings(tenantId, dialectURI);
    }
}
