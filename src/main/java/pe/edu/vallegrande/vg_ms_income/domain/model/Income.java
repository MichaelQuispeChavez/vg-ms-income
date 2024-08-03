package pe.edu.vallegrande.vg_ms_income.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.edu.vallegrande.vg_ms_income.domain.dto.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "income")
public class Income {
    @Id
    private String incomeId;
    private String personId;
    private String celebrantId;
    private LocalDateTime dateEvent;
    private List<String> categories;
    private char type;
    private List<String> fileUrls;
    private String personConfirmedId;
    private String comment;
    private char statusPayment;
    private boolean statusNotification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User celebrant;
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User personConfirmed;
}