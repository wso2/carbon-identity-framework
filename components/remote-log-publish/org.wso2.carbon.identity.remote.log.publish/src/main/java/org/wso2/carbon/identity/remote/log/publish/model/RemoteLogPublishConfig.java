/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.identity.remote.log.publish.model;

/**
 * Class for Remote Server Logger Information.
 */
public class RemoteLogPublishConfig {

    private String uuid;
    private String url;
    private int connectTimeoutMillis;
    private boolean verifyHostname = true;
    private String logType;
    private String username;
    private String password;
    private String keystoreLocation;
    private String keystorePassword;
    private String truststoreLocation;
    private String truststorePassword;

    public RemoteLogPublishConfig() {

    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public int getConnectTimeoutMillis() {

        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {

        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public String getLogType() {

        return logType;
    }

    public void setLogType(String logType) {

        this.logType = logType;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getKeystoreLocation() {

        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {

        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {

        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {

        this.keystorePassword = keystorePassword;
    }

    public String getTruststoreLocation() {

        return truststoreLocation;
    }

    public void setTruststoreLocation(String truststoreLocation) {

        this.truststoreLocation = truststoreLocation;
    }

    public String getTruststorePassword() {

        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {

        this.truststorePassword = truststorePassword;
    }

    public boolean isVerifyHostname() {

        return verifyHostname;
    }

    public void setVerifyHostname(boolean verifyHostname) {

        this.verifyHostname = verifyHostname;
    }
}