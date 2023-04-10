package org.wso2.carbon.identity.claim.metadata.mgt.model;

import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

import java.util.List;

/**
 * This class is used to update the claim.
 */
public class ClaimXDSWrapper implements XDSWrapper {

    private ClaimDialect claimDialect;
    private ClaimDialect oldClaimDialect;
    private ClaimDialect newClaimDialect;
    private LocalClaim localClaim;
    private List<LocalClaim> localClaimList;
    private String userStoreDomainName;
    private String localClaimURI;
    private ExternalClaim externalClaim;
    private String externalClaimDialectURI;
    private String externalClaimURI;
    private String tenantDomain;
    private int tenantId;

    public ClaimXDSWrapper(GRPCUpdateClaimBuilder builder) {

            this.claimDialect = builder.claimDialect;
            this.oldClaimDialect = builder.oldClaimDialect;
            this.newClaimDialect = builder.newClaimDialect;
            this.localClaim = builder.localClaim;
            this.localClaimList = builder.localClaimList;
            this.userStoreDomainName = builder.userStoreDomainName;
            this.localClaimURI = builder.localClaimURI;
            this.externalClaim = builder.externalClaim;
            this.externalClaimDialectURI = builder.externalClaimDialectURI;
            this.externalClaimURI = builder.externalClaimURI;
            this.tenantDomain = builder.tenantDomain;
            this.tenantId = builder.tenantId;
    }
    public ClaimDialect getClaimDialect() {
        return claimDialect;
    }

    public ClaimDialect getOldClaimDialect() {
        return oldClaimDialect;
    }

    public ClaimDialect getNewClaimDialect() {
        return newClaimDialect;
    }

    public LocalClaim getLocalClaim() {
        return localClaim;
    }

    public List<LocalClaim> getLocalClaimList() {
        return localClaimList;
    }

    public String getUserStoreDomainName() {
        return userStoreDomainName;
    }

    public String getLocalClaimURI() {
        return localClaimURI;
    }

    public ExternalClaim getExternalClaim() {
        return externalClaim;
    }

    public String getExternalClaimDialectURI() {
        return externalClaimDialectURI;
    }

    public String getExternalClaimURI() {
        return externalClaimURI;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public static class GRPCUpdateClaimBuilder {

        private ClaimDialect claimDialect;
        private ClaimDialect oldClaimDialect;
        private ClaimDialect newClaimDialect;
        private LocalClaim localClaim;
        private List<LocalClaim> localClaimList;
        private String userStoreDomainName;
        private String localClaimURI;
        private ExternalClaim externalClaim;
        private String externalClaimDialectURI;
        private String externalClaimURI;
        private String tenantDomain;
        private int tenantId;

        public GRPCUpdateClaimBuilder setClaimDialect(ClaimDialect claimDialect) {
            this.claimDialect = claimDialect;
            return this;
        }

        public GRPCUpdateClaimBuilder setOldClaimDialect(ClaimDialect oldClaimDialect) {
            this.oldClaimDialect = oldClaimDialect;
            return this;
        }

        public GRPCUpdateClaimBuilder setNewClaimDialect(ClaimDialect newClaimDialect) {
            this.newClaimDialect = newClaimDialect;
            return this;
        }

        public GRPCUpdateClaimBuilder setLocalClaim(LocalClaim localClaim) {
            this.localClaim = localClaim;
            return this;
        }

        public GRPCUpdateClaimBuilder setLocalClaimList(List<LocalClaim> localClaimList) {
            this.localClaimList = localClaimList;
            return this;
        }

        public GRPCUpdateClaimBuilder setUserStoreDomainName(String userStoreDomainName) {
            this.userStoreDomainName = userStoreDomainName;
            return this;
        }

        public GRPCUpdateClaimBuilder setLocalClaimURI(String localClaimURI) {
            this.localClaimURI = localClaimURI;
            return this;
        }

        public GRPCUpdateClaimBuilder setExternalClaim(ExternalClaim externalClaim) {
            this.externalClaim = externalClaim;
            return this;
        }

        public GRPCUpdateClaimBuilder setExternalClaimDialectURI(String externalClaimDialectURI) {
            this.externalClaimDialectURI = externalClaimDialectURI;
            return this;
        }

        public GRPCUpdateClaimBuilder setExternalClaimURI(String externalClaimURI) {
            this.externalClaimURI = externalClaimURI;
            return this;
        }

        public GRPCUpdateClaimBuilder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public GRPCUpdateClaimBuilder setTenantId(int tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public ClaimXDSWrapper build() {
            return new ClaimXDSWrapper(this);
        }
    }
}
