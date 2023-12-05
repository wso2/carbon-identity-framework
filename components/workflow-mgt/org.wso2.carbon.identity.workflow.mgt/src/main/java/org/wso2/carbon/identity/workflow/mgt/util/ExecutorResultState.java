/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.workflow.mgt.util;

/**
 * Workflow Execution Result Enumeration with its default state.
 *
 */
public enum ExecutorResultState {
    STARTED_ASSOCIATION{
        @Override public boolean state() {
            return false;
        }
    },
    COMPLETED{
        @Override public boolean state() {
            return true;
        }
    },
    NO_ASSOCIATION{
        @Override public boolean state() {
            return true;
        }
    },
    CONDITION_FAILED{
        @Override public boolean state() {
            return true;
        }
    },
    FAILED{
        @Override public boolean state() {
            return false;
        }
    };

    /**
     * Defautl State of Result
     * @return boolean
     */
    public boolean state() {
        return false;
    }
}
