package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminIncomeDto {
    private String personConfirmedId;
    private List<String> fileUrls;
    private char statusPayment;
    private boolean statusNotification;
    private String comment;
}