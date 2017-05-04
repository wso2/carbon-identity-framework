/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.identity.core;

import java.security.Key;
import java.security.cert.Certificate;

/**
 * This interface is an extension point to provide the  keys that
 * is used to sign Id tokens, JWT etc. The new implementation
 * should be registered as an OSGi service.
 */
public interface KeyProviderService {

    Key getPrivateKey(String tenantDomain) throws Exception;

    Certificate getCertificate(String tenantDomain, String alias) throws Exception;

}
