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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import java.util.Set;

/**
 * Translates ACR and AMR identifiers which is on the external protocol to internal representation and vice versa.
 * The ACR values are protocol specific identifiers.
 * The AMR values are protocol specific identifiers.
 *
 */
public interface AuthenticationMethodNameTranslator {

    /**
     * Translates the internal AMR to external protocol specific values.
     * The internal ACR may correspond to multiple external AMR values.
     * @param externalAmr
     * @param protocol
     * @return List of protocol specific ACR mapped to internal ACR. Usually this will be one.
     */
    String translateToInternalAmr(String externalAmr, String protocol);

    /**
     * Translates the internal AMR to external protocol specific values.
     * The internal AMR may correspond to multiple external AMR values.
     * @param internalAmr
     * @param protocol
     * @return List of protocol specific AMR mapped to internal AMR. Usually this will be one.
     */
    Set<String> translateToExternalAmr(String internalAmr, String protocol);
}
