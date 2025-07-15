package com.example.demo.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {

    /**
     * Drop in your corporate root CA JKS file if needed, otherwise
     * just return new RestTemplate().
     */
    @Bean
    RestTemplate restTemplate() throws Exception {

        Path jks = Path.of("/etc/ssl/rootCA.jks");
        if (Files.notExists(jks)) {
            return new RestTemplate();
        }

        var ks = KeyStore.getInstance("JKS");
        try (var in = Files.newInputStream(jks)) {
            ks.load(in, "changeit".toCharArray());
        }
        var sslCtx = SSLContexts.custom()
                                .loadTrustMaterial(ks, null)
                                .build();

        var socketFactory = SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(sslCtx)
                                .build();

        var connMgr = PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(socketFactory)
                                .build();

        var httpClient = HttpClients.custom()
                                .setConnectionManager(connMgr)
                                .build();

        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }
}
