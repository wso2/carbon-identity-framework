package org.wso2.carbon.identity.application.role.mgt.model;

/**
 * Application role assigned group model.
 */
public class Group {

    private String groupId;
    private String groupName;
    private String idpId;
    private String idpName;

    public Group(String groupId, String idpId) {

        this.groupId = groupId;
        this.idpId = idpId;
    }

    public String getGroupId() {

        return groupId;
    }

    public void setGroupId(String groupId) {

        this.groupId = groupId;
    }

    public String getGroupName() {

        return groupName;
    }

    public void setGroupName(String groupName) {

        this.groupName = groupName;
    }

    public String getIdpId() {

        return idpId;
    }

    public void setIdpId(String idpId) {

        this.idpId = idpId;
    }

    public String getIdpName() {

        return idpName;
    }

    public void setIdpName(String idpName) {

        this.idpName = idpName;
    }
}
