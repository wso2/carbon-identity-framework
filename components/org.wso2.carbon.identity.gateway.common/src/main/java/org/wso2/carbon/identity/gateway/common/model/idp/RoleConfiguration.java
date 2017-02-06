package org.wso2.carbon.identity.gateway.common.model.idp;

import java.util.ArrayList;
import java.util.List;


public class RoleConfiguration {

    private List<RoleMapping> roleMappings = new ArrayList<>();

    public List<RoleMapping> getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(List<RoleMapping> roleMappings) {
        this.roleMappings = roleMappings;
    }
}
