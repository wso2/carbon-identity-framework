package org.wso2.carbon.identity.role.mgt.core.v2;

public class RoleAudience {

    private String audience;
    private String audienceId;
    private String audienceName;


    public RoleAudience(String audience, String audienceId) {

        this.audience = audience;
        this.audienceId = audienceId;
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
