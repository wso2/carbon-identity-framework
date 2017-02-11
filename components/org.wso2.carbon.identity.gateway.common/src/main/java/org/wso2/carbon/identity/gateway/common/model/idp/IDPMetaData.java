package org.wso2.carbon.identity.gateway.common.model.idp;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class IDPMetaData{

    private List<IDPCertificate> certificates = new ArrayList<>();
    private String federationHub ;
    private String homeRealm ;
    private RoleConfiguration roleConfiguration;
    private Properties properties = new Properties();

    public List<IDPCertificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<IDPCertificate> certificates) {
        this.certificates = certificates;
    }

    public String getFederationHub() {
        return federationHub;
    }

    public void setFederationHub(String federationHub) {
        this.federationHub = federationHub;
    }

    public String getHomeRealm() {
        return homeRealm;
    }

    public void setHomeRealm(String homeRealm) {
        this.homeRealm = homeRealm;
    }

    public RoleConfiguration getRoleConfiguration() {
        return roleConfiguration;
    }

    public void setRoleConfiguration(RoleConfiguration roleConfiguration) {
        this.roleConfiguration = roleConfiguration;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
