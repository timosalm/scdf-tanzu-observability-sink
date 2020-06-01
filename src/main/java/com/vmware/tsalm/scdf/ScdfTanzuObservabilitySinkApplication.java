package com.vmware.tsalm.scdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ScdfTanzuObservabilitySinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScdfTanzuObservabilitySinkApplication.class, args);
    }
}
