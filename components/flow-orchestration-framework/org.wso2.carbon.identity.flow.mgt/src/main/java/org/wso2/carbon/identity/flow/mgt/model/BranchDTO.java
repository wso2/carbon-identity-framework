/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.flow.mgt.model;

import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;

import java.io.Serializable;

/**
 * One outgoing branch of a rule decision.
 * <p>
 * Branches are evaluated in the order they appear, and the first whose rule holds selects the next
 * step. The branch with no rule is the default: it is what makes a decision total, so exactly one
 * branch must be without one. Its position in the list is not what makes it the default -- the
 * absence of a rule is -- but it is written back last so the stored order reads as evaluation order.
 */
public class BranchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String nextId;
    private ORCombinedRule rule;

    public BranchDTO() {

    }

    private BranchDTO(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.nextId = builder.nextId;
        this.rule = builder.rule;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    /**
     * Label for this branch, shown on the canvas and cited in validation messages. An author reads
     * "branch 'Internal staff'" and knows what to fix; an array index tells them nothing.
     *
     * @return The branch name.
     */
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getNextId() {

        return nextId;
    }

    public void setNextId(String nextId) {

        this.nextId = nextId;
    }

    /**
     * The guard on this branch. Null marks the default branch, which always holds.
     *
     * @return The rule, or null when this is the default branch.
     */
    public ORCombinedRule getRule() {

        return rule;
    }

    public void setRule(ORCombinedRule rule) {

        this.rule = rule;
    }

    /**
     * Whether this branch is the default, taken when no other branch holds.
     *
     * @return true when the branch carries no rule.
     */
    public boolean isDefault() {

        return rule == null;
    }

    /**
     * Builder class to build {@link BranchDTO} objects.
     */
    public static class Builder {

        private String id;
        private String name;
        private String nextId;
        private ORCombinedRule rule;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder nextId(String nextId) {

            this.nextId = nextId;
            return this;
        }

        public Builder rule(ORCombinedRule rule) {

            this.rule = rule;
            return this;
        }

        public BranchDTO build() {

            return new BranchDTO(this);
        }
    }
}
