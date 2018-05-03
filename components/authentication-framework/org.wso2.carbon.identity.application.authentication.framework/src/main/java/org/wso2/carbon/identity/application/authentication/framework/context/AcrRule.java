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

package org.wso2.carbon.identity.application.authentication.framework.context;

/**
 * exact: where the authentication context statement in the assertion must be the exact match of, at least, one of the authentication contexts specified.
 * minimum: where the authentication context statement in the assertion must be, at least, as strong (as deemed by the identity provider) one of the authentication contexts specified.
 * maximum: where the authentication context statement in the assertion must be no stronger than any of the authentication contexts specified.
 * better: where the authentication context statement in the assertion must be stronger than any of the authentication contexts specified.
 */
public enum AcrRule {
    EXACT,
    MINIMUM,
    MAXIMUM,
    BETTER
}
