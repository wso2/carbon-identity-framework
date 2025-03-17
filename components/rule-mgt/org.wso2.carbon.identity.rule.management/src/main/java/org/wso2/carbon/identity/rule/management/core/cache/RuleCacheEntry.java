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

package org.wso2.carbon.identity.rule.management.core.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

/**
 * Cache entry for Rule Management.
 * This class is used to store Rule object in cache.
 */
public class RuleCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -8205516043545934797L;

    private Rule rule;

    public RuleCacheEntry(Rule rule) {

        this.rule = rule;
    }

    public Rule getRule() {

        return rule;
    }

    public void setRule(Rule rule) {

        this.rule = rule;
    }
}
