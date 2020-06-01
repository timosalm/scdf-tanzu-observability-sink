package com.vmware.tsalm.scdf.infrastructure.service;

import com.vmware.tsalm.scdf.domain.model.ObservabilityConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProxyConnectionObservabilityServiceTest {
    @Test
    void testSendMetricInWavefrontFormat() {
        var restTemplateBuilderMock = mock(RestTemplateBuilder.class);
        var restTemplateMock = mock(RestTemplate.class);
        when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);

        var configurationProperties = new ObservabilityConfigurationProperties("testName", "testSource",
                "metricJsonPath", null, Collections.emptyMap(), null,
                null, "testWavefrontProxyUrl");
        final String metricInWavefrontFormat = "testMetric";

        var service = new ProxyConnectionObservabilityService(restTemplateBuilderMock, configurationProperties);
        service.send(metricInWavefrontFormat);

        verify(restTemplateMock, Mockito.times(1))
                .postForEntity(eq(configurationProperties.getWavefrontProxyUrl()), eq(metricInWavefrontFormat),
                        eq(Void.class));
    }
}