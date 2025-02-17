/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.pre.update.password.action.api.model;

import org.wso2.carbon.identity.certificate.management.model.Certificate;

/**
 * Password Sharing class.
 */
public class PasswordSharing {

    /**
     * Password Sharing Format Enum.
     */
    public enum Format {

        PLAIN_TEXT,
        SHA256_HASHED
    }

    private final Format format;
    private final Certificate certificate;

    public PasswordSharing(Builder builder) {

        this.format = builder.format;
        this.certificate = builder.certificate;
    }

    public Format getFormat() {

        return format;
    }

    public Certificate getCertificate() {

        return certificate;
    }

    /**
     * Password Sharing Builder.
     */
    public static class Builder {

        private Format format;
        private Certificate certificate;

        public Builder format(Format format) {

            this.format = format;
            return this;
        }

        public Builder certificate(Certificate certificate) {

            this.certificate = certificate;
            return this;
        }

        public PasswordSharing build() {

            return new PasswordSharing(this);
        }
    }
}
