/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.model;

import java.io.Serializable;
import java.util.List;

/**
 * DTO class for Action.
 */
public class ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private ExecutorDTO executor;
    private String nextId;
    private List<BranchDTO> branches;

    public ActionDTO() {

    }

    private ActionDTO(Builder builder) {

        this.type = builder.type;
        this.executor = builder.executor;
        this.nextId = builder.nextId;
        this.branches = builder.branches;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public ExecutorDTO getExecutor() {

        return executor;
    }

    public void setExecutor(ExecutorDTO executor) {

        this.executor = executor;
    }

    public String getNextId() {

        return nextId;
    }

    public void setNextId(String nextId) {

        this.nextId = nextId;
    }

    /**
     * Outgoing branches of a rule decision, in evaluation order. Null for every other action type,
     * which goes to a single {@link #getNextId()} instead.
     *
     * @return The branches, or null when this action does not decide.
     */
    public List<BranchDTO> getBranches() {

        return branches;
    }

    public void setBranches(List<BranchDTO> branches) {

        this.branches = branches;
    }

    /**
     * Builder class to build {@link ActionDTO} objects.
     */
    public static class Builder {

        private String type;
        private ExecutorDTO executor;
        private String nextId;
        private List<BranchDTO> branches;

        public Builder branches(List<BranchDTO> branches) {

            this.branches = branches;
            return this;
        }

        public Builder type(String type) {

            this.type = type;
            return this;
        }

        public Builder executor(ExecutorDTO executor) {

            this.executor = executor;
            return this;
        }

        public Builder nextId(String nextId) {

            this.nextId = nextId;
            return this;
        }

        public ActionDTO build() {

            return new ActionDTO(this);
        }
    }
}
