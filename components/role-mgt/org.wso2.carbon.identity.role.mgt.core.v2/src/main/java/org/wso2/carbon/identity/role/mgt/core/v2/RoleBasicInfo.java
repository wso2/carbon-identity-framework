package org.wso2.carbon.identity.role.mgt.core.v2;

import org.wso2.carbon.identity.role.mgt.core.Entity;

/**
 * Represents the basic attributes of role.
 */
public class RoleBasicInfo extends Entity {

    private String audience;
    private String audienceId;
    private String audienceName;
    public RoleBasicInfo() {

    }

    public RoleBasicInfo(String id, String name) {

        super(id, name);
    }

    public String getAudience() {

        return audience;
    }

    public void setAudience(String audience) {

        this.audience = audience;
    }

    public String getAudienceId() {

        return audienceId;
    }

    public void setAudienceId(String audienceId) {

        this.audienceId = audienceId;
    }

    public String getAudienceName() {

        return audienceName;
    }

    public void setAudienceName(String audienceName) {

        this.audienceName = audienceName;
    }
}
