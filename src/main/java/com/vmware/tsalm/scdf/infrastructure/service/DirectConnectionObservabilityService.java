package com.vmware.tsalm.scdf.infrastructure.service;

import com.vmware.tsalm.scdf.domain.model.ObservabilityConfigurationProperties;
import com.vmware.tsalm.scdf.domain.model.service.ObservabilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@ConditionalOnExpression("!'${tanzu-observability.wavefront-domain:}'.isEmpty() and !'${tanzu-observability.wavefront-token:}'.isEmpty()")
public class DirectConnectionObservabilityService implements ObservabilityService {

    private final RestTemplate restTemplate;
    private final ObservabilityConfigurationProperties configurationProperties;

    public DirectConnectionObservabilityService(final RestTemplateBuilder restTemplateBuilder,
                                                final ObservabilityConfigurationProperties configurationProperties) {
        this.restTemplate = restTemplateBuilder.build();
        this.configurationProperties = configurationProperties;
    }

    @Override
    public void send(String metricInWavefrontFormat) {
        log.info("Send metric directly to wavefront");
        var headers = new HttpHeaders();
        headers.setBearerAuth(configurationProperties.getWavefrontToken());
        var httpEntity = new HttpEntity<>(metricInWavefrontFormat, headers);
        restTemplate.exchange(configurationProperties.getWavefrontDomain() + "/report", HttpMethod.POST,
                httpEntity, Void.class);
    }
}
