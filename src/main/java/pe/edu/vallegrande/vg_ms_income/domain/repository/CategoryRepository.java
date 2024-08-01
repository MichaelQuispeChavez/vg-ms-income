package pe.edu.vallegrande.vg_ms_income.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;

@Repository
public interface CategoryRepository  extends ReactiveMongoRepository<Category, String> {
}