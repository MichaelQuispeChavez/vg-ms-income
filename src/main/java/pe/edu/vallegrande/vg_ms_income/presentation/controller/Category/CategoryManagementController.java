package pe.edu.vallegrande.vg_ms_income.presentation.controller.Category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_income.application.service.CategoryService;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/management/income_category/v1")
public class CategoryManagementController {
    private final CategoryService categoryService;


    @PostMapping("/create")
    public Mono<Category> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

}