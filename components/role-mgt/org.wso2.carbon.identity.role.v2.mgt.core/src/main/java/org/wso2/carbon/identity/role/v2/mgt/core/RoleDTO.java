package org.wso2.carbon.identity.role.v2.mgt.core;

/**
 * Represents the role dto.
 */
public class RoleDTO {

    private String name;
    private String id;
    private int audienceRefId;
    private RoleAudience roleAudience;

    public RoleDTO(String name, int audienceRefId) {

        this.name = name;
        this.audienceRefId = audienceRefId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public int getAudienceRefId() {

        return audienceRefId;
    }

    public void setAudienceRefId(int audienceRefId) {

        this.audienceRefId = audienceRefId;
    }

    public RoleAudience getRoleAudience() {

        return roleAudience;
    }

    public void setRoleAudience(RoleAudience roleAudience) {

        this.roleAudience = roleAudience;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }
}
