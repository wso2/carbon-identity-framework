package org.wso2.carbon.identity.input.validation.mgt.model.validators;

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.Property;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.DEFAULT_ALPHANUMERIC_REGEX_PATTERN;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.IS_ALPHANUMERIC;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_ALPHANUMERIC_MISMATCH;

/**
 * Alphanumeric validator.
 */
public class AlphanumericValidator extends AbstractRulesValidator {

    /**
     * Validate the string against the validation criteria.
     *
     * @param context   Validation Context.
     * @return boolean
     * @throws InputValidationMgtClientException Error when string does not satisfy the validation criteria
     */
    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        String value = context.getValue();
        String field = context.getField();
        Map<String, String> attributesMap = context.getProperties();
        String alphanumericRegEx = DEFAULT_ALPHANUMERIC_REGEX_PATTERN;

        // Check whether value satisfies the alphanumeric criteria.
        if (attributesMap.containsKey(IS_ALPHANUMERIC)) {
            boolean isAlphanumeric = Boolean.parseBoolean(attributesMap.get(IS_ALPHANUMERIC));
            if (isAlphanumeric && value != null && !value.matches(alphanumericRegEx)) {
                throw new InputValidationMgtClientException(ERROR_VALIDATION_ALPHANUMERIC_MISMATCH.getCode(),
                    ERROR_VALIDATION_ALPHANUMERIC_MISMATCH.getMessage(),
                    String.format(ERROR_VALIDATION_ALPHANUMERIC_MISMATCH.getDescription(), field, alphanumericRegEx));
            }
        }

        return true;
    }

    /**
     * Get list of supported properties for the validator.
     *
     * @return  List<Property>
     */
    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property isAlphanumeric = new Property();
        isAlphanumeric.setName(IS_ALPHANUMERIC);
        isAlphanumeric.setDisplayName("Alphanumeric field value");
        isAlphanumeric.setDescription("Validate whether the field value is alphanumeric.");
        isAlphanumeric.setType("boolean");
        isAlphanumeric.setDisplayOrder(++parameterCount);
        configProperties.add(isAlphanumeric);

        return configProperties;
    }


    /**
     * Validate the configuration values of the properties for the validator.
     *
     * @param context   Validation Context.
     * @return  boolean
     */
    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        validatePropertyName(properties, this.getClass().getSimpleName(), context.getTenantDomain());
        if (properties.get(IS_ALPHANUMERIC) != null && !validateBoolean(properties.get(IS_ALPHANUMERIC),
                IS_ALPHANUMERIC, context.getTenantDomain())) {
            properties.remove(IS_ALPHANUMERIC);
        }
        return true;
    }
}
