package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IncomeEnriched {
    private String incomeId;
    private String personId;
    private String celebrantId;
    private LocalDateTime dateEvent;
    private List<Category> categories;
    private char type;
    private List<String> fileUrls;
    private String personConfirmedId;
    private String comment;
    private char statusPayment;
    private boolean statusNotification;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}