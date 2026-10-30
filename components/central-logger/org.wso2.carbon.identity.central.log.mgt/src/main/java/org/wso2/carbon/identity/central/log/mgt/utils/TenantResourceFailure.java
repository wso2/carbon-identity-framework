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

package org.wso2.carbon.identity.central.log.mgt.utils;

/**
 * Marker for exceptions caused by a resource configured by a tenant administrator, such as an action endpoint,
 * a tenant level email/SMS provider or an external connection. Such failures are reported through diagnostic
 * logs and are logged via {@link LoggerUtils#logTenantResourceFailure} instead of as server errors.
 * Must not be used for failures of resources configured at the server level.
 */
public interface TenantResourceFailure {

}
