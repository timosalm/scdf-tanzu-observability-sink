package com.vmware.tsalm.scdf.domain.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.json.JsonPathUtils;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class WavefrontFormat {

    private final ObservabilityConfigurationProperties configurationProperties;
    private final String dataJsonString;

    public String getFormattedString() throws IOException {
        var metricValue = extractMetricValueFromJson();

        var pointTagsMap = extractPointTagsMapFromJson(configurationProperties.getPointTagsJsonPathsPointValue(),
                dataJsonString);
        validatePointTagsKeyValuePairs(pointTagsMap);
        var formattedPointTagsPart = getFormattedPointTags(pointTagsMap);

        if (configurationProperties.getTimestampJsonPath() == null) {
            return String.format("\"%s\" %s source=%s %s", configurationProperties.getMetricName(), metricValue,
                    configurationProperties.getSource(), formattedPointTagsPart).trim();
        }

        var timeStamp = extractTimestampFromJson();
        return String.format("\"%s\" %s %d source=%s %s", configurationProperties.getMetricName(), metricValue, timeStamp,
                configurationProperties.getSource(), formattedPointTagsPart).trim();
    }

    private Long extractTimestampFromJson() throws IOException {
        try {
            return JsonPathUtils.evaluate(dataJsonString, configurationProperties.getTimestampJsonPath());
        } catch (ClassCastException e) {
            throw new ValidationException("The timestamp value has to be a number that reflects the epoch seconds of the " +
                    "metric (e.g. 1382754475).", e);
        }
    }

    private Number extractMetricValueFromJson() throws IOException {
        try {
            return JsonPathUtils.evaluate(dataJsonString, configurationProperties.getMetricJsonPath());
        } catch (ClassCastException e) {
            throw new ValidationException("The metric value has to be a double-precision floating point number or a " +
                    "long integer. It can be positive, negative, or 0.", e);
        }
    }

    private String getFormattedPointTags(Map<String, Object> pointTagsMap) {
        return pointTagsMap.entrySet().stream()
                .map(it -> String.format("%s=\"%s\"", it.getKey(), it.getValue()))
                .collect(Collectors.joining(" "));
    }

    private Map<String, Object> extractPointTagsMapFromJson(Map<String, String> pointTagsJsonPathsPointValue, String dataJsonString) {
        return pointTagsJsonPathsPointValue.entrySet().stream()
                .map(it -> {
                    try {
                        final Object pointValue = JsonPathUtils.evaluate(dataJsonString, it.getValue());
                        return new AbstractMap.SimpleEntry<String, Object>(it.getKey(), pointValue);
                    } catch (IOException e) {
                        log.warn("Unable to extract point tag for key " + it.getKey() + " from json data", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void validatePointTagsKeyValuePairs(Map<String, Object> pointTagsMap) {
        pointTagsMap.forEach((key, value) -> {
            if (!Pattern.matches("^[a-zA-Z0-9._-]+", key)) {
                throw new ValidationException("Point tag key \"" + key + "\" contains invalid characters: Valid " +
                        "characters are alphanumeric, hyphen (\"-\"), underscore (\"_\"), dot (\".\")");
            }

            var keyValueCombinationLength = key.length() + value.toString().length();
            if (keyValueCombinationLength > 254) {
                log.warn("Maximum allowed length for a combination of a point tag key and value " +
                        "is 254 characters. The length of combination for key " + key + " is " +
                        keyValueCombinationLength + ".");
            }
        });
    }
}
