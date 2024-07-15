/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.trusted.app.mgt.utils;

/**
 * Constants for Trusted App Manager.
 */
public class Constants {

    public static final String CP_ANDROID_TRUSTED_APPS = "/.well-known/trusted-apps/android";
    public static final String CP_IOS_TRUSTED_APPS = "/.well-known/trusted-apps/ios";
    public static final String CT_APPLICATION_JSON = "application/json";
    public static final String HTTP_RESP_HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HTTP_RESP_HEADER_PRAGMA = "Pragma";
    public static final String HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE = "no-store";
    public static final String HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE = "no-cache";

    // Response object
    public static final String NAMESPACE_ATTRIBUTE = "namespace";
    public static final String ANDROID_APP_NAMESPACE_VALUE = "android_app";
    public static final String PACKAGE_NAME_ATTRIBUTE = "package_name";
    public static final String CERT_FINGERPRINT_ATTRIBUTE = "sha256_cert_fingerprints";
    public static final String RELATION_ATTRIBUTE = "relation";
    public static final String TARGET_ATTRIBUTE = "target";
    public static final String APPS_ATTRIBUTE = "apps";

    // Permissions
    public static final String ANDROID_CREDENTIAL_PERMISSION = "delegate_permission/common.get_login_creds";
    public static final String ANDROID_HANDLE_URLS_PERMISSION = "delegate_permission/common.handle_all_urls";
    public static final String IOS_CREDENTIAL_PERMISSION = "webcredentials";

    private Constants() {

    }
}
