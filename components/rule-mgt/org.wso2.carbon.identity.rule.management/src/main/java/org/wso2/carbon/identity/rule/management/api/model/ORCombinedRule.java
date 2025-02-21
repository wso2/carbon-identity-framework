/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents an OR combined rule.
 * This class extends the Rule class and has a list of ANDCombinedRules.
 */
@JsonDeserialize(builder = ORCombinedRule.Builder.class)
public class ORCombinedRule extends Rule {

    private final List<ANDCombinedRule> rules;
    private final ConcurrentMap<String, List<Expression>> expressionsCache = new ConcurrentHashMap<>();

    /**
     * Builder for the ORCombinedRule.
     */
    private ORCombinedRule(Builder builder) {

        this.id = builder.id;
        this.isActive = builder.isActive;
        this.rules = builder.rules;
        this.condition = Condition.OR;
    }

    public Condition getCondition() {

        return condition;
    }

    public List<ANDCombinedRule> getRules() {

        return rules;
    }

    /**
     * @JsonIgnore annotation is used to ignore the expressions field when serializing and deserializing the object,
     * to and from JSON in order to store in the database.
     */
    @JsonIgnore
    @Override
    public List<Expression> getExpressions() {

        return expressionsCache.computeIfAbsent(id, k -> {
            List<Expression> expressions = new ArrayList<>();
            for (ANDCombinedRule rule : rules) {
                expressions.addAll(rule.getExpressions());
            }
            return expressions;
        });
    }

    /**
     * Builder for the ORCombinedRule.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private String id;
        private boolean isActive = true;
        private List<ANDCombinedRule> rules = new ArrayList<>();

        public Builder() {

        }

        public Builder(ORCombinedRule orCombinedRule) {

            this.id = orCombinedRule.id;
            this.rules = orCombinedRule.rules;
        }

        public Builder addRule(ANDCombinedRule andCombinedRule) {

            rules.add(andCombinedRule);
            return this;
        }

        public Builder setId(String id) {

            this.id = id;
            return this;
        }

        public Builder setActive(boolean isActive) {

            this.isActive = isActive;
            return this;
        }

        public Builder setCondition(Condition condition) {

            if (condition != Condition.OR) {
                throw new IllegalArgumentException("Condition must be OR for ORCombinedRule");
            }

            return this;
        }

        public Builder setRules(List<ANDCombinedRule> rules) {

            this.rules = rules;
            return this;
        }

        public ORCombinedRule build() {

            this.id = (this.id == null) ? UUID.randomUUID().toString() : this.id;
            return new ORCombinedRule(this);
        }
    }
}
