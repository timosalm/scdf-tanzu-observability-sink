package com.vmware.tsalm.scdf.interfaces;

import com.vmware.tsalm.scdf.domain.model.ObservabilityConfigurationProperties;
import com.vmware.tsalm.scdf.domain.model.WavefrontFormat;
import com.vmware.tsalm.scdf.domain.model.service.ObservabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class ObservabilitySink {

    private final ObservabilityConfigurationProperties configurationProperties;
    private final ObservabilityService service;

    @Bean
    Consumer<String> sendToTanzuObservability() {
        return inputJsonString -> {
            var observabilityFormat = new WavefrontFormat(configurationProperties, inputJsonString);
            String formattedString;
            try {
                formattedString = observabilityFormat.getFormattedString();
            } catch (IOException e) {
                log.error("Unable to transform input into Tanzu Observability format", e);
                return;
            }
            log.debug(formattedString);
            service.send(formattedString);
        };
    }
}
