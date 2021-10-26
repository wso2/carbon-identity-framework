/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.idp.mgt.secretprocessor;

/**
 * PlainTextPersistenceProcessor stores identity provider related secrets in plain text in the database.
 */
public class PlainTextPersistenceProcessor implements SecretPersistenceProcessor {

    @Override
    public String addSecret(int idpId, String name, String propertyName, String secretValue, String secretType) {

        return secretValue;
    }

    @Override
    public String getPreprocessedSecret(String secretId) {

        return secretId;
    }

    @Override
    public void deleteSecret(String secretId) {

    }

    @Override
    public String updateSecret(String secretId, String secretValue) {

        return secretValue;
    }
}
