package org.wso2.carbon.identity.entitlement.endpoint.resources.models;

import javax.xml.bind.annotation.*;

/**
 * Created by manujith on 5/22/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "subjectName",
        "resourceName",
        "subjectId",
        "action",
        "enableChildSearch"
})
@XmlRootElement(name = "EntitledAttributesRequest")
public class EntitledAttributesRequestModel {
    @XmlElement(required = false)
    private String subjectName;
    @XmlElement(required = false)
    private String resourceName;
    @XmlElement(required = false)
    private String subjectId;
    @XmlElement(required = false)
    private String action;
    @XmlElement(required = false)
    private boolean enableChildSearch;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEnableChildSearch() {
        return enableChildSearch;
    }

    public void setEnableChildSearch(boolean enableChildSearch) {
        this.enableChildSearch = enableChildSearch;
    }
}

