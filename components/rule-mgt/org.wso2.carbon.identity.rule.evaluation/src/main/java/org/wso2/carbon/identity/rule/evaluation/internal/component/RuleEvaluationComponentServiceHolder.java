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

package org.wso2.carbon.identity.rule.evaluation.internal.component;

import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.OperatorRegistry;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;

/**
 * Rule evaluation component service holder.
 */
public class RuleEvaluationComponentServiceHolder {

    private static final RuleEvaluationComponentServiceHolder INSTANCE = new RuleEvaluationComponentServiceHolder();

    private RuleManagementService ruleManagementService;
    private RuleMetadataService ruleMetadataService;

    private OperatorRegistry operatorRegistry;

    private RuleEvaluationComponentServiceHolder() {

    }

    public static RuleEvaluationComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    public RuleManagementService getRuleManagementService() {

        return ruleManagementService;
    }

    public void setRuleManagementService(
            RuleManagementService ruleManagementService) {

        this.ruleManagementService = ruleManagementService;
    }

    public RuleMetadataService getRuleMetadataService() {

        return ruleMetadataService;
    }

    public void setRuleMetadataService(RuleMetadataService ruleMetadataService) {

        this.ruleMetadataService = ruleMetadataService;
        // Load operators as rule metadata service is available now.
        this.operatorRegistry = OperatorRegistry.loadOperators();
    }

    public OperatorRegistry getOperatorRegistry() {

        return operatorRegistry;
    }
}
