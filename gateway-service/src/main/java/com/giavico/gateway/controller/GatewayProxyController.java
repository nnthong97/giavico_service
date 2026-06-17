package com.giavico.gateway.controller;

import com.giavico.gateway.config.GatewayRouteProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@RestController
public class GatewayProxyController {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            HttpHeaders.HOST.toLowerCase(),
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

    private final WebClient webClient;
    private final GatewayRouteProperties routeProperties;

    public GatewayProxyController(WebClient webClient, GatewayRouteProperties routeProperties) {
        this.webClient = webClient;
        this.routeProperties = routeProperties;
    }

    @RequestMapping({"/api/formulas/**", "/api/inventory/**", "/api/chat/**"})
    public Mono<ResponseEntity<Flux<DataBuffer>>> proxy(
            ServerWebExchange exchange,
            @RequestBody(required = false) Mono<byte[]> requestBody
    ) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        URI targetUri = URI.create(resolveBaseUrl(path) + path + querySuffix(exchange));
        HttpMethod method = exchange.getRequest().getMethod();

        WebClient.RequestBodySpec requestSpec = webClient.method(method)
                .uri(targetUri)
                .headers(headers -> copyRequestHeaders(exchange.getRequest().getHeaders(), headers));

        Mono<byte[]> payload = requestBody == null ? Mono.just(new byte[0]) : requestBody.defaultIfEmpty(new byte[0]);
        return payload.flatMap(bytes -> {
            WebClient.RequestHeadersSpec<?> headersSpec = shouldForwardBody(method, bytes)
                    ? requestSpec.bodyValue(bytes)
                    : requestSpec;

            return headersSpec.exchangeToMono(response -> Mono.just(ResponseEntity
                    .status(response.statusCode())
                    .headers(headers -> copyResponseHeaders(response.headers().asHttpHeaders(), headers))
                    .body(response.bodyToFlux(DataBuffer.class))));
        });
    }

    private String resolveBaseUrl(String path) {
        if (path.startsWith("/api/formulas")) {
            return routeProperties.formulaServiceUrl();
        }
        if (path.startsWith("/api/inventory")) {
            return routeProperties.inventoryServiceUrl();
        }
        if (path.startsWith("/api/chat")) {
            return routeProperties.chatAiServiceUrl();
        }
        throw new IllegalArgumentException("No route configured for path: " + path);
    }

    private String querySuffix(ServerWebExchange exchange) {
        String query = exchange.getRequest().getURI().getRawQuery();
        return query == null || query.isBlank() ? "" : "?" + query;
    }

    private boolean shouldForwardBody(HttpMethod method, byte[] body) {
        return body.length > 0 && method != HttpMethod.GET && method != HttpMethod.DELETE;
    }

    private void copyRequestHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                target.put(name, values);
            }
        });
    }

    private void copyResponseHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                target.put(name, values);
            }
        });
    }
}
