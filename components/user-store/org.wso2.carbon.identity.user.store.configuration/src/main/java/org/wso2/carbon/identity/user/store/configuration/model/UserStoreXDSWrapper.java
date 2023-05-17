package org.wso2.carbon.identity.user.store.configuration.model;

import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

/**
 * This class is used to wrap the UserStoreDTO object with the domain name and the operation type.
 */
public class UserStoreXDSWrapper implements XDSWrapper {

    private UserStoreDTO userStoreDTO;
    private String domain;
    private String[] domains;
    private boolean isStateChanged;
    private boolean isDisabled;
    private String repositoryClass;
    private String previousDomainName;
    private String timestamp;

    public UserStoreDTO getUserStoreDTO() {
        return userStoreDTO;
    }

    public String getDomain() {
        return domain;
    }

    public String[] getDomains() {
        return domains;
    }

    public boolean isStateChanged() {
        return isStateChanged;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public String getRepositoryClass() {
        return repositoryClass;
    }

    public String getPreviousDomainName() {
        return previousDomainName;
    }

    public UserStoreXDSWrapper (UserStoreXDSWrapperBuilder builder) {

        this.userStoreDTO = builder.userStoreDTO;
        this.domain = builder.domain;
        this.domains = builder.domains;
        this.isStateChanged = builder.isStateChanged;
        this.isDisabled = builder.isDisabled;
        this.repositoryClass = builder.repositoryClass;
        this.previousDomainName = builder.previousDomainName;
        this.timestamp = builder.timestamp;
    }

    /**
     * This class is used to build the UserStoreXDSWrapper object.
     */
    public static class UserStoreXDSWrapperBuilder {

        private UserStoreDTO userStoreDTO;
        private String domain;
        private String[] domains;
        private boolean isStateChanged;
        private boolean isDisabled;
        private String repositoryClass;
        private String previousDomainName;
        private String timestamp;

        public UserStoreXDSWrapperBuilder setUserStoreDTO(UserStoreDTO userStoreDTO) {

            this.userStoreDTO = userStoreDTO;
            return this;
        }

        public UserStoreXDSWrapperBuilder setDomain(String domain) {

            this.domain = domain;
            return this;
        }

        public UserStoreXDSWrapperBuilder setDomains(String[] domains) {

            this.domains = domains;
            return this;
        }

        public UserStoreXDSWrapperBuilder setIsStateChanged(boolean isStateChanged) {

            this.isStateChanged = isStateChanged;
            return this;
        }

        public UserStoreXDSWrapperBuilder setIsDisabled(boolean isDisabled) {

            this.isDisabled = isDisabled;
            return this;
        }

        public UserStoreXDSWrapperBuilder setRepositoryClass(String repositoryClass) {

            this.repositoryClass = repositoryClass;
            return this;
        }

        public UserStoreXDSWrapperBuilder setPreviousDomainName(String previousDomainName) {

            this.previousDomainName = previousDomainName;
            return this;
        }

        public UserStoreXDSWrapper build() {

            this.timestamp = String.valueOf(System.currentTimeMillis());
            return new UserStoreXDSWrapper(this);
        }

    }

}
