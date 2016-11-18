/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.keystore;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collection;

public interface KeyStoreService {

    Collection<String> listKeyStores() throws KeyStoreException;

    KeyStore getKeyStore(int keyStoreId) throws KeyStoreException;

    KeyStore getKeyStore(String keyStoreName) throws KeyStoreException;

    int addKeyStore(KeyStore keyStore) throws KeyStoreException;

    void updateKeyStore(int keyStoreId, KeyStore keyStore) throws KeyStoreException;

    void deleteStore(int keyStoreId) throws KeyStoreException;

    void addCertEntry(String keyStoreName, String alias, Certificate certificate) throws KeyStoreException;

    void addKeyEntry(String keyStoreName, String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException;

    void deleteEntry(String keyStoreName, String alias) throws KeyStoreException;

}
