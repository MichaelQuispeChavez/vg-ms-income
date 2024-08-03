package pe.edu.vallegrande.vg_ms_income.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_income.application.service.IncomeService;
import pe.edu.vallegrande.vg_ms_income.domain.dto.AdminIncomeDto;
import pe.edu.vallegrande.vg_ms_income.domain.dto.IncomeEnriched;
import pe.edu.vallegrande.vg_ms_income.domain.dto.UserIncome;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/income")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public Flux<IncomeEnriched> getAllIncome() {
        return incomeService.getAll();
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Income>> createIncome(@ModelAttribute UserIncome userDto,
            @RequestParam("files") MultipartFile[] files) {
        return incomeService.creatIncome(userDto, files)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PatchMapping("/update/{incomeId}")
    public Mono<ResponseEntity<Income>> updateIncome(@PathVariable String incomeId,
                                                     @RequestBody AdminIncomeDto adminDto) {
        return incomeService.updateIncome(incomeId, adminDto)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


}