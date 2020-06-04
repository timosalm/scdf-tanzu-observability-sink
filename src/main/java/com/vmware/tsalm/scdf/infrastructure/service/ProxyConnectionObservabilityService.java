package com.vmware.tsalm.scdf.infrastructure.service;

import com.vmware.tsalm.scdf.domain.model.ObservabilityConfigurationProperties;
import com.vmware.tsalm.scdf.domain.model.service.ObservabilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@Primary
@ConditionalOnExpression("!'${tanzu-observability.wavefront-proxy-url:}'.isEmpty()")
public class ProxyConnectionObservabilityService implements ObservabilityService {
    private final RestTemplate restTemplate;
    private final ObservabilityConfigurationProperties configurationProperties;

    public ProxyConnectionObservabilityService(final RestTemplateBuilder restTemplateBuilder,
                                               final ObservabilityConfigurationProperties configurationProperties) {
        this.restTemplate = restTemplateBuilder.build();
        this.configurationProperties = configurationProperties;
    }

    @Override
    public void send(String metricInWavefrontFormat) {
        log.info("Send metric to wavefront proxy");
        restTemplate.postForEntity(configurationProperties.getWavefrontProxyUrl(), metricInWavefrontFormat, Void.class);
    }
}
