package org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "user",
        "code"
})
@XmlRootElement(name = "confirmSelfRegistrationRequest")
public class ConfirmSelfRegistrationRequest {
    @XmlElement(required = true)
    private User user;
    @XmlElement(required = true)
    private String code;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}