/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception;

/**
 * This exception will be used to indicate when consent management for SSO is disabled. Extends of
 * {@link SSOConsentServiceException}.
 */
public class SSOConsentDisabledException extends SSOConsentServiceException {

    public SSOConsentDisabledException(String message) {
        super(message);
    }

    public SSOConsentDisabledException(String errorCode, String message) {
        super(errorCode, message);
    }

    public SSOConsentDisabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSOConsentDisabledException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
