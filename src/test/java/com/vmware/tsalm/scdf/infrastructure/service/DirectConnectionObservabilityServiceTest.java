package com.vmware.tsalm.scdf.infrastructure.service;

import com.vmware.tsalm.scdf.domain.model.ObservabilityConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DirectConnectionObservabilityServiceTest {

    @Test
    void testSendMetricInWavefrontFormat() {
        var restTemplateBuilderMock = mock(RestTemplateBuilder.class);
        var restTemplateMock = mock(RestTemplate.class);
        when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);

        var configurationProperties = new ObservabilityConfigurationProperties("testName", "testSource",
                "metricJsonPath", null, Collections.emptyMap(), "testWavefrontDomain",
                "testWavefrontToken", null);
        final String metricInWavefrontFormat = "testMetric";

        var service = new DirectConnectionObservabilityService(restTemplateBuilderMock, configurationProperties);
        service.send(metricInWavefrontFormat);

        var argument = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplateMock, Mockito.times(1))
                .exchange(eq(configurationProperties.getWavefrontDomain() + "/report"), eq(HttpMethod.POST),
                        argument.capture(), eq(Void.class));
        assertEquals("Bearer " + configurationProperties.getWavefrontToken(),
                Objects.requireNonNull(argument.getValue().getHeaders().get("Authorization")).get(0));
        assertEquals(metricInWavefrontFormat,
                Objects.requireNonNull(argument.getValue().getBody()));
    }
}