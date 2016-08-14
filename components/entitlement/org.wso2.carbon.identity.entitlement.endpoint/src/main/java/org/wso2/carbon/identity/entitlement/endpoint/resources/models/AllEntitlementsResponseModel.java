package org.wso2.carbon.identity.entitlement.endpoint.resources.models;

import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;

import javax.xml.bind.annotation.*;

/**
 * Created by manujith on 5/22/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "entitledResultSetDTO"
})
@XmlRootElement(name = "AllEntitlementsResponse")
public class AllEntitlementsResponseModel {
    @XmlElement(required = true)
    private EntitledResultSetDTO entitledResultSetDTO;

    public EntitledResultSetDTO getEntitledResultSetDTO() {
        return entitledResultSetDTO;
    }

    public void setEntitledResultSetDTO(EntitledResultSetDTO entitledResultSetDTO) {
        this.entitledResultSetDTO = entitledResultSetDTO;
    }
}

