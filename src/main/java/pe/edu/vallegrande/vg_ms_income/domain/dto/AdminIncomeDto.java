package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminIncomeDto {
    private char statusPayment;
    private boolean statusNotification;
    private String comment;
    private String personConfirmedId;
}