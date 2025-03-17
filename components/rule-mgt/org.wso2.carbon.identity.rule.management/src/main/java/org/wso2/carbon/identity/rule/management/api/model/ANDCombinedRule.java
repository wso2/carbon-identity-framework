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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an AND combined rule.
 * This class extends the Rule class and has a list of expressions.
 */
@JsonDeserialize(builder = ANDCombinedRule.Builder.class)
public class ANDCombinedRule extends Rule {

    private final List<Expression> expressions;

    private ANDCombinedRule(Builder builder) {

        this.id = builder.id;
        this.expressions = builder.expressions;
        this.condition = Condition.AND;
        this.isActive = true;
    }

    public Condition getCondition() {

        return condition;
    }

    public List<Expression> getExpressions() {

        return expressions;
    }

    /**
     * Builder for the ANDCombinedRule.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private String id;
        private List<Expression> expressions = new ArrayList<>();

        public Builder addExpression(Expression expression) {

            expressions.add(expression);
            return this;
        }

        public Builder setCondition(Condition condition) {

            if (condition != Condition.AND) {
                throw new IllegalArgumentException("Condition must be AND for ANDCombinedRule");
            }

            return this;
        }

        public Builder setExpressions(List<Expression> expressions) {

            this.expressions = expressions;
            return this;
        }

        public ANDCombinedRule build() {

            this.id = UUID.randomUUID().toString();
            return new ANDCombinedRule(this);
        }
    }
}
