package pe.edu.vallegrande.vg_ms_income.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_income.application.service.CategoryService;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/income_category")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Flux<Category> getAllAccountings() {
        return categoryService.getAll();
    }

    @PostMapping("/create")
    public Mono<Category> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

}