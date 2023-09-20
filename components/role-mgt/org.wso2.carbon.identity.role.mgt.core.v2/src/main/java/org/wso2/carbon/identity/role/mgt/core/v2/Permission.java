package org.wso2.carbon.identity.role.mgt.core.v2;

/**
 * Represents the permission.
 */
public class Permission {

    private String name;
    private String displayName;

    public Permission(String name, String displayName) {

        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }
}
