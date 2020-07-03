# [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) consumer/sink for [Tanzu Observability by Wavefront](https://tanzu.vmware.com/observability) (compatible with [Spring Cloud Data Flow](https://dataflow.spring.io/))

[Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) Java 11 application that consumes data in JSON 
format, coverts it into a metric in [Wavefront data format](https://docs.wavefront.com/wavefront_data_format.html) and 
sends the metric directly to Wavefront or a Wavefront proxy.

This application is using the RabbitMQ Binder for inputs. 
You can use other supported binders by replacing the `org.springframework.cloud.spring-cloud-stream-binder-rabbit` 
dependency in the pom.xml. 
  
You can configure the application via the following configuration properties (see src/main/resources/application.yml):
| Configuration Property | Required | Example | Description | Format |
| --- | --- | --- | --- | --- |
`tanzu-observability.metric-name` | yes | vehicle.mileage | The name of the metric | Valid characters are: a-z, A-Z, 0-9, hyphen ("-"), underscore ("_"), dot ("."), slash ("/"), and comma (","). |
`tanzu-observability.source` | yes | vehicle | The source of the metric | Valid characters are: a-z, A-Z, 0-9, hyphen ("-"), underscore ("_"), dot ("."). The length of the source field should be no more than 128 characters. |
`tanzu-observability.metric-json-path` | yes | $.mileage | The json path for the metric value | The metric has to be a number that can be parsed into a double-precision floating point number or a long integer. It can be positive, negative, or 0. |
`tanzu-observability.timestamp-json-path` | no | 1382754475 | The json path for a timestamp of the metric | Number that reflects the epoch seconds of the metric (e.g. 1382754475). |
`tanzu-observability.point-tags-json-paths-point-value` | no | point-tags-json-paths-point-value.vin: $.vin point-tags-json-paths-point-value.serial-number: $.serialNumber | Collection of custom metadata associated with the metric. | Point tags cannot be empty. Valid characters for keys: alphanumeric, hyphen ("-"), underscore ("_"), dot ("."). For values any character is allowed, including spaces. To include a double quote, escape it with a backslash, for example, `\"`. A backslash cannot be the last character in the tag value. Maximum allowed length for a combination of a point tag key and value is 254 characters (255 including the "=" separating key and value). If the value is longer, the point is rejected and logged. |
`tanzu-observability.wavefront-domain` | yes, if `wavefront-proxy-url` is not set | https://wavefront.com | The URL of the wavefront environment  | |
`tanzu-observability.wavefront-token` | yes, if `wavefront-proxy-url` is not set | 9bffbaff-c6fd-498a-ba79-f0c03de4343e | The token for the wavefront environment | |
`tanzu-observability.wavefront-proxy-url` | yes (recommended), if `wavefront-proxy-domain` is not set | http://wavefront-proxy.apps.internal:2878 | The URL of the wavefront proxy  | |

You can also configure the application via environment variables.
The configuration property `tanzu-observability.metric-name`corresponds for example to the environment 
variable `TANZU_OBSERVABILITY_METRIC_NAME`.

### Spring Cloud Data Flow (on Cloud Foundry)

#### How to add the sink to your SCDF instance and deploy a sample stream 

We will use the SCDF shell to add the sink and deploy a sample stream, but it's possible via the SCDF dashboard, too.

1. Start your shell (e.g. with the command `java -jar spring-cloud-dataflow-shell-2.4.2.RELEASE.jar`)
2. Set your SCDF instance (e.q. with command `dataflow config server https://my-data-flow-server.com/`)
3. Add the sink with the command `app register --name tanzu-observability-sink --type sink --uri https://example.com/mysink-0.0.1-SNAPSHOT.jar`.
The JAR archive of the latest source code in the master branch is available 
via the following URL: https://github.com/tsalm-pivotal/scdf-tanzu-observability-sink/raw/releases/scdf-tanzu-observability-sink-0.0.1-SNAPSHOT.jar. 
So you can the sink without modifications with the command `app register --name scdf-tanzu-observability-sink --type sink --uri https://github.com/tsalm-pivotal/scdf-tanzu-observability-sink/raw/releases/scdf-tanzu-observability-sink-0.0.1-SNAPSHOT.jar`
4. 
    - If you want to send the metrics **directly to your wavefront environment**, execute the following command with 
    your wavefront information in the stream definition:
    `stream create --name http-tanzu-observability-sink --definition "http | tanzu-observability-sink --tanzu-observability.wavefront-token='MY_TOKEN' --tanzu-observability.metric-json-path='$.mileage' --tanzu-observability.source='vehicle' --tanzu-observability.wavefront-domain='MY_WAVEFRONT_DOMAIN' --tanzu-observability.metric-name='vehicle-mileage' --JBP_CONFIG_OPEN_JDK_JRE='{jre: {version: 11.+}}'"`
    - If you want to send the metrics **to a wavefront proxy**, execute the following command with your wavefront 
    information in the stream definition:
    `stream create --name http-tanzu-observability-sink --definition "http | tanzu-observability-sink --tanzu-observability.wavefront-proxy='MY_WAVEFRONT_PROXY_URL' --tanzu-observability.metric-json-path='$.mileage' --tanzu-observability.source='vehicle' --tanzu-observability.metric-name='vehicle-mileage' --JBP_CONFIG_OPEN_JDK_JRE='{jre: {version: 11.+}}'"`
5. Deploy the stream with the command `stream deploy http-tanzu-observability-sink --properties "deployer.*.cloudfoundry.use-spring-application-json=false"`  
Hint: The runtime for the sink application will be provided by a (Java buildpack)[https://github.com/cloudfoundry/java-buildpack]. 
The JRE version of the buildpack (default version 8) is configurable via the `JBP_CONFIG_OPEN_JDK_JRE` environment 
variable. In the used SCDF version 2.4.2.RELEASE the configuration `deployer.*.cloudfoundry.use-spring-application-json=false` 
is required for the buildpack to recognize the env variable JBP_CONFIG_OPEN_JDK_JRE.
6. Call the endpoint of the http consumer (e.g. via `curl https://deployed-app-url.com/ -H "Content-type: application/json" -d "{ mileage: 14283 }"`) and view the metric in Tanzu Observability by Wavefront.
