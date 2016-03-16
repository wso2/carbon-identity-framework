/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.thrift.authentication.dao;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.utils.ThriftSession;

import java.util.List;

/**
 * Interface to manipulate thrift session info in database.
 */
public interface ThriftSessionDAO {

    List<ThriftSession> getAllSessions() throws IdentityException;

    boolean isSessionExisting(String sessionId) throws IdentityException;

    void addSession(ThriftSession session) throws IdentityException;

    void removeSession(String sessionId) throws IdentityException;

    void updateLastAccessTime(String sessionId, long lastAccessTime)
            throws IdentityException;

    ThriftSession getSession(String sessionId) throws IdentityException;

    ThriftSessionDAO getInstance();
}
