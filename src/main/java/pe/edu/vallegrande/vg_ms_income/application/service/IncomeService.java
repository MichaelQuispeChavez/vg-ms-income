package pe.edu.vallegrande.vg_ms_income.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_income.application.webclient.NotificationWebClient;
import pe.edu.vallegrande.vg_ms_income.application.webclient.StorageWebClient;
import pe.edu.vallegrande.vg_ms_income.application.webclient.UserWebClient;
import pe.edu.vallegrande.vg_ms_income.domain.dto.AdminIncomeDto;
import pe.edu.vallegrande.vg_ms_income.domain.dto.CategoryNotification;
import pe.edu.vallegrande.vg_ms_income.domain.dto.NotificationEmail;
import pe.edu.vallegrande.vg_ms_income.domain.dto.User;
import pe.edu.vallegrande.vg_ms_income.domain.dto.UserIncome;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;
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
    private final StorageWebClient storageWebClient;
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    public Flux<Income> listAllIncomes() {
        return incomeRepository.findAll()
                .flatMap(income -> retrieveUsersList(income)
                        .flatMap(tuple -> {
                            income.setUser(tuple.getT1());
                            income.setPersonConfirmed(tuple.getT2());
                            return retrieveCategory(income)
                                    .map(category -> {
                                        income.setCategory(category);
                                        return income;
                                    })
                                    .defaultIfEmpty(income);
                        }));
    }

public Flux<Income> listIncomesByPersonId(String personId) {
    return incomeRepository.findByPersonId(personId) // Suponiendo que tienes este método en tu repositorio
            .flatMap(income -> retrieveUsersList(income)
                    .flatMap(tuple -> {
                        income.setUser(tuple.getT1());
                        income.setPersonConfirmed(tuple.getT2());
                        return retrieveCategory(income)
                                .map(category -> {
                                    income.setCategory(category);
                                    return income;
                                })
                                .defaultIfEmpty(income);
                    }));
}


    private Mono<Category> retrieveCategory(Income income) {
        if (income.getCategoryId() != null) {
            return categoryRepository.findById(income.getCategoryId());
        } else {
            return Mono.empty();
        }
    }

    public Mono<ResponseEntity<Income>> createIncome(UserIncome userDto, MultipartFile[] files) {
        Income income = buildIncomeFromDto(userDto);

        return storageWebClient.uploadFiles(files, FOLDER_NAME, income.getPersonId(), income.getIncomeId())
                .flatMap(urls -> {
                    income.setFileUrls(urls);
                    return incomeRepository.save(income);
                })
                .map(savedIncome -> new ResponseEntity<>(savedIncome, HttpStatus.CREATED))
                .onErrorResume(e -> {
                    log.error("Error creating income: ", e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    public Mono<ResponseEntity<Income>> updateIncome(String incomeId, AdminIncomeDto adminDto) {
        log.info("EDITANDO INGRESO");

        return incomeRepository.findById(incomeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Income not found")))
                .flatMap(income -> updateIncomeDetails(income, adminDto)
                        .flatMap(updatedIncome -> sendNotificationAndUpdateStatus(updatedIncome, adminDto)));
    }

    private Income buildIncomeFromDto(UserIncome userDto) {
        Income income = new Income();
        income.setIncomeId(UUID.randomUUID().toString());
        income.setPersonId(userDto.getPersonId());
        income.setDateEvent(userDto.getDateEvent());
        income.setCategoryId(userDto.getCategoryId());
        income.setType('I');
        income.setTypePayment(userDto.getTypePayment());
        income.setFileUrls(List.of());
        income.setStatusPayment(PENDING);
        income.setNameProof(userDto.getNameProof());
        income.setProofId(userDto.getProofId());
        income.setStatusNotification(false);
        income.setCreatedAt(LocalDateTime.now());
        income.setUpdatedAt(LocalDateTime.now());
        return income;
    }

    private Mono<Income> updateIncomeDetails(Income income, AdminIncomeDto adminDto) {
        income.setComment(adminDto.getComment());
        income.setStatusPayment(adminDto.getStatusPayment());
        income.setPersonConfirmedId(adminDto.getPersonConfirmedId());
        income.setUpdatedAt(LocalDateTime.now());
        return incomeRepository.save(income);
    }

    private Mono<ResponseEntity<Income>> sendNotificationAndUpdateStatus(Income income, AdminIncomeDto adminDto) {
        return retrieveUsers(income, adminDto)
                .flatMap(tuple -> {
                    User person = tuple.getT1();
                    User personConfirmed = tuple.getT2();

                    return buildNotificationEmail(income, person, personConfirmed)
                            .flatMap(notificationEmail -> notificationWebClient.sendNotification(notificationEmail))
                            .flatMap(notificationResponse -> {
                                income.setStatusNotification(notificationResponse.isSuccess());
                                return incomeRepository.save(income)
                                        .map(updatedIncome -> new ResponseEntity<>(updatedIncome, HttpStatus.OK));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error updating income: ", e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    private Mono<Tuple2<User, User>> retrieveUsersList(Income income) {
        User emptyUser = new User();

        Mono<User> personMono = income.getPersonId() != null ? userWebClient.getUserById(income.getPersonId())
                : Mono.just(emptyUser);

        Mono<User> personConfirmedMono = income.getPersonConfirmedId() != null
                ? userWebClient.getUserById(income.getPersonConfirmedId())
                : Mono.just(emptyUser);

        return Mono.zip(personMono, personConfirmedMono);
    }

    private Mono<Tuple2<User, User>> retrieveUsers(Income income, AdminIncomeDto adminDto) {
        Mono<User> personMono = userWebClient.getUserById(income.getPersonId());
        Mono<User> personConfirmedMono = adminDto.getPersonConfirmedId() != null
                ? userWebClient.getUserById(adminDto.getPersonConfirmedId())
                : Mono.empty();
        return Mono.zip(personMono, personConfirmedMono);
    }

    private Mono<NotificationEmail> buildNotificationEmail(Income income, User person, User personConfirmed) {
        NotificationEmail notificationEmail = new NotificationEmail();
        notificationEmail.setCorrelative(income.getIncomeId());
        notificationEmail.setEmailUser(person != null ? person.getEmail() : "Unknown");
        notificationEmail.setUserName(person != null ? person.getFirstName() + " " + person.getLastName() : "Unknown");
        notificationEmail.setAdminName(personConfirmed != null
                ? personConfirmed.getFirstName() + " " + personConfirmed.getLastName()
                : "Unknown");
        notificationEmail.setComment(income.getComment());
        notificationEmail.setStatusPayment(String.valueOf(income.getStatusPayment()));

        return categoryRepository.findById(income.getCategoryId())
                .map(category -> {
                    List<CategoryNotification> categoryNotifications = new ArrayList<>();
                    if (category != null) {
                        CategoryNotification categoryNotification = new CategoryNotification();
                        categoryNotification.setName(category.getName());
                        categoryNotification.setAmount(category.getAmount());
                        categoryNotifications.add(categoryNotification);
                    }
                    notificationEmail.setCategories(categoryNotifications);
                    return notificationEmail;
                })
                .defaultIfEmpty(notificationEmail);
    }

}
