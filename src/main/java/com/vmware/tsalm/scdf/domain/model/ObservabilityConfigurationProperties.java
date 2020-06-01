package com.vmware.tsalm.scdf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.Map;

@ConfigurationProperties("tanzu-observability")
@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class ObservabilityConfigurationProperties {

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9./_,-]+")
    private String metricName;

    @NotEmpty
    @Size(max = 128)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+")
    private String source;

    @NotEmpty
    private String metricJsonPath;

    private String timestampJsonPath;

    private Map<String, String> pointTagsJsonPathsPointValue = Collections.emptyMap();

    private String wavefrontDomain;

    private String wavefrontToken;

    private String wavefrontProxyUrl;
}