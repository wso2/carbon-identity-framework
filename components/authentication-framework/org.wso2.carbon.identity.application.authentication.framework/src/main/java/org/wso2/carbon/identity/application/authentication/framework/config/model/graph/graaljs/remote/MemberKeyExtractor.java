/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import org.graalvm.polyglot.proxy.ProxyArray;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ContextPropertyResponse;

/**
 * Utility for extracting member keys from various container types returned by
 * {@link org.graalvm.polyglot.proxy.ProxyObject#getMemberKeys()}.
 * <p>
 * Handles three container types with nested array flattening:
 * <ul>
 *   <li>{@code String[]} — direct key extraction</li>
 *   <li>{@code Object[]} — with nested {@code String[]} flattening</li>
 *   <li>{@link ProxyArray} — with nested {@code String[]} and {@code Object[]} flattening</li>
 * </ul>
 */
final class MemberKeyExtractor {

    private MemberKeyExtractor() {
        // Utility class
    }

    /**
     * Extract member keys from the given keys object and add them to the response builder.
     *
     * @param keys            The keys object (String[], Object[], or ProxyArray).
     * @param responseBuilder The response builder to add keys to.
     */
    static void extractTo(Object keys, ContextPropertyResponse.Builder responseBuilder) {

        if (keys instanceof String[]) {
            for (String key : (String[]) keys) {
                responseBuilder.addMemberKeys(key);
            }
        } else if (keys instanceof Object[]) {
            for (Object key : (Object[]) keys) {
                if (key instanceof String[]) {
                    for (String s : (String[]) key) {
                        responseBuilder.addMemberKeys(s);
                    }
                } else {
                    responseBuilder.addMemberKeys(String.valueOf(key));
                }
            }
        } else if (keys instanceof ProxyArray) {
            ProxyArray proxyArray = (ProxyArray) keys;
            long size = proxyArray.getSize();
            for (long i = 0; i < size; i++) {
                Object element = proxyArray.get(i);
                if (element instanceof String[]) {
                    for (String s : (String[]) element) {
                        responseBuilder.addMemberKeys(s);
                    }
                } else if (element instanceof Object[]) {
                    for (Object o : (Object[]) element) {
                        responseBuilder.addMemberKeys(String.valueOf(o));
                    }
                } else {
                    responseBuilder.addMemberKeys(String.valueOf(element));
                }
            }
        }
    }
}
