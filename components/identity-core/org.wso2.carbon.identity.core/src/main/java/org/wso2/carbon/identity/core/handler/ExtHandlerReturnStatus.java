/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.handler;

/*
 * Return value from extension handlers. Used to control the invocation of handler chain.
 */
public enum ExtHandlerReturnStatus {

    CONTINUE("continue"),
    BREAK("break"),
    PROCESS_ONLY("process_only"),
    PRE_HANLDERS_ONLY("pre_handlers_only"),
    POST_HANDLERS_ONLY("post_handlers_only"),
    SKIP_PROCESS("skip_process"),
    SKIP_PRE_HANLDERS("skip_pre_handlers"),
    SKIP_POST_HANLDERS("skip_post_handlers");

    private String returnStatus;

    ExtHandlerReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Override
    public String toString() {
        return returnStatus;
    }

}
