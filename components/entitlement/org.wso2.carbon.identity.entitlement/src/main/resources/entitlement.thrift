/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

namespace java org.wso2.carbon.identity.entitlement.thrift

exception EntitlementException {
    1: required string message
}

service EntitlementService {
   string getDecision (
 	1: required string request
	2: required string sessionId) throws (1:EntitlementException ee)
   string getDecisionByAttributes (
 	1: required string subject
	2: required string resource
	3: required string action
	4: required list<string> environment
	5: required string sessionId) throws (1:EntitlementException ee)
}
