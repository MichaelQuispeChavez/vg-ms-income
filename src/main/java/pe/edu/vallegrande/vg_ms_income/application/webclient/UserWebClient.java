package pe.edu.vallegrande.vg_ms_income.application.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vg_ms_income.domain.dto.User;
import reactor.core.publisher.Mono;

@Component
public class UserWebClient {

    private final WebClient webClient;

    public UserWebClient(WebClient.Builder webClientBuilder, @Value("${spring.client.ms-user.url}") String msUserUrl) {
        this.webClient = webClientBuilder.baseUrl(msUserUrl).build();
    }

    public Mono<User> getUserById(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(User.class);
    }
}
