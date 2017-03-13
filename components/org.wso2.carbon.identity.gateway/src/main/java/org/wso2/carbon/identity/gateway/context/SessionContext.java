/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.context;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SessionContext implements Serializable {

    private static final long serialVersionUID = 530738975377939422L;
    private Map<String, SequenceContext> sequenceContexts = new HashMap();

    public void addSequenceContext(String serviceProvider, SequenceContext sequenceContext) {
        if (sequenceContext != null) {
            this.sequenceContexts.put(serviceProvider, sequenceContext);
        }
    }

    public SequenceContext getSequenceContext(String serviceProvider) {
        SequenceContext sequenceContext = sequenceContexts.get(serviceProvider);
        return sequenceContext;
    }

    public Collection<SequenceContext> getSequenceContexts() {
        return sequenceContexts.values();
    }
}
