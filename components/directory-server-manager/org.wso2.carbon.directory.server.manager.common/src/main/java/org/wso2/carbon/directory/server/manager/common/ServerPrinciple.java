/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.directory.server.manager.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates a server principle. Has getters and setters to modify
 * server principle attributes.
 */
@SuppressWarnings({"ALL"})
@XmlRootElement
public class ServerPrinciple implements Comparable, Serializable {

    private String serverName;
    private String serverDescription;
    private String serverPassword;

    public ServerPrinciple(String serverName) {
        this.serverName = serverName;
    }

    public ServerPrinciple(String serverName, String serverDescription) {
        this.serverName = serverName;
        this.serverDescription = serverDescription;
    }

    public ServerPrinciple(String serverName, String serverDescription, String serverPassword) {
        this.serverName = serverName;
        this.serverDescription = serverDescription;
        this.serverPassword = serverPassword;
    }

    public ServerPrinciple() {
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    @Override
    public int compareTo(Object o) {
        ServerPrinciple principle = (ServerPrinciple) o;
        return this.serverName.compareTo(principle.getServerName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerPrinciple that = (ServerPrinciple) o;

        if (!serverName.equals(that.serverName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return serverName.hashCode();
    }
}
