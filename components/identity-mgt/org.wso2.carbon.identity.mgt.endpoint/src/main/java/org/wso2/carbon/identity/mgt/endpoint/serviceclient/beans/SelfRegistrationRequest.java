package org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans;

import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.Property;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "user",
        "password",
        "claims",
        "properties"
})
@XmlRootElement(name = "selfRegistrationRequest")
public class SelfRegistrationRequest {
    @XmlElement(required = true)
    private User user;

    @XmlElement(required = true)
    private Claim[] claims;

    @XmlElement(required = false)
    private Property[] properties;


    @XmlElement(required = true)
    private String password;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Claim[] getClaims() {
        return claims;
    }

    public void setClaims(Claim[] claims) {
        this.claims = claims;
    }

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}