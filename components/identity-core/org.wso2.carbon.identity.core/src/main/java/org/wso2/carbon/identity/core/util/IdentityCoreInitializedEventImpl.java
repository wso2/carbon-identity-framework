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

/**
 * This empty service can be used to guarantee the order of activation ( No need to have empty service if
 * there is a valid service).
 * If we need to activate a component after org.wso2.carbon.identity.core is activated, within that service
 * can refer to this empty service which will guarantee that, org.wso2.carbon.identity.core will activated
 * before that.
 */

public class IdentityCoreInitializedEventImpl implements IdentityCoreInitializedEvent {
}
