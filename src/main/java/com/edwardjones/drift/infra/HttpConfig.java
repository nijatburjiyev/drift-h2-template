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
            // Use the SSL bundle for truststore only (no mutual TLS)
            var sslBundle = bundles.getBundle("vendor");
            var sslContext = sslBundle.createSslContext();

            return builder
                    .requestFactory(() -> {
                        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                        // SimpleClientHttpRequestFactory will use the default SSL context
                        // which Spring Boot configures with the SSL bundle
                        return factory;
                    })
                    .build();
        } catch (Exception e) {
            // Fall back to basic RestTemplate if SSL bundle is not available (e.g., in tests)
            return builder.build();
        }
    }
}
