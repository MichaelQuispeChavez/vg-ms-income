package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

@Data
public class User {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
}
