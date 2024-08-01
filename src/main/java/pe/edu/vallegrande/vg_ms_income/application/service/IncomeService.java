package pe.edu.vallegrande.vg_ms_income.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_income.application.feignclient.StorageFeignClient;
import pe.edu.vallegrande.vg_ms_income.domain.dto.AdminIncomeDto;
import pe.edu.vallegrande.vg_ms_income.domain.dto.IncomeEnriched;
import pe.edu.vallegrande.vg_ms_income.domain.dto.StorageResponseDto;
import pe.edu.vallegrande.vg_ms_income.domain.dto.UserIncome;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;
import pe.edu.vallegrande.vg_ms_income.domain.repository.CategoryRepository;
import pe.edu.vallegrande.vg_ms_income.domain.repository.IncomeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static pe.edu.vallegrande.vg_ms_income.application.util.Constant.FOLDER_NAME;
import static pe.edu.vallegrande.vg_ms_income.application.util.Constant.PENDING;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeService {

    private final StorageFeignClient storageFeignClient;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ObjectMapper mapper;

    private Mono<List<String>> extractFileUrls(String responseBody) {
        return Mono.fromCallable(() -> mapper.readValue(responseBody, StorageResponseDto.class).getFilesUrl());
    }

    public Flux<IncomeEnriched> getAll() {
        log.info("LISTANDO TODOS LOS INGRESOS");
        return incomeRepository.findAll()
                .flatMap(income -> {
                    IncomeEnriched incomeEnriched = new IncomeEnriched();
                    incomeEnriched.setIncomeId(income.getIncomeId());
                    incomeEnriched.setPersonId(income.getPersonId());
                    incomeEnriched.setCelebrantId(income.getCelebrantId());
                    incomeEnriched.setDateEvent(income.getDateEvent());
                    incomeEnriched.setType(income.getType());
                    incomeEnriched.setFileUrls(income.getFileUrls());
                    incomeEnriched.setPersonConfirmedId(income.getPersonConfirmedId());
                    incomeEnriched.setComment(income.getComment());
                    incomeEnriched.setStatusPayment(income.getStatusPayment());
                    incomeEnriched.setStatusNotification(income.isStatusNotification());
                    incomeEnriched.setCreatedAt(income.getCreatedAt());
                    incomeEnriched.setUpdatedAt(income.getUpdatedAt());

                    incomeEnriched.setCategories(new ArrayList<>());
                    return Flux.fromIterable(income.getCategories())
                            .flatMap(categoryId -> categoryRepository.findById(categoryId)
                                    .map(category -> {
                                        incomeEnriched.getCategories().add(category);
                                        return incomeEnriched;
                                    })
                            )
                            .last()
                            .switchIfEmpty(Mono.just(incomeEnriched));
                });
    }



    public Mono<ResponseEntity<Income>> creatIncome(UserIncome userDto, MultipartFile[] files) {
        Income income = new Income();
        income.setIncomeId(UUID.randomUUID().toString());
        income.setPersonId(userDto.getPersonId());
        income.setCelebrantId(userDto.getCelebrantId());
        income.setDateEvent(userDto.getDateEvent());
        income.setCategories(userDto.getCategories());
        income.setType(userDto.getType());
        income.setFileUrls(List.of());
        income.setStatusPayment(PENDING);
        income.setCreatedAt(LocalDateTime.now());
        income.setUpdatedAt(LocalDateTime.now());
        income.setStatusNotification(false);
        log.info("CREANDO INGRESO");
        return Mono
                .fromCallable(() -> storageFeignClient.uploadFile(files, FOLDER_NAME, income.getPersonId(),
                        income.getIncomeId()))
                .flatMap(responseEntity -> responseEntity.getStatusCode().is2xxSuccessful()
                        ? extractFileUrls(responseEntity.getBody())
                        : Mono.error(new RuntimeException("Failed to upload files to storage service")))
                .doOnNext(income::setFileUrls)
                .flatMap(urls -> incomeRepository.save(income))
                .map(savedAccounting -> new ResponseEntity<>(savedAccounting, HttpStatus.CREATED))
                .onErrorReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    public Mono<ResponseEntity<Income>> updateIncome(String incomeId, AdminIncomeDto adminDto,
            MultipartFile[] newFiles) {
        log.info("EDITANDO INGRESOS");
        return incomeRepository.findById(incomeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Accounting not found")))
                .flatMap(income -> Mono
                        .fromCallable(() -> storageFeignClient.uploadFile(newFiles, FOLDER_NAME, income.getPersonId(),
                                income.getIncomeId()))
                        .flatMap(responseEntity -> responseEntity.getStatusCode().is2xxSuccessful()
                                ? extractFileUrls(responseEntity.getBody())
                                : Mono.error(new RuntimeException("Failed to upload files to storage service")))
                        .map(newUrls -> {
                            income.getFileUrls().addAll(newUrls);
                            income.setComment(adminDto.getComment());
                            income.setStatusPayment(adminDto.getStatusPayment());
                            income.setStatusNotification(adminDto.isStatusNotification());
                            income.setUpdatedAt(LocalDateTime.now());
                            return income;
                        })
                        .flatMap(incomeRepository::save)
                        .map(updatedAccounting -> new ResponseEntity<>(updatedAccounting, HttpStatus.OK))
                        .onErrorReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)));
    }
}