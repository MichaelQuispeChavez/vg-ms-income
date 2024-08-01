package pe.edu.vallegrande.vg_ms_income.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class StorageResponseDto {
    private List<String> filesUrl;
}