package pe.edu.vallegrande.vg_ms_income.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;

@Repository
public interface IncomeRepository extends ReactiveMongoRepository<Income, String> {

}