/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.mgt.model;

import java.io.Serializable;

/**
 * DTO class for Executor.
 */
public class ExecutorDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String idpName;

    public ExecutorDTO() {

    }

    private ExecutorDTO(Builder builder) {

        this.name = builder.name;
        this.idpName = builder.idpName;
    }

    public ExecutorDTO(String name) {

        this.name = name;
    }

    public ExecutorDTO(String name, String idpName) {

        this.name = name;
        this.idpName = idpName;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIdpName() {

        return idpName;
    }

    public void setIdpName(String idpName) {

        this.idpName = idpName;
    }

    /**
     * Builder class to build {@link ExecutorDTO}.
     */
    public static class Builder {

        private String name;
        private String idpName;

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder idpName(String idpName) {

            this.idpName = idpName;
            return this;
        }

        public ExecutorDTO build() {

            return new ExecutorDTO(this);
        }
    }
}
