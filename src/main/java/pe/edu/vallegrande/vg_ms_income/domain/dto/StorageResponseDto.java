package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class StorageResponseDto {
    private String message;
    private String userCode;
    private String folderName;
    private String transactionCode;
    private List<String> filesUrl;
}