package org.wso2.carbon.identity.claim.mgt.dao;

import org.wso2.carbon.identity.claim.mgt.model.Claim;
import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.List;

public interface ClaimDAO {
    String LOCAL_NAME_DISPLAY_NAME = "DisplayName";
    String LOCAL_NAME_DESCRIPTION = "Description";
    String LOCAL_NAME_REQUIRED = "Required";
    String LOCAL_NAME_SUPPORTED_BY_DEFAULT = "SupportedByDefault";
    String LOCAL_NAME_REG_EX = "RegEx";
    String LOCAL_NAME_DISPLAY_ORDER = "DisplayOrder";
    String LOCAL_NAME_READ_ONLY = "ReadOnly";
    String LOCAL_NAME_CHECKED_ATTR = "CheckedAttribute";
    String LOCAL_CLAIM_URI = "http://wso2.org/claims";

    void addClaimMapping(org.wso2.carbon.user.api.ClaimMapping claim, int tenantId) throws UserStoreException;

    void updateClaim(org.wso2.carbon.user.api.ClaimMapping claim, int tenantId) throws UserStoreException;

    void deleteClaimMapping(org.wso2.carbon.user.api.ClaimMapping claimMapping, int tenantId) throws UserStoreException;

    void deleteDialect(String dialectUri, int tenantId) throws UserStoreException;

    boolean addClaimMappings(ClaimMapping[] claims, int tenantId) throws UserStoreException;

    int getDialectCount(int tenantId) throws UserStoreException;

    List<ClaimMapping> loadClaimMappings(int tenantId) throws UserStoreException;

    String getMappedAttribute(String claimURI, int tenantId) throws UserStoreException;

    String getMappedAttribute(String claimURI, int tenantId, String domain) throws UserStoreException;

    Claim getClaim(String claimURI, int tenantId) throws UserStoreException;

    ClaimMapping getClaimMapping(String claimURI, int tenantId) throws UserStoreException;

    List<ClaimMapping> loadClaimMappings(int tenantId, String dialectURI) throws UserStoreException;

}
