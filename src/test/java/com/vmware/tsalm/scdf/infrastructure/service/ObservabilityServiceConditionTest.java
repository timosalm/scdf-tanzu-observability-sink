package com.vmware.tsalm.scdf.infrastructure.service;

import com.vmware.tsalm.scdf.domain.model.ObservabilityConfigurationProperties;
import com.vmware.tsalm.scdf.domain.model.service.ObservabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityServiceConditionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(UserConfigurations.of(DirectConnectionObservabilityService.class,
                    ProxyConnectionObservabilityService.class, RestTemplateAutoConfiguration.class,
                    ObservabilityConfigurationProperties.class));

    @Test
    public void proxyConnectionShouldBeUsedIfWavefrontProxyAddressSet() {
        runner.withPropertyValues(
                "tanzu-observability.wavefront-proxy-url=http://wavefront-proxy.internal:2878",
                "tanzu-observability.wavefront-domain=",
                "tanzu-observability.wavefront-token="
        ).run(context -> {
            assertThat(context).hasSingleBean(ProxyConnectionObservabilityService.class);
            assertThat(context).doesNotHaveBean(DirectConnectionObservabilityService.class);
        });
    }

    @Test
    public void proxyConnectionShouldBeUsedIfWavefrontProxyAddressAndDomainAndTokenSet() {
        runner.withPropertyValues(
                "tanzu-observability.wavefront-proxy-url=http://wavefront-proxy.internal:2878",
                "tanzu-observability.wavefront-domain=https://my.wavefront.com",
                "tanzu-observability.wavefront-token=" + UUID.randomUUID()
        ).run(context -> {
            assertThat(context).hasSingleBean(ProxyConnectionObservabilityService.class);
            assertThat(context).hasSingleBean(DirectConnectionObservabilityService.class);

            assertThat(context).getBean(ObservabilityService.class)
                    .isNotInstanceOfAny(DirectConnectionObservabilityService.class);
            assertThat(context).getBean(ObservabilityService.class)
                    .isInstanceOf(ProxyConnectionObservabilityService.class);

        });
    }

    @Test
    public void directConnectionShouldBeUsedIfWavefrontDomainAndTokenSet() {
        runner.withPropertyValues(
                "tanzu-observability.wavefront-proxy-url=",
                "tanzu-observability.wavefront-domain=https://my.wavefront.com",
                "tanzu-observability.wavefront-token=" + UUID.randomUUID()
        ).run(context -> {
            assertThat(context).hasSingleBean(DirectConnectionObservabilityService.class);
            assertThat(context).doesNotHaveBean(ProxyConnectionObservabilityService.class);
        });
    }

    @Test
    public void directConnectionShouldBeUsedIfWavefrontProxyAddressOrDomainAndTokenNotSet() {
        runner.withPropertyValues(
                "tanzu-observability.wavefront-proxy-url=",
                "tanzu-observability.wavefront-domain=",
                "tanzu-observability.wavefront-token="
        ).run(context -> assertThat(context).doesNotHaveBean(ObservabilityService.class));
    }
}