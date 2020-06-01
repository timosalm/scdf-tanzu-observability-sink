package com.vmware.tsalm.scdf.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservabilityConfigurationPropertiesTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRequiredProperties() {
        var configurationProperties = new ObservabilityConfigurationProperties("v", "v", "v", null, null, null, null, null);
        var emptyValues = Arrays.asList(null, "");

        emptyValues.forEach(emptyValue -> {
            assertTrue(validator.validate(configurationProperties).isEmpty());
            configurationProperties.setMetricName(emptyValue);
            assertFalse(validator.validate(configurationProperties).isEmpty());
            configurationProperties.setMetricName("v");

            assertTrue(validator.validate(configurationProperties).isEmpty());
            configurationProperties.setSource(emptyValue);
            assertFalse(validator.validate(configurationProperties).isEmpty());
            configurationProperties.setSource("v");

            assertTrue(validator.validate(configurationProperties).isEmpty());
            configurationProperties.setMetricJsonPath(emptyValue);
            assertFalse(validator.validate(configurationProperties).isEmpty());
            configurationProperties.setMetricJsonPath("v");
        });
    }

    @Test
    void testValidMetricNameValues() {
        var configurationProperties = new ObservabilityConfigurationProperties("v", "v", "v", null, null, null, null, null);
        var validMetricNameValues = Arrays.asList("b", "B", "2", ".", "/", "_", ",", "-", "c.8W-2h_dE_,J-h/");
        assertTrue(validator.validate(configurationProperties).isEmpty());

        validMetricNameValues.forEach(validMetricNameValue -> {
            configurationProperties.setMetricName(validMetricNameValue);
            assertTrue(validator.validate(configurationProperties).isEmpty());
        });

        var invalidMetricNameValues = Arrays.asList(" ", ":", "a B", "#");
        invalidMetricNameValues.forEach(invalidMetricNameValue -> {
            configurationProperties.setMetricName(invalidMetricNameValue);
            assertFalse(validator.validate(configurationProperties).isEmpty());
        });
    }

    @Test
    void testValidSourceValues() {
        var configurationProperties = new ObservabilityConfigurationProperties("v", "v", "v", null, null, null, null, null);
        var validSourceValues = Arrays.asList("b", "B", "2", ".", "_", "-", "c.8W-2h_dE_J-h", "a".repeat(128));
        assertTrue(validator.validate(configurationProperties).isEmpty());

        validSourceValues.forEach(validSourceValue -> {
            configurationProperties.setSource(validSourceValue);
            assertTrue(validator.validate(configurationProperties).isEmpty());
        });

        var invalidSourceValues = Arrays.asList(" ", ":", "a B", "#", "/", ",", "a".repeat(129));
        invalidSourceValues.forEach(invalidSourceValue -> {
            configurationProperties.setSource(invalidSourceValue);
            assertFalse(validator.validate(configurationProperties).isEmpty());
        });
    }
}