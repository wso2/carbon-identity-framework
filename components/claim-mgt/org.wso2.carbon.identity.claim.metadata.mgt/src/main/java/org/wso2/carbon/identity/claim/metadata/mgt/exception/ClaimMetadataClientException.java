/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.exception;

import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;

/**
 * A custom Java {@code Exception} class used for the claim metadata management client error handling.
 */
public class ClaimMetadataClientException extends ClaimMetadataException {

    private static final long serialVersionUID = -3538539053023864717L;

    public ClaimMetadataClientException(String errorCode, String errorDescription) {

        super(errorCode, errorDescription);
    }

    public ClaimMetadataClientException(ClaimConstants.ErrorMessage error) {

        super(error.getCode(), error.getMessage());
    }
}
