package pe.edu.vallegrande.vg_ms_income.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;
import reactor.core.publisher.Flux;

@Repository
public interface IncomeRepository extends ReactiveMongoRepository<Income, String> {
    Flux<Income> findByPersonId(String personId); // Método para encontrar ingresos por personId
}
