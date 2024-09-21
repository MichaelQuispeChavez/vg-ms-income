package pe.edu.vallegrande.vg_ms_income.presentation.controller.Income;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_income.application.service.IncomeService;
import pe.edu.vallegrande.vg_ms_income.domain.dto.AdminIncomeDto;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("management/income/v1")
public class IncomeManagementController {
    private final IncomeService incomeService;

    public IncomeManagementController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }
    
    @GetMapping("/list")
    public Flux<Income> getAllIncome() {
        return incomeService.listAllIncomes();
    }

    
    @PatchMapping("/update/{incomeId}")
    public Mono<ResponseEntity<Income>> updateIncome(@PathVariable String incomeId,
                                                     @RequestBody AdminIncomeDto adminDto) {
        return incomeService.updateIncome(incomeId, adminDto)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

}
