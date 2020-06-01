package com.vmware.tsalm.scdf.interfaces;

import com.vmware.tsalm.scdf.domain.model.service.ObservabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Date;
import java.util.Locale;

@Import(TestChannelBinderConfiguration.class)
@SpringBootTest(properties = {
        "tanzu-observability.metric-name=vehicle-location",
        "tanzu-observability.source=vehicle-api",
        "tanzu-observability.metric-json-path=$.mileage",
        "tanzu-observability.timestamp-json-path=$.receivedAt",
        "tanzu-observability.point-tags-json-paths-point-value.vin=$.vin",
        "tanzu-observability.point-tags-json-paths-point-value.latitude=$.location.latitude",
        "tanzu-observability.wavefront-proxy-url=testUrl"
})
class ObservabilitySinkTest {

    @Autowired
    private InputDestination input;

    @MockBean
    private ObservabilityService observabilityServiceMock;

    @BeforeEach
    public void init() {
        Locale.setDefault(Locale.US);
    }

    @Test
    void testSendToTanzuObservability() {
        var timestamp = new Date().getTime();
        var dataJsonString = "{ \"mileage\": 1.5, \"receivedAt\": " + timestamp + ", \"vin\": \"test-vin\", " +
                "\"location\": {\"latitude\": 4.53, \"longitude\": 2.89 }}";

        input.send(MessageBuilder.withPayload(dataJsonString).build());

        var formattedString = "\"vehicle-location\" 1.5 " + timestamp + " source=vehicle-api latitude=\"4.53\" " +
                "vin=\"test-vin\"";
        Mockito.verify(observabilityServiceMock, Mockito.times(1)).send(formattedString);
    }
}