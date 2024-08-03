package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationEmail {
    private String correlative;
    private String emailUser;
    private String userName;
    private String adminName;
    private List<CategoryNotification> categories;
    private String comment;
    private String statusPayment;
}
