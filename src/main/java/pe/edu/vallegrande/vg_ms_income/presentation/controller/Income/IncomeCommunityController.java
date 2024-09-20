package pe.edu.vallegrande.vg_ms_income.presentation.controller.Income;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_income.application.service.IncomeService;
import pe.edu.vallegrande.vg_ms_income.domain.dto.UserIncome;
import pe.edu.vallegrande.vg_ms_income.domain.model.Income;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("community/income/v1")
public class IncomeCommunityController {

    private final IncomeService incomeService;

    public IncomeCommunityController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Income>> createIncome(
        @ModelAttribute UserIncome userDto,
        @RequestParam("files") MultipartFile[] files) {
        return incomeService.createIncome(userDto, files)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

}
