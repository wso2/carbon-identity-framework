/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.exception;

/**
 * Wrapping class for the error status and the error status message in to a single object.
 */
public class I18nErrorCodeWrapper {

    private String status;
    private String statusMsg;

    public I18nErrorCodeWrapper(String status, String statusMsg) {

        this.status = status;
        this.statusMsg = statusMsg;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getStatusMsg() {

        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {

        this.statusMsg = statusMsg;
    }
}
