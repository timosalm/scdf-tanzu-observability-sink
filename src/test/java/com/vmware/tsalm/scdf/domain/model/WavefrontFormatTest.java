package com.vmware.tsalm.scdf.domain.model;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WavefrontFormatTest {

    @BeforeEach
    public void init() {
        Locale.setDefault(Locale.US);
    }

    @Test
    void testGetFormattedString() throws IOException {
        var timestamp = new Date().getTime();
        var dataJsonString = "{ \"value\": 1.5, \"timestamp\": " + timestamp + ", "
                + "\"testProp1\": \"testvalue1\", \"testProp2\": \"testvalue2\" }";


        var pointTagsJsonPathsPointValueMap = Stream.of(
                new AbstractMap.SimpleEntry<>("testpoint1", "$.testProp1"),
                new AbstractMap.SimpleEntry<>("testpoint2", "$.testProp2")
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                "$.value", "$.timestamp", pointTagsJsonPathsPointValueMap, null, null, null);

        var result = new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        assertEquals("\"testMetricName\" 1.5 " + timestamp + " source=testSource testpoint2=\"testvalue2\""
                + " testpoint1=\"testvalue1\"", result);
    }

    @Test
    void testGetFormattedStringWithoutTimeStamp() throws IOException {
        var dataJsonString = "{ \"value\": 1.5, \"testProp1\": \"testvalue1\", \"testProp2\": \"testvalue2\" }";

        var pointTagsJsonPathsPointValueMap = Stream.of(new AbstractMap.SimpleEntry<>("testpoint1", "$.testProp1"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                "$.value", null, pointTagsJsonPathsPointValueMap, null, null, null);

        var result = new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        assertEquals("\"testMetricName\" 1.5 source=testSource testpoint1=\"testvalue1\"", result);
    }

    @Test
    void testGetFormattedStringWithoutPointTags() throws IOException {
        var timestamp = new Date().getTime();
        var dataJsonString = "{ \"value\": 1.5, \"timestamp\": " + timestamp + "}";

        var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                "$.value", "$.timestamp", Collections.emptyMap(), null, null, null);

        var result = new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        assertEquals("\"testMetricName\" 1.5 " + timestamp + " source=testSource", result);
    }

    @Test
    void testInvalidMetricValue() {
        var dataJsonString = "{ \"value\": a}";
        var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                "$.value", null, Collections.emptyMap(), null, null, null);
        var exception = Assertions.assertThrows(RuntimeException.class, () -> {
            new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        });
        assertTrue(exception.getLocalizedMessage().startsWith("The metric value has to be a double-precision floating"));
    }

    @Test
    void testInvalidTimestampValue() {
        var dataJsonString = "{ \"value\": 1.5, \"timestamp\": 2020-06-02T13:53:18+0000}";
        var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                "$.value", "$.timestamp", Collections.emptyMap(), null, null, null);
        var exception = Assertions.assertThrows(RuntimeException.class, () -> {
            new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        });
        assertTrue(exception.getLocalizedMessage().startsWith("The timestamp value has to be a number"));
    }

    @Test
    void testInvalidPointTagsLengthWarning() throws IOException {
        var logger = (Logger) LoggerFactory.getLogger(WavefrontFormat.class);
        var listAppender = new ListAppender<ILoggingEvent>();
        listAppender.start();
        logger.addAppender(listAppender);

        var testPointTagKey = "a".repeat(127);

        var pointTagsJsonPathsPointValueMap = Stream.of(new AbstractMap.SimpleEntry<>(testPointTagKey, "$.testPoint1"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                "$.value", null, pointTagsJsonPathsPointValueMap, null, null, null);

        var dataJsonString = "{ \"value\": 1.5, \"testPoint1\": \"" +
                "a".repeat(254 - testPointTagKey.length()) + "\" }";
        new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        assertEquals(0, listAppender.list.stream()
                .filter(event -> event.getMessage().startsWith("Maximum allowed length for a combination"))
                .count());

        var dataJsonStringWithTooLongValue = "{ \"value\": 1.5, \"testPoint1\": \"" +
                "a".repeat(255 - testPointTagKey.length()) + "\" }";
        new WavefrontFormat(configurationProperties, dataJsonStringWithTooLongValue).getFormattedString();

        assertEquals(1, listAppender.list.stream()
                .filter(event -> event.getMessage().startsWith("Maximum allowed length for a combination")
                        && event.getLevel().equals(Level.WARN))
                .count());
    }

    @Test
    void testInvalidPointTagKeys() throws IOException {
        var dataJsonString = "{ \"value\": 1.5, \"testPoint1\": \"testvalue1\" }";

        var validPointTagKeys = Arrays.asList("b", "B", "2", ".", "_", "-", "c.8W-2h_dE_J-h");
        for (String validPointTagKey : validPointTagKeys) {
            var pointTagsJsonPathsPointValueMap = Stream.of(new AbstractMap.SimpleEntry<>(validPointTagKey, "$.testPoint1"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                    "$.value", null, pointTagsJsonPathsPointValueMap, null, null, null);
            new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
        }

        var invalidPointTagKeys = Arrays.asList(" ", ":", "a B", "#", "/", ",");

        invalidPointTagKeys.forEach(invalidPointTagKey -> {
            var pointTagsJsonPathsPointValueMap = Stream.of(new AbstractMap.SimpleEntry<>(invalidPointTagKey, "$.testPoint1"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var configurationProperties = new ObservabilityConfigurationProperties("testMetricName", "testSource",
                    "$.value", null, pointTagsJsonPathsPointValueMap, null, null, null);

            var exception = Assertions.assertThrows(RuntimeException.class, () -> {
                new WavefrontFormat(configurationProperties, dataJsonString).getFormattedString();
            });
            assertTrue(exception.getLocalizedMessage()
                    .startsWith("Point tag key \"" + invalidPointTagKey + "\" contains invalid characters"));
        });
    }
}