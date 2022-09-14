/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.conn.ssl.AbstractVerifier;

import javax.net.ssl.SSLException;

/**
 * Hostname verifier that will be used in the Self Registration flow.
 */
public class SelfRegistrationHostnameVerifier extends AbstractVerifier {

    private final static String[] LOCALHOSTS = { "::1", "127.0.0.1", "localhost", "localhost.localdomain" };

    @Override
    public void verify(String s, String[] strings, String[] subjectAlts) throws SSLException {

        String[] subjectAltsWithLocalhosts = ArrayUtils.addAll(subjectAlts, LOCALHOSTS);
        this.verify(s, strings, subjectAltsWithLocalhosts, false);
    }
}
