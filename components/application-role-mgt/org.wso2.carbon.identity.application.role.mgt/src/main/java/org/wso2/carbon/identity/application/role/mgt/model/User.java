package org.wso2.carbon.identity.application.role.mgt.model;

/**
 * Application role assigned user model.
 */
public class User {

    private String id;
    private String userName;

    public User(String id, String userName) {

        this.id = id;
        this.userName = userName;
    }

    public User(String id) {

        this.id = id;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }
}
