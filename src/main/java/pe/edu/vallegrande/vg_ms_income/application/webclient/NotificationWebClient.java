package pe.edu.vallegrande.vg_ms_income.application.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vg_ms_income.domain.dto.NotificationEmail;
import pe.edu.vallegrande.vg_ms_income.domain.dto.NotificationResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class NotificationWebClient {

    private final WebClient webClient;

    public NotificationWebClient(WebClient.Builder builder, @Value("${spring.client.ms-notification-gmail.url}") String url) {
        this.webClient = builder
                .baseUrl(url)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(60))))
                .build();
    }

    public Mono<NotificationResponse> sendNotification(NotificationEmail email) {
        return webClient.post()
                .uri("/generate-pdf")
                .bodyValue(email)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Error response from the notification service"))
                )
                .bodyToMono(NotificationResponse.class)
                .doOnError(e -> System.err.println("Failed to send notification: " + e.getMessage()));
    }
}
