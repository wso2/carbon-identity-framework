package org.wso2.carbon.identity.entitlement.endpoint.resources.models;

import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;

import javax.xml.bind.annotation.*;

/**
 * Created by manujith on 5/22/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "identifier",
        "givenAttributes"
})
@XmlRootElement(name = "AllEntitlementsRequest")
public class AllEntitlementsRequestModel {
    @XmlElement(required = false)
    private String identifier;
    @XmlElement(required = false)
    private AttributeDTO[] givenAttributes;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public AttributeDTO[] getGivenAttributes() {
        return givenAttributes;
    }

    public void setGivenAttributes(AttributeDTO[] givenAttributes) {
        this.givenAttributes = givenAttributes;
    }
}

