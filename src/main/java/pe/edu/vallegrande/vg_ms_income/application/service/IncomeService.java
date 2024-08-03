package pe.edu.vallegrande.vg_ms_income.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_income.application.feignclient.StorageFeignClient;
import pe.edu.vallegrande.vg_ms_income.application.webclient.NotificationWebClient;
import pe.edu.vallegrande.vg_ms_income.application.webclient.UserWebClient;
import pe.edu.vallegrande.vg_ms_income.domain.dto.*;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;
import pe.edu.vallegrande.vg_ms_income.domain.repository.CategoryRepository;
import pe.edu.vallegrande.vg_ms_income.domain.repository.IncomeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
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

    private final UserWebClient userWebClient;
    private final NotificationWebClient notificationWebClient;
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
                .flatMap(income -> enrichIncome(income));
    }

    private Mono<IncomeEnriched> enrichIncome(Income income) {
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

        return enrichCategories(income, incomeEnriched)
                .flatMap(this::enrichUser)
                .flatMap(this::enrichCelebrant)
                .flatMap(this::enrichPersonConfirmed);
    }

    private Mono<IncomeEnriched> enrichCategories(Income income, IncomeEnriched incomeEnriched) {
        return Flux.fromIterable(income.getCategories())
                .flatMap(categoryId -> categoryRepository.findById(categoryId)
                        .map(category -> {
                            incomeEnriched.getCategories().add(category);
                            return incomeEnriched;
                        })
                        .onErrorResume(e -> {
                            log.error("Error retrieving category with ID {}", categoryId, e);
                            return Mono.just(incomeEnriched);
                        }))
                .then(Mono.just(incomeEnriched));
    }

    private Mono<IncomeEnriched> enrichUser(IncomeEnriched incomeEnriched) {
        if (incomeEnriched.getPersonId() == null) {
            return Mono.just(incomeEnriched);
        }
        return userWebClient.getUserById(incomeEnriched.getPersonId())
                .map(user -> {
                    incomeEnriched.setUser(user);
                    return incomeEnriched;
                })
                .defaultIfEmpty(incomeEnriched);
    }

    private Mono<IncomeEnriched> enrichCelebrant(IncomeEnriched incomeEnriched) {
        if (incomeEnriched.getCelebrantId() == null) {
            return Mono.just(incomeEnriched);
        }
        return userWebClient.getUserById(incomeEnriched.getCelebrantId())
                .map(celebrant -> {
                    incomeEnriched.setCelebrant(celebrant);
                    return incomeEnriched;
                })
                .defaultIfEmpty(incomeEnriched);
    }

    private Mono<IncomeEnriched> enrichPersonConfirmed(IncomeEnriched incomeEnriched) {
        if (incomeEnriched.getPersonConfirmedId() == null) {
            return Mono.just(incomeEnriched);
        }
        return userWebClient.getUserById(incomeEnriched.getPersonConfirmedId())
                .map(personConfirmed -> {
                    incomeEnriched.setPersonConfirmed(personConfirmed);
                    return incomeEnriched;
                })
                .defaultIfEmpty(incomeEnriched);
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

private Mono<Tuple2<User, User>> retrieveUsers(Income income, AdminIncomeDto adminDto) {
    Mono<User> personMono = userWebClient.getUserById(income.getPersonId());
    Mono<User> personConfirmedMono = Mono.empty();
    if (adminDto.getPersonConfirmedId() != null) {
        personConfirmedMono = userWebClient.getUserById(adminDto.getPersonConfirmedId());
    }
    return Mono.zip(personMono, personConfirmedMono);
}

private Mono<Income> updateIncomeDetails(Income income, AdminIncomeDto adminDto) {
    income.setComment(adminDto.getComment());
    income.setStatusPayment(adminDto.getStatusPayment());
    income.setPersonConfirmedId(adminDto.getPersonConfirmedId());
    income.setUpdatedAt(LocalDateTime.now());
    return incomeRepository.save(income);
}

private Mono<ResponseEntity<Income>> sendNotificationAndUpdateStatus(Income updatedIncome, User person, User personConfirmed) {
    return Flux.fromIterable(updatedIncome.getCategories())
            .flatMap(categoryId -> categoryRepository.findById(categoryId)
                    .map(category -> {
                        CategoryNotification categoryNotification = new CategoryNotification();
                        categoryNotification.setName(category.getName());
                        categoryNotification.setAmount(category.getAmount());
                        return categoryNotification;
                    })
                    .onErrorResume(e -> {
                        log.error("Error retrieving category with ID {}", categoryId, e);
                        return Mono.empty();
                    })
            )
            .collectList()
            .flatMap(categoryNotifications -> {
                NotificationEmail notificationEmail = new NotificationEmail();
                notificationEmail.setCorrelative(updatedIncome.getIncomeId());
                notificationEmail.setEmailUser(person != null ? person.getEmail() : "Unknown");
                notificationEmail.setUserName(person != null ? person.getFirstName() + " " + person.getLastName() : "Unknown");
                notificationEmail.setAdminName(personConfirmed != null ? personConfirmed.getFirstName() + " " + personConfirmed.getLastName() : "Unknown");
                notificationEmail.setCategories(categoryNotifications);
                notificationEmail.setComment(updatedIncome.getComment());
                notificationEmail.setStatusPayment(String.valueOf(updatedIncome.getStatusPayment()));

                return notificationWebClient.sendNotification(notificationEmail)
                        .flatMap(notificationResponse -> {
                            boolean statusNotification = notificationResponse.isSuccess();
                            updatedIncome.setStatusNotification(statusNotification);
                            return incomeRepository.save(updatedIncome)
                                    .map(finalUpdatedIncome -> new ResponseEntity<>(finalUpdatedIncome, HttpStatus.OK));
                        })
                        .doOnError(e -> log.error("Error sending notification", e))
                        .onErrorResume(e -> {
                            log.error("Error sending notification", e);
                            return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                        });
            });
}

public Mono<ResponseEntity<Income>> updateIncome(String incomeId, AdminIncomeDto adminDto) {
    log.info("EDITANDO INGRESO");

    return incomeRepository.findById(incomeId)
            .switchIfEmpty(Mono.error(new RuntimeException("Income not found")))
            .flatMap(income -> retrieveUsers(income, adminDto)
                    .flatMap(tuple -> {
                        User person = tuple.getT1();
                        User personConfirmed = tuple.getT2();
                        return updateIncomeDetails(income, adminDto)
                                .flatMap(updatedIncome -> sendNotificationAndUpdateStatus(updatedIncome, person, personConfirmed));
                    })
                    .onErrorResume(e -> {
                        log.error("Error updating income", e);
                        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                    })
            );
}



}
