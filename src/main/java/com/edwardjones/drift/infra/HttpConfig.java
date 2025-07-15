package com.edwardjones.drift.infra;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class HttpConfig {

    @Bean
    RestTemplate vendorRestTemplate(RestTemplateBuilder builder, SslBundles bundles) {
        try {
            return builder
                    .sslBundle(bundles.getBundle("vendor"))
                    .build();
        } catch (Exception e) {
            // Fall back to basic RestTemplate if SSL bundle is not available (e.g., in tests)
            return builder.build();
        }
    }
}
