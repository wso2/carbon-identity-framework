/*
*  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.user.store.configuration.deployer.util;

public class UserStoreConfigurationConstants {

    public static final String ENC_EXTENSION = "enc";
    public static final String XML_EXTENSION = "xml";
    public static final String ENCRYPT_TEXT = "#encrypt";

    public static final String PROPERTY_CLASS = "class";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ENCRYPT = "encrypt";
    public static final String PROPERTY_ENCRYPTED = "encrypted";
    public static final String PROPERTY = "Property";

    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
    public static final String SECRET_ALIAS = "";

    //these changes are introduced in chunkccesible from chunk 1
    public static final String SERVER_KEYSTORE_FILE = "Security.KeyStore.Location";
    public static final String SERVER_KEYSTORE_TYPE = "Security.KeyStore.Type";
    public static final String SERVER_KEYSTORE_PASSWORD = "Security.KeyStore.Password";
    public static final String SERVER_KEYSTORE_KEY_ALIAS = "Security.KeyStore.KeyAlias";

    // Internal keystore related properties.
    public static final String INTERNAL_KEYSTORE = "InternalKeystore";
    public static final String SERVER_INTERNAL_KEYSTORE_FILE = "Security.InternalKeyStore.Location";
    public static final String SERVER_INTERNAL_KEYSTORE_TYPE = "Security.InternalKeyStore.Type";
    public static final String SERVER_INTERNAL_KEYSTORE_PASSWORD = "Security.InternalKeyStore.Password";
    public static final String SERVER_INTERNAL_KEYSTORE_KEY_ALIAS = "Security.InternalKeyStore.KeyAlias";
    public static final String ENCRYPTION_KEYSTORE = "Security.UserStorePasswordEncryption";
    public static final String CRYPTO_PROVIDER = "CryptoService.InternalCryptoProviderClassName";
    public static final String SYMMETRIC_KEY_CRYPTO_PROVIDER = "org.wso2.carbon.crypto.provider" +
            ".SymmetricKeyInternalCryptoProvider";


    private UserStoreConfigurationConstants() {

    }

}
