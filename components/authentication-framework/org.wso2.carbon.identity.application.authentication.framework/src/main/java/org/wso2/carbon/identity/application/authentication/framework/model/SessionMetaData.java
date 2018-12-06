package org.wso2.carbon.identity.application.authentication.framework.model;

public class SessionMetaData {
    private String sessionId;
    private String property;
    private String value;
    private int insertedId;

    public SessionMetaData(String sessionId, String property, String value){
        this.sessionId=sessionId;
        this.property=property;
        this.value=value;
    }

    public SessionMetaData(int insertedId,String sessionId, String property, String value){
        this.insertedId=insertedId;
        this.sessionId=sessionId;
        this.property=property;
        this.value=value;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getInsertedId() {
        return insertedId;
    }

    public void setInsertedId(int insertedId) {
        this.insertedId = insertedId;
    }
}
