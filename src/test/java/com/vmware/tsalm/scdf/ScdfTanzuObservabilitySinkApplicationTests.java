package com.vmware.tsalm.scdf;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"tanzu-observability.metric-name=test-metric",
		"tanzu-observability.source=test-source",
		"tanzu-observability.metric-json-path=$.metric",
		"tanzu-observability.wavefront-proxy-url=http://example.com:2878"
})
class ScdfTanzuObservabilitySinkApplicationTests {

	@Test
	void contextLoads() {
	}
}
