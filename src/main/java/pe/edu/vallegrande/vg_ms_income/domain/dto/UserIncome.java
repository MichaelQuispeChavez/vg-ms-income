package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserIncome {
    private String personId;
    private String celebrantId;
    private LocalDateTime dateEvent;
    private List<String> categories;
    private char type;
    private List<String> fileUrls;
    private char statusPayment;
    private boolean statusNotification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}