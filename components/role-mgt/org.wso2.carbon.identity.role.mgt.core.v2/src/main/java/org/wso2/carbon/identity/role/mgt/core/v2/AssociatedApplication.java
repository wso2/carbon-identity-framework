package org.wso2.carbon.identity.role.mgt.core.v2;

/**
 * Represents the Associated Application.
 */
public class AssociatedApplication {

    private String id;
    private String name;

    public AssociatedApplication(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}
