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

package org.wso2.carbon.identity.rule.evaluation.api.model;

import java.util.function.BiPredicate;

/**
 * Operator model.
 * This class represents an operator used in a rule and its evaluation function.
 */
public class Operator {

    private final String name;
    private final BiPredicate<Object, Object> predicate;

    public Operator(String name, BiPredicate<Object, Object> predicate) {

        this.name = name;
        this.predicate = predicate;
    }

    public boolean apply(Object fieldValue, Object expectedValue) {

        return predicate.test(fieldValue, expectedValue);
    }

    public String getName() {

        return name;
    }
}
