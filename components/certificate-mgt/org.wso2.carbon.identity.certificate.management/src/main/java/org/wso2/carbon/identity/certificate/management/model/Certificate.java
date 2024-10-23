/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.certificate.management.model;

/**
 * This class holds the certificate information.
 */
public class Certificate {

    private String id;
    private String name;
    private String certificate;

    public Certificate() {
    }

    public Certificate(Builder certificateBuilder) {

        this.id = certificateBuilder.id;
        this.name = certificateBuilder.name;
        this.certificate = certificateBuilder.certificate;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getCertificate() {

        return certificate;
    }

    /**
     * Certificate builder.
     */
    public static class Builder {

        private String id;
        private String name;
        private String certificate;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder certificate(String certificate) {

            this.certificate = certificate;
            return this;
        }

        public Certificate build() {

            return new Certificate(this);
        }
    }
}
