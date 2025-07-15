package com.example.demo.batch;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.example.demo.infra.TokenService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Component
public class RedOakStreamReader<T> implements ItemStreamReader<T> {

    private final RestTemplate rest;
    private final TokenService tokenSvc;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Class<T> target;
    private final String endpoint;

    private JsonParser parser;

    /** Factory beans in JobConfig will supply endpoint & target */
    public RedOakStreamReader(@Value("#{target}") Class<T> target,
                              @Value("#{endpoint}") String endpoint,
                              RestTemplate rest, TokenService tokenSvc) {
        this.rest      = rest;
        this.tokenSvc  = tokenSvc;
        this.target    = target;
        this.endpoint  = endpoint;
    }

    @Override
    public void open(ExecutionContext ctx) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(tokenSvc.fetchToken());

        ResponseEntity<InputStream> response =
            rest.exchange(endpoint, HttpMethod.GET, new HttpEntity<>(h), InputStream.class);

        try {
            parser = mapper.getFactory().createParser(response.getBody());
            parser.nextToken();              // skip '['
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public T read() throws Exception {
        if (parser.nextToken() == JsonToken.END_ARRAY) {
            return null;                     // end of stream
        }
        return mapper.readValue(parser, target);
    }

    @Override
    public void close() {
        try {
            parser.close();
        } catch (IOException ignored) {
        }
    }
}
