/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.util;

public class IdentityCoreConstants {

    public static final String IDENTITY_CONFIG = "identity.xml";
    public static final String IDENTITY_DEFAULT_NAMESPACE = "http://wso2.org/projects/carbon/carbon.xml";
    public static final String HOST_NAME = "HostName";
    public static final String FILE_NAME_REGEX = "FileNameRegEx";
    public static final String PORTS_OFFSET = "Ports.Offset";

    public static final String PROXY_CONTEXT_PATH = "ProxyContextPath";
    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final String UTF_8 = "UTF-8";
    public static final String UTC = "UTC";
    public static final int EVENT_LISTENER_ORDER_ID = -1;

    public static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    public static final String USER_NOT_FOUND = "UserNotFound";
    public static final String EXISTING_USER = "UserAlreadyExisting";
    public final static String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    public final static String MULTI_ATTRIBUTE_SEPARATOR_DEFAULT = ",,,";

    public static final String XML_SIGNATURE_ALGORITHM_RSA_SHA1_URI = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String XML_DIGEST_ALGORITHM_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    public static final String WEB_CONTEXT_ROOT = "WebContextRoot";
    
    private IdentityCoreConstants(){
    }
}
