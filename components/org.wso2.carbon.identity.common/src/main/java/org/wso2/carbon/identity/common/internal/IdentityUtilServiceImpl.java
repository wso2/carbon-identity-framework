/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.util.jdbc.JDBCUtils;
import org.wso2.carbon.identity.common.util.passwordsafe.PasswordSafeUtils;
import org.wso2.carbon.identity.common.util.stream.StreamUtils;
import org.wso2.carbon.identity.common.util.keystore.KeyStoreUtils;
import org.wso2.carbon.identity.common.util.url.URLUtils;
import org.wso2.carbon.identity.common.util.validation.ValidationUtils;
import org.wso2.carbon.identity.common.util.xml.XMLUtils;
import org.wso2.carbon.identity.common.util.log.LogUtils;
import org.wso2.carbon.identity.common.util.IdentityUtilService;
import org.wso2.carbon.identity.common.util.IdentityUtils;

public class IdentityUtilServiceImpl implements IdentityUtilService {

    private static final Logger logger = LoggerFactory.getLogger(IdentityUtilServiceImpl.class);

    @Override
    public IdentityUtils getIdentityUtils() throws IdentityException {
        return IdentityUtils.getInstance();
    }

    @Override
    public JDBCUtils getJDBCUtils() throws IdentityException {
        return JDBCUtils.getInstance();
    }

    @Override
    public StreamUtils getStreamUtils() throws IdentityException {
        return StreamUtils.getInstance();
    }

    @Override
    public LogUtils getLogUtils() throws IdentityException {
        return LogUtils.getInstance();
    }

    @Override
    public PasswordSafeUtils getPasswordSafeUtils() throws IdentityException {
        return PasswordSafeUtils.getInstance();
    }

    @Override
    public KeyStoreUtils getKeyStoreUtils() throws IdentityException {
        return KeyStoreUtils.getInstance();
    }

    @Override
    public URLUtils getURLUtils() throws IdentityException {
        return URLUtils.getInstance();
    }

    @Override
    public XMLUtils getXMLUtils() throws IdentityException {
        return XMLUtils.getInstance();
    }

    @Override
    public ValidationUtils getValidationUtils() throws IdentityException {
        return ValidationUtils.getInstance();
    }
}
