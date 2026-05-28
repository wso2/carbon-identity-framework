/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.idp.extension;

import org.wso2.carbon.identity.debug.framework.extension.DebugTypeProvider;

/**
 * Extension of {@link DebugTypeProvider} for Identity Provider debug implementations.
 * Providers declare which authenticator names they handle; the {@code IdpDebugProviderRegistry}
 * iterates registered providers and delegates to the first one whose
 * {@link #supportsAuthenticator(String)} returns {@code true}.
 */
public interface IdpDebugTypeProvider extends DebugTypeProvider {

    /**
     * Returns {@code true} if this provider can handle IdPs whose enabled authenticator
     * implementation name matches.
     *
     * @param authenticatorName the authenticator name from the IdP's federated authenticator config.
     * @return {@code true} if this provider handles the given authenticator name.
     */
    boolean supportsAuthenticator(String authenticatorName);
}
