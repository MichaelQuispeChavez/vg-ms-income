package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserIncome {
    private String personId;
    private LocalDateTime dateEvent;
    private String categoryId;  
    private char type;
    private List<String> fileUrls;
    private char statusPayment;
    private boolean statusNotification;
    private String nameProof;
    private String proofId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}