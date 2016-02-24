/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.thrift;

public class ThriftConfigConstants {
    public static final String PARAM_ENABLE_THRIFT_SERVICE =
            "EntitlementSettings.ThirftBasedEntitlementConfig.EnableThriftService";
    public static final String PARAM_RECEIVE_PORT =
            "EntitlementSettings.ThirftBasedEntitlementConfig.ReceivePort";
    public static final String PARAM_CLIENT_TIMEOUT =
            "EntitlementSettings.ThirftBasedEntitlementConfig.ClientTimeout";
    public static final String PARAM_KEYSTORE_LOCATION =
            "EntitlementSettings.ThirftBasedEntitlementConfig.KeyStore.Location";
    public static final String PARAM_KEYSTORE_PASSWORD =
            "EntitlementSettings.ThirftBasedEntitlementConfig.KeyStore.Password";
    public static final String PARAM_HOST_NAME =
            "EntitlementSettings.ThirftBasedEntitlementConfig.ThriftHostName";
}
