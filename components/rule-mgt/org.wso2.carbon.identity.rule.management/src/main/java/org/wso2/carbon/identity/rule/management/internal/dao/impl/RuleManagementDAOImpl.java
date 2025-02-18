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

package org.wso2.carbon.identity.rule.management.internal.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementRuntimeException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementServerException;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.internal.constant.RuleSQLConstants;
import org.wso2.carbon.identity.rule.management.internal.dao.RuleManagementDAO;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Rule Management DAO Implementation.
 * This class is used to perform CRUD operations on Rule in the database.
 */
public class RuleManagementDAOImpl implements RuleManagementDAO {

    private static final String V1 = "1.0.0";

    /**
     * This method will add the Rule to the database and add the Rule Value References to the database.
     *
     * @param rule     Rule object
     * @param tenantId Tenant ID
     * @throws RuleManagementException If an error occurs while adding the rule to the database.
     */
    @Override
    public void addRule(Rule rule, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                int internalId = addRuleToDB(rule, tenantId);
                addRuleValueReferencesToDB(internalId, rule, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while creating the rule in the system.", e);
        }
    }

    /**
     * This method will update the Rule in the database and update the Rule Value References in the database,
     * by deleting all and adding back reference values for the updated rule.
     *
     * @param rule     Rule object
     * @param tenantId Tenant ID
     * @throws RuleManagementException If an error occurs while updating the rule in the database.
     */
    @Override
    public void updateRule(Rule rule, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                updateRuleInDB(rule, tenantId);
                int internalRuleId = getInternalRuleIdByRuleId(rule.getId(), tenantId);
                deleteRuleReferencesInDB(internalRuleId, tenantId);
                addRuleValueReferencesToDB(internalRuleId, rule, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while updating the rule in the system.", e);
        }
    }

    /**
     * This method will delete the Rule from the database.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException If an error occurs while deleting the rule from the database.
     */
    @Override
    public void deleteRule(String ruleId, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(RuleSQLConstants.Query.DELETE_RULE,
                        statement -> {
                            statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, ruleId);
                            statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                        });

                return null;
            });
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while deleting the rule in the system.", e);
        }
    }

    /**
     * This method will retrieve the Rule from the database.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @return Rule object
     * @throws RuleManagementException If an error occurs while retrieving the rule from the database.
     */
    @Override
    public Rule getRuleByRuleId(String ruleId, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        RuleData ruleData = new RuleData();
        try {
            jdbcTemplate.withTransaction(
                    template -> template.fetchSingleRecord(RuleSQLConstants.Query.GET_RULE_BY_ID,
                            (resultSet, rowNumber) -> {
                                ruleData.setRuleJson(getStringValueFromInputStream(
                                        resultSet.getBinaryStream(RuleSQLConstants.Column.RULE_CONTENT)));
                                ruleData.setActive(resultSet.getBoolean(RuleSQLConstants.Column.IS_ACTIVE));
                                return null;
                            },
                            statement -> {
                                statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, ruleId);
                                statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                            }));

            if (ruleData.getRuleJson() == null) {
                return null;
            }

            return new ORCombinedRule.Builder(convertJsonToRule(ruleData.getRuleJson()))
                    .setId(ruleId)
                    .setActive(ruleData.isActive())
                    .build();
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while retrieving the rule from the system.", e);
        }
    }

    /**
     * This method will activate the Rule in the database.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException If an error occurs while activating the rule in the database.
     */
    @Override
    public void activateRule(String ruleId, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(RuleSQLConstants.Query.CHANGE_RULE_STATUS,
                        statement -> {
                            statement.setBoolean(RuleSQLConstants.Column.IS_ACTIVE, true);
                            statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, ruleId);
                            statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while activating the rule in the system.", e);
        }
    }

    /**
     * This method will deactivate the Rule in the database.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException If an error occurs while deactivating the rule in the database.
     */
    @Override
    public void deactivateRule(String ruleId, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(RuleSQLConstants.Query.CHANGE_RULE_STATUS,
                        statement -> {
                            statement.setBoolean(RuleSQLConstants.Column.IS_ACTIVE, false);
                            statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, ruleId);
                            statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while deactivating the rule in the system.", e);
        }
    }

    private int addRuleToDB(Rule rule, int tenantId)
            throws TransactionException, IOException, RuleManagementException {

        InputStream ruleJsonAsInputStream = convertRuleToJson(rule);
        int ruleJsonStreamLength = ruleJsonAsInputStream.available();
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        int internalRuleId =
                jdbcTemplate.withTransaction(template -> template.executeInsert(RuleSQLConstants.Query.ADD_RULE,
                        statement -> {
                            statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, rule.getId());
                            statement.setBinaryStream(RuleSQLConstants.Column.RULE_CONTENT, ruleJsonAsInputStream,
                                    ruleJsonStreamLength);
                            statement.setBoolean(RuleSQLConstants.Column.IS_ACTIVE, rule.isActive());
                            statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                            statement.setString(RuleSQLConstants.Column.VERSION, V1);
                        }, rule, true));
        // Not all JDBC drivers support getting the auto generated database ID.
        // So if the ID is not returned, get the ID by querying the database.
        if (internalRuleId == 0) {
            internalRuleId = getInternalRuleIdByRuleId(rule.getId(), tenantId);
        }

        return internalRuleId;
    }

    private int getInternalRuleIdByRuleId(String ruleId, int tenantId) throws RuleManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(
                    template -> template.fetchSingleRecord(RuleSQLConstants.Query.GET_RULE_INTERNAL_ID_BY_ID,
                            (resultSet, rowNumber) -> resultSet.getInt(RuleSQLConstants.Column.RULE_INTERNAL_ID),
                            statement -> {
                                statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, ruleId);
                                statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                            }));
        } catch (TransactionException e) {
            throw new RuleManagementServerException("Error while retrieving the rule from the system.", e);
        }
    }

    private void addRuleValueReferencesToDB(int internalRuleId, Rule rule, int tenantId) throws TransactionException {

        ORCombinedRule orCombinedRule = (ORCombinedRule) rule;
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> template.executeBatchInsert(RuleSQLConstants.Query.ADD_RULE_REFERENCES,
                statement -> {
                    for (ANDCombinedRule rule1 : orCombinedRule.getRules()) {
                        for (Expression expression : rule1.getExpressions()) {
                            if (expression.getValue().getType() == Value.Type.REFERENCE) {
                                statement.setInt(RuleSQLConstants.Column.RULE_REFERENCE_ID, internalRuleId);
                                statement.setString(RuleSQLConstants.Column.FIELD_NAME, expression.getField());
                                statement.setString(RuleSQLConstants.Column.FIELD_REFERENCE,
                                        expression.getValue().getFieldValue());
                                statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                                statement.addBatch();
                            }
                        }
                    }
                }, null));
    }

    private void updateRuleInDB(Rule rule, int tenantId)
            throws IOException, TransactionException, RuleManagementServerException {

        InputStream ruleJsonAsInputStream = convertRuleToJson(rule);
        int ruleJsonStreamLength = ruleJsonAsInputStream.available();
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(RuleSQLConstants.Query.UPDATE_RULE,
                    statement -> {
                        statement.setBinaryStream(RuleSQLConstants.Column.RULE_CONTENT, ruleJsonAsInputStream,
                                ruleJsonStreamLength);
                        statement.setString(RuleSQLConstants.Column.RULE_EXTERNAL_ID, rule.getId());
                        statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                    });
            return null;
        });
    }

    private void deleteRuleReferencesInDB(int internalRuleId, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(RuleSQLConstants.Query.DELETE_RULE_REFERENCES,
                    statement -> {
                        statement.setInt(RuleSQLConstants.Column.RULE_REFERENCE_ID, internalRuleId);
                        statement.setInt(RuleSQLConstants.Column.TENANT_ID, tenantId);
                    });
            return null;
        });
    }

    private InputStream convertRuleToJson(Rule rule) throws RuleManagementServerException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return new ByteArrayInputStream(objectMapper.writeValueAsString(rule).getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuleManagementServerException("Failed to convert rule to JSON.", e);
        }
    }

    private ORCombinedRule convertJsonToRule(String ruleJson) throws RuleManagementServerException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(ruleJson, ORCombinedRule.class);
        } catch (JsonProcessingException e) {
            throw new RuleManagementServerException("Failed to convert JSON to rule.", e);
        }
    }

    private String getStringValueFromInputStream(InputStream stream) {

        if (stream == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuleManagementRuntimeException("Error while reading the rule content from storage.", e);
        }

        return sb.toString();
    }
}
