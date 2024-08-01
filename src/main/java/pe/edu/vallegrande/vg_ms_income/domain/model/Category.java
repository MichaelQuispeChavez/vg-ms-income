package pe.edu.vallegrande.vg_ms_income.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "income_category")
public class Category {
    @Id
    private String categoryId;
    private String name;
    private Double amount;
}