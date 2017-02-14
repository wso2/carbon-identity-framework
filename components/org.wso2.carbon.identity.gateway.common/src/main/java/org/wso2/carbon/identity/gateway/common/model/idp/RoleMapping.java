package org.wso2.carbon.identity.gateway.common.model.idp;

public class RoleMapping {
    private String localRoleName;
    private String idpRoleName;

    public String getIdpRoleName() {
        return idpRoleName;
    }

    public void setIdpRoleName(String idpRoleName) {
        this.idpRoleName = idpRoleName;
    }

    public String getLocalRoleName() {
        return localRoleName;
    }

    public void setLocalRoleName(String localRoleName) {
        this.localRoleName = localRoleName;
    }
}
