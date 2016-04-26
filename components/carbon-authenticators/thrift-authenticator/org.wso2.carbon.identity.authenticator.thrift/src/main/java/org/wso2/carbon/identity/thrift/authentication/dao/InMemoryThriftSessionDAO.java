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

import org.wso2.carbon.utils.ThriftSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class to manipulate thrift session info in memory.
 */
public class InMemoryThriftSessionDAO implements ThriftSessionDAO {

    private ConcurrentMap<String, ThriftSession> thriftSessionMap = new ConcurrentHashMap<String, ThriftSession>();

    public InMemoryThriftSessionDAO() {

    }


    @Override
    public List<ThriftSession> getAllSessions() {
        return new ArrayList<ThriftSession>(thriftSessionMap.values());
    }

    @Override
    public boolean isSessionExisting(String sessionId) {
        return thriftSessionMap.containsKey(sessionId);
    }

    @Override
    public void addSession(ThriftSession session) {
        thriftSessionMap.put(session.getSessionId(), session);
    }

    @Override
    public void removeSession(String sessionId) {
        thriftSessionMap.remove(sessionId);

    }

    @Override
    public void updateLastAccessTime(String sessionId, long lastAccessTime) {
        ThriftSession thriftSession = thriftSessionMap.get(sessionId);
        if (thriftSession != null) {
            thriftSession.setLastAccess(lastAccessTime);
        }
    }

    @Override
    public ThriftSession getSession(String sessionId) {
        return thriftSessionMap.get(sessionId);
    }

    @Override
    public ThriftSessionDAO getInstance() {
        return this;
    }
}
