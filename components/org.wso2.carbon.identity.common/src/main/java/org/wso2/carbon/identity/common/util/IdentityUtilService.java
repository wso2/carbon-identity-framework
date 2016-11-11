/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.util;

import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.util.IdentityUtils;
import org.wso2.carbon.identity.common.util.jdbc.JDBCUtils;
import org.wso2.carbon.identity.common.util.keystore.KeyStoreUtils;
import org.wso2.carbon.identity.common.util.log.LogUtils;
import org.wso2.carbon.identity.common.util.passwordsafe.PasswordSafeUtils;
import org.wso2.carbon.identity.common.util.stream.StreamUtils;
import org.wso2.carbon.identity.common.util.url.URLUtils;
import org.wso2.carbon.identity.common.util.validation.ValidationUtils;
import org.wso2.carbon.identity.common.util.xml.XMLUtils;

public interface IdentityUtilService {

    JDBCUtils getJDBCUtils() throws IdentityException;

    KeyStoreUtils getKeyStoreUtils() throws IdentityException;

    LogUtils getLogUtils() throws IdentityException;

    PasswordSafeUtils getPasswordSafeUtils() throws IdentityException;

    StreamUtils getStreamUtils() throws IdentityException;

    URLUtils getURLUtils() throws IdentityException;

    ValidationUtils getValidationUtils() throws IdentityException;

    XMLUtils getXMLUtils() throws IdentityException;

    IdentityUtils getIdentityUtils() throws IdentityException;

}
