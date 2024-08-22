package pe.edu.vallegrande.vg_ms_income.application.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import pe.edu.vallegrande.vg_ms_income.domain.dto.StorageResponseDto;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import reactor.core.publisher.Mono;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Stream;

@Component
public class StorageWebClient {

    private final WebClient webClient;

    public StorageWebClient(WebClient.Builder webClientBuilder, @Value("${spring.client.ms-storage.url}") String storageUrl) {
        this.webClient = webClientBuilder.baseUrl(storageUrl).build();
    }

    public Mono<List<String>> uploadFiles(MultipartFile[] files, String folderName, String userCode, String transactionCode) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        
        Stream.of(files).forEach(file -> 
            multipartBodyBuilder.part("files", file.getResource(), MediaType.MULTIPART_FORM_DATA)
        );
        multipartBodyBuilder.part("folderName", folderName);
        multipartBodyBuilder.part("userCode", userCode);
        multipartBodyBuilder.part("transactionCode", transactionCode);

        return webClient.post()
                .uri("/storage/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::extractFileUrls);
    }

    private Mono<List<String>> extractFileUrls(String responseBody) {
        return Mono.fromCallable(() -> {
            ObjectMapper mapper = new ObjectMapper();
            StorageResponseDto response = mapper.readValue(responseBody, StorageResponseDto.class);
            return response.getFilesUrl();
        });
    }
}
