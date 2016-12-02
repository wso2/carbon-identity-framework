package org.wso2.carbon.identity.gateway.model;

import java.io.Serializable;

public class UserClaim implements Serializable {

    private String uri;
    private String value;

    public UserClaim(String uri, String value) {
        this.uri = uri;
        this.value = value;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
